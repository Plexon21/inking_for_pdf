using System.ComponentModel.Composition;
using System.Linq;
using PdfTools.PdfViewerCSharpAPI.Extensibility;

namespace NoChangeAnnotationPointReworker
{
   
    public class NoChangeAnnotationReworker : IPdfAnnotationReworker
    {
        [Export(typeof(IPdfAnnotationReworker)),
         ExportMetadata("Name", "NoChangeReworker"),
         ExportMetadata("Version", 1)]
        public double[] ReworkPoints(double[] annotationPoints)
        {
            return annotationPoints;
        }
    }
}
