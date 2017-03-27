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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import com.pdf_tools.pdfviewer.DocumentManagement.IPdfDocumentManager;
import com.pdf_tools.pdfviewer.DocumentManagement.PdfDocument;
import com.pdf_tools.pdfviewer.DocumentManagement.PdfDocumentManagerMultithreaded;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.IOnVisiblePageRangeChangedListener;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.TPageLayoutMode;
import com.pdf_tools.pdfviewer.Model.IPdfViewerController.PdfTextWithinSelectionResult;
import com.pdf_tools.pdfviewer.caching.IGenericCache;
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
/**
 * @author fwe
 *
 */
public class PdfCanvas implements IPdfCanvas, IOnVisiblePageRangeChangedListener, IGenericCache.IItemLoadedToCacheListener<Integer>
{
    /**
     * Creates an empty canvas object of the underlying document.
     */
    PdfCanvas(PdfViewerController controller)
    {
        this.documentManager = new PdfDocumentManagerMultithreaded(controller);
        canvasRect = new Rectangle.Double();
        this.pageLayoutMode = TPageLayoutMode.OneColumn;
        this.controller = controller;
        newlyLoadedPages = new TreeSet<Integer>();
        pagesToLoadExactly = new TreeSet<Integer>();
        controller.registerOnVisiblePageRangeChanged(this);
        documentManager.registerPagesLoadedToCacheListener(this);
    }

    private enum TCanvasDirtyness
    {
        Clean, RectsDirty, CanvasDirty
    };

    /**
     * Returns the current layout mode of the associated document.
     * 
     * @return The layout mode of the document
     */
    @Override
    public TPageLayoutMode getPageLayoutMode()
    {
        return pageLayoutMode;
    }

    /**
     * Sets the current layout mode of the associated document.
     * 
     * @param layoutMode
     *            The new layout mode of the document
     * @throws PdfViewerException
     */
    @Override
    public void setPageLayoutMode(TPageLayoutMode layoutMode) throws IllegalArgumentException, PdfViewerException
    {
        if (layoutMode == null)
            throw new IllegalArgumentException("Argument cannot be null.");
        if (layoutMode == this.pageLayoutMode)
            return;

        this.pageLayoutMode = layoutMode;
        canvasDirty = TCanvasDirtyness.CanvasDirty;
    }

    /**
     * Returns the border size of the document in user space units.
     * 
     * @return The border size of the document
     */
    @Override
    public double getBorderSize()
    {
        return borderSize;
    }

    /**
     * Sets the border size of the document in centimetres. The value will be
     * converted and stored in user space units. A recalculation of the canvas
     * will be required afterwards.
     * 
     * @param borderSize
     *            The new border size in centimetres
     * @throws PdfViewerException
     */
    @Override
    public void setBorderSize(double borderSize) throws IllegalArgumentException, PdfViewerException
    {
        if (borderSize < 0)
            throw new IllegalArgumentException("Negative border size");
        if (borderSize > MAX_BORDER_SIZE)
            throw new IllegalArgumentException("Border size too big");
        if (borderSize == this.borderSize)
            return;

        this.borderSize = borderSize * CM_TO_INCH * 72;
        canvasDirty = TCanvasDirtyness.CanvasDirty;
    }

    /**
     * Returns the rotation of the document (0, 90, 180, 270)
     * 
     * @return The rotation of the document.
     */
    @Override
    public int getRotation()
    {
        return rotation;
    }

    /**
     * Sets the rotation of the document. A recalculation of the canvas will be
     * required afterwards.
     * 
     * @param rotation
     *            The new border size in centimetres
     * @throws PdfViewerException
     */
    @Override
    public void setRotation(int rotation) throws IllegalArgumentException, PdfViewerException
    {
        if (rotation % 90 != 0)
            throw new IllegalArgumentException("Illegal rotation value");
        this.rotation = rotation;
        documentManager.setRotation(rotation);

        canvasDirty = TCanvasDirtyness.CanvasDirty;
        controller.fireOnRotationChanged(rotation);
    }

