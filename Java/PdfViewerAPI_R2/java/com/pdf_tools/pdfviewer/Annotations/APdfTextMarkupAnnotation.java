package com.pdf_tools.pdfviewer.Annotations;

import java.util.HashMap;

import com.pdf_tools.pdfviewer.Model.PdfViewerController;

public abstract class APdfTextMarkupAnnotation extends APdfMarkupAnnotation
{

    public APdfTextMarkupAnnotation(HashMap<String, Object> dict, PdfViewerController controller)
    {
        super(dict, controller);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean isTextMarkup()
    {
        return true;
    }
}
