/***************************************************************************
 *
 * File:            PdfViewerController.java
 * 
 * Package:         3-Heights(TM) PDF Java Viewer
 *
 * Description:     The PDF Viewer Component controller.
 *
 * @author          Christian Hagedorn, PDF Tools AG   
 * 
 * Copyright:       Copyright (C) 2001 - 2008 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *                  
 ***************************************************************************/

package com.pdf_tools.pdfviewer.Model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation;
import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation.TPdfAnnotationType;
import com.pdf_tools.pdfviewer.Annotations.APdfMarkupAnnotation;
import com.pdf_tools.pdfviewer.Annotations.PdfGenericAnnotation;
import com.pdf_tools.pdfviewer.Annotations.PdfTextAnnotation;
import com.pdf_tools.pdfviewer.DocumentManagement.PdfDocument;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.*;
import com.pdf_tools.pdfviewer.Model.PdfViewerException.NoFileOpenedException;
import com.pdf_tools.pdfviewer.SwingAPI.PdfViewerComponent;
import com.pdf_tools.pdfviewer.converter.geom.Point;
import com.pdf_tools.pdfviewer.converter.geom.Rectangle;

import java.util.prefs.Preferences;

/**
 * The PdfViewerController class handles all interactions with the PDF Viewer.
 * Every method call go through this class and will be delegated to lower layer
 * classes if necessary. This class also contains all informations about the
 * viewport of the currently open document, stored in pixel coordinates.
 * 
 * @author cha
 *
 */
public class PdfViewerController extends NativeObject implements IPdfViewerController, IPdfControllerCallbackManager
{
    /**
     * Creates a new controller instance with the specified interface reference
     * to the GUI classes.
     * 
     * @param window
     *            The interface reference to the GUI classes
     */
    public PdfViewerController(PdfViewerComponent window)
    {
        this.component = window;

        OnOpenCompletedListenerList = new ArrayList<IOnOpenCompletedListener>();
        OnCloseCompletedListenerList = new ArrayList<IOnCloseCompletedListener>();
        OnClosingListenerList = new ArrayList<IOnClosingListener>();
        OnVisiblePageRangeChangedListenerList = new ArrayList<IOnVisiblePageRangeChangedListener>();
        OnZoomCompletedListenerList = new ArrayList<IOnZoomCompletedListener>();
        OnPageLayoutModeChangedListenerList = new ArrayList<IOnPageLayoutModeChangedListener>();
        OnFitModeChangedListenerList = new ArrayList<IOnFitModeChangedListener>();
        OnOutlinesLoadedListenerList = new ArrayList<IOnOutlinesLoadedListener>();
        OnThumbnailLoadedListenerList = new ArrayList<IOnThumbnailLoadedListener>();
        OnTextExtractedListenerList = new ArrayList<IOnTextExtractedListener>();
        OnRotationChangedListenerList = new ArrayList<IOnRotationChangedListener>();
        InternalOnSearchCompletedListenerList = new ArrayList<IInternalOnSearchCompletedListener>();
        OnTextAnnotationActionListenerList = new ArrayList<IOnTextAnnotationActionListener>();
        OnMarkupAnnotationHoverListenerList = new ArrayList<IOnMarkupAnnotationHoverListener>();
        OnMarkAnnotationClickedListenerList = new ArrayList<IAnnotationViewerListener>();
        OnNoAnnotationSelectedListenerList = new ArrayList<IAnnotationViewerListener>();
        OnMarkupAnnotationActionListenerList = new ArrayList<IOnMarkupAnnotationActionListener>();
        OnAnnotationCreatedListenerList = new ArrayList<IOnAnnotationCreatedListener>();
        OnAnnotationUpdatedListenerList = new ArrayList<IOnAnnotationUpdatedListener>();
        OnSaveCompletedListenerList = new ArrayList<IOnSaveCompletedListener>();
        OnAnnotationDeletedListenerList = new ArrayList<IOnAnnotationDeletedListener>();
        OnDrawCompletedListenerList = new ArrayList<IOnDrawCompletedListener>();

        fitMode = TFitMode.ACTUAL_SIZE;
        firstPageOnViewport = 1;
        lastPageOnViewport = 1;
        fileName = "-";
        canvas = new PdfCanvas(this);
        searcher = new PdfSearcher(canvas, this);
        viewport = new Viewport(this);
        String s = userPref.get("PageLayout", TPageLayoutMode.OneColumn.toString());
        try
        {
            changeTPageLayoutMode(TPageLayoutMode.valueOf(s));
        } catch (IllegalArgumentException e)
        {
            DebugLogger.log(e.getMessage());
        } catch (PdfViewerException e)
        {
            exceptionOccurred(e);
        }
    }

    /********************************
     * Public Management Methods *
     ********************************/

    @Override
    public void close()
    {
        canvas.close();
    }

    /**
     * @throws PdfViewerException
     * @see PdfDocument#getPageCount()
     */
    @Override
    public int getPageCount() throws PdfViewerException
    {
        return canvas.getPageCount();
    }

    @Override
    public int getPageNo()
    {
        return firstPageOnViewport;
    }

    @Override
    public String getFileName()
    {
        return fileName;
    }

    public int getLastPageNo()
    {
        return lastPageOnViewport;
    }

    @Override
    public double getZoom()
    {
        return viewport.getZoomFactor();
    }

    @Override
    public TFitMode getZoomMode()
    {
        return fitMode;
    }

    /**
     * @see PdfCanvas#getRotation()
     */
    @Override
    public int getRotation()
    {
        return canvas.getRotation();
    }

    /**
     * @see PdfCanvas#getLayoutMode()
     */
    @Override
    public TPageLayoutMode getPageLayoutMode()
    {
        return canvas.getPageLayoutMode();
    }

    /**
     * @see PdfCanvas#getBorderSize()
     */
    @Override
    public double getBorderSize()
    {
        return canvas.getBorderSize();
    }

    /**
     * Returns the upper left Point.Integer of the viewport in pixels.
     * 
     * @return The upper left Point.Integer of the viewport
     */
    public Point.Integer getViewportOrigin()
    {
        return viewport.rectangle.getLocation();
    }

    public Rectangle.Integer getViewport()
    {
        return viewport.rectangle;
    }

    /**
     * @throws PdfViewerException
     * @see PdfCanvas#getCanvasSize(pageNo)
     */
    public Rectangle.Double getCanvasSize() throws PdfViewerException
    {
        return canvas.getCanvasRect();
    }

    /**
     * @throws PdfViewerException
     * @see PdfCanvas#getPageRect()
     */
    public Rectangle.Double getPageRect(int pageNo) throws PdfViewerException
    {
        return canvas.getPageRect(pageNo);
    }

    /*
     * public PdfTextSearch getTextSearch() { throw new
     * NotImplementedException(); }
     */
    /**
     * Returns the width of the viewport in pixels.
     * 
     * @return The width of the viewport
     */
    public int getWidth()
    {
        return viewport.rectangle.width;
    }

    public void setTextSelectionStartPoint(Point.Integer start) throws PdfViewerException
    {
        textSelectionStartPoint = new Point.Double(PdfUtils.viewportToCanvas(start.x + viewport.rectangle.x, viewport.getZoomFactor()),
                PdfUtils.viewportToCanvas(start.y + viewport.rectangle.y, viewport.getZoomFactor()));
        textSelectionStartPage = getPageContainingPoint(textSelectionStartPoint);
    }

    private Point.Double textSelectionStartPoint = null;
    private int textSelectionStartPage = 0;

