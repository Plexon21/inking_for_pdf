using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using PdfTools.PdfViewerCSharpAPI.Extensibility;

namespace TestFormMapper04
{
    [Export(typeof(IPdfAnnotationFormMapper)),
     ExportMetadata("Name", "RectangleFormMapper"),
     ExportMetadata("Version", 1)]
    public class TestFormMapper04 : IPdfAnnotationFormMapper
    {
        public string AnnotationType { get; } = "eAnnotationRectangle";
        public IList<double[]> MapToForm(double[] annotationPoints)
        {
            return new List<double[]>();
        }
        public IList<IList<Point>> MapToForm(IList<Point> annotationPoints)
        {
            return new List<IList<Point>>();
        }
    }
}
