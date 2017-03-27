// -----------------------------------------------------------------------
// <copyright file="OutlineItem.cs" company="">
// TODO: Update copyright text.
// </copyright>
// -----------------------------------------------------------------------

namespace PdfTools.PdfViewerCSharpAPI.Utilities
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;

    /// <summary>
    /// TODO: Update summary.
    /// </summary>
    public class PdfOutlineItem
    {
            public PdfOutlineItem() { }
            public PdfOutlineItem(int id, int level, bool descendants, string title, PdfDestination dest) { this.id = id; this.level = level; this.descendants = descendants; this.title = title; this.dest = dest; }
            public int id;              // Unique outline identifier.
            public int level;           // The outline level relative to the other outlines of the array.
            public bool descendants;    // Whether this outline has descendants.
            public string title;        // The title of the outline.
            public PdfDestination dest;    // Destination of the outline item
    }
}
