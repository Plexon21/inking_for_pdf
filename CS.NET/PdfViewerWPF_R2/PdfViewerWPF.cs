/****************************************************************************
 *
 * File:            PdfViewerWPF.cs
 *
 * Description:     The 3-Heights™  Pdf Viewer Control for WPF
 *
 * Copyright:       Copyright (C) 2013 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *                  
 ***************************************************************************/

using System;
using System.Threading;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Threading;
using System.Text;

using PdfTools.PdfViewerWPF.CustomControls;
using PdfTools.PdfViewerCSharpAPI.Model;
using PdfTools.PdfViewerCSharpAPI.Utilities;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Collections.Generic;
using System.ComponentModel;


namespace PdfTools.PdfViewerWPF
{
    /// <summary>
    /// The 3-Heights™  PDF viewer control for WPF to display various file formats.
    /// </summary>
    public class PdfViewerWPF : Control, IDisposable, INotifyPropertyChanged
    {

        /// <summary>
        /// Sets the style of the control on first usage of the class.
        /// </summary>
        /// <remarks>
        /// This must be done in the static constructor to make sure that it is only called once (and before an actual instance of the control is created).
        /// </remarks>
        static PdfViewerWPF()
        {
            DefaultStyleKeyProperty.OverrideMetadata(typeof(PdfViewerWPF), new FrameworkPropertyMetadata(typeof(PdfViewerWPF)));
        }

        private IPdfViewerController controller = null;
        private PdfViewerComponent mainViewer;
        private PdfViewerPane pane;
        private TabControl navigationTabControl;
        private TabItem thumbnailTabItem, outlinesTabItem;
        private Grid grid;
        private GridLength navigationTabLength = new GridLength(2, GridUnitType.Star);
        private GridLength gridsplitterLength = new GridLength(10, GridUnitType.Pixel);

        private ThumbnailView thumbnailView;
        private OutlinesView outlinesView;
            


        /// <summary>
        /// Holds the Path  of the log file that is written. 
        /// Empty string means no log file is written.
        /// Default is empty string.
        /// </summary>
        public static String LogFilePath
        {
            set
            {
                Logger.FileName = value;
            }
            get
            {
                return Logger.FileName;
            }
        }

        protected override void OnPreviewMouseDown(MouseButtonEventArgs e)
        {
            this.Focus();
            base.OnPreviewMouseDown(e);
        }

        /// <summary>
        /// Create the viewer control.
        /// </summary>
        public PdfViewerWPF()
        {
            Logger.LogInfo("Creating PdfViewerWPF instance");

            controller = new PdfViewerController(BeginInvokeCallbackOnDispatcher);
            pane = new PdfViewerPane();
            Logger.LogInfo("Created PdfViewerWPF instance");

            pane.TextSelected += OnTextSelected;

            UseLayoutRounding = true;
            SnapsToDevicePixels = true;

            controller.PageCountChanged += OnPageCountChanged;
            controller.VisiblePageRangeChanged += OnPageNoChanged;
            controller.FitModeChanged += OnFitModeChanged;
            controller.PageLayoutModeChanged += OnPageLayoutModeChanged;
            controller.ResolutionChanged += OnResolutionChanged;
            controller.BorderChanged += OnBorderChanged;
            controller.ZoomChanged += OnZoomChanged;
            controller.RotationChanged += OnRotationChanged;
            controller.SearchCompleted += OnSearchCompleted;
            controller.OpenCompleted += OnOpenCompleted;
            controller.Initialize();
        }

        /// <summary>
        /// Destroy the viewer control.
        /// </summary>
        ~PdfViewerWPF()
        {
        }

        /// <summary>
        /// Release allocated unmanaged resources.
        /// </summary>
        public void Dispose()
        {
            Logger.LogInfo("Disposing");
            Dispose(true);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (disposing)
            {
                controller.Dispose();
            }
            Logger.SaveLog();
        }


        /// <summary>
        /// invoke the given callback on the Thread which owns the PdfViewerWinForms_R2 control. Returns after execution of callback terminates
        /// </summary>
        /// <param name="run">The callback to execute</param>
        public void BeginInvokeCallbackOnDispatcher(Action run)
        {
            Application.Current.Dispatcher.BeginInvoke(run, null);
        }

