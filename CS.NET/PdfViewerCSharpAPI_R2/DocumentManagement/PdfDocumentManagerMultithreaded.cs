/****************************************************************************
 *
 * File:            PdfDocumentManagerDummy.cs
 *
 * Description:     The 3-Heights™  Pdf Viewer Control for WPF
 *
 * Copyright:       Copyright (C) 2015 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *                  
 ***************************************************************************/

using PdfTools.PdfViewerCSharpAPI.Annotations;

namespace PdfTools.PdfViewerCSharpAPI.DocumentManagement
{
    using System;
    using System.Collections.Generic;
    using System.Collections.Concurrent;
    using System.Linq;
    using System.Text;
    using System.Windows.Media.Imaging;
    using System.Threading;
    using PdfTools.PdfViewerCSharpAPI.Utilities;
    using PdfTools.PdfViewerCSharpAPI.Model;
    using PdfTools.PdfViewerCSharpAPI.DocumentManagement.Requests;

    /// <summary>
    /// Implements a document manager which uses a separate background thread, which executes Open, Close and Draw commands on the document.
    /// A requestqueue is used to queue up multiple commands.
    /// </summary>
    public class PdfDocumentManagerMultithreaded : IPdfDocumentManager, IDisposable
    {
        private SynchronisedGenericPriorityQueue requestQueue;
        private IPdfDocument document;
        private IPdfControllerCallbackManager controller;
        private Thread rendererWorker;
        private bool rendererWorkerRunning = true;


        private struct PageCacheParams
        {
            public PageCacheParams(int rot, double zoom)
            {
                rotation = rot;
                zoomFactor = zoom;
            }
            public int rotation;
            public double zoomFactor;
        }
        private IGenericCache<int, PdfSourceRect, PageCacheParams> pageCache = null;
        private PageCacheParams pageCacheParams = new PageCacheParams(0, 0.0);

        private IGenericCache<int, IList<PdfTextFragment>, Object> textFragmentCache = null;
        
        private IGenericCache<ThumbnailCacheArgs, WriteableBitmap, Object> thumbnailCache = null;
        
        
        public PdfDocumentManagerMultithreaded(IPdfControllerCallbackManager controller)
        {
            //Utilities.Logger.FileName = @"P:\temp\wpflog.log";
            requestQueue = new SynchronisedGenericPriorityQueue();
            document = new PdfDocument();
            pageCache = new AlwaysRememberCache<int, PdfSourceRect, PageCacheParams>(GetPageFromSourceAsync);
            pageCache.FirstLoadParametrizer = PageCacheFirstLoadParametrizer;
            pageCache.ObjectEquatorDelegate = PageCacheEquator;
            pageCache.ParametrizationChanger = PageCacheParametrizationChanger;
            pageCache.ChangeParametrization(new PageCacheParams(0, 1.0));
            pageCache.PredictionAlgorithm = PageCachePredictionAlgorithm;
            PageCacheGuessGenerator pageCacheGuessGenerator = new PageCacheGuessGenerator();
            pageCache.GuessGenerator = pageCacheGuessGenerator;
            textFragmentCache = new AlwaysRememberCache<int, IList<PdfTextFragment>, Object>(GetTextFragmentsFromSource);
            thumbnailCache = new AlwaysRememberCache<ThumbnailCacheArgs, WriteableBitmap, Object>(LoadThumbnailFromSource);
            this.controller = controller;
            controller.RotationChanged += RotationChangedEventHandler;
            controller.ZoomChanged += ZoomFactorChangedEventHandler;
            rendererWorker = new Thread( new ThreadStart(WorkerRun));
            rendererWorker.Start();
        }

        public void Dispose()
        {
            Dispose(true);
        }

        protected virtual void Dispose(bool disposing)
        {
            rendererWorkerRunning = false;
            requestQueue.Dispose();
            if (!rendererWorker.Join(500))
                rendererWorker.Abort();
            document.Dispose();
        }

        private void PageCacheFirstLoadParametrizer(PdfSourceRect rect, PageCacheParams param)
        {
            if (param.rotation % 180 != 0)
                rect.RotateSize();
        }

        private bool PageCacheEquator(PdfSourceRect rect1, PdfSourceRect rect2)
        {
            return rect1.Equals(rect2);
        }


