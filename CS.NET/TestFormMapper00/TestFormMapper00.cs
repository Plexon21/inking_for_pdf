using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using PdfTools.PdfViewerCSharpAPI.Extensibility;

namespace TestFormMapper00
{
    [Export(typeof(IPdfAnnotationFormMapper)),
     ExportMetadata("Name", "Test00FormMapper"),
     ExportMetadata("Version", 1)]
    public class TestFormMapper00 : IPdfAnnotationFormMapper
    {
        public string AnnotationType { get; } = "eAnnotationRectangle";
        public IList<double[]> MapToForm(double[] annotationPoints)
        {
            return new List<double[]>{annotationPoints};
        }
       

        public IList<IList<Point>> MapToForm(IList<Point> annotationPoints)
        {
            return new List<IList<Point>>{annotationPoints};
        }
    }
}
