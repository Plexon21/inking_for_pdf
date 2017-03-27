package com.pdf_tools.pdfviewer.Requests;

import com.pdf_tools.pdfviewer.DocumentManagement.IPdfDocument;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;

public abstract class APdfRequest<T> implements IPdfRequest
{

    public APdfRequest()
    {
        completedEvent = new PdfEvent<T>();
    }

    protected int _priority;

    public int getPriority()
    {
        return _priority;
    }

    public abstract void execute(IPdfDocument document, IPdfControllerCallbackManager controller);

    public T waitForCompletion() throws PdfViewerException
    {
        return completedEvent.waitOnEvent();
    }

    protected PdfEvent<T> completedEvent;

}
