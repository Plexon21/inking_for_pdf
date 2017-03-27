// -----------------------------------------------------------------------
// <copyright file="PdfTextFragment.cs" company="">
// TODO: Update copyright text.
// </copyright>
// -----------------------------------------------------------------------

namespace PdfTools.PdfViewerCSharpAPI.Utilities
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;
    using PdfTools.PdfViewerCSharpAPI.DocumentManagement;

    /// <summary>
    /// TODO: Update summary.
    /// </summary>
    public class PdfTextFragment
    {
        public PdfTextFragment(PdfDocument.NativeTextFragment frag, int pageNo, PdfTextFragment previousTextFragment)
        {
            rectOnUnrotatedPage = new PdfSourceRect(frag.m_dX, frag.m_dY, frag.m_dWidth, frag.m_dHeight);
            text = frag.m_szTextA;
            this.pageNo = pageNo;
            _lastOnLine = this;

            glyphPositions = new double[frag.m_nGlyphPositionSize];
            Array.Copy(frag.m_pdGlyphPosition, glyphPositions, frag.m_nGlyphPositionSize);

            //if firstOnLine is entirely above this
            if (previousTextFragment==null || previousTextFragment.FirstOnLine.RectOnUnrotatedPage.dY > this.RectOnUnrotatedPage.dBottom)
            {
                _firstOnLine = this;
            }
            else
            {
                _firstOnLine = previousTextFragment.FirstOnLine;
                previousTextFragment._lastOnLine = this;
            }
        }

        public PdfSourceRect RectOnUnrotatedPage
        {
            get
            {
                return rectOnUnrotatedPage;
            }
        }

        public PdfSourceRect GetRectOnUnrotatedPage(int start, int end)
        {
            end = Math.Min(end, glyphPositions.Length - 1);
            PdfSourceRect rect = rectOnUnrotatedPage.Clone();
            rect.dX = glyphPositions[start];
            rect.dWidth = glyphPositions[end] - glyphPositions[start];
            return rect;
        }

        public int GetIndexOfClosestGlyph(double x)
        {
            int start = 0;
            int end = glyphPositions.Length - 1;
            
            while (true)
            {
                if (end - start < 2)
                {
                    //only end and start are the closest indices
                    if(Math.Abs(glyphPositions[end] - x) < Math.Abs(glyphPositions[start] - x))
                        return end;
                    return start;
                }
                int needle = (end - start) / 2 + start;
                switch(x.CompareTo(glyphPositions[needle])){
                    case 1:
                        start = needle;
                        break;
                    case -1:
                        end = needle;
                        break;
                    case 0:
                        return needle;
                }
            }

        }

        public string Text
        {
            get
            {
                return text;
            }
        }
        public int PageNo
        {
            get
            {
                return pageNo;
            }
        }

        PdfTextFragment _firstOnLine;
        public PdfTextFragment FirstOnLine
        {
            get
            {
                return _firstOnLine;
            }
        }
        PdfTextFragment _lastOnLine;
        public PdfTextFragment LastOnLine
        {
            get
            {
                if(_lastOnLine != this)
                    _lastOnLine = _lastOnLine.LastOnLine;
                return _lastOnLine;
            }
        }


        public override string ToString()
        {
            return String.Format("Text={0}, rectOnPage={1}", text, rectOnUnrotatedPage.ToString());
        }

        private PdfSourceRect rectOnUnrotatedPage; //These are source coordinates with origin in bottom left!
        private string text;
        private int pageNo;
        private double[] glyphPositions;
    }
}
