package com.pdf_tools.pdfviewer.Model;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation;
import com.pdf_tools.pdfviewer.Annotations.APdfMarkupAnnotation;
import com.pdf_tools.pdfviewer.Annotations.PdfGenericAnnotation;
import com.pdf_tools.pdfviewer.converter.geom.Rectangle;

/**
 * @author fwe The Interface of the Controller, as seen by the Documentmanager
 *         These methods are expected to run in the dispatcher thread (except
 *         for invokeCallback, which can be run in any thread)
 */
public interface IPdfControllerCallbackManager
{

    /**
     * Callback called by Worker, when Opening completed
     * 
     * @param arg0
     * @param arg1
     */
    void onOpenCompleted(PdfViewerException ex);

    /**
     * Callback called by Worker, when Closing completed
     */
    public void onCloseCompleted(PdfViewerException e);

    public void onClosing();

    /**
     * Callback called by Worker, when Drawing completed
     */
    public void onDrawCompleted(BufferedImage bitmap, PdfViewerException e);

    public void onOutlinesLoaded(int parentId, PdfOutlineItem items[], PdfViewerException e);

    public void onAnnotationsLoaded(int pageNo, PdfGenericAnnotation annotations[], PdfViewerException e);

    public void onAnnotationUpdated(APdfAnnotation annotation, PdfViewerException e);

    public void onAnnotationDeleted(int page, PdfViewerException e) throws PdfViewerException;

    public void onAnnotationCreated(int page);

    public void fireOnAnnotationDeleted(int page);
    
    public interface IAnnotationViewerListener
    {
        void onNoAnnotationSelected();

        void onMarkupAnnotationClicked(APdfMarkupAnnotation annotation);
    }
    
    public interface IInternalOnSearchCompletedListener
    {
        void internalOnSearchCompleted(int page, int index, Map<Integer, List<Rectangle.Double>> rects);
    }

    /**
     * Callback by worker, to inform controller of an occured exception
     * 
     * @param e
     */
    public void exceptionOccurred(PdfViewerException e);

    /**
     * Callback called by worker, when thumbnail is loaded
     * 
     * @param bitmap
     * @param e
     */
    public void onThumbnailLoadCompleted(int pageNo, BufferedImage bitmap, PdfViewerException e);

    void onSaveCompleted(PdfViewerException ex);

}
