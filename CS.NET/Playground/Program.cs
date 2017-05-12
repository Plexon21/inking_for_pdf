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

            var key = "c";
            while ("c".Equals(key))
            {
                Console.WriteLine("Insert name of Annotationtype");
                var input = Console.ReadLine();
                var res = handler.Show(input);
                var meta = handler.GetMetadata(input);
                if (res == null)
                {
                    res = "Annotation type " + input + " not found.";
                }
                Console.WriteLine(res);
                Console.WriteLine("Press c to continue");
                key = Console.ReadLine();
            }
        }
    }
}
