package com.pdf_tools.pdfviewer.Annotations;

import java.util.HashMap;

import com.pdf_tools.pdfviewer.Model.PdfViewerController;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;

public class PdfGenericAnnotation extends APdfAnnotation
{

    /**
     * Generic Annotation Object. All the information is stored in the hash map.
     * This is the object that is used for passing annotations from native to
     * java.
     * 
     * @param dict
     */
    PdfGenericAnnotation(HashMap<String, Object> dict)
    {

        m_dict = dict;

        // Type of annotation
        Object type = m_dict.get("SubType");
        if (type != null)
        {
            m_type = TPdfAnnotationType.fromString((String) type);
        }

    }

    public TPdfAnnotationType getType()
    {
        return m_type;
    }

    public HashMap<String, Object> getDict()
    {
        return m_dict;
    }

    public void action(PdfViewerController controller) throws PdfViewerException
    {

    }

    private HashMap<String, Object> m_dict;
    private TPdfAnnotationType m_type;

}
