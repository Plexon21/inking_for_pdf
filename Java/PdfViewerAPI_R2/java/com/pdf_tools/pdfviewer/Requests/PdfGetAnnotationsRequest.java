package com.pdf_tools.pdfviewer.Requests;

import com.pdf_tools.pdfviewer.Annotations.PdfGenericAnnotation;
import com.pdf_tools.pdfviewer.DocumentManagement.IPdfDocument;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;

public class PdfGetAnnotationsRequest extends APdfRequest<PdfGenericAnnotation[]>
{

    public PdfGetAnnotationsRequest(int page)
    {
        this._priority = 9;
        this.page = page;
    }

    @Override
    public void execute(IPdfDocument document, IPdfControllerCallbackManager controller)
    {
        try
        {
            PdfGenericAnnotation annotations[] = document.getAnnotationsOnPage(this.page);
            controller.onAnnotationsLoaded(page, annotations, null);
            completedEvent.triggerEvent(annotations, null);
        } catch (PdfViewerException ex)
        {
            completedEvent.triggerEvent(null, ex);
        }
    }

    private int page;
}
