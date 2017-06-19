using System;
using System.Text.RegularExpressions;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using System.ComponentModel;
using System.Collections.Generic;
using PdfTools.PdfViewerWPF;
using PdfTools.PdfViewerCSharpAPI.Model;
using PdfTools.PdfViewerCSharpAPI.Utilities;
using PdfTools.PdfViewerCSharpAPI.DocumentManagement;
using static PdfTools.PdfViewerCSharpAPI.DocumentManagement.PdfDocument;
using System.Runtime.InteropServices;

namespace ViewerWPFSample
{

    /// <summary>
    /// Interaktionslogik für MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        public static string extractedTextMessageBoxTitle = "Extracted text";

        private List<MenuItem> mouseModeMenuItems;

        public MainWindow()
        {
            InitializeComponent();

            PdfViewer.SearchCompleted += OnSearchCompleted;
            PdfViewer.OpenCompleted += OnOpenCompletedEventHandler;
            PdfViewer.PreviewKeyDown += OnKeyDown;

            PdfViewer.DragEnter += DragEnteredEventHandler;
            PdfViewer.Drop += DropEventHandler;
            PdfViewer.ContextMenuOpening += OnContextMenuOpening;
            PdfViewer.PropertyChanged += PageLayoutModeChangedEventHandler;
            PdfViewer.PropertyChanged += FitModeChangedEventHandler;
            PdfViewer.PropertyChanged += MouseModeChangedEventHandler;

            //Logger.FileName = @"E:\tmp\wpflog.txt";

            //Contextmenu stuff 
            ContextMenu = new ContextMenu();

            annotationMenuItem = new MenuItem();
            annotationMenuItem.Header = "Annotation";
            annotationMenuItem.Click += CreateAnnotation;
            ContextMenu.Items.Add(annotationMenuItem);

            highlightMenuItem = new MenuItem();
            highlightMenuItem.Header = "Highlight";
            //highlightMenuItem.Click += MoveSelectedRectsToHighlightedRects;
            ContextMenu.Items.Add(highlightMenuItem);

            removeHighlightMenuItem = new MenuItem();
            removeHighlightMenuItem.Header = "Remove Highlight";
            //removeHighlightMenuItem.Click += RemoveHighlightedRects;
            ContextMenu.Items.Add(removeHighlightMenuItem);

            copySelectedMenuItem = new MenuItem();
            copySelectedMenuItem.Header = "Copy to Clipboard";
            CopyCommand copyCommand = new CopyCommand(PdfViewer);
            copySelectedMenuItem.Command = copyCommand;
            ContextMenu.Items.Add(copySelectedMenuItem);
            InputBindings.Add(new KeyBinding(copyCommand, new KeyGesture(Key.C, ModifierKeys.Control)));

            mouseModeMenuItems = new List<MenuItem>();
            ContextMenu.Items.Add(new Separator());
            foreach (TMouseMode mouseMode in Enum.GetValues(typeof(TMouseMode)))
            {
                if (mouseMode == TMouseMode.eMouseUndefMode)
                    continue;
                MenuItem item = new MenuItem();
                item.Header = translateMouseMode(mouseMode);
                item.Click += delegate { PdfViewer.MouseMode = mouseMode; };
                ContextMenu.Items.Add(item);
                mouseModeMenuItems.Add(item);
            }
        }

        private String translateMouseMode(TMouseMode mode)
        {
            switch (mode)
            {
                case TMouseMode.eMouseMarkMode: return "Mark area tool";
                case TMouseMode.eMouseMoveMode: return "Hand tool";
                case TMouseMode.eMouseSelectMode: return "Text selection tool";
                case TMouseMode.eMouseZoomMode: return "Zoom tool";
                default: return "undefined mouse mode";
            }
        }

        MenuItem highlightMenuItem = null;
        MenuItem removeHighlightMenuItem = null;
        MenuItem copySelectedMenuItem = null;
        MenuItem annotationMenuItem = null;

