package com.pdf_tools.pdfviewer.Requests;

import com.pdf_tools.pdfviewer.DocumentManagement.IPdfDocument;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;

public class PdfOpenRequest extends APdfRequest<Void>
{

    public PdfOpenRequest(String filename, String password)
    {
        this.filename = filename;
        this.password = password;
        this._priority = 100;
    }

    @Override
    public void execute(IPdfDocument document, IPdfControllerCallbackManager controller)
    {

        try
        {
            document.open(filename, password);
            completedEvent.triggerEvent(null, null);
            controller.onOpenCompleted(null);
        } catch (PdfViewerException ex)
        {
            completedEvent.triggerEvent(null, ex);
            controller.onOpenCompleted(ex);
        }
    }

    public String filename;
    public String password;

}
