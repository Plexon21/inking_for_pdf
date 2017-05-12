/****************************************************************************
 *
 * File:            PdfDocment.cs
 *
 * Description:     The 3-Heights™  Pdf Viewer Control for WPF
 *
 * Copyright:       Copyright (C) 2015 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *                  
 ***************************************************************************/

namespace PdfTools.PdfViewerCSharpAPI.DocumentManagement
{
    using System;
    using System.Collections.Concurrent;
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
    using System.Linq;
    using System.Text;
    using System.Drawing.Imaging;
    using System.Windows.Media.Imaging;
    using PdfTools.PdfViewerCSharpAPI.Utilities;
    using PdfTools.PdfViewerCSharpAPI.Model;
    using System.Runtime.InteropServices;
    using System.Windows;
    using System.Drawing;
    using System.IO;

    /// <summary>
    /// Uses the PdfViewerAPI.dll to interact with the pdfDocument
    /// </summary>

    public class PdfDocument : IPdfDocument, IDisposable
    {

        IntPtr documentHandle;
        bool isOpen = false;

        //private static string filepath = @"P:\temp\bitmapSnapshots\rectLog.csv";

        public PdfDocument()
        {
        }

        public void Dispose()
        {
            Dispose(true);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (disposing)
            {
                if (isOpen)
                    Close();
            }
        }

        public void Open(string filename, byte[] fileMem, string password)
        {
            //File.WriteAllText(filepath, "count, page, sX, sY, sW, sH, tX, tY, tW, tH, otX, otY, otW, otH\n");
            //File.WriteAllText(filepath, "count, page, 1X, 1Y, 1W, 1H, 2X, 2Y, 2W, 2H, 3X, 3Y, 3W, 3H, 4X, 4Y, 4W, 4H, tX, tY, tW, tH\n");
            lastBitmap = null;
            if (isOpen)
            {
                PdfViewerDestroyObject(documentHandle);
            }


            if (filename != null)
            {
                Logger.LogInfo("Creating rendering engine");
                documentHandle = PdfViewerCreateObjectW(filename, IntPtr.Zero, IntPtr.Zero, password);
                Logger.LogInfo("Created rendering engine");
            }
            else
            {
                Logger.LogInfo("Creating rendering engine");
                GCHandle gchmemBlock = GCHandle.Alloc(fileMem, GCHandleType.Pinned);
                documentHandle = PdfViewerCreateObjectW(filename, gchmemBlock.AddrOfPinnedObject(), (IntPtr)fileMem.Length, password);
                gchmemBlock.Free();
                Logger.LogInfo("Created rendering engine");
            }
            if (documentHandle.ToInt64() == 0)
            {
                Logger.LogInfo("PdfViewerCreateObjectW reported error, throwing exception");
                UIntPtr size = PdfViewerGetLastErrorMessageW(null, UIntPtr.Zero);
                string message = "";
                if (size != UIntPtr.Zero)
                {
                    StringBuilder buffer = new StringBuilder((int)size.ToUInt32());
                    PdfViewerGetLastErrorMessageW(buffer, size);
                    message = buffer.ToString();
                }
                switch (PdfViewerGetLastError())
                {
                    case TViewerError.eLicenseError: throw new PdfLicenseInvalidException(message);
                    case TViewerError.eFileNotFoundError: throw new PdfFileNotFoundException(message);
                    case TViewerError.ePasswordError: throw new PdfPasswordException(message);
                    case TViewerError.eIllegalArgumentError: throw new ArgumentException(message);
                    case TViewerError.eUnsupportedFeatureError: throw new PdfUnsupportedFeatureException(message);
                    case TViewerError.eFileCorruptError: throw new PdfFileCorruptException(message);
                    default: throw new PdfViewerException("Unknown Error when trying to open File. Message: \"" + message + "\"");
                }
            }

            PageCount = PdfViewerGetPageCount(documentHandle);
            isOpen = true;
        }

