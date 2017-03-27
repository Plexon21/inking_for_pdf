package com.pdf_tools.pdfviewer.Model;

import com.pdf_tools.pdfviewer.Model.IPdfCommon.TDestinationType;

/**
 * @author fwe Class determining a specific rectangle within a pdf document,
 *         that can be used for navigation
 */
public class PdfDestination
{
    /**
     * @param page
     *            the page the destination is on
     * @param type
     *            the type of destination this is as a integer
     * @param dimensions
     *            array of 4 doubles specifying offsets and zooms {left, top,
     *            right, bottom, zoom} Note that the required dimensions
     *            parameters vary depending on the destination type
     */
    public PdfDestination(int page, int type, Double[] dimensions)
    {
        this(page, TDestinationType.values()[type], dimensions);
    }

    /**
     * @param page
     *            the page the destination is on
     * @param type
     *            the type of destination this is
     * @param dimensions
     *            array of 4 doubles specifying offsets and zooms {left, top,
     *            right, bottom, zoom} Note that the required dimensions
     *            parameters vary depending on the destination type
     */
    public PdfDestination(int page, TDestinationType type, Double[] dimensions)
    {
        this.page = page;
        this.type = type;
        this.left = dimensions[0];
        this.top = dimensions[1];
        this.right = dimensions[2];
        this.bottom = dimensions[3];
        this.zoom = dimensions[4];
    }

    @Override
    public String toString()
    {
        return "{" + page + ", " + type + ", " + left + ", " + top + ", " + right + ", " + bottom + ", " + zoom + "}";
    }

    /**
     * @return page number of destination
     */
    public int getPage()
    {
        return page;
    }

    /**
     * @return type of destination
     */
    public TDestinationType getType()
    {
        return type;
    }

    /**
     * @return zoom of destination
     */
    public double getZoom()
    {
        return zoom;
    }

    protected int page;
    protected TDestinationType type;
    protected Double left;
    protected Double top;
    protected Double right;
    protected Double bottom;
    protected Double zoom;
}
