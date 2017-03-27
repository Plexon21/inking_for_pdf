/****************************************************************************
 *
 * File:            Rectangle.java
 *
 * Description:     PDF Rectangle Struct
 *
 * Author:          Interface Generator, PDF Tools AG
 * 
 * Copyright:       Copyright (C) 2001 - 2016 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *
 ***************************************************************************/

package com.pdf_tools;

public class Rectangle
{
    public Rectangle(double left, double bottom, double right, double top)
    {
        this.left = left;
        this.bottom = bottom;
        this.right = right;
        this.top = top;
    }

    public double left;
    public double bottom;
    public double right;
    public double top;
    
     /**
     * 
     */
    public double getLeft()
    {
        return this.left;
    }

     /**
     * 
     */
    public void setLeft(double left)
    {
        this.left = left;
    }

     /**
     * 
     */
    public double getBottom()
    {
        return this.bottom;
    }

     /**
     * 
     */
    public void setBottom(double bottom)
    {
        this.bottom = bottom;
    }

     /**
     * 
     */
    public double getRight()
    {
        return this.right;
    }

     /**
     * 
     */
    public void setRight(double right)
    {
        this.right = right;
    }

     /**
     * 
     */
    public double getTop()
    {
        return this.top;
    }

     /**
     * 
     */
    public void setTop(double top)
    {
        this.top = top;
    }

    public double getWidth()
    {
        return right - left;
    }

    public double getHeight()
    {
        return top - bottom;
    }
    
    public Size getSize()
    {
        return new Size(getWidth(), getHeight());
    }
    
    public Point getLeftBottom()
    {
        return new Point(left, bottom);
    }
    
    public Point getLeftTop()
    {
        return new Point(left, top);
    }
    
    public Point getRightBottom()
    {
        return new Point(right, bottom);
    }
    
    public Point getRightTop()
    {
        return new Point(right, top);
    }
    
    public Point getCenter()
    {
        return new Point(0.5 * (left + right), 0.5 * (bottom + top));
    }
}
