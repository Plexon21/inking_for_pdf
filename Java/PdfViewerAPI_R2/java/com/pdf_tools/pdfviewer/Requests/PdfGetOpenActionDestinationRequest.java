package com.pdf_tools.pdfviewer.Requests;

import com.pdf_tools.pdfviewer.DocumentManagement.IPdfDocument;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager;
import com.pdf_tools.pdfviewer.Model.PdfAdvancedDestination;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;

public class PdfGetOpenActionDestinationRequest extends APdfRequest<PdfAdvancedDestination>
{

    public PdfGetOpenActionDestinationRequest()
    {
        this._priority = 88;
    }

    @Override
    public void execute(IPdfDocument document, IPdfControllerCallbackManager controller)
    {
        try
        {
            PdfAdvancedDestination dest = document.getOpenActionDestination();
            completedEvent.triggerEvent(dest, null);
        } catch (PdfViewerException ex)
        {
            completedEvent.triggerEvent(null, ex);
        }
    }

}
