/****************************************************************************
 *
 * File:            PdfCloseRequest.cs
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
    using PdfTools.PdfViewerCSharpAPI.Utilities;
    using PdfTools.PdfViewerCSharpAPI.Model;

    /// <summary>
    /// Requests closing the opened document
    /// </summary>
    public class PdfCloseRequest :APdfRequest<bool, object>
    {
        public PdfCloseRequest(bool triggerCallback)
            :base(triggerCallback, 100)
        {
        }

        public override void Execute(IPdfDocument document, IPdfControllerCallbackManager controller)
        {
            APdfRequest<bool, object>.InOutTuple tuple = new APdfRequest<bool, object>.InOutTuple(this.arguments, null);
            try
            {
                document.Close();
                this.completedEvent.TriggerEvent(tuple, null);
                if (this.arguments)
                    controller.OnCloseCompleted(null);
            }
            catch (PdfViewerException ex)
            {
                this.completedEvent.TriggerEvent(tuple, ex);
                if (this.arguments)
                    controller.OnCloseCompleted(ex);
            }
        }

        protected override object ExecuteNative(IPdfDocument document, bool arguments)
        {
            throw new NotImplementedException("This should not be called because APdfRequest.Execute has been overridden");
        }

        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, InOutTuple tuple, PdfViewerException ex)
        {
            throw new NotImplementedException("This should not be called because APdfRequest.Execute has been overridden");
        }
        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, PdfViewerException ex)
        {
            throw new NotImplementedException("This should not be called because APdfRequest.Execute has been overridden");
        }
    }
}
