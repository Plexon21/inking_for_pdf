using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.IO;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Threading;
using System.Text.RegularExpressions;
using System.Windows.Ink;
using System.Windows.Input.StylusPlugIns;
using Microsoft.Ink;
using PdfTools.PdfViewerCSharpAPI.Annotations;
using PdfTools.PdfViewerCSharpAPI.Model;
using PdfTools.PdfViewerCSharpAPI.Utilities;
using PdfTools.PdfViewerCSharpAPI.DocumentManagement;
using PdfTools.PdfViewerCSharpAPI.DocumentManagement.Requests;
using Cursors = System.Windows.Input.Cursors;
using Stroke = System.Windows.Ink.Stroke;
using System.Windows.Shapes;

namespace PdfTools.PdfViewerWPF.CustomControls
{
    /// <summary>
    /// Interaction logic for PdfViewerPane.xaml
    /// </summary>
    internal partial class PdfViewerPane : ContentControl // Multitouch.Framework.WPF.Controls.RandomCanvas
    {
        private WriteableBitmap bitmap;

        private IPdfViewerController controller;

        private DispatcherTimer inertiaScrollDispatchTimer;


        public PdfViewerPane()
        {
            IsEnabled = true;

            IsManipulationEnabled = true;
            SnapsToDevicePixels = true;
            UseLayoutRounding = true;

            AllowDrop = true;
            var dr = new DynamicRenderer();
            this.Background = Brushes.Transparent;
            this.Cursor = Cursors.Arrow;

            dr.Enabled = false;
            this.StylusPlugIns.Add(dr);
            StylusPlugIns.First().Enabled = false;

            inertiaScrollDispatchTimer = new DispatcherTimer(new TimeSpan(100000), DispatcherPriority.Normal, new EventHandler(inertiaScrollDispatchTimer_Tick), Application.Current.Dispatcher);
            inertiaScrollDispatchTimer.Stop();

        }

        public override void OnApplyTemplate()
        {
            base.OnApplyTemplate();
            this.MouseLeftButtonDown += LeftMouseDownEventHandler;
            this.MouseRightButtonDown += RightMouseDownEventHandler;
            this.MouseDown += MiddleMouseDownEventHandler;

            this.MouseLeftButtonUp += LeftMouseUpEventHandler;
            this.PreviewMouseMove += MouseMoveEventHandler;
            this.MouseWheel += MouseWheelEventHandler;
            this.LayoutUpdated += LayoutUpdatedEventHandler;

            this.IsManipulationEnabled = true;
            this.ManipulationDelta += TouchManipulationDelta;
            this.ManipulationStarting += TouchManipulationStarting;
            this.ManipulationInertiaStarting += TouchManipulationInertiaStarting;

            this.selectedRectangleOnCanvas += HandleSelectedRectangleOnCanvas;
        }

        public void SetController(IPdfViewerController controller)
        {
            this.controller = controller;
            controller.BitmapChanged += SetBitmap;
            controller.SearchCompleted += SearchCompletedEventHandler;
            controller.CloseCompleted += OnCloseCompletedEventHandler;
            controller.PageOrderChanged += OnPageOrderChangedEventHandler;

            controller.AnnotationsChanged += OnAnnotationsChangedEventHandler;
        }

        private void OnCloseCompletedEventHandler(PdfViewerException e)
        {
            clearRects();
        }

        private void OnPageOrderChangedEventHandler(IList<int> pageOrder)
        {
            clearRects();
        }

        private void clearRects()
        {
            _selectedText = string.Empty;
            selectedRects.Clear();
        }

        private void SearchCompletedEventHandler(PdfSearcher.SearchResult result)
        {
            if (result == null)
            {
                return;
            }
            lock (selectedRectsLock)
            {
                selectedRects = result.TextRects;
                _selectedText = result.Text;
                TextSelected(_selectedText);
            }
            InvalidateVisual();
        }

        private Brush _highlightBrush = new SolidColorBrush(Colors.Yellow) { Opacity = 0.5 };
        public Brush HighlightBrush
        {
            get
            {
                return _highlightBrush;
            }
            set
            {
                _highlightBrush = value;
            }
        }
        private Brush _markBrush = new SolidColorBrush(Colors.DodgerBlue) { Opacity = 0.5 };
        public Brush MarkBrush
        {
            set
            {
                _markBrush = value;
            }
        }

