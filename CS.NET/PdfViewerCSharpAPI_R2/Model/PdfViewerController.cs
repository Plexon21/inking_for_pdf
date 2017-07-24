using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.ComponentModel.Composition.Hosting;
using System.Linq;
using System.Text;
using PdfTools.PdfViewerCSharpAPI.Model;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows;
using System.Windows.Threading;
using System.Drawing;
using System.IO;
using PdfTools.PdfViewerCSharpAPI.Utilities;
using PdfTools.PdfViewerCSharpAPI.DocumentManagement.Requests;
using Microsoft.Win32;
using System.Runtime.InteropServices;
using System.Windows.Ink;
using PdfTools.PdfViewerCSharpAPI.Annotations;
using PdfTools.PdfViewerCSharpAPI.Extensibility;

namespace PdfTools.PdfViewerCSharpAPI.Model
{
    public class PdfViewerController : IPdfViewerController, IPdfControllerCallbackManager
    {

        #region Variables
        private static string registryKey = "Software\\PDF Tools AG\\PDF Viewer .NET";

        private static bool bIsInitialized = false;
        private static object isInitializedLock = new object();

        public static readonly System.Windows.Media.PixelFormat pixelFormat = PixelFormats.Pbgra32;

        public static Viewport viewportHint; //Debugging stuff

        private IPdfCanvas canvas;

        private Viewport viewport;

        private PdfSearcher searcher;

        private PdfTargetRect scrollableRect;

        private readonly static double MAX_ZOOM = 100;
        private readonly static double MIN_ZOOM = 0.01;

        private bool suspended = false;
        private bool ignoringEmbeddedPreferences = false;

        private String filename = "";
        private String password = "";
        private byte[] fileMem = null;

        private IList<PdfAnnotation> annotations;

        private IList<string> extensionFolders;
        private CompositionContainer extensionContainer;

        [ImportMany]
        public IEnumerable<Lazy<IPdfTextConverter, IPdfTextConverterMetadata>> textConverters;

        [ImportMany]
        public IEnumerable<Lazy<IPdfAnnotationFormMapper, IPdfAnnotationFormMapperMetadata>> annotationFormMappers;

        public string TextConverter { get; set; } = "WindowsInkTextConverter";

        public string AnnotationFormMapper { get; set; } = "NoChangeFormMapper";

        #endregion

        #region delegates


        private Action<Action> FireInvokeCallback;

        public Action<Action> InvokeCallback
        {
            get
            {
                return FireInvokeCallback;
            }
        }

        //For internal use:
        //we have to change the zoomfactor of all existing pdfRects, as it is used for conversion internally
        public delegate void ZoomFactorChanged(double zoomFactor);
        private void ZoomFactorChangedMethod(double zoomFactor)
        {
            OnZoomChanged(viewport.ZoomFactor);
        }

        #endregion

        #region protected Properties
        //properties, that are publicly readable, but only privately writable
        private int _firstPageOnViewport = 1;
        public int FirstPageOnViewport
        {
            get
            {
                return _firstPageOnViewport;
            }
            protected set
            {
                _firstPageOnViewport = value;
                canvas.PageNo = value;
            }
        }
        private int _lastPageOnViewport = 1;
        public int LastPageOnViewport
        {
            get
            {
                return _lastPageOnViewport;
            }
            protected set
            {
                _lastPageOnViewport = value;
            }
        }

        #endregion

        #region Constructors

        public PdfViewerController(Action<Action> invokeCallbackDelegate)
        {

            InitializeExtensions();
            Logger.LogInfo("Creating Object instance");
            this.FireInvokeCallback = invokeCallbackDelegate;
            this.viewport = new Viewport(ZoomFactorChangedMethod);
            this.canvas = new PdfCanvas(this);
            this.searcher = new PdfSearcher(canvas);
            searcher.SearchCompleted += OnSearchCompleted;
            scrollableRect = new PdfTargetRect();
            canvas.CanvasRectChanged += OnCanvasChangedEventHandler;
            /*
            RegistryKey key = Registry.CurrentUser.OpenSubKey(registryKey);
            if (key != null)
            {
                Logger.LogInfo("Loading values from registry");
                string s = (string)key.GetValue("Default PageLayoutMode");
                PageLayoutMode = (TPageLayoutMode)Enum.Parse(typeof(TPageLayoutMode), s);
                key.Close();
            }*/
            PageLayoutMode = (PageLayoutMode == TPageLayoutMode.None) ? TPageLayoutMode.OneColumn : PageLayoutMode;
            Logger.LogInfo("Created Object instance");
        }

        private void InitializeExtensions()
        {
            //TODO: Check for version and only take newest one.
            extensionFolders = new List<string>();
            extensionFolders.Add("TextConverters");
            extensionFolders.Add("AnnotationFormMappers");


            //An aggregate catalog that combines multiple catalogs
            var catalog = new AggregateCatalog();

            foreach (var s in extensionFolders)
            {
                string path = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, s);
                //Check the directory exists
                if (!Directory.Exists(path))
                {
                    Directory.CreateDirectory(path);
                }
                catalog.Catalogs.Add(new DirectoryCatalog(path, "*.dll"));
            }
            extensionContainer = new CompositionContainer(catalog);
            try
            {
                this.extensionContainer.ComposeParts(this);
            }
            catch (CompositionException compositionException)
            {
                Console.WriteLine(compositionException.ToString());
            }
        }

        public void Initialize()
        {
            Logger.LogInfo("Initialize");


            this.Resolution = new Resolution(96.0, 96.0);
            this.Border = 20;
            _fitMode = FitMode.FitTrueSize;
        }

        public void Dispose()
        {
            Dispose(true);
        }

        protected virtual void Dispose(bool disposing)
        {
            Logger.LogInfo("Disposing");
            if (disposing)
            {
                RegistryKey key = Registry.CurrentUser.CreateSubKey(registryKey);
                key.SetValue("Default PageLayoutMode", PageLayoutMode.ToString());
                key.Close();

                canvas.Dispose();
                PdfViewerUnInitialize();
            }
        }

        #endregion

        #region Helper Classes
        public class Viewport
        {
            private PdfTargetRect _rectangle;
            public PdfTargetRect Rectangle
            {
                get
                {
                    return _rectangle;
                }
                set
                {
                    _rectangle = value;
                }
            }
            private double _zoomFactor;
            public double ZoomFactor
            {
                get
                {
                    return _zoomFactor;
                }
                private set
                {
                    _zoomFactor = value;
                }
            }

            private PdfViewerController.ZoomFactorChanged zoomFactorChanged;

            public Viewport(PdfViewerController.ZoomFactorChanged zoomFactorChanged) : this(new PdfTargetRect(), 1.0, zoomFactorChanged) { }
            public Viewport(PdfTargetRect rectangle, double zoomFactor, PdfViewerController.ZoomFactorChanged zoomFactorChanged)
            {
                this._rectangle = rectangle;
                this.ZoomFactor = zoomFactor;
                this.zoomFactorChanged = zoomFactorChanged;
                viewportHint = this;
            }

            public void SetLocation(int x, int y)
            {
                _rectangle.iLocation = new PdfTargetPoint(x, y);
            }
            public void SetDimension(int width, int height)
            {
                _rectangle.SetSize(width, height);
            }
            public void Offset(int x, int y)
            {
                _rectangle.Offset(x, y);
            }

            public Viewport Clone()
            {
                return new Viewport(Rectangle.Clone(), ZoomFactor, null);
            }

            public void ZoomToContainRectangle(PdfTargetRect rect)
            {
                _rectangle.iCenter = rect.iCenter;
                double zoomFactorWidth = (double)rect.iWidth / (double)_rectangle.iWidth;
                double zoomFactorHeight = (double)rect.iHeight / (double)_rectangle.iHeight;

                ZoomCenteredOnViewportCenter(ZoomFactor / Math.Max(zoomFactorHeight, zoomFactorWidth));
            }

            public void ZoomCenteredOnViewportCenter(double newZoomFactor)
            {
                ZoomCenteredOnLocation(newZoomFactor, new PdfTargetPoint(Rectangle.iWidth / 2, Rectangle.iHeight / 2));
            }

            public void ZoomCenteredOnLocation(double newZoomFactor, PdfTargetPoint location)
            {
                newZoomFactor = Math.Min(Math.Max(newZoomFactor, MIN_ZOOM), MAX_ZOOM);
                PdfTargetPoint locationOnPixelCanvas = location + _rectangle.iLocation;
                locationOnPixelCanvas.MultInt(newZoomFactor / ZoomFactor);
                _rectangle.iLocation = locationOnPixelCanvas - location;
                ZoomFactor = newZoomFactor;
                zoomFactorChanged(newZoomFactor);
            }

