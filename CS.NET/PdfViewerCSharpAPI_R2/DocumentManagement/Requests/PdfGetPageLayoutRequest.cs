// -----------------------------------------------------------------------
// <copyright file="PdfGetLayoutModeRequest.cs" company="">
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
    public class PdfGetPageLayoutRequest : APdfRequest<object, TPageLayoutMode>
    {
        public PdfGetPageLayoutRequest()
            :base(null, 85)
        {
        }

        protected override TPageLayoutMode ExecuteNative(IPdfDocument document, object nothing)
        {
            return document.GetPageLayout();
        }

        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, InOutTuple tuple, PdfViewerException ex)
        {
        }
        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, PdfViewerException ex)
        {
        }
    }
}
