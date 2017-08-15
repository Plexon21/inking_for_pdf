using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using PdfTools.PdfViewerCSharpAPI.Extensibility;

[assembly: CLSCompliant(true)]
namespace AnnotationFormMapper
{
    [Export(typeof(IPdfAnnotationFormMapper)),
     ExportMetadata("Name", "EqualsFormMapper"),
     ExportMetadata("Version",1)]
    public class EqualsFormMapper:IPdfAnnotationFormMapper
    {
        public string AnnotationType { get; } = "Equals";
        public IList<double[]> MapToForm(double[] annotationPoints)
        {
            if (annotationPoints == null || annotationPoints.Length < 2 || annotationPoints.Length % 2 != 0) return null;
            var startPointX = annotationPoints[0];
            var startPointY = annotationPoints[1];
            var endPointX = annotationPoints[annotationPoints.Length - 2];
            var endPointY = annotationPoints[annotationPoints.Length - 1];

            return new List<double[]>
            {
                new double[] {startPointX, startPointY, endPointX, startPointY},
                new double[] { startPointX, endPointY, endPointX, endPointY}
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
                    new Point(lastPoint.X,firstPoint.Y)
                }, 
                new List<Point>
                {
                    new Point(firstPoint.X,lastPoint.Y),
                    lastPoint
                }
            };
        }

    }
}
