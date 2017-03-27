package com.pdf_tools.pdfviewer.DocumentManagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation;
import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation.TPdfAnnotationType;
import com.pdf_tools.pdfviewer.Annotations.PdfGenericAnnotation;
import com.pdf_tools.pdfviewer.Annotations.PdfGenericMarkupAnnotation;
import com.pdf_tools.pdfviewer.Annotations.PdfHighlightAnnotation;
import com.pdf_tools.pdfviewer.Annotations.PdfLinkAnnotation;
import com.pdf_tools.pdfviewer.Annotations.PdfTempAnnotation;
import com.pdf_tools.pdfviewer.Annotations.PdfTextAnnotation;
import com.pdf_tools.pdfviewer.Annotations.PdfWidgetAnnotation;
import com.pdf_tools.pdfviewer.Model.*;
import com.pdf_tools.pdfviewer.Model.PdfViewerException.NoFileOpenedException;
import com.pdf_tools.pdfviewer.Requests.*;
import com.pdf_tools.pdfviewer.caching.AlwaysRememberCache;
import com.pdf_tools.pdfviewer.caching.IGenericCache;
import com.pdf_tools.pdfviewer.converter.geom.Dimension;
import com.pdf_tools.pdfviewer.converter.geom.Rectangle;

public class PdfDocumentManagerMultithreaded implements IPdfDocumentManager
{

    public PdfDocumentManagerMultithreaded(IPdfControllerCallbackManager controller)
    {
        m_controller = (PdfViewerController) controller;
        queue = new PriorityBlockingQueue<IPdfRequest>(16, new PdfRequestComparator());
        document = new PdfDocument();
        worker = new Thread(new PdfDocumentManagerWorker(controller, queue, document));
        worker.start();
        pageCache = new AlwaysRememberCache<Integer, Rectangle.Double, Object, Integer>();
        PageCacheDelegateHolder pageCacheDelegateHolder = new PageCacheDelegateHolder(this);
        pageCache.setArgumentsToLoadPredictor(pageCacheDelegateHolder);
        pageCache.setGuessGenerator(pageCacheDelegateHolder);
        pageCache.setObjectLoader(pageCacheDelegateHolder);
        pageCache.setParametrizer(pageCacheDelegateHolder);
        textFragmentCache = new AlwaysRememberCache<Integer, List<PdfTextFragment>, Object, Object>();
        TextFragmentsCacheDelegateHolder textFragmentsCacheDelegateHolder = new TextFragmentsCacheDelegateHolder(this);
        textFragmentCache.setObjectLoader(textFragmentsCacheDelegateHolder);
        textFragmentCache.setItemUnloader(textFragmentsCacheDelegateHolder);
        annotationCache = new AlwaysRememberCache<Integer, List<APdfAnnotation>, Object, Object>();
        AnnotationCacheDelegateHolder annotationCacheDelegateHolder = new AnnotationCacheDelegateHolder(this);
        annotationCache.setObjectLoader(annotationCacheDelegateHolder);
        annotationCache.setItemAdder(annotationCacheDelegateHolder);
        annotationCache.setItemRemover(annotationCacheDelegateHolder);
    }

