package com.pdf_tools.pdfviewer.Requests;

import com.pdf_tools.pdfviewer.Annotations.PdfGenericAnnotation;

import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation.TPdfAnnotationType;
import com.pdf_tools.pdfviewer.DocumentManagement.IPdfDocument;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;

public class PdfCreateAnnotationRequest extends APdfRequest<PdfGenericAnnotation>
{

    public PdfCreateAnnotationRequest(TPdfAnnotationType type, int page, Double[] rect)
    {
        m_type = type;
        m_page = page;
        m_dRect = rect;
    }

    @Override
    public void execute(IPdfDocument document, IPdfControllerCallbackManager controller)
    {
        try
        {
            PdfGenericAnnotation annotHandle = document.createAnnotation(m_type, m_page, m_dRect);
            completedEvent.triggerEvent(annotHandle, null);
            controller.onAnnotationCreated(m_page);
        } catch (PdfViewerException e)
        {
            completedEvent.triggerEvent(null, e);
        }

    }

    private TPdfAnnotationType m_type;
    private int m_page;
    private Double[] m_dRect;
}
