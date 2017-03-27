using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace PdfTools.PdfViewerCSharpAPI.Utilities
{
    public class PdfSourceRect : IEquatable<PdfSourceRect>
    {
        double _dX, _dY, _dWidth, _dHeight;

        public PdfSourceRect() : this(0.0, 0.0, 0.0, 0.0) { }

        public PdfSourceRect(double x, double y, double width, double height)
        {
            _dX = x;
            _dY = y;
            _dWidth = width;
            _dHeight = height;
        }

        public override int GetHashCode()
        {
            return dX.GetHashCode() ^ dY.GetHashCode() ^ dWidth.GetHashCode() ^ dHeight.GetHashCode();
        }

        public static bool operator ==(PdfSourceRect r1, PdfSourceRect r2)
        {
            if(ReferenceEquals(r1, r2))
                return true;
            if ((object)r1 == null || (object)r2 == null)
                return false;

            return (r1._dX == r2._dX && r1._dY == r2._dY && r1._dWidth == r2._dWidth && r1._dHeight == r2._dHeight);
        }
        public static bool operator !=(PdfSourceRect r1, PdfSourceRect r2)
        {
            return !(r1 == r2);
        }
        public override bool Equals(object other)
        {
            PdfSourceRect ot = other as PdfSourceRect;
            return this == ot;
        }




        public PdfTargetRect GetTargetRect(double zoomFactor)
        {
            return new PdfTargetRect(PdfUtils.ConvertCanvas2Pixel(_dX, zoomFactor), PdfUtils.ConvertCanvas2Pixel(_dY, zoomFactor), PdfUtils.ConvertCanvas2Pixel(_dWidth, zoomFactor), PdfUtils.ConvertCanvas2Pixel(_dHeight, zoomFactor));
        }

        public void Offset(double x, double y)
        {
            _dX += x;
            _dY += y;
        }
        public void RotateSize()
        {
            double temp = _dWidth;
            _dWidth = _dHeight;
            _dHeight = temp;
        }
        public void ExtendToBeVerticallySymetrical()
        {
            _dX = Math.Min(_dX, -(_dX + _dWidth));
            _dWidth = -2 * _dX;
        }

        public void RotateProperDouble(int rotation, PdfSourcePoint point)
        {
            double temp;
            rotation = (rotation + 360) % 360;
            switch (rotation)
            {
                case 90:
                    //temp = (point.dY - _dY -_dHeight) + point.dX;
                    temp = -(_dY + _dHeight - point.dY) + point.dX;
                    _dY = (_dX - point.dX) + point.dY;
                    _dX = temp;
                    temp = _dWidth;
                    _dWidth = _dHeight;
                    _dHeight = temp;
                    break;
                case 180:
                    _dX = point.dX - (dRight - point.dX);
                    _dY = point.dY - (dBottom - point.dY);
                    break;
                case 270:
                    temp = (_dY - point.dY) + point.dX;
                    _dY = -(_dX + _dWidth - point.dX) + point.dY;
                    _dX = temp;
                    temp = _dWidth;
                    _dWidth = _dHeight;
                    _dHeight = temp;
                    break;
                default:
                    break;
            }
        }

        public void Shrink(double percentageToShrinkTo)
        {
            double shave = (1.0 - percentageToShrinkTo) / 2.0;
            _dX += _dWidth * shave;
            _dY += _dHeight * shave;
            _dWidth *= percentageToShrinkTo;
            _dHeight *= percentageToShrinkTo;
        }
        public PdfSourceRect intersectDouble(PdfSourceRect otherRect)
        {
            double x = Math.Max(otherRect._dX, _dX);
            double y = Math.Max(otherRect._dY, _dY);
            double width = Math.Min(otherRect.dRight, dRight) - x;
            double height = Math.Min(otherRect.dBottom, dBottom) - y;
            return new PdfSourceRect(x, y, width, height);
        }

        /// <summary>
        ///    |---------------------------------| 
        ///    |               r1                |
        ///    |---------------------------------|
        ///    |   r2   |  subtrahend   |   r4   |
        ///    |---------------------------------|
        ///    |               r3                |
        ///    |---------------------------------|
        /// </summary>
        /// <param name="subtrahend"></param>
        /// <returns>r1-r4</returns>

        public IList<PdfSourceRect> subtractRect(PdfSourceRect subtrahend)
        {
            //crop the subtrahend to be contained within minuend(this)
            subtrahend = subtrahend.intersectDouble(this);

            IList<PdfSourceRect> differences = new List<PdfSourceRect>();
            differences.Add(new PdfSourceRect(this._dX, this._dY, this._dWidth, subtrahend._dY - this._dY));
            differences.Add(new PdfSourceRect(this._dX, subtrahend._dY, subtrahend._dX - this._dX, subtrahend._dHeight));
            differences.Add(new PdfSourceRect(this._dX, subtrahend.dBottom, this._dWidth, this.dBottom - subtrahend.dBottom));
            differences.Add(new PdfSourceRect(subtrahend.dRight, subtrahend._dY, this.dRight - subtrahend.dRight, subtrahend._dHeight));
            return differences;
        }
        public bool intersectsDouble(PdfSourceRect otherRect)
        {
            bool horizontalContained = otherRect.dX < dRight && otherRect.dRight > _dX;
            bool verticalContained = otherRect.dY < dBottom && otherRect.dBottom > _dY;
            return horizontalContained && verticalContained;
        }

        public PdfSourceRect unionDouble(PdfSourceRect otherRect)
        {
            double x = Math.Min(otherRect.dX, _dX);
            double y = Math.Min(otherRect.dY, _dY);
            double width = Math.Max(otherRect.dRight, dRight) - x;
            double height = Math.Max(otherRect.dBottom, dBottom) - y;
            return new PdfSourceRect(x, y, width, height);
        }

        public double ShortestDistanceSquared(PdfSourcePoint point)
        {
            double l = dX - point.dX;
            double r = point.dX - dRight;
            double t = dY - point.dY;
            double b = point.dY - dBottom;
            double h = Math.Max(0.0, Math.Max(l, r));
            double v = Math.Max(0.0, Math.Max(t, b));
            return v * v + h * h;
        }

        public bool IsEmpty
        {
            get
            {
                return _dWidth <= 0 || _dHeight <= 0;
            }
        }

        public PdfSourceRect Clone()
        {
            return new PdfSourceRect(_dX, _dY, _dWidth, _dHeight);
        }
        public bool contains(PdfSourceRect other)
        {
            return other.dX >= _dX && other.dY >= _dY && other.dRight <= dRight && other.dBottom <= dBottom;
        }
        public bool contains(PdfSourcePoint point)
        {
            return point.dX >= _dX && point.dY >= _dY && point.dX <= dRight && point.dY <= dBottom;
        }

        public PdfSourcePoint GetOnPageCoordinates(PdfSourcePoint onCanvasCoordinates, int rotation)
        {
            PdfSourcePoint origin; //origin of page in canvas coordinates (botleft corner of unrotated page)
            switch(rotation){
                case 0:
                    origin = new PdfSourcePoint(_dX, dBottom);break;
                case 90:
                    origin = new PdfSourcePoint(_dX, _dY);break;
                case 180:
                    origin = new PdfSourcePoint(dRight, _dY);break;
                case 270:
                    origin = new PdfSourcePoint(dRight, dBottom);break;
                default:
                    throw new InvalidProgramException();
            }

            PdfSourcePoint onPage = onCanvasCoordinates - origin;

            onPage.RotateAroundOrigin(360 - rotation);
            onPage.dY = -onPage.dY;
                
            return onPage;
        }


        #region properties
        public double dX
        {
            get { return _dX; }
            set { _dX = value; }
        }
        public double dY
        {
            get { return _dY; }
            set { _dY = value; }
        }
        public double dWidth
        {
            get { return _dWidth; }
            set { _dWidth = value; }
        }
        public double dHeight
        {
            get { return _dHeight; }
            set { _dHeight = value; }
        }

        public double dRight
        {
            get { return _dX + _dWidth; }
            set { _dX = value - _dWidth; }
        }
        public double dBottom
        {
            get { return _dY + _dHeight; }
            set { _dY = value - _dHeight; }
        }

        public double dCenterX
        {
            get { return _dX + _dWidth / 2.0; }
            set { _dX = value - _dWidth / 2.0; }
        }
        public double dCenterY
        {
            get { return _dY + _dHeight / 2.0; }
            set { _dY = value - _dHeight / 2.0; }
        }

        public PdfSourcePoint dLocation
        {
            get { return new PdfSourcePoint(_dX, _dY); }
            set { _dX = value.dX; _dY = value.dY; }
        }

        public PdfSourcePoint dTopCenter
        {
            get { return new PdfSourcePoint(_dX + _dWidth / 2.0, _dY); }
        }
        public PdfSourcePoint dCenter
        {
            get { return new PdfSourcePoint(_dX + _dWidth / 2.0, _dY + _dHeight / 2.0); }
        }
        #endregion


        public PdfSourceRect CalculateRectOnCanvas(PdfSourceRect pageOnCanvasRect, int rotation)
        {
            PdfSourceRect rectOnCanvas = this.Clone();
            rectOnCanvas.dY = ((rotation % 180 == 0) ? pageOnCanvasRect.dHeight : pageOnCanvasRect.dWidth) - rectOnCanvas.dY - rectOnCanvas.dHeight;//transform from origin botleft to topleft

            double widthHalf = pageOnCanvasRect.dWidth / 2.0;    //attention! these are already rotated!
            double heightHalf = pageOnCanvasRect.dHeight / 2.0;  //attention! these are already rotated!
            switch (rotation)
            {
                case 90:
                    rectOnCanvas.RotateProperDouble(rotation, new PdfSourcePoint(widthHalf, widthHalf));
                    break;
                case 180:
                    rectOnCanvas.RotateProperDouble(rotation, new PdfSourcePoint(widthHalf, heightHalf));
                    break;
                case 270:
                    rectOnCanvas.RotateProperDouble(rotation, new PdfSourcePoint(heightHalf, heightHalf));
                    break;
                default:
                    break;
            }
            rectOnCanvas.Offset(pageOnCanvasRect.dX, pageOnCanvasRect.dY);
            return rectOnCanvas;
        }

        public bool Equals(PdfSourceRect other)
        {
            return (this.dX == other.dX && this.dY == other.dY && this.dWidth == other.dWidth && this.dHeight == other.dHeight);
        }

        public override string ToString()
        {
            return String.Format("[{0},{1},{2},{3}]", _dX, _dY, _dWidth, _dHeight);
        }
    }//end of class
}