    private class PageCacheDelegateHolder
            implements IGenericCache.IArgumentsToLoadPredictor<Integer>, IGenericCache.IGuessGenerator<Integer, Rectangle.Double>,
            IGenericCache.IObjectLoader<Integer, Rectangle.Double>, IGenericCache.IParametrizer<Rectangle.Double, Integer>
    {
        public PageCacheDelegateHolder(PdfDocumentManagerMultithreaded manager)
        {
            this.manager = manager;
        }

        private PdfDocumentManagerMultithreaded manager;

        @Override
        public void firstLoadParametrize(Rectangle.Double loadedObject, Integer rotation)
        {
            if (rotation % 180 != 0)
            {
                double temp = loadedObject.width;
                loadedObject.width = loadedObject.height;
                loadedObject.height = temp;
            }
        }

        @Override
        public void changeParametrizationOfLoadedObject(Rectangle.Double loadedObject, Integer oldRotation, Integer newRotation)
        {
            if ((newRotation - oldRotation) % 180 != 0)
            {
                double temp = loadedObject.width;
                loadedObject.width = loadedObject.height;
                loadedObject.height = temp;
            }
        }

        @Override
        public Rectangle.Double loadObject(Integer page) throws PdfViewerException
        {
            return manager.getPageRangeFromSource(page);
        }

        @Override
        public Rectangle.Double generateGuess(Map<Integer, Rectangle.Double> realValues)
        {
            if (guess != null)
                return (Rectangle.Double) guess.clone();
            Map<Double, Map<Double, Integer>> rectCount = new HashMap<Double, Map<Double, Integer>>();

            double[] max = null;
            for (Rectangle.Double rect : realValues.values())
            {
                Map<Double, Integer> m;
                if (rectCount.containsKey(rect.width))
                {
                    m = rectCount.get(rect.width);
                    if (m.containsKey(rect.height))
                        m.put(rect.height, m.get(rect.height) + 1);
                    // m.replace(rect.height, m.get(rect.height) + 1);
                    else
                        m.put(rect.height, 1);
                } else
                {
                    m = new HashMap<Double, Integer>();
                    rectCount.put(rect.width, m);
                    m.put(rect.height, 1);
                }

                if (max == null || rectCount.get(max[0]).get(max[1]) < m.get(rect.height))
                {
                    max = new double[] { rect.width, rect.height };
                }
            }
            guess = new Rectangle.Double(0.0, 0.0, max[0], max[1]);
            return (Rectangle.Double) guess.clone();
        }

        @Override
        public void invalidateGuess()
        {
            guess = null;
        }

        Rectangle.Double guess;

        @Override
        public List<Integer> predictObjectsToLoad(Integer pageToLoad) throws PdfViewerException
        {
            List<Integer> list = new ArrayList<Integer>();
            for (int i = Math.max(1, pageToLoad - 2); i <= Math.min(manager.getPageCount(), pageToLoad + 3); i++)
                list.add(i);
            return list;
        }
    }

    private class TextFragmentsCacheDelegateHolder
            implements IGenericCache.IObjectLoader<Integer, List<PdfTextFragment>>, IGenericCache.IItemUnloader<List<PdfTextFragment>>
    {
        public TextFragmentsCacheDelegateHolder(PdfDocumentManagerMultithreaded manager)
        {
            this.manager = manager;
        }

        PdfDocumentManagerMultithreaded manager;

        @Override
        public List<PdfTextFragment> loadObject(Integer page) throws PdfViewerException
        {
            return manager.getTextFragmentsFromSource(page);
        }

        @Override
        public void unloadItem(List<PdfTextFragment> objectToUnload)
        {
            for (PdfTextFragment frag : objectToUnload)
                frag.finalize();
        }
    }

    /**
     * Cache for the annotations. Works without prediction and loads one page at
     * a time (can be, and maybe should be changed)
     * 
     * @author pgl
     *
     */

    private class AnnotationCacheDelegateHolder implements IGenericCache.IObjectLoader<Integer, List<APdfAnnotation>>,
            IGenericCache.IItemAdder<Object, Integer, Object>, IGenericCache.IItemRemover<Object, Integer, Object>
    {
        public AnnotationCacheDelegateHolder(PdfDocumentManagerMultithreaded manager)
        {
            this.manager = manager;
        }

        PdfDocumentManagerMultithreaded manager;

        @Override
        public List<APdfAnnotation> loadObject(Integer page) throws PdfViewerException
        {
            PdfGenericAnnotation[] annotArray = manager.getAnnotationsOnPageFromSource(page).waitForCompletion();
            List<APdfAnnotation> annotList = new ArrayList<APdfAnnotation>();
            if (annotArray != null)
            {
                for (PdfGenericAnnotation annot : annotArray)
                {
                    annotList.add(specifyAnnotation(annot));
                }
            }

            return annotList;
        }

        /**
         * Create specific annotations based on the type of the
         * GenericAnnotation
         * 
         * @param annot:
         *            a generic annotation
         * @return an annotation depending on the type
         */
        private APdfAnnotation specifyAnnotation(PdfGenericAnnotation annot)
        {
            TPdfAnnotationType type = annot.getType();
            if (type == null)
                type = TPdfAnnotationType.eDefaultAnnotation;

            switch (type)
            {
            case eAnnotationLink:
                PdfLinkAnnotation lAnnot = new PdfLinkAnnotation(annot.getDict(), m_controller);
                return lAnnot;
            case eAnnotationText:
                PdfTextAnnotation tAnnot = new PdfTextAnnotation(annot.getDict(), m_controller);
                return tAnnot;
            case eAnnotationHighlight:
                PdfHighlightAnnotation hAnnot = new PdfHighlightAnnotation(annot.getDict(), m_controller);
                return hAnnot;
            case eAnnotationInk:
            case eAnnotationSquiggly:
            case eAnnotationSquare:
            case eAnnotationStamp:
                PdfGenericMarkupAnnotation gAnnot = new PdfGenericMarkupAnnotation(annot.getDict(), m_controller);
                return gAnnot;
            case eAnnotationWidget:
                PdfWidgetAnnotation wAnnot = new PdfWidgetAnnotation(annot.getDict(), m_controller);
                return wAnnot;
            default:
                PdfTempAnnotation tempAnnot = new PdfTempAnnotation(annot.getDict(), m_controller);
                tempAnnot.setType(type);
                return tempAnnot;
            }

        }

        @Override
        public void add(Object obj, Integer page, Object item)
        {
            @SuppressWarnings("unchecked")
            Map<Integer, List<APdfAnnotation>> dict = (Map<Integer, List<APdfAnnotation>>) obj;
            List<APdfAnnotation> annotList = dict.get(page);
            if (annotList == null)
                annotList = new ArrayList<APdfAnnotation>();
            annotList.add(specifyAnnotation((PdfGenericAnnotation) item));
        }

        @Override
        public void remove(Object obj, Integer page, Object item)
        {
            @SuppressWarnings("unchecked")
            Map<Integer, List<APdfAnnotation>> dict = (Map<Integer, List<APdfAnnotation>>) obj;
            List<APdfAnnotation> annotList = dict.get(page);
            for (APdfAnnotation annot : annotList)
            {
                if (annot.getHandle() == (Long) item)
                {
                    annotList.remove(annot);
                    break;
                }
            }

        }

    }

