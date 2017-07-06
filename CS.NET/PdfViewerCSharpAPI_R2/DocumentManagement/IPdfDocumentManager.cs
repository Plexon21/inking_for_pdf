/****************************************************************************
 *
 * File:            IPdfDocumentManager.cs
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
    using System.Collections.Generic;
    using System.Collections.Concurrent;
    using System.Linq;
    using System.Text;
    using System.Drawing;
    using System.Windows;
    using System.Windows.Media.Imaging;
    using PdfTools.PdfViewerCSharpAPI.Model;
    using PdfTools.PdfViewerCSharpAPI.Utilities;
    using PdfTools.PdfViewerCSharpAPI.DocumentManagement.Requests;

    /// <summary>
    /// Interface of a Documentmanager, which manages the access to the document
    /// </summary>
    public interface IPdfDocumentManager : IDisposable
    {
        /// <summary>
        /// Open a new file. This document will from now on represent this file
        /// </summary>
        /// <param name="filename"></param>
        /// <param name="password"></param>
        PdfOpenRequest Open(string filename, byte[] fileMem, string password);
        /// <summary>
        /// Closes the opened file. This document will not represent anything until a new file is opened
        /// <param name="triggerCallback">Whether the callback to controller should be called (used when its an actual close call and not to just reset)</param>
        /// </summary>
        PdfCloseRequest Close(bool triggerCallback);

        /// <summary>
        /// Draw a list of pages to a given bitmap
        /// </summary>
        /// <param name="width">The width of the bitmap to draw into</param>
        /// <param name="height">The height of the bitmap to draw into</param>
        /// <param name="rotation">The additional rotation caused by commands in the viewer</param>
        /// <param name="pages">A list of pages to be drawn</param>
        /// <param name="sourceRects">A rectangle of the section of each page on the document that is to be rendered</param>
        /// <param name="targetRects">A rectangle on the bitmap where each page is to be rendered to</param>
        /// <param name="resolution">The resolution of the bitmap</param>
        PdfDrawRequest Draw(int width, int height, Resolution resolution, int rotation, IDictionary<int, PdfSourceRect> pageRects, PdfViewerController.Viewport viewport);

        PdfGetPageLayoutRequest RequestPageLayout();

        PdfGetOpenActionDestinationRequest RequestOpenActionDestination();

        PdfGetOutlinesRequest RequestOutlines(int outlineId);

        APdfRequest<ThumbnailCacheArgs, WriteableBitmap> RequestThumbnail(ThumbnailCacheArgs args);

        WriteableBitmap GetThumbnail(ThumbnailCacheArgs args);

        void CancelRequest(IPdfRequest request);


        PdfSourceRect GetPageRect(int pageNo);
        PdfSourceRect GetPageRectGuess(int pageNo);
        bool IsPageExactlyLoaded(int pageNo);

        IList<PdfTextFragment> GetTextFragments(int page);


        event Action<int> PageRectLoaded;

        int PageCount
        {
            get;
        }

        IList<int> PageOrder
        {
            get;
            set;
        }

        IList<int> InversePageOrder
        {
            get;
        }

        int PageCacheSlidingWindowSize
        {
            set;
            get;
        }

        PdfLoadAnnotationsOnPageRequest LoadAnnotationsOnPage(int pageNr);
        PdfSaveAsRequest SaveAs(string fileName);
        PdfCreateAnnotationRequest CreateAnnotation(CreateAnnotationArgs args);
        PdfUpdateAnnotaionRequest UpdateAnnotation(UpdateAnnotationArgs args);
        PdfDeleteAnnotationRequest DeleteAnnotation(DeleteAnnotationArgs args);
    }
}
