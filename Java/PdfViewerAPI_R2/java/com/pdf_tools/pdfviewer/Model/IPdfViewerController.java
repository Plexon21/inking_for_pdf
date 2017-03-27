/***************************************************************************
 *
 * File:            IPdfViewerController.java
 * 
 * Package:         3-Heights(TM) PDF Java Viewer
 *
 * Description:     The PDF Viewer controller interface.
 *
 * @author          Christian Hagedorn, PDF Tools AG   
 * 
 * Copyright:       Copyright (C) 2001 - 2008 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *                  
 ***************************************************************************/

package com.pdf_tools.pdfviewer.Model;

import java.awt.geom.Point2D;
import java.util.List;

import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.*;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager.IInternalOnSearchCompletedListener;
import com.pdf_tools.pdfviewer.converter.geom.Point;
import com.pdf_tools.pdfviewer.converter.geom.Rectangle;

/**
 * The IPdfViewerController interface describes the interface between the user
 * application and the PdfViewer.
 * 
 * @author cha
 *
 */
/**
 * @author fwe
 *
 */
public interface IPdfViewerController extends Cloneable
{

    /**
     * @author fwe Class for returning the result of a text selection operation.
     *         Containing th
     */
    public class PdfTextWithinSelectionResult
    {
        public List<PdfTextFragment> fragments;
        public double startX;
        public double endX;

        public PdfTextWithinSelectionResult(List<PdfTextFragment> fragments, double startX, double endX)
        {
            this.fragments = fragments;
            this.startX = startX;
            this.endX = endX;
        }
    }

    /**
     * Open the file with the associated password located at the absolute path
     * specified by filename. If the file does not contain a password an empty
     * string must be passed. This method is executed asynchronously. Upon
     * completion the event onOpenCompleted() is called.
     * 
     * @param filename: The absolute path to the file
     * @param password: The password needed to open the file 
     * @throws PdfViewerException
     *             If opening has encountered a problem.
     */
    public void open(String filename, char[] password) throws PdfViewerException;

    /**
     * Close the currently open file. This method is executed asynchronously.
     * Upon completion the event onCloseCompleted() is called.
     */
    public void close();

    /**
     * Returns the number of pages of the currently open document. If the
     * current document is not open then 0 is returned.
     * 
     * @return The number of pages
     * @throws PdfViewerException
     *             If there is no file open
     */
    public int getPageCount() throws PdfViewerException;

    /**
     * Returns the first page that is visible on the viewport (the page with the
     * smallest pagenumber)
     * 
     * @return The page number
     */
    public int getPageNo();

    /**
     * Returns the last page that is visible on the viewport (the page with the
     * highest pagenumber)
     * 
     * @return
     */
    public int getLastPageNo();

    /**
     * Get a List of all textFragments that are contained within the given
     * rectangle
     * 
     * @param markedRect: the rectangle in pixelcoordinates that is selected
     * @return a list of textfragments within markedRect
     * @throws PdfViewerException
     */
    public List<PdfTextFragment> getTextWithinRegion(Rectangle.Integer markedRect) throws PdfViewerException;

    /**
     * Sets the page number to the specified value. A valid page number is
     * between 1 and the number of pages. If the current document is not open
     * then the method returns without doing anything.
     * 
     * @param pageNo: The new page number
     * @throws IllegalArgumentException: If the page number is not valid
     * @throws PdfViewerException: If there is no file open
     */
    public void setPageNo(int pageNo) throws IllegalArgumentException, PdfViewerException;

    /**
     * Moves the viewport to view a given destination
     * 
     * @param destination: The Destination to look at
     * @throws PdfViewerException: If there is no file open
     */
    public void setDestination(PdfDestination destination) throws PdfViewerException;

    /**
     * Return the zoom factor which has been set by setZoomFactor. After a
     * completed open call the zoom factor is 1.0. If the current document is
     * not open then 0 is returned.
     * 
     * @return The zoom factor
     */
    public double getZoom();

    /**
     * Sets the zoom factor to the specified value. If the current document is
     * not open then the method returns without doing anything.
     * 
     * @param zoomFactor: The new zoom factor
     * @throws IllegalArgumentException: If the zoom factor is not valid
     * @throws PdfViewerException: If there is no file open
     */
    public void setZoom(double zoomFactor) throws IllegalArgumentException, PdfViewerException;

