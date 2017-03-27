package com.pdf_tools.pdfviewer.Annotations;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import com.pdf_tools.pdfviewer.Model.PdfAdvancedDestination;
import com.pdf_tools.pdfviewer.Model.PdfViewerController;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.TDestinationType;

public class PdfLinkAnnotation extends APdfAnnotation
{

    public PdfLinkAnnotation(HashMap<String, Object> dict, PdfViewerController controller)
    {
        super(dict, controller);
        // TODO Auto-generated constructor stub
        String uri = (String) m_dict.get("URI");
        if (uri != null)
        {
            m_uri = uri;
        }

        String actionType = (String) m_dict.get("ActionType");
        if (actionType != null)
        {
            m_actionType = TPdfActionType.fromString(actionType);
        }
        @SuppressWarnings("unchecked")
        HashMap<String, Object> dest = (HashMap<String, Object>) m_dict.get("DestMap");
        if (dest != null && dest instanceof HashMap)
        {
            int destPage = (Integer) dest.get("Page");
            Double destArray[] = (Double[]) dest.get("Destination");
            Integer destType = (Integer) dest.get("DestType");
            m_destination = new PdfAdvancedDestination(destPage, TDestinationType.values()[destType], destArray);

        }

        Object quadPoints = dict.get("QuadPoints");
        if (quadPoints != null)
        {
            m_quadPoints = new PdfQuadPoints((Double[]) quadPoints);
        }
    }

    @Override
    public void action(int clickcount) throws PdfViewerException
    {
        if (m_actionType != null)
        {
            switch (m_actionType)
            {
            case eActionURI:
                if (Desktop.isDesktopSupported())
                {
                    try
                    {
                        Desktop.getDesktop().browse(new URI(m_uri));
                    } catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (URISyntaxException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                break;
            case eActionGoTo:
                if (m_destination != null)
                {
                    m_controller.setDestination(m_destination);
                }
            default:
                break;
            }
        } else
        {
            m_controller.setDestination(m_destination);
        }

    }

    @Override
    public void hover()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isMoveable()
    {
        return false;
    }

    @Override
    public boolean isLink()
    {
        return true;
    }

    private PdfAdvancedDestination m_destination;
    private String m_uri;
    private TPdfActionType m_actionType;
    private PdfQuadPoints m_quadPoints;
}
