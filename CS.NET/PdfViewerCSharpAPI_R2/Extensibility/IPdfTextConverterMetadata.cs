using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;

namespace PdfTools.PdfViewerCSharpAPI.Extensibility
{
    /// <summary>
    /// Used to distinguish text converter extensions before loading them
    /// </summary>
    public interface IPdfTextConverterMetadata
    {
        string Name { get; }

        [DefaultValue(1)]
        int Version { get; }
    }
}
