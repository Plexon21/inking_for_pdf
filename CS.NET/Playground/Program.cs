using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.ComponentModel.Composition.Hosting;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Playground
{
    public class Program
    {   
        static void Main(string[] args)
        {
            AnnotationHandler handler = new AnnotationHandler();
            foreach (var annot in handler.annotations) {
                Console.WriteLine(annot.Metadata.Name + " Version:" + annot.Metadata.Version);
                Console.WriteLine(annot.Value.show());
            }
            Console.ReadKey();
        }
    }
}
