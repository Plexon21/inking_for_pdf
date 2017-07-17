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

namespace WindowsInkTextConverter
{
    [Export(typeof(IPdfTextConverter)),
     ExportMetadata("Name", "WindowsInk"),
     ExportMetadata("Version", 1)]
    public class WindowsInkPdfTextConverter : IPdfTextConverter
    {
        public string ToText(IEnumerable<PdfAnnotation> strokes)
        {
            var actualStrokes = new StrokeCollection();
            foreach (var annot in strokes)
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
            using (MemoryStream ms = new MemoryStream())
            {
                strokes.Save(ms);
                var myInkCollector = new InkCollector();
                var ink = new Ink();
                ink.Load(ms.ToArray());

                using (RecognizerContext context = new RecognizerContext())
                {
                    if (ink.Strokes.Count > 0)
                    {
                        context.Strokes = ink.Strokes;

                        var result = context.Recognize(out RecognitionStatus status);

                        if (status == RecognitionStatus.NoError)
                            return result.TopString;
                    }
                    return null;
                }
            }
        }
    }
}