        /// <summary>
        /// invoke the given callback on the Thread which owns the PdfViewerWinForms_R2 control. Returns immediately (not necessarily after termination of callback)
        /// </summary>
        /// <param name="run">The callback to execute</param>
        public void InvokeCallbackOnDispatcher(Action run)
        {
            Application.Current.Dispatcher.Invoke(run, null);
        }

        /// <summary>
        /// Open a document from an input file.
        /// This method makes the objects in the document accessible. If there is an already open document it is closed first.
        /// </summary>
        /// <param name="filename">
        /// 
        /// The input file name and optionally the file path, drive or server string according to the operating systems file name specification rules.
        /// </param>
        /// <param name="password">
        /// The user or the owner password of the encrypted PDF document.
        /// If this parameter is null or an empty string the default (i.e. no password) is used.
        /// If the document is not a PDF document this parameter is ignored.
        /// </param>
        /// <returns>True if the file could successfully be opened. False if the file does not exist, is not accessible or the password is wrong.</returns>
        public bool Open(string filename, string password)
        {
            Logger.LogInfo("Opening");
            bool b = controller.Open(filename, password);
            pane.OverrideMouseModeToWait = true; //this is after Open call so it only gets triggered if there is no exception thrown
            Logger.LogInfo("Open returned");
            return b;
        }

        /// <summary>
        /// Open a document from a memory block.
        /// It makes the objects in the document accessible. If there is an already open document it is closed first.
        /// </summary>
        /// <param name="memBlock">
        /// Memory block containing the document stream as one dimensional byte array.
        /// </param>
        /// <param name="password">
        /// The user or the owner password of the encrypted PDF document.
        /// If this parameter is null or an empty string the default (i.e. no password) is used.
        /// If the document is not a PDF document this parameter is ignored.
        /// </param>
        /// <returns>True if the file could successfully be opened. False if the file does not exists, is not accessible or the password is wrong.</returns>
        public bool OpenMem(byte[] memBlock, string password)
        {
            Logger.LogInfo("Opening Mem");
            bool b = controller.OpenMem(memBlock, password);
            pane.OverrideMouseModeToWait = true; //this is after Open call so it only gets triggered if there is no exception thrown
            Logger.LogInfo("Open returned");
            return b;
        }

        /// <summary>
        /// Close the currently open document.
        /// </summary>
        /// <returns>True if document is closed.</returns>
        public bool Close()
        {
            Logger.LogInfo("Closing");
            controller.IsOpen = false;
            controller.Close();
            Logger.LogInfo("Closed");
            return true;
        }


        /// <summary>
        /// Pauses rendering of this control, until ResumeLayout() is called
        /// </summary>
        public void SuspendLayout()
        {
            Logger.LogInfo("Suspending Layout");
            controller.SuspendLayout();
        }

        /// <summary>
        /// Ends pausing the renderer and forces an immediate rendering call
        /// </summary>
        public void ResumeLayout()
        {
            Logger.LogInfo("Resuming Layout");
            controller.ResumeLayout();
        }


        private Action<string, bool> searchResultDelegate = null;



        /// <summary>
        /// Searches the passed text in the document.
        /// The search starts at the current page.
        /// In the case of a successful search, the document is scrolled
        /// to the next occurence of the text and the found string is highlighted.
        /// </summary>
        /// <param name="searchText">Is the text to search for.</param>
        /// <param name="resultDelegate">Callback for the result of the current search.</param>
        /// <exception cref="PdfTools.PdfViewerCSharpAPI.Utilities.PdfNoFileOpenedException">Throws if no file is open.</exception>
        [Obsolete("Usage of resultDelegate is deprecated. Use Search(string) instead and listen to SearchCompleted event")]
        public void Search(string searchText, Action<string, bool> resultDelegate)
        {
            this.searchResultDelegate = resultDelegate;
            searchPage = Math.Max(controller.FirstPageOnViewport, Math.Min(controller.LastPageOnViewport, searchPage));//~~
            controller.Search(searchText, searchPage, searchIndex);
        }

