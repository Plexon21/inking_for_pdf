// -----------------------------------------------------------------------
// <copyright file="PdfGetOpenActionDestinationRequest.cs" company="">
// TODO: Update copyright text.
// </copyright>
// -----------------------------------------------------------------------

namespace PdfTools.PdfViewerCSharpAPI.DocumentManagement.Requests
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;
    using PdfTools.PdfViewerCSharpAPI.Utilities;
    using PdfTools.PdfViewerCSharpAPI.Model;

    /// <summary>
    /// TODO: Update summary.
    /// </summary>
    public class PdfGetOpenActionDestinationRequest : APdfRequest<object, PdfDestination>
    {
        public PdfGetOpenActionDestinationRequest()
            : base(null, 88)
        {
        }

        protected override PdfDestination ExecuteNative(IPdfDocument document, object nothing)
        {
            return document.GetOpenActionDestination();
        }


        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, InOutTuple tuple, PdfViewerException ex)
        {
        }
        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, PdfViewerException ex)
        {
        }
    }
}
