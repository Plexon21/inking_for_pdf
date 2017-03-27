package com.pdf_tools.pdfviewer.Requests;

import com.pdf_tools.pdfviewer.DocumentManagement.IPdfDocument;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;

public class PdfCloseRequest extends APdfRequest<Void>
{

    public PdfCloseRequest()
    {
        _priority = 100;
    }

    @Override
    public void execute(IPdfDocument document, IPdfControllerCallbackManager controller)
    {
        try
        {
            controller.onClosing();
            document.close();
            this.completedEvent.triggerEvent(null, null);
            controller.onCloseCompleted(null);
        } catch (PdfViewerException ex)
        {
            this.completedEvent.triggerEvent(null, ex);
            controller.onCloseCompleted(ex);
        }
    }

}
