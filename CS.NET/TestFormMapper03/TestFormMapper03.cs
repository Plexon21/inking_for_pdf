using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using PdfTools.PdfViewerCSharpAPI.Extensibility;

namespace TestFormMapper03
{
    [Export(typeof(IPdfAnnotationFormMapper)),
     ExportMetadata("Name", "RectangleFormMapper"),
     ExportMetadata("Version", 1)]
    public class TestFormMapper03 : IPdfAnnotationFormMapper
    {
        public string AnnotationType { get; } = "eAnnotationRectangle";
        public IList<double[]> MapToForm(double[] annotationPoints)
        {
            return null;
        }
        public IList<Point> MapToForm(IList<Point> annotationPoints)
        {
            return null;
        }
    }
}
