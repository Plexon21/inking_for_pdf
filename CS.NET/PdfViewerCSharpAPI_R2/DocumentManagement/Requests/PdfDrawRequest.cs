/****************************************************************************
 *
 * File:            PdfDrawRequest.cs
 *
 * Description:     The 3-Heights™  Pdf Viewer Control for WPF
 *
 * Copyright:       Copyright (C) 2015 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *                  
 ***************************************************************************/
#define MEASUREDRAWINGTIME

namespace PdfTools.PdfViewerCSharpAPI.DocumentManagement.Requests
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;
    using PdfTools.PdfViewerCSharpAPI.Utilities;
    using PdfTools.PdfViewerCSharpAPI.Model;
    using System.Windows.Media.Imaging;
    using System.Diagnostics;



    public struct DrawArgs
    {
        public DrawArgs(int bitmapWidth, int bitmapHeight, Resolution resolution, int rotation, IList<KeyValuePair<int, PdfSourceRect>> pageRects, PdfViewerController.Viewport viewport)
        {
            this.bitmapWidth = bitmapWidth;
            this.bitmapHeight = bitmapHeight;
            this.resolution = resolution;
            this.rotation = rotation;
            this.pageRects = pageRects;
            this.viewport = viewport;
        }
        public int bitmapWidth, bitmapHeight, rotation;
        public Resolution resolution;
        public IList<KeyValuePair<int, PdfSourceRect>> pageRects;
        public PdfViewerController.Viewport viewport;
    }

    /// <summary>
    /// Request to draw part of a document to a bitmap 
    /// </summary>
    public class PdfDrawRequest : APdfRequest<DrawArgs, WriteableBitmap>
    {
        /// <summary>
        /// Create the Drawrequest
        /// </summary>
        /// <param name="bitmapWidth">width of the bitmap</param>
        /// <param name="bitmapHeight">height of the bitmap</param>
        /// <param name="rotation">Additional rotation of all pages due to rotate command in viewer</param>
        /// <param name="pages">List of pages to render</param>
        /// <param name="sourceRects">List of rectangles relative to their page origin in source</param>
        /// <param name="targetRects">List of rectangles on the bitmap, where the pages have to be drawn to</param>
        /// <param name="resolution">Resolution of the bitmap</param>
        public PdfDrawRequest(DrawArgs args)
            :base(args, 10)
        {
#if MEASUREDRAWINGTIME
            stopwatch = new Stopwatch();
            stopwatch.Start();
#endif //MEASUREDRAWINGTIME
        }

        protected override WriteableBitmap ExecuteNative(IPdfDocument document, DrawArgs args)
        {
#if MEASUREDRAWINGTIME
            waitTime = stopwatch.ElapsedMilliseconds;
#endif //MEASUREDRAWINGTIME
            WriteableBitmap bitmap = new WriteableBitmap(args.bitmapWidth, args.bitmapHeight, args.resolution.xdpi, args.resolution.ydpi, PdfViewerController.pixelFormat, null);
            document.Draw(bitmap, args.rotation, args.pageRects, args.viewport);
            bitmap.Freeze();
#if MEASUREDRAWINGTIME
            Logger.LogInfo("Bitmap rendered Input lag=" + stopwatch.ElapsedMilliseconds + ". processing time=" + (stopwatch.ElapsedMilliseconds - waitTime));
#endif //MEASUREDRAWINGTIME
            return bitmap;
        }
#if MEASUREDRAWINGTIME
        Stopwatch stopwatch;
#endif //MEASUREDRAWINGTIME
        long waitTime;



        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, InOutTuple tuple, PdfViewerException ex)
        {
            controller.OnDrawCompleted(ex, tuple.output);
        }
        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, PdfViewerException ex)
        {
            controller.OnDrawCompleted(ex, null);
        }
    }
}