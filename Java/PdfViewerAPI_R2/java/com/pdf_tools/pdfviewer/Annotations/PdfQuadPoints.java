package com.pdf_tools.pdfviewer.Annotations;

import java.util.ArrayList;
import java.util.List;

import com.pdf_tools.pdfviewer.converter.geom.Rectangle;

/**
 * Class for representing PDF quad points. They are used for link annotations
 * and MarkupAnnotations
 * 
 * @author pgl
 *
 */
public class PdfQuadPoints
{

    public PdfQuadPoints(Double[] rawQPoints)
    {
        m_quadPointRectList = new ArrayList<Rectangle.Double>();
        for (int i = 0; i < rawQPoints.length / 8; i++)
        {
            // these weird indices are thanks to adobe and their non
            // standard compliant quadpoints order. Index 4,5 is bottom left
            // corner of the rectangle, 2,3 the top right corner.
            double x1 = rawQPoints[i * 8 + 4];
            double y1 = rawQPoints[i * 8 + 5];
            double x2 = rawQPoints[i * 8 + 2];
            double y2 = rawQPoints[i * 8 + 3];
            m_quadPointRectList.add(new Rectangle.Double(x1, y1, x2 - x1, y2 - y1));
        }
    }

    public Double[] getRawQuadPoints()
    {
        return m_rawQuadPoints;
    }

    public List<Rectangle.Double> getQuadPointRectList()
    {
        return m_quadPointRectList;
    }

    private Double[] m_rawQuadPoints;

    private List<Rectangle.Double> m_quadPointRectList;
}
