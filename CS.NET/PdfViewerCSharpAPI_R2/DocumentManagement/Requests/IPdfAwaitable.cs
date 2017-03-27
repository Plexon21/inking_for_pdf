// -----------------------------------------------------------------------
// <copyright file="IPdfAwaitable.cs" company="">
// TODO: Update copyright text.
// </copyright>
// -----------------------------------------------------------------------

namespace PdfTools.PdfViewerCSharpAPI.DocumentManagement.Requests
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;
    using PdfTools.PdfViewerCSharpAPI.Utilities;

    /// <summary>
    /// /// An interface for things that can be waited on (e.g. the execution of a request) and that will 
    /// </summary>
    /// <typeparam name="T">The type of the parameter that is returned when waiting succeded</typeparam>
    public interface IPdfAwaitable<T>
    {
        event Action<T, PdfViewerException> Completed;
        T Wait();
    }
}
