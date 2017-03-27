package com.pdf_tools.pdfviewer.Requests;

import com.pdf_tools.pdfviewer.DocumentManagement.IPdfDocument;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;

public class PdfSaveRequest extends APdfRequest<Void>
{

    public PdfSaveRequest(String path)
    {
        _priority = 100;
        m_path = path;
    }

    @Override
    public void execute(IPdfDocument document, IPdfControllerCallbackManager controller)
    {
        try
        {
            document.saveAs(m_path);
            this.completedEvent.triggerEvent(null, null);
            controller.onSaveCompleted(null);
        } catch (PdfViewerException ex)
        {
            this.completedEvent.triggerEvent(null, ex);
            controller.onSaveCompleted(ex);
        }
    }

    private String m_path;

}