        public void Close()
        {
            if (isOpen)
            {
                isOpen = false;
                lastBitmap = null;
                Logger.LogInfo("Destroying rendering engine");
                PdfViewerDestroyObject(documentHandle);
                Logger.LogInfo("Destroyed rendering engine");
            }
        }


        private int _pageCount;
        public int PageCount
        {
            protected set
            {
                _pageCount = value;
            }
            get
            {
                if (!isOpen)
                {
                    throw new PdfNoFileOpenedException();
                }
                return _pageCount;
            }
        }

        public PdfSourceRect GetPageRect(int pageNo)
        {
            if (!isOpen)
            {
                throw new PdfNoFileOpenedException();
            }
            Logger.LogInfo("Loading pageRectangle from source");
            double width = 0;
            double height = 0;
            IntPtr pageHandle = PdfViewerGetPageRect(documentHandle, pageNo, ref width, ref height);
            if (pageHandle.ToInt64() == 0)
                throw new PdfViewerException(String.Format("Could not access page %i", pageNo));
            PdfSourceRect rect = new PdfSourceRect(0.0, 0.0, width, height);
            int rotation = PdfViewerGetRotation(documentHandle, pageNo) + 360;
            if (rotation % 180 != 0)
                rect.RotateSize();

            Logger.LogInfo("Loaded pageRectangles from source");
            return rect;
        }

        public WriteableBitmap LoadThumbnail(double sourceWidth, double sourceHeight, int targetWidth, int targetHeight, int page, Resolution resolution)
        {
            if (!isOpen)
            {
                throw new PdfNoFileOpenedException();
            }
            Logger.LogInfo("Loading thumbnail page " + page);
            WriteableBitmap bitmap = new WriteableBitmap(targetWidth, targetHeight, resolution.xdpi, resolution.ydpi, PdfViewerController.pixelFormat, null);
            bitmap.Lock();
            PdfViewerDraw(documentHandle, page, bitmap.PixelWidth, bitmap.PixelHeight, bitmap.BackBuffer, 0,
                0, 0, targetWidth, targetHeight,
                0.0, 0.0, sourceWidth, sourceHeight);
            bitmap.AddDirtyRect(new Int32Rect(0, 0, targetWidth, targetHeight));
            bitmap.Unlock();
            Logger.LogInfo("Loaded thumbnail page " + page);
            return bitmap;
        }

