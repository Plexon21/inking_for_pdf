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
        public double[] r;
        public string content;
        public string label;
        public double[] color;
        public double dBorderWidth;


        public UpdateAnnotationArgs(PdfAnnotation annot, double[] r, string content, string label, double[] color, double dBorderWidth)
        {
            this.Annot = annot;
            this.r = r;
            this.content = content;
            this.label = label;
            this.color = color;
            this.dBorderWidth = dBorderWidth;
        }
    }
    public class PdfUpdateAnnotaionRequest : APdfRequest<UpdateAnnotationArgs, int>
    {
        public PdfUpdateAnnotaionRequest(UpdateAnnotationArgs arguments)
            : base(arguments, 43) //TODO: why 43
        {
        }
        public PdfUpdateAnnotaionRequest(UpdateAnnotationArgs arguments, int priority)
            : base(arguments, priority)
        {
        }

        protected override int ExecuteNative(IPdfDocument document, UpdateAnnotationArgs args)
        {
            var result = document.UpdateAnnotation(args.Annot.AnnotationHandle,
                args.Annot.PageNr, args.r, args.content, args.label, args.color,
                args.dBorderWidth);
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
