package com.pdf_tools.pdfviewer.Requests;

import com.pdf_tools.pdfviewer.DocumentManagement.IPdfDocument;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;

public class PdfDeleteAnnotationRequest extends APdfRequest<Boolean>
{

    public PdfDeleteAnnotationRequest(long annotHandle, int page)
    {
        m_annotHandle = annotHandle;
        m_page = page;
        _priority = 10;
    }

    @Override
    public void execute(IPdfDocument document, IPdfControllerCallbackManager controller)
    {

        try
        {
            document.deleteAnnotation(m_annotHandle, m_page);
            completedEvent.triggerEvent(null, null);
            controller.fireOnAnnotationDeleted(m_page);
        } catch (PdfViewerException e)
        {
            completedEvent.triggerEvent(null, e);
        }

    }

    private long m_annotHandle;
    private int m_page;
}