            public void ScrollRectIntoView(PdfTargetRect rect)
            {
                int rightOvershoot = Math.Max(0, rect.iRight - _rectangle.iRight + viewportBorder);
                int bottomOvershoot = Math.Max(0, rect.iBottom - _rectangle.iBottom + viewportBorder);
                _rectangle.Offset(rightOvershoot, bottomOvershoot);

                int leftOvershoot = Math.Max(0, -rect.iX + _rectangle.iX + viewportBorder);
                int topOvershoot = Math.Max(0, -rect.iY + _rectangle.iY + viewportBorder);
                _rectangle.Offset(-leftOvershoot, -topOvershoot);
            }
            private int viewportBorder = 20;
        }
        #endregion

        #region Public Management Methods
        //Methods that do not update the bitmap, but are used for public interaction

        public static bool SetLicenseKey(string key)
        {

            lock (isInitializedLock)
            {
                if (!bIsInitialized)
                {
                    PdfViewerInitialize();
                    bIsInitialized = true;
                }
            }

            if (!PdfViewerSetLicenseKeyW(key))
            {
                /*
                UIntPtr size = PdfViewerGetLastErrorMessageW(null, UIntPtr.Zero);
                string message = "";
                if (size != UIntPtr.Zero)
                {
                    StringBuilder buffer = new StringBuilder((int)size.ToUInt32());
                    PdfViewerGetLastErrorMessageW(buffer, size);
                    message = buffer.ToString();
                }
                if (PdfViewerGetLastError() == TViewerError.eLicenseError)
                    throw new PdfLicenseInvalidException(message);
                else
                    throw new PdfLicenseInvalidException();
                 */
                return false;
            }
            return true;
        }

        [Obsolete("Use property LicenseIsValid.")]
        public static bool GetLicenseIsValid()
        {
            lock (isInitializedLock)
            {
                if (!bIsInitialized)
                {
                    PdfViewerInitialize();
                    bIsInitialized = true;
                }
            }

            return PdfViewerGetLicenseIsValid();
        }

        /// <summary>
        /// Check if the license is valid.
        ///
        /// </summary>
        public static bool LicenseIsValid
        {
            get
            {
                lock (isInitializedLock)
                {
                    if (!bIsInitialized)
                    {
                        PdfViewerInitialize();
                        bIsInitialized = true;
                    }
                }

                return PdfViewerGetLicenseIsValid();
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
                lock (isInitializedLock)
                {
                    if (!bIsInitialized)
                    {
                        PdfViewerInitialize();
                        bIsInitialized = true;
                    }
                }

                return PdfViewerGetProductVersionW();
            }
        }

        public int PageCount
        {
            get { return canvas.PageCount; }
        }

        public IList<int> PageOrder
        {
            get { return canvas.PageOrder; }
            set
            {
                if (value == null)
                    throw new ArgumentNullException("PageOrder", "PageOrder cannot be set to null");
                canvas.PageOrder = value;
                UpdatePageNo();
                FitAndUpdate(true);
            }
        }

        public IList<int> InversePageOrder
        {
            get { return canvas.InversePageOrder; }
        }

        public PdfTargetRect ViewportRect
        {
            get
            {
                return viewport.Rectangle.Clone();
            }
        }

        public double UserUnit
        {
            get
            {
                throw new NotImplementedException();
            }
            protected set
            {
                throw new NotImplementedException();
            }
        }

        public int PageCacheSlidingWindowSize
        {
            get
            {
                return canvas.DocumentManager.PageCacheSlidingWindowSize;
            }
            set
            {
                canvas.DocumentManager.PageCacheSlidingWindowSize = value;
            }
        }

        public void Search(string toSearch, int startPage, int startIndex)
        {
            searcher.Search(toSearch, startPage, startIndex);
        }



        public void SuspendLayout()
        {
            suspended = true;
        }

        public bool IgnoreEmbeddedPreferences
        {
            set
            {
                if (ignoringEmbeddedPreferences != value)
                    ignoringEmbeddedPreferences = value;
            }
            get
            {
                return ignoringEmbeddedPreferences;
            }
        }

        public IList<PdfTextFragment> GetTextOnPages(int firstPage, int lastPage)
        {
            return canvas.GetTextWithinPageRange(Math.Max(firstPage, 1), Math.Min(lastPage, canvas.PageCount));
        }

        public IList<PdfTextFragment> GetTextWithinRegion(PdfTargetRect markedRect)
        {
            markedRect = markedRect.Clone();//to not change original
            markedRect.Offset(viewport.Rectangle.iX, viewport.Rectangle.iY);
            return canvas.GetTextWithinRegion(markedRect.GetSourceRect(viewport.ZoomFactor), FirstPageOnViewport, LastPageOnViewport);
        }

        public void SetTextSelectionStartPoint(PdfTargetPoint start)
        {
            textSelectionStartPoint = new PdfSourcePoint(PdfUtils.ConvertPixel2Canvas(start.iX + viewport.Rectangle.iX, viewport.ZoomFactor), PdfUtils.ConvertPixel2Canvas(start.iY + viewport.Rectangle.iY, viewport.ZoomFactor));
            textSelectionStartPage = GetPageContainingPoint(textSelectionStartPoint);
        }

        private int textSelectionStartPage = 0;
        private PdfSourcePoint textSelectionStartPoint = null;
        public IList<PdfTextFragment> GetTextWithinSelection(PdfTargetPoint start, PdfTargetPoint end, ref double startDX, ref double endDX)
        {
            PdfSourcePoint startPoint = textSelectionStartPoint;
            int startPage = textSelectionStartPage;

            if (start != null) //otherwise the textSelectionStartPoint is already set by SetTextSelectionStartPoint
            {
                startPoint = new PdfSourcePoint(PdfUtils.ConvertPixel2Canvas(start.iX + viewport.Rectangle.iX, viewport.ZoomFactor), PdfUtils.ConvertPixel2Canvas(start.iY + viewport.Rectangle.iY, viewport.ZoomFactor));
                startPage = GetPageContainingPoint(textSelectionStartPoint);
            }
            PdfSourcePoint endPoint = new PdfSourcePoint(PdfUtils.ConvertPixel2Canvas(end.iX + viewport.Rectangle.iX, viewport.ZoomFactor), PdfUtils.ConvertPixel2Canvas(end.iY + viewport.Rectangle.iY, viewport.ZoomFactor));
            int endPage = GetPageContainingPoint(endPoint);
            if (startPage == -1 || endPage == -1)
                return null;
            //transform the sourcepoints to be onPage coordinates
            PdfSourceRect startPageRect = canvas.GetPageRect(startPage);
            PdfSourceRect endPageRect = canvas.GetPageRect(endPage);
            startPoint = startPageRect.GetOnPageCoordinates(startPoint, canvas.Rotation);
            endPoint = endPageRect.GetOnPageCoordinates(endPoint, canvas.Rotation);
            //transform to OnUnrotatedPage coordinates
            IList<PdfTextFragment> list;
            bool swap = false;
            if (endPage < startPage)
            {
                list = canvas.GetTextWithinSelection(endPoint, startPoint, endPage, startPage, ref swap);
                swap = !swap;
            }
            else
            {
                list = canvas.GetTextWithinSelection(startPoint, endPoint, startPage, endPage, ref swap);
            }
            startDX = swap ? endPoint.dX : startPoint.dX;
            endDX = swap ? startPoint.dX : endPoint.dX;
            return list;
        }

        //canvas coordinates
        private int GetPageContainingPoint(PdfSourcePoint point)
        {
            int startPage = 1;
            int endPage = PageCount;
            if (!PdfUtils.PageLayoutScrollingEnabled(PageLayoutMode))
            {
                startPage = FirstPageOnViewport;
                endPage = LastPageOnViewport;
            }
            while (true)
            {
                int needle = (endPage - startPage) / 2 + startPage;
                PdfSourceRect pageRect = canvas.GetPageRect(needle);
                int count = endPage - startPage + 1;

                if (count <= 2)
                {
                    if (canvas.GetPageRect(startPage).contains(point))
                        return startPage;
                    else if (canvas.GetPageRect(endPage).contains(point))
                        return endPage;
                    else
                        return -1;
                }

                if (point.dY < pageRect.dY)
                    endPage = needle;
                else if (point.dY > pageRect.dBottom)
                    startPage = needle;
                else if (point.dX < pageRect.dX)
                    endPage = needle;
                else if (point.dX > pageRect.dRight)
                    startPage = needle;
                else
                    return needle;

                if (count == endPage - startPage + 1)
                    return -1;  //We could not reduce the set of pages anymore. This implies that point is not on any page
            }
        }

        #endregion

