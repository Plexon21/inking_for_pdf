using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using System.Windows.Ink;
using PdfTools.PdfViewerCSharpAPI.Annotations;
using PdfTools.PdfViewerCSharpAPI.Extensibility;

namespace DummyTextConverter

{
    [Export(typeof(IPdfTextConverter)),
     ExportMetadata("Name", "DummyTextConverter"),
     ExportMetadata("Version", 1)]
    public class DummyTextConverter : IPdfTextConverter
    {
        public string ToText(IEnumerable<PdfAnnotation> strokes)
        {
            var count = 0;
            if (strokes != null) count = strokes.Count();
            return "There are " + count + " strokes in the Stroke-List";

        }

        public string ToText(StrokeCollection strokes)
        {
            var count = 0;
            if (strokes != null) count = strokes.Count;
            return "There are " + count + " strokes in the Stroke-List";
        }
    }
}
