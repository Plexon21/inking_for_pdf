/***************************************************************************
*
* File:            IPdfDocument.java
* 
* Package:         3-Heights(TM) PDF Java Viewer
*
* Description:     The interface of a PDF document.
*
* @author          Christian Hagedorn, PDF Tools AG   
* 
* Copyright:       Copyright (C) 2001 - 2008 PDF Tools AG, Switzerland
*                  All rights reserved.
*                  
***************************************************************************/

package com.pdf_tools.pdfviewer.DocumentManagement;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import com.pdf_tools.pdfviewer.Model.PdfAdvancedDestination;
import com.pdf_tools.pdfviewer.Model.PdfCanvas;
import com.pdf_tools.pdfviewer.Model.PdfOutlineItem;
import com.pdf_tools.pdfviewer.Model.PdfTextFragment;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;
import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation;
import com.pdf_tools.pdfviewer.Annotations.PdfGenericAnnotation;
import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation.TPdfAnnotationType;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.TPageLayoutMode;
import com.pdf_tools.pdfviewer.Model.PdfViewerController.Viewport;
import com.pdf_tools.pdfviewer.Model.PdfViewerException.NoFileOpenedException;
import com.pdf_tools.pdfviewer.converter.geom.Rectangle;

/**
 * The IPdfDocument interface describes the interface to the document class.
 * 
 * @author cha
 *
 */
public interface IPdfDocument
{
    /**
     * Open the file with the associated password located at the absolute path
     * specified by filename. If the file does not contain a password an empty
     * string must be passed. This method is invoked by {@link PdfCanvas#open()}
     * method of {@code PdfCanvas} class in the delegation chain.
     * 
     * @param filename
     *            The absolute path to the file
     * @param password
     *            The password needed to open the file (if no password required
     *            pass "" as password)
     * @throws PdfViewerException
     */
    public void open(String filename, String password) throws PdfViewerException;

    /**
     * Close the currently open document. This method is invoked by
     * {@link PdfCanvas#close()} method of {@code PdfCanvas} class in the
     * delegation chain.
     * 
     * @throws PdfViewerException
     */
    public void close() throws PdfViewerException;

    /**
     * Save the current file to path
     * 
     * @param path
     * @throws PdfViewerException
     */
    public void saveAs(String path) throws PdfViewerException;

    /**
     * Create new annotation
     * 
     * @param type:
     *            Type of the annotation
     * @paran page: page of the annotation
     * @param rect:
     *            Rectangle of the annotation
     * @returns PdfGenericAnnotation the newly created annotation that has to be
     *          added to the annotation cache
     * @throws PdfViewerException
     */
    public PdfGenericAnnotation createAnnotation(TPdfAnnotationType type, int page, Double[] rect) throws PdfViewerException;

    /**
     * Delete annotation
     * 
     * @param annotHandle:
     *            pointer to the annotation
     * @returns boolean: true for successful deletion, false otherwise
     */

    public void deleteAnnotation(long annotHandle, int page) throws PdfViewerException;

    /**
     * Returns the number of pages of the currently open document. If the
     * current document is not open then 0 is returned.
     * 
     * @return The number of pages
     * @throws com.pdf_tools.pdfviewer.Model.PdfViewerException.NoFileOpenedException
     */
    public int getPageCount() throws NoFileOpenedException;

    public Map<Integer, Rectangle.Double> getPageRangeFromSource(List<Integer> pages) throws PdfViewerException;

    public boolean isOpen();

    public TPageLayoutMode getPageLayout() throws PdfViewerException;

    public PdfAdvancedDestination getOpenActionDestination() throws PdfViewerException;

    public PdfOutlineItem[] getOutlines(int parentId) throws PdfViewerException;

    public PdfGenericAnnotation[] getAnnotationsOnPage(int pageNo) throws PdfViewerException;

    public void updateAnnotation(APdfAnnotation annotation) throws PdfViewerException;

    public List<PdfTextFragment> getTextFragments(int pageNo) throws PdfViewerException;

    public void draw(BufferedImage bitmap, int rotation, Map<Integer, Rectangle.Double> pageRectsDict, Viewport viewport)
            throws PdfViewerException;

    BufferedImage LoadThumbnail(double sourceWidth, double sourceHeight, int targetWidth, int targetHeight, int page)
            throws PdfViewerException;

}
