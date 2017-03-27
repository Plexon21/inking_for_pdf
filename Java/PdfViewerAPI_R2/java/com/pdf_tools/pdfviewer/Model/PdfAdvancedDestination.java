package com.pdf_tools.pdfviewer.Model;


import com.pdf_tools.pdfviewer.Model.IPdfCommon.TDestinationType;
import com.pdf_tools.pdfviewer.converter.geom.Rectangle;

public class PdfAdvancedDestination extends PdfDestination
{

    public PdfAdvancedDestination(int page, TDestinationType type, Double[] dimensions)
    {
        super(page, type, dimensions);
    }

    public PdfAdvancedDestination(PdfDestination destination)
    {
        super(destination.page, destination.type,
                new Double[] { destination.left, destination.top, destination.right, destination.bottom, destination.zoom });
    }

    /**
     * set location of page rectangle on canvas (only needed for getRect(),
     * getLeft(), getRight(), getTop() and getBottom() methods)
     */
    public void setPageRect(Rectangle.Double rect)
    {
        pageRect = rect;
    }

    /**
     * set the current viewport location (only needed for getRect(), getLeft(),
     * getRight(), getTop() and getBottom() methods)
     */
    public void setViewport(PdfViewerController.Viewport viewport)
    {
        Rectangle.Double rect = PdfUtils.viewportToCanvas(viewport.rectangle, viewport.getZoomFactor());
        left = Double.isNaN(left) ? rect.x : left;
        top = Double.isNaN(top) ? -32768 : top;
        right = Double.isNaN(right) ? rect.x + rect.width : right;
        bottom = Double.isNaN(bottom) ? 0.0 : bottom;
        zoom = (Double.isNaN(zoom) || zoom == 0.0) ? viewport.getZoomFactor() : zoom;
    }

    /**
     * @return x-coordinate of left end of destination rectangle
     */
    public int getLeft()
    {
        return PdfUtils.canvasToPixel(clip(sanityCheckNumber(left), pageRect.getMaxX()), zoom);
    }

    /**
     * @return x-coordinate of right end of destination rectangle
     */
    public int getRight()
    {
        return PdfUtils.canvasToPixel(clip(sanityCheckNumber(right), pageRect.getMaxX()), zoom);
    }

    /**
     * @return y-coordinate of top of destination rectangle
     */
    public int getTop()
    {
        return PdfUtils.canvasToPixel(clip(pageRect.height - sanityCheckNumber(top), pageRect.getMaxY()), zoom);
    }

    /**
     * @return y-coordinate of bottom of destination rectangle
     */
    public int getBottom()
    {
        return PdfUtils.canvasToPixel(clip(pageRect.height - sanityCheckNumber(bottom), pageRect.getMaxY()), zoom);
    }

    /**
     * @return destination rectangle
     */
    public Rectangle.Integer getRect()
    {
        return new Rectangle.Integer(getLeft(), getTop(), getRight() - getLeft(), getBottom() - getTop());
    }

    protected double clip(double toClip, double max)
    {
        return Math.max(0.0, Math.min(max, toClip));
    }

    protected double sanityCheckNumber(double number)
    {
        if (number == -32768)
        {
            return pageRect.height;// special case for some reason (behaviour
                                   // like adobe reader)
        } else if (Math.abs(number) >= 32768)
        {
            throw new IllegalArgumentException("Destination " + number + " is out of range");
        }
        return number;
    }

    private Rectangle.Double pageRect;
}
