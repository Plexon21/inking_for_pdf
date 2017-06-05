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

    public struct CreateAnnotationArgs
    {
        public PdfAnnotation Annot;

        public CreateAnnotationArgs(PdfAnnotation annot)
        {
            this.Annot = annot;
        }
    }
    public class PdfCreateAnnotationRequest : APdfRequest<CreateAnnotationArgs, PdfAnnotation>
    {
        public PdfCreateAnnotationRequest(CreateAnnotationArgs arguments)
            : base(arguments, 42)
        {
        }
        public PdfCreateAnnotationRequest(CreateAnnotationArgs arguments, int priority)
            : base(arguments, priority)
        {
        }

        protected override PdfAnnotation ExecuteNative(IPdfDocument document, CreateAnnotationArgs args)
        {
            var newAnnot = document.CreateAnnotation(PdfDocument.TPdfAnnotationType.eAnnotationFreeText,
                args.Annot.PageNr, args.Annot.Rect, 0, args.Annot.Colors, args.Annot.Colors.Length, 0.0);
            args.Annot.AnnotationHandle = newAnnot;
            return new PdfAnnotation(arguments.Annot);
        }

        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, InOutTuple tuple, PdfViewerException ex)
        {
            controller?.OnAnnotationCreated(ex, tuple.output);
        }

        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, PdfViewerException ex)
        {
            controller?.OnAnnotationCreated(ex, null);
        }
    }
}
