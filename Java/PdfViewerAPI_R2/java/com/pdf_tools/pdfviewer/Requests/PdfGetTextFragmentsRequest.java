package com.pdf_tools.pdfviewer.Requests;

import java.util.List;

import com.pdf_tools.pdfviewer.DocumentManagement.IPdfDocument;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager;
import com.pdf_tools.pdfviewer.Model.PdfTextFragment;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;

public class PdfGetTextFragmentsRequest extends APdfRequest<List<PdfTextFragment>>
{

    public PdfGetTextFragmentsRequest(int pageNo)
    {
        this._priority = 82;
        this.pageNo = pageNo;
    }

    @Override
    public void execute(IPdfDocument document, IPdfControllerCallbackManager controller)
    {
        try
        {
            List<PdfTextFragment> frags = document.getTextFragments(pageNo);
            completedEvent.triggerEvent(frags, null);
        } catch (PdfViewerException ex)
        {
            completedEvent.triggerEvent(null, ex);
        }
    }

    private int pageNo;

}
