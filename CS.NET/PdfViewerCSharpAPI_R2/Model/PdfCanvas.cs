using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows.Media.Imaging;
using System.Windows;
using System.Collections.Concurrent;
using PdfTools.PdfViewerCSharpAPI.Utilities;
using PdfTools.PdfViewerCSharpAPI.DocumentManagement;

namespace PdfTools.PdfViewerCSharpAPI.Model
{
    class PdfCanvas : IPdfCanvas
    {
        private enum TCanvasDirtyness { Clean, RectsDirty, CanvasDirty };

        IPdfDocumentManager documentManager;

        public PdfCanvas(IPdfControllerCallbackManager controller)
        {
            Rotation = 0;
            _canvasRect = new PdfSourceRect();
            documentManager = new PdfDocumentManagerMultithreaded(controller);
            pagesToLoadExactly = new SortedSet<int>();
            newlyLoadedPages = new SortedSet<int>();
            this.controller = controller;
            controller.VisiblePageRangeChanged += OnVisiblePageRangeChanged;
            documentManager.PageRectLoaded += OnPageLoaded;
        }

        public IPdfDocumentManager DocumentManager
        {
            get
            {
                return documentManager;
            }
        }

        public void Dispose()
        {
            Dispose(true);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (disposing)
            {
                documentManager.Dispose();
            }

        }

        /// <summary>
        /// Is true if the canvasRectangle needs to be updated
        /// </summary>
        private TCanvasDirtyness canvasDirty = TCanvasDirtyness.CanvasDirty;
        private IPdfControllerCallbackManager controller;

        private TPageLayoutMode _pageLayoutMode;
        public TPageLayoutMode PageLayoutMode
        {
            set
            {
                if (_pageLayoutMode != value)
                {
                    _pageLayoutMode = value;
                    canvasDirty = TCanvasDirtyness.CanvasDirty;
                }
            }
            private get
            {
                return _pageLayoutMode;
            }
        }

        private double _borderSize;
        public double BorderSize
        {
            get
            {
                return _borderSize;
            }
            set
            {
                if (_borderSize != value)
                {
                    _borderSize = value;
                    canvasDirty = TCanvasDirtyness.CanvasDirty;
                }
            }
        }

        private int _rotation;
        public int Rotation
        {
            get
            {
                return _rotation;
            }
            set
            {
                value = (value + 360) % 360;
                if (value % 90 == 0)
                {
                    _rotation = value;
                    canvasDirty = TCanvasDirtyness.CanvasDirty;
                    return;
                }
            }
        }

        public void Open(string filename, byte[] fileMem, string password)
        {
            //open the document
            documentManager.Close(true);
            PdfTools.PdfViewerCSharpAPI.DocumentManagement.Requests.PdfOpenRequest r = documentManager.Open(filename, fileMem, password);
            r.Completed += delegate(DocumentManagement.Requests.APdfRequest<DocumentManagement.Requests.OpenArguments, object>.InOutTuple o, PdfViewerException ex)
            {
                if (ex != null)
                {
                    controller.OnOpenCompleted(ex);
                    return;
                }
                //read the document pages
                canvasDirty = TCanvasDirtyness.CanvasDirty;
                Logger.LogInfo("Clearing loadExactly pages");
                pagesToLoadExactly.Clear();
                controller.OnOpenCompleted(null);
            };
        }

        public void Close()
        {
            documentManager.Close(true);
        }

        private PdfSourceRect _canvasRect;
        public PdfSourceRect CanvasRect
        {
            get
            {
                switch (PdfUtils.PageLayoutScrollingEnabled(PageLayoutMode))
                {
                    case true:
                        CalculateCanvas();
                        return _canvasRect;
                    default:
                        PdfSourceRect rect = GetUnionRectangleWithNeighbour(Math.Min(PageNo, PageCount));
                        rect.ExtendToBeVerticallySymetrical();
                        return rect;
                }
            }
            protected set { _canvasRect = value; }
        }

        private int _pageNo;
        public int PageNo
        {
            protected get { return _pageNo; }
            set
            {
                _pageNo = value;
                if (!PdfUtils.PageLayoutScrollingEnabled(PageLayoutMode))
                {
                    canvasDirty = TCanvasDirtyness.CanvasDirty;
                }
            }
        }

