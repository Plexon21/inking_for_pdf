
namespace PdfTools.PdfViewerCSharpAPI.Utilities
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;

    /// <summary>
    /// Representation of a Location on the 2D plane.
    /// Has internal representation in source and targetCoordinates transformed by PdfUtils.ConvertPixel2Canvas and PdfUtils.ConvertCanvas2Pixel.
    /// The coordinate transformations only get used, if necessary, I.E. if a point has been created in Canvascoordinates it will never be transformed until the first time it is used in Pixelcoordinates.
    /// </summary>
    public class PdfTargetPoint
    {
        int _iX, _iY;

        public PdfTargetPoint():this(0, 0)
        {
        }

        public PdfTargetPoint(System.Windows.Point point):this((int)point.X, (int)point.Y)
        {
        }

        public PdfTargetPoint(System.Drawing.Point point):this(point.X, point.Y)
        {
        }

        public PdfTargetPoint(int x, int y)
        {
            _iX = x;
            _iY = y;
        }

        public PdfSourcePoint GetSourcePoint(double zoomFactor)
        {
            return new PdfSourcePoint(PdfUtils.ConvertPixel2Canvas(_iX, zoomFactor), PdfUtils.ConvertPixel2Canvas(_iY, zoomFactor));
        }

        public static PdfTargetPoint operator +(PdfTargetPoint p1, PdfTargetPoint p2)
        {
            return new PdfTargetPoint(p1.iX + p2.iX, p1.iY + p2.iY);
        }

        public static PdfTargetPoint operator -(PdfTargetPoint p1, PdfTargetPoint p2)
        {
            return new PdfTargetPoint(p1.iX - p2.iX, p1.iY - p2.iY);
        }


        public void MultInt(double mult)
        {
            _iX = (int)((double)_iX * mult);
            _iY = (int)((double)_iY * mult);
        }

        #region Setting methods Int
        public void SetLocation(int x, int y)
        {
            _iX = x;
            _iY = y;
        }
        #endregion

        
        #region propertie getters Int
        public int iX
        {
            get { return _iX; }
            set { _iX = value; }
        }
        public int iY
        {
            get { return _iY; }
            set { _iY = value; }
        }
        #endregion

        public override string ToString()
        {
            return String.Format("Int:[{0},{1}]", _iX, _iY);
        }
    }
}