        protected override void OnRender(DrawingContext dc)
        {
            //Utilities.DebugLogger.Log("Start OnRender");

            //draw background
            Rect panelRect = new Rect(0.0, 0.0, this.ActualWidth, this.ActualHeight);
            dc.PushClip(new RectangleGeometry(panelRect));
            dc.DrawRectangle(this.Background, null, panelRect);

            //set pen for annotations
            Brush freehandBrush = new SolidColorBrush(AnnotationColor);
            freehandBrush.Opacity = 1.0;
            double width = ZoomRelativeAnnotationStrokeWidth ? AnnotationStrokeWidth : AnnotationStrokeWidth * controller.ZoomFactor;
            Pen annotPen = new Pen(freehandBrush, width);
            annotPen.EndLineCap = PenLineCap.Round;
            annotPen.StartLineCap = PenLineCap.Round;

            //set pen for selectedAnnotations
            Pen selectedAnnotationsPen = new Pen(new SolidColorBrush(Colors.Blue), 0.5);


            //Draw bitmap
            if (bitmap != null)
            {
                //PdfUtils.SaveBitmap("bitmap.png", bitmap);
                double dx = (double)bitmap.PixelWidth - this.ActualWidth;
                double dy = (double)bitmap.PixelHeight - this.ActualHeight;
                Rect bitmapRect = new Rect(0.0, 0.0, this.ActualWidth, this.ActualHeight);

                bitmapRect.Inflate(dx / 2, dy / 2);
                dc.DrawImage(bitmap, bitmapRect);
            }
            if (controller.IsOpen)
            {
                if (markingRectangle)
                {
                    Brush outlineBrush = _markBrush.Clone();
                    outlineBrush.Opacity = 1.0;
                    dc.DrawRectangle(_markBrush, new Pen(outlineBrush, 1.0), selectedRect);
                }
                if (drawingFreeHandAnnotation || MouseMode == TMouseMode.eMouseTextRecognitionMode)
                {

                    if (annotationPoints != null)
                    {
                        var drawingAnnotations = controller.DrawForm(annotationPoints);
                        for (int i = 0; i < drawingAnnotations.Count - 1; i++)
                        {
                            dc.DrawLine(annotPen, drawingAnnotations[i], drawingAnnotations[i + 1]);
                        }
                    }

                    if (strokes != null)
                    {
                        foreach (Stroke s in strokes)
                        {
                            for (int i = 0; i < s.StylusPoints.Count - 1; i++)
                            {
                                dc.DrawLine(annotPen, s.StylusPoints[i].ToPoint(), s.StylusPoints[i + 1].ToPoint());
                            }
                        }
                    }
                }
                if (selectedAnnotations.Count > 0)
                {
                    foreach (PdfAnnotation annot in selectedAnnotations)
                    {
                        PdfSourceRect rectOnPage = new PdfSourceRect(annot.Rect[0], annot.Rect[1], annot.Rect[2] - annot.Rect[0], annot.Rect[3] - annot.Rect[1]);

                        Rect rectWin = controller.TransformRectPageToViewportWinRect(rectOnPage, annot.PageNr);

                        dc.DrawRectangle(null, selectedAnnotationsPen, rectWin);
                    }
                }
                if (selectedRects.Count > 0)
                {
                    lock (selectedRectsLock)
                    {
                        for (int pageNo = controller.FirstPageOnViewport; pageNo <= controller.LastPageOnViewport; pageNo++)
                        {
                            if (!selectedRects.ContainsKey(pageNo))
                                continue;
                            foreach (PdfSourceRect pRect in selectedRects[pageNo])
                            {
                                System.Windows.Rect rect = controller.TransformRectPageToViewportWinRect(pRect, pageNo);
                                if (!rect.IsEmpty)
                                    dc.DrawRectangle(_markBrush, null, rect);
                            }
                        }
                    }
                }
            }
            dc.Pop();//Pop clip
            base.OnRender(dc);
            //Utilities.DebugLogger.Log("End OnRender");
        }

        private void SetBitmap(WriteableBitmap bitmap)
        {
            this.bitmap = bitmap;
            //trigger wpf to rerender this element:
            InvalidateVisual();
            //Utilities.DebugLogger.Log("Invalidated Visual");
        }