        /// <summary>
        /// Searches the passed text in the document.
        /// The search starts at the current page.
        /// In the case of a successful search, the document is scrolled
        /// to the next occurence of the text and the found string is highlighted.
        /// </summary>
        /// <param name="searchText">Is the text to search for.</param>
        /// <exception cref="PdfTools.PdfViewerCSharpAPI.Utilities.PdfNoFileOpenedException">Throws if no file is open.</exception>
        public void Search(string searchText)
        {
            Logger.LogInfo("Searching");
            searchResultDelegate = null;
            searchPage = Math.Max(controller.FirstPageOnViewport, Math.Min(controller.LastPageOnViewport, searchPage));//~~
            controller.Search(searchText, searchPage, searchIndex);
        }
        private int searchIndex = 0;
        private int searchPage = 1;


        /// <summary>
        /// The number of pages in the document.
        /// </summary>
        /// <exception cref="PdfTools.PdfViewerCSharpAPI.Utilities.PdfNoFileOpenedException">Throws if no file is open.</exception>
        public int PageCount
        {
            get
            {
                return controller.PageCount;
            }
        }

        /// <summary>
        /// Get whether there is currently a file open in the viewer.
        /// If a file is newly opened, this property will remain false until the opening action has completed
        /// </summary>
        public bool IsOpen
        {
            get
            {
                return controller.IsOpen;
            }
        }

        /// <summary>
        /// Get and Set new page order
        /// </summary>
        public IList<int> PageOrder
        {
            get { return controller.PageOrder; }
            set 
            { 
                controller.PageOrder = value;
                RaisePropertyChanged("PageCount");
            }
        }

        /// <summary>
        /// The actual destination in the document.
        /// The page denotes the first page in the viewport.
        /// The x and y positions denote the top left corner in the viewport.
        /// </summary>
        [Obsolete("Use ViewportDestination property instead")]
        public Destination Destination
        {
            get
            {
                PdfDestination d = controller.GetDestination();
                return new Destination(d.Page, d.RectOnPage.dX, d.RectOnPage.dY, d.Zoom(Zoom));
            }
            set
            {
                ViewportDestination = new PdfDestination(value.page, TDestination.eDestinationXYZ, value.x, value.y, 0.0, 0.0, value.zoom);
            }
        }
        /// <summary>
        /// The Destination of the viewport in the document.
        /// </summary>
        public PdfDestination ViewportDestination
        {
            get
            {
                return controller.GetDestination();
            }
            set
            {
                controller.SetDestination(value);
            }
        }

        /// <summary>
        /// Get the rectangle of the viewerPane in screencoordinates
        /// </summary>
        public Rect ViewerPaneRectangle
        {
            get
            {
                Point p1 = pane.PointToScreen(new Point(0.0, 0.0));
                Point p2 = pane.PointToScreen(new Point(pane.ActualWidth, pane.ActualHeight));
                Rect r = new Rect(p1, p2);
                return r;
            }
        }

        /// <summary>
        /// Get a string of all text on a range of pages. 
        /// The text is concatenated in the same order that the text objects are saved in the pdf
        /// (Depending on the creator of said pdf this might not be the human reading order)
        /// </summary>
        /// <param name="startPage">The first page that gets extracted</param>
        /// <param name="endPage">The last page that gets extracted</param>
        /// <returns>Text on given pages</returns>
        /// <exception cref="PdfTools.PdfViewerCSharpAPI.Utilities.PdfNoFileOpenedException">Throws if no file is open.</exception>
        public string GetTextOnPages(int startPage, int endPage)
        {
            IList<PdfTextFragment> frags = controller.GetTextOnPages(startPage, endPage);
            StringBuilder text = new StringBuilder("");
            foreach (PdfTextFragment frag in frags)
                text.Append(frag.Text).Append(" ");

            return text.ToString();
        }

        /// <summary>
        /// Get a reference to the thumbnails that are loaded for the current document. 
        /// Be aware, that the images for thumbnails are loaded dynamically depending on the visible thumbnails in the sidebar
        /// </summary>
        /// <returns>ItemCollection containing instances of type Pdftools.PdfViewerWPF.CustomControls.OutlinesView.OutlineTreeViewItem</returns>
        public ItemCollection GetThumbnails()
        {
            return (thumbnailView == null) ? null : thumbnailView.GetThumbnails();
        }

