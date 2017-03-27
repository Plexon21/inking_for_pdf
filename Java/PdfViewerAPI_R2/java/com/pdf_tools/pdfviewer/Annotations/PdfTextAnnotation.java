package com.pdf_tools.pdfviewer.Annotations;

import java.util.HashMap;

import com.pdf_tools.pdfviewer.Model.PdfViewerController;

public class PdfTextAnnotation extends APdfMarkupAnnotation
{

    public PdfTextAnnotation(HashMap<String, Object> dict, PdfViewerController controller)
    {
        super(dict, controller);
    }
}