        #region Public Update Methods
        //Public methods that cause bitmap updates

        public bool Open(string filename, string password)
        {
            return Open(filename, null, password);
        }
        public bool OpenMem(byte[] fileMem, string password)
        {
            return Open(null, fileMem, password);
        }

        private bool _shouldBecomeOpen = false; //indicates that an Open call was issued after the last close call, so we will pretend that the file actually is open for certain commands, as it may become open before they get executed
        private bool _isOpen = false;
        //Guess whether there is a file open or not. Based on last completed open / started close call. Only used for internal calls, to ensure that no PdfNoFileOpenedExceptions occur caused by own code.
        //Note however, that it is legitimate to call actions when _isOpen is false, but a Open() call has been issued (not returned successfully yet),
        // as we may queue commands to be executed right after opening. Should the open fail, then these commands will trigger exceptions.
        public bool IsOpen
        {
            get
            {
                return _isOpen;
            }
            set
            {
                _isOpen = value;
            }
        }

        private bool Open(string filename, byte[] fileMem, string password)
        {
            Logger.LogInfo("Opening File");
            FirstPageOnViewport = 1;
            lock (isInitializedLock)
            {
                if (!bIsInitialized)
                {
                    PdfViewerInitialize();
                    bIsInitialized = true;
                }
            }

            if (!PdfViewerGetLicenseIsValid())
            {
                UIntPtr size = PdfViewerGetLastErrorMessageW(null, UIntPtr.Zero);
                string message = "";
                if (size != UIntPtr.Zero)
                {
                    StringBuilder buffer = new StringBuilder((int)size.ToUInt32());
                    PdfViewerGetLastErrorMessageW(buffer, size);
                    message = buffer.ToString();
                }
                if (PdfViewerGetLastError() == TViewerError.eLicenseError)
                    throw new PdfLicenseInvalidException(message);
                else
                    throw new PdfLicenseInvalidException();
            }
            this.filename = filename;
            this.password = password;
            this.fileMem = fileMem;
            _shouldBecomeOpen = true;
            canvas.Open(filename, fileMem, password);
            Logger.LogInfo("Document is being opened - returning");
            return true;
        }

        public void Close()
        {
            Logger.LogInfo("Closing Document");
            this.filename = String.Empty;
            _isOpen = false;
            canvas.Close();
            _shouldBecomeOpen = false;
        }

        public string FileName
        {
            get
            {
                return this.filename;
            }
        }

        public PdfSourceRect GetPageRectGuess(int page)
        {
            return canvas.GetPageRect(page);
        }

        public PdfDestination GetDestination()
        {
            PdfSourceRect pageRect = canvas.GetPageRect(FirstPageOnViewport);
            PdfSourceRect viewportRect = viewport.Rectangle.GetSourceRect(viewport.ZoomFactor);
            double x = viewportRect.dX - pageRect.dX;
            double y = pageRect.dBottom - viewportRect.dBottom;
            double right = viewportRect.dRight - pageRect.dX;
            double top = pageRect.dY - viewportRect.dBottom;
            return new PdfDestination(FirstPageOnViewport, TDestination.eDestinationFitR, x, top, right, y, 0.0);
        }

        public void SetDestination(PdfDestination destination)
        {
            GoToDestination(destination);
            FitAndUpdate(false);
        }

        public void SetPageNo(int pageNo)
        {
            GoToDestination(pageNo);

            FitAndUpdate(false);
        }

        public void NextPage()
        {
            switch (PdfUtils.HorizontalPagePosition(PageLayoutMode, FirstPageOnViewport))
            {
                case -1: SetPageNo(FirstPageOnViewport + 2); break;
                default: SetPageNo(FirstPageOnViewport + 1); break;
            }
        }

        public void PreviousPage()
        {
            switch (PdfUtils.HorizontalPagePosition(PageLayoutMode, FirstPageOnViewport))
            {
                case -1: SetPageNo(FirstPageOnViewport - 2); break;
                case 0: SetPageNo(FirstPageOnViewport - 1); break;
                case 1: SetPageNo(FirstPageOnViewport - 3); break;//we always want the left page to be the primary page
            }
        }

        public int Rotate
        {
            get
            {
                return canvas.Rotation;
            }
            set
            {
                if (canvas.Rotation == value)
                    return;
                Logger.LogInfo("Rotating all pages");
                canvas.Rotation = value;
                OnRotationChanged(canvas.Rotation);
                if (_shouldBecomeOpen)
                {
                    GoToDestination(FirstPageOnViewport);
                    FitAndUpdate(false);
                }
            }
        }

        public double Border
        {
            get
            {
                return canvas.BorderSize;
            }
            set
            {
                if (canvas.BorderSize == value)
                    return;
                canvas.BorderSize = value;
                if (_shouldBecomeOpen)
                {
                    GoToDestination(FirstPageOnViewport);
                    FitAndUpdate(false);
                }
                OnBorderChanged(value);
            }
        }

        private Resolution _resolution;
        public Resolution Resolution
        {
            get { return _resolution; }
            set
            {
                if (_resolution.xdpi == value.xdpi && _resolution.ydpi == value.ydpi)
                    return;
                _resolution = value;
                OnResolutionChanged(value);
            }
        }

        private bool _scrollingToNextPageEnabled = true;
        public bool ScrollingToNextPageEnabled
        {
            get
            {
                return _scrollingToNextPageEnabled;
            }
            set
            {
                _scrollingToNextPageEnabled = value;
            }
        }



        private TPageLayoutMode _pageLayoutMode;
        public TPageLayoutMode PageLayoutMode
        {
            get
            {
                return _pageLayoutMode;
            }
            set
            {
                if (_pageLayoutMode == value)
                    return;
                ChangTPageLayoutMode(value);

                if (_shouldBecomeOpen)
                {
                    try
                    {
                        GoToDestination(FirstPageOnViewport);
                        FitAndUpdate(false);
                    }
                    catch (PdfNoFileOpenedException)
                    {
                        Logger.LogInfo("Viewportupdates for PageLayoutMode failed, as no file was open");
                        return;
                    }
                }
            }
        }
        private void ChangTPageLayoutMode(TPageLayoutMode value)
        {
            _pageLayoutMode = value;
            canvas.PageLayoutMode = value;
            OnPageLayoutModeChanged(value);
        }


        private FitMode _fitMode;
        public FitMode FitMode
        {
            get
            {
                return _fitMode;
            }
            set
            {
                if (_fitMode == value)
                    return;
                Logger.LogInfo("Setting FitMode");
                ChangeFitMode(value);
                int p = FirstPageOnViewport;
                if (_shouldBecomeOpen)
                {
                    bool pageChanged = FitViewport(true);
                    if (value == FitMode.FitPage)
                    {
                        GoToDestination(p);
                        pageChanged |= FitViewport(true);
                    }
                    else if (pageChanged)
                    {
                        OnVisiblePageRangeChanged(FirstPageOnViewport, LastPageOnViewport);
                    }
                    UpdateBitmapContent();
                }
            }
        }

        private void ChangeFitMode(FitMode value)
        {
            _fitMode = value;
            OnFitModeChanged(value);

            if (value == FitMode.FitTrueSize)
            {
                viewport.ZoomCenteredOnViewportCenter(1.0);
            }
        }


        public void ZoomToRectangle(PdfTargetRect rect)
        {
            if (_fitMode != FitMode.FitNone)
            {
                _fitMode = FitMode.FitNone;
                OnFitModeChanged(_fitMode);
            }

            PdfTargetRect zoomToRect = PdfTargetRect.CreateFromRectOnViewport(rect, viewport.Rectangle);
            viewport.ZoomToContainRectangle(zoomToRect);
            FitAndUpdate(false);
        }

        public void ZoomCenteredOnPosition(PdfTargetPoint point, double delta)
        {
            double value = viewport.ZoomFactor * delta;
            viewport.ZoomCenteredOnLocation(value, point);
            if (_fitMode != FitMode.FitNone)
            {
                _fitMode = FitMode.FitNone;
                OnFitModeChanged(_fitMode);
            }
            FitAndUpdate(false);
        }

        public double ZoomFactor
        {
            get
            {
                return viewport.ZoomFactor;
            }
            set
            {
                viewport.ZoomCenteredOnViewportCenter(value);
                if (_fitMode != FitMode.FitNone)
                {
                    _fitMode = FitMode.FitNone;
                    OnFitModeChanged(_fitMode);
                }
                FitAndUpdate(false);
            }
        }

