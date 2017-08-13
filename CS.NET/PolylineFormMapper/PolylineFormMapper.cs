using System;
using System.Collections;
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
     ExportMetadata("Name", "PolylineFormMapper"),
     ExportMetadata("Version", 1)]
    public class PolylineFormMapper : IPdfAnnotationFormMapper
    {
        public string AnnotationType { get; } = "Ink";
        public IList<double[]> MapToForm(double[] annotationPoints)
        {
            var result = new List<double[]>();
            if (!(annotationPoints?.Length > 4)) return null;
            for (int i = 0; i < annotationPoints.Length-3; i+=2)
            {
                result.Add(new double[]{annotationPoints[i],annotationPoints[i+1],annotationPoints[i+2],annotationPoints[i+3]});
            }
            return result;
        }

        public IList<Point> MapToForm(IList<Point> annotationPoints)
        {
            return annotationPoints;
        }
    }
}
