package com.pdf_tools.pdfviewer.Requests;

import java.awt.image.BufferedImage;

import com.pdf_tools.pdfviewer.DocumentManagement.IPdfDocument;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;

public class PdfGetThumbnailRequest extends APdfRequest<BufferedImage>
{

    public PdfGetThumbnailRequest(double sourceWidth, double sourceHeight, int targetWidth, int targetHeight, int page)
    {
        this.sourceWidth = sourceWidth;
        this.sourceHeight = sourceHeight;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.page = page;
        this._priority = 8;
    }

    @Override
    public void execute(IPdfDocument document, IPdfControllerCallbackManager controller)
    {
        try
        {
            BufferedImage bitmap = document.LoadThumbnail(sourceWidth, sourceHeight, targetWidth, targetHeight, page);
            completedEvent.triggerEvent(bitmap, null);
            controller.onThumbnailLoadCompleted(page, bitmap, null);
        } catch (PdfViewerException ex)
        {
            completedEvent.triggerEvent(null, ex);
            controller.onThumbnailLoadCompleted(page, null, ex);
        }
    }

    int targetWidth, targetHeight;
    int page;
    double sourceWidth, sourceHeight;
}
