/****************************************************************************
 *
 * File:            IPdfViewervoid cs
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
    using System.Drawing;
    using System.Windows;
    using System.Windows.Media.Imaging;
    using PdfTools.PdfViewerCSharpAPI.Utilities;
    using PdfTools.PdfViewerCSharpAPI.DocumentManagement.Requests;
    using PdfTools.PdfViewerCSharpAPI.Annotations;


    #region Configuration Enums



    /// <summary>
    /// The mode which defines whether the whole page or the page width fits into the view port or the page is shown in its true size.
    /// </summary>
    public enum FitMode
    {
        /// <summary>No fit mode.</summary>
        FitNone,
        /// <summary>The window is zoomed to fit the whole page into the viewport.</summary>
        FitPage,
        /// <summary>The window is zoomed to fit the page's width into the viewport.</summary>
        FitWidth,
        /// <summary>The window is zoomed to reflect the true size of the page.</summary>
        FitTrueSize
    };

    

    public enum TPageLayoutMode { None = 0, SinglePage = 1, OneColumn = 2, TwoColumnLeft = 3, TwoColumnRight = 4, TwoPageLeft = 5, TwoPageRight = 6 };

    public enum TDestination
    {
        eDestinationInvalid = 0, eDestinationFit = 1, eDestinationFitH = 2, eDestinationFitV = 3, eDestinationFitR = 4, eDestinationFitB = 5, eDestinationFitBH = 6, eDestinationFitBV = 7, eDestinationXYZ = 8
    };

    public enum TMouseMode { eMouseUndefMode, eMouseMoveMode, eMouseZoomMode, eMouseMarkMode, eMouseSelectMode, eMouseFreehandAnnotationMode };

    public enum TViewerTab { eOutlineTab, eThumbnailTab, eNone };

    /// <summary>
    /// The resolution in dots per inch (dpi).
    /// </summary>
    public struct Resolution
    {
        /// <summary>Create a Resolution structure.</summary>
        public Resolution(double xdpi, double ydpi) { this.xdpi = xdpi; this.ydpi = ydpi; }
        /// <summary>The horizontal resolution in dpi.</summary>
        public double xdpi;
        /// <summary>The vertical resolution in dpi.</summary>
        public double ydpi;
    };

    public struct Destination
    {
        public Destination(int page, double x, double y, double zoom) { this.page = page; this.x = x; this.y = y; this.zoom = zoom; }
        public int page;    // The page number (1 .. number of pages)
        public double x;    // The x position in user units, 0 = not set (raw pdf page coordinates, make no assumptions about the position based on raw coordinates)
        public double y;    // The y position in user units, 0 = not set (raw pdf page coordinates, make no assumptions about the position based on raw coordinates)
        public double zoom; // The zoom factor in percent (100.0 = true size, 0 = not set)
    };


    #endregion




    /// <summary>
    /// ViewerController, controlls the state of the viewer and produces the bitmap that can be drawn to the pane
    /// </summary>
    public interface IPdfViewerController : IDisposable
    {
        /// <summary>
        /// Open a new document
        /// </summary>
        /// <param name="filename">Path of the file to be opened</param>
        /// <param name="password">Password to open the file</param>
        /// <returns>Whether opening was succesful</returns>
        bool Open(string filename, string password);

        /// <summary>
        /// Open a new document
        /// </summary>
        /// <param name="memBlock">memory block containing the file</param>
        /// <param name="password">Password to open the file</param>
        /// <returns>Whether opening was succesful</returns>
        bool OpenMem(byte[] memBlock, string password);

        /// <summary>
        /// Close the opened document
        /// </summary>
        void Close();

        /// <summary>
        /// Scrolls by a specific deltavalue in pixels
        /// </summary>
        /// <param name="horizontalDelta">horizontal pixels</param>
        /// <param name="verticalDelta">vertical pixels</param>
        void Scroll(int horizontalDelta, int verticalDelta);

        /// <summary>
        /// Scrolls by a specific deltavalue in pixels
        /// </summary>
        /// <param name="delta">vector to scroll</param>
        void Scroll(Vector delta);


        /// <summary>
        /// Scrolls to a specific horizontal location in the document
        /// </summary>
        /// <param name="percentage">The location on the canvas to center given as a percentage of the total canvas width [0,1]</param>
        void ScrollToHorizontalPercentage(double percentage);

        /// <summary>
        /// Scrolls to a specific vertical location in the document
        /// </summary>
        /// <param name="percentage">The location on the canvas to center given as a percentage of the total canvas height [0,1]</param>
        void ScrollToVerticalPercentage(double percentage);

        /// <summary>
        /// Reposition the viewport to see a speficic page
        /// </summary>
        /// <param name="pageNo">The page to focus on</param>
        void SetPageNo(int pageNo);

        /// <summary>
        /// Get a destination representation, that allows to later go back to the same location in the pdf. Location means XYZ coordinates, but with resized viewport, appearance may differ.
        /// </summary>
        /// <returns>destination, that can later be used in SetDestination</returns>
        /// <exception cref="PdfTools.PdfViewerCSharpAPI.Utilities.PdfNoFileOpenedException">Thrown when there is no file opened to get destination</exception>
        PdfDestination GetDestination();
        /// <summary>
        /// Reposition the viewport to a destination
        /// </summary>
        /// <param name="destination">the destination to move the viewport to</param>
        void SetDestination(PdfDestination destination);



        PdfSourceRect GetPageRectGuess(int page);

        /// <summary>
        /// Zoom the viewport as much as possible to still contain the given rectangle
        /// </summary>
        /// <param name="rect">The rectangle which shoudl still be visible in the viewport</param>
        void ZoomToRectangle(PdfTargetRect rect);

        /// <summary>
        /// Update the dimensions of the viewport due to resizing of the viewer pane
        /// </summary>
        /// <param name="width">new width</param>
        /// <param name="height">new height</param>
        void UpdateViewportDimensions(int width, int height);



        /// <summary>
        /// Change the zoomFactor of the viewport and zoom zentered an a specific location
        /// </summary>
        /// <param name="point">The location to zoom centered to</param>
        /// <param name="delta">The delta of the zoomFactor</param>
        void ZoomCenteredOnPosition(PdfTargetPoint point, double delta);

        /// <summary>
        /// Pauses rendering of this control, until Resume() is called
        /// </summary>
        void SuspendLayout();

        /// <summary>
        /// Ends pausing the renderer and forces an immediate rendering call
        /// </summary>
        void ResumeLayout();


        /// <summary>
        /// get the number of pages in the document
        /// </summary>
        int PageCount { get; }


        int PageCacheSlidingWindowSize
        {
            set;
            get;
        }

        /// <summary>
        /// Retrieves all textfragments, that inersect with the given region
        /// </summary>
        /// <param name="markedRect">Rectangle of the requested region relative on viewerPane</param>
        /// <returns>List of Textfragments within the marked rectangle</returns>
        /// <exception cref="PdfTools.PdfViewerCSharpAPI.Utilities.PdfNoFileOpenedException">Thrown when there is no file opened to get text</exception>
        IList<PdfTextFragment> GetTextWithinRegion(PdfTargetRect markedRect);


        /// <summary>
        /// 
        /// </summary>
        /// <param name="start">Point where to start selecting text</param>
        /// <exception cref="PdfTools.PdfViewerCSharpAPI.Utilities.PdfNoFileOpenedException">Thrown when there is no file opened to get text</exception>
        void SetTextSelectionStartPoint(PdfTargetPoint start);

        IList<PdfTextFragment> GetTextWithinSelection(PdfTargetPoint start, PdfTargetPoint end, ref double startS, ref double endS);

        bool ScrollingToNextPageEnabled { get; set; }


        bool SearchMatchCase { set; get; }
        bool SearchWrap { set; get; }
        bool SearchPrevious { set; get; }
        bool SearchUseRegex { set; get; }

        /// <summary>
        /// Get all textfragments on a given pageRange
        /// </summary>
        /// <param name="firstPage"></param>
        /// <param name="lastPage"></param>
        /// <returns>List of TextFragments within the given page range</returns>
        /// <exception cref="PdfTools.PdfViewerCSharpAPI.Utilities.PdfNoFileOpenedException">Thrown when there is no file opened to get text</exception>
        IList<PdfTextFragment> GetTextOnPages(int firstPage, int lastPage);

        /// <summary>
        /// The documents user unit measure in points (1 pt = 1/72 inch)
        /// </summary>
        double UserUnit { get; }

        /// <summary>
        /// Get and set the additional rotation caused by viewer manipulation
        /// </summary>
        int Rotate { get; set; }

        /// <summary>
        /// Get and set the size of the border in between and around pages
        /// </summary>
        double Border { get; set; }

        string FileName { get; }

        /// <summary>
        /// Get and set the Resolution that should be used for the bitmap
        /// </summary>
        Resolution Resolution { get; set; }


        /// <summary>
        /// Get and set the PageDisplayMode that should be used
        /// </summary>
        TPageLayoutMode PageLayoutMode { get; set; }

        /// <summary>
        /// Get and set the FitMode that should be used
        /// </summary>
        FitMode FitMode { get; set; }

        /// <summary>
        /// Get and set the zoomfactor of the viewport
        /// </summary>
        double ZoomFactor { get; set; }

        /// <summary>
        /// Get the pagenumber of the first page visible of the viewport
        /// </summary>
        int FirstPageOnViewport { get; }

        /// <summary>
        /// Get the pagenumber of the last page visible of the viewport
        /// </summary>
        int LastPageOnViewport { get; }

        /// <summary>
        /// Set and get the viewer page order of the document. The document order in the pdf remains the same.
        /// </summary>
        IList<int> PageOrder { get; set; }

        /// <summary>
        /// Get the inverse page order to know which document page corresponds to which viewer page
        /// </summary>
        IList<int> InversePageOrder { get; }

        /// <summary>
        /// Get and set whether the viewer should ignore the preferences embedded in the document
        /// </summary>
        bool IgnoreEmbeddedPreferences { get; set; }

        /// <summary>
        /// transforms a rectangle that is given in unrotated pagecoordinates to viewport coordinates
        /// </summary>
        /// <param name="rectOnUnrotatedPage">The rectangle on the unrotated page</param>
        /// <param name="pageNo">The page the rectangle is on</param>
        /// <returns>The rectangle in viewport coordinates for WPF components</returns>
        /// <exception cref="PdfTools.PdfViewerCSharpAPI.Utilities.PdfNoFileOpenedException">Thrown when there is no file opened</exception>
        Rect TransformRectPageToViewportWinRect(PdfSourceRect rectOnUnrotatedPage, int pageNo);

        /// <summary>
        /// transforms a rectangle that is given in unrotated pagecoordinates to viewport coordinates
        /// </summary>
        /// <param name="rectOnUnrotatedPage">The rectangle on the unrotated page</param>
        /// <param name="pageNo">The page the rectangle is on</param>
        /// <returns>The rectangle in viewport coordinates for WinForms components</returns>
        /// <exception cref="PdfTools.PdfViewerCSharpAPI.Utilities.PdfNoFileOpenedException">Thrown when there is no file opened</exception>
        Rectangle TransformRectPageToViewportDrawRect(PdfSourceRect rectOnUnrotatedPage, int pageNo);
        /// <summary>
        /// transforms a rectangle that is given on the screen to a rectangle on canvas
        /// </summary>
        /// <param name="rect">rectangle on screen</param>
        /// <returns>rectangle on canvas</returns>
        /// <exception cref="PdfTools.PdfViewerCSharpAPI.Utilities.PdfNoFileOpenedException">Thrown when there is no file opened</exception>
        PdfSourceRect TransformOnScreenToOnCanvas(PdfTargetRect rect);
        /// <summary>
        /// Transforms a Point on screen to unrotated(!) pagecoordinates.
        /// </summary>
        /// <param name="point">Point on screen</param>
        /// <param name="page">Reference to the page that the point was on. This is passed by reference and is to be used as output of the method</param>
        /// <returns>Point on unrotated page</returns>
        /// <exception cref="PdfTools.PdfViewerCSharpAPI.Utilities.PdfNoFileOpenedException">Thrown when there is no file opened</exception>
        PdfSourcePoint TransformOnScreenToOnPage(PdfTargetPoint point, ref int page);


        void OpenOutlineItem(int outlineId);

        /// <summary>
        /// Load thumbnail and save to cache
        /// </summary>
        /// <param name="pageNo"></param>
        /// <param name="guaranteeExactness"></param>
        /// <returns></returns>
        /// <exception cref="PdfTools.PdfViewerCSharpAPI.Utilities.PdfNoFileOpenedException">Thrown when there is no file opened</exception>
        APdfRequest<ThumbnailCacheArgs, WriteableBitmap> LoadThumbnail(int pageNo, bool guaranteeExactness);


        /// <summary>
        /// Get thumbnail from cache
        /// </summary>
        /// <param name="pageNo"></param>
        /// <returns>bitmap of the thumbnail</returns>
        WriteableBitmap GetThumbnail(int pageNo);

        void CancelRequest(IPdfRequest request);

        int ThumbnailWidth { set; get; }
        int ThumbnailHeight { set; get; }

        bool IsOpen { set; get; }

        void Initialize();
        void NextPage();
        void PreviousPage();

        void Search(string toSearch, int startPage, int startIndex);



        Action<Action> InvokeCallback { get; }

        #region Events
        event Action<WriteableBitmap> BitmapChanged;
        event Action<PdfTargetRect> ScrollableAreaChanged;
        event Action<PdfTargetRect> ViewportRectangleChanged;
        event Action<PdfSearcher.SearchResult> SearchCompleted;

        event Action<PdfViewerException> OpenCompleted;
        event Action<PdfViewerException> CloseCompleted;
        event Action<int, int> VisiblePageRangeChanged;
        event Action<int> PageCountChanged;
        event Action<FitMode> FitModeChanged;
        event Action<TPageLayoutMode> PageLayoutModeChanged;
        event Action<Resolution> ResolutionChanged;
        event Action<double> BorderChanged;
        event Action<double> ZoomChanged;
        event Action<int> RotationChanged;
        event Action<int, IList<PdfOutlineItem>, PdfViewerException> OutlinesChanged;
        //event Action<int, int, WriteableBitmap, PdfViewerException> ThumbnailsChanged;
        event EventHandler<PdfViewerController.ThumbnailsChangedArgs> ThumbnailsChanged;
        event Action<IList<int>> PageOrderChanged;



        #endregion Events

        IPdfCanvas GetCanvas();

        void SaveAs(string fileName);

        IList<PdfAnnotation> GetAllAnnotationsOnPage(int pageNr);
        void CreateAnnotation(PdfAnnotation pdfAnnotation);
    }

}
