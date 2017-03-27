/****************************************************************************
 *
 * File:            IPdfRequest
.cs
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
    using PdfTools.PdfViewerCSharpAPI.Model;


    /// <summary>
    /// Interface for implementing Requests, that get performed on the Document in a background thread
    /// </summary>
    /// <typeparam name="T">The type of the parameter that is returned when waiting succeded</typeparam>
    public interface IPdfRequest
    {
        /// <summary>
        /// The priority of the associated request in the queue. Higher number means higher priority
        /// </summary>
        int Priority
        {
            get;
        }


        /// <summary>
        /// Execution of the request. It is important, that the execution method triggers the completedEvent before terminating!
        /// </summary>
        /// <param name="document">The document on which the request is performed</param>
        /// <param name="manager">The DocumentManager, which has to be informed about completion of the request</param>
        void Execute(IPdfDocument document, IPdfControllerCallbackManager controller);

        void Cancel();
    }
}