        /// <summary>
        /// Request loading the thumbnail image of a page from the Pdf file.
        /// The result will be returned asynchronously in event Pdftools.PdfViewerWPF.ThumbnailLoaded
        /// </summary>
        /// <param name="page">The page to load</param>
        public void RequestThumbnail(int page)
        {
            controller.LoadThumbnail(controller.InversePageOrder[page-1], true);
        }

        /// <summary>
        /// The width of thumbnailItembitmaps
        /// </summary>
        public int ThumbnailWidth
        {
            set
            {
                controller.ThumbnailWidth = value;
            }
            get
            {
                return controller.ThumbnailWidth;
            }
        }
        /// <summary>
        /// The height of thumbnailItembitmaps
        /// </summary>
        public int ThumbnailHeight
        {
            set
            {
                controller.ThumbnailHeight = value;
            }
            get
            {
                return controller.ThumbnailHeight;
            }
        }
        /// <summary>
        /// Get a reference to the outlines that are loaded for the current document.
        /// Be aware that outlines are loaded dynamically whenever the user expands a outline node in the tree.
        /// </summary>
        /// <returns>ItemCollection containing instances of type Pdftools.PdfViewerWpf.CustomControls.ThumbnailView.ThumbnailItem</returns>
        public TreeView GetOutlinesTreeView()
        {
            return (outlinesView == null) ? null : outlinesView.OutlineTreeView;
        }

        private TViewerTab _selectedViewerTab = TViewerTab.eOutlineTab;
        /// <summary>
        /// The selected tab on the sidebar. This selection determines which tab is shown in the sidebar.
        /// </summary>
        public TViewerTab SelectedViewerTab
        {
            set
            {
                _selectedViewerTab = value;
                if (navigationTabControl != null)
                    ApplySelectedViewerTab();
            }
            get
                {
                return _selectedViewerTab;
            }
        }

        private void NavigationTabControlSelectionChangedEventHandler(object sender, SelectionChangedEventArgs args)
        {
            if (navigationTabControl.SelectedItem == outlinesTabItem)
                _selectedViewerTab = TViewerTab.eOutlineTab;
            else if (navigationTabControl.SelectedItem == thumbnailTabItem)
                _selectedViewerTab = TViewerTab.eThumbnailTab;
            else
                _selectedViewerTab = TViewerTab.eNone;
        }

        private void ApplySelectedViewerTab()
        {
            switch (_selectedViewerTab)
            {
                    case TViewerTab.eOutlineTab:
                        navigationTabControl.SelectedItem = outlinesTabItem;
                        break;
                    case TViewerTab.eThumbnailTab:
                        navigationTabControl.SelectedItem = thumbnailTabItem;
                        break;
                    default:
                    throw new ArgumentException("The given tab " + _selectedViewerTab.ToString() + " cannot be selected");
                }
            }


        /// <summary>
        /// Called when the internal templates are loaded.
        /// This is a good time to retrieve references to the internal parts of this control. 
        /// </summary>
        public override void OnApplyTemplate()
        {
            base.OnApplyTemplate();
            Logger.LogInfo("Applying template");
            mainViewer = this.Template.FindName("PART_MainViewer", this) as PdfViewerComponent;
            outlinesView = this.Template.FindName("PART_OutlinesView", this) as OutlinesView;
            thumbnailView = this.Template.FindName("PART_ThumbnailView", this) as ThumbnailView;

            navigationTabControl = this.Template.FindName("PART_NavigationTabControl", this) as TabControl;
            thumbnailTabItem = this.Template.FindName("PART_ThumbnailViewTab", this) as TabItem;
            outlinesTabItem = this.Template.FindName("PART_OutlinesViewTab", this) as TabItem;
            grid = this.Template.FindName("PART_MainGrid", this) as Grid;

            outlinesView.SetController(controller);
            thumbnailView.SetController(controller);
            pane.SetController(controller);
            mainViewer.SetController(controller);
            mainViewer.SetViewerPane(pane);
            pane.MouseModeChanged += OnMouseModeChanged;
            navigationTabControl.SelectionChanged += NavigationTabControlSelectionChangedEventHandler;
            ApplySelectedViewerTab();
            ShowOutlines = _showOutlines;
            ShowThumbnails = _showThumbnails;
            pane.Focus();

            Logger.LogInfo("Template applied");
        }