        public void UpdateViewportDimensions(int width, int height)
        {
            //Utilities.DebugLogger.Log("(" + viewport.Rectangle.iWidth + ", " + viewport.Rectangle.iHeight + ") => (" + width + ", " + height + ")");

            if ((viewport.Rectangle.iWidth == width && viewport.Rectangle.iHeight == height) || width <= 0 || height <= 0 || Double.IsNaN(height) || Double.IsNaN(width))
            {
                //Nothing changed -> dont do anything
                //Viewport is empty -> dont do anything
                return;
            }

            viewport.SetDimension((int)width, (int)height);
            if (IsOpen)
                FitAndUpdate(true);
            //DEBUG
            /*if (viewport.Rectangle.iWidth != width || viewport.Rectangle.iHeight != height)
            {
                Utilities.Logger.Log("(" + viewport.Rectangle.iWidth + ", " + viewport.Rectangle.iHeight + ") => (" + width + ", " + height + ")");
            }*/
        }

        public void Scroll(Vector delta)
        {
            Scroll((int)delta.X, (int)delta.Y);
        }

        public void Scroll(int horizontalDelta, int verticalDelta)
        {
            if (!_isOpen)
                return;
            if (horizontalDelta == 0 && verticalDelta == 0)
            {
                return;
            }
            if (!PdfUtils.PageLayoutScrollingEnabled(PageLayoutMode))
            {
                PdfTargetRect pixelCanvas = canvas.CanvasRect.GetTargetRect(viewport.ZoomFactor);
                if (pixelCanvas.iBottom <= viewport.Rectangle.iBottom && verticalDelta < 0)
                {
                    if (!_scrollingToNextPageEnabled)
                        return;
                    if (LastPageOnViewport >= canvas.PageCount)
                        return;
                    NextPage();
                }
                else if (pixelCanvas.iY >= viewport.Rectangle.iY && verticalDelta > 0)
                {
                    if (!_scrollingToNextPageEnabled)
                        return;
                    if (FirstPageOnViewport == 1)
                        return;
                    PdfTargetRect pRect = canvas.GetUnionRectangleWithNeighbour(FirstPageOnViewport - 1).GetTargetRect(viewport.ZoomFactor);
                    GoToDestination(FirstPageOnViewport - 1, new PdfTargetPoint(pRect.iX, pRect.iBottom - viewport.Rectangle.iHeight));
                }
                else
                {
                    viewport.Offset(-horizontalDelta, -verticalDelta);
                }
            }
            else
            {
                viewport.Offset(-horizontalDelta, -verticalDelta);
            }
            FitAndUpdate(false);
        }

        public void ScrollToHorizontalPercentage(double percentage)
        {
            viewport.SetLocation((int)(percentage * ((double)scrollableRect.iWidth - (double)viewport.Rectangle.iWidth) - (double)scrollableRect.iWidth / 2), viewport.Rectangle.iY);
            FitAndUpdate(false);
        }
        public void ScrollToVerticalPercentage(double percentage)
        {
            viewport.SetLocation(viewport.Rectangle.iX, (int)(percentage * ((double)scrollableRect.iHeight - (double)viewport.Rectangle.iHeight)) + scrollableRect.iY);
            FitAndUpdate(false);
        }

        public void ResumeLayout()
        {
            suspended = false;
            UpdateBitmapContent();
        }
        #endregion

        #region annotation methods
        public void AddAnnoation(PdfAnnotation annot)
        {

            if (annotations == null)
            {
                annotations = new List<PdfAnnotation>();
                LoadAllAnnotationsOnPage(annot.PageNr);
            }
            annotations?.Add(annot);
        }

        public void CreateAnnotation(PdfAnnotation oldAnnot)
        {
            //TODO: Check for non working newer version. Loop over versions from newest to oldest until one works. Log which one was used
            var annots = annotationFormMappers.FirstOrDefault(a => a.Metadata.Name.Equals(AnnotationFormMapper))?.Value
                ?.MapToForm(oldAnnot.Rect);
            if (annots == null) return;
            if (annots.Count > 1)
            {
                var newAnnotations = annots.Select(points => new PdfAnnotation(oldAnnot) {Rect = points}).ToList();
                canvas.DocumentManager.CreateAnnotation(new CreateAnnotationArgs(newAnnotations));
            }
            else
            {
                oldAnnot.Rect = annots.First();
                canvas.DocumentManager.CreateAnnotation(new CreateAnnotationArgs(oldAnnot));
            }


            /*oldAnnot.Rect = annotationFormMappers.FirstOrDefault(a => a.Metadata.Name.Equals(AnnotationFormMapper))?.Value
                             ?.MapToForm(oldAnnot.Rect).FirstOrDefault() ?? oldAnnot.Rect;
            canvas.DocumentManager.CreateAnnotation(new CreateAnnotationArgs(oldAnnot));*/
        }

        public void UpdateAnnotation(UpdateAnnotationArgs args)
        {
            canvas.DocumentManager.UpdateAnnotation(args);
        }

        public PdfAnnotation GetAnnotation(long annotHandle)
        {
            if (annotations == null)
            {
                LoadAllAnnotations();
            }
            return annotations?.FirstOrDefault(a => a.GetHandleAsLong().Equals(annotHandle));
        }

        private void LoadAllAnnotations()
        {
            // TODO: implement
        }

        public void LoadAllAnnotationsOnPage(int pageNr)
        {
            canvas.DocumentManager.LoadAnnotationsOnPage(pageNr);
        }

        public IList<PdfAnnotation> GetAllAnnotationsOnPage(int pageNr)
        {
            LoadAllAnnotationsOnPage(pageNr);
            return annotations;
        }

        public void DeleteAnnotation(PdfAnnotation annot)
        {
            DeleteAnnotation(annot.GetHandleAsLong());
        }

        public void DeleteAnnotation(long annotHandle)
        {
            canvas.DocumentManager.DeleteAnnotation(new DeleteAnnotationArgs(annotHandle));
        }

        public string ConvertAnnotations(IEnumerable<PdfAnnotation> annots)
        {
            var res = textConverters.FirstOrDefault(p => p.Metadata.Name.Equals(TextConverter))?.Value?.ToText(annots);
            return res ?? $"No TextConverter with the name {TextConverter} found.";
        }
        public string ConvertAnnotations(StrokeCollection annots)
        {
            var res = textConverters.FirstOrDefault(p => p.Metadata.Name.Equals(TextConverter))?.Value?.ToText(annots);
            return res ?? $"No TextConverter with the name {TextConverter} found.";
        }

        #endregion

        public void SaveAs(string fileName)
        {
            canvas.DocumentManager.SaveAs(fileName);
        }

        #region Update Helper Methods
        //private helper methods that are used for internal bitmap updating

        private void FitAndUpdate(bool zoomingInAllowed)
        {
            if (FitViewport(zoomingInAllowed))
            {
                OnVisiblePageRangeChanged(FirstPageOnViewport, LastPageOnViewport);
            }
            UpdateBitmapContent();
        }

        private void GoToDestination(int pageNo)
        {
            GoToDestination(pageNo, new PdfTargetPoint(0, 0));
        }

        private void GoToDestination(int x, int y)
        {
            viewport.Rectangle.iLocation = new PdfTargetPoint(x, y);
        }

        private void GoToDestination(PdfDestination destination)
        {
            PdfSourceRect pageRect = canvas.GetPageRectGuaranteedExactly(destination.Page);
            PdfSourceRect destinationRect = destination.RectOnCanvas(pageRect, viewport);

            switch (destination.Type)
            {
                case TDestination.eDestinationXYZ:
                    GoToDestination(InversePageOrder[destination.Page - 1], (destinationRect.dLocation - pageRect.dLocation).GetTargetPoint(viewport.ZoomFactor));
                    ZoomCenteredOnPosition(destinationRect.dLocation.GetTargetPoint(viewport.ZoomFactor), destination.Zoom(viewport.ZoomFactor) / viewport.ZoomFactor);
                    break;
                case TDestination.eDestinationFitBV:
                case TDestination.eDestinationFitV:
                //TODO We cant do that, we'll fit to page instead:
                case TDestination.eDestinationFitB:
                case TDestination.eDestinationFit:
                    ChangeFitMode(FitMode.FitPage);
                    GoToDestination(InversePageOrder[destination.Page - 1]);
                    break;
                case TDestination.eDestinationFitBH:
                case TDestination.eDestinationFitH:
                    ChangeFitMode(FitMode.FitWidth);
                    GoToDestination(InversePageOrder[destination.Page - 1], destinationRect.dLocation.GetTargetPoint(viewport.ZoomFactor));
                    break;
                case TDestination.eDestinationFitR:
                    ZoomToRectangle(destinationRect.GetTargetRect(viewport.ZoomFactor));
                    break;
                case TDestination.eDestinationInvalid:
                default:
                    ArgumentException a = new ArgumentException();
                    Logger.LogException(a);
                    throw a;
            }
        }


