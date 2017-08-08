/****************************************************************************
 *
 * File:            IPdfDocumens.cs
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
    using PdfTools.PdfViewerCSharpAPI.Utilities;
    using PdfTools.PdfViewerCSharpAPI.Model;
    using PdfTools.PdfViewerCSharpAPI.DocumentManagement;

    /// <summary>
    /// Interface of a Document, which represents a pdf file in its raw form
    /// </summary>
    public interface IPdfDocument : IDisposable
    {
        /// <summary>
        /// Open a new file. This document will from now on represent this file
        /// </summary>
        /// <param name="filename"></param>
        /// <param name="password"></param>
        void Open(string filename, byte[] fileMem, string password);
       
        /// <summary>
        /// Closes the opened file. This document will not represent anything until a new file is opened
        /// </summary>
        void Close();


        IList<PdfTextFragment> LoadTextFragments(int pageNo);

        /// <summary>
        /// Get the amount of pages in this document
        /// </summary>
        int PageCount
        {
            get;
        }


        PdfSourceRect GetPageRect(int pageNo);

        void Draw(WriteableBitmap bitmap, int rotation, IList<KeyValuePair<int, PdfSourceRect>> pageRects, PdfViewerController.Viewport viewport);

        WriteableBitmap LoadThumbnail(double sourceWidth, double sourceHeight, int targetWidth, int targetHeight, int page, Resolution resolution);

        TPageLayoutMode GetPageLayout();
        PdfDestination GetOpenActionDestination();
        IList<PdfOutlineItem> GetOutlines(int parentId);

        #region [InkingForPDF] Annotation Methods

        /// <summary>
        /// Returns a List of all Annotations on a page
        /// </summary>
        /// <param name="pageNr"></param>
        /// <returns></returns>
        IList<PdfAnnotation> GetAnnotations(int pageNr);

        /// <summary>
        /// Creates a new Annotation
        /// </summary>
        /// <param name="annotType"></param>
        /// <param name="pageNr"></param>
        /// <param name="annotPoints"></param>
        /// <param name="annotPointsLength"></param>
        /// <param name="color"></param>
        /// <param name="colorLength"></param>
        /// <param name="strokeWidth"></param>
        /// <returns></returns>
        IntPtr CreateAnnotation(PdfDocument.TPdfAnnotationType annotType, int pageNr, double[] annotPoints, int annotPointsLength, double[] color, int colorLength, double strokeWidth);

        /// <summary>
        /// Retrieves pointer and size of the annotation array
        /// </summary>
        /// <param name="pageNr"></param>
        /// <param name="annotsPointer"></param>
        /// <param name="annotsLength"></param>
        /// <returns></returns>
        bool LoadAnnotations(int pageNr, out IntPtr annotsPointer, out int annotsLength);

        /// <summary>
        /// Updates an annotation with new values
        /// </summary>
        /// <param name="annotId"></param>
        /// <param name="pageNr"></param>
        /// <param name="boundingBox"></param>
        /// <param name="content"></param>
        /// <param name="label"></param>
        /// <param name="color"></param>
        /// <param name="strokeWidth"></param>
        /// <returns></returns>
        int UpdateAnnotation(IntPtr annotId, int pageNr, double[] boundingBox, string content, string label, double[] color, double strokeWidth);

        /// <summary>
        /// Deletes an annotation with the given id
        /// </summary>
        /// <param name="annotId"></param>
        void DeleteAnnotation(IntPtr annotId);

        /// <summary>
        /// Saves the document at the given path
        /// </summary>
        /// <param name="filePath"></param>
        /// <returns></returns>
        bool SaveAs(string filePath);

        #endregion [InkingForPDF] Annotation Methods
    }
}
