package com.pdf_tools.pdfviewer.Requests;

import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation;
import com.pdf_tools.pdfviewer.DocumentManagement.IPdfDocument;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;

public class PdfUpdateAnnotationRequest extends APdfRequest<Integer>
{
    private APdfAnnotation m_annotation;

    public PdfUpdateAnnotationRequest(APdfAnnotation annotation)
    {
        m_annotation = annotation;
    }

    @Override
    public void execute(IPdfDocument document, IPdfControllerCallbackManager controller)
    {
        try
        {
            document.updateAnnotation(m_annotation);
            controller.onAnnotationUpdated(m_annotation, null);
            completedEvent.triggerEvent(m_annotation.getPage(), null);
        } catch (PdfViewerException ex)
        {
            controller.onAnnotationUpdated(null, null);
            completedEvent.triggerEvent(null, ex);
        }

    }

}