        private void OnSearchCompleted(PdfSearcher.SearchResult result)
        {
            if (result != null)
            {
                this.searchIndex = result.Index;
                this.searchPage = result.PageNo;
            }
            if (searchResultDelegate != null)
            {
                searchResultDelegate.BeginInvoke("Why would you want the text back? You ought to know what you are looking for...", result != null, null, null);
            }
            Logger.LogInfo("Search Completed");
            SearchCompleted(result);
        }


        #region PropertyForwarding

        /// <summary>
        /// The documents user unit measure in points (1 pt = 1/72 inch).
        /// </summary>
        ///
        [Obsolete("The user unit is always 1.0")]//TODO: is it?
        public double UserUnit
        {
            get { return 1.0; }
        }


        /// <summary>
        /// Returns the currently selected text, when using TMouseMode.eMouseSelectMode and releasing the mouse button.
        /// </summary>
        public String SelectedText
        {
            get
            {
                return pane.SelectedText;
            }
        }

        /// <summary>
        /// The 'rotate' angle in multiples of 90 degrees.
        /// Each individual page is rotated by the given angle.
        /// The default angle is 0 degrees (no rotation).
        /// </summary>
        public int Rotate
        {
            get
            {
                return controller.Rotate;
            }
            set
            {
                controller.Rotate = value;
                RaisePropertyChanged("Rotate");
            }
        }

        /// <summary>
        /// The border size in user units.
        /// The default border size is 6.0.
        /// </summary>
        public double Border
        {
            get
            {
                return controller.Border;
            }
            set
            {
                controller.Border = value;
                RaisePropertyChanged("Border");
            }
        }

        /// <summary>
        /// The display resolution in dpi.
        /// Initially the resolution is set to the resolution of the display device (normally 96,96).
        /// </summary>
        public Resolution Resolution
        {
            get
            {
                return controller.Resolution;
            }
            set
            {
                try
                {
                    controller.Resolution = value;
                    RaisePropertyChanged("Resolution");
                }
                catch (PdfNoFileOpenedException)
                {
                    Logger.LogInfo("PdfNoFileOpenedException occured when setting Resolution");
                }

            }
        }

        /// <summary>
        /// The size of the sliding window for the internal page cache. Values &lt;0 set a infinitely large window size
        /// </summary>
        public int PageCacheSlidingWindowSize
        {
            get
            {
                return controller.PageCacheSlidingWindowSize;
            }
            set
            {
                controller.PageCacheSlidingWindowSize = value;
            }
        }


        /// <summary>
        /// The fit mode.
        /// The default fit mode is FitWidth.
        /// </summary>
        public FitMode FitMode
        {
            get
            {
                return controller.FitMode;
            }
            set
            {
                controller.FitMode = value;
                RaisePropertyChanged("FitMode");
            }
        }

        /// <summary>
        /// The page layout mode.
        /// </summary>
        public TPageLayoutMode PageLayoutMode
        {
            get
            {
                return controller.PageLayoutMode;
            }
            set
            {
                controller.PageLayoutMode = value;
                RaisePropertyChanged("PageLayoutMode");
            }
        }

        public TMouseMode MouseMode
        {
            get
            {
                return pane.MouseMode;
            }
            set
            {
                pane.MouseMode = value;
                RaisePropertyChanged("MouseMode");
            }
        }

        private bool updatingPageNo = false;
        /// <summary>
        /// The actual page number.
        /// </summary>
        public int PageNo
        {
            get
            {
                return controller.FirstPageOnViewport;
            }
            set
            {
                if (updatingPageNo)
                    return;
                updatingPageNo = true;
                try
                {
                    controller.SetPageNo(value);
                }
                catch (PdfNoFileOpenedException)
                {
                    Logger.LogInfo("PdfNoFileOpenedException occured when setting pageNo");
                }
                updatingPageNo = false;
            }
        }
        /// <summary>
        /// Scroll the viewport to a destination relative to the present one
        /// </summary>
        /// <param name="horizontalDelta"> distance in pixels towards the right</param>
        /// <param name="verticalDelta"> distance in pixels towards the bottom</param>
        public void Scroll(int horizontalDelta, int verticalDelta)
        {
            controller.Scroll(horizontalDelta, verticalDelta);
        }

