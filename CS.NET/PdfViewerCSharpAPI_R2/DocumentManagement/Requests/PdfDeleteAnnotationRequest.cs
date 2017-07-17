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
        public IList<long> annotationHandles;

        public DeleteAnnotationArgs(long handle)
        {
            this.annotationHandles = new List<long> { handle };
        }
        public DeleteAnnotationArgs(IList<long> handles)
        {
            this.annotationHandles = handles;
        }

    }
    public class PdfDeleteAnnotationRequest : APdfRequest<DeleteAnnotationArgs, IList<long>>
    {
        public PdfDeleteAnnotationRequest(DeleteAnnotationArgs arguments)
            : base(arguments, 45)
        {
        }
        public PdfDeleteAnnotationRequest(DeleteAnnotationArgs arguments, int priority)
            : base(arguments, priority)
        {
        }

        protected override IList<long> ExecuteNative(IPdfDocument document, DeleteAnnotationArgs args)
        {
            foreach (var handle in args.annotationHandles)
            {
                document.DeleteAnnotation(new IntPtr(handle));
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
