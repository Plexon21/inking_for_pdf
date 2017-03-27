/****************************************************************************
 *
 * File:            Size.java
 *
 * Description:     PDF Size Struct
 *
 * Author:          Interface Generator, PDF Tools AG
 * 
 * Copyright:       Copyright (C) 2001 - 2016 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *
 ***************************************************************************/

package com.pdf_tools;

public class Size
{
    public Size(double width, double height)
    {
        this.width = width;
        this.height = height;
    }

     /**
     * 
     */
    public double getWidth()
    {
        return this.width;
    }

     /**
     * 
     */
    public void setWidth(double width)
    {
        this.width = width;
    }

     /**
     * 
     */
    public double getHeight()
    {
        return this.height;
    }

     /**
     * 
     */
    public void setHeight(double height)
    {
        this.height = height;
    }

    public double width;
    public double height;
}
