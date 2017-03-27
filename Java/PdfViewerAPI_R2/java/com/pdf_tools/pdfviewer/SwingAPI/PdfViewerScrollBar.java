/***************************************************************************
 *
 * File:            PdfViewerScrollBar.java
 * 
 * Package:         3-Heights(TM) PDF Java Viewer
 *
 * Description:     The scrolling logic of the controller.
 *
 * @author          Christian Hagedorn, PDF Tools AG   
 * 
 * Copyright:       Copyright (C) 2001 - 2008 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *                  
 ***************************************************************************/

package com.pdf_tools.pdfviewer.SwingAPI;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JScrollBar;
import javax.swing.plaf.metal.MetalScrollBarUI;

import com.pdf_tools.pdfviewer.Model.DebugLogger;
import com.pdf_tools.pdfviewer.Model.PdfViewerController;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;
import com.pdf_tools.pdfviewer.converter.geom.Point;

/**
 * The PdfViewerScrollBar class defines the interface to handle the scrolling
 * logic of the controller. Scroll parameters are defined in pixels. The two
 * subclasses represent horizontal and vertical scrolling respectively.
 * 
 * @author cha
 *
 */
public abstract class PdfViewerScrollBar extends JScrollBar
{
    /**
     * Abstract constructor which creates a scroll bar with the specified
     * orientation (horizontal or vertical) and a reference to the controller.
     * 
     * @param orientation
     *            The orientation of the scroll bar
     * @param controller
     *            The controller reference.
     */
    PdfViewerScrollBar(int orientation, PdfViewerController controller)
    {
        super(orientation);
        this.controller = controller;
        setUnitIncrement(UNIT_INCREMENT);
        setBlockIncrement(BLOCK_INCREMENT);
        scrollPosition = 0;
        close();
    }

    public void close()
    {
        setEnabled(false);
    }

    /**
     * Sets the position of the thumb with respect to the viewport size in
     * pixels and the canvas size in pixels.
     */
    public abstract void update();

    /**
     * 
     */
    private static final long serialVersionUID = 95029561259469001L;

    protected PdfViewerController controller;
    protected int scrollPosition;

    protected static final int DPI = Toolkit.getDefaultToolkit().getScreenResolution();
    private static final int UNIT_INCREMENT = 20;
    private static final int BLOCK_INCREMENT = 40;
}

/**
 * UI Class to handle the size of the horizontal thumb of the scroll bar.
 * 
 * @author cha
 *
 */
class PdfViewerScrollBarUIHorizontal extends MetalScrollBarUI
{
    /**
     * Set the width of the thumb to <code>size</code>.
     */
    @Override
    protected Rectangle getThumbBounds()
    {
        return new Rectangle(super.getThumbBounds().x, super.getThumbBounds().y, size, super.getThumbBounds().height);
    }

    /**
     * Ensures that the width of the thumb is equal <code>size</code>.
     */
    @Override
    protected Dimension getMinimumThumbSize()
    {
        return new Dimension(size, size);
    }

    /**
     * Ensures that the width of the thumb is equal <code>size</code>.
     */
    @Override
    protected Dimension getMaximumThumbSize()
    {
        return new Dimension(size, size);
    }

    @Override
    protected Rectangle getTrackBounds()
    {
        return super.getTrackBounds();
    }

    public void setThumbSize(int size)
    {
        if (size < MIN_THUMB_SIZE)
            this.size = MIN_THUMB_SIZE;
        else
            this.size = size;
    }

    /** Defines the width of the thumb. */
    private int size;

    private static final int MIN_THUMB_SIZE = 20;
}

/**
 * UI Class to handle the size of the vertical thumb of the scroll bar.
 * 
 * @author cha
 *
 */
class PdfViewerScrollBarUIVertical extends MetalScrollBarUI
{
    /**
     * Set the height of the thumb to <code>size</code>.
     */
    @Override
    protected Rectangle getThumbBounds()
    {
        return new Rectangle(super.getThumbBounds().x, super.getThumbBounds().y, super.getThumbBounds().width, size);
    }

    /**
     * Ensures that the height of the thumb is equal <code>size</code>.
     */
    @Override
    protected Dimension getMinimumThumbSize()
    {
        return new Dimension(size, size);
    }

    /**
     * Ensures that the height of the thumb is equal <code>size</code>.
     */
    @Override
    protected Dimension getMaximumThumbSize()
    {
        return new Dimension(size, size);
    }

    @Override
    protected Rectangle getTrackBounds()
    {
        return super.getTrackBounds();
    }

    public void setThumbSize(int size)
    {
        if (size < MIN_THUMB_SIZE)
            this.size = MIN_THUMB_SIZE;
        else
            this.size = size;
    }

    /** Defines the height of the thumb. */
    private int size;

    private static final int MIN_THUMB_SIZE = 20;
}

/**
 * This class represents the horizontal scroll bar of the PDF viewer.
 * 
 * @author cha
 *
 */
class PdfViewerScrollBarHorizontal extends PdfViewerScrollBar
{

