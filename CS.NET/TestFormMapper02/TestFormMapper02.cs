using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using PdfTools.PdfViewerCSharpAPI.Extensibility;

namespace TestFormMapper02
{
    [Export(typeof(IPdfAnnotationFormMapper)),
     ExportMetadata("Name", "RectangleFormMapper"),
     ExportMetadata("Version", 2)]
    public class TestFormMapper02: IPdfAnnotationFormMapper
    {
        public string AnnotationType { get; } = "eAnnotationRectangle";
        public IList<double[]> MapToForm(double[] annotationPoints)
        {
            if (annotationPoints == null || annotationPoints.Length < 2 || annotationPoints.Length % 2 != 0) return null;
            var startPointX = annotationPoints[0];
            var startPointY = annotationPoints[1];
            var endPointX = annotationPoints[annotationPoints.Length - 2];
            var endPointY = annotationPoints[annotationPoints.Length - 1];

            return new List<double[]>
            {
                new double[]{ startPointX,startPointY, startPointX, endPointY, endPointX, endPointY, endPointX, startPointY }
            };
        }
        public IList<IList<Point>> MapToForm(IList<Point> annotationPoints)
        {
            if (annotationPoints == null || annotationPoints.Count < 2) return null;
            var firstPoint = annotationPoints[0];
            var lastPoint = annotationPoints[annotationPoints.Count - 1];
            return new List<IList<Point>>
            {
                new List<Point>
                {
                    firstPoint,
                    new Point(firstPoint.X, lastPoint.Y),
                    lastPoint,
                    new Point(lastPoint.X, firstPoint.Y),
                }
            };
        }
    }
}