        private void GoToDestination(int pageNo, PdfTargetPoint offset)
        {
            Logger.LogInfo("Going to destination on page " + pageNo);
            //sanitize input
            pageNo = Math.Min(pageNo, PageCount);
            pageNo = Math.Max(pageNo, 1);

            PdfTargetRect pageRect = canvas.GetPageRectGuaranteedExactly(pageNo).GetTargetRect(viewport.ZoomFactor);
            PdfTargetRect unionRectangle = canvas.GetUnionRectangleWithNeighbour(pageNo).GetTargetRect(viewport.ZoomFactor);
            viewport.Rectangle.iCenterX = unionRectangle.iCenterX;
            viewport.Rectangle.iY = unionRectangle.iY + offset.iY + 1; //+1, so the previous page does not touch the viewport
            if (viewport.Rectangle.iX > unionRectangle.iX || viewport.Rectangle.iRight < unionRectangle.iRight)
            {
                // we cant see the entire unionRectangle, go to page instead and
                // position its upper left corner in the upper left corner of
                // the viewport
                viewport.Rectangle.iLocation = pageRect.iLocation + offset;
            }

            switch (PageLayoutMode)
            {
                case TPageLayoutMode.SinglePage:
                    {
                        FirstPageOnViewport = pageNo;
                        LastPageOnViewport = pageNo;
                        break;
                    }
                case TPageLayoutMode.TwoPageLeft:
                    {
                        FirstPageOnViewport = pageNo - ((pageNo + 1) % 2);
                        LastPageOnViewport = Math.Min(canvas.PageCount, FirstPageOnViewport + 1);
                        break;
                    }
                case TPageLayoutMode.TwoPageRight:
                    {
                        if (pageNo == 1)
                        {
                            FirstPageOnViewport = 1;
                            LastPageOnViewport = 1;
                        }
                        else
                        {
                            FirstPageOnViewport = pageNo - (pageNo % 2);
                            LastPageOnViewport = Math.Min(canvas.PageCount, FirstPageOnViewport + 1);
                        }
                        break;
                    }
                default:
                    {
                        return;
                    }
            }
            //This is only executed for Pagelayouts without scrolling columns
            viewport.Rectangle.iX = PdfUtils.ConvertCanvas2Pixel(canvas.GetUnionRectangleWithNeighbour(FirstPageOnViewport).dX, viewport.ZoomFactor);
            OnVisiblePageRangeChanged(FirstPageOnViewport, LastPageOnViewport);//TODO sometimes they didnt actually change!
        }

        private void ScrollIntoView(int pageNo, IList<PdfTargetRect> textFragmentRectsOnCanvas)
        {
            if (!PdfUtils.PageLayoutScrollingEnabled(PageLayoutMode))
            {
                if (pageNo < FirstPageOnViewport || pageNo > LastPageOnViewport)
                    GoToDestination(pageNo);
            }
            foreach (PdfTargetRect rect in textFragmentRectsOnCanvas)
            {
                viewport.ScrollRectIntoView(rect);
            }
            //because we want the first one to be visible for sure
            viewport.ScrollRectIntoView(textFragmentRectsOnCanvas[0]);
        }
        //TODO continue

        /// <summary>
        /// Fits the viewport to the actual document, minimizing unused screenspace
        /// </summary>
        /// <param name="zoomingInAllowed">Sets whether zooming further in is allowed or only zooming out</param>
        /// <returns>Whether the visible page range on viewport has changed due to fitting and constraining (This does not trigger if the page has previously been modified, as it is done in LayoutPageMode)</returns>
        private bool FitViewport(Boolean zoomingInAllowed)
        {
            int oldFirstPage = this.FirstPageOnViewport;
            int oldLastPage = this.LastPageOnViewport;
            switch (PdfUtils.PageLayoutScrollingEnabled(PageLayoutMode))
            {
                case false:
                    // Because pagenumbers cannot change in pagemode, we dont have to call updatePageNo and dont loop.
                    FitViewportZoomfactorToVisiblePages(true);
                    ConstrainViewportToCanvas();
                    return (this.FirstPageOnViewport != oldFirstPage || this.LastPageOnViewport != oldLastPage);
                case true:
                    UpdatePageNo();
                    int recDepth = 0;
                    while (true)
                    {
                        bool visiblePagesChanged = false;
                        if (FitViewportZoomfactorToVisiblePages(zoomingInAllowed))
                        {
                            visiblePagesChanged |= UpdatePageNo();
                        }
                        if (ConstrainViewportToCanvas())
                        {
                            visiblePagesChanged |= UpdatePageNo();
                        }

                        if (visiblePagesChanged)
                        {
                            recDepth++;
                            if (recDepth > 10)
                            {
                                Utilities.Logger.Log("reached " + recDepth + ", which is highly unlikely to ever happen in the real world");
                                //do not continue
                            }
                            else
                                //We have to check again, because fitting might now yield different results, due to more pages being on the viewport
                                continue;
                        }
                        return (this.FirstPageOnViewport != oldFirstPage || this.LastPageOnViewport != oldLastPage);
                    }
                default:
                    throw new NotImplementedException("you broke math");
            }
        }

        /// <summary>
        /// Updates the FirstPageOnViewport and LastPageOnViewport variables according to the viewport position
        /// </summary>
        /// <returns>Whether the visible pagenumbers have changed</returns>
        private bool UpdatePageNo()
        {
            if (!PdfUtils.PageLayoutScrollingEnabled(PageLayoutMode))
            {
                return false;
            }

            int pageCount = canvas.PageCount;
            double viewportTop = PdfUtils.ConvertPixel2Canvas(viewport.Rectangle.iY, viewport.ZoomFactor);
            double viewportBot = PdfUtils.ConvertPixel2Canvas(viewport.Rectangle.iBottom, viewport.ZoomFactor);

            int pageNo = Math.Max(FirstPageOnViewport, 1);
            PdfSourceRect page = canvas.GetUnionRectangleWithNeighbour(pageNo);

            //iterate towards front until we find a page entirely above the viewport
            while (page.dBottom - canvas.BorderSize > viewportTop && pageNo > 1)
            {
                pageNo--;
                page = canvas.GetUnionRectangleWithNeighbour(pageNo);
            }
            //pageNo is a page somewhere above the viewport (not necessarily the one just outside! could be far away!)

            //iterate towards end until we find a page at least partially within the viewport
            while (page.dBottom - canvas.BorderSize < viewportTop && pageNo < pageCount)
            {
                pageNo++;
                page = canvas.GetUnionRectangleWithNeighbour(pageNo);
            }
            //PageNo is exactly the first page on the viewport

            bool changed = (FirstPageOnViewport != pageNo);
            FirstPageOnViewport = pageNo;

            //iterate towards end until we find a page entirely below the viewport
            while (page.dY + canvas.BorderSize < viewportBot && pageNo <= pageCount)
            {
                pageNo++;
                if (pageNo > pageCount)
                    break;
                page = canvas.GetUnionRectangleWithNeighbour(pageNo);
            }
            //pageNo is now the first page below the viewport 
            //(which may be an invalid page, if the last page on the viewport is the last page on the document)

            changed |= (LastPageOnViewport != pageNo - 1);
            LastPageOnViewport = pageNo - 1;

            return changed;
        }

