using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using PdfTools.PdfViewerCSharpAPI.Extensibility;

namespace RectangleFormMapper
{
    [Export(typeof(IPdfAnnotationFormMapper)),
     ExportMetadata("Name", "RectangleFormMapper"),
     ExportMetadata("Version", 1)]
    public class RectangleFormMapper : IPdfAnnotationFormMapper
    {
        public IList<double[]> MapToForm(double[] annotationPoints)
        {
            if (annotationPoints == null) return null;
            var startPointX = annotationPoints[0];
            var startPointY = annotationPoints[1];
            var endPointX = annotationPoints[annotationPoints.Length - 2];
            var endPointY = annotationPoints[annotationPoints.Length - 1];

            return new List<double[]>
            {
                new double[] {startPointX, startPointY, startPointX, endPointY},
                new double[] {startPointX, startPointY, endPointX, startPointY},
                new double[] {startPointX, endPointY, endPointX, endPointY},
                new double[] {endPointX, startPointY, endPointX, endPointY}
            };
        }
    }
}
