// -----------------------------------------------------------------------
// <copyright file="IGenericBitmap.cs" company="">
// TODO: Update copyright text.
// </copyright>
// -----------------------------------------------------------------------

namespace Pdftools.PdfViewerCSharpAPI_R2.BitmapClasses
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;

    /// <summary>
    /// TODO: Update summary.
    /// </summary>
    public interface IGenericBitmap
    {
        int PixelWidth { get; }
        int PixelHeight { get; }
        IntPtr BackBuffer { get; }
        void Freeze();
        void LockRect(int x, int y, int width, int height);
        void UnLock();
        Object Unwrap();

    }

    public enum TBitmapType{ WPF, WinForms};
    public static class BitmapFactory
    {
        public static IGenericBitmap Get(TBitmapType type, int bitmapWidth, int bitmapHeight, double xdpi, double ydpi)
        {
            switch(type)
            {
                case TBitmapType.WPF:
                    return new WPFBitmapWrapper(bitmapWidth, bitmapHeight, xdpi, ydpi);
                case TBitmapType.WinForms:
                    return new WinFormsBitmapWrapper(bitmapWidth, bitmapHeight, xdpi, ydpi);
                default:
                    return null;
            }
        }
        public static IGenericBitmap Get(Type type, int bitmapWidth, int bitmapHeight, double xdpi, double ydpi)
        {
            if(type == typeof(WinFormsBitmapWrapper))
                return new WinFormsBitmapWrapper(bitmapWidth, bitmapHeight, xdpi, ydpi);
            else if (type == typeof(WPFBitmapWrapper))
                return new WPFBitmapWrapper(bitmapWidth, bitmapHeight, xdpi, ydpi);
            return null;
        }
    }
}
