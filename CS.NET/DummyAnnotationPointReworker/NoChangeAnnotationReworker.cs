using System.ComponentModel.Composition;
using System.Linq;
using PdfTools.PdfViewerCSharpAPI.Extensibility;

namespace NoChangeAnnotationPointReworker
{
    [Export(typeof(IPdfAnnotationReworker)),
     ExportMetadata("Name", "NoChangeReworker"),
     ExportMetadata("Version", 1)]
    public class NoChangeAnnotationReworker : IPdfAnnotationReworker
    {
        public double[] ReworkPoints(double[] annotationPoints)
        {
            return annotationPoints;
        }
    }
}
