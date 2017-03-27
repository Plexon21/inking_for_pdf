// -----------------------------------------------------------------------
// <copyright file="WinFormsBitmap.cs" company="">
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
    public class WinFormsBitmapWrapper : IGenericBitmap
    {
        public WinFormsBitmapWrapper(int bitmapWidth, int bitmapHeight, double xdpi, double ydpi)
        {
            bitmap = new System.Drawing.Bitmap(bitmapWidth, bitmapHeight, pixelFormat);
        }
        public IntPtr BackBuffer
        {
            get
            {
                return bitmapData.Scan0;
            }
        }
        public int PixelWidth { get { return bitmap.Width; } }
        public int PixelHeight { get { return bitmap.Height; } }

        public void Freeze()
        {
            
        }
        public void LockRect(int x, int y, int width, int height)
        {
            bitmapData = bitmap.LockBits(new System.Drawing.Rectangle(0, 0, bitmap.Width, bitmap.Height), System.Drawing.Imaging.ImageLockMode.ReadWrite, pixelFormat); 
            //bitmapData = bitmap.LockBits(new System.Drawing.Rectangle(x, y, width, height), System.Drawing.Imaging.ImageLockMode.ReadWrite, pixelFormat); 
        }
        public void UnLock()
        {
            if (bitmapData == null)
                throw new InvalidOperationException("WinFormsBitmapWrapper.UnLock() may only be called if previously WinFormsBitmapWrapper.Lock(rect) has been called");
            bitmap.UnlockBits(bitmapData);
        }
        public Object Unwrap()
        {
            return bitmap;
        }

        private System.Drawing.Bitmap bitmap;
        private System.Drawing.Imaging.BitmapData bitmapData;
        private const System.Drawing.Imaging.PixelFormat pixelFormat = System.Drawing.Imaging.PixelFormat.Format32bppPArgb;
    }
}