    /**
     * @author fwe Comparator, which compares the priority of requests for
     *         priority queue
     */
    private class PdfRequestComparator implements Comparator<IPdfRequest>
    {
        @Override
        public int compare(IPdfRequest req1, IPdfRequest req2)
        {
            return -(Integer.valueOf(req1.getPriority()).compareTo(Integer.valueOf(req2.getPriority()))); // minus
                                                                                                          // to
                                                                                                          // have
                                                                                                          // highest
                                                                                                          // number
                                                                                                          // as
                                                                                                          // highest
                                                                                                          // priority
        }
    }

    @Override
    public synchronized PdfDrawRequest draw(Dimension bitmapSize, int rotation, Map<Integer, Rectangle.Double> pageRectsDict,
            PdfViewerController.Viewport viewport)
    {
        if (lastDrawRequest != null)
        {
            // We have a new drawRequest, so we do not need to execute the
            // previous one
            queue.remove(lastDrawRequest);
        }
        lastDrawRequest = new PdfDrawRequest(bitmapSize, rotation, pageRectsDict, viewport);
        queue.offer(lastDrawRequest);
        return lastDrawRequest;
    }

    @Override
    public synchronized PdfOpenRequest open(String filename, String password)
    {
        queue.clear();
        pageCache.invalidateCache();
        pageCache.changeParametrization(0);
        textFragmentCache.invalidateCache();
        PdfOpenRequest request = new PdfOpenRequest(filename, password);
        queue.offer(request);
        try
        {
            request.waitForCompletion();
        } catch (PdfViewerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return request;

    }

    @Override
    public synchronized PdfSaveRequest saveAs(String path) throws PdfViewerException
    {
        PdfSaveRequest request = new PdfSaveRequest(path);
        queue.offer(request);
        return request;
        // request.waitForCompletion();
    }

    @Override
    public synchronized void close()
    {
        pageCache.invalidateCache();
        pageCache.changeParametrization(0);
        try
        {
            m_controller.setRotation(0);
        } catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (PdfViewerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        textFragmentCache.invalidateCache();
        annotationCache.invalidateCache();
        PdfCloseRequest request = new PdfCloseRequest();
        queue.offer(request);
        try
        {
            request.waitForCompletion();
        } catch (PdfViewerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public PdfGetPageLayoutRequest requestPageLayoutMode()
    {
        PdfGetPageLayoutRequest request = new PdfGetPageLayoutRequest();
        queue.offer(request);
        return request;
    }

    @Override
    public PdfGetOpenActionDestinationRequest requestOpenActionDestination()
    {
        PdfGetOpenActionDestinationRequest request = new PdfGetOpenActionDestinationRequest();
        queue.offer(request);
        return request;
    }

    @Override
    public synchronized PdfGetOutlinesRequest requestOutlines(int parentId)
    {
        PdfGetOutlinesRequest request = new PdfGetOutlinesRequest(parentId);
        queue.offer(request);
        return request;
    }

    public synchronized PdfGetThumbnailRequest requestThumbnail(double sourceWidth, double sourceHeight, int targetWidth, int targetHeight,
            int page)
    {
        PdfGetThumbnailRequest request = new PdfGetThumbnailRequest(sourceWidth, sourceHeight, targetWidth, targetHeight, page);
        queue.offer(request);
        return request;
    }

    @Override
    public int getPageCount() throws NoFileOpenedException
    {
        return document.getPageCount();
    }

    public void setRotation(int rotation)
    {
        pageCache.changeParametrization(rotation);
    }

    public Rectangle.Double getPage(int page) throws PdfViewerException
    {
        return pageCache.get(page);
    }

    public Rectangle.Double getPageGuess(int page) throws PdfViewerException
    {
        return pageCache.getGuess(page);
    }

    public boolean isOpen()
    {
        return document.isOpen();
    }
    
    public void registerPagesLoadedToCacheListener(IGenericCache.IItemLoadedToCacheListener<Integer> listener)
    {
        pageCache.addItemLoadedToCacheListener(listener);
    }

    private synchronized Rectangle.Double getPageRangeFromSource(Integer page) throws PdfViewerException
    {
        PdfPageRangeRequest request = new PdfPageRangeRequest(Collections.singletonList(page), true);
        queue.offer(request);
        return request.waitForCompletion().get(page);
    }

    private synchronized List<PdfTextFragment> getTextFragmentsFromSource(Integer page) throws PdfViewerException
    {
        PdfGetTextFragmentsRequest request = new PdfGetTextFragmentsRequest(page);
        queue.offer(request);
        return request.waitForCompletion();
    }

    public List<PdfTextFragment> getTextFragments(int page) throws PdfViewerException
    {
        return textFragmentCache.get(page);
    }

    /**
     * Get annotations from document page
     * 
     * @param page
     * @return
     * @throws PdfViewerException
     */
    private synchronized PdfGetAnnotationsRequest getAnnotationsOnPageFromSource(int page) throws PdfViewerException
    {
        PdfGetAnnotationsRequest request = new PdfGetAnnotationsRequest(page);
        queue.offer(request);
        return request;
    }

    /**
     * Get annotations from cache
     * 
     * @param page:
     *            key for the annotation cache
     * @return array of annotations for a given page
     * @throws PdfViewerException
     */
    public List<APdfAnnotation> getAnnotationsOnPage(int page) throws PdfViewerException
    {
        return annotationCache.get(page);
    }

    /**
     * Update annotation
     * 
     * @param annotation:
     *            annotation to be updated
     * @return page: new page of the annotation
     * @throws PdfViewerException
     */
    public synchronized PdfUpdateAnnotationRequest updateAnnotation(APdfAnnotation annotation) throws PdfViewerException
    {
        PdfUpdateAnnotationRequest request = new PdfUpdateAnnotationRequest(annotation);
        queue.offer(request);
        return request;
    }

    /**
     * Add created annotation to annotationCache
     * 
     * @param page:
     *            page which the annotation belongs to
     * @param annotation:
     *            annotation
     * @throws PdfViewerException
     */

    public synchronized void createAnnotation(TPdfAnnotationType type, int page, Double[] rect) throws PdfViewerException
    {
        PdfCreateAnnotationRequest request = new PdfCreateAnnotationRequest(type, page, rect);
        queue.offer(request);
        PdfGenericAnnotation annot = request.waitForCompletion();
        annotationCache.addItemToCache(page, annot);
    }

    public synchronized void deleteAnnotation(int page, long annotHandle) throws PdfViewerException
    {
        PdfDeleteAnnotationRequest request = new PdfDeleteAnnotationRequest(annotHandle, page);
        queue.offer(request);
        request.waitForCompletion();
        annotationCache.removeItemFromCache(page, annotHandle);
    }

    private Thread worker;
    private PriorityBlockingQueue<IPdfRequest> queue;
    private IPdfDocument document;

    private PdfDrawRequest lastDrawRequest;
    private IGenericCache<Integer, Rectangle.Double, Object, Integer> pageCache;
    private IGenericCache<Integer, List<PdfTextFragment>, Object, Object> textFragmentCache;
    private IGenericCache<Integer, List<APdfAnnotation>, Object, Object> annotationCache;
    private PdfViewerController m_controller;

}
