package com.pdf_tools.pdfviewer.Model;

public class PdfOutlineItem
{
    public PdfOutlineItem()
    {
    }

    public PdfOutlineItem(int id, int level, boolean descendants, String title, PdfDestination dest)
    {
        this.id = id;
        this.level = level;
        this.descendants = descendants;
        this.title = title;
        this.dest = dest;
    }

    public int id; // Unique outline identifier.
    public int level; // The outline level relative to the other outlines of the
                      // array.
    public boolean descendants; // Whether this outline has descendants.
    public String title; // The title of the outline.
    public PdfDestination dest; // Destination of the outline item
}