        private void PageCacheParametrizationChanger(PdfSourceRect rect, PageCacheParams oldParam, PageCacheParams newParam)
        {
            if ((newParam.rotation - oldParam.rotation) % 180 != 0)
                rect.RotateSize();
        }

        private IList<int> PageCachePredictionAlgorithm(int pageToLoad)
        {
            int start = (_pageCacheSlidingWindowSize <= 0) ? 1 : Math.Max(1, pageToLoad - _pageCacheSlidingWindowSize / 2);
            int end = (_pageCacheSlidingWindowSize <= 0) ? PageCount : Math.Min(Math.Max(PageCount, pageToLoad), pageToLoad + _pageCacheSlidingWindowSize) - start + 1;
            IList<int> pageRange = Enumerable.Range(start, end).ToList<int>();
            pageRange.Remove(pageToLoad);
            return pageRange;
        }

        private class PageCacheGuessGenerator : IGuessGenerator<int, PdfSourceRect>
        {
            public PdfSourceRect GenerateGuess(IDictionary<int, PdfSourceRect> dict)
            {
                /* this solution doesnt set a zoomfactor, which is an issue      
                 * if (guess != null)
                                    return guess;
                                widthSum = 0.0;
                                heightSum = 0.0;
                                foreach (PdfRect rect in dict.Values)
                                {
                                    widthSum += rect.dWidth;
                                    heightSum += rect.dHeight;
                                }
                                guess = new PdfRect(0.0, 0.0, widthSum / (double)dict.Count, heightSum / (double)dict.Count);
                                return guess;
                 * }
                 * double widthSum, heightSum;
                  */
                if (guess != null)
                    return guess.Clone();
                rectCount = new Dictionary<PdfSourceRect, int>();
                KeyValuePair<PdfSourceRect, int> max = new KeyValuePair<PdfSourceRect, int>(null, -1);
                foreach (PdfSourceRect rect in dict.Values)
                {
                    if (rectCount.ContainsKey(rect))
                        rectCount[rect]++;
                    else
                        rectCount.Add(rect, 1);

                    if(max.Value < rectCount[rect])
                        max = new KeyValuePair<PdfSourceRect, int>(rect, rectCount[rect]);
                }
                guess = max.Key.Clone();
                return guess;
            }

            public void InvalidateGuess()
            {
                guess = null;
            }

            IDictionary<PdfSourceRect, int> rectCount;
            PdfSourceRect guess;
        }

        private void OnOpenCompletedEventHandler(APdfRequest<OpenArguments, object>.InOutTuple o, PdfViewerException ex)
        {
            if (ex == null)
            {
                // Default page order - normal behaviour
                _pageOrder = new List<int>(Enumerable.Range(1, document.PageCount));
                InversePageOrder = _pageOrder;
            }
            else
            {
                // opening request failed doing nothing
            }
        }

       private void OnCloseCompletedEventHandler(APdfRequest<bool, object>.InOutTuple o, PdfViewerException ex)
        {
            if (ex == null)
            {
                // invalidate caches
                pageCache.InvalidateCache();
                textFragmentCache.InvalidateCache();
                thumbnailCache.InvalidateCache();
            }
            else
            {
                // closing failed. keep caches
            }
        }

        private void ZoomFactorChangedEventHandler(double newZoomFactor)
        {
            pageCacheParams.zoomFactor = newZoomFactor;
            pageCache.ChangeParametrization(pageCacheParams);
        }
        private void RotationChangedEventHandler(int newRotation)
        {
            pageCacheParams.rotation = newRotation;
            pageCache.ChangeParametrization(pageCacheParams);
        }

        private void WorkerRun()
        {
            try
            {
                //System.Diagnostics.Stopwatch stopwatch = new System.Diagnostics.Stopwatch();
                IPdfRequest request;
                while (rendererWorkerRunning)
                {
                    //stopwatch.Restart();
                    request = requestQueue.Pop();
                    //Console.WriteLine("Waited for {0} milliseconds", stopwatch.ElapsedMilliseconds);
                    //stopwatch.Restart(); 
                    if (request != null)
                    {
                        try
                        {
                            request.Execute(document, controller);
                        }
                        catch (PdfNoFileOpenedException e)
                        {
                            Console.WriteLine(e.ToString());
                        }
                    }
                    //Console.WriteLine("Executed {0} for {1} milliseconds", request.ToString(), stopwatch.ElapsedMilliseconds);
                }
            }
            catch (ThreadInterruptedException)
            {
                return;
            }
        }

        