        /// <summary>
        /// Fits the viewport to the visible pages according to the active fitMode
        /// </summary>
        /// <param name="zoomingInAllowed">Sets whether zooming further in is allowed or only zooming out</param>
        /// <returns>Whether the viewport has been changed</returns>
        private bool FitViewportZoomfactorToVisiblePages(bool zoomingInAllowed)
        {
            //All zoomfactors in this method are relative to the old zoomfactor
            //e.g. a newZoomFactor of 2.0, with a viewport.ZoomFactor of 1.5 causes the new viewport.ZoomFactor to be 3.0
            switch (FitMode)
            {
                case FitMode.FitPage:
                    {
                        // Iterate through all visible pages and zoom out, such that each
                        // page would fit into the viewport, if it was offset correctly
                        double newZoomFactor = MAX_ZOOM;
                        for (int pageNo = FirstPageOnViewport; pageNo <= LastPageOnViewport; pageNo++)
                        {
                            PdfSourceRect pageRect = canvas.GetUnionRectangleWithNeighbour(pageNo);

                            // make page symmetric around x=0 if it isn't (this can happen in two 
                            // pages mode with unevenly wide pages)
                            // We do not move the page in X direction, but rather extend its
                            // preceived width, that we will later fit to
                            pageRect.ExtendToBeVerticallySymetrical();
                            double newZoomFactorWidth = PdfUtils.ConvertPixel2Canvas(viewport.Rectangle.iWidth, viewport.ZoomFactor) / pageRect.dWidth;
                            double newZoomFactorHeight = PdfUtils.ConvertPixel2Canvas(viewport.Rectangle.iHeight, viewport.ZoomFactor) / pageRect.dHeight;

                            double zoomFactor = Math.Min(newZoomFactorHeight, newZoomFactorWidth);
                            newZoomFactor = Math.Min(zoomFactor, newZoomFactor);
                        }

                        // Due to floating point inaccuracy this value can jump back and forth
                        // between < 1 and > 1 resulting in an endless loop for viewport adjustments
                        // fixing it to 1.0 if the value is within epsilon.
                        if (Math.Abs(newZoomFactor - 1.0) < 0.000001)
                            newZoomFactor = 1.0;

                        // Adjust zoomFactor if we have to zoom out to fit the page.
                        // if zooming in is allowed, we have to adjust the zoomfactor anyway
                        if (newZoomFactor < 1.0 || zoomingInAllowed)
                        {
                            viewport.ZoomCenteredOnViewportCenter(newZoomFactor * viewport.ZoomFactor);
                            return true;
                        }
                        return false;
                    }
                case FitMode.FitWidth:
                    {
                        // allVisiblePagesRect is a rectangle containing all pages on the
                        // viewport. We fit the width to that
                        PdfSourceRect allVisiblePagesRect = canvas.GetUnionRectangleWithNeighbour(FirstPageOnViewport);
                        for (int i = FirstPageOnViewport + 1; i <= LastPageOnViewport; i++)
                        {
                            PdfSourceRect r = canvas.GetUnionRectangleWithNeighbour(i);
                            allVisiblePagesRect = allVisiblePagesRect.unionDouble(r);
                        }
                        double newZoomFactor = PdfUtils.ConvertPixel2Canvas(viewport.Rectangle.iWidth, viewport.ZoomFactor) / allVisiblePagesRect.dWidth;

                        if (newZoomFactor < 1.0 || zoomingInAllowed)
                        {
                            // adjust viewport zoom to fit all pages
                            viewport.ZoomCenteredOnViewportCenter(newZoomFactor * viewport.ZoomFactor);
                            return true;
                        }
                        else
                        {
                            return false;
                        }

                    }
                default:
                    {
                        return false;
                    }
            }

        }

        private bool ConstrainViewportToCanvas()
        {
            bool changed = ConstrainViewportToRectangle(canvas.CanvasRect.GetTargetRect(viewport.ZoomFactor), true);
            /*
             * This would crop the viewport to the actual pages instead of just the canvas.
             * However, our design choice is to fit only to the viewport instead
            PdfRect visiblePagesRectangle = canvas.GetUnionRectangleWithNeighbour(FirstPageOnViewport);
            for (int pageNo = FirstPageOnViewport + 1; pageNo <= LastPageOnViewport; pageNo++)
            {
                visiblePagesRectangle = visiblePagesRectangle.unionDouble(canvas.GetUnionRectangleWithNeighbour(pageNo));
            }
            //Pages might not be as wide as the canvas, if only considering the visible pages => crop to that now
            changed |= ConstrainViewportToRectangle(visiblePagesRectangle, false);
             */
            return changed;
        }

        private bool ConstrainViewportToRectangle(PdfTargetRect rect, bool center)
        {
            // check if the viewport is not already constrained anyway
            if (rect.containsInt(viewport.Rectangle))
            {
                return false;
            }

            // check horizontal overruns
            if (viewport.Rectangle.iWidth > rect.iWidth)
            {
                if (center)
                {
                    viewport.Rectangle.iCenterX = rect.iCenterX;
                }
                else if (viewport.Rectangle.iX > rect.iX)
                {
                    viewport.Rectangle.iX = rect.iX;
                }
                else if (viewport.Rectangle.iRight < rect.iRight)
                {
                    viewport.Rectangle.iRight = rect.iRight;
                }
            }
            else if (viewport.Rectangle.iX < rect.iX)
            {
                viewport.Rectangle.iX = rect.iX;
            }
            else if (viewport.Rectangle.iRight > rect.iRight)
            {
                viewport.Rectangle.iRight = rect.iRight;
            }

            // check vertical overruns
            if (viewport.Rectangle.iHeight > rect.iHeight)
            {
                if (center)
                {
                    viewport.Rectangle.iCenterY = rect.iCenterY;
                }
                else if (viewport.Rectangle.iY > rect.iY)
                {
                    viewport.Rectangle.iY = rect.iY;
                }
                else if (viewport.Rectangle.iBottom < rect.iBottom)
                {
                    viewport.Rectangle.iBottom = rect.iBottom;
                }
            }
            else if (viewport.Rectangle.iY < rect.iY)
            {
                viewport.Rectangle.iY = rect.iY;
            }
            else if (viewport.Rectangle.iBottom > rect.iBottom)
            {
                viewport.Rectangle.iBottom = rect.iBottom;
            }
            //Console.WriteLine("Constrained Viewport (top:{0:N2}, mid:{1:N2}, bot:{2:N2}) to canvas (top:{3:N2}, mid:{4:N2}, bot:{5:N2}) ", viewport.Rectangle.iY, viewport.Rectangle.iCenterY, viewport.Rectangle.iBottom, rect.iY, rect.iCenterY, rect.iBottom);
            //            Console.WriteLine(Environment.StackTrace);
            return true;
        }

        private void UpdateBitmapContent()
        {
            if (viewport == null || viewport.Rectangle.IsEmpty || suspended)
            {
                if (viewport == null)
                    Logger.LogWarning("Viewport unitintialized - abort drawing"); //that cant even happen
                if (viewport.Rectangle.IsEmpty)
                    Logger.LogWarning("ViewportRectangle is empty: " + viewport.Rectangle.ToString() + " - abort drawing");
                if (suspended)
                    Logger.LogInfo("Drawing of viewer is suspended");
                return;
            }
            Logger.LogInfo("Updating bitmap content");

            Dictionary<int, PdfSourceRect> pageRects = new Dictionary<int, PdfSourceRect>();

            for (int page = this.FirstPageOnViewport; page <= this.LastPageOnViewport; page++)
            {
                pageRects.Add(page, canvas.GetPageRect(page).Clone());
            }
            canvas.DocumentManager.Draw(viewport.Rectangle.iWidth, viewport.Rectangle.iHeight, Resolution, canvas.Rotation, pageRects, viewport.Clone());

            UpdateScrollablePdfRect();
            OnScrollableAreaChanged(scrollableRect);
            OnViewportRectangleChanged(viewport.Rectangle);
        }

        private void OnCanvasChangedEventHandler(PdfSourceRect oldCanvas, PdfSourceRect newCanvas, int changedPage)
        {
            if (changedPage <= FirstPageOnViewport && !oldCanvas.IsEmpty) //if the old canvas is empty, then this was the very first canvas calculation and we do not have to offset
                viewport.Offset(0, PdfUtils.ConvertCanvas2Pixel(newCanvas.dHeight - oldCanvas.dHeight, viewport.ZoomFactor));
            UpdateScrollablePdfRect();
        }

        private void UpdateScrollablePdfRect()
        {
            PdfTargetRect canvasRect = canvas.CanvasRect.GetTargetRect(viewport.ZoomFactor);
            PdfTargetRect viewportRect = viewport.Rectangle;
            scrollableRect = canvasRect.Clone();

            if (viewportRect.iWidth > canvasRect.iWidth)
            {
                scrollableRect.iWidth = viewportRect.iWidth;
                scrollableRect.iX -= (viewportRect.iWidth - canvasRect.iWidth) / 2;
            }

            if (viewportRect.iHeight > canvasRect.iHeight)
            {
                scrollableRect.iHeight = viewportRect.iHeight;
                scrollableRect.iY -= (viewportRect.iHeight - canvasRect.iHeight) / 2;
            }

        }

        #endregion

        #region IPdfControllerCallbackManager Methods

