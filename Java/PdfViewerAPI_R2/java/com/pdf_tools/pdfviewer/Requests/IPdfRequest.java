package com.pdf_tools.pdfviewer.Requests;

import com.pdf_tools.pdfviewer.DocumentManagement.IPdfDocument;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager;
import com.pdf_tools.pdfviewer.Model.PdfViewerException.NoFileOpenedException;

/**
 * @author fwe Interface for Requests, that can be queued to be executed on a
 *         PdfDocument.
 */
public interface IPdfRequest
{

    /**
     * @returns priority, the larger the number, the higher the priority
     */
    public int getPriority();

    /**
     * Execute the Request
     * 
     * @param document
     *            the document that the request should be performed on
     * @param controller
     *            the Controller, that has to be informed when execution has
     *            finished
     * @throws NoFileOpenedException
     *             If the given document has no file opened
     */
    public void execute(IPdfDocument document, IPdfControllerCallbackManager controller);
}
