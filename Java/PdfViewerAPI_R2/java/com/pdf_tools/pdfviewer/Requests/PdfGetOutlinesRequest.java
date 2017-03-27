package com.pdf_tools.pdfviewer.Requests;

import com.pdf_tools.pdfviewer.DocumentManagement.IPdfDocument;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager;
import com.pdf_tools.pdfviewer.Model.PdfOutlineItem;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;

public class PdfGetOutlinesRequest extends APdfRequest<PdfOutlineItem[]>
{

    public PdfGetOutlinesRequest(int parentId)
    {
        this._priority = 9;
        this.parentId = parentId;
    }

    @Override
    public void execute(IPdfDocument document, IPdfControllerCallbackManager controller)
    {
        try
        {
            PdfOutlineItem items[] = document.getOutlines(parentId);
            controller.onOutlinesLoaded(parentId, items, null);
            completedEvent.triggerEvent(items, null);
        } catch (PdfViewerException ex)
        {
            controller.onOutlinesLoaded(parentId, null, ex);
            completedEvent.triggerEvent(null, ex);
        }
    }

    private int parentId;

}