        private bool _overrideMouseModeToWait = false;
        public bool OverrideMouseModeToWait
        {
            set
            {
                _overrideMouseModeToWait = value;
                SetCursorAccordingToMouseMode();
            }
            get
            {
                return _overrideMouseModeToWait;
            }
        }

        private bool _zoomRelativeAnnotationStrokeWidth = false;
        public bool ZoomRelativeAnnotationStrokeWidth
        {
            set
            {
                _zoomRelativeAnnotationStrokeWidth = value;
            }
            get
            {
                return _zoomRelativeAnnotationStrokeWidth;
            }
        }

        private double _annotationStrokeWidth = 1;
        public double AnnotationStrokeWidth
        {
            set
            {
                if (value >= 0)
                {
                    _annotationStrokeWidth = value;

                    if (selectedAnnotations.Count > 0)
                    {
                        foreach (PdfAnnotation annot in selectedAnnotations)
                        {
                            controller.UpdateAnnotation(annot.UpdateWidth(_annotationStrokeWidth));
                        }
                    }
                }
            }
            get
            {
                return _annotationStrokeWidth;
            }
        }

        private Color _annotationColor = Colors.Black;
        public Color AnnotationColor
        {
            set
            {
                _annotationColor = value;

                if (selectedAnnotations.Count > 0)
                {
                    foreach (PdfAnnotation annot in selectedAnnotations)
                    {
                        controller.UpdateAnnotation(annot.UpdateColor(_annotationColor));
                    }
                }
            }
            get
            {
                return _annotationColor;
            }
        }

        private TMouseMode _mouseMode = TMouseMode.eMouseUndefMode;
        public TMouseMode MouseMode
        {
            set
            {
                if (_mouseMode == value)
                    return;
                lock (selectedRectsLock)
                {
                    selectedRects.Clear();
                    _selectedText = string.Empty;
                }

                _mouseMode = value;
                SetCursorAccordingToMouseMode();
                InvalidateVisual();
                UpdateAnnotations(value);
                OnMouseModeChanged(value);
            }
            get
            {
                return _mouseMode;
            }
        }
        private void SetCursorAccordingToMouseMode()
        {
            if (OverrideMouseModeToWait)
                this.Cursor = System.Windows.Input.Cursors.Wait;
            else
            {
                switch (_mouseMode)
                {
                    case TMouseMode.eMouseSelectMode: this.Cursor = System.Windows.Input.Cursors.IBeam; break;
                    case TMouseMode.eMouseMoveMode: this.Cursor = System.Windows.Input.Cursors.Hand; break;
                    case TMouseMode.eMouseMarkMode: this.Cursor = System.Windows.Input.Cursors.Cross; break;
                    case TMouseMode.eMouseZoomMode: this.Cursor = System.Windows.Input.Cursors.Cross; break;
                    case TMouseMode.eMouseFreehandAnnotationMode: this.Cursor = System.Windows.Input.Cursors.Pen; break;
                    case TMouseMode.eMouseDeleteAnnotationMode: this.Cursor = System.Windows.Input.Cursors.Cross; break;
                    case TMouseMode.eMouseTextRecognitionMode: this.Cursor = System.Windows.Input.Cursors.Pen; break;
                    default: this.Cursor = System.Windows.Input.Cursors.Arrow; break;
                }
            }
        }

        private void UpdateAnnotations(TMouseMode value)
        {
            if (value == TMouseMode.eMouseEndTextRecognitionMode)
            {
                textRecognitionActive = false;

                //MessageBox.Show(controller.ConvertAnnotations(strokes, "WindowsInk"));//TODO: remove?
                //RecognizeText();

                //TODO: remove and use in update
                /*
                IList<PdfAnnotation> annotations = controller.GetAllAnnotationsOnPage(controller.FirstPageOnViewport);

                controller.UpdateAnnotation(annotations[0].Move(10,10));
                controller.UpdateAnnotation(annotations[0].Scale(2));
                controller.UpdateAnnotation(annotations[0].UpdateContent("newContent"));
                controller.UpdateAnnotation(annotations[0].UpdateLabel("newLabel"));
                */
                Random rng = new Random();

                for (int i = 0; i < 10000; i++)
                {
                    double[] color = new double[] { AnnotationColor.R / 255.0, AnnotationColor.G / 255.0, AnnotationColor.B / 255.0 };
                    double[] points = new double[] { rng.NextDouble() * 200 + 100, rng.NextDouble() * 200 + 100, rng.NextDouble() * 200 + 100, rng.NextDouble() * 200 + 100 };

                    controller.CreateAnnotation(new PdfAnnotation(PdfDocument.TPdfAnnotationType.eAnnotationInk, 1, points, color, 1));
                }

                MouseMode = TMouseMode.eMouseUndefMode;
            } else
            {
                strokes = new StrokeCollection();
            }

            selectedAnnotations.Clear();
        }

