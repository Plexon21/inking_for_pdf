/***************************************************************************
 *
 * File:            PdfDocument.java
 * 
 * Package:         3-Heights(TM) PDF Java Viewer
 *
 * Description:     The PDF Document description.
 *
 * @author          Christian Hagedorn, PDF Tools AG   
 * 
 * Copyright:       Copyright (C) 2001 - 2008 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *                  
 ***************************************************************************/

package com.pdf_tools.pdfviewer.DocumentManagement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.pdf_tools.pdfviewer.Model.PdfAdvancedDestination;
import com.pdf_tools.pdfviewer.Model.PdfOutlineItem;
import com.pdf_tools.pdfviewer.Model.PdfTextFragment;
import com.pdf_tools.pdfviewer.Model.PdfUtils;
import com.pdf_tools.pdfviewer.Model.PdfViewerController;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;
import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation;
import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation.TPdfAnnotationType;
import com.pdf_tools.pdfviewer.Annotations.PdfGenericAnnotation;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.TDestinationType;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.TPageLayoutMode;
import com.pdf_tools.pdfviewer.Model.PdfViewerException.*;
import com.pdf_tools.pdfviewer.converter.geom.Rectangle;

/**
 * The PdfDocument class represents the underlying document with access to all
 * pages.
 * 
 * @author cha
 *
 */
public final class PdfDocument extends com.pdf_tools.pdfviewer.Model.NativeObject implements IPdfDocument
{
    /**
     * Creates an empty document.
     */
    PdfDocument()
    {
        pageCountCache = -1;
        isOpen = false;
    }

    public void open(String filename, String password) throws PdfViewerException
    {
        if (isOpen)
        {
            close();
        }

        _documentHandle = createObject(filename, password);

        if (_documentHandle == 0)
            throw getLastException();
        readPageCount();
        if (pageCountCache <= 0)
        {
            throw new PdfViewerException("The file could be opened, but contains no pages");
        }
        lastBitmap = null;
        isOpen = true;
    }

    public void close() throws PdfViewerException
    {
        if (!isOpen)
        {
            return;
        }
        isOpen = false;

        if (_documentHandle != 0)
            destroyObject(_documentHandle);

        _documentHandle = 0;
        pageCountCache = 0;
    }

    public void saveAs(String path) throws PdfViewerException
    {
        if (!isOpen)
        {
            return;
        }
        boolean successful = false;
        if (_documentHandle != 0)
            successful = saveAs(_documentHandle, path);

        if (!successful)
            throw getLastException();

    }

    public int getPageCount() throws NoFileOpenedException
    {
        if (!isOpen)
            throw new NoFileOpenedException();
        return pageCountCache;
    }

    private void readPageCount()
    {
        if (_documentHandle == 0)
            pageCountCache = 0;
        else
            pageCountCache = getPageCount(_documentHandle);
    }

    @Override
    public Map<Integer, Rectangle.Double> getPageRangeFromSource(List<Integer> pages) throws PdfViewerException
    {
        Map<Integer, Rectangle.Double> pageRects = new HashMap<Integer, Rectangle.Double>(pages.size());
        for (Integer page : pages)
        {
            Rectangle.Double pageRect = getPageRect(_documentHandle, (int) page);
            int rot = getRotation(_documentHandle, (int) page) + _rotation;
            if (rot % 180 != 0)
            {
                pageRect = new Rectangle.Double(0.0, 0.0, pageRect.height, pageRect.width);
            }
            if (pageRect.width <= 0.0 || pageRect.height <= 0.0)
            {
                throw new PdfViewerException.PdfFileNotFoundException(getLastErrorMessage());
            }
            pageRects.put(page, pageRect);
        }
        return pageRects;
    }

    @Override
    public PdfGenericAnnotation[] getAnnotationsOnPage(int pageNo) throws PdfViewerException
    {
        return getAnnotationsOnPage(_documentHandle, pageNo);
    }

    public void updateAnnotation(APdfAnnotation annotation) throws PdfViewerException
    {
        // TODO: more efficient repainting of the page
        lastVisiblePages.clear();
        int page = updateAnnotation(_documentHandle, annotation);
        if (page < 0)
            throw getLastException();
    }

    @Override
    public PdfGenericAnnotation createAnnotation(TPdfAnnotationType type, int page, Double[] rect) throws PdfViewerException
    {
        lastVisiblePages.clear();
        return createAnnotation(_documentHandle, type, page, rect);
    }

    @Override
    public void deleteAnnotation(long annotHandle, int page) throws PdfViewerException
    {
        lastVisiblePages.clear();
        deleteAnnotation(_documentHandle, annotHandle, page);
    }

    public boolean isOpen()
    {
        return isOpen;
    }

