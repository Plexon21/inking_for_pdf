using Playground;
using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using System.Text;
using System.Threading.Tasks;


namespace PlaygroundExtension
{
    [Export(typeof(Annotation)),
    ExportMetadata("Name", "Text"),
    ExportMetadata("Version", 1)]
    public class TextAnnotation : Annotation
    {
        public override string show()
        {
            return "Hallo ich bin eine Textannotation.";
        }
    }
}
