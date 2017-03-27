package com.pdf_tools.pdfviewer.SwingAPI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Array;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;

import com.pdf_tools.pdfviewer.Model.IPdfViewerController;
import com.pdf_tools.pdfviewer.Model.PdfDestination;
import com.pdf_tools.pdfviewer.Model.PdfOutlineItem;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.*;

public class PdfOutlineView extends JComponent
        implements IOnOpenCompletedListener, IOnOutlinesLoadedListener, TreeExpansionListener, IOnCloseCompletedListener
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PdfOutlineView()
    {
        setLayout(new BorderLayout());
        root = new OutlineNode(controller);
        tree = new JTree(root);
        tree.setRootVisible(false);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeExpansionListener(this);
        tree.setShowsRootHandles(true);

        JScrollPane pane = new JScrollPane(tree);
        add(pane);
        this.setMinimumSize(new Dimension(200, 1));
    }

    public void setController(IPdfViewerController controller)
    {
        this.controller = controller;
        controller.registerOnOpenCompleted(this);
        controller.registerOnCloseCompleted(this);
        controller.registerOnOutlinesLoaded(this);
    }

    @Override
    public void onOutlinesLoaded(int parentId, PdfOutlineItem[] items, PdfViewerException ex)
    {
        if (ex != null)
        {
            return;
        }
        if (items == null)
            return;

        TreePath tp = new TreePath(root.getPath());
        if (parentId == 0)
        {
            while (root.getChildCount() > 0)
            {
                OutlineNode node = (OutlineNode) root.getFirstChild();
                ((DefaultTreeModel) tree.getModel()).removeNodeFromParent(node);
            }

            recursivelyAddOutlines(root, items, 0, tp);
            tree.expandPath(tp);
        } else
        {
            recursivelySearchForParentToAddOutlines(root, items, parentId, tp);
        }

        // expand the tree at root or at the newly loaded node after they have
        // been
        // loaded. otherwise two clicks on a tree node is needed to expand if
        // the
        // children haven't been loaded yet.
        tree.expandPath(find(root, parentId));
    }

    /**
     * Find path to node
     * 
     * @param root:
     *            root node of the tree
     * @param parentID:
     *            id of the node we need the path of
     * @return TreePath to the node with id parentID. Default return path to
     *         root.
     */
    private TreePath find(DefaultMutableTreeNode root, int parentID)
    {
        @SuppressWarnings("unchecked")
        Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
        while (e.hasMoreElements())
        {
            OutlineNode node = (OutlineNode) e.nextElement();
            if (node.id == parentID)
            {
                return new TreePath(node.getPath());
            }
        }
        return new TreePath(root.getPath());
    }

    /**
     * 
     * @param oparent
     * @param itemsToAdd
     * @param toSearch
     * @param path
     * @return true if found parent and added outlines
     */
    private boolean recursivelySearchForParentToAddOutlines(OutlineNode parent, PdfOutlineItem[] itemsToAdd, int toSearch, TreePath path)
    {
        for (OutlineNode child = (OutlineNode) parent.getChildAt(0); child != null; child = (OutlineNode) parent.getChildAfter(child))
        {
            path = path.pathByAddingChild(child);
            if (child.id == toSearch)
            {
                ((DefaultTreeModel) tree.getModel()).removeNodeFromParent((OutlineNode) child.getFirstChild());// remove
                                                                                                               // placeholder
                                                                                                               // stub
                int index = recursivelyAddOutlines(child, itemsToAdd, 0, path);
                return index == itemsToAdd.length;// return true if all items
                                                  // were added
            } else if (!child.isLeaf())
            {
                if (recursivelySearchForParentToAddOutlines(child, itemsToAdd, toSearch, path))
                    return true;
            }
            path = path.getParentPath();
        }
        return false;
    }

    /**
     * 
     * @param parent
     * @param items
     * @param index
     * @param path
     * @return the next index of the items array that has not been added yet
     */
    private int recursivelyAddOutlines(OutlineNode parent, PdfOutlineItem[] items, int index, TreePath path)
    {

        PdfOutlineItem outlineItem;
        while (true)
        {
            outlineItem = items[index];

            OutlineNode node = new OutlineNode(outlineItem, controller);
            tree.addMouseListener(node);
            path = path.pathByAddingChild(node);

            // add node as child
            ((DefaultTreeModel) tree.getModel()).insertNodeInto(node, parent, parent.getChildCount());

            index++;

            if (outlineItem.descendants)
            {
                if (index >= Array.getLength(items) || items[index].level <= outlineItem.level)
                {
                    // the child is not loaded yet. insert a stub
                    node.add(new OutlineNode(controller));
                    if (index >= Array.getLength(items))
                        return index;

                } else
                {
                    index = recursivelyAddOutlines(node, items, index, path);
                    tree.expandPath(path);// expand only works if there are
                                          // children
                    if (index >= Array.getLength(items))
                        return index;
                }
            }

            path = path.getParentPath(); // remove child from path

            if (index >= Array.getLength(items) || items[index].level < outlineItem.level)
            {
                return index;
            }
        }
    }

    @Override
    public void treeExpanded(TreeExpansionEvent event)
    {
        OutlineNode node = (OutlineNode) event.getPath().getLastPathComponent();
        if (node.getChildCount() > 0 && !((OutlineNode) node.getFirstChild()).isLoaded())
            controller.loadOutlines(node.id);
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event)
    {
    }

    @Override
    public void onOpenCompleted(PdfViewerException ex)
    {
        controller.loadOutlines(0);
    }

    @Override
    public void onCloseCompleted(PdfViewerException ex)
    {
        while (root.getChildCount() > 0)
        {
            OutlineNode node = (OutlineNode) root.getFirstChild();
            ((DefaultTreeModel) tree.getModel()).removeNodeFromParent(node);
        }
    }

    private OutlineNode root;
    private JTree tree;
    private IPdfViewerController controller;

    /********************
     * Helper class *
     ********************/

    private class OutlineNode extends DefaultMutableTreeNode implements MouseListener
    {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        // Stub
        public OutlineNode(IPdfViewerController controller)
        {
            this.controller = controller;
        }

        // real node
        public OutlineNode(PdfOutlineItem item, IPdfViewerController controller)
        {
            this(controller);
            SetValues(item);
        }

        public void SetValues(PdfOutlineItem item)
        {
            text = item.title;
            id = item.id;
            destination = item.dest;
        }

        public String toString()
        {
            return text;
        }

        public boolean isLoaded()
        {
            return id != -1;
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
            TreePath tp = tree.getPathForLocation(e.getX(), e.getY());
            if (tp != null && tp.getLastPathComponent() == this && destination != null)
            {
                try
                {
                    controller.setDestination(destination);
                } catch (PdfViewerException e1)
                {
                    // just dont do anything then
                }
            }
        }

        private String text = "OutlineView";
        private IPdfViewerController controller;
        private int id = -1;
        private PdfDestination destination;

        @Override
        public void mousePressed(MouseEvent e)
        {
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
        }
    }
}