    /**
     * Returns the zoom mode which has been set by setZoomFactor. If the zoom
     * mode was not set yet, the zoom factor was changed manually, or the
     * current document is not open, then IPdfViewer.ZoomMode.ACTUAL_SIZE is
     * returned. After a completed open call the zoom mode is
     * IPdfViewer.ZoomMode.ACTUAL_SIZE.
     * 
     * @return The zoom mode
     */
    public TFitMode getZoomMode();

    /**
     * Sets the zoom mode to the specified mode. If the current document is not
     * open then the method returns without doing anything.
     * 
     * @param zoomMode: The new zoom mode
     * @throws IllegalArgumentException: If the argument is null
     * @throws PdfViewerException: If there is no file open
     */
    public void setFitMode(TFitMode zoomMode) throws IllegalArgumentException, PdfViewerException;

    /**
     * Returns the rotation which has been set by setRotation. After a completed
     * open call the rotation is 0. If the current document is not open then 0
     * is returned.
     * 
     * @return The rotation
     */
    public int getRotation();

    /**
     * Returns the currently opened file name. Sets "-" when nothing is opened.
     * 
     * @return The file name
     */
    public String getFileName();

    /**
     * Sets the rotation to the specified value. If the current document is not
     * open then the method returns without doing anything.
     * 
     * @param rotation: The new rotation
     * @throws IllegalArgumentException: If the rotation is not valid
     * @throws PdfViewerException: If there is no file open
     */
    public void setRotation(int rotation) throws IllegalArgumentException, PdfViewerException;

    /**
     * Returns the layout mode which has been set by setLayoutMode. After a
     * completed open call the layout mode is IPdfViewer.PdfLayoutMode.DOCUMENT.
     * If the current document is not open, then
     * IPdfViewer.PdfLayoutMode.DOCUMENT is returned.
     * 
     * @return The layout mode
     */
    public TPageLayoutMode getPageLayoutMode();

    /**
     * Sets the layout mode to the specified mode.
     * 
     * @param layoutMode: The new layout mode
     * @throws IllegalArgumentException: If the argument is null
     * @throws PdfViewerException: If there is no file open
     */
    public void setPageLayoutMode(TPageLayoutMode layoutMode) throws IllegalArgumentException, PdfViewerException;

    /**
     * Returns the border which has been set by setBorderSize.
     * 
     * @return The borderSize
     */
    public double getBorderSize();

    /**
     * Sets the border size to the specified value. The border size should be
     * set before the document is opened. Otherwise the layout has to be created
     * again.
     * 
     * @param borderSize: The new border size
     * @throws IllegalArgumentException: If the border size is not valid.
     * @throws PdfViewerException: If there is no file open
     */
    public void setBorderSize(double borderSize) throws IllegalArgumentException, PdfViewerException;

    /**
     * Searches the document for a given searchstring
     * 
     * @param toSearch: the string to search for
     * @param startPage: the page on which searching is started
     * @param startIndex: the index of the first character on the startPage, from where 
     *                    searching is started (When repeatedly searching the same term,
     *                    insert the index of the last found match here)
     * @throws PdfViewerException
     */
    public void search(String toSearch, int startPage, int startIndex) throws PdfViewerException;

    /**
     * configures searching algorithm
     * 
     * @param matchCase: true iff search should be case sensitive
     * @param wrap: true iff reaching the end/beginning of the document should resume search at beginning/end
     * @param previous: true iff searching should be done back to front instead of front to back of document
     * @param useRegex: true iff the given search string should be interpreted as a regular expression
     */
    public void configureSearcher(boolean matchCase, boolean wrap, boolean previous, boolean useRegex);

    /**
     * Pauses rendering of this control, until resume() is called
     */
    public void suspend();

    /**
     * Ends pausing the renderer (initiated by pause()) and forces an immediate
     * rendering call
     * 
     * @throws PdfViewerException: if an exception gets thrown out of rendering call
     */
    public void resume() throws PdfViewerException;