    /// <returns>Whether the page has been drawn or queued to lists for drawing.
    /// Returns false if no valid information about previous
    /// drawRequest</returns>
    private boolean CalculateSourceTargetRects(BufferedImage bitmap, int rotation, int page, Rectangle.Double pageRect,
            List<Integer> pages, List<Rectangle.Double> sourceRects, List<Rectangle.Integer> targetRects, Rectangle.Double visibleRectOnPage,
            PdfViewerController.Viewport viewport)
    {
        if (!lastVisiblePages.contains(page) || visibleRectOnPage.isEmpty())
            return false;

        // calculate the area to copy
        int lastIndex = lastVisiblePages.indexOf(page);
        Rectangle.Double reusableRectOnPage = PdfUtils.IntersectSourceRects(lastVisibleRectOnPages.get(lastIndex), visibleRectOnPage);
        Rectangle.Integer reusableTargetRect, reusableTargetRectOnLastBitmap;
        int numsteps = 0;
        while (true)
        {
            reusableTargetRect = GetTargetRect(reusableRectOnPage, pageRect, viewport);
            reusableTargetRectOnLastBitmap = GetTargetRect(reusableRectOnPage, lastPageRects.get(lastIndex), lastViewport);
            if (reusableTargetRectOnLastBitmap.isEmpty())
                return false; // if we cant reuse anything, there is nothing to
                              // do
            if (reusableTargetRect.width != reusableTargetRectOnLastBitmap.width
                    || reusableTargetRect.height != reusableTargetRectOnLastBitmap.height)
            {
                if (numsteps > 100)
                    return false;
                // Console.WriteLine("Inconsistent reusableTargetRect sizes: {0}
                // vs. {1} and {2} vs. {3}", reusableTargetRect.iWidth,
                // reusableTargetRectOnLastBitmap.iWidth,
                // reusableTargetRect.iHeight,
                // reusableTargetRectOnLastBitmap.iHeight);
                PdfUtils.ShrinkSourceRect(reusableRectOnPage, 0.99);
                numsteps++;

            } else
                break;
        }

        reusedTargetRects.add(reusableTargetRect);

        // read from last bitmap
        BufferedImage reusableImage = lastBitmap.getSubimage(reusableTargetRectOnLastBitmap.x, reusableTargetRectOnLastBitmap.y,
                reusableTargetRectOnLastBitmap.width, reusableTargetRectOnLastBitmap.height);

        Graphics2D g = bitmap.createGraphics();
        g.drawImage(reusableImage, reusableTargetRect.x, reusableTargetRect.y, reusableTargetRect.width, reusableTargetRect.height, null);
        g.dispose();

        // calculate rects that need to be drawn newly
        List<Rectangle.Integer> targetDifs = subtractRects(GetTargetRect(pageRect, viewport), reusableTargetRect);
        for (int i = 0; i < 4; i++)
        {
            Rectangle.Integer targetRect = targetDifs.get(i);
            if (targetRect.isEmpty())
                continue;
            Rectangle.Double sourceRect = GetSourceFromTargetRect(targetRect, pageRect, viewport);
            targetRects.add(targetRect);
            sourceRects.add(sourceRect);
            pages.add(page);
        }
        return true;
    }

    /**
     * @param minuend
     *            the rectangle, from which another is being subtracted
     * @param subtrahend
     *            the Rectangle.Integer that gets subtracted
     * @return a list of 4 rectangles. The disjoint union of these plus the
     *         subtrahend result in the minuend
     * 
     *         picture, with origin in top/left, but works with other coords
     *         still
     * @formatter:off
     *         |---------------------------------| 
     *         |               r1                |
     *         |---------------------------------|
     *         |   r2   |  subtrahend   |   r4   |
     *         |---------------------------------|
     *         |               r3                |
     *         |---------------------------------|
     * @formatter:on
     */
    private List<Rectangle.Integer> subtractRects(Rectangle.Integer minuend, Rectangle.Integer subtrahend)
    {
        subtrahend = subtrahend.intersection(minuend);
        List<Rectangle.Integer> differences = new ArrayList<Rectangle.Integer>();
        differences.add(new Rectangle.Integer(minuend.x, minuend.y, minuend.width, subtrahend.y - minuend.y));
        differences.add(new Rectangle.Integer(minuend.x, subtrahend.y, subtrahend.x - minuend.x, subtrahend.height));
        differences.add(new Rectangle.Integer(minuend.x, (int) subtrahend.getMaxY(), minuend.width, (int) minuend.getMaxY() - (int) subtrahend.getMaxY()));
        differences.add(new Rectangle.Integer((int) subtrahend.getMaxX(), subtrahend.y, (int) minuend.getMaxX() - (int) subtrahend.getMaxX(), subtrahend.height));
        return differences;
    }