        public void CreateAnnotation(object sender, RoutedEventArgs e)
        {

            PdfViewer.MouseMode = TMouseMode.eMouseFreehandAnnotationMode;

            #region TESTING
            /*
            PdfDocument doc = (PdfDocument)this.PdfViewer.GetController().GetCanvas().DocumentManager.GetDocument();

            

            double[] r1 = new double[] { 400, 400, 450, 400 };
            double[] r2 = new double[] { 450, 400, 450, 450 };
            double[] r3 = new double[] { 450, 450, 400, 450 };
            double[] r4 = new double[] { 400, 450, 400, 400 };

            double[] spline = new double[] { 200, 200, 300, 300, 100, 350 };
            double[] dMark = new double[] { 50, 50 };

            int count = 0;

            bool x = doc.GetAnnotations(1, out IntPtr pointer, ref count);

            double[] color1 = new double[] { 1, 0, 0, 0 };
            double[] color2 = new double[] { 0, 1, 0, 0 };
            double[] color3 = new double[] { 0, 0, 1, 0 };
            double[] color4 = new double[] { 0, 0, 0, 1 };

            //doc.DeleteAnnotation(pointer[0]);

            //IntPtr markup = doc.CreateAnnotation(PdfDocument.TPdfAnnotationType.eAnnotationText, 1, dMark, 2, color4 ,4, 1);
            //IntPtr tspline = doc.CreateAnnotation(PdfDocument.TPdfAnnotationType.eAnnotationInk, 1, spline, 6, color4, 4, 10);
            IntPtr t1 = doc.CreateAnnotation(PdfDocument.TPdfAnnotationType.eAnnotationInk, 1, r1, 4, color1, 4, 10);
            IntPtr t2 = doc.CreateAnnotation(PdfDocument.TPdfAnnotationType.eAnnotationInk, 1, r2, 4, color2, 4, 10);
            IntPtr t3 = doc.CreateAnnotation(PdfDocument.TPdfAnnotationType.eAnnotationInk, 1, r3, 4, color3, 4, 10);
            IntPtr t4 = doc.CreateAnnotation(PdfDocument.TPdfAnnotationType.eAnnotationInk, 1, r4, 4, color4, 4, 10);


            //x = doc.GetAnnotations(1, out pointer, ref count);








            //TPdfAnnotation anno2 = (TPdfAnnotation)Marshal.PtrToStructure(pointers[count-1], typeof(TPdfAnnotation));





            x = doc.GetAnnotations(1, out pointer, ref count);

            IntPtr[] pointers = new IntPtr[count];
            IntPtr p = pointer;

            for (int i = 0; i < count; i++)
            {
                pointers[i] = p;
                p += Marshal.SizeOf(typeof(IntPtr));
            }

            TPdfAnnotation anno = (TPdfAnnotation)Marshal.PtrToStructure(pointers[0], typeof(TPdfAnnotation));
            int asdf = doc.UpdateAnnotation(anno.annotationHandle, 1, null, "content", "label", color2, 4, -1);

            TPdfAnnotation anno2 = (TPdfAnnotation)Marshal.PtrToStructure(pointers[0], typeof(TPdfAnnotation));

            //doc.DeleteAnnotation(anno.annotationHandle);


            //annotationMenuItem.Header = "done";
            var cont = (PdfViewerController)this.PdfViewer.GetController();
            cont.LoadAllAnnotationsOnPage(1);
            */
#endregion TESTING
        }


        /// <summary>
        /// Disposes of the internal viewer (important to prevent memory leaks).
        /// </summary>
        /// <param name="e">Event parameters.</param>
        protected override void OnClosed(EventArgs e)
        {
            PdfViewer.Dispose();
            base.OnClosed(e);
        }

        private void OnSearchCompleted(PdfSearcher.SearchResult result)
        {
            if (result == null)
            {
                MessageBox.Show(Properties.MainWindowRes.search_no_match);
            }
        }

