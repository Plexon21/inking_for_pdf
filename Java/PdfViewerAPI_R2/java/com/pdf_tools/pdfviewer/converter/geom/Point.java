package com.pdf_tools.pdfviewer.converter.geom;

public abstract class Point
{
    public static class Integer extends Point
    {
        public int x;
        public int y;
        
        public Integer()
        {
            this(0,0);
        }
        
        public Integer(int x, int y)
        {
            this.x = x;
            this.y = y;
        }
        
        public double getX()
        {
            return x;
        }
        
        public double getY()
        {
            return y;
        }
        
        public void setLocation(double x, double y)
        {
            this.x = (int) Math.floor(x+0.5);
            this.y = (int) Math.floor(y+0.5);
        }
        
        @Override
        public String toString()
        {
            return getClass().getName() + "[x=" + x + ",y=" + y +"]";
        }
    }
    
    public static class Double extends Point
    {
        public double x;
        public double y;
        
        public Double(double x, double y)
        {
            this.x = x;
            this.y = y;
        }
        
        public Double()
        {
            this(0.0, 0.0);
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
        public String toString()
        {
            return getClass().getName() + "[x=" + x + ",y=" + y +"]";
        }
    }
    
    public abstract double getX();
    public abstract double getY();
}
