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
        IList<PdfAnnotation> LoadAnnotations(int argsPageNr);


        IntPtr CreateAnnotation(PdfDocument.TPdfAnnotationType eType, int iPage, double[] r, int iLen, double[] color, int nColors, double dBorderWidth);
        bool GetAnnotations(int pageNo, out IntPtr pdfAnnotations, ref int count);
        void DeleteAnnotation(IntPtr anno);
        bool SaveAs( string fileName);
    }
}