    /**
     * Returns the page rectangle of the specified page with regard of rotation
     * in canvas coordinates (user space units). If the page number is invalid
     * an empty rectangle at position (0,-1) is returned. This saves comparisons
     * in the updatePageNo() method.
     * 
     * @param pageNo
     *            The page whose rectangle to return
     * 
     * @return The page rectangle of the specified page if the page is valid.
     *         Otherwise an empty rectangle at position (0,-1)
     * @throws PdfViewerException
     */
    @Override
    public Rectangle.Double getPageRect(int pageNo) throws PdfViewerException
    {
        calculateCanvas();
        return (Rectangle.Double) documentManager.getPageGuess(pageNo).clone();
    }

    public Rectangle.Double getPageRectGuaranteedExcactly(int pageNo) throws PdfViewerException
    {
        if (canvasDirty != TCanvasDirtyness.CanvasDirty)
            canvasDirty = TCanvasDirtyness.RectsDirty;
        pagesToLoadExactly.add(pageNo);
        calculateCanvas();
        return (Rectangle.Double) documentManager.getPage(pageNo).clone();
    }

    /**
     * returns a new rectangle containing the page specified by @pageNo unified
     * with any horizontal neighboring pages including borders
     * 
     * @param pageNo
     * @return
     * @throws PdfViewerException
     */
    @Override
    public Rectangle.Double getUnionRectangleWithNeighbour(int pageNo) throws PdfViewerException
    {
        Rectangle.Double pageRect = getPageRect(pageNo);
        switch (pageLayoutMode.horizontalScrollPosition(pageNo))
        {
        case -1:
            if (pageNo + 1 <= getPageCount())
            {
                pageRect = (Rectangle.Double) pageRect.createUnion(getPageRect(pageNo + 1));
            }
            break;
        case 1:
            if (pageNo - 1 > 0)
            {
                pageRect = (Rectangle.Double) pageRect.createUnion(getPageRect(pageNo - 1));
            }
            break;
        }
        pageRect.x -= getBorderSize();
        pageRect.width += 2.0 * getBorderSize();
        pageRect.y -= getBorderSize();
        pageRect.height += 2.0 * getBorderSize();
        return pageRect;
    }

    /**
     * Method called on open request.
     * 
     * @throws PdfViewerException
     * 
     * @throws PdfViewerException
     */
    @Override
    public void open(String filename, String password) throws PdfViewerException
    {
        setRotation(0);
        canvasDirty = TCanvasDirtyness.CanvasDirty;
        documentManager.open(filename, password);
    }

    /**
     * Method called on close request.
     */
    @Override
    public void close()
    {
        documentManager.close();
    }

    // TODO is that still required now that we have the
    // OnVisiblePageRangeChanged event?
    private int pageNo = 1;

    @Override
    public void setPageNo(int pageNo) throws PdfViewerException
    {
        this.pageNo = pageNo;
        if (!pageLayoutMode.isScrolling())
        {
            canvasDirty = TCanvasDirtyness.CanvasDirty;
        }
    }

    @Override
    public IPdfDocumentManager getDocumentManager()
    {
        return documentManager;
    }

    /**
     * Returns the canvas rectangle in user space units.
     * 
     * @return The canvas rectangle
     * @throws PdfViewerException
     */
    @Override
    public Rectangle.Double getCanvasRect() throws PdfViewerException
    {
        calculateCanvas();
        return canvasRect;
    }

    /**
     * @throws PdfViewerException
     * @see PdfDocument#getPageCount()
     */
    @Override
    public int getPageCount() throws PdfViewerException
    {
        return documentManager.getPageCount();
    }
    
    public boolean isOpen()
    {
        return documentManager.isOpen();
    }

    @Override
    public void draw(Dimension bitmapSize, int rotation, Map<Integer, Rectangle.Double> pageRects, PdfViewerController.Viewport viewport)
    {
        documentManager.draw(bitmapSize, rotation, pageRects, viewport);
    }

    public List<PdfTextFragment> GetTextWithinPageRange(int firstPage, int lastPage, double zoomFactor) throws PdfViewerException
    {
        return GetTextWithinRegion(new Rectangle.Integer(), firstPage, lastPage, zoomFactor);
    }