        private void CreateAnnotation()
        {

            int pointCount = annotationPoints.Count;

            double[] points = new double[pointCount * 2];

            try
            {
                int firstPage = 0;

                PdfSourcePoint firstPoint = controller.TransformOnScreenToOnPage(new PdfTargetPoint(annotationPoints[0]), ref firstPage);

                points[0] = firstPoint.dX;
                points[1] = firstPoint.dY;

                if (pointCount >= 2)
                {

                    for (int i = 1; i < pointCount; i++)
                    {
                        int page = 0;
                        PdfSourcePoint point = controller.TransformOnScreenToOnPage(new PdfTargetPoint(annotationPoints[i]), ref page);

                        if (page != firstPage)
                        {
                            throw new ArgumentOutOfRangeException("page", "not all points are on the same page");
                        }

                        points[i * 2] = point.dX;
                        points[i * 2 + 1] = point.dY;
                    }

                }

                double[] color = new double[] { AnnotationColor.R / 255.0, AnnotationColor.G / 255.0, AnnotationColor.B / 255.0 };
                double width = ZoomRelativeAnnotationStrokeWidth ? AnnotationStrokeWidth / controller.ZoomFactor : AnnotationStrokeWidth;

                controller.CreateAnnotation(new PdfAnnotation(PdfDocument.TPdfAnnotationType.eAnnotationInk, firstPage, points, color, width));

            }
            catch (ArgumentOutOfRangeException e)
            {
                Logger.LogError("Not all points of the drawn annotation are on the same Page. Aborting creation.");
                Logger.LogException(e);
            }

            annotationPoints = null;
        }

        private void HandleSelectedRectangleOnCanvas(PdfSourceRect rectOnCanvas)
        {
            selectedRectOnPage = controller.TransformRectOnCanvasToOnPage(rectOnCanvas, out int pageNr);

            if (selectedRectOnPage != null && pageNr > 0)
            {
                controller.LoadAllAnnotationsOnPage(pageNr);
            }
        }


        private void OnAnnotationsChangedEventHandler(IList<PdfAnnotation> annots)
        {
            if (selectedRectOnPage != null)
            {
                IList<PdfAnnotation> markedAnnotations = annots?.Where(annot => annot.IsContainedInRect(selectedRectOnPage)).ToList<PdfAnnotation>();

                if (markedAnnotations != null && markedAnnotations.Count > 0)
                {
                    switch (MouseMode)
                    {
                        case TMouseMode.eMouseMarkMode:

                            selectedAnnotations = markedAnnotations;

                            break;
                        case TMouseMode.eMouseDeleteAnnotationMode:

                            foreach (PdfAnnotation annot in markedAnnotations)
                            {
                                controller.DeleteAnnotation(annot);
                            }

                            break;
                        default:
                            selectedRectOnPage = null;
                            break;
                    }
                }

            }

            InvalidateVisual();
        }

        private void OnMouseModeChanged(TMouseMode arg) { if (MouseModeChanged != null) MouseModeChanged(arg); }

        public event Action<TMouseMode> MouseModeChanged;
        public event Action<PdfSourceRect> selectedRectangleOnCanvas;
        public event Action<String> TextSelected;

        #region TouchEventHandlers
        private double rotationFromManipulation = 0.0;

        private void TouchManipulationStarting(object sender, ManipulationStartingEventArgs e)
        {
            e.ManipulationContainer = this;
            e.Handled = true;
            rotationFromManipulation = 0.0;
        }

        private double diag = Math.Sqrt(2.0);