        public IList<PdfTextFragment> GetTextFragments(int page)
        {
            return textFragmentCache.Get(_pageOrder[page-1]);
        }


        public PdfOpenRequest Open(string filename, byte[] fileMem, string password)
        {
            PdfOpenRequest request = new PdfOpenRequest(new OpenArguments(filename, fileMem, password));
            request.Completed += OnOpenCompletedEventHandler;
            requestQueue.Add(request);
            pageCache.InvalidateCache();
            textFragmentCache.InvalidateCache();
            thumbnailCache.InvalidateCache();
            pageCacheParams.rotation = 0;
            pageCacheParams.zoomFactor = 1.0;
            pageCache.ChangeParametrization(pageCacheParams);
            return request;
        }

        public PdfCloseRequest Close(bool triggerCallback)
        {
            PdfCloseRequest request = new PdfCloseRequest(triggerCallback);
            request.Completed += OnCloseCompletedEventHandler;
            requestQueue.Add(request);
            return request;
        }

        private IPdfRequest lastDrawRequest = null;
        // source and targetrects may be empty! (due to crops with pages etc.)
        public PdfDrawRequest Draw(int width, int height, Resolution resolution, int rotation, IDictionary<int, PdfSourceRect> pageRects, PdfViewerController.Viewport viewport)
        {
            IList<KeyValuePair<int, PdfSourceRect>> newPageRects = new List<KeyValuePair<int, PdfSourceRect>>();
            
            foreach (int key in pageRects.Keys)
            {
                int newKey;
                if (key - 1 < _pageOrder.Count)
                    newKey = _pageOrder[key - 1];
                else
                    continue;
                
                if (newKey > document.PageCount)
                    continue;
                newPageRects.Add(new KeyValuePair<int, PdfSourceRect>(newKey, pageRects[key]));
            }

            PdfDrawRequest request = new PdfDrawRequest(new DrawArgs(width, height, resolution, rotation, newPageRects, viewport));
            if (lastDrawRequest == null)
                requestQueue.Add(request);
            else
                requestQueue.Replace(lastDrawRequest, request);
            lastDrawRequest = request;
            //add new draw request
            return request;
        }

        public PdfGetOutlinesRequest RequestOutlines(int outlineId)
        {
            PdfGetOutlinesRequest request = new PdfGetOutlinesRequest(outlineId);
            requestQueue.Add(request);
            return request;
        }

        public APdfRequest<ThumbnailCacheArgs, WriteableBitmap> RequestThumbnail(ThumbnailCacheArgs args)
        {
            return thumbnailCache.GetAsync(args);
        }
        private PdfLoadThumbnailRequest LoadThumbnailFromSource(ThumbnailCacheArgs args)
        {
            PdfLoadThumbnailRequest request = new PdfLoadThumbnailRequest(args);
            requestQueue.Add(request, true);
            return request;
        }

        public WriteableBitmap GetThumbnail(ThumbnailCacheArgs args)
        {
            return thumbnailCache.Get(args);
        }


        public void CancelRequest(IPdfRequest request)
        {
            requestQueue.CancelRequest(request);
            if (request.GetType().IsInstanceOfType(typeof(PdfLoadThumbnailRequest)))
                thumbnailCache.CancelPendingRequest((PdfLoadThumbnailRequest)request);
            else if (request.GetType().IsInstanceOfType(typeof(PdfGetPageRectRequest)))
                pageCache.CancelPendingRequest((PdfGetPageRectRequest)request);
            else if (request.GetType().IsInstanceOfType(typeof(PdfGetTextFragmentsRequest)))
                textFragmentCache.CancelPendingRequest((PdfGetTextFragmentsRequest)request);
            request.Cancel();
        }

        public PdfGetPageLayoutRequest RequestPageLayout()
        {
            PdfGetPageLayoutRequest request = new PdfGetPageLayoutRequest();
            requestQueue.Add(request);
            return request;
        }

        public PdfGetOpenActionDestinationRequest RequestOpenActionDestination()
        {
            PdfGetOpenActionDestinationRequest request = new PdfGetOpenActionDestinationRequest();
            requestQueue.Add(request);
            return request;
        }

        #region [InkingForPDF] Annotation Methods

        public PdfCreateAnnotationRequest CreateAnnotations(CreateAnnotationArgs args)
        {
            var request = new PdfCreateAnnotationRequest(args);
            requestQueue.Add(request);
            return request;
        }

