using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using PdfTools.PdfViewerCSharpAPI.Extensibility;
using PdfTools.PdfViewerCSharpAPI.Model;

namespace TestPdfViewerCSharpAPI
{
    [TestClass]
    public class TestPdfViewerController
    {
        [TestMethod]
        public void TestLoadTextConverter()
        {
            var cont = new PdfViewerController(a => { });
            var res = cont.LoadTextConverter();
            
        }
    }
}
