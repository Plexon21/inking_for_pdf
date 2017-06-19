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

    public struct DeleteAnnotationArgs
    {
        public long annotationHandle;

        public DeleteAnnotationArgs(long handle)
        {
            this.annotationHandle = handle;
        }
      
    }
    public class PdfDeleteAnnotationRequest : APdfRequest<DeleteAnnotationArgs,long>
    {
        public PdfDeleteAnnotationRequest(DeleteAnnotationArgs arguments)
            : base(arguments, 42)
        {
        }
        public PdfDeleteAnnotationRequest(DeleteAnnotationArgs arguments, int priority)
            : base(arguments, priority)
        {
        }

        protected override long ExecuteNative(IPdfDocument document, DeleteAnnotationArgs args)
        {
            document.DeleteAnnotation(new IntPtr(args.annotationHandle));
            return args.annotationHandle;
        }

        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, InOutTuple tuple, PdfViewerException ex)
        {
            controller.OnAnnotationDeleted(ex, tuple.output);
        }

        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, PdfViewerException ex)
        {
            controller.OnAnnotationDeleted(ex, null);
        }
    }
}
