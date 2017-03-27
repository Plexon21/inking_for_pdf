using System;
using System.Collections.Generic;
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
using System.Windows.Shapes;

namespace PdfTools.PdfViewerWPF.CustomControls
{

    using PdfTools.PdfViewerCSharpAPI.Model;
    using PdfTools.PdfViewerCSharpAPI.Utilities;

    /// <summary>
    /// Interaction logic for OutlinesView.xaml
    /// </summary>
    internal partial class OutlinesView : UserControl
    {
        public OutlinesView()
        {
            InitializeComponent();
        }

        public void SetController(IPdfViewerController controller){
            this.controller = controller;
            controller.OutlinesChanged += OnOutlineItemsLoaded;
            controller.OpenCompleted += OnDocumentLoaded;
            controller.CloseCompleted += OnCloseCompleted;
        }

        private void OnDocumentLoaded(PdfViewerException ex)
        {
            if (ex != null)
                return;
            try
            {
            controller.OpenOutlineItem(0);
            }catch(PdfNoFileOpenedException)
            {
                Logger.LogInfo("PdfNoFileOpenedException occured when trying to load outlines");
            }
        }
        private void OnCloseCompleted(PdfViewerException ex){
            OutlineTreeView.Items.Clear();
            InvalidateVisual();
        }

        private void OnSelectedItemChanged(object sender, RoutedPropertyChangedEventArgs<object> args)
        {
            OutlineTreeViewItem item = (OutlineTreeViewItem) args.NewValue;
            if (args.Handled != true && item != null && item.Destination != null && controller.IsOpen)//that can happen when its not loaded yet and its only a stub
            {
                controller.SetDestination(item.Destination);
                args.Handled = true;
            }
        }

        /// <summary>
        /// When controller loaded new outlineItems, add them to the existing treeView or replace it if parentId == 0
        /// </summary>
        /// <param name="parentId"></param>
        /// <param name="outlineItems"></param>
        /// <param name="ex"></param>
        private void OnOutlineItemsLoaded(int parentId, IList<PdfOutlineItem> outlineItems, PdfViewerException ex)
        {
            if (ex != null)
            {
                if (ex is PdfNoFileOpenedException)
                {
                    OnCloseCompleted(null);
                    return;
                }
                throw new NotImplementedException();
            }

            if (parentId == 0)
            {
                if (outlineItems == null)
                    return; 
                OutlineTreeView.Items.Clear();
                IEnumerator<PdfOutlineItem> enumerator = outlineItems.GetEnumerator();
                enumerator.MoveNext();
                recursivelyAddOutlines(OutlineTreeView, enumerator);
                InvalidateVisual();
            }
            else
            {
                IEnumerator<PdfOutlineItem> enumerator = outlineItems.GetEnumerator();
                enumerator.MoveNext();
                bool success = recursivelySearchForParentToAddOutlines(OutlineTreeView, parentId, enumerator);
            }

        }

        /// <summary>
        /// Search for the Item with ID toSearch in the tree recursively and when finding it, call recursivelySearchForParentToAddOutlines
        /// </summary>
        /// <param name="parent"></param>
        /// <param name="toSearch"></param>
        /// <param name="toInsert"></param>
        /// <returns></returns>
        private bool recursivelySearchForParentToAddOutlines(ItemsControl parent, int toSearch, IEnumerator<PdfOutlineItem> toInsert)
        {
            foreach(OutlineTreeViewItem child in parent.Items)
            {
                if (child.Id == toSearch)
                {
                    child.Items.Clear();
                    recursivelyAddOutlines(child, toInsert);
                    return true;
                }
                else if(child.HasItems)
                {
                    if (recursivelySearchForParentToAddOutlines(child, toSearch, toInsert))
                        return true;
                }
            }
            return false;
        }

        /// <summary>
        /// Add the outlineItems from enumerator-List to the parent
        /// </summary>
        /// <param name="parent"></param>
        /// <param name="enumerator"></param>
        /// <returns>Whether the enumerator has reached the end of the collection</returns>
        private bool recursivelyAddOutlines(ItemsControl parent, IEnumerator<PdfOutlineItem> enumerator){
            PdfOutlineItem outlineItem;
            while (true)
            {
                outlineItem = enumerator.Current;
                OutlineTreeViewItem treeViewItem = new OutlineTreeViewItem(outlineItem, controller);
                parent.Items.Add(treeViewItem);

                if(!enumerator.MoveNext())
                    return true;
                if (outlineItem.descendants)
                {
                    if (enumerator.Current.level <= outlineItem.level)
                    {
                        //the child is not loaded yet. insert a stub
                        treeViewItem.Items.Add(new OutlineTreeViewItem(controller));
                    }
                    else
                    {
                        treeViewItem.ExpandSubtree();
                        if (recursivelyAddOutlines(treeViewItem, enumerator))
                            return true;
                    }       
                }

                if (enumerator.Current.level < outlineItem.level)
                {
                    return false;
                }
            }
        }

        private IPdfViewerController controller;


        public class OutlineTreeViewItem : TreeViewItem
        {
            //insert a stub, until we loaded the actual data
            public OutlineTreeViewItem(IPdfViewerController controller)
            {
                this.Header = "Loading";
                this.controller = controller;
                _id = -1;
            }

            public OutlineTreeViewItem(PdfOutlineItem outlineItem, IPdfViewerController controller)
                : this(controller)
            {
                this.Header = outlineItem.title;
                this._destination = outlineItem.dest;
                this._id = outlineItem.id;

                this.Expanded += ExpandEventHandler;
            }



            private void ExpandEventHandler(object sender, RoutedEventArgs args)
            {
                if (this.HasItems && ((OutlineTreeViewItem)this.Items[0]).Id == -1 && controller.IsOpen)
                    controller.OpenOutlineItem(_id);
            }

            private int _id;
            public int Id
            {
                get
                {
                    return _id;
                }
            }

            private PdfDestination _destination;
            public PdfDestination Destination
            {
                get
                {
                    return _destination;
                }
            }

            private IPdfViewerController controller;
        }//end outlineTreeViewItem class

    }
}