    public List<PdfTextFragment> GetTextWithinRegion(Rectangle.Integer markedRect, int firstPage, int lastPage, double zoomFactor)
            throws PdfViewerException
    {
        // the rectOnCanvas have to be precalculated
        List<PdfTextFragment> containedFragments = new ArrayList<PdfTextFragment>();
        // intersect the markedRect with the textFragments (transformed to
        // canvas coordinates)
        for (int page = firstPage; page <= lastPage; page++)
        {
            for (PdfTextFragment frag : documentManager.getTextFragments(page))
            {
                Rectangle.Double rectOnPage = frag.getRectOnUnrotatedPage();
                Rectangle.Double rectOnCanvas = PdfUtils.CalculateRectOnCanvas(rectOnPage, getPageRectGuaranteedExcactly(page), rotation);
                Rectangle rectOnPixel = PdfUtils.canvasToPixel(rectOnCanvas, zoomFactor);
                if (markedRect.isEmpty() || markedRect.intersects(rectOnPixel))
                    containedFragments.add(frag);
            }
        }

        return containedFragments;
    }

    @Override
    public void onVisiblePageRangeChanged(int firstPage, int lastPage)
    {
        if (canvasDirty != TCanvasDirtyness.CanvasDirty)
            canvasDirty = TCanvasDirtyness.RectsDirty;
        for (int i = firstPage; i <= lastPage; i++)
            pagesToLoadExactly.add(i);
    }

    @Override
    public void onItemLoadedToCache(Integer newlyLoadedPage)
    {
        if (canvasDirty != TCanvasDirtyness.CanvasDirty)
            canvasDirty = TCanvasDirtyness.RectsDirty;
        newlyLoadedPages.add(newlyLoadedPage);
    }

    private double getPreviousBottom(int page, int start, int end) throws PdfViewerException
    {
        if (page <= start)
            return 0.0;
        return documentManager.getPageGuess(page - 1).getMaxY();
    }

    private double getNextTop(int page, int start, int end) throws PdfViewerException
    {
        if (page >= end)
            return canvasRect.getMaxY();
        return documentManager.getPageGuess(page + 1).y;
    }

    private int getNextRelevantPage(int start, int end)
    {
        SortedSet<Integer> s1 = newlyLoadedPages.subSet(start, end + 1);
        int p1 = (s1.isEmpty()) ? Integer.MAX_VALUE : s1.first();
        SortedSet<Integer> s2 = pagesToLoadExactly.subSet(start, end + 1);
        int p2 = (s2.isEmpty()) ? Integer.MAX_VALUE : s2.first();
        return Math.min(p1, p2);
    }

