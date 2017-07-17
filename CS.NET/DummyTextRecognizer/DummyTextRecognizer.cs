using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using PdfTools.PdfViewerCSharpAPI.Annotations;
using PdfTools.PdfViewerCSharpAPI.Extensibility;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Input.StylusPlugIns;

namespace DummyTextRecognizer
{
    [Export(typeof(IPdfTextConverter)),
     ExportMetadata("Name", "DummyTextRecognizer"),
     ExportMetadata("Version", 1)]
    public class DummyTextConverter : IPdfTextConverter
    {
        public string ToText(IEnumerable<PdfAnnotation> strokes)
        {
            return "There are " + strokes.Count() + " strokes in the Stroke-List";

        }

        public string ToText(StrokeCollection strokes)
        {
            return "There are " + strokes.Count + " strokes in the Stroke-List";
        }
    }
}
