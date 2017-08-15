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

    /// <summary>
    /// Representation of an Annotation
    /// </summary>
    public class PdfAnnotation
    {
        public IntPtr AnnotId;
        public readonly int PageNr;
        public readonly PdfDocument.TPdfAnnotationType SubType;
        public readonly double[] Colors;
        public readonly int Flags;
        public readonly double[] Rect;
        public readonly double[] QuadPoints;
        public readonly string Contents;
        public readonly bool IsLink;
        public readonly string ActionType;
        public readonly bool HasUri;
        public readonly string Uri;
        public readonly int DestType;
        public readonly bool[] HasDestVal;
        public readonly double[] DestArray;
        public readonly bool IsMarkup;
        public readonly string TextLabel;
        public readonly bool HasPopup;
        public readonly IntPtr PopupAnnotation;
        public readonly double BorderWidth;

        public PdfAnnotation(PdfDocument.TPdfAnnotationType eType, int iPage, double[] r, double[] color, double dBorderWidth = 1.0)
        {
            this.SubType = eType;
            this.PageNr = iPage;
            if (r == null) Rect = null;
            else
            {
                var tmpRect = new double[r.Length];
                for (int i = 0; i < r.Length; i++)
                {
                    tmpRect[i] = r[i];
                }
                this.Rect = tmpRect;
            }
            if (color == null) Colors = null;
            else
            {
                var tmpColor = new double[color.Length];
                for (int i = 0; i < color.Length; i++)
                {
                    tmpColor[i] = color[i];
                }
                this.Colors = tmpColor;
            }
            this.BorderWidth = dBorderWidth;
        }

        public PdfAnnotation(PdfDocument.TPdfAnnotationType eType, int iPage, double[] r, string content, double[] color, double dBorderWidth = 1.0) :this(eType,iPage,r,color,dBorderWidth)
        {
            this.Contents = content;
        }

        /// <summary>
        /// constructor used to read an annotation from the API
        /// Uses marshalling to read the attributes
        /// </summary>
        /// <param name="annot"></param>
        public PdfAnnotation(PdfDocument.TPdfAnnotation annot)
        {
            this.AnnotId = annot.annotationHandle;
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
                quadPointArray[i] = (double)Marshal.PtrToStructure(pQuadPoints, typeof(double));
                pQuadPoints += Marshal.SizeOf(typeof(double));
            }

            this.QuadPoints = quadPointArray;
            this.Contents = Marshal.PtrToStringUni(annot.ptrContents);
            this.IsLink = annot.isLink == 1;
            this.ActionType = Marshal.PtrToStringAnsi(annot.ptrActionType);
            this.HasUri = annot.hasURI == 1;
            this.Uri = Marshal.PtrToStringUni(annot.ptrURI);
            this.DestType = annot.destType;
            this.HasDestVal = annot.hasDestVal.Select(num => num == 1).ToArray();
            this.DestArray = annot.destArray;
            this.IsMarkup = annot.isMarkup == 1;
            this.TextLabel = Marshal.PtrToStringUni(annot.ptrTextLabel);
            this.HasPopup = annot.hasPopup == 1;
            this.PopupAnnotation = annot.m_pPopupAnnot;
        }

        public PdfAnnotation(PdfAnnotation annot)
        {
            this.AnnotId = annot.AnnotId;
            this.PageNr = annot.PageNr;
            this.SubType = annot.SubType;
            var tmpCol = new double[annot.Colors.Length];
            for (int i = 0; i < annot.Colors.Length; i++)
            {
                tmpCol[i] = annot.Colors[i];
            }
            this.Colors = tmpCol;
            this.Flags = annot.Flags;
            if (annot.Rect == null) Rect = null;
            else
            {
                var tmpRect = new double[annot.Rect.Length];
                for (int i = 0; i < annot.Rect.Length; i++)
                {
                    tmpRect[i] = annot.Rect[i];
                }
                this.Rect = tmpRect;
            }
            if (annot.QuadPoints == null) QuadPoints = null;
            else
            {
                var tmpQuad = new double[annot.QuadPoints.Length];
                for (int i = 0; i < annot.QuadPoints.Length; i++)
                {
                    tmpQuad[i] = annot.QuadPoints[i];
                }
                this.QuadPoints = tmpQuad;
            }
            this.Contents = annot.Contents;
            this.IsLink = annot.IsLink;
            this.ActionType = annot.ActionType;
            this.HasUri = annot.HasUri;
            this.Uri = annot.Uri;
            this.DestType = annot.DestType;
            if (annot.HasDestVal == null) HasDestVal = null;
            else
            {
                var tmpHDest = new bool[annot.HasDestVal.Length];
                for (int i = 0; i < annot.HasDestVal.Length; i++)
                {
                    tmpHDest[i] = annot.HasDestVal[i];
                }
                this.HasDestVal = tmpHDest;
            }
            if (annot.DestArray == null) DestArray = null;
            else
            {
                var tmpDest = new double[annot.DestArray.Length];
                for (int i = 0; i < annot.DestArray.Length; i++)
                {
                    tmpDest[i] = annot.DestArray[i];
                }
                this.DestArray = tmpDest;
            }
            this.IsMarkup = annot.IsMarkup;
            this.TextLabel = annot.TextLabel;
            this.HasPopup = annot.HasPopup;
            this.PopupAnnotation = annot.PopupAnnotation;
            this.BorderWidth = annot.BorderWidth;
        }

        public long GetHandleAsLong()
        {
            return AnnotId.ToInt64();
        }

        /// <summary>
        /// Converts a string to the corresponding TPdfAnnotationType
        /// </summary>
        /// <param name="annotSubType"></param>
        /// <returns></returns>
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
                    return PdfDocument.TPdfAnnotationType.eAnnotationWidget;
                default:
                    return PdfDocument.TPdfAnnotationType.eAnnotationInk;
            }
        }


        /// <summary>
        /// Checks whether the bounding rectangle of this annotation is intersected by a rectangle.
        /// If OnIntersect is false the markedRect has to completly contain the bounding rectangle.
        /// </summary>
        /// <param name="markedRect"></param>
        /// <param name="onIntersect"></param>
        /// <returns></returns>
        public bool ContainsOrIntersectsWithRect(PdfSourceRect markedRect, bool onIntersect)
        {
            PdfSourceRect annotRect = new PdfSourceRect(Rect[0], Rect[1], Rect[2] - Rect[0], Rect[3] - Rect[1]);

            if (onIntersect)
            {
                return markedRect.intersectsDouble(annotRect);
            }
            return markedRect.contains(annotRect);
        }

        /// <summary>
        /// Checks whether a point is inside the bounding rectangle of this annotation
        /// </summary>
        /// <param name="point"></param>
        /// <returns></returns>
        public bool ContainsPoint(PdfSourcePoint point)
        {
            return Rect[0] <= point.dX && Rect[1] <= point.dY && Rect[2] >= point.dX && Rect[3] >= point.dY;
        }

        /// <summary>
        /// Returns an UpdateAnnotation object that can be used to move the annotation the given amount
        /// </summary>
        /// <param name="deltaX"></param>
        /// <param name="deltaY"></param>
        /// <returns></returns>
        public UpdateAnnotation Move(double deltaX, double deltaY)
        {
            double[] rect = new double[] { Rect[0] + deltaX, Rect[1] + deltaY, Rect[2] + deltaX, Rect[3] + deltaY };

            return new UpdateAnnotation(this, rect, null, null, null, -1);
        }

        /// <summary>
        /// Returns an UpdateAnnotation object that can be used to change the content of the annotation
        /// </summary>
        /// <param name="content"></param>
        /// <returns></returns>
        public UpdateAnnotation UpdateContent(string content)
        {
            return new UpdateAnnotation(this, null, content, null, null, -1);
        }

        /// <summary>
        /// Returns an UpdateAnnotation object that can be used to change the label of the annotation
        /// </summary>
        /// <param name="label"></param>
        /// <returns></returns>
        public UpdateAnnotation UpdateLabel(string label)
        {
            return new UpdateAnnotation(this, null, null, label, null, -1);
        }

        /// <summary>
        /// Returns an UpdateAnnotation object that can be used to change the color of the annotation
        /// </summary>
        /// <param name="color"></param>
        /// <returns></returns>
        public UpdateAnnotation UpdateColor(Color color)
        {
            double[] colorArray = new double[] { color.R / 255.0, color.G / 255.0, color.B / 255.0 };
            return new UpdateAnnotation(this, null, null, null, colorArray, -1);
        }

        /// <summary>
        /// Returns an UpdateAnnotation object that can be used to change the borderwidth of the annotation
        /// </summary>
        /// <param name="width"></param>
        /// <returns></returns>
        public UpdateAnnotation UpdateWidth(double width)
        {
            return new UpdateAnnotation(this, null, null, null, null, width);
        }
    }
}
