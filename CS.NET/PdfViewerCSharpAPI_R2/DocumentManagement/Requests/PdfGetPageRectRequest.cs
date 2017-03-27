// -----------------------------------------------------------------------
// <copyright file="PdfGetPageRangeRequest.cs" company="">
// TODO: Update copyright text.
// </copyright>
// -----------------------------------------------------------------------

namespace PdfTools.PdfViewerCSharpAPI.DocumentManagement.Requests 
{
    using System;
    using System.Collections.Generic;
    using System.Collections.Concurrent;
    using System.Linq;
    using System.Text;
    using PdfTools.PdfViewerCSharpAPI.Utilities;
    using PdfTools.PdfViewerCSharpAPI.Model;

    /// <summary>
    /// TODO: Update summary.
    /// </summary>
    public class PdfGetPageRectRequest : APdfRequest<int, PdfSourceRect>
    {

        public PdfGetPageRectRequest(int page)
            : base(page, 80)
        {
        }

        protected override PdfSourceRect ExecuteNative(IPdfDocument document, int page)
        {
            Utilities.Logger.LogInfo("Getting page " + page);
            return document.GetPageRect(page);
        }

        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, InOutTuple tuple, PdfViewerException ex)
        {
        }
        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, PdfViewerException ex)
        {
        }
    }
}