        /// <summary>
        /// Go to next page (PageNo + 1)
        /// </summary>
        public void NextPage()
        {
            controller.NextPage();
        }

        /// <summary>
        /// Go to previous page (PageNo - 1)
        /// </summary>
        public void PreviousPage()
        {
            controller.PreviousPage();
        }

        /// <summary>
        /// The actual zoom factor.
        /// The default zoom factor is 100%, however,
        /// due to the default FitMode a document may well
        /// have an other zoom after being opened.
        /// </summary>
        public double Zoom
        {
            get
            {
                return controller.ZoomFactor * 100;
            }
            set
            {
                try
                {
                    controller.ZoomFactor = value / 100;
                    RaisePropertyChanged("Zoom");
                }
                catch (PdfNoFileOpenedException)
                {
                    Logger.LogInfo("PdfNoFileOpenedException occured when setting Zoom");
                }
            }
        }



        /// <summary>
        /// Configure whether searching considers casing or not
        /// </summary>
        public bool SearchMatchCase
        {
            get
            {
                return controller.SearchMatchCase;
            }
            set
            {
                controller.SearchMatchCase = value;
                RaisePropertyChanged("SearchMatchCase");
            }
        }


        /// <summary>
        /// Configure whether searching will wrap around when reaching the beginning/end of the document
        /// </summary>
        public bool SearchWrap
        {
            get { return controller.SearchWrap; }
            set
            {
                controller.SearchWrap = value;
                RaisePropertyChanged("SearchWrap");
            }
        }

        /// <summary>
        /// Configure wheather searching will search backwards or forward from the starting position or last found match within the document
        /// </summary>
        public bool SearchPrevious
        {
            get { return controller.SearchPrevious; }
            set
            {
                controller.SearchPrevious = value; 
                RaisePropertyChanged("SearchPrevious");
            }
        }

        /// <summary>
        /// Configure whether searching will interpret the searched string as a regular expression or as plain text
        /// </summary>
        public bool SearchRegex
        {
            get { return controller.SearchUseRegex; }
            set
            {
                controller.SearchUseRegex = value;
            }
        }

        public double AnnotationStrokeWidth
        {
            get
            {
                return pane.AnnotationStrokeWidth;
            }
            set
            {
                pane.AnnotationStrokeWidth = value;
            }
        }


        bool _showOutlines = true;
        /// <summary>
        /// Determines if the Outlines tab is available
        /// The default value is true.
        /// </summary>
        public bool ShowOutlines
        {
            get
            {
                return _showOutlines;
            }
            set
            {
                _showOutlines = value;
                if (outlinesTabItem == null)
                    return;
                if (_showOutlines)
                {
                    outlinesTabItem.Visibility = Visibility.Visible;
                    navigationTabControl.Visibility = Visibility.Visible;
                    if (!navigationTabControl.Items.Contains(outlinesTabItem))
                        navigationTabControl.Items.Add(outlinesTabItem);
                    if (grid.ColumnDefinitions[0].Width.Value == 0.0)
                    {
                        grid.ColumnDefinitions[0].Width = navigationTabLength;
                        grid.ColumnDefinitions[1].Width = gridsplitterLength;
                    }
                    navigationTabControl.SelectedItem = outlinesTabItem;
                }
                else
                {
                    outlinesTabItem.Visibility = Visibility.Collapsed;
                    if (thumbnailTabItem.Visibility == Visibility.Collapsed)
                    {
                        navigationTabControl.Visibility = Visibility.Collapsed;

                        navigationTabLength = grid.ColumnDefinitions[0].Width;
                        grid.ColumnDefinitions[0].Width = new GridLength(0);
                        grid.ColumnDefinitions[1].Width = new GridLength(0);
                    }
                    else
                    {
                        navigationTabControl.SelectedItem = thumbnailTabItem;
                    }
                }
            }
        }

