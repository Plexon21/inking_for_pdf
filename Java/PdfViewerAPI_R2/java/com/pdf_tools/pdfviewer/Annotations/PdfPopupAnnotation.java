package com.pdf_tools.pdfviewer.Annotations;

import java.util.HashMap;

import com.pdf_tools.pdfviewer.Model.PdfViewerController;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;

public class PdfPopupAnnotation extends APdfAnnotation
{
    public PdfPopupAnnotation(HashMap<String, Object> m_dict, APdfAnnotation parent, PdfViewerController controller)
    {
        super(m_dict, controller);

        Object open = m_dict.get("isOpen");
        if (open != null)
        {
            // TODO: implement proper popup 
            // state behaviour
            // setOpen((Boolean) open);
            setOpen(false);
        }
        m_parent = parent;
    }

    public boolean isOpen()
    {
        return m_isOpen;
    }

    public void setOpen(boolean m_isOpen)
    {
        this.m_isOpen = m_isOpen;
    }

    private boolean m_isOpen;
    private APdfAnnotation m_parent;

    @Override
    public void action(int clickcount) throws PdfViewerException
    {
        // TODO Auto-generated method stub

    }
}