        public void OnOpenCompleted(PdfViewerException ex)
        {
            Logger.LogInfo("Opening of document completed - starting visual updates");
            FireInvokeCallback(delegate ()
            {
                if (ex != null)
                {
                    Logger.LogException(ex);
                    OnOpenCompletedCall(ex);
                    return;
                }

                Logger.LogInfo("Reseting searcher, rotation and viewport");
                searcher.OnNewDocumentOpened();
                canvas.Rotation = 0;
                viewport.ZoomCenteredOnViewportCenter(1.0);
                OnRotationChanged(canvas.Rotation);
                if (_shouldBecomeOpen)//it might be that there was already another close call before the open completed
                {
                    try
                    {
                        if (!ignoringEmbeddedPreferences)
                        {
                            Logger.LogInfo("Load embedded preferences");
                            TPageLayoutMode pageLayout = canvas.DocumentManager.RequestPageLayout().Wait().output;
                            if (pageLayout != TPageLayoutMode.None)
                            {
                                //We directly call the change method as we do not want to execute the goto's associated with the PageLayout Property
                                ChangTPageLayoutMode(pageLayout);
                            }

                            PdfDestination destination = canvas.DocumentManager.RequestOpenActionDestination().Wait().output;

                            if (destination.Type != TDestination.eDestinationInvalid)
                            {
                                GoToDestination(destination);
                            }
                            else
                            {
                                GoToDestination(FirstPageOnViewport);
                            }
                        }
                        else
                        {
                            GoToDestination(FirstPageOnViewport);
                        }

                        FitViewport(true);
                        OnVisiblePageRangeChanged(FirstPageOnViewport, LastPageOnViewport);
                        UpdateBitmapContent();

                        //set canvassize and stuff
                        OnPageCountChanged(canvas.PageCount);
                        OnOpenCompletedCall(ex);
                    }
                    catch (PdfNoFileOpenedException)
                    {
                        Logger.LogInfo("Viewportupdates for Border failed, as no file was open");
                        return;
                    }
                }

            }
            );
        }

        /**
         * Callback called by Worker, when Closing completed
         */
        public void OnCloseCompleted(PdfViewerException ex)
        {
            FireInvokeCallback(delegate ()
            {
                if (ex != null)
                {
                    Exception e = new NotImplementedException("This PdfException is unhandled!", ex);
                    Logger.LogException(e);
                    throw e;
                }
                if (!viewport.Rectangle.IsEmpty)
                {
                    WriteableBitmap bitmap = new WriteableBitmap(viewport.Rectangle.iWidth, viewport.Rectangle.iHeight, Resolution.xdpi, Resolution.ydpi, pixelFormat, null);
                    OnBitmapChanged(bitmap);
                }

                OnCloseCompletedCall(ex);
                OnScrollableAreaChanged(new PdfTargetRect());
                OnViewportRectangleChanged(viewport.Rectangle);
            }
            );
        }

        /**
         * Callback called by Worker, when Drawing completed
         */
        public void OnDrawCompleted(PdfViewerException ex, WriteableBitmap bitmap)
        {
            FireInvokeCallback(delegate ()
            {

                if (ex == null)
                {
                    //continue
                }
                else if (ex is PdfNoFileOpenedException)
                {
                    Logger.LogException(ex);
                    return;
                }
                else
                {
                    Exception e = new NotImplementedException("This PdfException is unhandled!", ex);
                    Logger.LogException(e);
                    throw e;
                }
                OnBitmapChanged(bitmap);
            }
            );
        }

        public void OnPageOrderChangedCompleted(IList<int> pageOrder)
        {
            FireInvokeCallback(delegate ()
            {
                OnPageOrderChanged(pageOrder);
            });
        }

        private void OnSearchCompleted(PdfSearcher.SearchResult result)
        {
            if (result != null)
            {
                List<PdfTargetRect> list = new List<PdfTargetRect>();
                foreach (KeyValuePair<int, IList<PdfSourceRect>> rectsOnPage in result.TextRects)
                {
                    foreach (PdfSourceRect rect in rectsOnPage.Value)
                    {
                        list.Add(rect.CalculateRectOnCanvas(canvas.GetPageRect(rectsOnPage.Key), canvas.Rotation).GetTargetRect(viewport.ZoomFactor));
                    }
                }
                ScrollIntoView(result.TextRects.Keys.Min(), list);
            }
            if (FitViewport(false))
                OnVisiblePageRangeChanged(FirstPageOnViewport, LastPageOnViewport);
            UpdateBitmapContent();
        }

        public void OnAnnotationCreated(PdfViewerException ex, IList<PdfAnnotation> annots)
        {
            FireInvokeCallback(delegate ()
            {
                FitAndUpdate(false);
                UpdateBitmapContent();
            });

        }
        public void OnAnnotationsLoaded(PdfViewerException pdfViewerException, IList<PdfAnnotation> tupleOutput)
        {
            FireInvokeCallback(delegate ()
            {
                annotations = tupleOutput;
            });
        }
        public void OnAnnotationUpdate(PdfViewerException pdfViewerException, IList<int> i)
        {
            FireInvokeCallback(delegate ()
            {
                FitAndUpdate(false);
                UpdateBitmapContent();
            });
        }
        public void OnAnnotationDeleted(PdfViewerException pdfViewerException, object o)
        {
            FireInvokeCallback(delegate ()
            {
                LoadAllAnnotationsOnPage(FirstPageOnViewport); // TODO: find out why this is important
                FitAndUpdate(false);
                UpdateBitmapContent();
            });
        }

        #endregion

        #region Searcher configuration

        public bool SearchMatchCase
        {
            set
            {
                if (searcher.MatchCase != value)
                    searcher.MatchCase = value;
            }
            get
            {
                return searcher.MatchCase;
            }
        }
        public bool SearchWrap
        {
            set
            {
                if (searcher.Wrap != value)
                    searcher.Wrap = value;
            }
            get
            {
                return searcher.Wrap;
            }
        }
        public bool SearchPrevious
        {
            set
            {
                if (searcher.Previous != value)
                    searcher.Previous = value;
            }
            get
            {
                return searcher.Previous;
            }
        }
        public bool SearchUseRegex
        {
            set
            {
                if (searcher.UseRegex != value)
                    searcher.UseRegex = value;
            }
            get
            {
                return searcher.UseRegex;
            }
        }
        #endregion

        #region Transform methods
        public Rect TransformRectPageToViewportWinRect(PdfSourceRect rectOnUnrotatedPage, int pageNo)
        {
            PdfTargetRect rectOnCanvas = rectOnUnrotatedPage.CalculateRectOnCanvas(canvas.GetPageRect(pageNo), canvas.Rotation).GetTargetRect(viewport.ZoomFactor);
            return rectOnCanvas.GetWinRect(viewport.Rectangle);
        }
        public Rectangle TransformRectPageToViewportDrawRect(PdfSourceRect rectOnUnrotatedPage, int pageNo)
        {
            PdfTargetRect rectOnCanvas = rectOnUnrotatedPage.CalculateRectOnCanvas(canvas.GetPageRect(pageNo), canvas.Rotation).GetTargetRect(viewport.ZoomFactor);
            return rectOnCanvas.GetDrawingRect(viewport.Rectangle);
        }

        public PdfSourceRect TransformOnScreenToOnCanvas(PdfTargetRect rect)
        {
            rect = rect + viewport.Rectangle.iLocation;
            return rect.GetSourceRect(viewport.ZoomFactor);
        }

        public PdfSourcePoint TransformOnScreenToOnPage(PdfTargetPoint point, ref int page)
        {
            point = point + viewport.Rectangle.iLocation;
            PdfSourcePoint s = point.GetSourcePoint(viewport.ZoomFactor);
            page = GetPageContainingPoint(s);
            return canvas.GetPageRect(page).GetOnPageCoordinates(s, canvas.Rotation);
        }

        public PdfSourceRect TransformRectOnScreenToOnPage(Rect rectOnPage, out int page) // TODO : delete if not used
        {
            // TODO: handle points outside of page correctly UPDATE : does not seem to be possible

            int pagePoint1 = 0;
            int pagePoint2 = 0;

            PdfSourcePoint p1 = TransformOnScreenToOnPage(new PdfTargetPoint(rectOnPage.TopLeft), ref pagePoint1);
            PdfSourcePoint p2 = TransformOnScreenToOnPage(new PdfTargetPoint(rectOnPage.BottomRight), ref pagePoint2);

            if (pagePoint1 != 0 && pagePoint2 != 0 && pagePoint1 == pagePoint2)
            {
                page = pagePoint1;
                return new PdfSourceRect(Math.Min(p1.dX, p2.dX), Math.Min(p1.dY, p2.dY), Math.Abs(p1.dX - p2.dX), Math.Abs(p1.dY - p2.dY));
            }

            page = 0;
            return null;
        }

