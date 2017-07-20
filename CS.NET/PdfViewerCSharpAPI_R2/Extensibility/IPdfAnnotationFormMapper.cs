using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace PdfTools.PdfViewerCSharpAPI.Extensibility
{
    public interface IPdfAnnotationFormMapper
    {
        IList<double[]> MapToForm(double[] annotationPoints);
    }
}