        bool _showThumbnails = true;
        bool _visibilityChanged = true;
        /// <summary>
        /// Determines if the Thumbnails tab is available
        /// The default value is true.
        /// Disabling the thumbnails might yield performance increases when opening very large files.
        /// </summary>
        public bool ShowThumbnails
        {
            get
            {
                return _showThumbnails;
            }
            set
            {
                _visibilityChanged = _showThumbnails != value;
                _showThumbnails = value;
                
                
                
                if (thumbnailTabItem == null)
                    return;



                if (value)
                {
                    if (!thumbnailView.handlersRegistered)
                    {
                        thumbnailView.RegisterHandlers();
                        thumbnailView.handlersRegistered = true;
                    }
                        
                    thumbnailTabItem.Visibility = Visibility.Visible;
                    navigationTabControl.Visibility = Visibility.Visible;
                    if (!navigationTabControl.Items.Contains(thumbnailTabItem))
                        navigationTabControl.Items.Add(thumbnailTabItem);
                    if (grid.ColumnDefinitions[0].Width.Value == 0.0)
                    {
                        grid.ColumnDefinitions[0].Width = navigationTabLength;
                        grid.ColumnDefinitions[1].Width = gridsplitterLength;
                    }
                    navigationTabControl.SelectedItem = thumbnailTabItem;
                }
                else
                {
                    if (thumbnailView.handlersRegistered)
                    {
                        thumbnailView.UnregisterHandlers();
                        thumbnailView.handlersRegistered = false;
                    }

                    thumbnailTabItem.Visibility = Visibility.Collapsed;

                    if (outlinesTabItem.Visibility == Visibility.Collapsed)
                    {
                        navigationTabControl.Visibility = Visibility.Collapsed;
                        navigationTabLength = grid.ColumnDefinitions[0].Width;
                        grid.ColumnDefinitions[0].Width = new GridLength(0);
                        grid.ColumnDefinitions[1].Width = new GridLength(0);
                    }
                    else
                    {
                        navigationTabControl.SelectedItem = outlinesTabItem;
                    }
                }
            }
        }

        // #TODO: Setter for NumberOfThumbnailItems in the list.

        /// <summary>
        /// Configure the viewer to ingore open actions and viewing preferences that are embedded.
        /// This property has to be set before opening a file, to ignore the embedded preferences in said file.
        /// </summary>
        public bool IgnoreEmbeddedPreferences
        {
            get
            {
                return controller.IgnoreEmbeddedPreferences;
            }
            set
            {
                controller.IgnoreEmbeddedPreferences = value;
                RaisePropertyChanged("IgnoreEmbeddedPreferences");
            }
        }
        /// <summary>
        /// Configure whether scrolling to the top or bottom of a page will jump to the next/previous page when using a PageLayoutMode with single or dual pages (SinglePage, TwoPageLeft or TwoPageRight)
        /// Enabled by default
        /// </summary>
        public bool ScrollingToNextPageEnabled
        {
            get
            {
                return controller.ScrollingToNextPageEnabled;
            }
            set
            {
                controller.ScrollingToNextPageEnabled = value;
            }
        }

        
        private void IgnoreEmbeddedPreferencesChanged()
        {
            RaisePropertyChanged("IgnoreEmbeddedPreferences");
        }

        /// <summary>
        /// Set the license key for the viewer API
        /// </summary>
        /// <param name="key">license key</param>
        /// <returns></returns>
        public static bool SetLicenseKey(string key)
        {
            return PdfViewerController.SetLicenseKey(key);
        }

        [Obsolete("Use property LicenseIsValid.")]
        public static bool GetLicenseIsValid()
        {
            return PdfViewerController.LicenseIsValid;
        }

        /// <summary>
        /// Check whether the viewer has a valid license registered
        /// </summary>
        /// <returns>true if there is a valid license</returns>
        public static bool LicenseIsValid
        {
            get
            {
                return PdfViewerController.LicenseIsValid;
            }
        }

        /// <summary>
        /// The version of the product.
        ///
        /// The version of the product in the format &apos;A.B.C.D&apos;.
        ///
        /// </summary>
        public static string ProductVersion
        {
            get
            {
                return PdfViewerController.ProductVersion;
            }
        }