    /**
     * Creates a new horizontal scroll bar for the viewport window and the
     * underlying document.
     * 
     * @param controller
     *            The controller reference.
     */
    PdfViewerScrollBarHorizontal(PdfViewerController controller)
    {
        super(JScrollBar.HORIZONTAL, controller);
        setUI(ui);
        addAdjustmentListener(new AdjustmentListener()
        {
            public void adjustmentValueChanged(AdjustmentEvent e)
            {
                scrollPosition = e.getValue();
                try
                {
                    PdfViewerScrollBarHorizontal.this.controller.scrollTo(scrollPosition,
                            PdfViewerScrollBarHorizontal.this.controller.getViewportOrigin().y);
                } catch (PdfViewerException ex)
                {
                    DebugLogger.log(ex.toString());
                }
            }
        });

        addComponentListener(new ComponentListener()
        {

            public void componentShown(ComponentEvent e)
            {
            }

            public void componentResized(ComponentEvent e)
            {
                update();
                repaint();
            }

            public void componentMoved(ComponentEvent e)
            {
            }

            public void componentHidden(ComponentEvent e)
            {
            }
        });
    }

    @Override
    public void update()
    {
        try
        {
            if (controller.getPageCount() == 0)
            {
                setEnabled(false);
                return;
            }

            double size = ui.getTrackBounds().width
                    / ((controller.getCanvasSize().width * DPI / 72.0 * controller.getZoom()) / (controller.getWidth()));
            ui.setThumbSize((int) Math.floor(size));
            revalidate();
            repaint();

            if (controller.getCanvasSize().getWidth() * DPI / 72.0 * controller.getZoom() <= controller.getWidth())
            {
                setEnabled(false);
                return;
            } else
                setEnabled(true);

            int viewportPosition = controller.getViewportOrigin().x;
            int lowerBound = (int) Math.floor(controller.getCanvasSize().x * DPI / 72.0 * controller.getZoom());
            int upperBound = (int) Math
                    .floor((controller.getCanvasSize().x + controller.getCanvasSize().width) * DPI / 72.0 * controller.getZoom())
                    - controller.getWidth();

            if (viewportPosition > upperBound)
                viewportPosition = upperBound;
            else if (viewportPosition < lowerBound)
                viewportPosition = lowerBound;

            setValues(viewportPosition, 0, lowerBound, upperBound);
        } catch (PdfViewerException ex)
        {
            DebugLogger.log("Updated Failed: " + ex.toString());
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 8066066647455417150L;

    private PdfViewerScrollBarUIHorizontal ui = new PdfViewerScrollBarUIHorizontal();

}

/**
 * This class represents the vertical scroll bar of the PDF viewer.
 * 
 * @author cha
 *
 */
class PdfViewerScrollBarVertical extends PdfViewerScrollBar
{
    /**
     * Creates a new vertical scroll bar for the viewport window and the
     * underlying document.
     * 
     * @param controller
     *            The controller reference.
     */
    PdfViewerScrollBarVertical(PdfViewerController controller)
    {
        super(JScrollBar.VERTICAL, controller);
        setUI(ui);

        addAdjustmentListener(new AdjustmentListener()
        {
            public void adjustmentValueChanged(AdjustmentEvent e)
            {
                scrollPosition = e.getValue();
                setNewViewportPosition();
            }
        });

        addComponentListener(new ComponentListener()
        {

            public void componentShown(ComponentEvent e)
            {
            }

            public void componentResized(ComponentEvent e)
            {
                ;
                repaint();
            }

            public void componentMoved(ComponentEvent e)
            {
            }

            public void componentHidden(ComponentEvent e)
            {
            }
        });
    }

    public void update()
    {
        try
        {
            if (!controller.isOpen() || controller.getPageCount() == 0)
            {
                setEnabled(false);
                return;
            }

            double size = ui.getTrackBounds().height
                    / ((controller.getCanvasSize().height * DPI / 72.0 * controller.getZoom()) / (controller.getHeight()));
            ui.setThumbSize((int) Math.floor(size));
            revalidate();
            repaint();
            if (controller.getCanvasSize().getHeight() * DPI / 72.0 * controller.getZoom() <= controller.getHeight())
            {
                setEnabled(false);
                return;
            }
            setEnabled(true);

            int viewportPosition = controller.getViewportOrigin().y;
            int upperBound = (int) Math
                    .ceil((controller.getCanvasSize().height * DPI / 72.0 * controller.getZoom()) - controller.getHeight());
            if (controller.getPageLayoutMode().isScrolling() && viewportPosition > upperBound)
                viewportPosition = upperBound;

            setValues(viewportPosition, 0, 0, upperBound - 1);
        } catch (PdfViewerException ex)
        {
            DebugLogger.log("Scrollbar update failed: " + ex.toString());
        }
    }

    private void setNewViewportPosition()
    {
        try
        {
            Point.Integer viewport = PdfViewerScrollBarVertical.this.controller.getViewportOrigin();

            controller.scrollTo(viewport.x, scrollPosition);
        } catch (PdfViewerException ex)
        {
            DebugLogger.log(ex.toString());
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 4413042755433436276L;

    private PdfViewerScrollBarUIVertical ui = new PdfViewerScrollBarUIVertical();

}
