// -----------------------------------------------------------------------
// <copyright file="PdfAction.cs" company="">
// TODO: Update copyright text.
// </copyright>
// -----------------------------------------------------------------------

namespace PdfTools.PdfViewerCSharpAPI.Utilities
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;
    using PdfTools.PdfViewerCSharpAPI.Model;

    /// <summary>
    /// Destination as specified in PDF reference 8.2.1 "Destinations" (quote:)
    /// A destination defines a particular view of a document, consisting of the following items:
    /// * The page of the document to be displayed
    /// * The location of the document window on that page
    /// * The magnification (zoom) factor to use when displaying the page
    /// 
    /// These destinations are defines as "explicit destinations"
    /// </summary>
    public class PdfDestination
    {
        /// <summary>
        /// Create new destination object
        /// </summary>
        /// <param name="page">page to focus on</param>
        /// <param name="type">Type of destination (as specified in PDF rerefence, table 8.2) Depending on the type, some other parameters of the constructor will be ignored</param>
        /// <param name="left">Defines "left" parameter, if the given type needs is</param>
        /// <param name="top">Defines "top" parameter, if the given type needs is</param>
        /// <param name="right">Defines "right" parameter, if the given type needs is</param>
        /// <param name="bottom">Defines "bottom" parameter, if the given type needs is</param>
        /// <param name="zoom">Defines "zoom" parameter, if the given type needs is</param>
        public PdfDestination(int page, TDestination type, double left, double top, double right, double bottom, double zoom)
        {
            this.page = page;
            this.type = type;
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.zoom = zoom;
        }

        private int page;
        private TDestination type;
        private double left;
        private double top;
        private double right;
        private double bottom;
        private double zoom;


        public PdfSourceRect RectOnCanvas(PdfSourceRect pageRect, PdfViewerController.Viewport viewport)
        {
            PdfSourceRect rect = viewport.Rectangle.GetSourceRect(viewport.ZoomFactor);
            double left = Double.IsNaN(this.left) ? rect.dX : (this.left + pageRect.dX);
            double top = (Double.IsNaN(this.top) || this.top == -32768) ? pageRect.dY : (pageRect.dBottom - this.top);
            double width = Double.IsNaN(this.right) ? rect.dRight - left : this.right - left;
            double height = Double.IsNaN(this.bottom) ? (rect.dBottom - top) : (this.bottom - top);
            return new PdfSourceRect(left, top, Math.Max(width, 0.0), Math.Max(height, 0.0));
        }

        public double Zoom(double viewportZoomFactor)
        {
            return (Double.IsNaN(zoom) || zoom == 0.0) ? viewportZoomFactor : zoom;
        }

        public new string ToString()
        {
            return String.Format("{0}, {1}, {2}, {3}, {4}, {5}, {6}", page, type, left, top, right, bottom, zoom);
        }

        public int Page
        {
            get
            {
                return page;
            }
        }

        public TDestination Type
        {
            get
            {
                return type;
            }
        }
/// <summary>
        /// Rectangle which reflects the top, right, left and bottom coordinates from the destination correctly. Be aware, that the rectangle itsself is not valid, but merely some of its coordinates, depending on the type of the destination
        /// </summary>
        public PdfSourceRect RectOnPage
        {
            get
            {
                return new PdfSourceRect(left, top, right - left, bottom - top);
            }
        }

        private double Clip(double toClip, double max)
        {
            return Math.Max(0.0, Math.Min(max, toClip));
        }
        private double sanityCheckNumber(double number, double pageRectHeight)
        {
            if (number == -32768)
            {
                return pageRectHeight;//special case for some reason (behaviour like adobe reader)
            }
            else if (Math.Abs(number) >= 32768)
            {
                throw new ArgumentException("Destination " + number + " is out of range");
            }
            return number;
        }

    }
}