    private Rectangle.Double GetSourceFromTargetRect(Rectangle.Integer targetRect, Rectangle.Double pageRect,
            PdfViewerController.Viewport viewport)
    {
        Rectangle.Integer targetClone = new Rectangle.Integer(targetRect.x + viewport.rectangle.x, targetRect.y + viewport.rectangle.y, targetRect.width,
                targetRect.height);
        Rectangle.Double sourceRect = PdfUtils.viewportToCanvas(targetClone, viewport.getZoomFactor());
        sourceRect.x -= pageRect.x;
        sourceRect.y -= pageRect.y;
        // transform to botleft origin
        sourceRect.y = pageRect.height - sourceRect.y - sourceRect.height;
        return sourceRect;
    }

    private Rectangle.Integer GetTargetRect(Rectangle.Double pageRect, PdfViewerController.Viewport viewport)
    {
        return GetTargetRect(null, pageRect, viewport);
    }

    // we ought to ensure, that the size of the resulting target rect is
    // independent of the viewport offset
    // the problem is, that sometimes the rectOnPage is not entirely within the
    // viewport and it thus gets cropped a bit by one of the viewports, yielding
    // inconsistent size
    private Rectangle.Integer GetTargetRect(Rectangle.Double rectOnPage, Rectangle.Double pageRect, PdfViewerController.Viewport viewport)
    {
        Rectangle.Double pageSubRect;
        if (rectOnPage == null)
            pageSubRect = pageRect;
        else
        {
            pageSubRect = new Rectangle.Double(rectOnPage.x + pageRect.x, rectOnPage.y + pageRect.y, rectOnPage.width, rectOnPage.height);
        }
        Rectangle.Integer targetRect = PdfUtils.canvasToPixel(pageSubRect, viewport.getZoomFactor());
        targetRect = targetRect.intersection(viewport.rectangle);
        targetRect.x -= viewport.rectangle.x;
        targetRect.y -= viewport.rectangle.y;
        return targetRect;
    }

    @Override
    public void draw(BufferedImage bitmap, int rotation, Map<Integer, Rectangle.Double> pageRectsDict,
            PdfViewerController.Viewport viewport) throws PdfViewerException
    {
        // draw background gray
        Graphics2D g2 = (Graphics2D) bitmap.getGraphics();
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        List<Rectangle.Double> sourceRects = new ArrayList<Rectangle.Double>();
        List<Rectangle.Integer> targetRects = new ArrayList<Rectangle.Integer>();
        List<Integer> pages = new ArrayList<Integer>(); // The list of
                                                        // pagenumbers for each
                                                        // s/t rect that needs
                                                        // to be drawn
        List<Integer> newPages = new ArrayList<Integer>(); // pages that are
                                                           // completely new
                                                           // drawn and nothing
                                                           // is reused
        List<Rectangle.Double> pageRects = new ArrayList<Rectangle.Double>();
        List<Rectangle.Double> visibleRectOnPages = new ArrayList<Rectangle.Double>();
        reusedTargetRects = new ArrayList<Rectangle.Integer>();

        // Use information in pageRectsDict etc. to fill in the lists for
        // iterating later (sourcerects etc.)
        for (Map.Entry<Integer, Rectangle.Double> entry : pageRectsDict.entrySet())
        {
            Rectangle.Double pageRect = entry.getValue();
            int page = entry.getKey();
            double zoomFactor = viewport.getZoomFactor();

            // crop the page with viewport
            Rectangle.Integer visibleTargetRect = PdfUtils.canvasToPixel(pageRect, zoomFactor).intersection(viewport.rectangle);
            Rectangle.Double visibleRectOnPage = PdfUtils.viewportToCanvas(visibleTargetRect, zoomFactor);
            visibleRectOnPage.x -= pageRect.x;
            visibleRectOnPage.y -= pageRect.y;

            boolean insertedRectanglesToDraw = false;
            if (lastBitmap != null && lastRotation == rotation && Math.abs(lastViewport.getZoomFactor() / zoomFactor - 1.0) < 0.01)
                insertedRectanglesToDraw = CalculateSourceTargetRects(bitmap, rotation, page, pageRect, pages, sourceRects, targetRects,
                        visibleRectOnPage, viewport);
            if (!insertedRectanglesToDraw)
            {
                // i just have to do the same thing in here as
                // calculateSourceTargetRects would do
                Rectangle.Integer targetRect = GetTargetRect(visibleRectOnPage, pageRect, viewport);
                sourceRects.add(GetSourceFromTargetRect(targetRect, pageRect, viewport));
                targetRects.add(targetRect);
                pages.add(page);
                newPages.add(page);
            }
            pageRects.add(pageRect);
            visibleRectOnPages.add(visibleRectOnPage);
        }

        // iterate through everything that needs to be drawn
        byte[] bufferData = ((DataBufferByte) bitmap.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < pages.size(); i++)
        {
            int pageNo = pages.get(i);
            Rectangle.Double s = sourceRects.get(i);
            Rectangle.Integer t = targetRects.get(i);
            if (t.isEmpty())
                continue;

            boolean succeded = draw(_documentHandle, pageNo, bitmap.getWidth(), bitmap.getHeight(), bufferData, rotation, t.x, t.y, t.width,
                    t.height, s.x, s.y, s.width, s.height);
            if (!succeded)
            {
                throw new PdfViewerException(getLastErrorMessage());
            }
        }

        lastBitmap = bitmap;
        lastVisiblePages.clear();
        lastVisiblePages.addAll(pageRectsDict.keySet());
        lastPageRects = pageRects;
        lastRotation = rotation;
        lastViewport = viewport;
        lastVisibleRectOnPages = visibleRectOnPages;

        // visualizeDraw(bitmap, pages, targetRects, newPages);
    }

