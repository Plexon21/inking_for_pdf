package com.pdf_tools.pdfviewer.geom;

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public abstract class RectangularShape implements IShape, Cloneable
{

    /**
     * This is an abstract class that cannot be instantiated directly.
     */
    protected RectangularShape() {
    }

    /**
     * Returns the X coordinate of the upper-left corner of
     * the framing rectangle in <code>double</code> precision.
     * @return the X coordinate of the upper-left corner of
     * the framing rectangle.
     */
    public abstract double getX();

    /**
     * Returns the Y coordinate of the upper-left corner of
     * the framing rectangle in <code>double</code> precision.
     * @return the Y coordinate of the upper-left corner of
     * the framing rectangle.
     */
    public abstract double getY();

    /**
     * Returns the width of the framing rectangle in
     * <code>double</code> precision.
     * @return the width of the framing rectangle.
     */
    public abstract double getWidth();

    /**
     * Returns the height of the framing rectangle
     * in <code>double</code> precision.
     * @return the height of the framing rectangle.
     */
    public abstract double getHeight();
    
    /**
     * Returns the framing {@link Rectangle2D}
     * that defines the overall shape of this object.
     * @return a <code>Rectangle2D</code>, specified in
     * <code>double</code> coordinates.
     */
    public Rectangle getFrame() {
        return new Rectangle.Double(getX(), getY(), getWidth(), getHeight());
    }

    /**
     * Returns the smallest X coordinate of the framing
     * rectangle of the <code>Shape</code> in <code>double</code>
     * precision.
     * @return the smallest X coordinate of the framing
     *          rectangle of the <code>Shape</code>.
     */
    public double getMinX() {
        return getX();
    }

    /**
     * Returns the smallest Y coordinate of the framing
     * rectangle of the <code>Shape</code> in <code>double</code>
     * precision.
     * @return the smallest Y coordinate of the framing
     *          rectangle of the <code>Shape</code>.
     * @since 1.2
     */
    public double getMinY() {
        return getY();
    }

    /**
     * Returns the largest X coordinate of the framing
     * rectangle of the <code>Shape</code> in <code>double</code>
     * precision.
     * @return the largest X coordinate of the framing
     *          rectangle of the <code>Shape</code>.
     * @since 1.2
     */
    public double getMaxX() {
        return getX() + getWidth();
    }

    /**
     * Returns the largest Y coordinate of the framing
     * rectangle of the <code>Shape</code> in <code>double</code>
     * precision.
     * @return the largest Y coordinate of the framing
     *          rectangle of the <code>Shape</code>.
     * @since 1.2
     */
    public double getMaxY() {
        return getY() + getHeight();
    }

    /**
     * Returns the X coordinate of the center of the framing
     * rectangle of the <code>Shape</code> in <code>double</code>
     * precision.
     * @return the X coordinate of the center of the framing rectangle
     *          of the <code>Shape</code>.
     * @since 1.2
     */
    public double getCenterX() {
        return getX() + getWidth() / 2.0;
    }

    /**
     * Returns the Y coordinate of the center of the framing
     * rectangle of the <code>Shape</code> in <code>double</code>
     * precision.
     * @return the Y coordinate of the center of the framing rectangle
     *          of the <code>Shape</code>.
     * @since 1.2
     */
    public double getCenterY() {
        return getY() + getHeight() / 2.0;
    }

    /**
     * Determines whether the <code>RectangularShape</code> is empty.
     * When the <code>RectangularShape</code> is empty, it encloses no
     * area.
     * @return <code>true</code> if the <code>RectangularShape</code> is empty;
     *          <code>false</code> otherwise.
     * @since 1.2
     */
    public abstract boolean isEmpty();

    /**
     * Sets the location and size of the framing rectangle of this
     * <code>Shape</code> to the specified rectangular values.
     *
     * @param x the X coordinate of the upper-left corner of the
     *          specified rectangular shape
     * @param y the Y coordinate of the upper-left corner of the
     *          specified rectangular shape
     * @param w the width of the specified rectangular shape
     * @param h the height of the specified rectangular shape
     * @see #getFrame
     * @since 1.2
     */
    public abstract void setFrame(double x, double y, double w, double h);




    /**
     * Sets the diagonal of the framing rectangle of this <code>Shape</code>
     * based on the two specified coordinates.  The framing rectangle is
     * used by the subclasses of <code>RectangularShape</code> to define
     * their geometry.
     *
     * @param x1 the X coordinate of the start point of the specified diagonal
     * @param y1 the Y coordinate of the start point of the specified diagonal
     * @param x2 the X coordinate of the end point of the specified diagonal
     * @param y2 the Y coordinate of the end point of the specified diagonal
     * @since 1.2
     */
    public void setFrameFromDiagonal(double x1, double y1,
                                     double x2, double y2) {
        if (x2 < x1) {
            double t = x1;
            x1 = x2;
            x2 = t;
        }
        if (y2 < y1) {
            double t = y1;
            y1 = y2;
            y2 = t;
        }
        setFrame(x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * Sets the framing rectangle of this <code>Shape</code>
     * based on the specified center point coordinates and corner point
     * coordinates.  The framing rectangle is used by the subclasses of
     * <code>RectangularShape</code> to define their geometry.
     *
     * @param centerX the X coordinate of the specified center point
     * @param centerY the Y coordinate of the specified center point
     * @param cornerX the X coordinate of the specified corner point
     * @param cornerY the Y coordinate of the specified corner point
     * @since 1.2
     */
    public void setFrameFromCenter(double centerX, double centerY,
                                   double cornerX, double cornerY) {
        double halfW = Math.abs(cornerX - centerX);
        double halfH = Math.abs(cornerY - centerY);
        setFrame(centerX - halfW, centerY - halfH, halfW * 2.0, halfH * 2.0);
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(Point p) {
        return contains(p.getX(), p.getY());
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean intersects(Rectangle r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(Rectangle r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public Rectangle.Integer getBounds() {
        double width = getWidth();
        double height = getHeight();
        if (width < 0 || height < 0) {
            return new Rectangle.Integer();
        }
        double x = getX();
        double y = getY();
        double x1 = Math.floor(x);
        double y1 = Math.floor(y);
        double x2 = Math.ceil(x + width);
        double y2 = Math.ceil(y + height);
        return new Rectangle.Integer((int) x1, (int) y1,
                                      (int) (x2 - x1), (int) (y2 - y1));
    }

    /**
     * Creates a new object of the same class and with the same
     * contents as this object.
     * @return     a clone of this instance.
     * @exception  OutOfMemoryError            if there is not enough memory.
     * @see        java.lang.Cloneable
     * @since      1.2
     */
    public abstract Object clone();

}
