using System.ComponentModel.Composition;
using System.Linq;
using PdfTools.PdfViewerCSharpAPI.Utilities;

namespace DummyAnnotationPointReworker
{
    [Export(typeof(IPdfAnnotationReworker)),
     ExportMetadata("Name", "DummyReworker"),
     ExportMetadata("Version", 1)]
    public class DummyAnnotationReworker : IPdfAnnotationReworker
    {
        public double[] ReworkPoints(double[] annotationPoints)
        {
            return annotationPoints.Select(p => p += 1.0).ToArray();
        }
    }
}