        public PdfGetAnnotationsOnPageRequest GetAnnotationsOnPage(int pageNr)
        {
            var request = new PdfGetAnnotationsOnPageRequest(new PdfGetAnnotationsOnPageArgs(pageNr));
            requestQueue.Add(request);
            return request;
        }

        public PdfUpdateAnnotationRequest UpdateAnnotations(UpdateAnnotationArgs args)
        {
            var request = new PdfUpdateAnnotationRequest(args);
            requestQueue.Add(request);
            return request;
        }

        public PdfDeleteAnnotationRequest DeleteAnnotations(DeleteAnnotationArgs args)
        {
            var request = new PdfDeleteAnnotationRequest(args);
            requestQueue.Add(request);
            return request;
        }

        public PdfSaveAsRequest SaveAs(string fileName)
        {
            var request =  new PdfSaveAsRequest(new SaveAsArguments(fileName));
            requestQueue.Add(request);
            return request;
        }

        #endregion [InkingForPDF] Annotation Methods

        private IList<int> _pageOrder;
        public IList<int> PageOrder
        {
            get { return _pageOrder; }
            set
            {
                if (value == null)
                    throw new ArgumentNullException("PageOrder", "PageOrder cannot be set to null");
                //if (value.Count != document.PageCount)
                //    throw new ArgumentException("PageOrder", "PageOrder needs to assign every page");
                _pageOrder = value;
                InversePageOrder = value;
                controller.OnPageOrderChangedCompleted(_pageOrder);
            }
        }

        private IList<int> _inversePageOrder;
        public IList<int> InversePageOrder
        {
            get { return _inversePageOrder; }
            private set
            {
                if (_inversePageOrder == null || _inversePageOrder.Count != document.PageCount)
                    _inversePageOrder = new List<int>(Enumerable.Range(1, document.PageCount));
                for (int i = 0; i<_pageOrder.Count; i++)
                {
                    _inversePageOrder[value[i]-1] = i+1;
                }
            }
        }

        public int PageCount
        {
            get
            {
                if (_pageOrder == null)
                    return 0;
                return _pageOrder.Count;
            }
        }

        private int _pageCacheSlidingWindowSize = 10;
        public int PageCacheSlidingWindowSize
        {
            set
            {
                _pageCacheSlidingWindowSize = value;
            }
            get
            {
                return _pageCacheSlidingWindowSize;
            }
        }

        private PdfGetPageRectRequest GetPageFromSourceAsync(int page)
        {
            Logger.LogInfo("Loading page " + page);
            PdfGetPageRectRequest request = new PdfGetPageRectRequest(page);
            requestQueue.Add(request);
            return request;
        }

        private APdfRequest<int, IList<PdfTextFragment>> GetTextFragmentsFromSource(int pageNo)
        {
            PdfGetTextFragmentsRequest request = new PdfGetTextFragmentsRequest(pageNo);
            requestQueue.Add(request);
            return request;
        }

        public PdfSourceRect GetPageRect(int pageNo)
        {
            //Console.WriteLine("Loading Page {0} exactly", pageNo);            
            // Viewport might show more pages than we have entries in the
            // page order list, if that's the case take the original page
            if (_pageOrder.Count > 0 && _pageOrder.Count >= pageNo)
                pageNo = _pageOrder[pageNo - 1];
            
            return pageCache.Get(pageNo);
        }
        public PdfSourceRect GetPageRectGuess(int pageNo)
        {
            //Console.WriteLine("Loading Page {0} guessed", pageNo);
            // Viewport might show more pages than we have entries in the
            // page order list, if that's the case take the original page
            if (_pageOrder.Count > 0 && _pageOrder.Count >= pageNo)
                pageNo = _pageOrder[pageNo - 1];

            return pageCache.GetGuess(pageNo);
        }

        /// <summary>
        /// Returns true if the page in the cache is not a guess
        /// </summary>
        /// <param name="pageNo"></param>
        /// <returns></returns>
        public bool IsPageExactlyLoaded(int pageNo)
        {
            return pageCache.ExactlyLoaded(pageNo);
        }

        public event Action<int> PageRectLoaded
        {
            add { pageCache.ItemLoadedToCache += value; }
            remove { pageCache.ItemLoadedToCache -= value; }
        }
    }
}
