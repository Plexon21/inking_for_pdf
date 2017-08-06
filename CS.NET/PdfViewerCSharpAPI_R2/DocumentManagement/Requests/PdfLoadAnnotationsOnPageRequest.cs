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

    public struct PdfGetAnnotationsOnPageArgs
    {
        public int PageNr;

        public PdfGetAnnotationsOnPageArgs(int pageNr)
        {
            this.PageNr = pageNr;
        }
    }
    public class PdfGetAnnotationsOnPageRequest : APdfRequest<PdfGetAnnotationsOnPageArgs, IList<PdfAnnotation>>
    {
        public PdfGetAnnotationsOnPageRequest(PdfGetAnnotationsOnPageArgs arguments)
            : base(arguments, 45)
        {
        }
        public PdfGetAnnotationsOnPageRequest(PdfGetAnnotationsOnPageArgs arguments, int priority)
            : base(arguments, priority)
        {
        }

        protected override IList<PdfAnnotation> ExecuteNative(IPdfDocument document, PdfGetAnnotationsOnPageArgs args)
        {
           var annotations = document.GetAnnotations(args.PageNr);
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
