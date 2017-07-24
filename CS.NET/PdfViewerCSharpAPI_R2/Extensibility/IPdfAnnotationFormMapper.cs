using System.Collections.Generic;
using System.Windows;

namespace PdfTools.PdfViewerCSharpAPI.Extensibility
{
    public interface IPdfAnnotationFormMapper
    {
        IList<double[]> MapToForm(double[] annotationPoints);
        IList<Point> MapToForm(IList<Point> annotationPoints);
    }
}
