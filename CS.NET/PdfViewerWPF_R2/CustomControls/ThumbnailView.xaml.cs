using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace PdfTools.PdfViewerWPF.CustomControls
{
    using PdfTools.PdfViewerCSharpAPI.Model;
    using PdfTools.PdfViewerCSharpAPI.Utilities;
    using PdfTools.PdfViewerCSharpAPI.DocumentManagement.Requests;

    /// <summary>
    /// Interaction logic for ThumbnailView.xaml
    /// </summary>
    internal partial class ThumbnailView : UserControl
    {
        /// <summary>
        /// Thumbnail view element
        /// </summary>
        public ThumbnailView()
        {
            InitializeComponent();

            ThumbnailListBox.Width = Double.NaN;
            ThumbnailListBox.Height = Double.NaN;
            LayoutUpdated += LayoutUpdatedEventHandler;
            ThumbnailListBox.SelectionChanged += SelectionChangedEventHandler;
            ThumbnailListBox.ItemsSource = thumbnailItemDict;
            this.IsVisibleChanged += VisibilityChangedEventHandler;
            thumbnailItemDict = new NotifyingCollectionAsDictWrapper<int, AThumbnailItem>();
            ThumbnailListBox.ItemsSource = thumbnailItemDict;
        }



        private void VisibilityChangedEventHandler(object sender, DependencyPropertyChangedEventArgs args)
        {
            // TODO: When thumbnails are turned on it shows page 1-x in the thumbnail view and not
            // the page we are actually on -> needs to be fixed.
            AThumbnailItem item;
            bool retrieved = thumbnailItemDict.Dict.TryGetValue(SelectedIndex, out item);
            ThumbnailItem tItem = item as ThumbnailItem;
            if (tItem != null)
                tItem.ScrollThisElementIntoView();
        }

        public void SetController(IPdfViewerController controller)
        {
            this.controller = controller;
            controller.ThumbnailHeight = thumbnailSize;
            controller.ThumbnailWidth = thumbnailSize;
        }

        public void RegisterHandlers()
        {
            controller.ThumbnailsChanged += ThumbnailLoadedEventHandler;
            controller.OpenCompleted += OnOpenCompletedEventHandler;
            controller.CloseCompleted += CloseCompletedEventHandler;
            controller.PageOrderChanged += PageOrderChangedEventHandler;
            controller.VisiblePageRangeChanged += OnVisiblePageRangeChangedEventHandler;
            controller.RotationChanged += RotationChangedEventHandler;
            OnOpenCompletedEventHandler(null);
        }

        public void UnregisterHandlers()
        {
            CloseCompletedEventHandler(null);
            controller.ThumbnailsChanged -= ThumbnailLoadedEventHandler;
            controller.OpenCompleted -= OnOpenCompletedEventHandler;
            controller.CloseCompleted -= CloseCompletedEventHandler;
            controller.PageOrderChanged -= PageOrderChangedEventHandler;
            controller.VisiblePageRangeChanged -= OnVisiblePageRangeChangedEventHandler;
            controller.RotationChanged -= RotationChangedEventHandler;
        }



        public ItemCollection GetThumbnails()
        {
            return ThumbnailListBox.Items;
        }

        private void PageOrderChangedEventHandler(IList<int> pageOrder)
        {
            CloseCompletedEventHandler(null);
            OnOpenCompletedEventHandler(null);
        }

        private void CloseCompletedEventHandler(PdfViewerException e)
        {
            thumbnailItemDict.Clear();
            ThumbnailItemDictChanged();
            InvalidateVisual();
        }

        private void LayoutUpdatedEventHandler(object sender, EventArgs e)
        {
            ThumbnailListBox.Width = this.ActualWidth;
            ThumbnailListBox.Height = this.ActualHeight;
        }

        private void SelectionChangedEventHandler(object sender, SelectionChangedEventArgs args)
        {
            FrameworkElement element = (FrameworkElement)ThumbnailListBox.SelectedItem;
            if (element != null)
            {
                ThumbnailListBox.ScrollIntoView(element);
                //element.BringIntoView(); // this one only works sometimes... because of reasons... 
            }
        }

        private void ScrollElementIntoView(ThumbnailItem item)
        {
            ThumbnailListBox.ScrollIntoView(item);
            this.Focus();
        }

        private void ThumbnailItemDictChanged()
        {
            ThumbnailListBox.Items.Refresh();
        }
        private static T FindVisualChild<T>(DependencyObject obj) where T : DependencyObject
        {
            for (int i = 0; i < VisualTreeHelper.GetChildrenCount(obj); i++)
            {
                DependencyObject child = VisualTreeHelper.GetChild(obj, i);
                if (child != null && child is T)
                    return (T)child;
                else
                {
                    T childOfChild = FindVisualChild<T>(child);
                    if (childOfChild != null)
                        return childOfChild;
                }
            }
            return null;
        }
        private void OnOpenCompletedEventHandler(PdfViewerException e)
        {
            if (e != null)
                return;

            thumbnailItemDict.Clear();
            int NoOfThumbnails = Math.Min(controller.PageCount, 5);
            for (int i = 0; i < NoOfThumbnails; i++)
            {
                ThumbnailItem item = new ThumbnailItem(i + 1, NoOfThumbnails, controller, SetSelection, ScrollElementIntoView);
                item.Update();
                thumbnailItemDict.Add(i + 1, item);
            }
        }

        private void OnVisiblePageRangeChangedEventHandler(int firstPage, int lastPage)
        {
            SelectedIndex = firstPage;
        }
        private int _selectedIndex = 0;
        protected int SelectedIndex
        {
            set
            {
                _selectedIndex = value;
                if (this.IsVisible)
                {
                    if (!thumbnailItemDict.Dict.ContainsKey(_selectedIndex))
                    {
                        return;
                    }
                    ThumbnailListBox.SelectedItem = thumbnailItemDict.Dict[_selectedIndex];
                }
            }
            get
            {
                return _selectedIndex;
            }
        }

        private void ThumbnailLoadedEventHandler(object sender, PdfTools.PdfViewerCSharpAPI.Model.PdfViewerController.ThumbnailsChangedArgs args)
        {
            if (args.ex != null)
                return;
            foreach (ThumbnailItem item in thumbnailItemDict)
            {
                item.OnImageLoaded(args.bitmap, args.pdfPage);
                ThumbnailListBox.InvalidateVisual();
            }

        }

        private void RotationChangedEventHandler(int rotation)
        {
            foreach (AThumbnailItem item in thumbnailItemDict.Dict.Values)
            {
                item.RotationChanged(rotation);
            }
        }

        private void SetSelection(ThumbnailItem item)
        {
            if (controller.IsOpen)
                controller.SetPageNo(item.PageNo);
        }

        private bool _handlersRegistered = false;
        public bool handlersRegistered
        {
            get
            {
                return _handlersRegistered;
            }

            set
            {
                _handlersRegistered = value;
            }
        }

        private IPdfViewerController controller;
        private static int thumbnailSize = 100;
        private NotifyingCollectionAsDictWrapper<int, AThumbnailItem> thumbnailItemDict = new NotifyingCollectionAsDictWrapper<int, AThumbnailItem>();

        public abstract class AThumbnailItem : Grid
        {

            protected Action<ThumbnailItem> setSelection;
            protected Action<ThumbnailItem> scrollIntoView;
            protected IPdfViewerController controller;
            protected int rotation = 0;
            public abstract void RotationChanged(int newRotation);
        }


        public class ThumbnailItem : AThumbnailItem
        {
            public ThumbnailItem(int ID, int NoOfThumbnails, IPdfViewerController controller, Action<ThumbnailItem> setSelection, Action<ThumbnailItem> scrollIntoView)
            {
                //set size to be automatically determined by children
                this.Width = double.NaN;
                this.Height = double.NaN;
                this.MinWidth = thumbnailSize;
                this.MinHeight = thumbnailSize;

                this.controller = controller;
                this.setSelection = setSelection;
                this.scrollIntoView = scrollIntoView;
                controller.VisiblePageRangeChanged += OnVisiblePageRangeChanged;

                this._ID = ID;
                this._noOfThumbnails = NoOfThumbnails;
                this._fileName = controller.FileName;

                //row/column definitions
                ColumnDefinition c = new ColumnDefinition();
                c.Width = new GridLength(thumbnailSize, GridUnitType.Auto);
                this.ColumnDefinitions.Add(c);
                RowDefinition r = new RowDefinition();
                r.Height = new GridLength(thumbnailSize, GridUnitType.Auto);
                this.RowDefinitions.Add(r);
                RowDefinition r2 = new RowDefinition();
                r2.Height = new GridLength(thumbnailSize / 5, GridUnitType.Auto);
                this.RowDefinitions.Add(r2);

                //add elements
                this.text = new TextBlock();

                this.text.HorizontalAlignment = HorizontalAlignment.Center;
                this.text.VerticalAlignment = VerticalAlignment.Center;
                Grid.SetColumn(this.text, 0);
                Grid.SetRow(this.text, 1);
                this.Children.Add(this.text);

                this.image = new Image();
                border = new Border();

                border.BorderBrush = Brushes.DarkGray;
                border.BorderThickness = new Thickness(1);
                border.Margin = new Thickness(0.0);
                image.Margin = new Thickness(0.0);

                Grid.SetColumn(border, 0);
                Grid.SetRow(border, 0);
                this.Children.Add(border);

                //this._pageNo = pageNo;

                this.HorizontalAlignment = HorizontalAlignment.Center;
                this.VerticalAlignment = VerticalAlignment.Center;
                this.Margin = new Thickness(5.0);
                this.MouseDown += MouseDownEventHandler;

                this.image.Source = emptyWriteableBitmap;


                image.VerticalAlignment = VerticalAlignment.Top;
                image.HorizontalAlignment = HorizontalAlignment.Left;
                image.Stretch = Stretch.Uniform;




                //border.Child = r;
                border.Child = image;
                border.Width = Double.NaN;
                border.Height = Double.NaN;
                //this.UpdateLayout();
                InvalidateVisual();
            }

            public void Update()
            {
                if (controller.IsOpen && _fileName == controller.FileName)
                {
                    /*
                     * Maybe overly complicated way to have a sliding 'window'. Normally I want the thumbmails showing
                     * pages around the current page shown in the viewer. If page 5 is in the viewport and we have 5 thumbnails then:
                     * Thumbnail 1 shows page 3
                     * Thumbnail 2 shows page 4
                     * Thumbnail 3 shows page 5 (current page)
                     * Thumbnail 4 shows page 6
                     * Thumbnail 5 shows page 7
                     * 
                     * To determine the current page in the viewport we use <controller.LastPageOnViewport + controller.FirstPageOnViewport)/2>
                     * And we subtract <noOfThumbnails/2+1> from the current page to offset for the first thumbnail
                     * Now we add _ID so we go from centerPage-Offset to centerPage+Offset
                     * BUT: If we are on page 1 of the document we would have negative pages so thats why we use Math.Max to clamp it to the _ID
                     * The same for the end of the document where we clamp it to controller.PageCount - _noOfThumbnails + _ID
                     * */

                    this._pageNo = Math.Min(Math.Max((controller.LastPageOnViewport + controller.FirstPageOnViewport) / 2 - (_noOfThumbnails / 2 + 1) + _ID, _ID), controller.PageCount - _noOfThumbnails + _ID);
                    request = controller.LoadThumbnail(this._pageNo, true);

                    // Thumbnail has already been loaded before
                    // We get it from the cache
                    if (request == null && controller.PageCount >= _noOfThumbnails)
                    {
                        bitmap = controller.GetThumbnail(this._pageNo);
                        OnImageLoaded(bitmap, this._pageNo);
                    }
                }
            }

            protected void OnVisiblePageRangeChanged(int firstPage, int lastPage)
            {
                if (controller.IsOpen)
                {
                    Update();
                }
                else if (request != null)
                {
                    Logger.LogInfo("Canceling loading Thumbnail " + _pageNo);
                    controller.CancelRequest(request);
                    request = null;
                }
            }

            private void MouseDownEventHandler(object sender, MouseEventArgs args)
            {
                setSelection(this);
            }

            public override void RotationChanged(int newRotation)
            {
                this.rotation = newRotation;
                if (imageLoaded)
                {
                    RotateTransform rotateTransform = new RotateTransform(rotation, image.Width / 2, image.Height / 2);
                    image.LayoutTransform = rotateTransform;
                    InvalidateVisual();
                }
            }

            public void OnImageLoaded(WriteableBitmap bitmap, int pdfPage)
            {
                if (!(pdfPage == _pageNo))
                    return;
                this.image.Width = bitmap.PixelWidth;
                this.image.Height = bitmap.PixelHeight;
                this.image.Source = bitmap;
                this.text.Text = controller.InversePageOrder[pdfPage - 1].ToString();
                RotateTransform rotateTransform = new RotateTransform(this.rotation, image.Width / 2, image.Height / 2);
                image.LayoutTransform = rotateTransform;
                imageLoaded = true;
                InvalidateVisual();
            }

            public bool IsImageLoaded()
            {
                return imageLoaded;
            }

            public int PageNo
            {
                get
                {
                    return _pageNo;
                }
            }

            public void ScrollThisElementIntoView()
            {
                scrollIntoView(this);
            }

            public override String ToString()
            {
                return String.Format("Page {0}", _pageNo);
            }

            private int _ID;
            private int _noOfThumbnails;
            private int _pageNo;
            private string _fileName;
            private TextBlock text;
            private Image image;

            private Border border;
            private bool imageLoaded = false;

            private APdfRequest<ThumbnailCacheArgs, WriteableBitmap> request = null;
            private WriteableBitmap bitmap = null;


            private static WriteableBitmap emptyWriteableBitmap = new WriteableBitmap(thumbnailSize, thumbnailSize, 96.0, 96.0, PdfViewerController.pixelFormat, null);

        }
        /*
         * Looks like a INotifyCollectionChanged compatible ICollection<Value> (to be usable as ListBox.ItemSource) from outside,
         * but is a wrapped dictionary to keep dictionary entries sorted and dynamically load them.
         * Dictionary exposed as property 'Dict'.
         * ICollection functionality is readonly, modifyer methods will cause NotImplementedExceptions.
         */
        private class NotifyingCollectionAsDictWrapper<Key, Value> : ICollection<Value>, INotifyCollectionChanged
        {
            public NotifyingCollectionAsDictWrapper()
            {
                dict = new SortedList<Key, Value>();
                //_values = new ObservableCollection<Value>(base.Values);
            }

            private SortedList<Key, Value> dict;
            public event NotifyCollectionChangedEventHandler CollectionChanged;

            public SortedList<Key, Value> Dict
            {
                get
                {
                    return dict;
                }
            }

            public bool ContainsKey(Key key)
            {
                lock (dict)
                {
                    return dict.ContainsKey(key);
                }
            }

            public Value this[Key key]
            {
                get
                {
                    lock (dict)
                    {
                        return dict[key];
                    }
                }
            }

            public void Add(Key key, Value value)
            {
                int index;
                lock (dict)
                {
                    dict.Add(key, value);
                    index = dict.IndexOfKey(key);
                }
                if (CollectionChanged != null)
                    CollectionChanged(this, new NotifyCollectionChangedEventArgs(System.Collections.Specialized.NotifyCollectionChangedAction.Add, value, index));
                Logger.LogInfo("Add (" + key.ToString() + ", " + value.ToString() + ")");
            }

            public bool Remove(Key key)
            {

                lock (key)
                {
                    Value value = dict[key];
                    Logger.LogInfo("Remove (" + key.ToString() + ", " + value.ToString() + ")");

                    int index = dict.IndexOfKey(key);
                    if (dict.Remove(key))
                    {
                        if (CollectionChanged != null)
                            CollectionChanged(this, new NotifyCollectionChangedEventArgs(System.Collections.Specialized.NotifyCollectionChangedAction.Remove, value, index));
                        return true;
                    }
                }
                return false;
            }

            public void Clear()
            {
                lock (dict)
                {
                    dict.Clear();
                }
                Logger.LogInfo("Clear");
                if (CollectionChanged != null)
                    CollectionChanged(this, new NotifyCollectionChangedEventArgs(System.Collections.Specialized.NotifyCollectionChangedAction.Reset));
            }
            public IEnumerator<Value> GetEnumerator()
            {
                return dict.Values.GetEnumerator();
            }

            System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
            {
                return dict.Values.GetEnumerator();
            }

            public override string ToString()
            {
                StringBuilder s = new StringBuilder("dict");
                lock (dict)
                {
                    foreach (KeyValuePair<Key, Value> entry in dict)
                    {
                        s.AppendFormat("{0}={1}; ", entry.Key.ToString(), entry.Value.ToString());
                    }
                }
                return s.ToString();
            }

            #region forwarding
            //ICollection properties
            public int Count
            {
                get
                {
                    return dict.Count;
                }
            }
            public bool IsReadOnly
            {
                get
                {
                    return true;
                }
            }

            //ICollection methods
            public void Add(Value value)
            {
                throw new NotImplementedException("The thumbnailItemDict is not meant to be modified directly. use thumbnailItemDict.Dict instead");
            }
            public bool Contains(Value value)
            {
                return dict.ContainsValue(value);
            }
            public void CopyTo(Value[] array, int arrayIndex)
            {
                throw new NotImplementedException("The thumbnailItemDict is not meant to be modified directly. use thumbnailItemDict.Dict instead");
            }
            public bool Remove(Value value)
            {
                throw new NotImplementedException("The thumbnailItemDict is not meant to be modified directly. use thumbnailItemDict.Dict instead");
            }

            #endregion forwarding
        }
    }
}
