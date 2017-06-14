using PdfTools.PdfViewerCSharpAPI.DocumentManagement;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;

namespace PdfTools.PdfViewerCSharpAPI.Annotations
{
    public class PdfAnnotation
    {
        public IntPtr AnnotationHandle;
        public int PageNr;
        public PdfDocument.TPdfAnnotationType SubType;
        public double[] Colors;
        public int Flags;
        public double[] Rect;
        public double[] QuadPoints;
        public string Contents;
        public bool IsLink;
        public string ActionType;
        public bool HasUri;
        public string Uri;
        public int DestType;
        public bool[] HasDestVal;
        public double[] DestArray;
        public bool IsMarkup;
        public string TextLabel;
        public bool HasPopup;
        public IntPtr PopupAnnotation;

        public PdfAnnotation(PdfDocument.TPdfAnnotationType eType, int iPage, double[] r,
            double[] color, double dBorderWidth = 0.0d)
        {
            this.SubType = eType;
            this.PageNr = iPage;
            this.Rect = r;
        }

        public PdfAnnotation(PdfDocument.TPdfAnnotation annot)
        {
            this.AnnotationHandle = annot.annotationHandle;
            this.PageNr = annot.pageNr;
            this.SubType = ConvertSubtype(annot.subType);
            this.Colors = annot.colors;
            this.Flags = annot.flags;
            this.Rect = annot.rect;
            this.QuadPoints = annot.quadPoints;
            this.Contents = annot.contents;
            this.IsLink = annot.isLink;
            this.ActionType = annot.actionType;
            this.HasUri = annot.hasURI;
            this.Uri = annot.URI;
            this.DestType = annot.destType;
            this.HasDestVal = annot.hasDestVal;
            this.DestArray = annot.destArray;
            this.IsMarkup = annot.isMarkup;
            this.TextLabel = annot.textLabel;
            this.HasPopup = annot.hasPopup;
            this.PopupAnnotation = annot.m_pPopupAnnot;
        }

        private PdfDocument.TPdfAnnotationType ConvertSubtype(string annotSubType)
        {
            switch (annotSubType)
            {
                case "Ink":
                    return PdfDocument.TPdfAnnotationType.eAnnotationInk;
                default:
                    return PdfDocument.TPdfAnnotationType.eAnntationUnknown;
            }
        }

        public PdfAnnotation(PdfAnnotation annot)
        {
            AnnotationHandle = annot.AnnotationHandle;
            PageNr = annot.PageNr;
            SubType = annot.SubType;
            Colors = annot.Colors;
            Flags = annot.Flags;
            Rect = annot.Rect;
            QuadPoints = annot.QuadPoints;
            Contents = annot.Contents;
            IsLink = annot.IsLink;
            ActionType = annot.ActionType;
            HasUri = annot.HasUri;
            Uri = annot.Uri;
            DestType = annot.DestType;
            HasDestVal = annot.HasDestVal;
            DestArray = annot.DestArray;
            IsMarkup = annot.IsMarkup;
            TextLabel = annot.TextLabel;
            HasPopup = annot.HasPopup;
            PopupAnnotation = annot.PopupAnnotation;
        }
    }
}
