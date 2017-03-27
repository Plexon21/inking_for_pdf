/****************************************************************************
 *
 * File:            PdfViewerException.cs
 *
 * Description:     The 3-Heights™  Pdf Viewer Control for WPF
 *
 * Copyright:       Copyright (C) 2015 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *                  
 ***************************************************************************/

namespace PdfTools.PdfViewerCSharpAPI.Utilities
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;

    /// <summary>
    /// Exception which passes information about issues opening and reading a pdf.
    /// </summary>
    public class PdfViewerException : Exception
    {
        public PdfViewerException() : base()
        {

        }

        public PdfViewerException(string message) : base(message)
        { }
    }

    public class PdfNoFileOpenedException : PdfViewerException
    {
        public PdfNoFileOpenedException() : base("No file is open to process this operation") { }
    }

    public class PdfFileNotFoundException : PdfViewerException
    {
        public PdfFileNotFoundException()
            : base("File was not found")
        { }
        public PdfFileNotFoundException(string message)
            : base(message)
        { }
    }

    public class PdfFileCorruptException : PdfViewerException
    {
        public PdfFileCorruptException(string message) : base(message) { }
    }
    public class PdfPageNotCachedException : PdfViewerException
    {
        public PdfPageNotCachedException(IList<int> missingPages)
        {
            this.missingPages = missingPages;
        }

        public IList<int> missingPages;
    }
    public class PdfLicenseInvalidException : PdfViewerException
    {
        public PdfLicenseInvalidException(string message = "") : base(message) { }
    }
    public class PdfPasswordException : PdfViewerException
    {
        public PdfPasswordException(string message = "") : base(message) { }
    }
    public class PdfUnsupportedFeatureException : PdfViewerException
    {
        public PdfUnsupportedFeatureException(string message = "") : base(message) { }
    }

    public class PdfRequestCanceledException : PdfViewerException
    {
        public PdfRequestCanceledException(string message = "") : base(message) { }
    }
}
