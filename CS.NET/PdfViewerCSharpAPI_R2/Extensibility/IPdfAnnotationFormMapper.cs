using System.Collections.Generic;
using System.Windows;

namespace PdfTools.PdfViewerCSharpAPI.Extensibility
{
    /// <summary>
    /// Implemented by form mapper extensions
    /// </summary>
    public interface IPdfAnnotationFormMapper
    {
        /// <summary>
        /// used to create annotations of specific type
        /// </summary>
        string AnnotationType { get; }

        /// <summary>
        /// map points to the specific points to display the form
        /// </summary>
        /// <param name="annotationPoints"></param>
        /// <returns></returns>
        IList<double[]> MapToForm(double[] annotationPoints);

        /// <summary>
        /// map points to the specific points to display the form while drawing
        /// </summary>
        /// <param name="annotationPoints"></param>
        /// <returns></returns>
        IList<IList<Point>> MapToForm(IList<Point> annotationPoints);
    }
}
