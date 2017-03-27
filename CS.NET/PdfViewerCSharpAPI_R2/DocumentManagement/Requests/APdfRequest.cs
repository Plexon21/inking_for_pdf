/****************************************************************************
 *
 * File:            IPdfRequest
.cs
 *
 * Description:     The 3-Heights™  Pdf Viewer Control for WPF
 *
 * Copyright:       Copyright (C) 2015 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *                  
 ***************************************************************************/

namespace PdfTools.PdfViewerCSharpAPI.DocumentManagement.Requests
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;
    using PdfTools.PdfViewerCSharpAPI.Model;
    using PdfTools.PdfViewerCSharpAPI.Utilities;


    /// <summary>
    /// Interface for implementing Requests, that get performed on the Document in a background thread
    /// </summary>
    /// <typeparam name="T">The type of the parameter that is returned when waiting succeded</typeparam>
    public abstract class APdfRequest<Arguments, Output> : IPdfAwaitable<APdfRequest<Arguments, Output>.InOutTuple>, IPdfRequest
    {
        public struct InOutTuple
        {
            public Arguments arguments;
            public Output output;
            public InOutTuple(Arguments arguments, Output output)
            {
                this.arguments = arguments;
                this.output = output;
            }
        }


        public APdfRequest(Arguments arguments, int priority)
        {
            this.arguments = arguments;
            completedEvent = new PdfEvent<InOutTuple>();
            this.Priority = priority;
        }

        private int _priority;
        /// <summary>
        /// The priority of the associated request in the queue. Higher number means higher priority
        /// </summary>
        public int Priority
        {
            get
            {
                return _priority;
            }
            protected set
            {
                _priority = value;
            }
        }

        public override String ToString()
        {
            return this.GetType().Name + " Priority=" + Priority;
        }

        protected Arguments arguments;

        public Arguments InputArguments
        {
            get
            {
                return arguments; 
            }
        }

        //public abstract void Execute(IPdfDocument document, IPdfControllerCallbackManager controller);
        public virtual void Execute(IPdfDocument document, IPdfControllerCallbackManager controller)
        {
            try
            {
                InOutTuple tuple = new InOutTuple(arguments, ExecuteNative(document, arguments));
                this.completedEvent.TriggerEvent(tuple, null);
                triggerControllerCallback(controller, tuple, null);
            }
            catch (PdfViewerException ex)
            {
                this.completedEvent.TriggerEvent(ex);
                triggerControllerCallback(controller, ex);
            }
        }

        protected abstract Output ExecuteNative(IPdfDocument document, Arguments arguments);

        protected abstract void triggerControllerCallback(IPdfControllerCallbackManager controller, InOutTuple tuple, PdfViewerException ex);
        protected abstract void triggerControllerCallback(IPdfControllerCallbackManager controller, PdfViewerException ex);

        protected PdfEvent<InOutTuple> completedEvent;

        public event Action<InOutTuple, PdfViewerException> Completed
        {
            add { completedEvent.Completed += value; }
            remove { completedEvent.Completed -= value; }
        }

        public InOutTuple Wait()
        {
            return completedEvent.WaitOnEvent();
        }

        public void Cancel()
        {
            completedEvent.TriggerEvent(new PdfRequestCanceledException());
        }
    }
}