        /// <returns>Whether the page has been drawn or queued to lists for drawing. Returns false if no valid information about previous drawRequest</returns>
        private bool CalculateSourceTargetRects(WriteableBitmap bitmap, int rotation, int page, PdfSourceRect pageRect, IList<int> pages, IList<PdfSourceRect> sourceRects, IList<PdfTargetRect> targetRects, PdfSourceRect visibleRectOnPage, PdfViewerController.Viewport viewport)
        {
            if (!lastVisiblePages.Contains(page) || visibleRectOnPage.IsEmpty)
                return false;

            int lastIndex = lastVisiblePages.IndexOf(page);
            PdfSourceRect reusableRectOnPage = lastVisibleRectOnPages[lastIndex].intersectDouble(visibleRectOnPage);
            PdfTargetRect reusableTargetRect, reusableTargetRectOnLastBitmap;
            while (true)
            {
                reusableTargetRect = GetTargetRect(reusableRectOnPage, pageRect, viewport);
                reusableTargetRectOnLastBitmap = GetTargetRect(reusableRectOnPage, lastPageRects[lastIndex], lastViewport);
                if (reusableTargetRectOnLastBitmap.IsEmpty)
                    return false; //if we cant reuse anything, there is nothing to do    
                if (reusableTargetRect.iWidth != reusableTargetRectOnLastBitmap.iWidth || reusableTargetRect.iHeight != reusableTargetRectOnLastBitmap.iHeight)
                {
                    //Console.WriteLine("Inconsistent reusableTargetRect sizes: {0} vs. {1}  and  {2} vs. {3}", reusableTargetRect.iWidth, reusableTargetRectOnLastBitmap.iWidth, reusableTargetRect.iHeight, reusableTargetRectOnLastBitmap.iHeight);
                    reusableRectOnPage.Shrink(0.99);
                }
                else
                    break;
            }

            reusedTargetRects.Add(reusableTargetRect);

            //calculate the area to copy
            int bytesPerPixel = (lastBitmap.Format.BitsPerPixel + 7) / 8;
            int stride = reusableTargetRectOnLastBitmap.iWidth * bytesPerPixel;
            byte[] pixels = new byte[reusableTargetRectOnLastBitmap.iWidth * reusableTargetRectOnLastBitmap.iHeight * bytesPerPixel];

            //read from last bitmap
            lastBitmap.CopyPixels(reusableTargetRectOnLastBitmap.GetInt32Rect(), pixels, stride, 0); //(lastBitmap.PixelWidth * (reusableTargetRectOnLastBitmap.iY - 1) + reusableTargetRectOnLastBitmap.iX - 1) * bytesPerPixel);

            //write to new bitmap
            bitmap.Lock();
            bitmap.WritePixels(reusableTargetRect.GetInt32Rect(), pixels, stride, 0);
            bitmap.AddDirtyRect(reusableTargetRect.GetInt32Rect());
            bitmap.Unlock();

            //calculate rects that need to be drawn newly
            IList<PdfTargetRect> targetDifs = GetTargetRect(pageRect, viewport).subtractRect(reusableTargetRect);
            for (int i = 0; i < 4; i++)
            {

                PdfTargetRect targetRect = targetDifs[i];
                if (targetRect.IsEmpty)
                    continue;
                PdfSourceRect sourceRect = GetSourceFromTargetRect(targetRect, pageRect, viewport);
                targetRects.Add(targetRect);
                sourceRects.Add(sourceRect);
                pages.Add(page);
            }
            return true;
        }

        private PdfSourceRect GetSourceFromTargetRect(PdfTargetRect targetRect, PdfSourceRect pageRect, PdfViewerController.Viewport viewport)
        {
            PdfTargetRect targetClone = targetRect.Clone();
            targetClone.Offset(viewport.Rectangle.iX, viewport.Rectangle.iY);
            PdfSourceRect sourceRect = targetClone.GetSourceRect(viewport.ZoomFactor);
            sourceRect.Offset(-pageRect.dX, -pageRect.dY);
            sourceRect.dY = pageRect.dHeight - sourceRect.dBottom;
            return sourceRect;
        }

        private PdfSourceRect GetSourceRect(PdfSourceRect pageRect, PdfViewerController.Viewport viewport)
        {
            return GetSourceRect(null, pageRect, viewport);
        }

        private PdfSourceRect GetSourceRect(PdfSourceRect rectOnPage, PdfSourceRect pageRect, PdfViewerController.Viewport viewport)
        {
            PdfSourceRect pageSubRect;
            if (rectOnPage == null)
                pageSubRect = pageRect;
            else
            {
                pageSubRect = rectOnPage.Clone();
                pageSubRect.Offset(pageRect.dX, pageRect.dY);//yes we have to translate forth and back to ensure that viewport clipping is always done in the exact same manner (rounding errors!)
            }
            PdfSourceRect sourceRect = pageSubRect.intersectDouble(viewport.Rectangle.GetSourceRect(viewport.ZoomFactor));
            // translate sourcePdfRect, to be relative to page instead of relative to the canvas
            sourceRect.Offset(-pageRect.dX, -pageRect.dY);
            // translate origin from top/left to bottom/left
            sourceRect.dY = pageRect.dHeight - sourceRect.dBottom;
            return sourceRect;
        }

        private PdfTargetRect GetTargetRect(PdfSourceRect pageRect, PdfViewerController.Viewport viewport)
        {
            return GetTargetRect(null, pageRect, viewport);
        }

