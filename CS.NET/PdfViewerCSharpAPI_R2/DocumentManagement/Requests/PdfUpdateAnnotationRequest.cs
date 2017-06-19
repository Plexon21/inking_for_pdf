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

    public struct UpdateAnnotationArgs
    {
        public PdfAnnotation Annot;

        public UpdateAnnotationArgs(PdfAnnotation annot)
        {
            this.Annot = annot;
        }
    }
    public class PdfUpdateAnnotaionRequest : APdfRequest<UpdateAnnotationArgs, int>
    {
        public PdfUpdateAnnotaionRequest(UpdateAnnotationArgs arguments)
            : base(arguments, 43)
        {
        }
        public PdfUpdateAnnotaionRequest(UpdateAnnotationArgs arguments, int priority)
            : base(arguments, priority)
        {
        }

        protected override int ExecuteNative(IPdfDocument document, UpdateAnnotationArgs args)
        {
            var result = document.UpdateAnnotation(args.Annot.AnnotationHandle,
                args.Annot.PageNr, args.Annot.Rect, args.Annot.Contents, args.Annot.TextLabel, args.Annot.Colors,
                args.Annot.BorderWidth);
            return result;
        }

        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, InOutTuple tuple, PdfViewerException ex)
        {
            controller.OnAnnotationUpdate(ex, tuple.output);
        }

        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, PdfViewerException ex)
        {
            controller.OnAnnotationUpdate(ex, -1);
        }
    }
}