    /**
     * Determines whether the viewer will ignore the preferences, that are
     * embedded in the pdf file (pageLayout, openAction etc.)
     * 
     * @param ignore: true if the viewer is to ignore embedded preferences
     */
    public void setIgnoringPreferences(boolean ignore);

    /**
     * get whether the viewer ignores embedded preferences
     * 
     * @returns true if ignoring preferences
     */
    public boolean getIgnoringPreferences();

    /**
     * transforms a rectangle that is given in unrotated pagecoordinates to
     * viewport coordinates
     * 
     * @param rectOnUnrotatedPage: The rectangle on the unrotated page
     * @param pageNo: page the rectangle is on
     * @return The rectangle in viewport coordinates
     * @throws PdfViewerException
     */
    public Rectangle.Integer transformRectPageToViewport(Rectangle.Double rectOnUnrotatedPage, int pageNo) throws PdfViewerException;

    public int transformOnScreenToOnPage(Point.Integer onScreen, Point.Double onPage) throws PdfViewerException;

    public void setThumbnailWidth(int width);

    public void setThumbnailHeight(int height);

    public void loadOutlines(int parentId);

    public void loadThumbnail(int pageNo, boolean guaranteeExactness) throws PdfViewerException;

    public List<APdfAnnotation> loadAnnotationsOnPage(int pageNo) throws PdfViewerException;

    public void setTextSelectionStartPoint(Point.Integer start) throws PdfViewerException;

    public PdfTextWithinSelectionResult getTextWithinSelection(Point.Integer start, Point.Integer end) throws PdfViewerException;

    /**
     * @param listener
     *            A object implementing IOnOpenCompletedListener, whose
     *            onOpenCompleted method will be called by this controller if
     *            opening a file has completed
     */
    public void registerOnOpenCompleted(IOnOpenCompletedListener listener);

    /**
     * @param listener
     *            A object implementing IOnCloseCompletedListener , whose
     *            onCloseCompleted method will be called by this controller if
     *            closing a file has completed
     */
    public void registerOnCloseCompleted(IOnCloseCompletedListener listener);

    /**
     * @param listener
     *            A object implementing IOnClosingListener , whose onClosing
     *            method will be called by this controller if closing a file has
     *            started
     */
    public void registerOnClosing(IOnClosingListener listener);

    /**
     * @param listener
     * 
     */

    public void registerOnSaveCompleted(IOnSaveCompletedListener listener);

    /**
     * @param listener
     *            A object implementing IOnCurrentPageNoListener, whose
     *            onCurrentPageNo method will be called by this controller if
     *            the topmost page on the viewport has changed
     */
    public void registerOnVisiblePageRangeChanged(IOnVisiblePageRangeChangedListener listener);

    /**
     * @param listener
     *            A object implementing IOnZoomCompletedListener, whose
     *            onZoomCompleted method will be called by this controller if
     *            zooming has complted
     */
    public void registerOnZoomCompleted(IOnZoomCompletedListener listener);

    public void registerOnFitModeChanged(IOnFitModeChangedListener listener);

    /**
     * @param listener
     *            A object implementing IOnPageLayoutModeChanged, whose
     *            onPageLayoutModeChanged method will be called by this
     *            controller, if the PageLayoutMode changes
     */
    public void registerOnPageLayoutModeChanged(IOnPageLayoutModeChangedListener listener);

    /**
     * 
     * @param listener
     *            A object implementing IOnOutlinesLoadedListener, whose
     *            onOutlinesLoaded method will be called by this controller, if
     *            new outlines have been loaded
     */
    public void registerOnOutlinesLoaded(IOnOutlinesLoadedListener listener);

    public void registerOnThumbnailLoaded(IOnThumbnailLoadedListener listener);

    public void registerOnTextExtracted(IOnTextExtractedListener listener);

    public void registerOnRotationChanged(IOnRotationChangedListener listener);

    public void registerInternalOnSearchCompleted(IInternalOnSearchCompletedListener listener);
    
    public void registerOnAnnotationUpdated(IOnAnnotationUpdatedListener listener);
    
    public void registerOnAnnotationCreated(IOnAnnotationCreatedListener listener);
    
    public void registerOnAnnotationDeleted(IOnAnnotationDeletedListener listener);
    
    public void registerOnDrawCompleted(IOnDrawCompletedListener listener);

}
