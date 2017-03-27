package com.pdf_tools.pdfviewer.Model;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation;
import com.pdf_tools.pdfviewer.Annotations.APdfMarkupAnnotation;
import com.pdf_tools.pdfviewer.Annotations.PdfTextAnnotation;
import com.pdf_tools.pdfviewer.converter.geom.Rectangle;

public interface IPdfCommon
{
    /**
     * Zoom modes available for set and getZoomMode.
     */
    public enum TFitMode
    {
        /**
         * Default zoom mode
         */
        ACTUAL_SIZE {
            public String toString()
            {
                return "Actual Size";
            }
        },
        /**
         * Fit the width of pages to the given window (If showing 2 columns of
         * pages, then fit to width of both pages next to each other)
         */
        FIT_WIDTH {
            public String toString()
            {
                return "Fit Width";
            }
        },
        /**
         * Fit the page to the window such that it is entirely contained (If
         * showing 2 columns then fit such that both are entirely visible)
         */
        FIT_PAGE {
            public String toString()
            {
                return "Fit Page";
            }
        };

    }

    /**
     * Mouse Modes determine what effect mouse clicking and dragging has.
     * 
     * @see com.pdf_tools.pdfviewer.SwingAPI.PdfViewerComponent#setMouseMode(com.pdf_tools.pdfviewer.Model.IPdfCommon.PdfMouseMode)
     */
    public enum PdfMouseMode
    {
        /**
         * Dragging the mouse will draw a rectangle, and releasing will zoom to
         * said rectangle
         */
        ZOOM {
            public String toString()
            {
                return "Zoom";
            }
        },
        /**
         * Dragging the mouse will scroll the document
         */
        MOVE {
            public String toString()
            {
                return "Move";
            }
        },
        /**
         * Dragging the mouse will highlight selected text
         */
        HIGHLIGHT {
            public String toString()
            {
                return "Highlight";
            }
        },
        /**
         * Dragging the mouse will select all text between the points where the
         * dragging started and ended
         */
        SELECT {
            public String toString()
            {
                return "Select";
            }
        },
        
        /**
         * Draging the mouse will draw a freehand line
         */
        FREEHAND {
            public String toString()
            {
                return "Freehand";
            }
        }
    }

    /**
     * @author fwe Configure how pages are displayed
     */
    public enum TPageLayoutMode
    {
        /**
         * Do not specify what pagelayout should be used
         */
        None(0),
        /**
         * Show a single page at a time
         */
        SinglePage(1),
        /**
         * Show a single column of pages
         */
        OneColumn(2),
        /**
         * Show two columns. The first page is in the left column
         */
        TwoColumnLeft(3),
        /**
         * Show two columns. The first page is in the right column
         */
        TwoColumnRight(4),
        /**
         * Show two pages at a time. The first page is on the left side
         */
        TwoPageLeft(5),
        /**
         * Show two pages at a time. The first page is on the right side
         */
        TwoPageRight(6);
        private final int id;

        TPageLayoutMode(int id)
        {
            this.id = id;
        }

        public int getValue()
        {
            return id;
        }

        public boolean isScrolling()
        {
            return (id == OneColumn.id || id == TwoColumnLeft.id || id == TwoColumnRight.id);
        }

        public int horizontalScrollPosition(int pageNo)
        {
            switch (this)
            {
            case TwoColumnLeft:
            case TwoPageLeft:
                return 1 - 2 * (pageNo % 2);
            case TwoColumnRight:
            case TwoPageRight:
                return (pageNo == 1) ? 0 : -1 + 2 * (pageNo % 2);
            case OneColumn:
            case SinglePage:
            default:
                return 0;
            }
        }
    }

    public enum TDestinationType
    {
        eDestinationInvalid(0), eDestinationFit(1), eDestinationFitH(2), eDestinationFitV(3), eDestinationFitR(4), eDestinationFitB(
                5), eDestinationFitBH(6), eDestinationFitBV(7), eDestinationXYZ(8);
        private final int id;

        TDestinationType(int id)
        {
            this.id = id;
        }

        public int getValue()
        {
            return id;
        }
    }

    /************************
     * Event listeners *
     ************************/

    /**
     * @author fwe Interface for implementing a listener, which triggers, when
     *         the controller completed opening a file
     * 
     */
    public interface IOnOpenCompletedListener
    {
        void onOpenCompleted(PdfViewerException ex);
    }