        private void TouchManipulationDelta(object sender, ManipulationDeltaEventArgs e)
        {
            if (!controller.IsOpen)
                return;

            rotationFromManipulation += e.DeltaManipulation.Rotation;
            if (rotationFromManipulation > 90.0)
            {
                try
                {
                    controller.Rotate = 90;
                }
                catch (PdfNoFileOpenedException)
                {
                }
                rotationFromManipulation = 0.0;
            }
            else if (rotationFromManipulation < -90.0)
            {
                try
                {
                    controller.Rotate = -90;
                }
                catch (PdfNoFileOpenedException)
                {
                }
                rotationFromManipulation = 0.0;
            }

            Vector translation = e.DeltaManipulation.Translation;
            if (translation.Length != 0.0)
                controller.Scroll(translation);

            Vector scale = e.DeltaManipulation.Scale;
            if (scale.Length != diag)
                controller.ZoomCenteredOnPosition(new PdfTargetPoint(e.ManipulationOrigin), scale.Length / diag);
        }

        void TouchManipulationInertiaStarting(object sender, ManipulationInertiaStartingEventArgs e)
        {

            // Decrease the velocity of the Rectangle's movement by 
            // 10 inches per second every second.
            // (10 inches * 96 pixels per inch / 1000ms^2)
            e.TranslationBehavior.DesiredDeceleration = 10.0 * 96.0 / (1000.0 * 1000.0);

            // Decrease the velocity of the Rectangle's resizing by 
            // 0.1 inches per second every second.
            // (0.1 inches * 96 pixels per inch / (1000ms^2)
            e.ExpansionBehavior.DesiredDeceleration = 0.1 * 96.0 / (1000.0 * 1000.0);

            // Decrease the velocity of the Rectangle's rotation rate by 
            // 2 rotations per second every second.
            // (2 * 360 degrees / (1000ms^2)
            e.RotationBehavior.DesiredDeceleration = 720.0 / (1000.0 * 1000.0);

            e.Handled = true;
        }

        #endregion

        #region MouseEventHandlers

        private System.Windows.Point lastMousePosition;
        private bool mouseScrolling = false;
        private bool middleMouseScrolling = false;
        private bool markingRectangle = false;
        private bool selectingText = false;
        private bool drawingFreeHandAnnotation = false;
        private Rect selectedRect = new Rect();
        private bool textRecognitionActive = false;

        //annotation
        private List<System.Windows.Point> annotationPoints;
        private StrokeCollection strokes = new StrokeCollection();
        private IList<PdfAnnotation> selectedAnnotations = new List<PdfAnnotation>();
        private PdfSourceRect selectedRectOnPage;



        private void MouseWheelEventHandler(Object sender, MouseWheelEventArgs e)
        {
            if (!controller.IsOpen)
                return;

            e.Handled = true;
            bool ctrlDown = ((Keyboard.Modifiers & ModifierKeys.Control) > 0);
            if (ctrlDown)
            {
                controller.ZoomCenteredOnPosition(new PdfTargetPoint(e.GetPosition(this)), Math.Pow(1.1, (double)e.Delta / 60.0));
            }
            else
            {
                controller.Scroll(0, e.Delta);
            }
        }

        public void RecognizeText()
        {
            MessageBox.Show(controller.ConvertAnnotations(strokes)); 
        }

        private void RightMouseDownEventHandler(Object sender, MouseEventArgs e)
        {
            Keyboard.Focus(this);
        }

        private void LeftMouseDownEventHandler(Object sender, MouseEventArgs e)
        {
            Keyboard.Focus(this);
            selectedRects.Clear();
            selectedAnnotations.Clear();
            _selectedText = String.Empty;
            if (!controller.IsOpen)
                return;
            if (OverrideMouseModeToWait)
                return;
            if (middleMouseScrolling)
                return; //handled by middlemousedown

            if (MouseMode == TMouseMode.eMouseZoomMode || MouseMode == TMouseMode.eMouseMarkMode || MouseMode == TMouseMode.eMouseDeleteAnnotationMode)
            {
                //start marking rectangle
                lastMousePosition = e.GetPosition(this);
                markingRectangle = true;
            }
            else if (MouseMode == TMouseMode.eMouseSelectMode)
            {
                lastMousePosition = e.GetPosition(this);
                controller.SetTextSelectionStartPoint(new PdfTargetPoint(lastMousePosition));
                this.Cursor = Cursors.IBeam;
                selectingText = true;
            }
            else if (MouseMode == TMouseMode.eMouseFreehandAnnotationMode)
            {
                annotationPoints = new List<Point>();
                annotationPoints.Add(e.GetPosition(this));
                drawingFreeHandAnnotation = true;

                StylusPlugIns.First().Enabled = true;
            } else if (MouseMode == TMouseMode.eMouseTextRecognitionMode)
            {
                textRecognitionActive = true;
                annotationPoints = new List<Point>();
                annotationPoints.Add(e.GetPosition(this));
                StylusPlugIns.First().Enabled = true;
            }
            else
            {
                //moving
                lastMousePosition = e.GetPosition(this);
                this.Cursor = Cursors.SizeAll;
                mouseScrolling = true;
            }
            e.Handled = true;
        }

