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
    ExportMetadata("Name", "Ink"),
    ExportMetadata("Version", 2)]
    public class InkAnnotation : Annotation
    {
        public override string show()
        {
            return "Gugugseli, Ink here";
        }
    }
}
