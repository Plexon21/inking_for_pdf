using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows.Ink;
using PdfTools.PdfViewerCSharpAPI.Annotations;

namespace PdfTools.PdfViewerCSharpAPI.Extensibility
{
    /// <summary>
    /// Implemented by text converter extensions
    /// </summary>
    public interface IPdfTextConverter
    {
        /// <summary>
        /// converts annotations to text
        /// </summary>
        /// <param name="annots"></param>
        /// <returns></returns>
        string ToText(IEnumerable<PdfAnnotation> annots);

        /// <summary>
        /// converts strokes from the windows ink library to text
        /// </summary>
        /// <param name="annots"></param>
        /// <returns></returns>
        string ToText(StrokeCollection strokes);
    }
}
