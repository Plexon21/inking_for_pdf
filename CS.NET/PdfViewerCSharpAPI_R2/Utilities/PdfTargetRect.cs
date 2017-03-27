using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace PdfTools.PdfViewerCSharpAPI.Utilities
{
    public class PdfTargetRect
    {
        int _iX, _iY, _iWidth, _iHeight;

        public PdfTargetRect():this(0, 0, 0, 0)
        {
        }
        public PdfTargetRect(int x, int y, int width, int height)
        {
            _iX = x;
            _iY = y;
            _iWidth = width;
            _iHeight = height;
        }
        public PdfTargetRect(System.Windows.Rect rect)
            : this((int)rect.X, (int)rect.Y, (int)rect.Width, (int)rect.Height)
        {
        }
        
        public PdfTargetRect(System.Drawing.Rectangle rect)
            : this(rect.X, rect.Y, rect.Width, rect.Height)
        {
        }

        public static PdfTargetRect CreateFromRectOnViewport(PdfTargetRect rectOnViewport, PdfTargetRect viewportRect)
        {
            return new PdfTargetRect((int)rectOnViewport._iX + viewportRect._iX, (int)rectOnViewport._iY + viewportRect._iY, (int)rectOnViewport._iWidth, (int)rectOnViewport._iHeight);
        }

        public static bool operator ==(PdfTargetRect r1, PdfTargetRect r2)
        {
            if ((object)r1 == null || (object)r2 == null)
                return ((object)r1 == null && (object)r2 == null);

            return (r1._iX == r2._iX && r1._iY == r2._iY && r1._iWidth == r2._iWidth && r1._iHeight == r2._iHeight);
        }
        public static bool operator !=(PdfTargetRect r1, PdfTargetRect r2)
        {
            return !(r1 == r2);
        }

        public override bool Equals(object other)
        {
            PdfTargetRect ot = other as PdfTargetRect;
            return this == ot;
        }

        public static PdfTargetRect operator +(PdfTargetRect r, PdfTargetPoint p)
        {
            return new PdfTargetRect(r.iX + p.iX, r.iY + p.iY, r.iWidth, r.iHeight);
        }

        public override int GetHashCode()
        {
            return iX.GetHashCode() ^ iY.GetHashCode() ^ iWidth.GetHashCode() ^ iHeight.GetHashCode();
        }


        public PdfSourceRect GetSourceRect(double zoomFactor)
        {
            return new PdfSourceRect(PdfUtils.ConvertPixel2Canvas(_iX, zoomFactor), PdfUtils.ConvertPixel2Canvas(_iY, zoomFactor), PdfUtils.ConvertPixel2Canvas(_iWidth, zoomFactor), PdfUtils.ConvertPixel2Canvas(_iHeight, zoomFactor));
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
        public IList<PdfTargetRect> subtractRect(PdfTargetRect subtrahend)
        {
            //crop the subtrahend to be contained within minuend(this)
            subtrahend = subtrahend.intersectInt(this);

            IList<PdfTargetRect> differences = new List<PdfTargetRect>();
            differences.Add(new PdfTargetRect(this._iX, this._iY, this._iWidth, subtrahend._iY - this._iY));
            differences.Add(new PdfTargetRect(this._iX, subtrahend._iY, subtrahend._iX - this._iX, subtrahend._iHeight));
            differences.Add(new PdfTargetRect(this._iX, subtrahend.iBottom, this._iWidth, this.iBottom - subtrahend.iBottom));
            differences.Add(new PdfTargetRect(subtrahend.iRight, subtrahend._iY, this.iRight - subtrahend.iRight, subtrahend._iHeight));
            return differences;
        }


        public void SetSize(int width, int height)
        {
            _iWidth = width;
            _iHeight = height;
        }

        public void Offset(int x, int y)
        {
            _iX += x;
            _iY += y;
        }


        public void RotateSize()
        {
                int temp = _iWidth;
                _iWidth = _iHeight;
                _iHeight = temp;
        }

        public bool IsEmpty
        {
            get
            {
                return _iWidth <= 0 || _iHeight <= 0;
            }
        }
        
        public PdfTargetRect intersectInt(PdfTargetRect otherRect)
        {
            int x = Math.Max(otherRect._iX, _iX);
            int y = Math.Max(otherRect._iY, _iY);
            int width = Math.Min(otherRect.iRight, iRight) - x;
            int height = Math.Min(otherRect.iBottom, iBottom) - y;
            return new PdfTargetRect(x, y, width, height);
        }

        public bool intersectsInt(PdfTargetRect otherRect)
        {
            bool horizontalContained = otherRect._iX < iRight && otherRect.iRight > _iX;
            bool verticalContained = otherRect._iY < iBottom && otherRect.iBottom > _iY;
            return horizontalContained && verticalContained;
        }
        public PdfTargetRect unionInt(PdfTargetRect otherRect)
        {
            int x = Math.Min(otherRect._iX, _iX);
            int y = Math.Min(otherRect._iY, _iY);
            int width = Math.Max(otherRect.iRight, iRight) - x;
            int height = Math.Max(otherRect.iBottom, iBottom) - y;
            return new PdfTargetRect(x, y, width, height);
        }

        public PdfTargetRect Clone()
        {
            return new PdfTargetRect(_iX, _iY, _iWidth, _iHeight);
        }
        public bool containsInt(PdfTargetRect other)
        {
            return other._iX >= _iX && other._iY >= _iY && other.iRight <= iRight && other.iBottom <= iBottom;
        }


        #region properties Int
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
        public int iWidth
        {
            get { return _iWidth; }
            set { _iWidth = value; }
        }
        public int iHeight
        {
            get { return _iHeight; }
            set { _iHeight = value; }
        }

        public int iRight
        {
            get { return _iX + _iWidth; }
            set { _iX = value - _iWidth; }
        }
        public int iBottom
        {
            get { return _iY + _iHeight; }
            set { _iY = value - _iHeight; }
        }

        public int iCenterX
        {
            get { return _iX + _iWidth / 2; }
            set { _iX = value - _iWidth / 2; }
        }
        public int iCenterY
        {
            get { return _iY + _iHeight / 2; }
            set { _iY = value - _iHeight / 2; }
        }

        public PdfTargetPoint iLocation
        {
            get { return new PdfTargetPoint(_iX, _iY); }
            set { _iX = value.iX; _iY = value.iY; }
        }

        public PdfTargetPoint iTopCenter
        {
            get { return new PdfTargetPoint(_iX + _iWidth / 2, _iY); }
            set { _iX = value.iX - _iWidth / 2; _iY = value.iY; }
        }
        public PdfTargetPoint iCenter
        {
            get { return new PdfTargetPoint(_iX + _iWidth / 2, _iY + _iHeight / 2); }
            set { _iX = value.iX - _iWidth / 2; _iY = value.iY - _iHeight / 2; }
        }
        #endregion

        #region Rectangleconverters
        public System.Drawing.Rectangle GetDrawingRect()
        {
            return new System.Drawing.Rectangle(_iX, _iY, _iWidth, _iHeight);
        }


        public System.Drawing.Rectangle GetDrawingRect(PdfTargetRect viewport)
        {
            PdfTargetRect intersected = this.intersectInt(viewport);
            if (intersected.IsEmpty)
                return new System.Drawing.Rectangle(0, 0, 0, 0);
            return new System.Drawing.Rectangle(intersected._iX - viewport.iX, intersected._iY - viewport.iY, intersected._iWidth, intersected._iHeight);
        }

        public System.Windows.Rect GetWinRect(PdfTargetRect viewport)
        {
            PdfTargetRect intersected = this.intersectInt(viewport);
            if (intersected.IsEmpty)
                return new System.Windows.Rect(0, 0, 0, 0);
            return new System.Windows.Rect(intersected._iX - viewport.iX, intersected._iY - viewport.iY, intersected._iWidth, intersected._iHeight);
        }

        public System.Windows.Int32Rect GetInt32Rect()
        {
            return new System.Windows.Int32Rect(_iX, _iY, _iWidth, _iHeight);
        }
        #endregion

        public override string ToString()
        {   
            return String.Format("[{0},{1},{2},{3}]", _iX, _iY, _iWidth, _iHeight);
        }
    }
}
