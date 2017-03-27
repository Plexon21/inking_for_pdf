using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System;
using System.Windows.Controls.Primitives;
using PdfTools.PdfViewerCSharpAPI.Model;
using PdfTools.PdfViewerCSharpAPI.Utilities;


namespace PdfTools.PdfViewerWPF.CustomControls
{
    /// <summary>
    /// </summary>
    internal partial class PdfViewerComponent : UserControl
    {

        public bool DocumentOpened()
        {
            return true;
        }

        private PdfViewerPane viewerPane;
        private IPdfViewerController controller;


        private ScrollBar HorizontalScrollbar;
        private ScrollBar VerticalScrollbar;

        //sets the size of the scrollbars, must be >=20
        private static readonly int scrollbarSize = 20;



        /// <summary>
        /// Initializes a new TileScrollViewer.
        /// </summary>
        public PdfViewerComponent()
        {
            InitializeComponent();

            //IsScrollingByMouseDragEnabled = true;

            HorizontalScrollbar = new ScrollBar();
            VerticalScrollbar = new ScrollBar();
            HorizontalScrollbar.Orientation = Orientation.Horizontal;
            VerticalScrollbar.Orientation = Orientation.Vertical;
            
            Grid.SetColumn(HorizontalScrollbar, 0);
            Grid.SetRow(HorizontalScrollbar, 1);
            Grid.SetColumn(VerticalScrollbar, 1);
            Grid.SetRow(VerticalScrollbar, 0);
            viewerComponentGrid.Children.Add(HorizontalScrollbar);
            viewerComponentGrid.Children.Add(VerticalScrollbar);

            HorizontalScrollbar.Height = scrollbarSize;
            VerticalScrollbar.Width = scrollbarSize;

            GridLength gH = new GridLength(HorizontalScrollbar.Height);
            GridLength gV = new GridLength(VerticalScrollbar.Width);
            scrollbarheight.Height = gH;
            scrollbarwidth.Width = gV;

            HorizontalScrollbar.Scroll += HorizontalScrollHandler;
            VerticalScrollbar.Scroll += VerticalScrollHandler;

            HorizontalScrollbar.IsEnabled = false;
            HorizontalScrollbar.Maximum = 0.0;
            VerticalScrollbar.IsEnabled = false;
            VerticalScrollbar.Maximum = 0.0;
        }

        
        /// <summary>
        /// Set the viewerPane shown in this component
        /// </summary>
        /// <param name="pane"></param>
        public void SetViewerPane(PdfViewerPane pane)
        {
            this.viewerPane = pane;

            Grid.SetColumn(viewerPane, 0);
            Grid.SetRow(viewerPane, 0);
            viewerComponentGrid.Children.Add(viewerPane);
        }


        public void SetController(IPdfViewerController controller)
        {
            this.controller = controller;
            controller.ScrollableAreaChanged += onScrollableAreaChanged;
            controller.ViewportRectangleChanged += onViewportRectangleChanged;
        }

        #region ScrollbarMethods

        private PdfTargetRect viewportRect = null;
        private PdfTargetRect scrollableRect = null;

        private void onScrollableAreaChanged(PdfTargetRect scrollableRect)
        {
            this.scrollableRect = scrollableRect;
            if (viewportRect != null)
                updateScrollBars(viewportRect, scrollableRect);
        }
        private void onViewportRectangleChanged(PdfTargetRect viewportRect)
        {
            this.viewportRect = viewportRect;
            if (scrollableRect != null)
                updateScrollBars(viewportRect, scrollableRect);
        }


        private void updateScrollBars(PdfTargetRect viewportRect, PdfTargetRect scrollableRect)
        {
            double horizontalPosition = ((double)viewportRect.iX + (double)scrollableRect.iWidth / 2.0)  / (double)(scrollableRect.iWidth - viewportRect.iWidth);
            double verticalPosition = (double)(viewportRect.iY - scrollableRect.iY)/ (double)(scrollableRect.iHeight - viewportRect.iHeight);
            double horizontalSize = (double)viewportRect.iWidth / (double)scrollableRect.iWidth;
            double verticalSize = (double)viewportRect.iHeight / (double)scrollableRect.iHeight;
            if (horizontalSize >= 1.0 || Double.IsNaN(horizontalPosition))
            {
                HorizontalScrollbar.IsEnabled = false;
                HorizontalScrollbar.Maximum = 0.0;
            }
            else
            {
                HorizontalScrollbar.IsEnabled = true;
                HorizontalScrollbar.Maximum = 1.0;
                HorizontalScrollbar.Value = horizontalPosition;
                HorizontalScrollbar.ViewportSize = horizontalSize / (1 - horizontalSize);

                HorizontalScrollbar.SmallChange = 0.05 * horizontalSize;
                HorizontalScrollbar.LargeChange = 0.9 * horizontalSize;
            }
            if (verticalSize >= 1.0 || Double.IsNaN(verticalPosition))
            {
                VerticalScrollbar.IsEnabled = false;
                VerticalScrollbar.Maximum = 0.0;
            }
            else
            {
                VerticalScrollbar.IsEnabled = true;
                VerticalScrollbar.Maximum = 1.0;
                VerticalScrollbar.Value = verticalPosition;
                VerticalScrollbar.ViewportSize = verticalSize / (1 - verticalSize);
                //DebugLogger.Log("verticalscrollbar pos=" + verticalPosition + "\t verticalSize=" + verticalSize);

                VerticalScrollbar.SmallChange = 0.05 * verticalSize;
                VerticalScrollbar.LargeChange = 0.9 * verticalSize;
            }
        }


        private void HorizontalScrollHandler(object sender, ScrollEventArgs e)
        {
            if(controller.IsOpen)
                controller.ScrollToHorizontalPercentage(e.NewValue);
        }
        private void VerticalScrollHandler(object sender, ScrollEventArgs e)
        {
            if (controller.IsOpen)
                controller.ScrollToVerticalPercentage(e.NewValue);
        }

        #endregion ScrollbarMethods


    } // class DragDropScrollViewer
} // namepsace Pdftools.PdfViewerNET.CustomControls
