using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows.Data;
using PdfTools.PdfViewerWPF;
using PdfTools.PdfViewerCSharpAPI.Model;

namespace ViewerWPFSample.Converters
{
    /// <summary>
    /// Converts Resolution structs into pretty strings.
    /// </summary>
    public class ResolutionConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
        {
            Resolution resolution = (Resolution)value;
            return new StringBuilder(resolution.xdpi.ToString()).Append("x").Append(resolution.ydpi.ToString()).ToString();
        }

        public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
