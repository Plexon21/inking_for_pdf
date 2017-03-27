package com.pdf_tools.pdfviewer.converter.geom;

/**
 * Custom implementation of the awt classes for <code>java.awt.geom.Rectangle2D.Double</code> and <code>java.awt.Rectangle</code> 
 * They implement the basic methods of their awt counter parts but not all of them. The implementations of the the functions
 * are following the awt implementations.
 */
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class Rectangle extends RectangularShape
{
    public static class Integer extends Rectangle
    {
        public int x;
        public int y;
        public int width;
        public int height;
        
        public Integer()
        {
            this(0, 0, 0, 0);
        }
        
        public Integer(int x, int y, int w, int h)
        {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }
        
        @Override
        public double getHeight()
        {
            return height;
        }
        
        @Override
        public double getWidth()
        {
            return width;
        }
        
        @Override
        public double getX()
        {
            return x;
        }
        
        @Override
        public double getY()
        {
            return y;
        }
                
        public Rectangle createUnion(Rectangle r) {
            Rectangle dest = new Rectangle.Integer();
            Rectangle.union(this, r, dest);
            return dest;
        }

        @Override
        public boolean isEmpty()
        {
            return (width <= 0) || (height <= 0);
        }

        @Override
        public void setFrame(double x, double y, double w, double h)
        {
            this.x = (int) x;
            this.y = (int) y;
            this.width = (int) w;
            this.height = (int) h;
        }

        public Integer intersection(Integer r) {
            int tx1 = this.x;
            int ty1 = this.y;
            int rx1 = r.x;
            int ry1 = r.y;
            long tx2 = tx1; tx2 += this.width;
            long ty2 = ty1; ty2 += this.height;
            long rx2 = rx1; rx2 += r.width;
            long ry2 = ry1; ry2 += r.height;
            if (tx1 < rx1) tx1 = rx1;
            if (ty1 < ry1) ty1 = ry1;
            if (tx2 > rx2) tx2 = rx2;
            if (ty2 > ry2) ty2 = ry2;
            tx2 -= tx1;
            ty2 -= ty1;
            // tx2,ty2 will never overflow (they will never be
            // larger than the smallest of the two source w,h)
            // they might underflow, though...
            if (tx2 < java.lang.Integer.MIN_VALUE) tx2 = java.lang.Integer.MIN_VALUE;
            if (ty2 < java.lang.Integer.MIN_VALUE) ty2 = java.lang.Integer.MIN_VALUE;
            return new Integer(tx1, ty1, (int) tx2, (int) ty2);
        }
        
        public Point.Integer getLocation()
        {
            return new Point.Integer(x, y);
        }

        public void setLocation(Point.Integer p)
        {
            this.x = p.x;
            this.y = p.y;
        }
        
        public void setLocation(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public Dimension getSize() {
            return new Dimension(width, height);
        }
        
        public void translate(int dx, int dy) {
            int oldv = this.x;
            int newv = oldv + dx;
            if (dx < 0) {
                // moving leftward
                if (newv > oldv) {
                    // negative overflow
                    // Only adjust width if it was valid (>= 0).
                    if (width >= 0) {
                        // The right edge is now conceptually at
                        // newv+width, but we may move newv to prevent
                        // overflow.  But we want the right edge to
                        // remain at its new location in spite of the
                        // clipping.  Think of the following adjustment
                        // conceptually the same as:
                        // width += newv; newv = MIN_VALUE; width -= newv;
                        width += newv - java.lang.Integer.MIN_VALUE;
                        // width may go negative if the right edge went past
                        // MIN_VALUE, but it cannot overflow since it cannot
                        // have moved more than MIN_VALUE and any non-negative
                        // number + MIN_VALUE does not overflow.
                    }
                    newv = java.lang.Integer.MIN_VALUE;
                }
            } else {
                // moving rightward (or staying still)
                if (newv < oldv) {
                    // positive overflow
                    if (width >= 0) {
                        // Conceptually the same as:
                        // width += newv; newv = MAX_VALUE; width -= newv;
                        width += newv - java.lang.Integer.MAX_VALUE;
                        // With large widths and large displacements
                        // we may overflow so we need to check it.
                        if (width < 0) width = java.lang.Integer.MAX_VALUE;
                    }
                    newv = java.lang.Integer.MAX_VALUE;
                }
            }
            this.x = newv;

            oldv = this.y;
            newv = oldv + dy;
            if (dy < 0) {
                // moving upward
                if (newv > oldv) {
                    // negative overflow
                    if (height >= 0) {
                        height += newv - java.lang.Integer.MIN_VALUE;
                        // See above comment about no overflow in this case
                    }
                    newv = java.lang.Integer.MIN_VALUE;
                }
            } else {
                // moving downward (or staying still)
                if (newv < oldv) {
                    // positive overflow
                    if (height >= 0) {
                        height += newv - java.lang.Integer.MAX_VALUE;
                        if (height < 0) height = java.lang.Integer.MAX_VALUE;
                    }
                    newv = java.lang.Integer.MAX_VALUE;
                }
            }
            this.y = newv;
        }

        @Override
        public Double getBounds2D()
        {
            throw new NotImplementedException();
        }

        public void setRect(double x, double y, double w, double h)
        {
            this.x = (int) Math.floor(x);
            this.y = (int) Math.floor(y);
            this.width = (int) Math.floor(w);
            this.height = (int) Math.floor(h);
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (o instanceof Rectangle.Integer)
            {
                Rectangle.Integer r = (Rectangle.Integer) o;
                return (this.x == r.x && this.y == r.y && this.width == r.width && this.height == r.height);
            }
            else
            {
                return false;
            }
        }
        
        @Override
        public Object clone()
        {
            Integer r = new Integer(this.x, this.y, this.width, this.height);
            return r;
        }

        @Override
        public Rectangle createIntersection(Rectangle r)
        {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
    public static class Double extends Rectangle
    {
        public double x;
        public double y;
        public double width;
        public double height;

        
        public Double(double x, double y, double w, double h)
        {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }
        
        public Double()
        {
            this(0.0, 0.0, 0.0, 0.0);
        }
        @Override
        public double getX()
        {
            return x;
        }

        @Override
        public double getY()
        {
            return y;
        }

        @Override
        public double getWidth()
        {
            return width;
        }

        @Override
        public double getHeight()
        {
            return height;
        }

        @Override
        public boolean isEmpty()
        {
            return (width <= 0.0) || (height <= 0.0);
        }



        public void setRect(double x, double y, double w, double h)
        {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }
        
        public Rectangle createUnion(Rectangle r) {
            Rectangle dest = new Rectangle.Double();
            Rectangle.union(this, r, dest);
            return dest;
        }

        @Override
        public Double getBounds2D()
        {
            throw new NotImplementedException();
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (o instanceof Rectangle.Double)
            {
                Rectangle.Double r = (Rectangle.Double) o;
                return (this.x == r.x && this.y == r.y && this.width == r.width && this.height == r.height);
            }
            else
            {
                return false;
            }
        }

        @Override
        public Object clone()
        {
            Double r = new Double(this.x, this.y, this.width, this.height);
            return r;
        }
        
        public String toString() {
            return getClass().getName()
                + "[x=" + x +
                ",y=" + y +
                ",w=" + width +
                ",h=" + height + "]";
        }

        @Override
        public Rectangle createIntersection(Rectangle r)
        {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
    
    public abstract void setRect(double x, double y, double w, double h);
    public abstract Rectangle createUnion(Rectangle r);  
    public abstract Rectangle createIntersection(Rectangle r);
    
    public static void union(Rectangle src1,
            Rectangle src2,
            Rectangle dest) {
        double x1 = Math.min(src1.getMinX(), src2.getMinX());
        double y1 = Math.min(src1.getMinY(), src2.getMinY());
        double x2 = Math.max(src1.getMaxX(), src2.getMaxX());
        double y2 = Math.max(src1.getMaxY(), src2.getMaxY());
        dest.setFrameFromDiagonal(x1, y1, x2, y2);
    }
    
    public boolean contains(double x, double y) {
        double x0 = getX();
        double y0 = getY();
        return (x >= x0 &&
                y >= y0 &&
                x < x0 + getWidth() &&
                y < y0 + getHeight());
    }
    
    public boolean contains(double x, double y, double w, double h) {
        if (isEmpty() || w <= 0 || h <= 0) {
            return false;
        }
        double x0 = getX();
        double y0 = getY();
        return (x >= x0 &&
                y >= y0 &&
                (x + w) <= x0 + getWidth() &&
                (y + h) <= y0 + getHeight());
    }
    
    public boolean intersects(double x, double y, double w, double h) {
        if (isEmpty() || w <= 0 || h <= 0) {
            return false;
        }
        double x0 = getX();
        double y0 = getY();
        return (x + w > x0 &&
                y + h > y0 &&
                x < x0 + getWidth() &&
                y < y0 + getHeight());
    }
    
    @Override
    public void setFrame(double x, double y, double w, double h)
    {
        setRect(x, y, w, h);
    }
    
    public static void intersect(Rectangle src1,
            Rectangle src2,
            Rectangle dest) 
    {
        double x1 = Math.max(src1.getMinX(), src2.getMinX());
        double y1 = Math.max(src1.getMinY(), src2.getMinY());
        double x2 = Math.min(src1.getMaxX(), src2.getMaxX());
        double y2 = Math.min(src1.getMaxY(), src2.getMaxY());
        dest.setFrame(x1, y1, x2-x1, y2-y1);
    }

}