        private PdfTextFragment FindNearestTextFragment(PdfSourcePoint location, List<PdfTextFragment> haystack, PdfSourceRect pageRect)
        {
            /* To use This, you have to invert y axis of if-checks
            int needle = haystack.Count / 2; //the fragment we currently look at
            int start = 0;
            int end = haystack.Count - 1;
            while (true)
            {
                PdfSourceRect rect = haystack[needle].RectOnUnrotatedPage;

                if (location.dY < rect.dY)
                    end = haystack.IndexOf(haystack[needle].LastOnLine);
                else if (location.dY > rect.dBottom)
                    start = haystack.IndexOf(haystack[needle].FirstOnLine);
                else
                {
                    start = haystack.IndexOf(haystack[needle].FirstOnLine);
                    end = haystack.IndexOf(haystack[needle].LastOnLine);
                    break;
                }
                int s = haystack.IndexOf(haystack[start].LastOnLine) + 1;
                int t = haystack.IndexOf(haystack[end].FirstOnLine) - 1;
                if (s > t)
                    break;
                needle = (t - s) / 2 + s;
            }
            //there are only two ways to reach this:
            // * haystack[needle] is on the correct line (and thus [start,end] interval describes this one line
            // * [start,end] interval spans 2 lines or less
            haystack = haystack.GetRange(start, end - start + 1);
            */
            //figure out the closest one
            double minDist = double.MaxValue;
            PdfTextFragment minDistFrag = null;
            foreach (PdfTextFragment frag in haystack)
            {
                double dist = frag.RectOnUnrotatedPage.ShortestDistanceSquared(location);
                if (dist < minDist)
                {
                    minDist = dist;
                    minDistFrag = frag;
                }
            }
            return minDistFrag;
        }

        public IList<PdfTextFragment> GetTextWithinSelection(PdfSourcePoint start, PdfSourcePoint end, int firstPage, int lastPage, ref bool swap)
        {
            IList<PdfTextFragment> containedFragments = new List<PdfTextFragment>();
            IList<int> pageRange = Enumerable.Range(firstPage, lastPage - firstPage + 1).ToList();
            //intersect the markedRect with the textFragments (transformed to canvas coordinates)
            foreach (int page in pageRange)
            {
                List<PdfTextFragment> frags;
                try
                {
                    frags = (List<PdfTextFragment>)documentManager.GetTextFragments(page);
                }catch(PdfRequestCanceledException)
                {
                    Logger.LogWarning("Request to load textfragments was canceled somehow (that is not supposed to happen)");
                    continue;
                }
                if (frags.Count == 0)
                    continue;
                PdfTextFragment firstFrag = frags.First();
                PdfTextFragment lastFrag = frags.Last();
                if (page == firstPage)
                {
                    firstFrag = FindNearestTextFragment(start, frags, GetPageRect(page));
                }
                if (page == lastPage)
                {
                    lastFrag = FindNearestTextFragment(end, frags, GetPageRect(page));
                }
                if (firstFrag == frags.First() && lastFrag == frags.Last())
                {
                    containedFragments = containedFragments.Concat(frags).ToList();
                }
                else
                {
                    int firstIndex = frags.IndexOf(firstFrag);
                    int lastIndex = frags.IndexOf(lastFrag);
                    if (firstIndex > lastIndex) //first and last are swapped
                    {
                        swap = true;
                        containedFragments = containedFragments.Concat(frags.GetRange(lastIndex, firstIndex - lastIndex + 1)).ToList();
                    }
                    else
                    {
                        swap = false;
                        containedFragments = containedFragments.Concat(frags.GetRange(firstIndex, lastIndex - firstIndex + 1)).ToList();
                    }
                }
            }
            return containedFragments;
        }

        public IList<PdfTextFragment> GetTextWithinPageRange(int firstPage, int lastPage)
        {
            return GetTextWithinRegion(new PdfSourceRect(), firstPage, lastPage);
        }

        public IList<PdfTextFragment> GetTextWithinRegion(PdfSourceRect markedRect, int firstPage, int lastPage)
        {
            IList<PdfTextFragment> containedFragments = new List<PdfTextFragment>();
            IList<int> pageRange = Enumerable.Range(firstPage, lastPage - firstPage + 1).ToList();
            //intersect the markedRect with the textFragments (transformed to canvas coordinates)
            foreach (int page in pageRange)
            {
                IList<PdfTextFragment> textFragments;
                try
                {
                    textFragments = documentManager.GetTextFragments(page);
                }
                catch(PdfRequestCanceledException)
                {
                    continue;
                }
                foreach (PdfTextFragment frag in textFragments)
                {
                    PdfSourceRect rectOnCanvas = frag.RectOnUnrotatedPage.CalculateRectOnCanvas(GetPageRect(page), Rotation);
                    if (markedRect.IsEmpty || rectOnCanvas.intersectsDouble(markedRect))
                        containedFragments.Add(frag);
                }
            }
            return containedFragments;
        }


        public int PageCount
        {
            get
            {
                return documentManager.PageCount;
            }
        }

        public IList<int> PageOrder
        {
            get { return documentManager.PageOrder; }
            set 
            { 
                documentManager.PageOrder = value;
                canvasDirty = TCanvasDirtyness.CanvasDirty;
                CalculateCanvas();
            }
        }

