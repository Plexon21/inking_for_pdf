using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;

namespace PdfTools.PdfViewerCSharpAPI.Utilities
{
    public interface IPdfTextConverterMetadata
    {
        string Name { get; }

        [DefaultValue(1)]
        int Version { get; }
    }
}