    private BufferedImage lastBitmap = null;
    private int lastRotation;
    private List<Integer> lastVisiblePages = new ArrayList<Integer>();
    private List<Rectangle.Double> lastVisibleRectOnPages;
    private List<Rectangle.Double> lastPageRects;
    private List<Rectangle.Integer> reusedTargetRects;
    private PdfViewerController.Viewport lastViewport;

    private void visualizeDraw(BufferedImage bitmap, List<Integer> pages, List<Rectangle.Integer> targetRects, List<Integer> newPages)
    {
        ColorModel cm = bitmap.getColorModel();
        BufferedImage bmp = new BufferedImage(cm, bitmap.copyData(null), cm.isAlphaPremultiplied(), null);
        Graphics2D g = bmp.createGraphics();
        for (Rectangle.Integer t : targetRects)
        {
            if (t.isEmpty())
                continue;
            int page = pages.get(targetRects.indexOf(t));
            g.setColor(new Color(255, 0, 128, 64));
            if (newPages.contains(page))
            {
                g.setColor(new Color(0, 64, 255, 64));
            }
            g.fillRect(t.x, t.y, t.width, t.height);
            g.drawString(Integer.toString(page), t.x, t.y);
        }
        g.setColor(new Color(255, 128, 0, 64));
        for (Rectangle.Integer t : reusedTargetRects)
        {
            g.fillRect(t.x, t.y, t.width, t.height);
        }
        File file = new File("P://temp//bitmapjava//bitmap" + visualizeCount + ".png");
        visualizeCount++;
        try
        {
            ImageIO.write(bmp, "png", file);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private int visualizeCount = 0;

    @Override
    public BufferedImage LoadThumbnail(double sourceWidth, double sourceHeight, int targetWidth, int targetHeight, int page)
            throws PdfViewerException
    {
        if (!isOpen)
        {
            throw new NoFileOpenedException();
        }

        BufferedImage bitmap = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_4BYTE_ABGR);
        DataBufferByte buf = ((DataBufferByte) bitmap.getRaster().getDataBuffer());

        boolean succeded = draw(_documentHandle, page, bitmap.getWidth(), bitmap.getHeight(), buf.getData(), 0, 0, 0, targetWidth,
                targetHeight, 0.0, 0.0, sourceWidth, sourceHeight);

        return bitmap;
    }

    public TPageLayoutMode getPageLayout() throws PdfViewerException
    {
        if (!isOpen)
        {
            throw new NoFileOpenedException();
        }

        int mode = getPageLayout(_documentHandle);
        TPageLayoutMode layout = TPageLayoutMode.values()[mode];
        return layout;
    }

    public PdfAdvancedDestination getOpenActionDestination() throws PdfViewerException
    {
        if (!isOpen)
        {
            throw new NoFileOpenedException();
        }

        int[] page = new int[1];
        Double[] dimensions = new Double[] { Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN };
        TDestinationType type = TDestinationType.values()[getOpenActionDestination(_documentHandle, page, dimensions)];
        return new PdfAdvancedDestination(page[0], type, dimensions);
    }

    public PdfOutlineItem[] getOutlines(int parentId) throws PdfViewerException
    {
        if (!isOpen)
        {
            throw new NoFileOpenedException();
        }

        PdfOutlineItem items[] = getOutlineItems(_documentHandle, parentId);
        return items;
    }

    public List<PdfTextFragment> getTextFragments(int pageNo)
    {
        PdfTextFragment[] fragments = getTextFragments(_documentHandle, pageNo);
        return Arrays.asList(fragments);
    }

    private long _documentHandle;
    private int pageCountCache;

    private boolean isOpen = false;;
    private int _rotation;

}