        private void textExtractedEventHandler(string text)
        {
            MessageBoxWindow mbw = new MessageBoxWindow(text, extractedTextMessageBoxTitle);
            mbw.Show();
            /*MessageBox.Show(this, text, extractedTextMessageBoxTitle);
            Clipboard.SetText(text);*/
        }

        private void Open_Click(object sender, RoutedEventArgs e)
        {
            Microsoft.Win32.OpenFileDialog dialog = new Microsoft.Win32.OpenFileDialog();
            dialog.Multiselect = false;
            bool? result = dialog.ShowDialog();

            if (result == true)
            {
                OpenFile(dialog.FileName, "");
                DocumentName.Text = dialog.FileName;
            }
        }
        private string filename = "";
        private string password = "";
        public void OpenFile(string filename, string password)
        {
            this.filename = filename;
            this.password = password;
            try
            {
                PdfViewer.Open(filename, password);
            }
            catch (PdfLicenseInvalidException ex)
            {
                if (TryObtainValidLicense(ex))
                    OpenFile(filename, password);
            }
        }

        private bool TryObtainValidLicense(PdfLicenseInvalidException ex)
        {
            InputDialog inputDialog = new InputDialog(ex.Message + " Please insert a valid license:");
            this.Focus();
            if (inputDialog.ShowDialog() != true)
                return false;
            try
            {
                PdfViewerWPF.SetLicenseKey(inputDialog.Answer);
            }
            catch (PdfLicenseInvalidException e)
            {
                TryObtainValidLicense(e);
            }
            return true;
        }


        private void Close_Click(object sender, RoutedEventArgs e)
        {
            PdfViewer.Close();
        }


        private void Left_Click(object sender, RoutedEventArgs e)
        {
            PdfViewer.PreviousPage();
        }

        private void Start_Click(object sender, RoutedEventArgs e)
        {
            PdfViewer.PageNo = 1;
        }


        private void Right_Click(object sender, RoutedEventArgs e)
        {
            PdfViewer.NextPage();
        }

        private void End_Click(object sender, RoutedEventArgs e)
        {
            PdfViewer.PageNo = PdfViewer.PageCount;
        }

        private void FitActualSize_Click(object sender, RoutedEventArgs e)
        {
            PdfViewer.FitMode = FitMode.FitTrueSize;
            HighlightButton(FitActualSize_Button, true);
            HighlightButton(FitPage_Button, false);
            HighlightButton(FitWidth_Button, false);
        }

        private void FitPage_Click(object sender, RoutedEventArgs e)
        {
            PdfViewer.FitMode = FitMode.FitPage;
            HighlightButton(FitActualSize_Button, false);
            HighlightButton(FitPage_Button, true);
            HighlightButton(FitWidth_Button, false);
        }

        private void FitWidth_Click(object sender, RoutedEventArgs e)
        {
            PdfViewer.FitMode = FitMode.FitWidth;
            HighlightButton(FitActualSize_Button, false);
            HighlightButton(FitPage_Button, false);
            HighlightButton(FitWidth_Button, true);
        }

        private void FitModeChangedEventHandler(object sender, PropertyChangedEventArgs e)
        {
            if (e.PropertyName != "FitMode")
                return;
            HighlightButton(FitActualSize_Button, false);
            HighlightButton(FitPage_Button, false);
            HighlightButton(FitWidth_Button, false);


            switch (PdfViewer.FitMode)
            {
                case FitMode.FitTrueSize:
                    HighlightButton(FitActualSize_Button, true);
                    break;
                case FitMode.FitPage:
                    HighlightButton(FitPage_Button, true);
                    break;
                case FitMode.FitWidth:
                    HighlightButton(FitWidth_Button, true);
                    break;
                default:
                    break;
            }
        }

