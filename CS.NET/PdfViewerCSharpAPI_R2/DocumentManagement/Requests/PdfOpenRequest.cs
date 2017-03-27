/****************************************************************************
 *
 * File:            PdfOpenRequest.cs
 *
 * Description:     The 3-Heights™  Pdf Viewer Control for WPF
 *
 * Copyright:       Copyright (C) 2015 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *                  
 ***************************************************************************/

namespace PdfTools.PdfViewerCSharpAPI.DocumentManagement.Requests
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;
    using PdfTools.PdfViewerCSharpAPI.Utilities;
    using PdfTools.PdfViewerCSharpAPI.Model;

    public struct OpenArguments
    {
        public OpenArguments(string filename, byte[] fileMem, string password)
        {
            this.filename = filename;
            this.fileMem = fileMem;
            this.password = password;
        }

        public string filename, password;
        public byte[] fileMem;
    }


    /// <summary>
    /// Request to open a new Document
    /// </summary>
    public class PdfOpenRequest : APdfRequest<OpenArguments, object>
    {
        /// <summary>
        /// Creates the OpenRequest
        /// </summary>
        /// <param name="filename">The full or relative path of the file to open</param>
        /// <param name="password">The password used to open the file (may be empty if not needed)</param>
        public PdfOpenRequest(OpenArguments args)
            :base(args, 99)
        { }

        protected override object ExecuteNative(IPdfDocument document, OpenArguments args)
        {
            document.Open(args.filename, args.fileMem, args.password);
            return null;
        }

        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, InOutTuple tuple, PdfViewerException ex)
        {
        }
        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, PdfViewerException ex)
        {
        }
    }
}
