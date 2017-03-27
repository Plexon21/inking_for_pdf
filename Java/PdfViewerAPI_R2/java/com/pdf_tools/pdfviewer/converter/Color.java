package com.pdf_tools.pdfviewer.converter;


public class Color
{
    public float[] frgbvalue;
    public float falpha;
    
    public Color(float r, float g, float b, float a)
    {
        frgbvalue = new float[3];
        frgbvalue[0] = r;
        frgbvalue[1] = g;
        frgbvalue[2] = b;
        falpha = a;
    }
    
}
