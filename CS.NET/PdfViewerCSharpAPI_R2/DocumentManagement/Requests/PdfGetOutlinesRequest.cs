// -----------------------------------------------------------------------
// <copyright file="PdfGetOutlinesRequest.cs" company="">
// TODO: Update copyright text.
// </copyright>
// -----------------------------------------------------------------------

namespace PdfTools.PdfViewerCSharpAPI.DocumentManagement.Requests
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;
    using PdfTools.PdfViewerCSharpAPI.Model;
    using PdfTools.PdfViewerCSharpAPI.Utilities;

    /// <summary>
    /// TODO: Update summary.
    /// </summary>
    public class PdfGetOutlinesRequest : APdfRequest<int, IList<PdfOutlineItem>>
    {

        public PdfGetOutlinesRequest(int outlineId)
            :base(outlineId, 72)
        {
        }

        protected override IList<PdfOutlineItem> ExecuteNative(IPdfDocument document, int outlineId)
        {
            return document.GetOutlines(outlineId);       
        }

        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, InOutTuple tuple, PdfViewerException ex)
        {
            controller.OnOutlinesLoaded(ex, tuple.arguments, tuple.output);
        }
        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, PdfViewerException ex)
        {
            controller.OnOutlinesLoaded(ex, this.arguments, null);
        }
    }
}
