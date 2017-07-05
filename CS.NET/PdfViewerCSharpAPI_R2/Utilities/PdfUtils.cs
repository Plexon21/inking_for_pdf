
namespace PdfTools.PdfViewerCSharpAPI.Utilities
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;
    using System.Windows;
    using System.IO;
    using System.Windows.Media.Imaging;
    
    using PdfTools.PdfViewerCSharpAPI.Model;
    using System.Windows.Media;

    /// <summary>
    /// Library of utility methods used by PdfViewer
    /// </summary>
    public static class PdfUtils
    {

        public static double ConvertPixel2Canvas(int pixel, double zoomFactor)
        {
            return ((double)pixel) / AdditionalZoomFactorX / zoomFactor;
        }

        public static Rect ConvertPixel2Canvas(Int32Rect pixelRect, double zoomFactor)
        {
            return new Rect(ConvertPixel2Canvas(pixelRect.X, zoomFactor), ConvertPixel2Canvas(pixelRect.Y, zoomFactor), ConvertPixel2Canvas(pixelRect.Width, zoomFactor), ConvertPixel2Canvas(pixelRect.Height, zoomFactor));
        }


        public static int ConvertCanvas2Pixel(double canvasCoord, double zoomFactor)
        {
            return (int)(canvasCoord * zoomFactor * AdditionalZoomFactorX);
        }

        public static Int32Rect ConvertCanvas2Pixel(Rect onCanvasRect, double zoomFactor)
        {
            return new Int32Rect(ConvertCanvas2Pixel(onCanvasRect.X, zoomFactor), ConvertCanvas2Pixel(onCanvasRect.Y, zoomFactor), ConvertCanvas2Pixel(onCanvasRect.Width, zoomFactor), ConvertCanvas2Pixel(onCanvasRect.Height, zoomFactor));
        }

        public static Int32Rect IntersectIntRect(Int32Rect r1, Int32Rect r2)
        {
            int x = Math.Max(r1.X, r2.X);
            int y = Math.Max(r1.Y, r2.Y);
            int width = Math.Min(r1.X + r1.Width, r2.X + r2.Width) - x;
            int height = Math.Min(r1.Y + r1.Height, r2.Y + r2.Height) - y;
            if (width <= 0 || height <= 0)
            {
                return new Int32Rect();//The intersection is empty
            }
            else
            {
                return new Int32Rect(x, y, width, height);
            }
        }

        public static Point SubtractPoint(Point minuend, Point subtrahend){
            return new Point(minuend.X - subtrahend.X, minuend.Y - subtrahend.Y);
        }

        public static double AdditionalZoomFactorX = 1.0;
        public static double AdditionalZoomFactorY = 1.0;

        public static Rect ConvertInt32Rect2Rect(Int32Rect rect)
        {
            return new Rect((double)rect.X, (double)rect.Y, (double)rect.Width, (double)rect.Height);
        }

        public static bool IsInt32RectSizeEqual(Int32Rect r1, Int32Rect r2)
        {
            return r1.Width == r2.Width && r1.Height == r2.Height;
        }

        /// <summary>
        /// Calculates the native DPI of the UIelements on the screen and saves them for later conversions
        /// This has to be called before using the conversion methods ConverCanvas2Pixel and ConvertPixel2Canvas
        /// </summary>
        /// <param name="source"></param>
        public static void RegisterSource(PresentationSource source)
        {
            AdditionalZoomFactorX = source.CompositionTarget.TransformToDevice.M11;
            AdditionalZoomFactorY = source.CompositionTarget.TransformToDevice.M22;
            if (AdditionalZoomFactorX != AdditionalZoomFactorY)
                throw new NotImplementedException("This application cant deal with different horizontal and vertical DPI yet");
        }

        /// <summary>
        /// Creates a byteArray, which is efficiently filled with a repeating sequence of bytes
        /// Optimised for performance on long bytearrays
        /// </summary>
        /// <param name="sequence">The sequence of bytes that is repeated</param>
        /// <param name="numberOfRepetitions">number of repetitions of sequence</param>
        /// <returns>filled byteArray with length = sequence.length * numberOfRepetitions</returns>

        public static byte[] CreateFilledArray(byte[] sequence, int numberOfRepetitions)
        {
            byte[] resultArray = new byte[sequence.Length * numberOfRepetitions];
            Array.Copy(sequence, resultArray, sequence.Length);
            int repetitions = 1;
            for (; repetitions < numberOfRepetitions/2; repetitions *= 2)
            {
                Array.Copy(resultArray, 0, resultArray, repetitions * sequence.Length, repetitions * sequence.Length);
            }
            Array.Copy(resultArray, 0, resultArray, repetitions * sequence.Length, (numberOfRepetitions - repetitions) * sequence.Length);
            return resultArray;
        }

        /// <summary>
        /// Saves a bitmap to disk for debugging purposes
        /// </summary>
        /// <param name="filename"></param>
        /// <param name="image"></param>
        public static void SaveBitmap(string filename, WriteableBitmap image)
        {
            if (filename != string.Empty)
            {
                using (FileStream stream = new FileStream(filename, FileMode.Create))
                {
                    PngBitmapEncoder encoder = new PngBitmapEncoder();
                    encoder.Frames.Add(BitmapFrame.Create(image));
                    encoder.Save(stream);
                    stream.Close();
                }
            }
        }


        /// <summary>
        /// Returns whether the given page in the given pagelayoutmode is left(-1), center(0) or right(+1) positioned on the canvas
        /// </summary>
        /// <param name="e">Active PageLayoutMode</param>
        /// <param name="pageNo">pageNumber</param>
        /// <returns></returns>
        public static int HorizontalPagePosition(TPageLayoutMode e, int pageNo){
            switch(e)
            {
                case TPageLayoutMode.TwoColumnLeft:
                case TPageLayoutMode.TwoPageLeft:
                    return 1 - 2 * (pageNo % 2);
                case TPageLayoutMode.TwoColumnRight:
                case TPageLayoutMode.TwoPageRight:
                    return (pageNo == 1) ? 0 : -1 + 2 * (pageNo % 2);
                case TPageLayoutMode.OneColumn:
                case TPageLayoutMode.SinglePage:
                default:
                    return 0;
            }
        }

        public static bool PageLayoutScrollingEnabled(TPageLayoutMode e)
        {
            switch (e)
            {
                case TPageLayoutMode.SinglePage:
                case TPageLayoutMode.TwoPageLeft:
                case TPageLayoutMode.TwoPageRight:
                    return false;
                case TPageLayoutMode.TwoColumnLeft:
                case TPageLayoutMode.TwoColumnRight:
                case TPageLayoutMode.OneColumn:
                    return true;
                default:
                    throw new ArgumentException();
            }
        }

        public static double[] ConvertRGBToCYMK(Color color)
        {
            double red = color.R / 255.0;
            double green = color.G / 255.0;
            double blue = color.B / 255.0;

            double black = Math.Min(1 - red, Math.Min(1 - green, 1 - blue));
            double cyan = (1 - red - black) / (1 - black);
            double magenta = (1 - green - black) / (1 - black);
            double yellow = (1 - blue - black) / (1 - black);

            return new double[] { cyan, magenta, yellow, black };
        }

    }
}
