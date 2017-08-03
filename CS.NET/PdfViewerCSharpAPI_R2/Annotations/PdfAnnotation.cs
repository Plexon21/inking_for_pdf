using PdfTools.PdfViewerCSharpAPI.DocumentManagement;
using PdfTools.PdfViewerCSharpAPI.DocumentManagement.Requests;
using PdfTools.PdfViewerCSharpAPI.Utilities;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Windows.Media;

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
        public double BorderWidth;

        public PdfAnnotation(PdfDocument.TPdfAnnotationType eType, int iPage, double[] r,
            double[] color, double dBorderWidth = 0.0d)
        {
            this.SubType = eType;
            this.PageNr = iPage;
            this.Rect = r;
            this.Colors = color;
            this.BorderWidth = dBorderWidth;
        }

        public PdfAnnotation(PdfDocument.TPdfAnnotation annot)
        {
            this.AnnotationHandle = annot.annotationHandle;
            this.PageNr = annot.pageNr;
            this.SubType = ConvertSubtype(Marshal.PtrToStringAnsi(annot.ptrSubtype));

            double[] colorArray = new double[annot.nrOfColors];
            IntPtr pColorArray = annot.ptrColors;
            for (int i = 0; i < annot.nrOfColors; i++)
            {
                colorArray[i] = (double)Marshal.PtrToStructure(pColorArray, typeof(double));
                pColorArray += Marshal.SizeOf(typeof(double));
            }

            this.Colors = colorArray;
            this.Flags = annot.flags;
            this.Rect = annot.rect;

            double[] quadPointArray = new double[annot.nrOfQuadPoints];
            IntPtr pQuadPoints = annot.ptrQuadPoints;
            for (int i = 0; i < annot.nrOfQuadPoints; i++)
            {
                quadPointArray[i] = (double)Marshal.PtrToStructure(pColorArray, typeof(double));
                pColorArray += Marshal.SizeOf(typeof(double));
            }

            this.QuadPoints = quadPointArray;
            this.Contents = Marshal.PtrToStringAnsi(annot.ptrContents);
            this.IsLink = annot.isLink == 1;
            this.ActionType = Marshal.PtrToStringAnsi(annot.ptrActionType);
            this.HasUri = annot.hasURI == 1;
            this.Uri = Marshal.PtrToStringAnsi(annot.ptrURI);
            this.DestType = annot.destType;
            this.HasDestVal = annot.hasDestVal.Select(num => num == 1).ToArray();
            this.DestArray = annot.destArray;
            this.IsMarkup = annot.isMarkup == 1;
            this.TextLabel = Marshal.PtrToStringAnsi(annot.ptrTextLabel);
            this.HasPopup = annot.hasPopup == 1;
            this.PopupAnnotation = annot.m_pPopupAnnot;
        }
        public PdfAnnotation(PdfAnnotation annot)
        {
            AnnotationHandle = annot.AnnotationHandle;
            PageNr = annot.PageNr;
            SubType = annot.SubType;
            var tmpCol = new double[annot.Colors.Length];
            for (int i = 0; i < annot.Colors.Length; i++)
            {
                tmpCol[i] = annot.Colors[i];
            }
            Colors = tmpCol;
            Flags = annot.Flags;
            if (annot.Rect == null) Rect = null;
            else
            {
                var tmpRect = new double[annot.Rect.Length];
                for (int i = 0; i < annot.Rect.Length; i++)
                {
                    tmpRect[i] = annot.Rect[i];
                }
                Rect = tmpRect;
            }
            if (annot.QuadPoints == null) QuadPoints = null;
            else
            {
                var tmpQuad = new double[annot.QuadPoints.Length];
                for (int i = 0; i < annot.QuadPoints.Length; i++)
                {
                    tmpQuad[i] = annot.QuadPoints[i];
                }
                QuadPoints = tmpQuad;
            }
            Contents = annot.Contents;
            IsLink = annot.IsLink;
            ActionType = annot.ActionType;
            HasUri = annot.HasUri;
            Uri = annot.Uri;
            DestType = annot.DestType;
            if (annot.HasDestVal == null) HasDestVal = null;
            else
            {
                var tmpHDest = new bool[annot.HasDestVal.Length];
                for (int i = 0; i < annot.HasDestVal.Length; i++)
                {
                    tmpHDest[i] = annot.HasDestVal[i];
                }
                HasDestVal = tmpHDest;
            }
            if (annot.DestArray == null) DestArray = null;
            else
            {
                var tmpDest = new double[annot.DestArray.Length];
                for (int i = 0; i < annot.DestArray.Length; i++)
                {
                    tmpDest[i] = annot.DestArray[i];
                }
                DestArray = tmpDest;
            }
            IsMarkup = annot.IsMarkup;
            TextLabel = annot.TextLabel;
            HasPopup = annot.HasPopup;
            PopupAnnotation = annot.PopupAnnotation;
            BorderWidth = annot.BorderWidth;
        }

        public long GetHandleAsLong()
        {
            return AnnotationHandle.ToInt64();
        }

        public void SetHandleFromLong(long value)
        {
            this.AnnotationHandle = new IntPtr(value);
        }

        public static PdfDocument.TPdfAnnotationType ConvertSubtype(string annotSubType)
        {
            switch (annotSubType.ToLower(CultureInfo.InvariantCulture))
            {
                // Add further annotation-types here
                case "eannotationtext":
                case "text":
                    return PdfDocument.TPdfAnnotationType.eAnnotationText;
                case "eannotationlink":
                case "link":
                    return PdfDocument.TPdfAnnotationType.eAnnotationLink;
                case "eannotationfreetext":
                case "freetext":
                    return PdfDocument.TPdfAnnotationType.eAnnotationFreeText;
                case "eannotationhighlight":
                case "highlight":
                    return PdfDocument.TPdfAnnotationType.eAnnotationHighlight;
                case "eannotationink":
                case "ink":
                    return PdfDocument.TPdfAnnotationType.eAnnotationInk;
                case "eannotationpopup":
                case "popup":
                    return PdfDocument.TPdfAnnotationType.eAnnotationPopup;
                case "eannotationwidet":
                case "widget":
                    return PdfDocument.TPdfAnnotationType.eAnnotationWidet;
                default:
                    return PdfDocument.TPdfAnnotationType.eAnnotationInk;
            }
        }



        public bool ContainsOrIntersectsWithRect(PdfSourceRect markedRect, bool OnIntersect)
        {
            PdfSourceRect annotRect = new PdfSourceRect(Rect[0], Rect[1], Rect[2] - Rect[0], Rect[3] - Rect[1]);

            if (OnIntersect)
            {
                return markedRect.intersectsDouble(annotRect);
            }
            return markedRect.contains(annotRect);
        }

        public bool ContainsPoint(PdfSourcePoint point)
        {
            return Rect[0] < point.dX && Rect[1] < point.dY && Rect[2] > point.dX && Rect[3] > point.dY;
        }

        public UpdateAnnotation Move(double x, double y)
        {
            double[] rect = new double[] { Rect[0] + x, Rect[1] + y, Rect[2] + x, Rect[3] + y };

            return new UpdateAnnotation(this, rect, null, null, null, -1);
        }

        public UpdateAnnotation Scale(double factor) //this is just to test... TODO: delete
        {
            double width = Rect[2] - Rect[0];
            double height = Rect[3] - Rect[1];
            double newWidth = width * factor;
            double newHeight = height * factor;

            double[] rect = new double[] {
                Rect[0] + (width/2) - (newWidth/2),
                Rect[1] + (height/2) - (newHeight/2),
                Rect[2] - (width/2) + (newWidth/2),
                Rect[3] - (height/2) + (newHeight/2)
            };

            return new UpdateAnnotation(this, rect, null, null, null, -1);
        }

        public UpdateAnnotation UpdateContent(string content)
        {
            return new UpdateAnnotation(this, null, content, null, null, -1);
        }

        public UpdateAnnotation UpdateLabel(string label)
        {
            return new UpdateAnnotation(this, null, null, label, null, -1);
        }

        public UpdateAnnotation UpdateColor(Color color)
        {
            double[] colorArray = new double[] { color.R / 255.0, color.G / 255.0, color.B / 255.0 };
            return new UpdateAnnotation(this, null, null, null, colorArray, -1);
        }

        public UpdateAnnotation UpdateWidth(double width)
        {
            return new UpdateAnnotation(this, null, null, null, null, width);
        }
    }
}