        //we ought to ensure, that the size of the resulting target rect is independent of the viewport offset
        //the problem is, that sometimes the rectOnPage is not entirely within the viewport and it thus gets cropped a bit by one of the viewports, yielding inconsistent size
        private PdfTargetRect GetTargetRect(PdfSourceRect rectOnPage, PdfSourceRect pageRect, PdfViewerController.Viewport viewport)
        {
            PdfSourceRect pageSubRect;
            if (rectOnPage == null)
                pageSubRect = pageRect;
            else
            {
                pageSubRect = rectOnPage.Clone();
                pageSubRect.Offset(pageRect.dX, pageRect.dY);
            }
            PdfTargetRect targetRect = pageSubRect.GetTargetRect(viewport.ZoomFactor);
            targetRect = targetRect.intersectInt(viewport.Rectangle);
            targetRect.Offset(-viewport.Rectangle.iX, -viewport.Rectangle.iY);
            //Subtract the offset of the viewportPdfRectangle on the Canvas. This way we get coordinats in relation to the viewport
            return targetRect;
        }

        // source and targetrects may be empty! (due to crops with pages etc.)
        public void Draw(WriteableBitmap bitmap, int rotation, IList<KeyValuePair<int, PdfSourceRect>> pageRectsDict, PdfViewerController.Viewport viewport)
        {
            Logger.LogInfo("Drawing bitmap");
            if (!isOpen)
            {
                throw new PdfNoFileOpenedException();
            }

            IList<PdfSourceRect> sourceRects = new List<PdfSourceRect>();
            IList<PdfTargetRect> targetRects = new List<PdfTargetRect>();
            IList<int> visiblePages = new List<int>();
            IList<int> pages = new List<int>(); //The list of pagenumbers for each s/t rect that needs to be drawn
            IList<int> newPages = new List<int>(); //pages that are completely new drawn and nothing is reused
            IList<PdfSourceRect> pageRects = new List<PdfSourceRect>();
            IList<PdfSourceRect> visibleRectOnPages = new List<PdfSourceRect>();
            reusedTargetRects = new List<PdfTargetRect>();

            foreach (KeyValuePair<int, PdfSourceRect> keyValuePair in pageRectsDict)
            {
                PdfSourceRect pageRect = keyValuePair.Value;
                int page = keyValuePair.Key;
                if (!visiblePages.Contains(page))
                    visiblePages.Add(page);
                // crop the page with viewport  //Note that the size of the resulting rectangle may vary depending on viewport/pageRect offsets, due to rounding issues
                //PdfSourceRect visibleRectOnPage = pageRect.intersectDouble(viewport.Rectangle.GetSourceRect(viewport.ZoomFactor));
                PdfTargetRect visibleTargetRect = pageRect.GetTargetRect(viewport.ZoomFactor).intersectInt(viewport.Rectangle);
                PdfSourceRect visibleRectOnPage = visibleTargetRect.GetSourceRect(viewport.ZoomFactor);
                visibleRectOnPage.Offset(-pageRect.dX, -pageRect.dY);
                bool insertedRectanglesToDraw = false;
                if (lastBitmap != null && lastRotation == rotation && Math.Abs(lastViewport.ZoomFactor / viewport.ZoomFactor - 1.0) < 0.01)
                    insertedRectanglesToDraw = CalculateSourceTargetRects(bitmap, rotation, page, pageRect, pages, sourceRects, targetRects, visibleRectOnPage, viewport);
                if (!insertedRectanglesToDraw)
                {
                    //i just have to do the same thing in here as calculateSourceTargetRects would do
                    PdfTargetRect targetRect = GetTargetRect(visibleRectOnPage, pageRect, viewport);
                    sourceRects.Add(GetSourceFromTargetRect(targetRect, pageRect, viewport));
                    targetRects.Add(targetRect);
                    pages.Add(page);
                    newPages.Add(page);
                }
                pageRects.Add(pageRect);
                visibleRectOnPages.Add(visibleRectOnPage);
            }

            int drewThisManyTimes = 0;
            //DebugLogger.Log("Started drawing of pages " + string.Join(",", pages.Select(i => i.ToString()).ToArray()));
            for (int i = 0; i < pages.Count; i++)
            {
                int p = pages[i];
                PdfSourceRect s = sourceRects[i];
                PdfTargetRect t = targetRects[i];

                //The target may be so small, that we cant see it or it can be horizontally shifted out of the viewport
                if (t.IsEmpty)
                    continue;

                //draw the content of the sourcerectangle in the pdf to the targetRectangle on the viewer bitmap
                bitmap.Lock();

                Logger.LogInfo("Drawing page " + p + " targetRect=" + t.ToString());

                PdfViewerDraw(documentHandle, p, bitmap.PixelWidth, bitmap.PixelHeight, bitmap.BackBuffer, rotation,
                    t.iX, t.iY, t.iWidth, t.iHeight,
                    s.dX, s.dY, s.dWidth, s.dHeight);
                drewThisManyTimes++;
                Logger.LogInfo("Finished drawing page " + p);

                bitmap.AddDirtyRect(t.GetInt32Rect());
                bitmap.Unlock();
            }

            lastBitmap = bitmap;
            lastVisiblePages = visiblePages;
            lastPageRects = pageRects;
            lastRotation = rotation;
            lastViewport = viewport;
            lastVisibleRectOnPages = visibleRectOnPages;
            //DebugLogger.Log("Finished drawing of pages " + string.Join(",", pages.Select(i => i.ToString()).ToArray()));
            //visualizeDraw(bitmap.Clone(), rotation, pages, sourceRects, targetRects, newPages);
            Logger.LogInfo("Drew bitmap using " + drewThisManyTimes + " native calls");
        }

