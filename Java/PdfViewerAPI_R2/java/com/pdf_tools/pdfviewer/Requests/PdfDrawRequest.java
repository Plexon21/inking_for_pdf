package com.pdf_tools.pdfviewer.Requests;

import java.awt.image.BufferedImage;
import java.util.Map;

import com.pdf_tools.pdfviewer.DocumentManagement.IPdfDocument;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager;
import com.pdf_tools.pdfviewer.Model.PdfViewerController;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;
import com.pdf_tools.pdfviewer.converter.geom.Dimension;
import com.pdf_tools.pdfviewer.converter.geom.Rectangle;

public class PdfDrawRequest extends APdfRequest<BufferedImage>
{

    public PdfDrawRequest(Dimension bitmapSize, int rotation, Map<Integer, Rectangle.Double> pageRects,
            PdfViewerController.Viewport viewport)
    {
        this.bitmapSize = bitmapSize;
        this.rotation = rotation;
        this._priority = 10;
        this.pageRects = pageRects;
        this.viewport = viewport;
    }

    @Override
    public void execute(IPdfDocument document, IPdfControllerCallbackManager controller)
    {
        if (bitmapSize.height > 0 && bitmapSize.width > 0)
        {
            BufferedImage bitmap = new BufferedImage(bitmapSize.width, bitmapSize.height, BufferedImage.TYPE_4BYTE_ABGR);

            try
            {
                document.draw(bitmap, rotation, pageRects, viewport);
                completedEvent.triggerEvent(bitmap, null);
                controller.onDrawCompleted(bitmap, null);
            } catch (PdfViewerException ex)
            {
                completedEvent.triggerEvent(bitmap, ex);
                controller.onDrawCompleted(bitmap, ex);
            }
        }
    }

    private Dimension bitmapSize;
    private int rotation;
    private Map<Integer, Rectangle.Double> pageRects;
    private PdfViewerController.Viewport viewport;

}