    /**
     * Recalculates the locations of the pages on the canvas.
     * 
     * @throws PdfViewerException
     */
    private void calculateCanvas() throws PdfViewerException
    {
        if (documentManager.getPageCount() == 0 || canvasDirty == TCanvasDirtyness.Clean)
        {
            return;
        }

        int start = 1, end = documentManager.getPageCount();

        if (!pageLayoutMode.isScrolling())
        {
            switch (pageLayoutMode.horizontalScrollPosition(pageNo))
            {
            case -1:
            {
                start = pageNo;
                end = Math.min(pageNo + 1, end);
                break;
            }
            case 0:
            {
                start = pageNo;
                end = pageNo;
                break;
            }
            case 1:
            {
                start = pageNo - 1;
                end = pageNo;
                break;
            }
            }
        }

        Rectangle.Double oldCanvasRect = (Rectangle.Double) canvasRect.clone();
        boolean entireCanvasDirty = (canvasDirty == TCanvasDirtyness.CanvasDirty);
        if (entireCanvasDirty)
        {
            canvasRect.x = 0.0;
            canvasRect.width = 0.0;
        }

        canvasDirty = TCanvasDirtyness.Clean;
        double offset = 0.0;
        int changedPage = Integer.MAX_VALUE;
        int page = entireCanvasDirty ? start : Math.max(start, getNextRelevantPage(start, end));
        for (; page <= end; page++)
        {
            Rectangle.Double rect = null;
            if (pagesToLoadExactly.remove(page))
            {
                rect = documentManager.getPage(page);
            } else
            {
                rect = documentManager.getPageGuess(page);
            }
            SortedSet<Integer> s1 = newlyLoadedPages.subSet(start, end + 1);
            int nPage = (s1.isEmpty()) ? Integer.MAX_VALUE : s1.first();
            if (!entireCanvasDirty && nPage < page)
            {
                // maybe the getting of pages has getted a page before this one
                // (due to predictionAlgorithm). In this case, consider this
                // page first
                page = nPage;
                rect = documentManager.getPageGuess(page);
            }
            if (newlyLoadedPages.remove(page) || entireCanvasDirty)
            // If the page is newly loaded, calculate its position exactly. Also
            // if the entire canvas is dirty, calculate always
            {
                changedPage = Math.min(changedPage, page);
                // position the page newly, knowing that its the first time we
                // know its exact size
                switch (pageLayoutMode.horizontalScrollPosition(page))
                {
                case 0:
                {
                    // place 1 page
                    rect.x = -rect.width / 2.0;
                    rect.y = getPreviousBottom(page, start, end) + borderSize;
                    if (rect.x - borderSize < canvasRect.x)
                    {
                        canvasRect.width += canvasRect.x - (rect.x - borderSize);
                        canvasRect.x = rect.x - borderSize;
                    }
                    if (rect.getMaxX() + borderSize > canvasRect.getMaxX())
                    {
                        canvasRect.width += rect.getMaxX() + borderSize - canvasRect.getMaxX();
                    }
                    offset = rect.getMaxY() + borderSize - getNextTop(page, start, end);
                    break;
                }
                case -1:
                {
                    // place a page left
                    rect.x = 0.0 - borderSize / 2.0 - rect.width;
                    rect.y = Math.max(getPreviousBottom(page, start, end), getPreviousBottom(page - 1, start, end)) + borderSize;
                    if (rect.x - borderSize < canvasRect.x)
                    {
                        canvasRect.width += canvasRect.x - (rect.x - borderSize);
                        canvasRect.x = rect.x - borderSize;
                    }
                    offset = rect.y - getNextTop(page, start, end);
                    break;

                }
                case 1:
                {
                    // place a page right
                    rect.x = 0.0 + borderSize / 2.0;
                    rect.y = documentManager.getPageGuess(page - 1).y;
                    if (rect.getMaxX() + borderSize > canvasRect.getMaxX())
                    {
                        canvasRect.width += rect.getMaxX() + borderSize - canvasRect.getMaxX();
                    }
                    offset = Math.max(getPreviousBottom(page, start, end), rect.getMaxY()) + borderSize - getNextTop(page, start, end);
                    break;
                }
                }
            } else
            {
                // these pages are already positioned and only need updating if
                // there is an offset (other pages resizing might cause entire
                // rest of document to shift)
                if (offset == 0.0)
                {
                    // if there is no offset, we dont need to do anything until
                    // we hit a page that needs to be loaded anew
                    page = getNextRelevantPage(start, end);
                    if (page == Integer.MAX_VALUE)
                        break;// there is no more page to load -> stop iterating
                              // through pages
                    page--;
                    continue;
                }
                rect.y += offset;
            }
        }
        // we have loaded and positioned all pages, now we only need to resize
        // the canvas

        // There is already a border at the top, but bottom, left and right
        // border have to be added
        if (pageLayoutMode.horizontalScrollPosition(end) == -1)
            offset = documentManager.getPageGuess(end).getMaxY() + borderSize - canvasRect.getMaxY();
        if (offset != 0.0)
        {
            canvasRect.height += offset;
            controller.onCanvasRectChanged(oldCanvasRect, canvasRect, changedPage);
        }
        // visualizeCanvas();
        return;
    }