        public PdfSourceRect TransformRectOnCanvasToOnPage(PdfSourceRect rectOnCanvas, out int pageNr)
        {
            PdfSourcePoint canvasMiddle = rectOnCanvas.dCenter;
            pageNr = GetPageContainingPoint(canvasMiddle);

            if (pageNr > 0)
            {
                PdfSourcePoint canvasTopLeft = new PdfSourcePoint(rectOnCanvas.dX, rectOnCanvas.dY);
                PdfSourcePoint canvasBottomRight = new PdfSourcePoint(rectOnCanvas.dRight, rectOnCanvas.dBottom);

                PdfSourceRect pageRect = GetPageRectGuess(pageNr);

                PdfSourcePoint pagePoint1 = pageRect.GetOnPageCoordinates(canvasTopLeft, Rotate);
                PdfSourcePoint pagePoint2 = pageRect.GetOnPageCoordinates(canvasBottomRight, Rotate);

                return new PdfSourceRect(Math.Min(pagePoint1.dX, pagePoint2.dX), Math.Min(pagePoint1.dY, pagePoint2.dY), Math.Abs(pagePoint1.dX - pagePoint2.dX), Math.Abs(pagePoint1.dY - pagePoint2.dY));
            }
            else
            {
                return null;
            }


        }

        #endregion

        #region ThumbnailRelated methods

        private int thumbnailWidth = 50, thumbnailHeight = 50;

        public int ThumbnailWidth
        {
            set { thumbnailWidth = value; }
            get { return thumbnailWidth; }
        }
        public int ThumbnailHeight
        {
            set { thumbnailHeight = value; }
            get { return thumbnailHeight; }
        }

        public APdfRequest<ThumbnailCacheArgs, WriteableBitmap> LoadThumbnail(int pageNo, bool gueranteeExactness)
        {
            PdfSourceRect sourceRect = null;
            bool loadPage = false;
            if (gueranteeExactness && canvas.DocumentManager.IsPageExactlyLoaded(pageNo))
                sourceRect = canvas.GetPageRectGuaranteedExactly(pageNo);
            else
                loadPage = true;

            ThumbnailCacheArgs args = new ThumbnailCacheArgs(loadPage, canvas.Rotation, sourceRect, thumbnailWidth, thumbnailHeight, pageNo, Resolution);
            return canvas.DocumentManager.RequestThumbnail(args);
        }

        public WriteableBitmap GetThumbnail(int pageNo)
        {
            ThumbnailCacheArgs args = new ThumbnailCacheArgs(false, canvas.Rotation, null, thumbnailWidth, thumbnailHeight, pageNo, Resolution);
            return canvas.DocumentManager.GetThumbnail(args);
        }

        public void OnThumbnailLoaded(int pdfPage, WriteableBitmap bitmap, PdfViewerException exception)
        {
            FireInvokeCallback(delegate ()
            {
                ThumbnailsChangedArgs args = new ThumbnailsChangedArgs(pdfPage, bitmap, exception);
                // If the corresponding page is still displayed somewhere in the viewer, execute the event callbacks
                if (PageOrder.Contains(pdfPage))
                    OnThumbnailsChanged(args);
            }
            );
        }


        public void CancelRequest(IPdfRequest request)
        {
            canvas.DocumentManager.CancelRequest(request);
        }


        #endregion ThumbnailRelated methods

        #region OutlinesRelated methods


        public void OpenOutlineItem(int outlineId)
        {
            canvas.DocumentManager.RequestOutlines(outlineId);
        }

        public void OnOutlinesLoaded(PdfViewerException ex, int parentId, IList<PdfOutlineItem> outlineItems)
        {
            FireInvokeCallback(delegate ()
            {
                OnOutlinesChanged(parentId, outlineItems, ex);
            }
            );
        }

        #endregion OutlinesRelated methods

        #region event nullMethods
        private void OnBitmapChanged(WriteableBitmap arg) { if (BitmapChanged != null) BitmapChanged(arg); }
        private void OnScrollableAreaChanged(PdfTargetRect arg) { if (ScrollableAreaChanged != null) ScrollableAreaChanged(arg); }
        private void OnViewportRectangleChanged(PdfTargetRect arg) { if (ViewportRectangleChanged != null) ViewportRectangleChanged(arg); }

        private void OnOpenCompletedCall(PdfViewerException arg) { _isOpen = (arg == null); _shouldBecomeOpen = (arg == null); if (OpenCompleted != null) OpenCompleted(arg); }
        private void OnCloseCompletedCall(PdfViewerException arg) { if (CloseCompleted != null) CloseCompleted(arg); }
        private void OnVisiblePageRangeChanged(int arg1, int arg2) { if (VisiblePageRangeChanged != null) VisiblePageRangeChanged(arg1, arg2); }
        private void OnPageCountChanged(int arg) { if (PageCountChanged != null) PageCountChanged(arg); }
        private void OnFitModeChanged(FitMode arg) { if (FitModeChanged != null) FitModeChanged(arg); }
        private void OnPageLayoutModeChanged(TPageLayoutMode arg) { if (PageLayoutModeChanged != null) PageLayoutModeChanged(arg); }
        private void OnResolutionChanged(Resolution arg) { if (ResolutionChanged != null) ResolutionChanged(arg); }
        private void OnBorderChanged(double arg) { if (BorderChanged != null) BorderChanged(arg); }
        private void OnZoomChanged(double arg) { if (ZoomChanged != null) ZoomChanged(arg); }
        private void OnRotationChanged(int arg) { if (RotationChanged != null) RotationChanged(arg); }
        private void OnOutlinesChanged(int arg1, IList<PdfOutlineItem> arg2, PdfViewerException arg3) { if (OutlinesChanged != null) OutlinesChanged(arg1, arg2, arg3); }
        private void OnPageOrderChanged(IList<int> arg1) { if (PageOrderChanged != null) PageOrderChanged(arg1); }
        //private void OnThumbnailsChanged(int arg1, int arg2, WriteableBitmap arg3, PdfViewerException arg4) { if (OutlinesChanged != null) ThumbnailsChanged(arg1, arg2, arg3, arg4); }
        private void OnThumbnailsChanged(ThumbnailsChangedArgs e)
        {
            EventHandler<ThumbnailsChangedArgs> handler = ThumbnailsChanged;
            if (handler != null)
            {
                handler(this, e);
            }
        }
        #endregion

        #region event redeclaration
        public event Action<WriteableBitmap> BitmapChanged;
        public event Action<PdfTargetRect> ScrollableAreaChanged;
        public event Action<PdfTargetRect> ViewportRectangleChanged;
        public event Action<PdfSearcher.SearchResult> SearchCompleted
        {
            add { searcher.SearchCompleted += value; }
            remove { searcher.SearchCompleted -= value; }
        }

        public class ThumbnailsChangedArgs : EventArgs
        {
            public int pdfPage;
            public WriteableBitmap bitmap;
            public PdfViewerException ex;

            /// <summary>
            /// Thumbnails changed event arguments
            /// </summary>
            /// <param name="pdfPage">Pdf page number of the thumbnail</param>
            /// <param name="bitmap">Bitmap of the thumbnail</param>
            /// <param name="ex">Exception, is null when there were no issues with the request.</param>
            public ThumbnailsChangedArgs(int pdfPage, WriteableBitmap bitmap, PdfViewerException ex)
            {
                this.pdfPage = pdfPage;
                this.bitmap = bitmap;
                this.ex = ex;
            }
        }

        public event EventHandler<ThumbnailsChangedArgs> ThumbnailsChanged;
        public event Action<PdfViewerException> OpenCompleted;
        public event Action<PdfViewerException> CloseCompleted;
        public event Action<int, int> VisiblePageRangeChanged;
        public event Action<int> PageCountChanged;
        public event Action<FitMode> FitModeChanged;
        public event Action<TPageLayoutMode> PageLayoutModeChanged;
        public event Action<Resolution> ResolutionChanged;
        public event Action<double> BorderChanged;
        public event Action<double> ZoomChanged;
        public event Action<int> RotationChanged;
        public event Action<int, IList<PdfOutlineItem>, PdfViewerException> OutlinesChanged;
        //public event Action<int, int, WriteableBitmap, PdfViewerException> ThumbnailsChanged;
        public event Action<IList<int>> PageOrderChanged;
        #endregion event redeclaration

        #region native imports
        private enum TViewerError
        {
            eLicenseError = 0, ePasswordError = 1, eFileNotFoundError = 2, eUnknownError = 3, eIllegalArgumentError = 4, eOutOfMemoryError = 5, eFileCorruptError = 6, eUnsupportedFeatureError = 7
        };
        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern bool PdfViewerSetLicenseKeyW(string licensekey);
        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern bool PdfViewerGetLicenseIsValid();
        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern string PdfViewerGetProductVersionW();
        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern void PdfViewerInitialize();
        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern void PdfViewerUnInitialize();
        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern TViewerError PdfViewerGetLastError();
        [DllImport("PdfViewerAPI.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, CallingConvention = CallingConvention.StdCall)]
        static extern UIntPtr PdfViewerGetLastErrorMessageW(StringBuilder errorMessageBuffer, UIntPtr errorMessageBufferSize);




        #endregion native imports
    }
}