        private void MouseModeChangedEventHandler(object sender, PropertyChangedEventArgs e)
        {
            if (e.PropertyName != "MouseMode")
                return;
            HighlightButton(ZoomMode_Button, false);
            HighlightButton(Mark_Button, false);
            HighlightButton(TextSelection_Button, false);

            switch (PdfViewer.MouseMode)
            {
                case TMouseMode.eMouseMarkMode:
                    HighlightButton(Mark_Button, true);
                    break;
                case TMouseMode.eMouseZoomMode:
                    HighlightButton(ZoomMode_Button, true);
                    break;
                case TMouseMode.eMouseSelectMode:
                    HighlightButton(TextSelection_Button, true);
                    break;
            }

        }

        private void OnOpenCompletedEventHandler(PdfViewerException exception)
        {
            if (exception == null)
            {
                return;
            }

            Logger.LogException(exception);
            if (exception is PdfLicenseInvalidException)
            {
                if (TryObtainValidLicense((PdfLicenseInvalidException)exception))
                    OpenFile(filename, password);
            }
            else if (exception is PdfFileNotFoundException)
            {
                MessageBox.Show("File \"" + filename + "\" was not found.");
            }
            else if (exception is PdfPasswordException)
            {
                PasswordWindow passwordWindow = new PasswordWindow();
                this.Focus();
                if (passwordWindow.ShowDialog() != true)
                    return;
                OpenFile(filename, passwordWindow.Password);
            }
            else if (exception is PdfFileCorruptException)
            {
                MessageBox.Show("The opened file \"" + filename + "\" is not a pdf file or is corrupt:\n" + exception.Message);
                Logger.LogException(exception);
            }
            else if (exception is PdfUnsupportedFeatureException)
            {
                MessageBox.Show("The opened file \"" + filename + "\" uses features that are not supported by the rendering engine:\n" + exception.Message);
                Logger.LogException(exception);
            }
            else if (exception is PdfNoFileOpenedException)
            {
                MessageBox.Show("The Application closed the file before opening could complete");
                Logger.LogException(exception);
            }
            else
            {
                MessageBox.Show("An unexpected Exception occured: \"" + exception.Message + "\" " + exception.ToString(), "Exception");
                Logger.LogException(exception);
            }
        }

        private void ZoomIn_Click(object sender, RoutedEventArgs e)
        {
            PdfViewer.Zoom = PdfViewer.Zoom * 6 / 5;
        }

        private void ZoomOut_Click(object sender, RoutedEventArgs e)
        {
            PdfViewer.Zoom = PdfViewer.Zoom * 5 / 6;
        }

        private void ZoomMode_Click(object sender, RoutedEventArgs e)
        {

            if (PdfViewer.MouseMode == TMouseMode.eMouseZoomMode)
                PdfViewer.MouseMode = TMouseMode.eMouseMoveMode;
            else
                PdfViewer.MouseMode = TMouseMode.eMouseZoomMode;
        }

        private void MarkClick(object sender, RoutedEventArgs e)
        {
            if (PdfViewer.MouseMode == TMouseMode.eMouseMarkMode)
                PdfViewer.MouseMode = TMouseMode.eMouseMoveMode;
            else
                PdfViewer.MouseMode = TMouseMode.eMouseMarkMode;
        }

        private void TextSelectionClick(object sender, RoutedEventArgs e)
        {
            if (PdfViewer.MouseMode == TMouseMode.eMouseSelectMode)
            {
                PdfViewer.MouseMode = TMouseMode.eMouseMoveMode;
            }
            else
            {
                PdfViewer.MouseMode = TMouseMode.eMouseSelectMode;
            }
        }

        private void Rotate_Click(object sender, RoutedEventArgs e)
        {
            PdfViewer.Rotate = PdfViewer.Rotate + 90;
        }


