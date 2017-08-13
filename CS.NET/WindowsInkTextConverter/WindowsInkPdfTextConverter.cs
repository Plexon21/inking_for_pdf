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
using Microsoft.Ink;
using Stroke = System.Windows.Ink.Stroke;

namespace AnnotationTextConverter
{
    [Export(typeof(IPdfTextConverter)),
     ExportMetadata("Name", "WindowsInkTextConverter"),
     ExportMetadata("Version", 1)]
    public class WindowsInkPdfTextConverter : IPdfTextConverter
    {
        public string ToText(IEnumerable<PdfAnnotation> annots)
        {
            if (annots == null) return null;
            var actualStrokes = new StrokeCollection();
            foreach (var annot in annots)
            {
                var stylusPoints = new StylusPointCollection();
                for (int i = 0; i < annot.Rect.Length - 1; i += 2)
                {
                    stylusPoints.Add(new StylusPoint(annot.Rect[i], annot.Rect[i + 1])); ;
                }
                actualStrokes.Add(new Stroke(stylusPoints));
            }
            return ToText(actualStrokes);

        }

        public string ToText(StrokeCollection strokes)
        {
            using (var ms = new MemoryStream())
            {
                if (strokes == null) return null;
                strokes.Save(ms);
                var ink = new Ink();
                ink.Load(ms.ToArray());
                if (ink.Strokes.Count <= 0) return null;

                using (var context = new RecognizerContext())
                {
                    context.Strokes = ink.Strokes;

                    var result = context.Recognize(out RecognitionStatus status);
                    return status == RecognitionStatus.NoError ? result.TopString : null;
                }
            }
        }
    }
}
