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

        public CreateAnnotationArgs(PdfDocument.TPdfAnnotationType eType, 
            int iPage, double[] r, double[] color, double dBorderWidth = 0.0d)
        {
            this.Annot = new PdfAnnotation(eType, iPage, r, color, dBorderWidth);
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
            var newAnnot = document.CreateAnnotation(args.Annot.SubType,
                args.Annot.PageNr, args.Annot.Rect, args.Annot.Rect.Length, args.Annot.Colors, args.Annot.Colors.Length, args.Annot.BorderWidth);
            args.Annot.AnnotationHandle = newAnnot;
            return new PdfAnnotation(arguments.Annot);
        }

        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, InOutTuple tuple, PdfViewerException ex)
        {
            controller.OnAnnotationCreated(ex, tuple.output);
        }

        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, PdfViewerException ex)
        {
            controller.OnAnnotationCreated(ex, null);
        }
    }
}
