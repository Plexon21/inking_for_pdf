
namespace PdfTools.PdfViewerCSharpAPI.DocumentManagement.Requests
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;
    using PdfTools.PdfViewerCSharpAPI.Utilities;
    using PdfTools.PdfViewerCSharpAPI.Model;

    public struct SaveAsArguments
    {
        public SaveAsArguments(string fileName)
        {
            this.fileName = fileName;
        }
        
        public string fileName;
    }


    /// <summary>
    /// Request to save a document
    /// </summary>
    public class PdfSaveAsRequest : APdfRequest<SaveAsArguments, bool>
    {
        /// <summary>
        /// Creates the SaveAsRequest
        /// </summary>
        public PdfSaveAsRequest(SaveAsArguments args)
            :base(args, 95)
        { }

        protected override bool ExecuteNative(IPdfDocument document, SaveAsArguments args)
        {
           return document.SaveAs(args.fileName);
        }

        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, InOutTuple tuple, PdfViewerException ex)
        {
        }
        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, PdfViewerException ex)
        {
        }
    }
}