        /// <summary>
        /// Is the brush to use for the rectangular overlays of search results.
        /// Default is a lightblue SolidColorBrush with opacity of 0.5.
        /// </summary>
        public Brush SearchOverlayBrush
        {
            get { return pane.HighlightBrush; }
            set
            {
                pane.HighlightBrush = value;
            }
        }

        private void RaisePropertyChanged(string propName)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs(propName));
            }
        }



        #endregion PropertyForwarding

        private void OnOpenCompleted(PdfViewerException ex)
        {
            Logger.LogInfo("Open Completed");
            pane.OverrideMouseModeToWait = false;
            controller.IsOpen = true;
        }

        #region Callback delegates for controller, to change dependency properties
        private void OnPageNoChanged(int firstPage, int lastPage)
        {
            updatingPageNo = true;
            RaisePropertyChanged("PageNo");
            updatingPageNo = false;
        }

        private void OnPageCountChanged(int pageCount)
        {
            RaisePropertyChanged("PageCount");
        }

        private void OnFitModeChanged(FitMode fitMode)
        {
            RaisePropertyChanged("FitMode");
        }

        private void OnPageLayoutModeChanged(TPageLayoutMode pageLayoutMode)
        {
            RaisePropertyChanged("PageLayoutMode");
        }

        private void OnResolutionChanged(Resolution resolution)
        {
            RaisePropertyChanged("Resolution");
        }

        private void OnBorderChanged(double border)
        {
            RaisePropertyChanged("Border");
        }

        private void OnZoomChanged(double zoomFactor)
        {
            RaisePropertyChanged("Zoom");
        }

        private void OnRotationChanged(int rotation)
        {
            RaisePropertyChanged("Rotate");
        }

        private void OnMouseModeChanged(TMouseMode mode)
        {
            RaisePropertyChanged("MouseMode");
        }

        private void OnTextSelected(string text)
        {
            RaisePropertyChanged("SelectedText");
        }

        #endregion

        #region Events

        /// <summary>
        /// Update properties when they change
        /// </summary>
        public event PropertyChangedEventHandler PropertyChanged;

        /// <summary>
        /// Event triggers if a thumbnail image has been loaded. If loading a thumbnail was requested and the request has failed, 
        /// the causing exception will be passed as argument (otherwise null).
        /// int: pdfPage page number within the pdf structure
        /// WriteableBitmap: bitmap of the thumbnail
        /// PdfViewerException: exception, != null if an exception occured
        /// </summary>
        public event EventHandler<PdfViewerController.ThumbnailsChangedArgs> ThumbnailLoaded
        {
            add { controller.ThumbnailsChanged += value; }
            remove { controller.ThumbnailsChanged -= value; }
        }


        /// <summary>
        /// Event triggers if Opening a file has completed. If there was any issue opening the file, then the according exception is passed as an argument
        /// </summary>
        public event Action<PdfViewerException> OpenCompleted
        {
            add { controller.OpenCompleted += value; }
            remove { controller.OpenCompleted -= value; }
        }
        /// <summary>
        /// Event triggers if a file has been closed and is not locked by the viewer API anymore.
        /// </summary>
        public event Action<PdfViewerException> CloseCompleted
        {
            add { controller.CloseCompleted += value; }
            remove { controller.CloseCompleted -= value; }
        }

        /// <summary>
        /// Event triggers when Text has been selected using TMouseMode.eMouseModeSelect
        /// </summary>
        public event Action<String> TextSelected
        {
            add { pane.TextSelected += value; }
            remove { pane.TextSelected -= value; }
        }

        /// <summary>
        /// Event triggers when a search command has terminated and returns a PdfSearcher.SearchResult marking the next found search match.
        /// </summary>
        public event Action<PdfSearcher.SearchResult> SearchCompleted;

        /// <summary>
        /// Event triggers when Visible Page range has changed.
        /// </summary>
        public event Action<int, int> VisiblePageRangeChanged
        {
            add { controller.VisiblePageRangeChanged += value; }
            remove { controller.VisiblePageRangeChanged -= value;  }
        }

        /// <summary>
        /// Event triggers when the page ordering changed
        /// </summary>
        public event Action<IList<int>> PageOrderChanged
        {
            add { controller.PageOrderChanged += value; }
            remove { controller.PageOrderChanged -= value; }
        }

        #endregion Events
    }
}
