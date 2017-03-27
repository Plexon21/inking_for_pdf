
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
    public class PdfSourcePoint
    {
        double _dX, _dY;

     
        public PdfSourcePoint():this(0.0, 0.0)
        {
        }

        public PdfSourcePoint(double x, double y)
        {
            _dX = x;
            _dY = y;
        }

        public PdfTargetPoint GetTargetPoint(double zoomFactor)
        {
            return new PdfTargetPoint(PdfUtils.ConvertCanvas2Pixel(_dX, zoomFactor), PdfUtils.ConvertCanvas2Pixel(_dY, zoomFactor));
        }

        public static PdfSourcePoint operator +(PdfSourcePoint p1, PdfSourcePoint p2)
        {
            return new PdfSourcePoint(p1.dX + p2.dX, p1.dY + p2.dY);
        }
        public static PdfSourcePoint operator -(PdfSourcePoint p1, PdfSourcePoint p2)
        {
            return new PdfSourcePoint(p1.dX - p2.dX, p1.dY - p2.dY);
        }

        public void RotateAroundOrigin(int rotation)
        {
            rotation = (rotation + 360) % 360;
            switch(rotation)
            {
                case 0:
                    break;
                case 90:
                    double x = _dX;
                    _dX = -_dY;
                    _dY = x;
                    break;
                case 180:
                    _dX = -_dX;
                    _dY = -_dY;
                    break;
                case 270:
                    double xx = _dX;
                    _dX = _dY;
                    _dY = -xx;
                    break;
                default:
                    throw new InvalidProgramException();
            }
        }

        public double dX
        {
            get {  return _dX; }
            set {  _dX = value; }
        }
        public double dY
        {
            get {  return _dY; }
            set {  _dY = value; }
        }

        public override string ToString()
        {
            return String.Format("Dbl:[{0},{1}]", _dX, _dY);
        }
    }
}
