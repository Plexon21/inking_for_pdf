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
        public IList<IntPtr> annotationHandles;

        public DeleteAnnotationArgs(IntPtr handle)
        {
            this.annotationHandles = new List<IntPtr> { handle };
        }
        public DeleteAnnotationArgs(IList<IntPtr> handles)
        {
            this.annotationHandles = handles;
        }

    }
    public class PdfDeleteAnnotationRequest : APdfRequest<DeleteAnnotationArgs, IList<IntPtr>>
    {
        public PdfDeleteAnnotationRequest(DeleteAnnotationArgs arguments)
            : base(arguments, 50)
        {
        }
        public PdfDeleteAnnotationRequest(DeleteAnnotationArgs arguments, int priority)
            : base(arguments, priority)
        {
        }

        protected override IList<IntPtr> ExecuteNative(IPdfDocument document, DeleteAnnotationArgs args)
        {
            foreach (var handle in args.annotationHandles)
            {
                document.DeleteAnnotation(handle);
            }
            return args.annotationHandles;
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
