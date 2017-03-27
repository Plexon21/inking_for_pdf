/****************************************************************************
 *
 * File:            Point.java
 *
 * Description:     PDF Point Struct
 *
 * Author:          Interface Generator, PDF Tools AG
 * 
 * Copyright:       Copyright (C) 2001 - 2016 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *
 ***************************************************************************/

package com.pdf_tools;

public class Point
{
    public Point(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

     /**
     * 
     */
    public double getX()
    {
        return this.x;
    }

     /**
     * 
     */
    public void setX(double x)
    {
        this.x = x;
    }

     /**
     * 
     */
    public double getY()
    {
        return this.y;
    }

     /**
     * 
     */
    public void setY(double y)
    {
        this.y = y;
    }

    public double x;
    public double y;
}
