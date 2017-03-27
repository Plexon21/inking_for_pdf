package com.pdf_tools.pdfviewer.DocumentManagement;

import java.util.List;
import java.util.Map;

import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation;
import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation.TPdfAnnotationType;
import com.pdf_tools.pdfviewer.Model.PdfTextFragment;
import com.pdf_tools.pdfviewer.Model.PdfViewerController;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;
import com.pdf_tools.pdfviewer.Model.PdfViewerException.NoFileOpenedException;
import com.pdf_tools.pdfviewer.Requests.*;
import com.pdf_tools.pdfviewer.caching.IGenericCache;
import com.pdf_tools.pdfviewer.converter.geom.Dimension;
import com.pdf_tools.pdfviewer.converter.geom.Rectangle;

/**
 * @author fwe The document manager, as it is seen by the canvas and controller
 */
public interface IPdfDocumentManager
{

    /**
     * queues drawcommand on the document, returns immediately
     * 
     * @param bitmap
     *            the bitmap to draw into
     * @param pages
     *            the pagenumbers of the pages that have to be drawn
     * @param sourceRects
     *            the rectangles which have to be read from the source on the
     *            page
     * @param targetRects
     *            the rectangles where the pages have to be drawn on the bitmap
     * @param zoomFactor
     *            the zoomfactor which translates source to target coords
     * 
     */
    public PdfDrawRequest draw(Dimension bitmapSize, int rotation, Map<Integer, Rectangle.Double> pageRects,
            PdfViewerController.Viewport viewport);

    /**
     * Queues opening of file, returns immediately
     * 
     * @param filename
     * @param password
     * 
     *            Will notify end of operation via
     *            this.controller.onOpenCompleted()
     */
    public PdfOpenRequest open(String filename, String password);

    /**
     * Save file
     * 
     * @param path
     * 
     */

    public PdfSaveRequest saveAs(String path) throws PdfViewerException;

    /**
     * Queues closing of file, returns immediately Will notify end of operation
     * via this.controller.onCloseCompleted()
     */
    public void close();

    /**
     * @returns number of pages in the associated Document
     */
    public int getPageCount() throws NoFileOpenedException;

    public void setRotation(int rotation);

    public Rectangle.Double getPage(int page) throws PdfViewerException;

    public Rectangle.Double getPageGuess(int page) throws PdfViewerException;
    
    public boolean isOpen();

    public PdfGetPageLayoutRequest requestPageLayoutMode();

    public PdfGetOpenActionDestinationRequest requestOpenActionDestination();

    public PdfGetOutlinesRequest requestOutlines(int parentId);

    public PdfGetThumbnailRequest requestThumbnail(double sourceWidth, double sourceHeight, int targetWidth, int targetHeight, int page);

    public List<PdfTextFragment> getTextFragments(int page) throws PdfViewerException;

    public List<APdfAnnotation> getAnnotationsOnPage(int page) throws PdfViewerException;

    public PdfUpdateAnnotationRequest updateAnnotation(APdfAnnotation annotation) throws PdfViewerException;

    public void createAnnotation(TPdfAnnotationType type, int page, Double[] rect) throws PdfViewerException;

    public void deleteAnnotation(int page, long annotHandle) throws PdfViewerException;

    public void registerPagesLoadedToCacheListener(IGenericCache.IItemLoadedToCacheListener<Integer> listener);
}
