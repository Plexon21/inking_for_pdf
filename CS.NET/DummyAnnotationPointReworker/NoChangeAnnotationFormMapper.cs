﻿using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using PdfTools.PdfViewerCSharpAPI.Extensibility;

namespace NoChangeAnnotationFormMapper
{
    [Export(typeof(IPdfAnnotationFormMapper)),
     ExportMetadata("Name", "NoChangeFormMapper"),
     ExportMetadata("Version", 1)]
    public class NoChangeAnnotationFormMapper : IPdfAnnotationFormMapper
    {
        public IList<double[]> MapToForm(double[] annotationPoints)
        {
            return new List<double[]>{annotationPoints};
        }
    }
}
