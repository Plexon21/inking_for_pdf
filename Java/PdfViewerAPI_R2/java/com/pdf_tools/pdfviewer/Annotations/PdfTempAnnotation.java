/**
 * Temporary annotation for all the unimplemented annotations
 */

package com.pdf_tools.pdfviewer.Annotations;

import java.util.HashMap;

import com.pdf_tools.pdfviewer.Model.PdfViewerController;

public class PdfTempAnnotation extends APdfMarkupAnnotation
{

    public PdfTempAnnotation(HashMap<String, Object> dict, PdfViewerController controller)
    {
        super(dict, controller);
    }

    @Override
    public void action(int clickcount)
    {
        // m_controller.fireOnMarkupAnnotationAction(this);
    }

    public void setType(TPdfAnnotationType type)
    {
        m_type = type;
    }

}