        private void MouseMoveEventHandler(Object sender, MouseEventArgs e)
        {
            if (!controller.IsOpen)
                return;
            if (OverrideMouseModeToWait)
                return;
            if (mouseScrolling)
            {
                Vector delta = System.Windows.Point.Subtract(e.GetPosition(this), lastMousePosition);
                controller.Scroll(delta);
                lastMousePosition = e.GetPosition(this);
            }
            else if (middleMouseScrolling)
            {
                Vector delta = System.Windows.Point.Subtract(e.GetPosition(this), lastMousePosition);
                double fac = delta.X / -delta.Y;

                if (delta.Length < 10)
                {
                    Cursor = Cursors.ScrollAll;
                }
                else if (Math.Abs(-delta.Y) <= 1.0)
                {
                    if (delta.X > 0)
                    {
                        Cursor = Cursors.ScrollE;
                    }
                    else
                    {
                        Cursor = Cursors.ScrollW;
                    }
                }
                else if (-delta.Y > 0)
                {
                    if (fac > 4)
                    {
                        Cursor = Cursors.ScrollE;
                    }
                    else if (fac > 0.25)
                    {
                        Cursor = Cursors.ScrollNE;
                    }
                    else if (fac > -0.25)
                    {
                        Cursor = Cursors.ScrollN;
                    }
                    else if (fac > -4)
                    {
                        Cursor = Cursors.ScrollNW;
                    }
                    else
                    {
                        Cursor = Cursors.ScrollW;
                    }
                }
                else
                {
                    if (fac > 4)
                    {
                        Cursor = Cursors.ScrollW;
                    }
                    else if (fac > 0.25)
                    {
                        Cursor = Cursors.ScrollSW;
                    }
                    else if (fac > -0.25)
                    {
                        Cursor = Cursors.ScrollS;
                    }
                    else if (fac > -4)
                    {
                        Cursor = Cursors.ScrollSE;
                    }
                    else
                    {
                        Cursor = Cursors.ScrollE;
                    }
                }

            }
            else if (markingRectangle)
            {
                selectedRect = new Rect(lastMousePosition, e.GetPosition(this));
                InvalidateVisual();
            }
            else if (selectingText)
            {

                PdfTargetPoint p2 = new PdfTargetPoint(e.GetPosition(this));
                double s1 = 0.0, s2 = 0.0;
                //IList<PdfTextFragment> frags = controller.GetTextWithinSelection(new PdfTargetPoint(lastMousePosition), p2, ref s1, ref s2);
                IList<PdfTextFragment> frags = null;
                try
                {
                    frags = controller.GetTextWithinSelection(null, p2, ref s1, ref s2);
                }
                catch (PdfNoFileOpenedException)
                {
                    selectingText = false;
                    return;
                }
                if (frags == null)
                    return;
                selectedRects.Clear();
                StringBuilder textBuilder = new StringBuilder();

                // If there are no fragments return;
                if (!frags.Any())
                    return;

                //special treatment for first and last element
                PdfTextFragment first = frags[0];
                PdfTextFragment last = frags[frags.Count - 1];
                int firstIndex = first.GetIndexOfClosestGlyph(s1);
                int lastIndex = last.GetIndexOfClosestGlyph(s2);
                int firstPageNo = controller.InversePageOrder[first.PageNo - 1];
                int lastPageNo = controller.InversePageOrder[last.PageNo - 1];

                if (frags.Count == 1)
                {
                    //It might be that start and end point are in wrong order, as they are only ordered by textfragment and there is only one in this collection
                    if (lastIndex < firstIndex)
                    {
                        int tmp = lastIndex;
                        lastIndex = firstIndex;
                        firstIndex = tmp;
                    }
                    selectedRects.Add(firstPageNo, new List<PdfSourceRect>() { first.GetRectOnUnrotatedPage(firstIndex, lastIndex) });
                    textBuilder.Append(first.Text.Substring(firstIndex, lastIndex - firstIndex)).Append(" ");
                }
                else
                {
                    selectedRects.Add(firstPageNo, new List<PdfSourceRect>() { first.GetRectOnUnrotatedPage(firstIndex, int.MaxValue) });
                    textBuilder.Append(first.Text.Substring(firstIndex, first.Text.Length - firstIndex)).Append(" ");
                    if (firstPageNo != lastPageNo)
                        selectedRects.Add(lastPageNo, new List<PdfSourceRect>());
                    selectedRects[lastPageNo].Add(last.GetRectOnUnrotatedPage(0, lastIndex));

                    //remove first and last
                    frags.RemoveAt(0);
                    frags.RemoveAt(frags.Count - 1);
                    foreach (PdfTextFragment frag in frags)
                    {
                        int fragPageNo = controller.InversePageOrder[frag.PageNo - 1];
                        if (!selectedRects.ContainsKey(fragPageNo))
                            selectedRects.Add(fragPageNo, new List<PdfSourceRect>());
                        selectedRects[fragPageNo].Add(frag.RectOnUnrotatedPage);
                        textBuilder.Append(frag.Text).Append(" ");
                    }
                    textBuilder.Append(last.Text.Substring(0, lastIndex));
                }
                _selectedText = textBuilder.ToString();
                this.InvalidateVisual();
            }
            else if (drawingFreeHandAnnotation || textRecognitionActive)
            {
                annotationPoints.Add(e.GetPosition(this));
                InvalidateVisual();
            }
            else if (MouseMode == TMouseMode.eMouseFreehandAnnotationMode || MouseMode == TMouseMode.eMouseTextRecognitionMode) InvalidateVisual();

            e.Handled = true;
        }


