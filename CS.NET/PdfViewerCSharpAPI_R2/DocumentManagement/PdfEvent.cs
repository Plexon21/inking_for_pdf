/****************************************************************************
 *
 * File:            PdfEvent.cs
 *
 * Description:     The 3-Heights™  Pdf Viewer Control for WPF
 *
 * Copyright:       Copyright (C) 2015 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *                  
 ***************************************************************************/

namespace PdfTools.PdfViewerCSharpAPI.DocumentManagement
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;
    using System.Threading;
    using PdfTools.PdfViewerCSharpAPI.Utilities;
    

    /// <summary>
    /// A custom event which passes exceptions from trigger and passes an argument
    /// </summary>
    public class PdfEvent<T>
    {
        private T argument;
        private PdfViewerException ex = null;
        private Semaphore sem = new Semaphore(0, 1);

        /// <summary>
        /// Trigger the event and release one waiting thread, passing an argument
        /// </summary>
        /// <param name="argument">The argument to pass to the waiting thread</param>
        /// <param name="ex">The exception that occured in the triggering thread</param>
        public void TriggerEvent(T argument, PdfViewerException ex)
        {
            this.argument = argument;
            if (ex != null)
            {
                Logger.LogException(ex, 1);
                this.ex = ex;
            }
            try
            {
                sem.Release();
            }
            catch (SemaphoreFullException)
            {
                return;
            }
            if (Completed != null)
                Completed(argument, ex);
        }

        public void TriggerEvent(PdfViewerException ex)
        {
            if (ex == null)
            {
                ex = new PdfViewerException("PdfEvent.TriggerEvent(ex) cannot be called with null");
                Logger.LogException(ex, 1);
            }
            this.ex = ex;
            try
            {
                sem.Release();
            }
            catch (SemaphoreFullException)
            {
                return;
            }
            
            if (Completed != null)
                Completed(argument, ex);
        }

        public event Action<T, PdfViewerException> Completed;


        /// <summary>
        /// Wait for another thread to trigger this event and read the passed argument. throw an exception, if the trigger passes one
        /// </summary>
        /// <returns>Argument to be passed</returns>
        public T WaitOnEvent()
        {
            sem.WaitOne();
            sem.Release();//In case of multiple listeners to the event
            if (ex != null)
            {
                throw ex;
            }
            return argument;
        }
    }
}
