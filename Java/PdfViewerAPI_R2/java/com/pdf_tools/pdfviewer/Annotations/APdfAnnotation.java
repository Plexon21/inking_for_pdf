package com.pdf_tools.pdfviewer.Annotations;

import java.util.HashMap;

import com.pdf_tools.pdfviewer.Model.PdfViewerController;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;
import com.pdf_tools.pdfviewer.converter.Color;
import com.pdf_tools.pdfviewer.converter.geom.Rectangle;

public abstract class APdfAnnotation
{

    APdfAnnotation()
    {
    };

    APdfAnnotation(HashMap<String, Object> dict, PdfViewerController controller)
    {

        m_controller = controller;
        m_dict = dict;
        m_modified = false;

        // TODO: convert general hash map to member variables

        // Handle of the annotation

        Object handle = m_dict.get("Handle");
        if (handle != null)
        {
            m_handle = ((Long) handle).longValue();
        }

        Object page = m_dict.get("Page");
        if (page != null)
        {
            setPage((Integer) page);
        }
        
        Object flags = m_dict.get("Flags");
        if (flags != null)
        {
            m_flags = (Integer)flags;
        }

        // Rectangle of the annotation
        Double[] rectArray = (Double[]) (m_dict.get("Rect"));
        if (rectArray != null)
        {
            m_rect = new Rectangle.Double(rectArray[0], rectArray[1], rectArray[2] - rectArray[0],
                    rectArray[3] - rectArray[1]);
            m_dRect = new Double[] { rectArray[0], rectArray[1], rectArray[2], rectArray[3] };
        }

        // Assign type of annotation
        Object type = m_dict.get("SubType");
        if (type != null)
        {
            m_type = TPdfAnnotationType.fromString((String) type);
        }

        String content = (String) dict.get("Contents");
        if (content != null)
        {
            m_content = content;
        }

        Object color = m_dict.get("Color");
        if (color != null)
        {
            Double[] colorArray = (Double[]) color;
            int size = colorArray.length;

            // TODO: handle gray scale and cmyk
            if (size == 3)
            {
                float alpha = 0.3f;
                m_color = new Color(colorArray[0].floatValue(), colorArray[1].floatValue(), colorArray[2].floatValue(),
                        alpha);
                m_luminance = Luminance(colorArray);
            }
        }

        Object author = m_dict.get("T");
        if (author != null)
        {
            m_author = (String) author;
        }

    };

    public APdfAnnotation(TPdfAnnotationType type, int page, long handle)
    {
        m_type = type;
        m_page = page;
        m_handle = handle;
    }

    public double getLuminance()
    {
        return m_luminance;
    }

    private double Luminance(Double[] color)
    {
        for (Double colorComp : color)
        {
            colorComp /= 255;
            colorComp = (colorComp <= 0.03928f) ? colorComp /= 12.92f : Math.pow(((colorComp + 0.055) / 1.055), 2.4);
        }
        return 0.2126 * color[0] + 0.7152 * color[1] + 0.0722 * color[2];

    }
    
    public Long getParentHandle()
    {
        return null;
    }

    public Color getColor()
    {
        return m_color;
    }

    public void setColor(Color m_color)
    {
        this.m_color = m_color;
    }

    public Rectangle.Double getRect()
    {
        return m_rect;
    }

    public String getContent()
    {
        return new String("Implement me!");
    }

    // defines the click action for an annotation
    public void action(int clickcount) throws PdfViewerException
    {
        return;
    }

    // what happens when the mouse hovers over the annotation
    public void hover()
    {
    };

    // This method
    public boolean isOpen()
    {
        return false;
    }


    public boolean isMarkup()
    {
        return false;
    }

    public boolean isTextMarkup()
    {
        return false;
    }

    public boolean isLink()
    {
        return false;
    }
    
    public boolean isMoveable()
    {
        return (m_flags & TPdfAnnotationFlag.eReadOnly.type) == 0 && (m_flags & TPdfAnnotationFlag.eLocked.type) == 0;
    }
    
    public boolean isPopup()
    {
        return false;
    }
    
    protected void SetModified(boolean modified)
    {
        m_modified = modified;
    }