        private void visualizeDraw(WriteableBitmap bitmap, int rotation, IList<int> pages, IList<PdfSourceRect> sourceRects, IList<PdfTargetRect> targetRects, IList<int> newPages)
        {
            var bmp = new System.Drawing.Bitmap(bitmap.PixelWidth, bitmap.PixelHeight,
                                     bitmap.BackBufferStride,
                                     PixelFormat.Format32bppPArgb,
                                     bitmap.BackBuffer);

            Graphics g = System.Drawing.Graphics.FromImage(bmp);
            Color color = Color.HotPink;
            System.Drawing.Font font = System.Drawing.SystemFonts.DefaultFont;
            foreach (PdfTargetRect t in targetRects)
            {
                if (t.IsEmpty)
                    continue;
                int page = pages[targetRects.IndexOf(t)];
                if (newPages.Contains(page))
                    color = Color.DodgerBlue;
                //g.DrawRectangle(new Pen(color), t.GetDrawingRect());
                g.FillRectangle(new SolidBrush(Color.FromArgb(50, color)), t.GetDrawingRect());
                g.DrawString(page.ToString(), font, new SolidBrush(color), (float)t.iX, (float)t.iY);
            }
            color = Color.Orange;
            foreach (PdfTargetRect t in reusedTargetRects)
            {
                //g.DrawRectangle(new Pen(color), t.GetDrawingRect());
                g.FillRectangle(new SolidBrush(Color.FromArgb(50, color)), t.GetDrawingRect());
            }

            bmp.Save(@"P:\temp\bitmapSnapshots\bitmap" + visualizeCount.ToString() + ".png", ImageFormat.Png);
            visualizeCount++;
        }

        private int visualizeCount = 0;
        //private int reusableCount = 0;


        private WriteableBitmap lastBitmap = null;
        private int lastRotation;
        private IList<int> lastVisiblePages;
        private IList<PdfSourceRect> lastVisibleRectOnPages;
        private IList<PdfSourceRect> lastPageRects;
        private IList<PdfTargetRect> reusedTargetRects;
        private PdfViewerController.Viewport lastViewport;

