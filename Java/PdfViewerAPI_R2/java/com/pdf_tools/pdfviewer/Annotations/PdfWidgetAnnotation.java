package com.pdf_tools.pdfviewer.Annotations;

import java.util.HashMap;

import com.pdf_tools.pdfviewer.Model.PdfViewerController;

public class PdfWidgetAnnotation extends APdfAnnotation
{
    public PdfWidgetAnnotation(HashMap<String, Object> dict, PdfViewerController controller)
    {
        super(dict, controller);
    }
    
    public boolean isMoveable()
    {
        return false;
    }
}
