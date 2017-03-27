package com.pdf_tools.pdfviewer.converter.geom;


public interface IShape
{
    public Rectangle.Integer getBounds();

    public Rectangle.Double getBounds2D();

    public boolean contains(double x, double y);

    public boolean contains(Point p);

    public boolean intersects(double x, double y, double w, double h);

    public boolean intersects(Rectangle r);

    public boolean contains(double x, double y, double w, double h);

    public boolean contains(Rectangle r);
}
