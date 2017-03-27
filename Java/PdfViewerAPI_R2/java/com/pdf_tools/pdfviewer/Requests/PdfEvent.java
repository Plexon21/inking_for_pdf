package com.pdf_tools.pdfviewer.Requests;

import java.util.concurrent.Semaphore;

import com.pdf_tools.pdfviewer.Model.PdfViewerException;

/**
 * @author fwe Event that
 * @param <T>
 */
public class PdfEvent<T>
{

    /**
     * Trigger the event (all wait invocations return)
     * 
     * @param argument
     *            The object to return to event listeners
     * @param ex
     *            An PdfViewerException occured in the worker (null if none)
     */
    public void triggerEvent(T argument, PdfViewerException ex)
    {
        synchronized (sem)
        {
            this.argument = argument;
            this.ex = ex;
        }
        sem.release();
    }

    /**
     * Trigger the event (all wait invocations return)
     * 
     * @param ex
     *            An PdfViewerException occured in the worker (null if none)
     */
    public void triggerEvent(PdfViewerException ex)
    {
        triggerEvent(null, ex);
    }

    /**
     * wait for the event to be triggered
     * 
     * @returns An Object T given by trigger
     * @throws PdfViewerException,
     *             any PdfViewerException that occured in worker thread
     * @throws InterruptedException
     */
    public T waitOnEvent() throws PdfViewerException
    {
        try
        {
            sem.acquire();
        } catch (InterruptedException e)
        {
            throw new PdfViewerException("Native thread was interrupted");
        }
        sem.release();
        synchronized (sem)
        {
            if (ex != null)
            {
                throw ex;
            }
            return argument;
        }
    }

    private T argument = null;
    private PdfViewerException ex = null;
    private Semaphore sem = new Semaphore(0);
}
