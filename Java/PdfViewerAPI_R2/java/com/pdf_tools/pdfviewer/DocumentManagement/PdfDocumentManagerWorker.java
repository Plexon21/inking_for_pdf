package com.pdf_tools.pdfviewer.DocumentManagement;

import java.util.concurrent.PriorityBlockingQueue;

import com.pdf_tools.pdfviewer.Model.DebugLogger;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager;
import com.pdf_tools.pdfviewer.Requests.IPdfRequest;

public class PdfDocumentManagerWorker implements Runnable
{

    public PdfDocumentManagerWorker(IPdfControllerCallbackManager controller, PriorityBlockingQueue<IPdfRequest> requestQueue,
            IPdfDocument document)
    {
        this.controller = controller;
        this.requestQueue = requestQueue;
        this.document = document;
    }

    public void run()
    {
        IPdfRequest request;
        while (true)
        {
            // wait for something to pop of queue
            try
            {
                request = requestQueue.take();
            } catch (InterruptedException e)
            {
                DebugLogger.log("DocumentWorker was interrupted - terminating");
                e.printStackTrace();
                return;
            }
            // execute the request
            request.execute(document, controller);
        }
    }

    private IPdfControllerCallbackManager controller;
    private PriorityBlockingQueue<IPdfRequest> requestQueue;
    private IPdfDocument document;

}
