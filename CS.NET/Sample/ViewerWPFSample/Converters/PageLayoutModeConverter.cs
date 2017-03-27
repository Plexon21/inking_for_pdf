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
    /// Converts the PageLayoutMode to a pretty string.
    /// </summary>
    public class PageLayoutModeConverter : IValueConverter
    {

        public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
        {
            TPageLayoutMode mode = (TPageLayoutMode)value;
            switch (mode)
            {
                case TPageLayoutMode.TwoColumnLeft:
                case TPageLayoutMode.TwoPageLeft:
                    return ViewerWPFSample.Properties.MainWindowRes.doublee;
                case TPageLayoutMode.TwoColumnRight:
                case TPageLayoutMode.TwoPageRight:
                    return ViewerWPFSample.Properties.MainWindowRes.double_title;
                default:
                    return ViewerWPFSample.Properties.MainWindowRes.single;
            }
        }

        public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
