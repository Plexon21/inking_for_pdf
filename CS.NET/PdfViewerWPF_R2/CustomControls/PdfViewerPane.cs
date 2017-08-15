﻿using System;
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
            selectedAnnotations.Clear();
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
            Brush freehandBrush = new SolidColorBrush(AnnotationStrokeColor);
            freehandBrush.Opacity = 1.0;
            double width = AnnotationStrokeWidthZoomDependent ? AnnotationStrokeWidth : AnnotationStrokeWidth * controller.ZoomFactor;
            Pen annotPen = new Pen(freehandBrush, width);
            annotPen.EndLineCap = PenLineCap.Round;
            annotPen.StartLineCap = PenLineCap.Round;

            //set pen for selectedAnnotations
            Pen selectedAnnotationsPen = new Pen(new SolidColorBrush(Colors.Blue), 0.5);

            //set pen for movingAnnotations
            Pen movingAnnotationsPen = new Pen(new SolidColorBrush(Colors.Red), 0.5);


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
                if (creatingDrawingAnnotation)
                {
                    if (annotationPoints != null)
                    {
                        var annotList = controller.DrawForm(annotationPoints);
                        if (annotList != null)
                        {
                            foreach (var annot in annotList)
                            {
                                if (annot != null && annot.Count > 1)
                                {
                                    for (int i = 0; i < annot.Count - 1; i++)
                                    {
                                        dc.DrawLine(annotPen, annot[i], annot[i + 1]);
                                    }
                                }
                            }

                        }
                    }
                }
                if (MouseMode == TMouseMode.eMouseClickAnnotationMode)
                {
                    if (annotationPoints != null)
                    {
                        var annotList = controller.DrawForm(annotationPoints);
                        if (annotList != null)
                        {
                            foreach (var annot in annotList)
                            {
                                if (annot != null && annot.Count > 1)
                                {
                                    for (var i = 0; i < annot.Count - 1; i++)
                                    {
                                        dc.DrawLine(annotPen, annot[i], annot[i + 1]);
                                    }
                                    dc.DrawLine(annotPen, annot[annot.Count - 1],
                                        lastMousePosition);
                                }
                            }
                        }
                    }
                }

                if (MouseMode == TMouseMode.eMouseTextRecognitionMode)
                {
                    if (annotationPoints != null)
                    {
                        for (int i = 0; i < annotationPoints.Count - 1; i++)
                        {
                            dc.DrawLine(annotPen, annotationPoints[i], annotationPoints[i + 1]);
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

                    if (movingAnnotation)
                    {
                        dc.DrawLine(movingAnnotationsPen, annotationPoints[0], lastMousePosition);

                        double deltaX = lastMousePosition.X - annotationPoints[0].X;
                        double deltaY = lastMousePosition.Y - annotationPoints[0].Y;

                        foreach (PdfAnnotation annot in selectedAnnotations)
                        {
                            PdfSourceRect rectOnPage = new PdfSourceRect(annot.Rect[0], annot.Rect[1], annot.Rect[2] - annot.Rect[0], annot.Rect[3] - annot.Rect[1]);

                            Rect rectWin = controller.TransformRectPageToViewportWinRect(rectOnPage, annot.PageNr);

                            rectWin.X += deltaX;
                            rectWin.Y += deltaY;

                            dc.DrawRectangle(null, movingAnnotationsPen, rectWin);
                        }
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
                UpdateAnnotationOnMouseModeChanged(value);
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
                    case TMouseMode.eMouseDrawAnnotationMode: this.Cursor = System.Windows.Input.Cursors.Pen; break;
                    case TMouseMode.eMouseTextRecognitionMode: this.Cursor = System.Windows.Input.Cursors.Pen; break;
                    default: this.Cursor = System.Windows.Input.Cursors.Arrow; break;
                }
            }
        }

        #region [InkingForPDF] Annotation Variables

        private IList<System.Windows.Point> annotationPoints;
        private PdfSourceRect selectedRectOnPage;
        private IList<PdfAnnotation> selectedAnnotations = new List<PdfAnnotation>();
        private StrokeCollection strokes = new StrokeCollection();
        private TMouseMode stylusRememberedMouseMode = TMouseMode.eMouseUndefMode;

        private double maxAnnotationStrokeWidth = 12;

        //backing fields
        private bool _annotationStrokeWidthZoomDependent = false;
        private double _annotationStrokeWidth = 1;
        private Color _annotationStrokeColor = Colors.Black;
        private bool _annotationMarkingOnIntersect = false;

        #endregion [InkingForPDF] Annotation Variables

        #region [InkingForPDF] Annotation Properties

        public bool AnnotationStrokeWidthZoomDependent
        {
            set
            {
                _annotationStrokeWidthZoomDependent = value;
            }
            get
            {
                return _annotationStrokeWidthZoomDependent;
            }
        }

        public double AnnotationStrokeWidth
        {
            set
            {
                if (value >= 0 && value <= maxAnnotationStrokeWidth)
                {
                    _annotationStrokeWidth = value;

                    if (selectedAnnotations.Count > 0)
                    {
                        controller.UpdateAnnotations(new UpdateAnnotationArgs(selectedAnnotations.Select(annot => annot.UpdateWidth(_annotationStrokeWidth)).ToList()));
                    }
                }
            }
            get
            {
                return _annotationStrokeWidth;
            }
        }

        public Color AnnotationStrokeColor
        {
            set
            {
                _annotationStrokeColor = value;
                _annotationStrokeColor.A = 255;

                if (selectedAnnotations.Count > 0)
                {
                    controller.UpdateAnnotations(new UpdateAnnotationArgs(selectedAnnotations.Select(annot => annot.UpdateColor(_annotationStrokeColor)).ToList()));
                }
            }
            get
            {
                return _annotationStrokeColor;
            }
        }

        public bool AnnotationMarkingOnIntersect
        {
            set
            {
                _annotationMarkingOnIntersect = value;
            }
            get
            {
                return _annotationMarkingOnIntersect;
            }
        }

        #endregion [InkingForPDF] Annotation Properties

        #region [InkingForPDF] Annotation Methods


        public void MassCreateAnnotations(int annotCount, int pointCount)
        {
            Random rnd = new Random();

            double minX = 100;
            double minY = 100;
            double maxX = 400;
            double maxY = 700;

            IList<PdfAnnotation> annots = new List<PdfAnnotation>();
            double[] color = new double[] { AnnotationStrokeColor.R / 255.0, AnnotationStrokeColor.G / 255.0, AnnotationStrokeColor.B / 255.0 };
            double width = AnnotationStrokeWidthZoomDependent ? AnnotationStrokeWidth / controller.ZoomFactor : AnnotationStrokeWidth;


            for (int annotIndex = 0; annotIndex < annotCount; annotIndex++)
            {
                double[] annotPoints = new double[pointCount * 2];

                for (int point = 0; point < pointCount; point++)
                {
                    annotPoints[point * 2] = (rnd.NextDouble() * (maxX - minX)) + minX;
                    annotPoints[point * 2 + 1] = (rnd.NextDouble() * (maxY - minY)) + minY;
                }

                annots.Add(new PdfAnnotation(PdfDocument.TPdfAnnotationType.eAnnotationInk, controller.FirstPageOnViewport, annotPoints, color, width));
            }

            controller.CreateAnnotationsWithoutMapper(annots);
        }


        private void UpdateAnnotationOnMouseModeChanged(TMouseMode value)
        {
            if (value != TMouseMode.eMouseTextRecognitionMode)
            {
                strokes = new StrokeCollection();
            }
            if (value != TMouseMode.eMouseMarkMode)
            {
                movingAnnotation = false;
                mouseOverAnnotationInMarkMode = false;
            }
            annotationPoints = null;

            selectedAnnotations.Clear();
        }

        private void CreateAnnotation()
        {

            int pointCount = annotationPoints.Count;

            double[] points = new double[pointCount * 2];

            try
            {
                int firstPage = 0;

                PdfSourcePoint firstPoint = controller.TransformOnScreenToOnNearestPage(new PdfTargetPoint(annotationPoints[0]), ref firstPage);

                points[0] = firstPoint.dX;
                points[1] = firstPoint.dY;

                if (pointCount >= 2)
                {

                    for (int i = 1; i < pointCount; i++)
                    {
                        PdfSourcePoint point = controller.TransformOnScreenToOnSpecificPage(new PdfTargetPoint(annotationPoints[i]), firstPage);

                        points[i * 2] = point.dX;
                        points[i * 2 + 1] = point.dY;
                    }

                }

                double[] color = new double[] { AnnotationStrokeColor.R / 255.0, AnnotationStrokeColor.G / 255.0, AnnotationStrokeColor.B / 255.0 };
                double width = AnnotationStrokeWidthZoomDependent ? AnnotationStrokeWidth / controller.ZoomFactor : AnnotationStrokeWidth;

                controller.CreateAnnotation(new PdfAnnotation(PdfDocument.TPdfAnnotationType.eAnnotationInk, firstPage, points, color, width));

                annotationPoints = null;
            }
            catch (ArgumentOutOfRangeException e)
            {
                Logger.LogError("Could not transform a point into page coordinates. Aborting creation.");
                Logger.LogException(e);


                annotationPoints = null;
                InvalidateVisual();
            }
        }

        private void MoveAnnotations(Point pointOrigin, Point pointDestination)
        {
            try
            {
                int pageOrigin = 0;
                int pageDestination = 0;

                PdfSourcePoint pointOriginPage = controller.TransformOnScreenToOnNearestPage(new PdfTargetPoint(pointOrigin), ref pageOrigin);
                PdfSourcePoint pointDestinationPage = controller.TransformOnScreenToOnNearestPage(new PdfTargetPoint(pointDestination), ref pageDestination);

                if (pageOrigin == pageDestination)
                {
                    PdfSourcePoint delta = pointDestinationPage - pointOriginPage;

                    controller.UpdateAnnotations(new UpdateAnnotationArgs(selectedAnnotations.Select(annot => annot.Move(delta.dX, delta.dY)).ToList()));
                }
            }
            catch (ArgumentOutOfRangeException ex)
            {
                Logger.LogError("Could not transform origin odr destination point into page coordinates");
                Logger.LogException(ex);
            }

            InvalidateVisual();

        }

        public void DeleteSelectedAnnotations()
        {
            mouseOverAnnotationInMarkMode = false;
            movingAnnotation = false;
            SetCursorAccordingToMouseMode();

            controller.DeleteAnnotations(selectedAnnotations);
        }

        public void EndTextRecognitionMode()
        {
            textRecognitionActive = false;
            RecognizeText();

            strokes = new StrokeCollection();
            MouseMode = TMouseMode.eMouseSelectMode;
        }
        public void EndCurrentClickAnnotation()
        {
            creatingClickAnnotation = false;
            if (annotationPoints != null) CreateAnnotation();
        }
        public void AbortCurrentClickAnnotation()
        {
            creatingClickAnnotation = false;
            annotationPoints = null;
            InvalidateVisual();
        }

        private void HandleSelectedRectangleOnCanvas(PdfSourceRect rectOnCanvas)
        {
            if (MouseMode != TMouseMode.eMouseMarkMode) return;

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
                IList<PdfAnnotation> markedAnnotations;

                if (selectedAnnotations != null && selectedAnnotations.Count > 0)
                {
                    markedAnnotations = annots.Where(annot => selectedAnnotations.Any(selAnnot => selAnnot.GetHandleAsLong() == annot.GetHandleAsLong())).ToList();
                }
                else
                {
                    markedAnnotations = annots?.Where(annot => annot.ContainsOrIntersectsWithRect(selectedRectOnPage, AnnotationMarkingOnIntersect)).ToList<PdfAnnotation>();
                }

                if (markedAnnotations != null)
                {
                    if (markedAnnotations.Count > 0)
                    {
                        if (MouseMode == TMouseMode.eMouseMarkMode)
                        {
                            selectedAnnotations = markedAnnotations;
                        }
                        else
                        {
                            selectedRectOnPage = null;
                        }
                    }
                    else
                    {
                        selectedAnnotations.Clear();
                    }
                }
            }

            InvalidateVisual();
        }


        public void RecognizeText()
        {
            //MessageBox.Show(controller.ConvertAnnotations(strokes));

            if (strokes != null && strokes.Count > 0)
            {
                Stroke firstStroke = strokes[0];

                if (firstStroke != null && firstStroke.StylusPoints != null && firstStroke.StylusPoints.Count > 0)
                {
                    StylusPoint firstPoint = firstStroke.StylusPoints[0];
                    int page = 0;

                    PdfSourcePoint pointOnPage = controller.TransformOnScreenToOnNearestPage(new PdfTargetPoint((int)firstPoint.X, (int)firstPoint.Y), ref page);

                    string content = controller.ConvertAnnotations(strokes);
                    double[] point = new double[] { pointOnPage.dX, pointOnPage.dY };
                    double[] color = new double[] { AnnotationStrokeColor.R / 255.0, AnnotationStrokeColor.G / 255.0, AnnotationStrokeColor.B / 255.0 };

                    controller.CreateTextAnnotation(content, page, point, color);

                }
            }
        }

        #endregion [InkingForPDF] Annotation Methods

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
        private Rect selectedRect = new Rect();
        private bool textRecognitionActive = false;

        private bool creatingDrawingAnnotation = false;
        private bool creatingClickAnnotation = false;
        private bool mouseOverAnnotationInMarkMode = false;
        private bool movingAnnotation = false;


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

        private void RightMouseDownEventHandler(Object sender, MouseEventArgs e)
        {
            Keyboard.Focus(this);
        }

        private void LeftMouseDownEventHandler(Object sender, MouseEventArgs e)
        {
            Keyboard.Focus(this);
            selectedRects.Clear();
            if (!mouseOverAnnotationInMarkMode) selectedAnnotations.Clear();
            movingAnnotation = false;

            _selectedText = String.Empty;
            if (!controller.IsOpen)
                return;
            if (OverrideMouseModeToWait)
                return;
            if (middleMouseScrolling)
                return; //handled by middlemousedown

            if (mouseOverAnnotationInMarkMode)
            {
                annotationPoints = new List<Point> { e.GetPosition(this) };
                movingAnnotation = true;
            }
            else if (MouseMode == TMouseMode.eMouseZoomMode || MouseMode == TMouseMode.eMouseMarkMode)
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
            else if (MouseMode == TMouseMode.eMouseDrawAnnotationMode)
            {
                annotationPoints = new List<Point>();
                annotationPoints.Add(e.GetPosition(this));
                creatingDrawingAnnotation = true;

                StylusPlugIns.First().Enabled = true;
            }
            else if (MouseMode == TMouseMode.eMouseClickAnnotationMode)
            {
                creatingClickAnnotation = true;
                StylusPlugIns.First().Enabled = true;
            }
            else if (MouseMode == TMouseMode.eMouseTextRecognitionMode)
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
            else if (creatingDrawingAnnotation || textRecognitionActive)
            {
                if (annotationPoints == null) annotationPoints = new List<Point>();
                annotationPoints.Add(e.GetPosition(this));
                InvalidateVisual();
            }
            else if (creatingClickAnnotation)
            {
                lastMousePosition = e.GetPosition(this);
                InvalidateVisual();
            }
            else if (movingAnnotation)
            {
                lastMousePosition = e.GetPosition(this);
                InvalidateVisual();
            }
            else if (MouseMode == TMouseMode.eMouseMarkMode && selectedAnnotations != null && selectedAnnotations.Count > 0)
            {
                int page = 0;
                try
                {
                    PdfSourcePoint point = controller.TransformOnScreenToOnNearestPage(new PdfTargetPoint(e.GetPosition(this)), ref page);

                    if (page > 0 && selectedAnnotations.Any(annot => annot.ContainsPoint(point) && annot.PageNr == page))
                    {
                        this.Cursor = Cursors.SizeAll;
                        this.mouseOverAnnotationInMarkMode = true;
                    }
                    else
                    {
                        this.Cursor = Cursors.Cross;
                        this.mouseOverAnnotationInMarkMode = false;
                    }
                }
                catch (ArgumentOutOfRangeException ex)
                {
                    Logger.LogError("Could not transform the point into page coordinates");
                    Logger.LogException(ex);
                    this.Cursor = Cursors.Cross;
                }


            }

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
                else if (MouseMode == TMouseMode.eMouseMarkMode && selectedRectangleOnCanvas != null)
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
            else if (creatingDrawingAnnotation)
            {
                creatingDrawingAnnotation = false;

                CreateAnnotation();
            }
            else if (textRecognitionActive)
            {
                var s = new Stroke(new StylusPointCollection(annotationPoints));
                strokes.Add(s);

                StylusPlugIns.First().Enabled = false;

                annotationPoints = null;
                textRecognitionActive = false;
            }
            else if (creatingClickAnnotation)
            {
                if (annotationPoints == null) annotationPoints = new List<Point>();
                annotationPoints.Add(e.GetPosition(this));
            }
            else if (movingAnnotation)
            {
                movingAnnotation = false;

                if (annotationPoints != null && annotationPoints.Count > 0)
                {
                    MoveAnnotations(annotationPoints[0], lastMousePosition);
                }

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
            stylusRememberedMouseMode = MouseMode;
            MouseMode = TMouseMode.eMouseDrawAnnotationMode;
        }

        protected override void OnStylusLeave(StylusEventArgs e)
        {
            base.OnStylusLeave(e);
            if (stylusRememberedMouseMode != TMouseMode.eMouseUndefMode)
            {
                MouseMode = stylusRememberedMouseMode;
            }
            else
            {
                MouseMode = TMouseMode.eMouseMoveMode;
            }
            SetCursorAccordingToMouseMode();
        }

        #endregion StylusHandlers



    }
}