    /**
     * @author pgl Interface for implementing a listener which triggers when the
     *         controller completed saving a file
     */

    public interface IOnSaveCompletedListener
    {
        void onSaveCompleted(PdfViewerException ex);
    }

    /**
     * @author fwe Interface for implementing a listener, which triggers, when
     *         the controller completed closing a file
     * 
     */
    public interface IOnCloseCompletedListener
    {
        void onCloseCompleted(PdfViewerException ex);
    }

    /**
     * @author fwe Interface for implementing a listener, which triggers, when
     *         starting to close a file (before resources are cleaned up)
     * 
     */
    public interface IOnClosingListener
    {
        void onClosing();
    }

    /**
     * @author fwe Interface for implementing a listener, which triggers, when
     *         the currently displayed pages changed
     * 
     */
    public interface IOnVisiblePageRangeChangedListener
    {
        void onVisiblePageRangeChanged(int firstPage, int lastPage);
    }
    
    public interface IOnDrawCompletedListener
    {
        void onDrawCompleted(BufferedImage bitmap);
    }

    /**
     * @author fwe Interface for implementing a listener, which triggers, when
     *         the controller completed zooming
     */
    public interface IOnZoomCompletedListener
    {
        void onZoomCompleted(double zoomFactor);
    }

    /**
     * @author fwe Interface for implementing a listener, which triggers, when
     *         the pagelayoutmode changes
     */
    public interface IOnPageLayoutModeChangedListener
    {
        void onPageLayoutModeChanged(TPageLayoutMode newMode);
    }

    /**
     * @author fwe Interface for implementing a listener, which triggers, when
     *         the fitmode changes
     */
    public interface IOnFitModeChangedListener
    {
        void onFitModeChanged(TFitMode fitmode);
    }

    /**
     * @author fwe Interface for implementing a listener, which triggers, when
     *         the outlines have been loaded
     */
    public interface IOnOutlinesLoadedListener
    {
        void onOutlinesLoaded(int parentId, PdfOutlineItem items[], PdfViewerException ex);
    }

    /**
     * @author fwe Interface for implementing a listener, which triggers, when
     *         the thumbnails have been loaded
     */
    public interface IOnThumbnailLoadedListener
    {
        void onThumbnailLoaded(int pageNo, BufferedImage bitmap, PdfViewerException ex);
    }

    /**
     * @author fwe Interface for implementing a listener, which triggers, when
     *         text has been extracted
     */
    public interface IOnTextExtractedListener
    {
        void onTextExtracted(String extractedText);
    }

    /**
     * @author pgl Interface for implementing a listener which triggers when the
     *         action of an annotation has been called.
     */
    public interface IOnTextAnnotationActionListener
    {
        void onTextAnnotationAction(PdfTextAnnotation annotation);
    }

    /**
     * 
     * @author pgl Interface for implementing a listener which triggers when the
     *         action of a markup annotation has been called
     */

    public interface IOnMarkupAnnotationActionListener
    {
        void onMarkupAnnotationAction(APdfMarkupAnnotation annotation);
    }

    /**
     * @author pgl Interface for implementing a listener which triggers when the
     *         hover method of an annotation has been called.
     */

    public interface IOnMarkupAnnotationHoverListener
    {
        void onTextAnnotationHover(APdfMarkupAnnotation annotation);
    }

    public interface IOnAnnotationUpdatedListener
    {
        void onAnnotationUpdated(APdfAnnotation annotation);
    }

    public interface IOnAnnotationCreatedListener
    {
        void onAnnotationCreated(int page);
    }

    public interface IOnAnnotationDeletedListener
    {
        void onAnnotationDeleted(int page);
    }

    /**
     * @author fwe Interface for implementing a listener, which triggers, when
     *         the rotation of the document has changed
     */
    public interface IOnRotationChangedListener
    {
        void onRotationChanged(int newRoatation);
    }

    public interface IOnSearchCompletedListener
    {
        void onSearchCompleted(int page, int index, Map<Integer, List<java.awt.geom.Rectangle2D.Double>> rects);
    }

    /**
     * @author fwe Interface for implementing a listener, which triggers, when
     *         the mousemode changed
     */
    public interface IOnMouseModeChangedListener
    {
        void onMouseModeChanged(PdfMouseMode newMouseMode);
    }

}