        /*        private void LayoutModeChangedEventHandler(object sender, DependencyPropertyChangedEventArgs e)
                {
                  HighlightButton(LayoutDocument_Button, false);
                    HighlightButton(LayoutPage_Button, false);
  
                    LayoutMode mode = (LayoutMode)e.NewValue;
                    switch (mode)
                    {
                        case LayoutMode.LayoutDocument:
                            HighlightButton(LayoutDocument_Button, true);
                            break;
                    }
                        case LayoutMode.LayoutPage:
                            HighlightButton(LayoutPage_Button, true);
                            break;
                        default:
                            break;
                }
        */
        private void PageLayoutSinglePage_Click(object sender, RoutedEventArgs e)
        {
            PdfViewer.PageLayoutMode = TPageLayoutMode.SinglePage;
        }
        private void PageLayoutOneColumn_Click(object sender, RoutedEventArgs e)
        {
            PdfViewer.PageLayoutMode = TPageLayoutMode.OneColumn;
        }
        private void PageLayoutTwoColumnLeft_Click(object sender, RoutedEventArgs e)
        {
            PdfViewer.PageLayoutMode = TPageLayoutMode.TwoColumnLeft;
        }
        private void PageLayoutTwoColumnRight_Click(object sender, RoutedEventArgs e)
        {
            PdfViewer.PageLayoutMode = TPageLayoutMode.TwoColumnRight;
        }
        private void PageLayoutTwoPageLeft_Click(object sender, RoutedEventArgs e)
        {
            PdfViewer.PageLayoutMode = TPageLayoutMode.TwoPageLeft;
        }
        private void PageLayoutTwoPageRight_Click(object sender, RoutedEventArgs e)
        {
            PdfViewer.PageLayoutMode = TPageLayoutMode.TwoPageRight;
        }
        /*
        private bool paused = false;
        private void PausePlayClick(object sender, RoutedEventArgs e)
        {
            if (paused)
            {
                PdfViewer.ResumeLayout();
            }
            else
            {
                PdfViewer.SuspendLayout();
            }
            paused = !paused;
            HighlightButton(PausePlay_Button, paused);
        }*/

        private void PageLayoutModeChangedEventHandler(object sender, PropertyChangedEventArgs args)
        {
            if (args.PropertyName != "PageLayoutMode")
                return;
            HighlightButton(PageLayoutSinglePage_Button, false);
            HighlightButton(PageLayoutOneColumn_Button, false);
            HighlightButton(PageLayoutTwoColumnLeft_Button, false);
            HighlightButton(PageLayoutTwoColumnRight_Button, false);
            HighlightButton(PageLayoutTwoPageLeft_Button, false);
            HighlightButton(PageLayoutTwoPageRight_Button, false);

            switch (PdfViewer.PageLayoutMode)
            {
                case TPageLayoutMode.SinglePage:
                    HighlightButton(PageLayoutSinglePage_Button, true);
                    break;
                case TPageLayoutMode.OneColumn:
                    HighlightButton(PageLayoutOneColumn_Button, true);
                    break;
                case TPageLayoutMode.TwoColumnLeft:
                    HighlightButton(PageLayoutTwoColumnLeft_Button, true);
                    break;
                case TPageLayoutMode.TwoColumnRight:
                    HighlightButton(PageLayoutTwoColumnRight_Button, true);
                    break;
                case TPageLayoutMode.TwoPageLeft:
                    HighlightButton(PageLayoutTwoPageLeft_Button, true);
                    break;
                case TPageLayoutMode.TwoPageRight:
                    HighlightButton(PageLayoutTwoPageRight_Button, true);
                    break;
            }
        }

        private void HighlightButton(Button button, bool highlight)
        {
            Brush brush = (highlight) ? Brushes.DodgerBlue : Brushes.Transparent;
            button.Background = brush;
        }


        private void ShadowOnOff_Click(object sender, RoutedEventArgs e)
        {
            if (PdfViewer.Border == 0)
            {
                PdfViewer.Border = 6;
            }
            else
            {
                PdfViewer.Border = 0;
            }
        }

