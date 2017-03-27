/****************************************************************************
 *
 * File:            IPdfCanvas.cs
 *
 * Description:     The 3-Heights™  Pdf Viewer Control for WPF
 *
 * Copyright:       Copyright (C) 2015 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *                  
 ***************************************************************************/

namespace PdfTools.PdfViewerCSharpAPI.Model
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;
    using System.Windows.Media.Imaging;
    using System.Windows;
    using PdfTools.PdfViewerCSharpAPI.Utilities;
    using PdfTools.PdfViewerCSharpAPI.DocumentManagement;

    /// <summary>
    /// Representation of the canvas, which is a 2D-plane on which all pages of the associated document are placed
    /// </summary>
    public interface IPdfCanvas : IDisposable
    {

        /// <summary>
        /// Get and set the size of the border in between and around pages
        /// </summary>
        double BorderSize { set; get; }

        /// <summary>
        /// Get and set the additional rotation caused by viewer manipulation
        /// </summary>
        int Rotation { set; get; }

        /// <summary>
        /// Set the pageLayoutMode to be used
        /// </summary>
        TPageLayoutMode PageLayoutMode { set;  }


        /// <summary>
        /// Gets the DocumentManager, which handles the document access
        /// </summary>
        IPdfDocumentManager DocumentManager{ get; }

        /// <summary>
        /// Open a new document
        /// </summary>
        /// <param name="filename">Path of the file to be opened</param>
        /// <param name="password">Password to open the file</param>
        /// <returns>Whether opening was successful</returns>
        void Open(string filename, byte[] fileMem, string password);

        /// <summary>
        /// Close the opened document
        /// </summary>
        void Close();

        /// <summary>
        /// gets all textfragments that intersect the given rectangle
        /// </summary>
        /// <param name="markedRect"></param>
        /// <returns></returns>
        IList<PdfTextFragment> GetTextWithinRegion(PdfSourceRect markedRect, int firstPageNo, int lastPageNo);

        IList<PdfTextFragment> GetTextWithinSelection(PdfSourcePoint start, PdfSourcePoint end, int firstPageNo, int lastPageNo, ref bool swap);

        /// <summary>
        /// gets all textfragments that lie on the provided pages
        /// </summary>
        /// <param name="firstPageNo"></param>
        /// <param name="lastPageNo"></param>
        /// <returns></returns>
        IList<PdfTextFragment> GetTextWithinPageRange(int firstPageNo, int lastPageNo);

        /// <summary>
        /// Get the rectangle of the current canvas
        /// </summary>
        PdfSourceRect CanvasRect { get; }

        /// <summary>
        /// Get number of pages in the document
        /// </summary>
        int PageCount { get; }

        /// <summary>
        /// Set the current topmost page on the viewport (only relevant if LayoutMode == LayoutPage)
        /// </summary>
        int PageNo { set; }

        /// <summary>
        /// Set and the page order of the document.
        /// </summary>
        IList<int> PageOrder { get; set; }

        /// <summary>
        /// Get the 
        /// </summary>
        IList<int> InversePageOrder { get; }

        /// <summary>
        /// Get the rectangle of a page in canvas coordinates
        /// </summary>
        /// <param name="pageNo">The page to get</param>
        /// <returns>the pageRectangle</returns>
        PdfSourceRect GetPageRect(int pageNo);


        PdfSourceRect GetPageRectGuaranteedExactly(int pageNumber);

        /// <summary>
        /// Get the union of the rectangle of a specific pages and its neighbour including borders around these rectangles
        /// (if PageDisplay == PageDisplayDoubleWithTitlePage or PageDisplayDouble each page may have a neighbour)
        /// </summary>
        /// <param name="pageNo">The page to look up</param>
        /// <returns> unionRectangle </returns>
        PdfSourceRect GetUnionRectangleWithNeighbour(int pageNo);

        event Action<PdfSourceRect, PdfSourceRect, int> CanvasRectChanged;
    }
}
