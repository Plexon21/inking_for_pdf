using System.Collections.Generic;
using System.Windows;

namespace PdfTools.PdfViewerCSharpAPI.Extensibility
{
    public interface IPdfAnnotationFormMapper
    {
        string AnnotationType { get; }
        IList<double[]> MapToForm(double[] annotationPoints);
        IList<IList<Point>> MapToForm(IList<Point> annotationPoints);
    }
}
