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
        public IList<PdfAnnotation> Annots;

        public CreateAnnotationArgs(PdfAnnotation annot)
        {
            this.Annots = new List<PdfAnnotation> { annot };
        }
        public CreateAnnotationArgs(IList<PdfAnnotation> annots)
        {
            this.Annots = annots;
        }

        public CreateAnnotationArgs(PdfDocument.TPdfAnnotationType eType,
            int iPage, double[] r, double[] color, double dBorderWidth )
        {
            this.Annots = new List<PdfAnnotation> { new PdfAnnotation(eType, iPage, r, color, dBorderWidth) };
        }
    }

    /// <summary>
    /// Requests the creation of a new annotation
    /// </summary>
    public class PdfCreateAnnotationRequest : APdfRequest<CreateAnnotationArgs, IList<PdfAnnotation>>
    {
        public PdfCreateAnnotationRequest(CreateAnnotationArgs arguments)
            : base(arguments, 60)
        {
        }
        public PdfCreateAnnotationRequest(CreateAnnotationArgs arguments, int priority)
            : base(arguments, priority)
        {
        }

        protected override IList<PdfAnnotation> ExecuteNative(IPdfDocument document, CreateAnnotationArgs args)
        {
            var res = new List<PdfAnnotation>();
            foreach (var annot in args.Annots)
            {
                var newAnnotHandle = document.CreateAnnotation(annot.SubType,
                    annot.PageNr, annot.Rect, annot.Rect.Length, annot.Colors, annot.Colors.Length, annot.BorderWidth);

                var annotSize = Marshal.SizeOf(typeof(PdfDocument.TPdfAnnotation));
                var newAnnot = (PdfDocument.TPdfAnnotation)Marshal.PtrToStructure(newAnnotHandle, typeof(PdfDocument.TPdfAnnotation));
                var newAnnotObj = new PdfAnnotation(newAnnot);
                annot.AnnotId = newAnnotObj.AnnotId;
                res.Add(annot);
            }

            return res;
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