    public void setRectPosition(int rotation, double x, double y)
    {
        double width = m_rect.width;
        double height = m_rect.height;
        m_rect.x = x;
        m_rect.y = y;

        if (rotation == 0)
        {
            m_dRect[0] = x;
            m_dRect[1] = y;
            m_dRect[2] = x + width;
            m_dRect[3] = y + height;
        } else if (rotation == 90)
        {
            m_dRect[0] = x - width;
            m_dRect[1] = y;
            m_dRect[2] = x;
            m_dRect[3] = y + height;
        } else if (rotation == 180)
        {
            m_dRect[0] = x - width;
            m_dRect[1] = y - height;
            m_dRect[2] = x;
            m_dRect[3] = y;
        } else if (rotation == 270)
        {
            m_dRect[0] = x;
            m_dRect[1] = y - height;
            m_dRect[2] = x + width;
            m_dRect[3] = y;
        }

        m_rect.setRect(m_dRect[0], m_dRect[1], m_dRect[2] - m_dRect[0], m_dRect[3] - m_dRect[1]);
    }

    public TPdfAnnotationType getType()
    {
        return m_type;
    }

    public String getAuthor()
    {
        return m_author;
    }

    public int getPage()
    {
        return m_page;
    }

    public void setPage(int m_page)
    {
        this.m_page = m_page;
        SetModified(true);
    }

    public long getHandle()
    {
        return m_handle;
    }

    public PdfQuadPoints getQuadPoints()
    {
        return m_quadPoints;
    }


    
    public enum TPdfActionType
    {
        eActionGoTo("GoTo"), eActionURI("URI");
        private final String type;

        TPdfActionType(String type)
        {
            this.type = type;
        }

        public String getName()
        {
            return type;
        }

        public static TPdfActionType fromString(String text)
        {
            if (text != null)
            {
                for (TPdfActionType b : TPdfActionType.values())
                {
                    if (text.equalsIgnoreCase(b.type))
                    {
                        return b;
                    }
                }
            }
            return null;
        }
    }

    public enum TPdfAnnotationFlag
    {
        eInvisible(1), eHidden(2), ePrint(4), eNoZoom(8), eNoRotate(16), eNoView(32), eReadOnly(64), eLocked(
                128), eToggleNoView(256), eLockedContents(512);

        private int type;

        private TPdfAnnotationFlag(int iType)
        {
            this.type = iType;
        }
    }

    public enum TPdfAnnotationType
    {
        eAnnotationUnknown(null, 0), 
        eAnnotationText("Text", 1), 
        eAnnotationLink("Link", 2), 
        eAnnotationFreeText("FreeText", 3), 
        eAnnotationLine("Line", 4),
        eAnnotationSquare("Square", 5),
        eAnnotationCircle("Circle", 6),
        eAnnotationPolygon("Polygon", 7),
        eAnnotationPolyLine("PolyLine", 8),
        eAnnotationHighlight("Highlight", 9), 
        eAnnotationInk("Ink", 15), 
        eAnnotationPopup("Popup", 16), 
        eAnnotationWidget("Widget"), 
        eDefaultAnnotation("Default"),
        eAnnotationUnderline("Underline", 10),
        eAnnotationSquiggly("Squigly", 11),
        eAnnotationStrikeOut("StrikeOut", 12),
        eAnnotationStamp("Stamp", 13),
        eAnnotationCaret("Caret", 14),
        eAnnotationFileAttachment("Attachment", 17),
        eAnnotationSound("Sound", 18),
        eAnnotationMovie("Movie", 19),
        eAnnotationScreen("Screen", 21),
        eAnnotationPrinterMark("PrinterMark", 22),
        eAnnotationTrapNet("TrapNet", 23),
        eAnnotationWatermark("Watermark", 24),
        eAnnotation3D("3D", 25);
        private final String sType;
        private final int iType;

        TPdfAnnotationType(String sType, int iType)
        {
            this.sType = sType;
            this.iType = iType;
        }

        TPdfAnnotationType(String sType)
        {
            this.sType = sType;
            this.iType = -1;
        }

        public String getName()
        {
            return sType;
        }

        public int getType()
        {
            return iType;
        }

        public static TPdfAnnotationType fromString(String text)
        {
            if (text != null)
            {
                for (TPdfAnnotationType b : TPdfAnnotationType.values())
                {
                    if (text.equalsIgnoreCase(b.sType))
                    {
                        return b;
                    }
                }
            }
            return null;
        }
    }


    protected PdfViewerController m_controller;
    protected HashMap<String, Object> m_dict;
    private int m_page;
    protected int m_flags;
    protected Rectangle.Double m_rect;
    protected Double[] m_dRect;
    protected TPdfAnnotationType m_type;
    protected Color m_color;
    protected double m_luminance;
    protected TPdfActionType m_actionType;
    protected String m_author;
    protected long m_handle;
    protected boolean m_modified;
    protected String m_content;
    protected PdfQuadPoints m_quadPoints;

}
