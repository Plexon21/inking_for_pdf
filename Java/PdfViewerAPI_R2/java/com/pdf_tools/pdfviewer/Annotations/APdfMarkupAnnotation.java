package com.pdf_tools.pdfviewer.Annotations;

import java.util.HashMap;

import com.pdf_tools.pdfviewer.Model.PdfViewerController;

public abstract class APdfMarkupAnnotation extends APdfAnnotation
{

    public APdfMarkupAnnotation(HashMap<String, Object> dict, PdfViewerController controller)
    {
        super(dict, controller);

        Object popup_dict = dict.get("Popup");
        if (popup_dict != null)
        {
            m_popup = new PdfPopupAnnotation((HashMap<String, Object>) popup_dict, this, controller);
        }

        Object quadPoints = dict.get("QuadPoints");
        if (quadPoints != null)
        {
            m_quadPoints = new PdfQuadPoints((Double[]) quadPoints);
        }
    }

    public APdfMarkupAnnotation(TPdfAnnotationType type, int page, long handle)
    {
        super(type, page, handle);
    }

    @Override
    public String getContent()
    {
        return m_content;
    }

    public boolean isMarkup()
    {
        return true;
    }

    public void setContent(String content)
    {
        m_content = content;
        m_author = System.getProperty("user.name");
    }

    public PdfPopupAnnotation getPopup()
    {
        return m_popup;
    }

    @Override
    public void hover()
    {
        m_controller.fireOnAnnotationHover(this);
    }

    @Override
    public void action(int clickcount)
    {
        if (clickcount > 1)
        {
            m_controller.fireOnMarkupAnnotationAction(this);
        } else
        {
            m_controller.fireOnMarkupAnnotationClicked(this);
        }

    }

    private PdfPopupAnnotation m_popup;
}