        public IList<int> InversePageOrder
        {
            get { return documentManager.InversePageOrder;  }
        }

        public PdfSourceRect GetPageRect(int pageNumber)
        {
            CalculateCanvas();
            PdfSourceRect rect = documentManager.GetPageRectGuess(pageNumber);
            return rect;
        }
        public PdfSourceRect GetPageRectGuaranteedExactly(int pageNumber)
        {
            if (canvasDirty != TCanvasDirtyness.CanvasDirty)
                canvasDirty = TCanvasDirtyness.RectsDirty;
            Logger.LogInfo("Adding page " + pageNumber + " to be loaded exactly");
            pagesToLoadExactly.Add(pageNumber);
            CalculateCanvas();
            return documentManager.GetPageRect(pageNumber);
        }

        public PdfSourceRect GetUnionRectangleWithNeighbour(int pageNumber)
        {
            CalculateCanvas();
            PdfSourceRect pageRect = documentManager.GetPageRectGuess(pageNumber);
            PdfSourceRect pageRect2;
            if (PdfUtils.HorizontalPagePosition(PageLayoutMode, pageNumber) == 1)
            {
                pageRect2 = documentManager.GetPageRectGuess(pageNumber - 1);
                pageRect = pageRect.unionDouble(pageRect2);
            }
            else if (PdfUtils.HorizontalPagePosition(PageLayoutMode, pageNumber) == -1 && pageNumber + 1 < PageCount)
            {
                //unify with right neighbour if it exists
                pageRect2 = documentManager.GetPageRectGuess(pageNumber + 1);
                pageRect = pageRect.unionDouble(pageRect2);
            }
            else
                pageRect = pageRect.Clone();
            pageRect.dX -= BorderSize;
            pageRect.dWidth += 2.0 * BorderSize;
            pageRect.dY -= BorderSize;
            pageRect.dHeight += 2.0 * BorderSize;

            return pageRect;
        }

        private void OnVisiblePageRangeChanged(int firstPage, int lastPage)
        {
            if (canvasDirty != TCanvasDirtyness.CanvasDirty)
                canvasDirty = TCanvasDirtyness.RectsDirty;
            for (int i = firstPage; i <= lastPage; i++)
            {
                pagesToLoadExactly.Add(i);
                Logger.LogInfo("Adding page " + i + " to be loaded exactly");
            }
            //Console.WriteLine("Visible Pages: {0} - {1}", firstPage, lastPage);
        }

        private void OnPageLoaded(int newPage)
        {
            lock (newlyLoadedPagesLock)
            {
                canvasDirty = TCanvasDirtyness.RectsDirty;
                newlyLoadedPages.Add(newPage);
            }
        }

        private Object newlyLoadedPagesLock = new Object();
        private SortedSet<int> newlyLoadedPages;
        private SortedSet<int> pagesToLoadExactly;

        private double GetPreviousBottom(int pageNo, int start, int end)
        {
            if (pageNo <= start)
                return 0.0;
            return documentManager.GetPageRectGuess(pageNo - 1).dBottom;
        }
        private double GetNextTop(int pageNo, int start, int end)
        {
            if (pageNo >= end)
                return _canvasRect.dBottom;
            return documentManager.GetPageRectGuess(pageNo + 1).dY;
        }



        private int GetNextRelevantPage(int start, int end)
        {
            int p1;
            lock (newlyLoadedPagesLock)
            {
                p1 = newlyLoadedPages.FirstOrDefault<int>(number => number >= start && number <= end);
            }
            int p2 = pagesToLoadExactly.FirstOrDefault<int>(number => number >= start && number <= end);
            p1 = (p1 < 1) ? int.MaxValue : p1;
            p2 = (p2 < 1) ? int.MaxValue : p2;
            return Math.Min(p1, p2);
        }

