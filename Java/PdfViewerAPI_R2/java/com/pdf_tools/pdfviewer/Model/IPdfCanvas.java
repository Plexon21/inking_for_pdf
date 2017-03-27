/***************************************************************************
 *
 * File:            PdfCanvas.java
 * 
 * Package:         3-Heights(TM) PDF Java Viewer
 *
 * Description:     The PDF Canvas of the current document in user space
 * 					units.
 * 
 * @author          Christian Hagedorn, PDF Tools AG   
 * 
 * Copyright:       Copyright (C) 2001 - 2008 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *                  
 ***************************************************************************/

package com.pdf_tools.pdfviewer.Model;

import java.util.List;
import java.util.Map;

import com.pdf_tools.pdfviewer.DocumentManagement.IPdfDocumentManager;
import com.pdf_tools.pdfviewer.DocumentManagement.PdfDocument;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.TPageLayoutMode;
import com.pdf_tools.pdfviewer.Model.IPdfViewerController.PdfTextWithinSelectionResult;
import com.pdf_tools.pdfviewer.converter.geom.Dimension;
import com.pdf_tools.pdfviewer.converter.geom.Point;
import com.pdf_tools.pdfviewer.converter.geom.Rectangle;

/**
 * The PdfCanvas class represents the canvas of the current document in user
 * space units. The y coordinate ranges from 0 to canvas.height and the x
 * coordinate ranges from -canvas.width/2 to +canvas.width/2 for ONE_PAGE. In
 * TWO_PAGES modes x = 0 is always in the middle of the middle border. Otherwise
 * in ONE_PAGE mode x = 0 corresponds to the center point of the canvas width.
 * All values in this class are stored and returned in user space units (1/72
 * inch).
 * 
 * @author cha
 *
 */
public interface IPdfCanvas
{
    /**
     * Returns the border size of the document in user space units.
     * 
     * @return The border size of the document
     */
    public double getBorderSize();

    public void setPageNo(int pageNo) throws PdfViewerException;

    /**
     * Sets the border size of the document in centimetres. The value will be
     * converted and stored in user space units. A recalculation of the canvas
     * will be required afterwards.
     * 
     * @param borderSize: The new border size in centimetres
     * @throws PdfViewerException
     * @throws IllegalArgumentException
     */
    public void setBorderSize(double borderSize) throws IllegalArgumentException, PdfViewerException;

    /**
     * Returns the rotation of the document (0, 90, 180, 270)
     * 
     * @return The rotation of the document.
     */
    public int getRotation();

    public List<PdfTextFragment> GetTextWithinPageRange(int firstPage, int lastPage, double zoomFactor) throws PdfViewerException;

    public List<PdfTextFragment> GetTextWithinRegion(Rectangle.Integer markedRect, int firstPage, int lastPage, double zoomFactor)
            throws PdfViewerException;

    /**
     * Sets the rotation of the document. A recalculation of the canvas will be
     * required afterwards.
     * 
     * @param rotation: The new border size in centimetres
     * @throws PdfViewerException
     */
    public void setRotation(int rotation) throws IllegalArgumentException, PdfViewerException;

    /**
     * Returns the page Rectangle.Integer of the specified page with regard of rotation
     * in canvas coordinates (user space units). If the page number is invalid
     * an empty Rectangle.Integer at position (0,-1) is returned. This saves comparisons
     * in the updatePageNo() method.
     * 
     * @param pageNo: The page whose Rectangle.Integer to return
     * 
     * @return The page Rectangle.Integer of the specified page if the page is valid.
     *         Otherwise an empty Rectangle.Integer at position (0,-1)
     * @throws PdfViewerException
     * @throws PdfViewerException
     */
    public Rectangle.Double getPageRect(int pageNo) throws PdfViewerException;

    /**
     * Method called on open request.
     * 
     * @throws PdfViewerException
     * @throws PdfViewerException
     */
    public void open(String filename, String password) throws PdfViewerException;

    /**
     * Method called on close request.
     */
    public void close();

    /**
     * Returns the canvas Rectangle.Integer in user space units.
     * 
     * @return The canvas rectangle
     * @throws PdfViewerException
     * @throws PdfViewerException
     */
    public Rectangle.Double getCanvasRect() throws PdfViewerException;

    /**
     * @throws PdfViewerException
     * @see PdfDocument#getPageCount()
     */
    public int getPageCount() throws PdfViewerException;

    /**
     * returns a new Rectangle.Integer containing the page specified by @pageNo unified
     * with any horizontal neighboring pages including borders
     * 
     * @param pageNo
     * @return
     * @throws PdfViewerException
     * @throws PdfViewerException
     */
    public Rectangle.Double getUnionRectangleWithNeighbour(int pageNo) throws PdfViewerException;

    /**
     * queues drawcommand on the document, returns immediately
     * 
     * @param bitmap: the bitmap to draw into
     * @param pages: the pagenumbers of the pages that have to be drawn
     * @param sourceRects: the rectangles which have to be read from the source on the page
     * @param targetRects: the rectangles where the pages have to be drawn on the bitmap
     */
    public void draw(Dimension bitmapSize, int rotation, Map<Integer, Rectangle.Double> pageRects, PdfViewerController.Viewport viewport);

    public IPdfDocumentManager getDocumentManager();

    void setPageLayoutMode(TPageLayoutMode layoutMode) throws IllegalArgumentException, PdfViewerException;

    TPageLayoutMode getPageLayoutMode();

    PdfTextWithinSelectionResult getTextWithinSelection(Point.Double start, Point.Double end, int firstPage, int lastPage)
            throws PdfViewerException;
}