    public PdfTextWithinSelectionResult getTextWithinSelection(Point.Integer start, Point.Integer end) throws PdfViewerException
    {
        Point.Double startPoint = textSelectionStartPoint;
        int startPage = textSelectionStartPage;
        if (start != null)
        {
            startPoint = new Point.Double(PdfUtils.viewportToCanvas(start.x + viewport.rectangle.x, viewport.getZoomFactor()),
                    PdfUtils.viewportToCanvas(start.y + viewport.rectangle.y, viewport.getZoomFactor()));
            startPage = getPageContainingPoint(startPoint);
        }
        Point.Double endPoint = new Point.Double(PdfUtils.viewportToCanvas(end.x + viewport.rectangle.x, viewport.getZoomFactor()),
                PdfUtils.viewportToCanvas(end.y + viewport.rectangle.y, viewport.getZoomFactor()));
        int endPage = getPageContainingPoint(endPoint);
        if (startPage == -1 || endPage == -1)
            return null;
        // transform the sourcepoints to be onPage coordinates
        Rectangle.Double startPageRect = canvas.getPageRect(startPage);
        Rectangle.Double endPageRect = canvas.getPageRect(endPage);
        startPoint = PdfUtils.CalcOnPageCoordinates(startPoint, startPageRect, canvas.getRotation());
        endPoint = PdfUtils.CalcOnPageCoordinates(endPoint, endPageRect, canvas.getRotation());
        // transform to OnUnrotatedPage coordinates
        if (endPage < startPage)
        {
            // start and end are swapped
            return canvas.getTextWithinSelection(endPoint, startPoint, endPage, startPage);
        } else
        {
            return canvas.getTextWithinSelection(startPoint, endPoint, startPage, endPage);
        }
    }

    public int getPageContainingPoint(Point.Double point) throws PdfViewerException
    {
        int startPage = 1;
        int endPage = getPageCount();

        if (!canvas.getPageLayoutMode().isScrolling())
        {
            startPage = firstPageOnViewport;
            endPage = lastPageOnViewport;
        }

        while (true)
        {
            int needle = (endPage - startPage) / 2 + startPage;
            Rectangle.Double pageRect = canvas.getPageRect(needle);
            int count = endPage - startPage + 1;

            if (count <= 2)
            {
                if (canvas.getPageRect(startPage).contains(point))
                    return startPage;
                else if (canvas.getPageRect(endPage).contains(point))
                    return endPage;
                else
                    return -1;
            }

            if (point.y < pageRect.y)
                endPage = needle;
            else if (point.y > pageRect.getMaxY())
                startPage = needle;
            else if (point.x < pageRect.x)
                endPage = needle;
            else if (point.x > pageRect.getMaxX())
                startPage = needle;
            else
                return needle;

            if (count == endPage - startPage + 1)
                return -1; // We could not reduce the set of pages anymore. This
                           // implies that Point.Integer is not on any page
        }
    }

    /**
     * Returns the height of the viewport in pixels.
     * 
     * @return The height of the viewport
     */
    public int getHeight()
    {
        return viewport.rectangle.height;
    }

    public void suspend()
    {
        suspended = true;
    }

    public void setIgnoringPreferences(boolean ignore)
    {
        ignoreDocumentPreferences = ignore;
    }

    public boolean getIgnoringPreferences()
    {
        return ignoreDocumentPreferences;
    }

    public List<PdfTextFragment> getTextWithinRegion(Rectangle.Integer markedRect) throws PdfViewerException
    {
        return canvas.GetTextWithinRegion(markedRect, firstPageOnViewport, lastPageOnViewport, viewport.getZoomFactor());
    }

    /**************************
     * Listener Methods *
     **************************/

    @Override
    public void registerOnOpenCompleted(IOnOpenCompletedListener listener)
    {
        OnOpenCompletedListenerList.add(listener);
    }

    @Override
    public void registerOnCloseCompleted(IOnCloseCompletedListener listener)
    {
        OnCloseCompletedListenerList.add(listener);
    }

    @Override
    public void registerOnClosing(IOnClosingListener listener)
    {
        OnClosingListenerList.add(listener);
    }

    @Override
    public void registerOnSaveCompleted(IOnSaveCompletedListener listener)
    {
        OnSaveCompletedListenerList.add(listener);
    }

    @Override
    public void registerOnVisiblePageRangeChanged(IOnVisiblePageRangeChangedListener listener)
    {
        OnVisiblePageRangeChangedListenerList.add(listener);
        listener.onVisiblePageRangeChanged(firstPageOnViewport, lastPageOnViewport);
    }

    @Override
    public void registerOnZoomCompleted(IOnZoomCompletedListener listener)
    {
        OnZoomCompletedListenerList.add(listener);
        listener.onZoomCompleted(viewport.getZoomFactor());
    }

    @Override
    public void registerOnPageLayoutModeChanged(IOnPageLayoutModeChangedListener listener)
    {
        OnPageLayoutModeChangedListenerList.add(listener);
        listener.onPageLayoutModeChanged(getPageLayoutMode());
    }

    public void registerOnFitModeChanged(IOnFitModeChangedListener listener)
    {
        OnFitModeChangedListenerList.add(listener);
        listener.onFitModeChanged(fitMode);
    }

    public void registerOnOutlinesLoaded(IOnOutlinesLoadedListener listener)
    {
        OnOutlinesLoadedListenerList.add(listener);
    }

    public void registerOnThumbnailLoaded(IOnThumbnailLoadedListener listener)
    {
        OnThumbnailLoadedListenerList.add(listener);
    }

    public void registerOnTextExtracted(IOnTextExtractedListener listener)
    {
        OnTextExtractedListenerList.add(listener);
    }

    public void registerOnAnnotationAction(IOnMarkupAnnotationActionListener listener)
    {
        OnMarkupAnnotationActionListenerList.add(listener);
    }

    public void registerOnAnnotationHover(IOnMarkupAnnotationHoverListener listener)
    {
        OnMarkupAnnotationHoverListenerList.add(listener);
    }

    public void registerOnTextAnnotationClicked(IAnnotationViewerListener listener)
    {
        OnMarkAnnotationClickedListenerList.add(listener);
    }

    public void registerOnNoAnnotationSelected(IAnnotationViewerListener listener)
    {
        OnNoAnnotationSelectedListenerList.add(listener);
    }
    
    @Override
    public void registerOnAnnotationCreated(IOnAnnotationCreatedListener listener)
    {
       OnAnnotationCreatedListenerList.add(listener);
    }

    public void registerOnAnnotationDeleted(IOnAnnotationDeletedListener listener)
    {
        OnAnnotationDeletedListenerList.add(listener);
    }

    public void registerOnAnnotationUpdated(IOnAnnotationUpdatedListener listener)
    {
        OnAnnotationUpdatedListenerList.add(listener);
    }
    
    @Override
    public void registerInternalOnSearchCompleted(IInternalOnSearchCompletedListener listener)
    {
        InternalOnSearchCompletedListenerList.add(listener);        
    }

    @Override
    public void registerOnRotationChanged(IOnRotationChangedListener listener)
    {
        OnRotationChangedListenerList.add(listener);
    }

    @Override
    public void registerOnDrawCompleted(IOnDrawCompletedListener listener) 
    {
        OnDrawCompletedListenerList.add(listener);
    }

    private void fireOnCurrentPageNo() throws PdfViewerException
    {
        for (IOnVisiblePageRangeChangedListener i : OnVisiblePageRangeChangedListenerList)
        {
            i.onVisiblePageRangeChanged(firstPageOnViewport, lastPageOnViewport);
        }
    }

    private void fireOnDrawCompleted(BufferedImage bitmap) 
    {
        for (IOnDrawCompletedListener i : OnDrawCompletedListenerList) 
        {
            i.onDrawCompleted(bitmap);
        }
    }

    private void fireOnZoomCompleted()
    {
        for (IOnZoomCompletedListener i : OnZoomCompletedListenerList)
            i.onZoomCompleted(viewport.getZoomFactor());
    }

