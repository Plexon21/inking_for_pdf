﻿using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using System.Windows;
using PdfTools.PdfViewerCSharpAPI.Extensibility;

[assembly: CLSCompliant(true)]
namespace AnnotationFormMapper
{
    [Export(typeof(IPdfAnnotationFormMapper)),
     ExportMetadata("Name", "NoChangeFormMapper"),
     ExportMetadata("Version", 1)]
    public class NoChangeAnnotationFormMapper : IPdfAnnotationFormMapper
    {
        public string AnnotationType { get; } = "eAnnotationInk";

        public IList<double[]> MapToForm(double[] annotationPoints)
        {
            return new List<double[]>{annotationPoints};
        }

        public IList<IList<Point>> MapToForm(IList<Point> annotationPoints)
        {
            return new List<IList<Point>>{annotationPoints};
        }
    }
}
