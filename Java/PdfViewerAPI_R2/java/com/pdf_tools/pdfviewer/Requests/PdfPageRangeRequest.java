package com.pdf_tools.pdfviewer.Requests;

import java.util.List;
import java.util.Map;

import com.pdf_tools.pdfviewer.DocumentManagement.IPdfDocument;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;
import com.pdf_tools.pdfviewer.converter.geom.Rectangle;

public class PdfPageRangeRequest extends APdfRequest<Map<Integer, Rectangle.Double>>
{

    public PdfPageRangeRequest(List<Integer> pages)
    {
        this(pages, false);
    }

    public PdfPageRangeRequest(List<Integer> pages, boolean highPriority)
    {
        this.pages = pages;
        this._priority = highPriority ? 80 : 5;
    }

    @Override
    public void execute(IPdfDocument document, IPdfControllerCallbackManager controller)
    {
        try
        {
            Map<Integer, Rectangle.Double> rects = document.getPageRangeFromSource(pages);
            completedEvent.triggerEvent(rects, null);
        } catch (PdfViewerException ex)
        {
            completedEvent.triggerEvent(null, ex);
        }
    }

    List<Integer> pages;
}