    /**
     * Draws a visual representation of the entire canvas (used for debugging)
     * 
     * @throws PdfViewerException
     */
    @SuppressWarnings("unused")
    private void visualizeCanvas() throws PdfViewerException
    {
        // init image
        BufferedImage image = new BufferedImage((int) canvasRect.width, (int) canvasRect.height, BufferedImage.TYPE_USHORT_GRAY);
        Graphics2D graphics = image.createGraphics();

        int start = 1, end = documentManager.getPageCount();
        if (!pageLayoutMode.isScrolling())
        {
            switch (pageLayoutMode.horizontalScrollPosition(pageNo))
            {
            case -1:
            {
                start = pageNo;
                end = Math.min(pageNo + 1, end);
                break;
            }
            case 0:
            {
                start = pageNo;
                end = pageNo;
                break;
            }
            case 1:
            {
                start = pageNo - 1;
                end = pageNo;
                break;
            }
            }
        }

        for (int pageNo = start; pageNo <= end; pageNo++)
        {
            Rectangle.Double rect = documentManager.getPageGuess(pageNo);
            graphics.fillRect((int) (rect.x - canvasRect.x), (int) (rect.y - canvasRect.y), (int) rect.width, (int) rect.height);
        }

        int x = (int) (canvasRect.width / 2.0);

        graphics.setColor(Color.LIGHT_GRAY);
        graphics.drawLine(x, 0, x, (int) canvasRect.height);

        try
        {
            File file = new File("E:\\tmp\\canvasSnapshots\\canvas" + canvasSnapshotNumber++ + ".png");
            ImageIO.write(image, "png", file);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public PdfTextWithinSelectionResult getTextWithinSelection(Point.Double start, Point.Double end, int firstPage, int lastPage)
            throws PdfViewerException
    {
        boolean swap = false;
        List<PdfTextFragment> containedFragments = new ArrayList<PdfTextFragment>();
        for (int page = firstPage; page <= lastPage; page++)
        {
            List<PdfTextFragment> frags = (List<PdfTextFragment>) documentManager.getTextFragments(page);
            if (frags.isEmpty())
            {
                continue;
            }
            PdfTextFragment firstFrag = frags.get(0);
            PdfTextFragment lastFrag = frags.get(frags.size() - 1);
            if (page == firstPage)
            {
                firstFrag = FindNearestTextFragment(start, frags, getPageRect(page));
            }
            if (page == lastPage)
            {
                lastFrag = FindNearestTextFragment(end, frags, getPageRect(page));
            }
            if (firstFrag == frags.get(0) && lastFrag == frags.get(frags.size() - 1))
            {
                containedFragments.addAll(frags);
            } else
            {
                int firstIndex = frags.indexOf(firstFrag);
                int lastIndex = frags.indexOf(lastFrag);
                if (firstIndex > lastIndex) // first and last are swapped
                {
                    swap = true;
                    containedFragments.addAll(frags.subList(lastIndex, firstIndex + 1));
                } else
                {
                    containedFragments.addAll(frags.subList(firstIndex, lastIndex + 1));
                }
            }
        }
        return new PdfTextWithinSelectionResult(containedFragments, swap ? end.x : start.x, swap ? start.x : end.x);
    }

    private PdfTextFragment FindNearestTextFragment(Point.Double location, List<PdfTextFragment> haystack, Rectangle.Double pageRect)
    {
        // figure out the closest one
        double minDist = Double.MAX_VALUE;
        PdfTextFragment minDistFrag = null;
        for (PdfTextFragment frag : haystack)
        {
            double dist = PdfUtils.ShortestDistanceSquared(frag.getRectOnUnrotatedPage(), location);
            if (dist < minDist)
            {
                minDist = dist;
                minDistFrag = frag;
            }
        }
        return minDistFrag;
    }

    private int canvasSnapshotNumber = 1;

    private Rectangle.Double canvasRect;

    public IPdfDocumentManager documentManager; // todo that shoudnt be public
    private TPageLayoutMode pageLayoutMode;

    private double borderSize = 10.0;
    private int rotation;
    private final static double MAX_BORDER_SIZE = 100.0;
    private final static double CM_TO_INCH = 0.393701;

    private TCanvasDirtyness canvasDirty = TCanvasDirtyness.CanvasDirty;
    private PdfViewerController controller;// That shouldn't be necessary

    private SortedSet<Integer> newlyLoadedPages;
    private SortedSet<Integer> pagesToLoadExactly;

}
