package com.pdf_tools.pdfviewer.Requests;

import com.pdf_tools.pdfviewer.DocumentManagement.IPdfDocument;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.TPageLayoutMode;

public class PdfGetPageLayoutRequest extends APdfRequest<TPageLayoutMode>
{

    public PdfGetPageLayoutRequest()
    {
        this._priority = 85;
    }

    @Override
    public void execute(IPdfDocument document, IPdfControllerCallbackManager controller)
    {

        try
        {
            TPageLayoutMode layout = document.getPageLayout();
            completedEvent.triggerEvent(layout, null);
        } catch (PdfViewerException ex)
        {
            completedEvent.triggerEvent(null, ex);
        }
    }

}
