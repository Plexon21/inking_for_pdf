using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Ink;
using PdfTools.PdfViewerCSharpAPI.Annotations;
using PdfTools.PdfViewerCSharpAPI.Extensibility;

namespace TestFormMapper08
{
    [Export(typeof(IPdfTextConverter)),
     ExportMetadata("Name", "RectangleFormMapper"),
     ExportMetadata("Version", 1)]

    public class TestFormMapper08:IPdfTextConverter
    {
        public string ToText(IEnumerable<PdfAnnotation> annots)
        {
            return "";
        }

        public string ToText(StrokeCollection strokes)
        {
            return "";
        }
    }
}
