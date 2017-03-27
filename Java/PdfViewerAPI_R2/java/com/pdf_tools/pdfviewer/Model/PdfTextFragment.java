package com.pdf_tools.pdfviewer.Model;

import java.util.Arrays;
import java.util.List;

import com.pdf_tools.pdfviewer.converter.geom.Rectangle;

public class PdfTextFragment extends NativeObject
{

    PdfTextFragment(String text, int pageNo, double dX, double dY, double dWidth, double dHeight, Double[] pOffsets)
    {
        rectOnUnrotatedPage = new Rectangle.Double(dX, dY, dWidth, dHeight);
        this.text = text;
        this.pageNo = pageNo;
        glyphPositions = Arrays.asList(pOffsets);
    }

    private boolean finalized = false;

    @Override
    public void finalize()
    {
        if (!finalized)
            releaseTextFragment(this);
        finalized = true;
    }

    public Rectangle.Double getRectOnUnrotatedPage()
    {
        return rectOnUnrotatedPage;
    }

    public Rectangle.Double getRectOnUnrotatedPage(int start, int end)
    {
        end = Math.min(end, glyphPositions.size() - 1);
        Rectangle.Double r = (Rectangle.Double) rectOnUnrotatedPage.clone();
        r.x = glyphPositions.get(start);
        r.width = glyphPositions.get(end) - glyphPositions.get(start);
        return r;
    }

    public String getText()
    {
        return text;
    }

    public int getPageNo()
    {
        return pageNo;
    }

    public String toString()
    {
        return "Text=" + text + ", rectOnUnrotatedPage=" + rectOnUnrotatedPage.toString();
    }

    public int getIndexOfClosestGlyph(double x)
    {
        int start = 0;
        int end = glyphPositions.size() - 1;

        while (true)
        {
            if (end - start < 2)
            {
                // only end and start are the closest indices
                if (Math.abs(glyphPositions.get(end) - x) < Math.abs(glyphPositions.get(start) - x))
                    return end;
                return start;
            }
            int needle = (end - start) / 2 + start;
            switch (Double.compare(x, glyphPositions.get(needle)))
            {
            case 1:
                start = needle;
                break;
            case -1:
                end = needle;
                break;
            case 0:
                return needle;
            }
        }
    }

    private List<Double> glyphPositions;
    private Rectangle.Double rectOnUnrotatedPage;
    private String text;
    private int pageNo;

}