        private void CalculateCanvas()
        {
            if (canvasDirty == TCanvasDirtyness.Clean)
            {
                return;
            }

            int start = 1, end = PageCount;

            //If we are in a mode which doesnt show all pages, we only need to go through the shown ones
            if (!PdfUtils.PageLayoutScrollingEnabled(PageLayoutMode))
            {
                switch (PdfUtils.HorizontalPagePosition(PageLayoutMode, PageNo))
                {
                    case -1:
                        {
                            start = PageNo;
                            end = Math.Min(PageNo + 1, end);//because PageNo + 1 doesn't necessarily exist in this case
                            break;
                        }
                    case 0:
                        {
                            start = PageNo;
                            end = PageNo;
                            break;
                        }
                    case 1:
                        {
                            start = PageNo - 1; //pageNo-1 always exists in this case
                            end = PageNo;
                            break;
                        }
                }
            }

            int changedPage = int.MaxValue;
            PdfSourceRect oldCanvasRect = _canvasRect.Clone();
            bool entireCanvasDirty = (canvasDirty == TCanvasDirtyness.CanvasDirty);
            canvasDirty = TCanvasDirtyness.Clean;
            if (entireCanvasDirty)
            {
                _canvasRect.dWidth = 0.0;
                _canvasRect.dX = 0.0;
            }
            double offset = 0.0;
            int page = entireCanvasDirty ? start : Math.Max(start, GetNextRelevantPage(start, end));
            for (; page <= end; page++)
            {
                PdfSourceRect rect = null;
                if (pagesToLoadExactly.Remove(page))
                {
                    Logger.LogInfo("page " + page + " was loaded exactly");
                    //load page exactly
                    rect = documentManager.GetPageRect(page);
                }
                else
                {
                    rect = documentManager.GetPageRectGuess(page);
                }
                int nPage;
                lock (newlyLoadedPagesLock)
                {
                    nPage = newlyLoadedPages.FirstOrDefault(number => number >= start && number <= end);
                }
                if (!entireCanvasDirty && nPage > 1 && nPage < page)
                {
                    //maybe the getting of pages has getted a page before this one (due to predictionAlgorithm). In this case, consider this page first
                    page = nPage;
                    rect = documentManager.GetPageRectGuess(page);
                }

                bool removed = false;
                lock (newlyLoadedPagesLock)
                {
                    removed = newlyLoadedPages.Remove(page);
                }
                if (removed || entireCanvasDirty)//If the page is newly loaded, calculate its position exactly. Also if the entire canvas is dirty, calculate always
                {
                    changedPage = Math.Min(changedPage, page);
                    //position the page newly, knowing that its the first time we know its exact size
                    switch (PdfUtils.HorizontalPagePosition(PageLayoutMode, page))
                    {
                        case 0:
                            {
                                //place 1 page
                                rect.dCenterX = 0.0;
                                rect.dY = GetPreviousBottom(page, start, end) + _borderSize;
                                if (rect.dX - _borderSize < _canvasRect.dX)
                                {
                                    _canvasRect.dWidth += _canvasRect.dX - (rect.dX - _borderSize);
                                    _canvasRect.dX = rect.dX - _borderSize;
                                }
                                if (rect.dRight + _borderSize > _canvasRect.dRight)
                                {
                                    _canvasRect.dWidth += rect.dRight + _borderSize - _canvasRect.dRight;
                                }
                                offset = rect.dBottom + _borderSize - GetNextTop(page, start, end);
                                break;
                            }
                        case -1:
                            {
                                //place left page
                                rect.dRight = 0.0 - _borderSize / 2.0;
                                rect.dY = Math.Max(GetPreviousBottom(page, start, end), GetPreviousBottom(page - 1, start, end)) + _borderSize;
                                if (rect.dX - _borderSize < _canvasRect.dX)
                                {
                                    _canvasRect.dWidth += _canvasRect.dX - (rect.dX - _borderSize);
                                    _canvasRect.dX = rect.dX - _borderSize;
                                }
                                offset = rect.dY - GetNextTop(page, start, end);
                                break;
                            }
                        default:
                            {
                                //place right page
                                rect.dX = 0.0 + _borderSize / 2.0;
                                rect.dY = documentManager.GetPageRectGuess(page - 1).dY;
                                if (rect.dRight + _borderSize > _canvasRect.dRight)
                                {
                                    _canvasRect.dWidth += rect.dRight + _borderSize - _canvasRect.dRight;
                                }
                                offset = Math.Max(GetPreviousBottom(page, start, end), rect.dBottom) + _borderSize - GetNextTop(page, start, end);
                                break;
                            }
                    }
                }
                else
                {
                    //Only pages that are not newly loaded make it here. Adjust their offset
                    if (offset == 0.0)
                    {
                        //if there is no offset, we dont need to do anything until we hit a page that needs to be loaded anew
                        page = GetNextRelevantPage(start, end);
                        if (page == int.MaxValue)
                            break;//there is no more page to load -> stop iterating through pages
                        page--;
                        continue;
                    }
                    rect.Offset(0.0, offset);
                }
            }//end for-loop
            //we have loaded and positioned all pages, now we only need to resize the canvas
            lock (newlyLoadedPagesLock)
            {
                if (newlyLoadedPages.Count == 0 && pagesToLoadExactly.Count == 0)
                    canvasDirty = TCanvasDirtyness.Clean;
            }
            if (PdfUtils.HorizontalPagePosition(PageLayoutMode, end) == -1)
                offset = documentManager.GetPageRectGuess(end).dBottom + _borderSize - _canvasRect.dBottom;
            if (offset != 0.0)
            {
                _canvasRect.dHeight += offset;
                CanvasRectChanged(oldCanvasRect, _canvasRect, changedPage);
            }
            return;
        }

        public event Action<PdfSourceRect, PdfSourceRect, int> CanvasRectChanged;
    }
}
