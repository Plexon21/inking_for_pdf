using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.ComponentModel.Composition;
using System.ComponentModel.Composition.Hosting;
using System.IO;

namespace Playground
{
    public class AnnotationHandler
    {
        private CompositionContainer container;

        public AnnotationHandler()
        {
            //An aggregate catalog that combines multiple catalogs
            var catalog = new AggregateCatalog();

            string path = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Extensions");

            //Check the directory exists
            if (!Directory.Exists(path))
            {
                Directory.CreateDirectory(path);
            }

            catalog.Catalogs.Add(new DirectoryCatalog(path, "*.dll"));

            //Create the CompositionContainer with the parts in the catalog  
            container = new CompositionContainer(catalog);

            //Fill the imports of this object  
            try
            {
                this.container.ComposeParts(this);
            }
            catch (CompositionException compositionException)
            {
                Console.WriteLine(compositionException.ToString());
            }
        }
        [ImportMany]
        public IEnumerable<Lazy<Annotation, IAnnotationMetadata>> annotations;
    }
}
