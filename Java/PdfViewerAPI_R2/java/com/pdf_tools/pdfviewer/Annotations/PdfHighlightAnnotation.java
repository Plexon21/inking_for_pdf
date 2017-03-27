package com.pdf_tools.pdfviewer.Annotations;

import java.util.HashMap;

import com.pdf_tools.pdfviewer.Model.PdfViewerController;

public class PdfHighlightAnnotation extends APdfTextMarkupAnnotation
{

    public PdfHighlightAnnotation(HashMap<String, Object> dict, PdfViewerController controller)
    {
        super(dict, controller);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean isMoveable()
    {
        return false;
    }

}
