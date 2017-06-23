using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace PdfTools.PdfViewerCSharpAPI.Utilities
{
    public interface IPdfAnnotationReworker
    {
        double[] ReworkPoints(double[] annotationPoints);
    }
}
