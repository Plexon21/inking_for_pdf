using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using PdfTools.PdfViewerCSharpAPI.Annotations;
using PdfTools.PdfViewerCSharpAPI.Model;
using PdfTools.PdfViewerCSharpAPI.Utilities;

namespace PdfTools.PdfViewerCSharpAPI.DocumentManagement.Requests
{

    public struct PdfLoadAnnotationsOnPageArgs
    {
        public int PageNr;

        public PdfLoadAnnotationsOnPageArgs(int pageNr)
        {
            this.PageNr = pageNr;
        }
    }
    public class PdfLoadAnnotationsOnPageRequest : APdfRequest<PdfLoadAnnotationsOnPageArgs, IList<PdfAnnotation>>
    {
        public PdfLoadAnnotationsOnPageRequest(PdfLoadAnnotationsOnPageArgs arguments)
            : base(arguments, 42)
        {
        }
        public PdfLoadAnnotationsOnPageRequest(PdfLoadAnnotationsOnPageArgs arguments, int priority)
            : base(arguments, priority)
        {
        }

        protected override IList<PdfAnnotation> ExecuteNative(IPdfDocument document, PdfLoadAnnotationsOnPageArgs args)
        {
           var annotations = document.LoadAnnotations(args.PageNr);
            return annotations;
        }

        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, InOutTuple tuple, PdfViewerException ex)
        {
            controller?.OnAnnotationsLoaded(ex,tuple.output);
        }

        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, PdfViewerException ex)
        {
            controller?.OnAnnotationsLoaded(ex, null);
        }
    }
}