        private void SearchTextBox_OnTextChanged(object sender, TextChangedEventArgs e)
        {
            if (string.IsNullOrEmpty(SearchTextBox.Text))
            {
                NextSearchButton.IsEnabled = false;
            }
            else
            {
                NextSearchButton.IsEnabled = true;
            }
        }

        private void NextSearchClick(object sender, RoutedEventArgs e)
        {
            Search();
        }

        private void Search()
        {
            if (PdfViewer.PageCount <= 0)
            {
                return;
            }
            PdfViewer.Search(SearchTextBox.Text);
        }


        private void SearchResult(string searchText, bool result)
        {
            if (!result)
            {
                MessageBox.Show(string.Format(Properties.MainWindowRes.text_not_found, searchText), Properties.MainWindowRes.not_found);
            }
        }

        private void SearchResultNextSearch(string searchText, bool result)
        {
            if (!result)
            {
                MessageBox.Show(Properties.MainWindowRes.no_more_matches, Properties.MainWindowRes.search);
            }
        }

        private void SearchTextBox_OnKeyDown(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.Enter)
            {
                Search();
            }
        }

        private void CancelSearchClick(object sender, RoutedEventArgs e)
        {
        }


        private void OnKeyDown(Object sender, KeyEventArgs e)
        {
            e.Handled = true;
            if (!PdfViewer.IsOpen)
                return;
            switch (e.Key)
            {
                case Key.PageUp:
                    PdfViewer.PreviousPage();
                    break;
                case Key.PageDown:
                    PdfViewer.NextPage();
                    break;
                case Key.Up:
                    PdfViewer.Scroll(0, (int)(0.05 * PdfViewer.ActualHeight));
                    break;
                case Key.Down:
                    PdfViewer.Scroll(0, (int)(-0.05 * PdfViewer.ActualHeight));
                    break;
                case Key.Left:
                    PdfViewer.Scroll((int)(0.05 * PdfViewer.ActualWidth), 0);
                    break;
                case Key.Right:
                    PdfViewer.Scroll((int)(-0.05 * PdfViewer.ActualWidth), 0);
                    break;
                default:
                    //do nothing
                    return;
            }
        }

        #region Drag and drop event handlers

        private void DragEnteredEventHandler(Object sender, DragEventArgs e)
        {
            if (!e.Data.GetDataPresent(DataFormats.FileDrop))
                e.Effects = DragDropEffects.None;
            else
                e.Effects = DragDropEffects.Copy;
        }

        private void DropEventHandler(Object sender, DragEventArgs e)
        {
            string[] filenames = (string[])e.Data.GetData(DataFormats.FileDrop);
            if (filenames != null && filenames[0] != null)
                PdfViewer.Open(filenames[0], "");
        }


        #endregion Drag and drop event handlers


        public class CopyCommand : ICommand
        {
            public CopyCommand(PdfViewerWPF viewer)
            {
                this.viewer = viewer;
            }

            public bool CanExecute(Object parameter)
            {
                return viewer != null;
            }

            public void Execute(Object parameter)
            {
                string text = Regex.Replace(viewer.SelectedText, "\\s+", " ");
                System.Windows.Clipboard.SetText(text);
            }
            private PdfViewerWPF viewer = null;
            public event EventHandler CanExecuteChanged
            {
                add { }
                remove { }
            }
        }

        private void OnContextMenuOpening(object sender, ContextMenuEventArgs args)
        {
            string s = translateMouseMode(PdfViewer.MouseMode);
            foreach (MenuItem item in mouseModeMenuItems)
            {
                item.IsChecked = s.Equals(item.Header);
            }
            highlightMenuItem.IsEnabled = false;
            removeHighlightMenuItem.IsEnabled = false;
            copySelectedMenuItem.IsEnabled = (PdfViewer.SelectedText.Length > 0);
        }
    }
}
