using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Playground
{
    public interface IAnnotationMetadata
    {
        string Name { get; }

        [DefaultValue(1)]
        int Version { get; }
    }
}
