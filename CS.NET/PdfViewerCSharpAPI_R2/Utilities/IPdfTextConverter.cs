using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows.Ink;
using PdfTools.PdfViewerCSharpAPI.Annotations;

namespace PdfTools.PdfViewerCSharpAPI.Utilities
{
    public interface IPdfTextConverter
    {
        string ToText(IEnumerable<PdfAnnotation> annots);
        string ToText(StrokeCollection strokes);
    }
}
