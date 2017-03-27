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
    public class PdfGetTextFragmentsRequest : APdfRequest<int, IList<PdfTextFragment>>
    {
        public PdfGetTextFragmentsRequest(int page)
            : base(page, 40)
        { }

        protected override IList<PdfTextFragment> ExecuteNative(IPdfDocument document, int page)
        {
            return document.LoadTextFragments(page);
        }

        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, InOutTuple tuple, PdfViewerException ex)
        {
        }
        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, PdfViewerException ex)
        {
        }
    }
}