    private void fireOnPageLayoutModeChanged(TPageLayoutMode newMode)
    {
        for (IOnPageLayoutModeChangedListener i : OnPageLayoutModeChangedListenerList)
            i.onPageLayoutModeChanged(newMode);
    }

    private void fireOnFitModeChanged(TFitMode newMode)
    {
        for (IOnFitModeChangedListener i : OnFitModeChangedListenerList)
            i.onFitModeChanged(newMode);
    }

    private void fireOnOutlinesLoaded(int parentId, PdfOutlineItem items[], PdfViewerException ex)
    {
        for (IOnOutlinesLoadedListener i : OnOutlinesLoadedListenerList)
            i.onOutlinesLoaded(parentId, items, ex);
    }

    private void fireOnThumbnailLoaded(int pageNo, BufferedImage bitmap, PdfViewerException ex)
    {
        for (IOnThumbnailLoadedListener i : OnThumbnailLoadedListenerList)
            i.onThumbnailLoaded(pageNo, bitmap, ex);
    }
    
    private void fireOnAnnotationCreated(int pageNo)
    {
        for (IOnAnnotationCreatedListener listener : OnAnnotationCreatedListenerList)
        {
            listener.onAnnotationCreated(pageNo);
        }
    }
    
    public void fireOnRotationChanged(int newRotation)
    {
        for (IOnRotationChangedListener i : OnRotationChangedListenerList)
            i.onRotationChanged(newRotation);
    }

    public void fireOnTextExtracted(String extractedText)
    {
        for (IOnTextExtractedListener i : OnTextExtractedListenerList)
            i.onTextExtracted(extractedText);
    }

    public void fireOnAnnotationAction(APdfAnnotation annotation)
    {
        for (IOnTextAnnotationActionListener i : OnTextAnnotationActionListenerList)
            i.onTextAnnotationAction((PdfTextAnnotation) annotation);
    }

    public void fireOnMarkupAnnotationAction(APdfMarkupAnnotation annotation)
    {
        for (IOnMarkupAnnotationActionListener i : OnMarkupAnnotationActionListenerList)
            i.onMarkupAnnotationAction(annotation);
    }

    public void fireOnAnnotationHover(APdfMarkupAnnotation annotation)
    {
        for (IOnMarkupAnnotationHoverListener i : OnMarkupAnnotationHoverListenerList)
        {
            i.onTextAnnotationHover(annotation);
        }
    }

    public void fireOnMarkupAnnotationClicked(APdfMarkupAnnotation annotation)
    {
        for (IAnnotationViewerListener i : OnMarkAnnotationClickedListenerList)
            i.onMarkupAnnotationClicked(annotation);
    }

    public void fireOnNoAnnotationSelected()
    {
        for (IAnnotationViewerListener i : OnNoAnnotationSelectedListenerList)
            i.onNoAnnotationSelected();
    }

    public void fireOnAnnotationUpdated(APdfAnnotation annot)
    {
        for (IOnAnnotationUpdatedListener i : OnAnnotationUpdatedListenerList)
            i.onAnnotationUpdated(annot);
    }

