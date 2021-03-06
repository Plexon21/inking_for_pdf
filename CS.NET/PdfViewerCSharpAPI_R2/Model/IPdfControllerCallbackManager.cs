﻿/****************************************************************************
 *
 * File:            IPdfControllerCallbackManager.cs
 *
 * Description:     The 3-Heights™  Pdf Viewer Control for WPF
 *
 * Copyright:       Copyright (C) 2015 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *                  
 ***************************************************************************/

using PdfTools.PdfViewerCSharpAPI.Annotations;

namespace PdfTools.PdfViewerCSharpAPI.Model
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;
    using System.Windows.Media.Imaging;
    using PdfTools.PdfViewerCSharpAPI.Utilities;
    using PdfTools.PdfViewerCSharpAPI.DocumentManagement;

    /// <summary>
    /// Interface for callbacks on the viewercontroller, notifying of executed requests
    /// </summary>
    public interface IPdfControllerCallbackManager
    {
        /// <summary>
        /// Notifies, that an open request has been executed
        /// </summary>
        void OnOpenCompleted(PdfViewerException ex);

        /// <summary>
        /// Notifies, that a close request has been executed
        /// </summary>
        void OnCloseCompleted(PdfViewerException ex);

        /// <summary>
        /// Notifies, that a draw request has been executed
        /// </summary>
        /// <param name="bitmap">The bitmap that has been drawn and is ready to be displayed</param>
        void OnDrawCompleted(PdfViewerException ex, WriteableBitmap bitmap);

        /// <summary>
        /// Notifies controller, that outlines have been loaded
        /// </summary>
        /// <param name="ex"></param>
        /// <param name="outlineItems"></param>
        void OnOutlinesLoaded(PdfViewerException ex, int parentId, IList<PdfOutlineItem> outlineItems);

        void OnThumbnailLoaded(int pageNo, WriteableBitmap bitmap, PdfViewerException exception);

        void OnPageOrderChangedCompleted(IList<int> pageOrder);

        #region events
        event Action<double> ZoomChanged;
        event Action<int> RotationChanged;
        event Action<int, int> VisiblePageRangeChanged;
        event Action<IList<int>> PageOrderChanged;
        #endregion

        #region [InkingForPDF] Annotation Methods

        /// <summary>
        /// Notifies controller, that annotations have been created
        /// </summary>
        /// <param name="pdfViewerException"></param>
        /// <param name="annots"></param>
        void OnAnnotationCreated(PdfViewerException pdfViewerException, IList<PdfAnnotation> annots);

        /// <summary>
        /// Notifies controller, that annotations have been loaded
        /// </summary>
        /// <param name="pdfViewerException"></param>
        /// <param name="annots"></param>
        void OnAnnotationsLoaded(PdfViewerException pdfViewerException, IList<PdfAnnotation> annots);

        /// <summary>
        /// Notifies controller, that annotations have been updated
        /// </summary>
        /// <param name="pdfViewerException"></param>
        /// <param name="updateResults"></param>
        void OnAnnotationUpdate(PdfViewerException pdfViewerException, IList<int> updateResults);

        /// <summary>
        /// Notifies controller, that annotations have been deleted
        /// </summary>
        /// <param name="pdfViewerException"></param>
        /// <param name="annotIds"></param>
        void OnAnnotationDeleted(PdfViewerException pdfViewerException, IList<IntPtr> annotIds);

        #endregion [InkingForPDF] Annotation Methods
    }
}
