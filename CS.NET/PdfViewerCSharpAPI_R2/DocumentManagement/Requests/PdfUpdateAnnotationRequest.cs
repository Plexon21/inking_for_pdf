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
    public class UpdateAnnotation
    {
        public PdfAnnotation Annot;
        public double[] r;
        public string content;
        public string label;
        public double[] color;
        public double dBorderWidth;

        public UpdateAnnotation(PdfAnnotation annot, double[] r, string content, string label, double[] color, double dBorderWidth)
        {
            this.Annot = annot;
            this.r = r;
            this.content = content;
            this.label = label;
            this.color = color;
            this.dBorderWidth = dBorderWidth;
        }
    }

    public struct UpdateAnnotationArgs
    {
        public IList<UpdateAnnotation> updateAnnots;


        public UpdateAnnotationArgs(PdfAnnotation annot, double[] r, string content, string label, double[] color, double dBorderWidth)
        {
            updateAnnots = new List<UpdateAnnotation>() { new UpdateAnnotation(annot, r, content, label, color, dBorderWidth) };
        }

        public UpdateAnnotationArgs(IList<UpdateAnnotation> annots)
        {
            updateAnnots = annots;
        }
        public UpdateAnnotationArgs(UpdateAnnotation annot)
        {
            updateAnnots = new List<UpdateAnnotation>() { annot };
        }
    }
    public class PdfUpdateAnnotaionRequest : APdfRequest<UpdateAnnotationArgs, IList<int>>
    {
        public PdfUpdateAnnotaionRequest(UpdateAnnotationArgs arguments)
            : base(arguments, 55)
        {
        }
        public PdfUpdateAnnotaionRequest(UpdateAnnotationArgs arguments, int priority)
            : base(arguments, priority)
        {
        }

        protected override IList<int> ExecuteNative(IPdfDocument document, UpdateAnnotationArgs args)
        {
            var results = new List<int>();
            foreach (var annot in args.updateAnnots)
            {
                var result = document.UpdateAnnotation(annot.Annot.AnnotationHandle,
                    annot.Annot.PageNr, annot.r, annot.content, annot.label, annot.color,
                    annot.dBorderWidth);
                results.Add(result);
            }
            return results;
        }

        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, InOutTuple tuple, PdfViewerException ex)
        {
            controller.OnAnnotationUpdate(ex, tuple.output);
        }

        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, PdfViewerException ex)
        {
            controller.OnAnnotationUpdate(ex, new List<int>(){-1});
        }
    }
}
