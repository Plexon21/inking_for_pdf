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
    /// Converts the FitMode to a pretty string.
    /// </summary>
    public class FitModeConverter : IValueConverter
    {

        public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
        {
            FitMode mode = (FitMode)value;
            switch (mode)
            {
                case FitMode.FitTrueSize:
                    return ViewerWPFSample.Properties.MainWindowRes.fit_actual;
                case FitMode.FitPage:
                    return ViewerWPFSample.Properties.MainWindowRes.fit_page;
                case FitMode.FitWidth:
                    return ViewerWPFSample.Properties.MainWindowRes.fit_width;
                default:
                    return ViewerWPFSample.Properties.MainWindowRes.fit_none;
            }
        }

        public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
