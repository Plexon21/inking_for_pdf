package com.pdf_tools.pdfviewer.converter;

import com.pdf_tools.pdfviewer.converter.geom.Point;
import com.pdf_tools.pdfviewer.converter.geom.Rectangle;

public class Converter
{
    
    /**
     * Create a Point.Double object from Point.Double
     * @param p Point.Double
     * @return new Point.Double
     */
    public static java.awt.geom.Point2D.Double createAWTPoint2D(Point.Double p)
    {
        return new java.awt.geom.Point2D.Double(p.x, p.y);
    }
    
    /**
     * Create a Point.Double object from awt.geom.Point2D
     * @param p Point.Double
     * @return new Point.Double
     */
    public static Point.Double createPointD(java.awt.geom.Point2D.Double p)
    {
        return new Point.Double(p.x, p.y);
    }
    
    /**
     * Create a java.awt.Point.Integer object from Point.Integer
     * @param p Point.Integer
     * @return new java.awt.Point
     */
    public static java.awt.Point createAWTPoint(Point.Integer p)
    {
        return new java.awt.Point(p.x, p.y);
    }
    
    /**
     * Create a <code>Point.Integer</code> object from <code>java.awt.Point</code>
     * @param p java.awt.Point
     * @return new Point.Integer
     */
    public static Point.Integer createPointI(java.awt.Point p)
    {
        return new Point.Integer(p.x, p.y);
    }
    
    /**
     * Create <code>java.awt.Rectangle</code> from <code>Rectangle.Integer</code>
     * @param r <code>Rectangle.Integer</code>
     * @return new <code>java.awt.Rectangle</code>
     */
    public static java.awt.Rectangle createAWTRect(Rectangle.Integer r)
    {
        return new java.awt.Rectangle(r.x, r.y, r.width, r.height);
    }
    
    /**
     * Create <code>java.awt.Color</code> from <code>Color</code>
     * @param c an object of the custom implementation of <code>Color</code>
     * @return new <code>java.awt.Color</code>
     */
    public static java.awt.Color createAWTColor(Color c)
    {
        return new java.awt.Color(c.frgbvalue[0], c.frgbvalue[1], c.frgbvalue[2], c.falpha);
    }
    
}