        private Dictionary<int, IList<PdfSourceRect>> selectedRects = new Dictionary<int, IList<PdfSourceRect>>();//these are temporarily marked
        private Object selectedRectsLock = new Object();
        private Object highlightedRectsLock = new Object();
        private void LeftMouseUpEventHandler(Object sender, MouseEventArgs e)
        {
            if (!controller.IsOpen)
                return;
            if (mouseScrolling)
            {
                Vector delta = System.Windows.Point.Subtract(e.GetPosition(this), lastMousePosition);
                controller.Scroll(delta);
                this.Cursor = Cursors.Hand;
                mouseScrolling = false;
            }
            else if (markingRectangle)
            {
                selectedRect = new Rect(lastMousePosition, e.GetPosition(this));
                markingRectangle = false;
                if (MouseMode == TMouseMode.eMouseZoomMode)
                {
                    controller.ZoomToRectangle(new PdfTargetRect(selectedRect));
                }
                else if ((MouseMode == TMouseMode.eMouseMarkMode || MouseMode == TMouseMode.eMouseDeleteAnnotationMode) && selectedRectangleOnCanvas != null)
                {
                    selectedRectangleOnCanvas(controller.TransformOnScreenToOnCanvas(new PdfTargetRect(selectedRect)));
                }

                InvalidateVisual();
            }
            else if (selectingText)
            {
                selectingText = false;
                if (TextSelected != null)
                    TextSelected(_selectedText);
                this.InvalidateVisual();
            }
            else if (drawingFreeHandAnnotation)
            {
                drawingFreeHandAnnotation = false;

                CreateAnnotation();

                /*
                int pointCount = annotationPoints.Count;

                double[] points = new double[pointCount * 2];
                //double[] color = PdfUtils.ConvertRGBToCMYK(AnnotationColor);
                double[] color = new double[] { AnnotationColor.R / 255.0, AnnotationColor.G / 255.0, AnnotationColor.B / 255.0 };

                double width = ZoomRelativeAnnotationStrokeWidth ? AnnotationStrokeWidth / controller.ZoomFactor : AnnotationStrokeWidth;

                try // TODO: handle points outside of the page correctly
                {
                    int firstPage = 0;

                    PdfSourcePoint firstPoint = controller.TransformOnScreenToOnPage(new PdfTargetPoint(annotationPoints[0]), ref firstPage);

                    points[0] = firstPoint.dX;
                    points[1] = firstPoint.dY;

                    for (int i = 1; i < pointCount; i++)
                    {

                        int page = 0;
                        PdfSourcePoint point = controller.TransformOnScreenToOnPage(new PdfTargetPoint(annotationPoints[i]), ref page);

                        if (page != firstPage)
                        {
                            throw new ArgumentOutOfRangeException("page", "not all points are on the same page");
                        }

                        points[i * 2] = point.dX;
                        points[i * 2 + 1] = point.dY;
                    }

                    controller.CreateAnnotation(new PdfAnnotation(PdfDocument.TPdfAnnotationType.eAnnotationInk, firstPage,
                        points, color, width));

                }
                catch (CompositionContractMismatchException ex)
                {
                    Logger.LogException(ex);
                    MessageBox.Show(ex.Message, "Fehler beim verwenden einer Extension");
                }
                catch (ArgumentOutOfRangeException ex)
                {
                    Logger.LogError("User tried to create annotation outside of page");
                }
                catch (Exception ex)
                {
                    Logger.LogException(ex);
                    MessageBox.Show(ex.Message);
                }

                annotationPoints = null;
                InvalidateVisual();

                //IList<PdfAnnotation> annotations = controller.GetAllAnnotationsOnPage(controller.FirstPageOnViewport); //TODO: find out why this is important

                //string path = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);
                //controller.SaveAs(path + "\\Test.pdf");
                */
            }
            else if (textRecognitionActive)
            {
                var s = new Stroke(new StylusPointCollection(annotationPoints));
                strokes.Add(s);

                StylusPlugIns.First().Enabled = false;

                annotationPoints = null;
                textRecognitionActive = false;
            }

            e.Handled = true;
        }

