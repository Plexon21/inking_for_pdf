using System;
using System.Collections.Generic;
using System.Windows.Ink;
using System.Windows.Input;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using PdfTools.PdfViewerCSharpAPI.Annotations;

namespace AnnotationTextConverter
{
    [TestClass]
    public class TestWindowsInkPdfTextConverter
    {
        [TestMethod]
        public void TestToTextStrokes()
        {
            var input = new StrokeCollection(new List<Stroke>
            {
                new Stroke(new StylusPointCollection()
                {
                    new StylusPoint(1.1, 1.1),
                    new StylusPoint(1.2, 1.2)
                }),
                new Stroke(new StylusPointCollection()
                {
                    new StylusPoint(2.1, 2.1),
                    new StylusPoint(2.2, 2.2)
                })
            });
            var expected = typeof(string);
            var converter = new WindowsInkPdfTextConverter();
            var output = converter.ToText(input);
            Assert.AreEqual(expected, output.GetType());
        }

        [TestMethod]
        public void TestToTextStrokesFail()
        {
            StrokeCollection input = null;
            var converter = new WindowsInkPdfTextConverter();
            var output = converter.ToText(input);
            Assert.IsNull(output);

            input = new StrokeCollection();
            converter = new WindowsInkPdfTextConverter();
            output = converter.ToText(input);
            Assert.IsNull(output);
        }
    }
}