    public void fireOnAnnotationDeleted(int page)
    {
        for (IOnAnnotationDeletedListener i : OnAnnotationDeletedListenerList)
        {
            i.onAnnotationDeleted(page);
        }
        try
        {
            updateBitmap();
        } catch (PdfViewerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void fireOnSearchCompleted(int page, int index, Map<Integer, List<Rectangle.Double>> rects) throws PdfViewerException
    {
        if (rects != null)
        {
            List<Rectangle.Double> rectList = new ArrayList<Rectangle.Double>();
            for (Entry<Integer, List<Rectangle.Double>> rectsOnPage : rects.entrySet())
            {
                for (Rectangle.Double rect : rectsOnPage.getValue())
                {
                    rectList.add(PdfUtils.CalculateRectOnCanvas(rect, canvas.getPageRect(rectsOnPage.getKey()), canvas.getRotation()));
                }
            }
            scrollIntoView(page, rectList);
            if (fitViewport(false))
                fireOnCurrentPageNo();
            updateBitmap();
        }
        for (IInternalOnSearchCompletedListener i : InternalOnSearchCompletedListenerList)
            i.internalOnSearchCompleted(page, index, rects);
    }

    /********************************
     * Public Update Methods *
     ********************************/

    @Override
    public void open(String filename, char[] password) throws PdfViewerException
    {
        if (!getLicenseIsValid())
        {
            throw getLastException();
        }
        fileName = filename;
        String strPassword;
        if (password != null)
        {
            strPassword = String.valueOf(password);
        } else
        {
            strPassword = "";
        }
        canvas.open(filename, strPassword);

    }

    public void saveAs(String path) throws PdfViewerException
    {
        canvas.documentManager.saveAs(path);
    }

    public void scroll(int distX, int distY) throws PdfViewerException
    {
        goToDestination(viewport.rectangle.x + distX, viewport.rectangle.y + distY);
        if (fitCycle(false))
        {
            fireOnCurrentPageNo();
        }
        updateBitmap();
    }

    public void setDestination(PdfDestination destination) throws PdfViewerException
    {
        goToDestination(new PdfAdvancedDestination(destination));
        if (fitCycle(false))
        {
            fireOnCurrentPageNo();
        }
        updateBitmap();
    }

    public boolean isOpen() 
    {
        return canvas.isOpen();
    }

    public void scrollTo(int absX, int absY) throws PdfViewerException
    {
        goToDestination(absX, absY);
        if (fitCycle(false))
        {
            fireOnCurrentPageNo();
        }
        updateBitmap();
    }

    /**
     * @throws PdfViewerException
     * @see PdfCanvas#setPageNo(int)
     */
    @Override
    public void setPageNo(int pageNo) throws IllegalArgumentException, PdfViewerException
    {
        if (pageNo < 1)
            throw new IllegalArgumentException("Page number must be positive");
        if (pageNo > canvas.getPageCount())
            throw new IllegalArgumentException("Page number " + pageNo + " does not exist");
        if (canvas.getPageCount() == 0)
            return;

        goToDestination(pageNo);

        if (fitCycle(false))
        {
            fireOnCurrentPageNo();
        }
        updateBitmap();
    }

    @Override
    public void setZoom(double zoomFactor) throws IllegalArgumentException, PdfViewerException
    {
        if (zoomFactor <= 0)
            throw new IllegalArgumentException("Invalid zoom value.");
        if (zoomFactor == viewport.getZoomFactor())
            return;
        zoomFactor = Math.max(zoomFactor, MIN_ZOOM);
        zoomFactor = Math.min(zoomFactor, MAX_ZOOM);

        viewport.zoomCenteredOnViewportCenter(zoomFactor);
        this.fitMode = TFitMode.ACTUAL_SIZE;
        try
        {

            if (fitCycle(false))
            {
                fireOnCurrentPageNo();
            }

            updateBitmap();
        } catch (NoFileOpenedException ex)
        {
            // dont do anything then
        }
    }

    /**
     * @throws PdfViewerException
     * @see PdfCanvas#setCanvasMode()
     */
    @Override
    public void setPageLayoutMode(TPageLayoutMode layoutMode) throws IllegalArgumentException, PdfViewerException
    {
        if (canvas.getPageLayoutMode().equals(layoutMode))
            return;
        Point.Double offset;
        try
        {
            offset = getOffsetOnPage();
            changeTPageLayoutMode(layoutMode);
            // set the viewport to look at the page that it looked at before
            // changing the canvasmode
            goToDestination(firstPageOnViewport, offset);
            // update the pagenumbers

            if (fitCycle(true))
            {
                fireOnCurrentPageNo();
            }
            updateBitmap();
        } catch (NoFileOpenedException ex)
        {
            changeTPageLayoutMode(layoutMode);
        }
    }

    private void changeTPageLayoutMode(TPageLayoutMode layoutMode) throws IllegalArgumentException, PdfViewerException
    {
        canvas.setPageLayoutMode(layoutMode);
        fireOnPageLayoutModeChanged(layoutMode);

        // TODO: you might wanna have a dispose method or something to do that
        // instead of saving it to registry at every change
        userPref.put("PageLayout", this.getPageLayoutMode().toString());
    }

    /**
     * @throws PdfViewerException
     * @see PdfCanvas#setBorderSize()
     */
    @Override
    public void setBorderSize(double borderSize) throws IllegalArgumentException, PdfViewerException
    {
        try
        {
            Point.Double offset = getOffsetOnPage();
            canvas.setBorderSize(borderSize);
            // set the viewport to look at the page that it looked at before
            // changing the borderSize
            goToDestination(firstPageOnViewport, offset);
            if (fitCycle(true))
            {
                fireOnCurrentPageNo();
            }
            updateBitmap();
        } catch (NoFileOpenedException ex)
        {
            canvas.setBorderSize(borderSize);
        }
    }

    /**
     * @throws PdfViewerException
     * @see PdfCanvas#setRotation()
     */
    @Override
    public void setRotation(int rotation) throws IllegalArgumentException, PdfViewerException
    {
        try
        {
            rotation = ((rotation % 360) + 360) % 360;
            canvas.setRotation(rotation);
            // set the viewport to look at the page that it looked at before
            // changing the rotation
            goToDestination(firstPageOnViewport);
            if (fitCycle(true))
            {
                fireOnCurrentPageNo();
            }
            updateBitmap();
        } catch (NoFileOpenedException ex)
        {
            canvas.setRotation(rotation);
        }
    }

    @Override
    public void setFitMode(TFitMode fitMode) throws IllegalArgumentException, PdfViewerException
    {
        if (fitMode == null)
            throw new IllegalArgumentException("Argument cannot be null.");

        try
        {
            if (canvas.getPageCount() == 0)
                return;
            changeFitMode(fitMode);

            if (fitMode == TFitMode.ACTUAL_SIZE)
            {
                // set the viewport to look at the page that it looked at before
                // changing the ZoomMode
                goToDestination(firstPageOnViewport);
            }
            if (fitCycle(true))
            {
                fireOnCurrentPageNo();
            }
            updateBitmap();

        } catch (NoFileOpenedException ex)
        {
            changeFitMode(fitMode);
        }
    }

    private void changeFitMode(TFitMode fitMode)
    {
        this.fitMode = fitMode;
        fireOnFitModeChanged(fitMode);

        if (this.fitMode == TFitMode.ACTUAL_SIZE)
            viewport.setZoomFactor(1.0);
    }

    /**
     * Sets the size of the viewport in pixels.
     * 
     * @param width
     *            The new viewport width
     * @param height
     *            The new parameter height
     * @throws PdfViewerException
     *             If there was no File open (viewport sizes are still adapted
     *             though)
     */
    public void setSize(int width, int height) throws PdfViewerException
    {
        if (width != viewport.rectangle.width || height != viewport.rectangle.height)
        {
            viewport.rectangle.width = width;
            viewport.rectangle.height = height;
            try
            {
                if (fitCycle(true))
                {
                    fireOnCurrentPageNo();
                }
                updateBitmap();
            } catch (NoFileOpenedException ex)
            {
                // then there is nothing to do
            }
        }
    }

    /**
     * Called on zoom Rectangle.Integer drawn onto viewer pane. Calculate new zoom
     * factor and the new viewport origin with the specified rectangle
     * parameters.
     * 
     * @param x
     *            ZoomRectangle.x
     * @param y
     *            ZoomRectangle.y
     * @param width
     *            ZoomRectangle.width
     * @param height
     *            ZoomRectangle.height
     * @throws PdfViewerException
     */
    public void setZoomRectangle(int x, int y, int width, int height) throws PdfViewerException
    {
        // calculate newZoomFactor
        double newZoomFactorHeight = (double) viewport.rectangle.height / (double) height * viewport.getZoomFactor();
        double newZoomFactorWidth = (double) viewport.rectangle.width / (double) width * viewport.getZoomFactor();
        double newZoomFactor = Math.min(newZoomFactorHeight, newZoomFactorWidth);
        newZoomFactor = Math.max(newZoomFactor, MIN_ZOOM);
        newZoomFactor = Math.min(newZoomFactor, MAX_ZOOM);

        // Set new position
        viewport.rectangle.setLocation(
                (int) ((double) (x + width / 2) / viewport.getZoomFactor() * newZoomFactor) - viewport.rectangle.width / 2,
                (int) ((double) (y + height / 2) / viewport.getZoomFactor() * newZoomFactor) - viewport.rectangle.height / 2);

        // Set new zoom factor
        viewport.setZoomFactor(newZoomFactor);
        fitMode = TFitMode.ACTUAL_SIZE;
        if (fitCycle(false))
        {
            fireOnCurrentPageNo();
        }

        fireOnZoomCompleted();
        updateBitmap();
    }

    public void resume() throws PdfViewerException
    {
        suspended = false;
        updateBitmap();
    }

    /********************************
     * Update helper methods *
     ********************************/

    /**
     * Will fit according to zoomMode, update pagenumbers and constrain the
     * viewport in a cycle, until no more changes occur
     * 
     * @param zoomingInAllowed
     *            specifies whether the method may zoom the viewport further in
     *            to fit the content better
     * @returns whether firstPageOnViewport has changed
     * @throws PdfViewerException
     * 
     */
    private boolean fitCycle(boolean zoomingInAllowed) throws PdfViewerException
    {
        int oldFirstPage = this.firstPageOnViewport;
        if (!canvas.getPageLayoutMode().isScrolling())
        {
            // Because pagenumbers cannot change in pagemode, we dont have to
            // call
            // updatePageNo and dont loop.
            fitViewport(true);
            constrainViewport();
            return (this.firstPageOnViewport != oldFirstPage);
        } else
        {
            updatePageNo();
            while (true)
            {
                boolean visiblePagesChanged = false;
                if (fitViewport(zoomingInAllowed))
                {
                    visiblePagesChanged |= updatePageNo();
                }
                if (constrainViewport())
                {
                    visiblePagesChanged |= updatePageNo();
                }
                if (visiblePagesChanged)
                {
                    continue;
                } else
                {
                    return (this.firstPageOnViewport != oldFirstPage);
                }
            }
        }
    }

    private Point.Double getOffsetOnPage() throws PdfViewerException
    {
        Rectangle.Double canvasViewport = PdfUtils.viewportToCanvas(viewport.rectangle, viewport.getZoomFactor());
        Rectangle.Double pageRect = canvas.getPageRect(firstPageOnViewport);
        Point.Double viewportOrigin = new Point.Double(canvasViewport.x - pageRect.x, canvasViewport.y - pageRect.y);

        return viewportOrigin;

    }

    private void goToDestination(int pageNo) throws PdfViewerException
    {
        goToDestination(pageNo, new Point.Double(0.0, 0.0));
    }

    private void goToDestination(int pageNo, Point.Integer offset) throws PdfViewerException
    {
        goToDestination(pageNo, new Point.Double(offset.x, offset.y));
    }

    private void goToDestination(int pageNo, Point.Double offset) throws PdfViewerException
    {
        // crop pageNo
        pageNo = Math.max(1, Math.min(canvas.getPageCount(), pageNo));

        if (!canvas.getPageLayoutMode().isScrolling())
        {
            // We only need to update the pageNumbers, the viewport will stay at
            // the same location and be adjusted due to constrainViewport and
            // fitViewport
            switch (canvas.getPageLayoutMode())
            {
            case SinglePage:
            {
                firstPageOnViewport = pageNo;
                lastPageOnViewport = pageNo;
                break;
            }
            case TwoPageLeft:
            {
                firstPageOnViewport = pageNo - ((pageNo + 1) % 2);
                lastPageOnViewport = Math.min(canvas.getPageCount(), firstPageOnViewport + 1);
                break;
            }
            case TwoPageRight:
            {
                if (pageNo == 1)
                {
                    firstPageOnViewport = 1;
                    lastPageOnViewport = 1;
                } else
                {
                    firstPageOnViewport = pageNo - (pageNo % 2);
                    lastPageOnViewport = Math.min(canvas.getPageCount(), firstPageOnViewport + 1);
                }
                break;
            }
            default:
                break;
            }
            canvas.setPageNo(firstPageOnViewport);
            fireOnCurrentPageNo();
        } else
        {
            Rectangle.Double unionRectangle = canvas.getUnionRectangleWithNeighbour(pageNo);
            viewport.rectangle.x = PdfUtils.canvasToPixel(unionRectangle.x, viewport.getZoomFactor()) - viewport.rectangle.width / 2;
            viewport.rectangle.y = PdfUtils.canvasToPixel(unionRectangle.y, viewport.getZoomFactor()) + (int) offset.y + 1;
            Rectangle.Integer targetRectangle = PdfUtils.canvasToPixel(unionRectangle, viewport.getZoomFactor());
            if (viewport.rectangle.x >= targetRectangle.x && viewport.rectangle.getMaxX() <= targetRectangle.getMaxX())
            {
                // we see everything, its fine
                return;
            } else
            {
                // we cant see the entire unionRectangle, go to page instead and
                // position its upper left corner in the upper left corner of
                // the viewport
                viewport.rectangle.x = PdfUtils.canvasToPixel(canvas.getPageRect(pageNo).x - canvas.getBorderSize(),
                        viewport.getZoomFactor()) + (int) offset.x;
            }
        }
    }

    /**
     * @param x
     *            Coordinate on Viewport
     * @param y
     *            Coordinate on Viewport
     */
    private void goToDestination(int x, int y)
    {
        viewport.rectangle.x = x;
        viewport.rectangle.y = y;
    }

    /**
     * @param x
     *            coordinate on canvas
     * @param y
     *            coordinate on canvas
     * @throws PdfViewerException
     */
    private void goToDestination(double x, double y) throws PdfViewerException
    {
        viewport.rectangle.x = PdfUtils.canvasToPixel(x, viewport.getZoomFactor());
        viewport.rectangle.y = PdfUtils.canvasToPixel(y, viewport.getZoomFactor());
    }

    private void goToDestination(PdfAdvancedDestination destination) throws PdfViewerException
    {
        destination.setPageRect(canvas.getPageRect(destination.getPage()));
        destination.setViewport(viewport);
        Rectangle.Integer destinationRect = destination.getRect();

        switch (destination.getType())
        {
        case eDestinationXYZ:
            goToDestination(destination.getPage(), destinationRect.getLocation());
            viewport.zoomCenteredOnLocation(destination.getZoom(), destinationRect.getLocation());
            break;
        case eDestinationFitBV:
        case eDestinationFitV:
            // TODO We cant do that, we'll fit to page instead:
        case eDestinationFitB:
        case eDestinationFit:
            changeFitMode(TFitMode.FIT_PAGE);
            goToDestination(destination.getPage());
            break;
        case eDestinationFitBH:
        case eDestinationFitH:
            changeFitMode(TFitMode.FIT_WIDTH);
            goToDestination(destination.getPage(), destinationRect.getLocation());
            break;
        case eDestinationFitR:
            this.setZoomRectangle(destinationRect.x, destinationRect.y, destinationRect.width, destinationRect.height);
            break;
        case eDestinationInvalid:
        default:
            throw new IllegalArgumentException();
        }
    }

    private void scrollIntoView(int pageNo, List<Rectangle.Double> textFragmentRectsOnCanvas) throws PdfViewerException
    {
        if (!canvas.getPageLayoutMode().isScrolling())
        {
            if (pageNo < firstPageOnViewport || pageNo > lastPageOnViewport)
                goToDestination(pageNo);
        }
        for (Rectangle.Double rect : textFragmentRectsOnCanvas)
        {
            viewport.scrollRectIntoView(rect);
        }
        // because we want the first one to be visible for sure
        viewport.scrollRectIntoView(textFragmentRectsOnCanvas.get(0));

    }

    /**
     * keeps the viewport inside the canvas, if viewport < canvas centers the
     * canvas on the viewport, if viewport > canvas And does the same thing with
     * the Rectangle.Integer of all visible pages
     * 
     * @throws PdfViewerException
     * 
     * @returns whether this method had to move the viewport
     */
    private boolean constrainViewport() throws PdfViewerException
    {
        boolean changed = constrainViewportToRectangle(canvas.getCanvasRect(), true);

        /*
         * This part would be for constraining the viewport to the visible
         * pages, but we only constrain to canvas Rectangle.Double
         * visiblePagesRectangle.Integer = new Rectangle.Double(); for (int pageNo =
         * firstPageOnViewport; pageNo <= lastPageOnViewport; pageNo++) {
         * visiblePagesRectangle.Integer = (Rectangle.Double)
         * visiblePagesRectangle.createUnion(layout.getPageRect(pageNo)); }
         * visiblePagesRectangle.x -= layout.getBorderSize();
         * visiblePagesRectangle.y -= layout.getBorderSize();
         * visiblePagesRectangle.width += 2.0 * layout.getBorderSize();
         * visiblePagesRectangle.height += 2.0 * layout.getBorderSize(); changed
         * |= constrainViewportToRectangle(visiblePagesRectangle, false);
         */
        return changed;
    }

    private boolean constrainViewportToRectangle(Rectangle.Double rect, boolean center)
    {
        Rectangle.Double viewportRect = PdfUtils.viewportToCanvas(viewport.rectangle, viewport.getZoomFactor());

        // check if the viewport is not already constrained anyway
        if (rect.contains(viewportRect))
        {
            return false;
        }

        // check horizontal overruns
        if (viewportRect.width > rect.width)
        {
            if (center)
            {
                viewportRect.x = rect.getCenterX() - viewportRect.width / 2.0;
            } else if (viewportRect.x > rect.x)
            {
                viewportRect.x = rect.x;
            } else if (viewportRect.getMaxX() < rect.getMaxX())
            {
                viewportRect.x = rect.getMaxX() - viewportRect.width;
            }
        } else if (viewportRect.x < rect.x)
        {
            viewportRect.x = rect.x;
        } else if (viewportRect.getMaxX() > rect.getMaxX())
        {
            viewportRect.x = rect.getMaxX() - viewportRect.width;
        }

        // check vertical overruns
        if (viewportRect.height > rect.height)
        {
            if (center)
            {
                viewportRect.y = rect.getCenterY() - viewportRect.height / 2.0;
            } else if (viewportRect.y > rect.y)
            {
                viewportRect.y = rect.y;
            } else if (viewportRect.getMaxY() < rect.getMaxY())
            {
                viewportRect.y = rect.getMaxY() - viewportRect.height;
            }
        } else if (viewportRect.y < rect.y)
        {
            viewportRect.y = rect.y;
        } else if (viewportRect.getMaxY() > rect.getMaxY())
        {
            viewportRect.y = rect.getMaxY() - viewportRect.height;
        }

        // transform viewport back to pixels and set
        viewport.rectangle = PdfUtils.canvasToPixel(viewportRect, viewport.getZoomFactor());
        return true;

    }

    /**
     * Zooms out the viewport, to fit the content within the viewport according
     * to the zoomMode
     * 
     * @param zoomingInAllowed
     *            specifies whether the method may zoom the viewport further in
     *            to fit the content better
     * @throws PdfViewerException
     * @returns whether zooming out has been performed
     */
    private boolean fitViewport(boolean zoomingInAllowed) throws PdfViewerException
    {

        switch (fitMode)
        {
        case FIT_PAGE:
        {
            // Iterate through all visible pages and zoom out, such that each
            // page would fit into the viewport, if it was offset correctly
            double newZoomFactor = 1.0;
            for (int pageNo = this.firstPageOnViewport; pageNo <= lastPageOnViewport; pageNo++)
            {
                Rectangle.Double pageRect = canvas.getUnionRectangleWithNeighbour(pageNo);

                // make page symmetric around x=0 if it isn't (this can happen
                // in
                // two
                // pages mode with unevenly wide pages)
                // We do not move the page in X direction, but rather extend its
                // virtual width, that we will later fit to
                if (pageRect.getCenterX() != 0.0)
                {
                    double maxHalfWidth = Math.max(-pageRect.x, pageRect.getMaxX());
                    pageRect.x = -maxHalfWidth;
                    pageRect.width = 2.0 * maxHalfWidth;
                }

                double widthToScaleTo = (pageRect.getWidth());
                double newZoomFactorWidth = PdfUtils.viewportToCanvas(viewport.rectangle.width, widthToScaleTo);
                double heightToScaleTo = (pageRect.getHeight());
                double newZoomFactorHeight = PdfUtils.viewportToCanvas(viewport.rectangle.height, heightToScaleTo);

                double zoomFactor = Math.min(newZoomFactorHeight, newZoomFactorWidth);
                newZoomFactor = Math.min(zoomFactor, newZoomFactor);
            }

            // Adjust zoomFactor if we have to zoom out to fit the page.
            // if zooming in is allowed, we have to adjust the zoomfactor anyway
            if (newZoomFactor < viewport.getZoomFactor() || zoomingInAllowed)
            {
                viewport.zoomCenteredOnLocation(newZoomFactor, new Point.Integer((int) viewport.rectangle.getCenterX(), viewport.rectangle.y));
                return true;
            }
            return false;
        }
        case FIT_WIDTH:
        {
            // allVisiblePagesRect is a Rectangle.Integer containing all pages on the
            // viewport. We fit the width to that
            Rectangle.Double allVisiblePagesRect = getPageRect(firstPageOnViewport);
            for (int i = firstPageOnViewport + 1; i <= lastPageOnViewport; i++)
            {
                Rectangle.Double r = getPageRect(i);
                allVisiblePagesRect = (Rectangle.Double) allVisiblePagesRect.createUnion(r);
            }
            allVisiblePagesRect.x -= canvas.getBorderSize();
            allVisiblePagesRect.width += 2 * canvas.getBorderSize();
            double widthToScale = (allVisiblePagesRect.getWidth());
            double newZoomFactor = PdfUtils.viewportToCanvas(viewport.rectangle.width, widthToScale);
            if (newZoomFactor < viewport.getZoomFactor() || zoomingInAllowed)
            {
                // adjust viewport zoom to fit all pages
                viewport.rectangle.y = (int) Math.ceil(viewport.rectangle.y / viewport.getZoomFactor() * newZoomFactor);
                viewport.setZoomFactor(newZoomFactor);
                Rectangle.Integer visibleRectangle = PdfUtils.canvasToPixel(allVisiblePagesRect, newZoomFactor);
                if (viewport.rectangle.getBounds().getMaxX() < visibleRectangle.getMaxX()
                        || viewport.rectangle.getBounds().getMinX() > visibleRectangle.getMinX())
                {
                    // adjust viewport position to view all pages
                    viewport.rectangle.x = (int) visibleRectangle.getCenterX() - viewport.rectangle.width / 2;
                }
                return true;

            } else
            {
                Rectangle.Integer visibleRectangle = PdfUtils.canvasToPixel(allVisiblePagesRect, viewport.getZoomFactor());
                if (viewport.rectangle.getBounds().getMaxX() < visibleRectangle.getMaxX() - 1
                        || viewport.rectangle.getBounds().getMinX() > visibleRectangle.getMinX())
                {
                    // adjust viewport position to view all pages
                    viewport.rectangle.x = (int) visibleRectangle.getCenterX() - viewport.rectangle.width / 2;
                    return true;
                }
                return false;
            }

        }
        default:
        {
            return false;
        }
        }

    }

    /**
     * Called when the viewport was being updated.
     * 
     * @throws PdfViewerException
     */
    private synchronized void updateBitmap() throws PdfViewerException
    {
        if (canvas.getPageCount() == 0 || suspended)
            return;

        Map<Integer, Rectangle.Double> pageRects = new HashMap<Integer, Rectangle.Double>();
        for (int i = firstPageOnViewport; i <= lastPageOnViewport; i++)
        {
            pageRects.put(i, canvas.getPageRect(i));
        }
        canvas.draw(viewport.rectangle.getSize(), canvas.getRotation(), pageRects, viewport.clone());
    }

    /**
     * Updates this.pageNo and this. lastPageOnViewport to represent the first
     * and last page currently seen on the viewport
     * 
     * @returns whether the first or last visible page has changed
     * @throws PdfViewerException
     */
    private boolean updatePageNo() throws PdfViewerException
    {
        // return dumbUpdatePageNo();
        return smartUpdatePageNo();
    }

    private boolean smartUpdatePageNo() throws PdfViewerException
    {
        Rectangle.Double viewportRect = PdfUtils.viewportToCanvas(viewport.rectangle, viewport.getZoomFactor());
        int pageCount = canvas.getPageCount();
        double viewportTop = viewportRect.getMinY();
        double viewportBot = viewportRect.getMaxY();

        int pageNo = firstPageOnViewport;
        Rectangle.Double page = canvas.getUnionRectangleWithNeighbour(pageNo);

        // iterate towards front until we find a page entirely above the
        // viewport
        while (page.getMaxY() - canvas.getBorderSize() > viewportTop && pageNo > 1)
        {
            pageNo--;
            page = canvas.getUnionRectangleWithNeighbour(pageNo);
        }
        // pageNo is a page somewhere above the viewport (not necessarily the
        // one just outside! could be far away!)

        // iterate towards end until we find a page at least partially within
        // the viewport
        while (page.getMaxY() - canvas.getBorderSize() < viewportTop && pageNo < pageCount)
        {
            pageNo++;
            page = canvas.getUnionRectangleWithNeighbour(pageNo);
        }
        // PageNo is exactly the first page on the viewport

        boolean changed = (firstPageOnViewport != pageNo);
        firstPageOnViewport = pageNo;
        canvas.setPageNo(firstPageOnViewport);

        // iterate towards end until we find a page entirely below the viewport
        while (page.getMinY() + canvas.getBorderSize() < viewportBot && pageNo <= pageCount)
        {
            pageNo++;
            if (pageNo > pageCount)
                break;
            page = canvas.getUnionRectangleWithNeighbour(pageNo);
        }
        // pageNo is now the first page below the viewport
        // (which may be an invalid page, if the last page on the viewport is
        // the last page on the document)

        changed |= (lastPageOnViewport != pageNo - 1);
        lastPageOnViewport = pageNo - 1;

        return changed;
    }

    public void onCanvasRectChanged(Rectangle.Double oldCanvasRect, Rectangle.Double newCanvasRect, int pageChanged)
    {
        if (pageChanged <= firstPageOnViewport && !oldCanvasRect.isEmpty())
            viewport.rectangle.y += PdfUtils.canvasToPixel(newCanvasRect.height - oldCanvasRect.height, viewport.getZoomFactor());
    }

    /************************************
     * TextSearch Methods *
     ************************************/

    /**
     * 
     * @param toSearch
     * @param startPage
     * @param startIndex
     * @param next
     * @throws PdfViewerException
     */
    public void search(String toSearch, int startPage, int startIndex) throws PdfViewerException
    {
        searcher.search(toSearch, startPage, startIndex);
    }

    public void configureSearcher(boolean matchCase, boolean wrap, boolean previous, boolean useRegex)
    {
        searcher.configureSearcher(matchCase, wrap, previous, useRegex);
    }

    /*****************************************
     * transformation methods *
     *****************************************/
    public Rectangle.Integer transformRectPageToViewport(Rectangle.Double rectOnUnrotatedPage, int pageNo) throws PdfViewerException
    {
        Rectangle.Double rectOnCanvas = PdfUtils.CalculateRectOnCanvas(rectOnUnrotatedPage, canvas.getPageRect(pageNo),
                canvas.getRotation());
        Rectangle.Integer rectOnViewport = PdfUtils.canvasToPixel(rectOnCanvas, viewport.getZoomFactor());
        rectOnViewport.translate(-viewport.rectangle.x, -viewport.rectangle.y);
        return rectOnViewport;
    }

    public int transformOnScreenToOnPage(Point.Integer onScreen, Point.Double onPage) throws PdfViewerException
    {
        double x = PdfUtils.viewportToCanvas(onScreen.x + viewport.rectangle.x, viewport.getZoomFactor());
        double y = PdfUtils.viewportToCanvas(onScreen.y + viewport.rectangle.y, viewport.getZoomFactor());
        Point.Double onCanvas = new Point.Double(x, y);
        int page = getPageContainingPoint(onCanvas);
        Point.Double onPage2 = PdfUtils.CalcOnPageCoordinates(onCanvas, canvas.getPageRect(page), canvas.getRotation());
        onPage.x = onPage2.x;
        onPage.y = onPage2.y;
        return page;
    }

    /*****************************************
     * IPdfControllerCallbackManager Methods *
     *****************************************/
    /*
     * private abstract class CallbackHandler<T> implements Runnable { public
     * CallbackHandler(IPdfControllerCallbackManager controller, T argument) {
     * this.controller = controller; this.argument = argument; }
     * 
     * IPdfControllerCallbackManager controller; T argument; }
     * 
     * @Override public void onDrawCompleted(BufferedImage bitmap,
     * PdfViewerException ex) { drawCompletedEvent.triggerEvent(bitmap, ex);
     * Runnable r = new CallbackHandler<BufferedImage>(controller, bitmap) {
     * public void run() { controller.onDrawCompleted(argument); } };
     * controller.invokeCallback(r); }
     */

    @Override
    public void onOpenCompleted(final PdfViewerException ex)
    {

        PdfViewerComponent.invokeCallback(new Runnable()
        {
            public void run()
            {
                PdfViewerException e = ex;
                if (e == null)
                {
                    try
                    {
                        if (!ignoreDocumentPreferences)
                        {
                            TPageLayoutMode mode = canvas.getDocumentManager().requestPageLayoutMode().waitForCompletion();
                            if (mode != TPageLayoutMode.None)
                                changeTPageLayoutMode(mode);
                            PdfAdvancedDestination dest = canvas.getDocumentManager().requestOpenActionDestination().waitForCompletion();
                            if (dest.getType() != TDestinationType.eDestinationInvalid)
                                goToDestination(dest);
                            else
                                goToDestination(firstPageOnViewport);
                        } else
                        {
                            goToDestination(firstPageOnViewport);
                        }
                        fireOnCurrentPageNo();
                        fireOnPageLayoutModeChanged(getPageLayoutMode());
                        fireOnZoomCompleted();

                        fitCycle(false);
                        updateBitmap();
                    } catch (PdfViewerException x)
                    {
                        e = x;
                    }
                }
                for (IOnOpenCompletedListener listener : OnOpenCompletedListenerList)
                {
                    listener.onOpenCompleted(e);
                }
            }
        });
    }

    @Override
    public void onSaveCompleted(PdfViewerException ex)
    {
        if (ex != null)
        {
            for (IOnSaveCompletedListener listener : OnSaveCompletedListenerList)
            {
                listener.onSaveCompleted(ex);
            }
        }
    }

    @Override
    public void onCloseCompleted(final PdfViewerException ex)
    {
        PdfViewerComponent.invokeCallback(new Runnable()
        {
            public void run()
            {
                // fire on last draw completed to draw grey background again
                fireOnDrawCompleted(null);
                firstPageOnViewport = 1;
                lastPageOnViewport = 1;
                component.update();
                for (IOnCloseCompletedListener listener : OnCloseCompletedListenerList)
                {
                    listener.onCloseCompleted(ex);
                }
            }
        });
    }

    @Override
    public void onClosing()
    {
        for (IOnClosingListener listener : OnClosingListenerList)
        {
            listener.onClosing();
        }
    }

    @Override
    public void onDrawCompleted(final BufferedImage bitmap, final PdfViewerException ex)
    {
        PdfViewerComponent.invokeCallback(new Runnable()
        {
            public void run()
            {
                // only inform listeners about this event
                // if there is still a document opened
                if (canvas.isOpen())
                {
                    fireOnDrawCompleted(bitmap);
                component.update();
            }
            }
        });
    }

    @Override
    public void exceptionOccurred(PdfViewerException e)
    {
        e.printStackTrace();
        System.exit(-1);
    }

    /********************************
     * Helper Classes *
     ********************************/

    public class Viewport
    {
        public Viewport(PdfViewerController parrent)
        {
            this(new Rectangle.Integer(), 1.0, parrent);
        }

        public Viewport(Rectangle.Integer rect, double zoom, PdfViewerController parent)
        {
            this.parent = parent;
            rectangle = rect;
            setZoomFactor(zoom);

        }

        public Viewport clone()
        {
            return new Viewport((Rectangle.Integer) rectangle.clone(), _zoomFactor, null);
        }

        public Rectangle.Integer rectangle;

        private double _zoomFactor;
        private PdfViewerController parent;

        public void setZoomFactor(double zoomFactor)
        {
            this._zoomFactor = zoomFactor;
            if (parent != null)
                parent.fireOnZoomCompleted();
        }

        public double getZoomFactor()
        {
            return _zoomFactor;
        }

        public void zoomCenteredOnLocation(double newZoomFactor, Point.Integer location)
        {
            int deltaX = rectangle.x - location.x;
            int deltaY = rectangle.y - location.y;
            location.setLocation(((double) location.x) / _zoomFactor * newZoomFactor, ((double) location.y) / _zoomFactor * newZoomFactor);
            rectangle.x = location.x + deltaX;
            rectangle.y = location.y + deltaY;
            setZoomFactor(newZoomFactor);
            java.awt.Point p = new java.awt.Point();
            p.setLocation(4., 3.);
        }

        public void zoomCenteredOnViewportCenter(double newZoomFactor)
        {
            this.zoomCenteredOnLocation(newZoomFactor, new Point.Integer((int) rectangle.getCenterX(), (int) rectangle.getCenterY()));
        }

        public void scrollRectIntoView(Rectangle.Double rect)
        {
            Rectangle.Integer tRect = PdfUtils.canvasToPixel(rect, _zoomFactor);
            int rightOvershoot = Math.max(0, (int) tRect.getMaxX() - (int) rectangle.getMaxX() + viewportBorder);
            int bottomOvershoot = Math.max(0, (int) tRect.getMaxY() - (int) rectangle.getMaxY() + viewportBorder);
            rectangle.x += rightOvershoot;
            rectangle.y += bottomOvershoot;

            int leftOvershoot = Math.max(0, -tRect.x + rectangle.x + viewportBorder);
            int topOvershoot = Math.max(0, -tRect.y + rectangle.y + viewportBorder);
            rectangle.x -= leftOvershoot;
            rectangle.y -= topOvershoot;
        }

        private int viewportBorder = 20;
    }

    /******************************
     * Annotations related stuff *
     ******************************/

    // Dummy
    public void onAnnotationsLoaded(final int page, final PdfGenericAnnotation annotations[], final PdfViewerException ex)
    {
        return;
    }

    @Override
    public void onAnnotationUpdated(final APdfAnnotation annot, PdfViewerException e)
    {
        PdfViewerComponent.invokeCallback(new Runnable()
        {
            
            @Override
            public void run()
            {
                fireOnAnnotationUpdated(annot);
                try
                {
                    updateBitmap();
                } catch (PdfViewerException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onAnnotationDeleted(int page, PdfViewerException e) throws PdfViewerException
    {
        updateBitmap();
    }

    @Override
    public void onAnnotationCreated(final int page)
    {
        PdfViewerComponent.invokeCallback(new Runnable()
        {
            public void run()
            {
                try
                {
                    updateBitmap();
                    fireOnAnnotationCreated(page);
                } catch (PdfViewerException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public List<APdfAnnotation> loadAnnotationsOnPage(int page) throws PdfViewerException
    {
        return canvas.getDocumentManager().getAnnotationsOnPage(page);
    }

    public void udpateAnnotation(APdfAnnotation m_annotation) throws PdfViewerException
    {
        canvas.getDocumentManager().updateAnnotation(m_annotation);
    }

    public void createAnnotation(TPdfAnnotationType type, int page, Double[] rect) throws PdfViewerException
    {
        canvas.getDocumentManager().createAnnotation(type, page, rect);
    }

    public void deleteAnnotation(int page, long annotHandle) throws PdfViewerException
    {
        canvas.getDocumentManager().deleteAnnotation(page, annotHandle);
    }

    /******************************
     * OutlinesRelated stuff *
     ******************************/

    public void onOutlinesLoaded(final int parentId, final PdfOutlineItem items[], final PdfViewerException ex)
    {
        PdfViewerComponent.invokeCallback(new Runnable()
        {
            public void run()
            {
                fireOnOutlinesLoaded(parentId, items, ex);
            }
        });
    }

    public void loadOutlines(int parentId)
    {
        canvas.getDocumentManager().requestOutlines(parentId);
    }

    /******************************
     * ThumbnailRelated stuff *
     ******************************/

    private int thumbnailWidth = 70;
    private int thumbnailHeight = 70;

    public void setThumbnailWidth(int width)
    {
        thumbnailWidth = width;
    }

    public void setThumbnailHeight(int height)
    {
        thumbnailHeight = height;
    }

    @Override
    public void onThumbnailLoadCompleted(final int pageNo, final BufferedImage bitmap, final PdfViewerException e)
    {
        PdfViewerComponent.invokeCallback(new Runnable()
        {
            public void run()
            {
                fireOnThumbnailLoaded(pageNo, bitmap, e);
            }
        });
    }

    @Override
    public void loadThumbnail(int pageNo, boolean guaranteeExactness) throws PdfViewerException
    {
        Rectangle.Double sourceRect;
        if (guaranteeExactness)
            sourceRect = canvas.getPageRectGuaranteedExcactly(pageNo);
        else
            sourceRect = canvas.getPageRect(pageNo);

        double sourceRectWidth = canvas.getRotation() % 180 == 0 ? sourceRect.width : sourceRect.height;
        double sourceRectHeight = canvas.getRotation() % 180 == 0 ? sourceRect.height : sourceRect.width;

        // find out what the target rect looks like (it needs to be a scaled
        // version of source)
        double wScale = ((double) thumbnailWidth) / sourceRectWidth;
        double hScale = ((double) thumbnailHeight) / sourceRectHeight;
        int targetWidth = thumbnailWidth;
        int targetHeight = thumbnailHeight;
        if (wScale < hScale)
        {
            // use wScale
            targetHeight = Math.max(1, (int) (wScale * sourceRectHeight));
        } else
        {
            // use hScale
            targetWidth = Math.max(1, (int) (hScale * sourceRectWidth));
        }
        canvas.getDocumentManager().requestThumbnail(sourceRectWidth, sourceRectHeight, targetWidth, targetHeight, pageNo);
    }

    /********************************
     * Native Includes *
     ********************************/

    public static void setNewLicenseKey(String s) throws PdfViewerException
    {
        if (!PdfViewerController.setLicenseKey(s))
            throw getLastException();

    }

    public static void getAnyLicenseIsValid() throws PdfViewerException
    {
        if (!PdfViewerController.getLicenseIsValid())
            throw getLastException();
    }

    public static String getProductVersion()
    {
        return PdfViewerController.getProductVersion2();
    }

    /********************************
     * Variables *
     ********************************/

    private Viewport viewport;
    private ArrayList<IOnOpenCompletedListener> OnOpenCompletedListenerList;
    private ArrayList<IOnCloseCompletedListener> OnCloseCompletedListenerList;
    private ArrayList<IOnClosingListener> OnClosingListenerList;
    private ArrayList<IOnVisiblePageRangeChangedListener> OnVisiblePageRangeChangedListenerList;
    private ArrayList<IOnZoomCompletedListener> OnZoomCompletedListenerList;
    private ArrayList<IOnPageLayoutModeChangedListener> OnPageLayoutModeChangedListenerList;
    private ArrayList<IOnFitModeChangedListener> OnFitModeChangedListenerList;
    private ArrayList<IOnOutlinesLoadedListener> OnOutlinesLoadedListenerList;
    private ArrayList<IOnThumbnailLoadedListener> OnThumbnailLoadedListenerList;
    private ArrayList<IOnTextExtractedListener> OnTextExtractedListenerList;
    private ArrayList<IOnRotationChangedListener> OnRotationChangedListenerList;
    private ArrayList<IInternalOnSearchCompletedListener> InternalOnSearchCompletedListenerList;
    private ArrayList<IOnTextAnnotationActionListener> OnTextAnnotationActionListenerList;
    private ArrayList<IOnMarkupAnnotationHoverListener> OnMarkupAnnotationHoverListenerList;
    private ArrayList<IAnnotationViewerListener> OnMarkAnnotationClickedListenerList;
    private ArrayList<IAnnotationViewerListener> OnNoAnnotationSelectedListenerList;
    private ArrayList<IOnMarkupAnnotationActionListener> OnMarkupAnnotationActionListenerList;
    private ArrayList<IOnAnnotationCreatedListener> OnAnnotationCreatedListenerList;
    private ArrayList<IOnAnnotationUpdatedListener> OnAnnotationUpdatedListenerList;
    private ArrayList<IOnSaveCompletedListener> OnSaveCompletedListenerList;
    private ArrayList<IOnAnnotationDeletedListener> OnAnnotationDeletedListenerList;
    private ArrayList<IOnDrawCompletedListener> OnDrawCompletedListenerList;

    private int firstPageOnViewport, lastPageOnViewport;
    private String fileName;

    private TFitMode fitMode;
    private PdfCanvas canvas; // should be IPdfCanvas
    private PdfViewerComponent component;
    private PdfSearcher searcher;

    private BufferedImage outBitmap;
    private boolean suspended = false;

    private boolean ignoreDocumentPreferences = false;

    private final static double MAX_ZOOM = 100;
    private final static double MIN_ZOOM = 0.01;

    private static Preferences userPref = Preferences.userRoot().node("PDF Viewer Java");





}