        private string _selectedText = "";
        public string SelectedText
        {
            get
            {
                return _selectedText;
            }
        }


        private void MiddleMouseDownEventHandler(Object sender, MouseButtonEventArgs e)
        {
            Keyboard.Focus(this);
            //This gets executed on all buttondowns not just middle!
            if (middleMouseScrolling)
            {
                middleMouseScrolling = false;
                this.Cursor = Cursors.Arrow;
                inertiaScrollDispatchTimer.Stop();
            }
            else if (e.ChangedButton == MouseButton.Middle)
            {
                lastMousePosition = e.GetPosition(this);
                this.Cursor = Cursors.ScrollAll;
                middleMouseScrolling = true;
                inertiaScrollDispatchTimer.Start();
            }
            e.Handled = true;
        }

        protected void inertiaScrollDispatchTimer_Tick(object sender, EventArgs e)
        {
            if (!controller.IsOpen)
            {
                middleMouseScrolling = false;
                this.Cursor = Cursors.Arrow;
                inertiaScrollDispatchTimer.Stop();
            }
            else
            {
                Vector delta = System.Windows.Point.Subtract(lastMousePosition, Mouse.GetPosition(this));
                controller.Scroll(delta * 0.1);
                inertiaScrollDispatchTimer.Start();
            }
        }

        private void LayoutUpdatedEventHandler(Object sender, EventArgs e)
        {
            //Utilities.DebugLogger.Log("Start Updating Viewport Dimensions (" + this.ActualWidth + ", " + this.ActualHeight + ")");
            controller.UpdateViewportDimensions((int)this.ActualWidth, (int)this.ActualHeight);
            //Utilities.DebugLogger.Log("End Updating Viewport Dimensions");
        }

        #endregion EventHandlers

        #region StylusHandlers
        protected override void OnStylusDown(StylusDownEventArgs e)
        {

        }
        protected override void OnStylusMove(StylusEventArgs e)
        {

        }
        protected override void OnStylusUp(StylusEventArgs e)
        {

        }

        protected override void OnStylusEnter(StylusEventArgs e)
        {
            base.OnStylusEnter(e);
            MouseMode = TMouseMode.eMouseFreehandAnnotationMode;
        }

        protected override void OnStylusLeave(StylusEventArgs e)
        {
            base.OnStylusLeave(e);
            MouseMode = TMouseMode.eMouseSelectMode;
        }

        #endregion StylusHandlers

    }
}