        public TPageLayoutMode GetPageLayout()
        {
            if (!isOpen)
                throw new PdfNoFileOpenedException();
            return PdfViewerGetPageLayout(documentHandle);
        }

        public PdfDestination GetOpenActionDestination()
        {
            Logger.LogInfo("Getting Open Action");
            if (!isOpen)
            {
                throw new PdfNoFileOpenedException();
            }
            int page = 0;
            double left = Double.NaN;
            double top = Double.NaN;
            double right = Double.NaN;
            double bottom = Double.NaN;
            double zoom = Double.NaN;
            TDestination s = PdfViewerGetOpenActionDestination(documentHandle, ref page, ref left, ref top, ref right, ref bottom, ref zoom);
            Logger.LogInfo("Returning Open Action");
            return new PdfDestination(page, s, left, top, right, bottom, zoom);
        }

        //structs to parse CObjects
        [StructLayout(LayoutKind.Sequential)]
        public struct Point
        {
            public Point(double x, double y) { this.x = x; this.y = y; }
            public double x;    // Horizontal coordinate.
            public double y;    // Vertical coordinate.
        }
        [StructLayout(LayoutKind.Sequential)]
        private struct PIOutlineItem
        {
            public int id;
            public int level;
            [MarshalAs(UnmanagedType.I1)]
            public bool descendants;
            public IntPtr stringPtr;
            public string title { get { return Marshal.PtrToStringUni(stringPtr); } }
            public int pageNo;
            public Point pt;
            public double zoom;
            public double left;
            public double top;
            public double right;
            public double bottom;
            public IntPtr destPtr;
            public string destType { get { return Marshal.PtrToStringUni(destPtr); } }
        }
        [StructLayout(LayoutKind.Sequential)]
        public struct NativeTextFragment
        {
            public IntPtr p;
            public string m_szTextA { get { return Marshal.PtrToStringUni(p); } }
            public int m_textLength;
            public double m_dX;
            public double m_dY;//from bottom of page
            public double m_dWidth;
            public double m_dHeight;
            public IntPtr m_pdGlyphPositionNative;
            public double[] m_pdGlyphPosition
            {
                get
                {
                    double[] array = new double[m_nGlyphPositionSize];
                    IntPtr pG = m_pdGlyphPositionNative;
                    for (int i = 0; i < m_nGlyphPositionSize; i++)
                    {
                        array[i] = (double)Marshal.PtrToStructure(pG, typeof(double));
                        pG += Marshal.SizeOf(typeof(double));
                    }
                    return array;
                    ;
                }
            }
            public int m_nGlyphPositionSize;
        }


        //Marshalling der Annotation aus der dll
        [StructLayout(LayoutKind.Sequential)]
        public struct TPdfAnnotation
        {
            
            public IntPtr annotationHandle;

           
            public int pageNr;
            
            
            public IntPtr ptrSubtype;
            
            public string subType { get { return Marshal.PtrToStringUni(ptrSubtype); } }
            
            
            public int nrOfColors;
            
            public IntPtr ptrColors;
            
            public double[] colors
            {
                get
                {
                    double[] array = new double[nrOfColors];
                    IntPtr pG = ptrColors;
                    for (int i = 0; i < nrOfColors; i++)
                    {
                        array[i] = (double)Marshal.PtrToStructure(pG, typeof(double));
                        pG += Marshal.SizeOf(typeof(double));
                    }
                    return array;
                }
            }
            
