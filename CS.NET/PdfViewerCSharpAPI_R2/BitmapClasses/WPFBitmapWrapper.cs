// -----------------------------------------------------------------------
// <copyright file="WPFBitmap.cs" company="">
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
    public class WPFBitmapWrapper : IGenericBitmap
    {
        public WPFBitmapWrapper(int bitmapWidth, int bitmapHeight, double xdpi, double ydpi)
        {
            bitmap = new System.Windows.Media.Imaging.WriteableBitmap(bitmapWidth, bitmapHeight, xdpi, ydpi, pixelFormat, null);
        }

        public IntPtr BackBuffer
        {
            get
            {
                return bitmap.BackBuffer;
            }
        }

        public int PixelWidth { get { return bitmap.PixelWidth; } }
        public int PixelHeight { get { return bitmap.PixelHeight; } }

        public void Freeze()
        {
            bitmap.Freeze();
        }
        public void LockRect(int x, int y, int width, int height)
        {
            bitmap.Lock();
            this.rect = new System.Windows.Int32Rect(x, y, width, height);
        }
        public void UnLock()
        {
            if (rect == null)
                throw new InvalidOperationException("WinFormsBitmapWrapper.UnLock() may only be called if previously WinFormsBitmapWrapper.Lock(rect) has been called");
            bitmap.AddDirtyRect(rect);
            bitmap.Unlock();
        }
        public Object Unwrap()
        {
            return bitmap;
        }

        private System.Windows.Media.Imaging.WriteableBitmap bitmap;
        private System.Windows.Int32Rect rect;

        private readonly System.Windows.Media.PixelFormat pixelFormat = System.Windows.Media.PixelFormats.Pbgra32;
    }
}
