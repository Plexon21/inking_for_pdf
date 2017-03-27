/***************************************************************************
 *
 * File:            PdfViewerComponent.java
 * 
 * Package:         3-Heights(TM) PDF Java Viewer
 *
 * Description:     The PDF Viewer component.
 *
 * @author          Christian Hagedorn, PDF Tools AG   
 * 
 * Copyright:       Copyright (C) 2001 - 2008 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *                  
 ***************************************************************************/

package com.pdf_tools.pdfviewer.SwingAPI;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation.TPdfAnnotationType;
import com.pdf_tools.pdfviewer.Model.PdfDestination;
import com.pdf_tools.pdfviewer.Model.PdfViewerController;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.*;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager.IInternalOnSearchCompletedListener;
import com.pdf_tools.pdfviewer.converter.geom.Rectangle;

/**
 * The PDF viewer component. This is the main interface for all interactions
 * with the Viewer.
 */
public class PdfViewerComponent extends JComponent implements WindowFocusListener, IInternalOnSearchCompletedListener
{

    /**
     * Creates a new instance of the PDF Viewer with optional scroll bars.
     * 
     * @param withScrollBars
     *            If true scroll bars will be added. Otherwise no scroll bars
     *            will be shown.
     */
    public PdfViewerComponent(boolean withScrollBars, JFrame frame)
    {
        // adding this object as window focus listener
        frame.addWindowFocusListener(this);
        // Minimum frame size - this avoids a viewerPane so small that it starts
        // to
        // become very slow in FIT_MODE since it has to render too many pages
        frame.setMinimumSize(new Dimension(400, 400));

        // Viewer objects
        controller = new PdfViewerController(this);
        controller.registerInternalOnSearchCompleted(this);
        
        OnSearchCompletedListenerList = new ArrayList<IOnSearchCompletedListener>();
        setLayout(new GridBagLayout());
        // Swing objects
        viewerPane = new PdfViewerPane(controller);
        viewerPaneWithScrollbars = new JPanel();
        viewerPaneWithScrollbars.setMinimumSize(new Dimension(800, 600));
        verticalScrollBar = new PdfViewerScrollBarVertical(controller);
        horizontalScrollBar = new PdfViewerScrollBarHorizontal(controller);
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        outlineView = new PdfOutlineView();
        thumbnailView = new PdfThumbnailView();
        annotationPane = new PdfAnnotationView(controller);
        viewSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, viewerPaneWithScrollbars, annotationPane);
        viewSplitPane.setResizeWeight(1.0);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, viewSplitPane);
        outlineView.setController(controller);
        thumbnailView.setController(controller);
        c2 = new GridBagConstraints();
        c2.fill = GridBagConstraints.BOTH;
        c2.weightx = 1.0;
        c2.weighty = 1.0;
        add(splitPane, c2);
        tabbedPane.add("Thumbnails", thumbnailView);
        tabbedPane.add("Outlines", outlineView);
        viewerPaneWithScrollbars.setLayout(new GridBagLayout());

        internalFrame = new JInternalFrame("Annotation", true, true, true);
        internalFrame.setSize(200, 200);
        internalFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        internalFrame.setVisible(false);

        /*
         * this.setTransferHandler(new TransferHandler(){
         * 
         * public boolean canImport(TransferHandler.TransferSupport info){
         * if(info.isDataFlavorSupported(df)) }
         * 
         * });
         */

        this.setDropTarget(new DropTarget()
        {
            private static final long serialVersionUID = -8291395466802961851L;

            public synchronized void dragOver(DropTargetDragEvent evt)
            {
                evt.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
                Object transferData;
                try
                {
                    transferData = evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                } catch (UnsupportedFlavorException e)
                {
                    evt.rejectDrag();
                    return;
                } catch (IOException e)
                {
                    evt.rejectDrag();
                    return;
                }
                if (transferData instanceof List)
                {
                    for (Object file : ((List<Object>) transferData))
                    {
                        if (file instanceof File)
                        {
                            // String fileName = ((File)
                            // file).getName().toLowerCase();
                            String extension = getExtension((File) file);
                            if (extension.equals("pdf") || extension.equals("PDF"))
                            {
                                return;
                            }
                        }
                    }
                    // we have traversed all files and could not find a valid
                    // file
                }
                evt.rejectDrag();
            }

            public synchronized void drop(DropTargetDropEvent evt)
            {
                evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                Object transferData;
                try
                {
                    transferData = evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                } catch (UnsupportedFlavorException e)
                {
                    return;
                } catch (IOException e)
                {
                    return;
                }
                if (transferData instanceof List)
                {
                    for (Object file : ((List<?>) transferData))
                    {
                        if (file instanceof File)
                        {
                            // String fileName = ((File)
                            // file).getName().toLowerCase();
                            String extension = getExtension((File) file);
                            if (extension.equals("pdf") || extension.equals("PDF"))
                            {
                                try
                                {
                                    // close the currently opened document
                                    // before opening the new one
                                    controller.close();
                                    controller.open(((File) file).getPath(), null);
                                    return;
                                } catch (PdfViewerException e)
                                {
                                    evt.rejectDrop();
                                    return;
                                }
                            }
                        }
                    }
                    // we have traversed all files and could not find a valid
                    // file
                }
                evt.rejectDrop();
            }

        });

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        viewerPaneWithScrollbars.add(viewerPane, c);
        if (withScrollBars)
        {
            c.weightx = 0;
            c.weighty = 0;
            c.gridx = 1;
            c.fill = GridBagConstraints.VERTICAL;
            viewerPaneWithScrollbars.add(verticalScrollBar, c);
            c.gridx = 0;
            c.gridy = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            viewerPaneWithScrollbars.add(horizontalScrollBar, c);
        }
        enableScrollBars(withScrollBars);
    }

    /**
     * repaint the UI
     */
    public void update()
    {
        horizontalScrollBar.update();
        verticalScrollBar.update();
        viewerPane.repaint();
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        // controller.draw((Graphics2D) g);
    }

    private static String getExtension(File f)
    {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1)
        {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /**
     * Sets the mouse mode to the specified mode. If the current document is not
     * open then the method returns without doing anything.
     * 
     * @param mouseMode
     *            The new mouse mode
     * @throws IllegalArgumentException
     *             If the argument is null
     */
    public void setMouseMode(PdfMouseMode mouseMode)
    {
        viewerPane.setMouseMode(mouseMode);
    }

    /**
     * Enables or Disables the scroll bars depending on the parameter flag.
     * 
     * @param enable
     *            Flag indicating whether to enable or disable the scroll bars.
     */
    public void enableScrollBars(boolean enable)
    {
        horizontalScrollBar.setEnabled(enable);
        verticalScrollBar.setEnabled(enable);
    }

    /**
     * Configure whether the listing of Outlines is visible in the sidebar
     * 
     * @param show
     *            true if outlines should be displayed
     */
    public void showOutlines(boolean show)
    {
        if (show)
        {
            if (tabbedPane.indexOfTabComponent(outlineView) == -1)
            {
                tabbedPane.add("Outlines", outlineView);
                ShowSideBar(true);
            }
        } else
        {
            Component selected = tabbedPane.getSelectedComponent();
            tabbedPane.remove(outlineView);
            if (selected != outlineView)
                tabbedPane.setSelectedComponent(selected);// because indexes
                                                          // might have
                                                          // changed by
                                                          // removing
            else if (tabbedPane.getComponents().length > 0)
                tabbedPane.setSelectedIndex(0);
            else
                ShowSideBar(false);
        }
        outlineView.setEnabled(show);
        repaint();
    }

    /**
     * Configure whether the listing of Thumbnails is visible in the sidebar
     * 
     * @param show
     *            true if Thumbnails should be displayed
     */
    public void showThumbnails(boolean show)
    {
        if (show)
        {
            if (tabbedPane.indexOfTabComponent(thumbnailView) == -1)
            {
                tabbedPane.add("Thumbnail", thumbnailView);
                ShowSideBar(true);
            }

        } else
        {
            Component selected = tabbedPane.getSelectedComponent();
            tabbedPane.remove(thumbnailView);
            if (selected != thumbnailView)
                tabbedPane.setSelectedComponent(selected);// because indexes
                                                          // might have
                                                          // changed by
                                                          // removing
            else if (tabbedPane.getComponents().length > 0)
                tabbedPane.setSelectedIndex(0);
            else
                ShowSideBar(false);
        }
        thumbnailView.setEnabled(show);

    }

    private void ShowSideBar(boolean show)
    {
        List<Component> components = Arrays.asList(getComponents());
        if (show)
        {
            if (components.contains(viewSplitPane))
                remove(viewSplitPane);
            if (!components.contains(splitPane))
            {
                splitPane.setRightComponent(viewSplitPane);
                add(splitPane, c2);
                thumbnailView.validate();
                outlineView.validate();
                splitPane.validate();
            }
        } else
        {
            if (components.contains(splitPane))
                remove(splitPane);
            if (!components.contains(viewerPaneWithScrollbars))
                add(viewSplitPane, c2);
        }
        splitPane.resetToPreferredSizes();
        this.validate();
    }

    public void showAnnotations(boolean show)
    {
        List<Component> components = Arrays.asList(viewSplitPane.getComponents());
        if (show)
        {
            if (components.contains(annotationPane))
                return;
            else
                viewSplitPane.add(annotationPane);
        } else
        {
            if (components.contains(annotationPane))
            {
                viewSplitPane.remove(annotationPane);
            }
        }

    }

    /********************************
     * Forwarded controller methods *
     ********************************/

    /**
     * Open the file with the associated password located at the absolute path
     * specified by filename. If the file does not contain a password an empty
     * string must be passed. This method is executed asynchronously. Upon
     * completion the event onOpenCompleted() is called.
     * 
     * @param filename
     *            The absolute path to the file
     * @param password
     *            The password needed to open the file (if no password required
     *            pass "" as password)
     * @throws PdfViewerException
     *             If opening has encountered a problem.
     */
    public void open(String filename, char[] password) throws PdfViewerException
    {
        controller.open(filename, password);
    }

    public void saveFile(String path) throws PdfViewerException
    {
        controller.saveAs(path);
    }

    /**
     * Close the currently open file. This method is executed asynchronously.
     * Upon completion the event onCloseCompleted() is called.
     */
    public void close()
    {
        controller.close();
    }

    /**
     * Returns the number of pages of the currently open document. If the
     * current document is not open then 0 is returned.
     * 
     * @return The number of pages
     * @throws PdfViewerException
     *             If the operation failed.
     */
    public int getPageCount() throws PdfViewerException
    {
        return controller.getPageCount();
    }

    /**
     * Returns the first page that is visible on the viewport (the page with the
     * smallest pagenumber)
     * 
     * @return The page number
     */
    public int getPageNo()
    {
        return controller.getPageNo();
    }

    /**
     * Returns the last page that is visible on the viewport (the page with the
     * highest pagenumber)
     * 
     * @return last visible page number
     */
    public int getLastPageNo()
    {
        return controller.getLastPageNo();
    }

    public String getFileName()
    {
        return controller.getFileName();
    }

    /**
     * Sets the page number to the specified value. A valid page number is
     * between 1 and the number of pages. If the current document is not open
     * then the method returns without doing anything.
     * 
     * @param pageNo
     *            The new page number
     * @throws IllegalArgumentException
     *             If the page number is not valid
     * @throws PdfViewerException
     *             If the operation failed.
     */
    public void setPageNo(int pageNo) throws IllegalArgumentException, PdfViewerException
    {
        controller.setPageNo(pageNo);
    }

    /**
     * Moves the viewport to view a given destination
     * 
     * @param destination
     *            The Destination to look at
     * @throws PdfViewerException
     *             If the operation failed.
     */
    public void setDestination(PdfDestination destination) throws PdfViewerException
    {
        controller.setDestination(destination);
    }

    /**
     * Return the zoom factor which has been set by setZoomFactor. After a
     * completed open call the zoom factor is 1.0. If the current document is
     * not open then 0 is returned.
     * 
     * @return The zoom factor
     */
    public double getZoom()
    {
        return controller.getZoom();
    }

    /**
     * Sets the zoom factor to the specified value. If the current document is
     * not open then the method returns without doing anything.
     * 
     * @param zoomFactor
     *            The new zoom factor
     * @throws IllegalArgumentException
     *             If the zoom factor is not valid
     * @throws PdfViewerException
     *             If the operation failed.
     */
    public void setZoom(double zoomFactor) throws IllegalArgumentException, PdfViewerException, PdfViewerException
    {
        controller.setZoom(zoomFactor);
    }

    /**
     * Returns the zoom mode which has been set by setZoomFactor. If the zoom
     * mode was not set yet, the zoom factor was changed manually, or the
     * current document is not open, then IPdfViewer.ZoomMode.ACTUAL_SIZE is
     * returned. After a completed open call the zoom mode is
     * IPdfViewer.ZoomMode.ACTUAL_SIZE.
     * 
     * @return The zoom mode
     */
    public TFitMode getZoomMode()
    {
        return controller.getZoomMode();
    }

    /**
     * Choose how the viewport fits the document to the window
     * 
     * @param fitMode
     *            The new fit mode
     * @throws IllegalArgumentException
     *             If the argument is null
     * @throws PdfViewerException
     *             If the operation failed.
     */
    public void setFitMode(TFitMode fitMode) throws IllegalArgumentException, PdfViewerException
    {
        controller.setFitMode(fitMode);
    }

    /**
     * Returns the rotation which has been set by setRotation. After a completed
     * open call the rotation is 0. If the current document is not open then 0
     * is returned.
     * 
     * @return The rotation
     */
    public int getRotation()
    {
        return controller.getRotation();
    }

    /**
     * Sets the rotation to the specified value. If the current document is not
     * open then the method returns without doing anything.
     * 
     * @param rotation
     *            The new rotation
     * @throws IllegalArgumentException
     *             If the rotation is not valid
     * @throws PdfViewerException
     *             If the operation failed.
     */
    public void setRotation(int rotation) throws IllegalArgumentException, PdfViewerException
    {
        controller.setRotation(rotation);
    }

    /**
     * Returns the layout mode which has been set by setLayoutMode. After a
     * completed open call the layout mode is IPdfViewer.PdfLayoutMode.DOCUMENT.
     * If the current document is not open, then
     * IPdfViewer.PdfLayoutMode.DOCUMENT is returned.
     * 
     * @return The layout mode
     */
    public TPageLayoutMode getPageLayoutMode()
    {
        return controller.getPageLayoutMode();
    }

    /**
     * Sets the layout mode to the specified mode.
     * 
     * @param layoutMode
     *            The new layout mode
     * @throws IllegalArgumentException
     *             If the argument is null
     * @throws PdfViewerException
     *             If the operation failed.
     */
    public void setPageLayoutMode(TPageLayoutMode layoutMode) throws IllegalArgumentException, PdfViewerException
    {
        controller.setPageLayoutMode(layoutMode);
    }

    /**
     * Returns the border which has been set by setBorderSize.
     * 
     * @return The borderSize
     */
    public double getBorderSize()
    {
        return controller.getBorderSize();
    }

    /**
     * Sets the border size to the specified value. The border size should be
     * set before the document is opened. Otherwise the layout has to be created
     * again.
     * 
     * @param borderSize
     *            The new border size
     * @throws IllegalArgumentException
     *             If the border size is not valid.
     * @throws PdfViewerException
     *             If the operation failed.
     */
    public void setBorderSize(double borderSize) throws IllegalArgumentException, PdfViewerException
    {
        controller.setBorderSize(borderSize);
    }

    /**
     * Create an annotation on the page
     * @param type TPdfAnnotation type
     * @param page on which page the annotation should be
     * @param rect rectangle/list of vertices/quadpoints of the annotation
     */
    public void createAnnotation(TPdfAnnotationType type, int page, Double[] rect)
    {
        try
        {
            controller.createAnnotation(type, page, rect);
        } catch (PdfViewerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * Returns the mouse mode which has been set by setMouseMode. If no mode was
     * set IPdfViewer.PdfMouseMode.ZOOM is returned.
     * 
     * @return The mouse mode
     */
    public PdfMouseMode getMouseMode()
    {
        return viewerPane.getMouseMode();
    }

    /**
     * Searches the document for a given searchstring
     * 
     * @param toSearch
     *            the string to search for
     * @param startPage
     *            the page on which searching is started
     * @param startIndex
     *            the index of the first character on the startPage, from where
     *            searching is started (When repeatedly searching the same term,
     *            insert the index of the last found match here)
     * @throws PdfViewerException
     *             if search failed
     */
    public void search(String toSearch, int startPage, int startIndex) throws PdfViewerException
    {
        controller.search(toSearch, startPage, startIndex);
    }

    /**
     * configures searching algorithm
     * 
     * @param matchCase
     *            true iff search should be case sensitive
     * @param wrap
     *            true iff reaching the end/beginning of the document should
     *            resume search at beginning/end
     * @param previous
     *            true iff searching should be done back to front instead of
     *            front to back of document
     * @param useRegex
     *            true iff the given search string should be interpreted as a
     *            regular expression
     */
    public void configureSearcher(boolean matchCase, boolean wrap, boolean previous, boolean useRegex)
    {
        controller.configureSearcher(matchCase, wrap, previous, useRegex);
    }

    /**
     * Pauses rendering of this control, until resume() is called
     */
    public void suspend()
    {
        controller.suspend();
    }

    /**
     * Ends pausing the renderer (initiated by pause()) and forces an immediate
     * rendering call
     * 
     * @throws PdfViewerException
     *             if an exception gets thrown out of rendering call
     */
    public void resume() throws PdfViewerException
    {
        controller.resume();
    }

    /**
     * Determines whether the viewer will ignore the preferences, that are
     * embedded in the pdf file (pageLayout, openAction etc.)
     * 
     * @param ignore
     *            true if the viewer is to ignore embedded preferences
     */
    public void setIgnoringPreferences(boolean ignore)
    {
        controller.setIgnoringPreferences(ignore);
    }

    /**
     * get whether the viewer ignores embedded preferences
     * 
     * @return true if ignoring preferences
     */
    public boolean getIgnoringPreferences()
    {
        return controller.getIgnoringPreferences();
    }

    /**
     * set a new license key to use
     * 
     * @param key
     *            the license key to use
     * @throws PdfViewerException
     *             if the provided license key is not valid. Check exception
     *             type and message for further information
     */
    public static void setLicenseKey(String key) throws PdfViewerException
    {
        PdfViewerController.setNewLicenseKey(key);
    }

    /**
     * Test whether license is valid
     * 
     * @throws PdfViewerException
     *             is thrown if the license is not valid. Check exception type
     *             and message for further information
     */
    public static void getLicenseIsValid() throws PdfViewerException
    {
        PdfViewerController.getAnyLicenseIsValid();
    }

     /**
     * Get Version of the product
     * 
     * @throws PdfViewerException
     *             is thrown if the license is not valid. Check exception type
     *             and message for further information
     */
    public static String getProductVersion()
    {
        return PdfViewerController.getProductVersion();
    }

    /**
     * Run a operation on the GUI thread (this should be done with all
     * GUI-related operations)
     * 
     * @param r
     *            the operation that is run
     */
    public static void invokeCallback(Runnable r)
    {
        SwingUtilities.invokeLater(r);
    }

    /******************
     * Eventlisteners *
     ******************/
    /**
     * @param listener
     *            A object implementing IOnOpenCompletedListener, whose
     *            onOpenCompleted method will be called by this controller if
     *            opening a file has completed
     */

    public void registerOnOpenCompleted(IOnOpenCompletedListener listener)
    {
        controller.registerOnOpenCompleted(listener);
    }

    /**
     * @param listener
     *            A object implementing IOnCloseCompletedListener , whose
     *            onCloseCompleted method will be called by this controller if
     *            closing a file has completed
     */
    public void registerOnCloseCompleted(IOnCloseCompletedListener listener)
    {
        controller.registerOnCloseCompleted(listener);
    }

    /**
     * @param listener
     *            A object implementing IOnClosingListener , whose onClosing
     *            method will be called by this controller if closing a file has
     *            started
     */
    public void registerOnClosing(IOnClosingListener listener)
    {
        controller.registerOnClosing(listener);
    }

    public void registerOnSaveCompleted(IOnSaveCompletedListener listener)
    {
        controller.registerOnSaveCompleted(listener);
    }

    /**
     * @param listener
     *            A object implementing IOnCurrentPageNoListener, whose
     *            onCurrentPageNo method will be called by this controller if
     *            the topmost page on the viewport has changed
     */
    public void registerOnVisiblePageRangeChanged(IOnVisiblePageRangeChangedListener listener)
    {
        controller.registerOnVisiblePageRangeChanged(listener);
    }

    /**
     * @param listener
     *            A object implementing IOnZoomCompletedListener, whose
     *            onZoomCompleted method will be called by this controller if
     *            zooming has complted
     */
    public void registerOnZoomCompleted(IOnZoomCompletedListener listener)
    {
        controller.registerOnZoomCompleted(listener);
    }

    public void registerOnFitModeChanged(IOnFitModeChangedListener listener)
    {
        controller.registerOnFitModeChanged(listener);
    }

    /**
     * @param listener
     *            A object implementing IOnPageLayoutModeChanged, whose
     *            onPageLayoutModeChanged method will be called by this
     *            controller, if the PageLayoutMode changes
     */
    public void registerOnPageLayoutModeChanged(IOnPageLayoutModeChangedListener listener)
    {
        controller.registerOnPageLayoutModeChanged(listener);
    }

    /**
     * 
     * @param listener
     *            A object implementing IOnOutlinesLoadedListener, whose
     *            onOutlinesLoaded method will be called by this controller, if
     *            new outlines have been loaded
     */
    public void registerOnOutlinesLoaded(IOnOutlinesLoadedListener listener)
    {
        controller.registerOnOutlinesLoaded(listener);
    }

    /**
     * @param listener
     *            A object implementing IOnThumbnailLoadedListener, whose
     *            onThumbnailLoaded method will be called by this controller, if
     *            the thumbnails have been loaded
     */
    public void registerOnThumbnailLoaded(IOnThumbnailLoadedListener listener)
    {
        controller.registerOnThumbnailLoaded(listener);
    }

    /**
     * @param listener
     *            A object implementing IOnTextExtractedListener, whose
     *            onTextExtracted method will be called by this controller, if
     *            text has been extracted
     */
    public void registerOnTextExtracted(IOnTextExtractedListener listener)
    {
        controller.registerOnTextExtracted(listener);
    }

    /**
     * @param listener
     *            A object implementing IOnRotationChangedListener, whose
     *            onRotationChanged method will be called by this controller, if
     *            the Rotation of the document has changed
     */
    public void registerOnRotationChanged(IOnRotationChangedListener listener)
    {
        controller.registerOnRotationChanged(listener);
    }

    /**
     * @param listener
     *            A object implementing IOnSearchCompletedListener, whose
     *            onSearchCompleted method will be called by this controller, if
     *            a search operation returns
     */
    public void registerOnSearchCompleted(IOnSearchCompletedListener listener)
    {
        OnSearchCompletedListenerList.add(listener);
    }
    
    @Override
    public void internalOnSearchCompleted(int page, int index,
            Map<Integer, List<com.pdf_tools.pdfviewer.converter.geom.Rectangle.Double>> rects)
    {
        Map<Integer, List<java.awt.geom.Rectangle2D.Double>> newRects = null;
        
        if (rects != null)
        {
            newRects = new HashMap<Integer, List<java.awt.geom.Rectangle2D.Double>>();
            for (Map.Entry<Integer, List<Rectangle.Double>> entry : rects.entrySet())
            {
                List<java.awt.geom.Rectangle2D.Double> rectList = new ArrayList<java.awt.geom.Rectangle2D.Double>();
                for (Rectangle.Double r : entry.getValue())
                {
                    // convert the shit out of the rectangles
                    rectList.add(new java.awt.geom.Rectangle2D.Double(r.x, r.y, r.width, r.height));
                }
                newRects.put(entry.getKey(), rectList);
            }
        }
        
        for (IOnSearchCompletedListener listener : OnSearchCompletedListenerList)
        {
            listener.onSearchCompleted(page, index, newRects);
        }
    }

    /**
     * @param listener
     *            A object implementing IOnMouseModeChangedListener, whose
     *            onMouseModeChanged method will be called by this controller,
     *            if the active mouse mode changed
     */
    public void registerOnMouseModeChanged(IOnMouseModeChangedListener listener)
    {
        viewerPane.registerOnMouseModeChanged(listener);
    }

    /*****************
     * members *
     *****************/
    private static final long serialVersionUID = -3993187478568332683L;

    private PdfViewerController controller;
    private JComponent viewerPaneWithScrollbars;
    private PdfViewerPane viewerPane;
    private PdfViewerScrollBarVertical verticalScrollBar;
    private PdfViewerScrollBarHorizontal horizontalScrollBar;
    private JTabbedPane tabbedPane;
    private PdfOutlineView outlineView;
    private PdfThumbnailView thumbnailView;
    private PdfAnnotationView annotationPane;
    private JSplitPane splitPane, viewSplitPane;
    private JInternalFrame internalFrame;
    private ArrayList<IOnSearchCompletedListener> OnSearchCompletedListenerList;
    GridBagConstraints c2;
    @Override
    public void windowGainedFocus(WindowEvent e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowLostFocus(WindowEvent e)
    {
        viewerPane.onLostFocus();
    }

}