            public int flags;
            [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
            public double[] rect;
            public IntPtr ptrQuadPoints;
            
            public double[] quadPoints
            {
                get
                {
                    double[] array = new double[nrOfQuadPoints];
                    IntPtr pG = ptrQuadPoints;
                    for (int i = 0; i < nrOfQuadPoints; i++)
                    {
                        array[i] = (double)Marshal.PtrToStructure(pG, typeof(double));
                        pG += Marshal.SizeOf(typeof(double));
                    }
                    return array;
                }
            }
            
            public int nrOfQuadPoints;
            public IntPtr ptrContents;
            public string contents { get { return Marshal.PtrToStringUni(ptrContents); } }
            [MarshalAs(UnmanagedType.I1)]
            public bool isLink;
            public IntPtr ptrActionType;
            public string actionType { get { return Marshal.PtrToStringUni(ptrActionType); } }
            [MarshalAs(UnmanagedType.I1)]
            public bool hasURI;
            public IntPtr ptrURI;
            public string URI { get { return Marshal.PtrToStringUni(ptrURI); } }
            int destType;
            [MarshalAs(UnmanagedType.ByValArray, ArraySubType = UnmanagedType.I1, SizeConst = 5)]
            public bool[] hasDestVal;
            [MarshalAs(UnmanagedType.ByValArray, SizeConst = 5)]
            public double[] destArray;
            int destPage;
            [MarshalAs(UnmanagedType.I1)]
            public bool isMarkup;
            public IntPtr ptrTextLabel;
            public string textLabel { get { return Marshal.PtrToStringUni(ptrTextLabel); } }
            [MarshalAs(UnmanagedType.I1)]
            public bool hasPopup;
            TPopupAnnotation m_pPopupAnnot;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct TPopupAnnotation
        {
            public IntPtr popupAnnotationHandle;
            [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
            public double[] rect;
            [MarshalAs(UnmanagedType.I1)]
            public bool isOpen;
            public IntPtr ptrSubtype;
            public string subType { get { return Marshal.PtrToStringUni(ptrSubtype); } }
        }

        public IList<PdfTextFragment> LoadTextFragments(int pageNo)
        {
            if (!isOpen)
            {
                throw new PdfNoFileOpenedException();
            }

            Logger.LogInfo("Loading textFragments of page " + pageNo);
            IntPtr pArray = IntPtr.Zero;
            int count = 0;
            if (!PdfViewerExtractTextFragments(documentHandle, pageNo, ref pArray, ref count))
                return null;

            long textFragmentSize = Marshal.SizeOf(new NativeTextFragment());
            IntPtr pTextFragment = pArray;
            IList<PdfTextFragment> textFragments = new List<PdfTextFragment>();
            PdfTextFragment lastFrag = null;
            for (int i = 0; i < count; i++)
            {
                NativeTextFragment textFragment = (NativeTextFragment)Marshal.PtrToStructure(pTextFragment, typeof(NativeTextFragment));
                lastFrag = new PdfTextFragment(textFragment, pageNo, lastFrag);
                textFragments.Add(lastFrag);
                pTextFragment = new IntPtr(pTextFragment.ToInt64() + textFragmentSize);
            }
            PdfViewerDisposeTextFragments(pArray, count);
            Logger.LogInfo("Loaded textFragments of page " + pageNo);
            return textFragments;
        }

        public IList<PdfOutlineItem> GetOutlines(int parentId)
        {
            if (!isOpen)
            {
                throw new PdfNoFileOpenedException();
            }
            Logger.LogInfo("Getting outlines children of parent " + parentId);
            IntPtr pArray = IntPtr.Zero;
            int count = 0;
            if (!PdfViewerGetOutlineItems(documentHandle, parentId, ref pArray, ref count) || count <= 0)
                return null;

            IntPtr pItem = pArray;
            long piItemSize = Marshal.SizeOf(new PIOutlineItem());
            IList<PdfOutlineItem> outlineItems = new List<PdfOutlineItem>();
            for (int i = 0; i < count; i++)
            {
                PIOutlineItem piOutlineItem = (PIOutlineItem)Marshal.PtrToStructure(pItem, typeof(PIOutlineItem));
                PdfOutlineItem outlineItem = new PdfOutlineItem();
                outlineItem.id = piOutlineItem.id;
                outlineItem.level = piOutlineItem.level;
                outlineItem.descendants = piOutlineItem.descendants;
                outlineItem.title = piOutlineItem.title;
                outlineItem.dest = new PdfDestination(piOutlineItem.pageNo, TDestination.eDestinationXYZ,
                                       piOutlineItem.pt.x, piOutlineItem.pt.y, 0, 0, piOutlineItem.zoom);
                outlineItems.Add(outlineItem);
                pItem = new IntPtr(pItem.ToInt64() + piItemSize);
            }
            PdfViewerDisposeOutlineItems(pArray, count);
            Logger.LogInfo("Returning outlines children of parent " + parentId);
            return outlineItems;
        }

        #region DLL imports

        private enum TViewerError
        {
            eLicenseError = 0, ePasswordError = 1, eFileNotFoundError = 2, eUnknownError = 3, eIllegalArgumentError = 4, eOutOfMemoryError = 5, eFileCorruptError = 6, eUnsupportedFeatureError = 7
        };

        public enum TPdfAnnotationType
        {
            eAnntationUnknown = 0,
            eAnnotationText = 1,
            eAnnotationLink = 2,
            eAnnotationFreeText = 3,
            eAnnotationHighlight = 9,
            eAnnotationInk = 15,
            eAnnotationPopup = 16,
            eAnnotationWidet = 20
        }

        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern IntPtr PdfViewerCreateObjectW(string filename, IntPtr fileMem, IntPtr fileMemLength, string password);
        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern void PdfViewerDestroyObject(IntPtr handle);
        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern int PdfViewerGetPageCount(IntPtr handle);
        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern IntPtr PdfViewerGetPageRect(IntPtr handle, int page, ref double width, ref double height);
        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern int PdfViewerGetRotation(IntPtr handle, int page);
        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern bool PdfViewerDraw(IntPtr handle, int iPage,
            int iWidth, int iHeight, IntPtr pBuffer, int iRotation,
            int iTargetX, int iTargetY, int iTargetWidth, int iTargetHeight,
            double dSourceX, double dSourceY, double dSourceWidth, double dSourceHeight);
        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern TPageLayoutMode PdfViewerGetPageLayout(IntPtr handle);
        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern TDestination PdfViewerGetOpenActionDestination(IntPtr handle, ref int page, ref double left, ref double top, ref double right, ref double bottom, ref double zoom);
        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern bool PdfViewerGetOutlineItems(IntPtr handle, int parentID, ref IntPtr outlineItems, ref int count);
        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern bool PdfViewerDisposeOutlineItems(IntPtr outlineItems, int count);
        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern bool PdfViewerExtractTextFragments(IntPtr handle, int pageNo, ref IntPtr textFragments, ref int count);
        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern bool PdfViewerDisposeTextFragments(IntPtr textFragments, int nCount);

        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern TViewerError PdfViewerGetLastError();
        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern UIntPtr PdfViewerGetLastErrorMessageW(StringBuilder errorMessageBuffer, UIntPtr errorMessageBufferSize);

        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern IntPtr PdfViewerCreateAnnotation(IntPtr pHandle, TPdfAnnotationType eType, int iPage, double[] r, int iLen, double[] color, int nColors, double dBorderWidth);

        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern void PdfViewerDeleteAnnotation(IntPtr annot);

        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern bool PdfViewerGetAnnotationsOnPage(IntPtr handle, int pageNo, out IntPtr pdfAnnotations, ref int count);
        #endregion

        public IntPtr CreateAnnotation(TPdfAnnotationType eType, int iPage, double[] r, int iLen, double[] color, int nColors, double dBorderWidth)
        {
            return PdfViewerCreateAnnotation(documentHandle, eType, iPage, r, iLen, color, nColors, dBorderWidth);
        }

        public bool GetAnnotations(int pageNo, out IntPtr pdfAnnotations, ref int count)
        {
            return PdfViewerGetAnnotationsOnPage(documentHandle, pageNo, out pdfAnnotations, ref count);
        }

        public void DeleteAnnotation(IntPtr anno)
        {
            PdfViewerDeleteAnnotation(anno);
        }
    }
}
