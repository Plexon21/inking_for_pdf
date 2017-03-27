package com.pdf_tools.pdfviewer.SwingAPI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import com.pdf_tools.pdfviewer.Model.IPdfViewerController;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;
import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.*;

public class PdfThumbnailView extends JComponent implements IOnCloseCompletedListener, IOnOpenCompletedListener,
        IOnVisiblePageRangeChangedListener, IOnAnnotationUpdatedListener, IOnAnnotationCreatedListener, 
        IOnAnnotationDeletedListener, IOnThumbnailLoadedListener, ChangeListener
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PdfThumbnailView()
    {
        setLayout(new BorderLayout());
        thumbnailList = new Vector<ThumbnailItem>();
        pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.setBackground(backgroundColor);
        scrollPane = new JScrollPane(pane);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.getViewport().addChangeListener(this);
        add(scrollPane);
        this.setMinimumSize(
                new Dimension(thumbnailImageWidth + 2 * padding + scrollPane.getVerticalScrollBar().getPreferredSize().width + 5, 1));
    }

    public void setController(IPdfViewerController controller)
    {
        this.controller = controller;
        controller.registerOnCloseCompleted(this);
        controller.registerOnOpenCompleted(this);
        controller.registerOnThumbnailLoaded(this);
        controller.registerOnVisiblePageRangeChanged(this);
        controller.registerOnAnnotationUpdated(this);
        controller.registerOnAnnotationCreated(this);
        controller.registerOnAnnotationDeleted(this);
        controller.setThumbnailHeight(thumbnailImageHeight);
        controller.setThumbnailWidth(thumbnailImageWidth);
    }

    @Override
    public void onThumbnailLoaded(int pageNo, BufferedImage bitmap, PdfViewerException ex)
    {
        if (ex != null)
            return;

        thumbnailList.get(pageNo - 1).thumbnailLoaded(bitmap);
    }

    @Override
    public void onOpenCompleted(PdfViewerException ex)
    {
        if (ex != null)
            return; // then there is nothing to do here
        int pageCount = 0;
        try
        {
            pageCount = controller.getPageCount();
        } catch (PdfViewerException e)
        {
            return;
        }
        for (int i = 1; i <= pageCount; i++)
        {
            ThumbnailItem item = new ThumbnailItem(this, i);
            item.setLocation(0, (thumbnailImageHeight + thumbnailTextHeight + 2 * padding) * (i - 1));
            thumbnailList.addElement(item);
            // For some reason, the whole layout messes up if this isnt here
            pane.add(Box.createRigidArea(new Dimension(this.getWidth(), 1)));
            pane.add(item);
        }
        highlightIndex = controller.getPageNo() - 1;
        thumbnailList.get(highlightIndex).setHighlight(true);
        recalcLayout();
        this.stateChanged(null);
    }

    @Override
    public void onCloseCompleted(PdfViewerException ex)
    {
        // remove all thumbnailitems
        thumbnailList.clear();
        pane.removeAll();
        scrollPane.repaint();
    }

    @Override
    public void onVisiblePageRangeChanged(int firstPage, int lastPage)
    {
        if (thumbnailList.size() >= firstPage)
        {
            // change highlighting to the currently selected page
            if (highlightFrameIndex < thumbnailList.size())
                thumbnailList.get(highlightFrameIndex).setHighlightFrame(false);
            highlightFrameIndex = firstPage - 1;
            thumbnailList.get(highlightFrameIndex).setHighlightFrame(true);

            // scroll the page into view
            Rectangle r = new Rectangle(thumbnailList.get(firstPage - 1).getBounds());
            JViewport v = scrollPane.getViewport();
            r.translate(-v.getViewPosition().x, -v.getViewPosition().y);
            v.scrollRectToVisible(r);
            scrollPane.repaint();
        }
    }
    
    @Override
    public void onAnnotationDeleted(int page)
    {
        redrawThumbnail(page);
    }
    
    @Override
    public void onAnnotationUpdated(APdfAnnotation annotation)
    {
        if (annotation != null)
            redrawThumbnail(annotation.getPage());
    }
    
    @Override
    public void onAnnotationCreated(int page)
    {
        redrawThumbnail(page);
    }
    
    private void redrawThumbnail(int page)
    {
        for (ThumbnailItem item : thumbnailList)
        {
            if (item.getPageNo() == page)
            {
                item.bitmap = null;
                item.loadThumbnail();
                
            }
        }
    }

    /**
     * update the highlight properties for the previously highlighted thumbnail
     * and the new one. Do it for both the highlight frame and the highlight
     * background
     * 
     * @param pageNo
     */
    private void updateHighlights(int pageNo)
    {
        if (thumbnailList.size() >= pageNo)
        {
            // change highlightFrame to the currently selected page
            if (highlightFrameIndex < thumbnailList.size())
                thumbnailList.get(highlightFrameIndex).setHighlightFrame(false);
            highlightFrameIndex = pageNo - 1;
            thumbnailList.get(highlightFrameIndex).setHighlightFrame(true);

            // change highlight to the currently selected page
            if (highlightIndex < thumbnailList.size())
                thumbnailList.get(highlightIndex).setHighlight(false);
            highlightIndex = pageNo - 1;
            thumbnailList.get(highlightIndex).setHighlight(true);
        }

    }

    private void recalcLayout()
    {
        int height = 0;
        for (ThumbnailItem item : thumbnailList)
        {
            height += item.getHeight() + 1;
        }
        pane.setPreferredSize(new Dimension(thumbnailImageWidth + 2 * padding, height));
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        for (ThumbnailItem item : thumbnailList)
        {
            item.loadThumbnail();
        }
    }

    private JScrollPane scrollPane;
    private JPanel pane;
    private Vector<ThumbnailItem> thumbnailList;
    private IPdfViewerController controller;
    private int highlightIndex = Integer.MAX_VALUE;
    private int highlightFrameIndex = Integer.MAX_VALUE;

    private class ThumbnailItem extends JComponent implements MouseListener, IOnRotationChangedListener
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public ThumbnailItem(PdfThumbnailView viewer, int pageNo)
        {
            this.thumbnailViewer = viewer;
            this.pageNo = pageNo;
            this.text = Integer.toString(pageNo);
            this.addMouseListener(this);
            /*
             * this.setMinimumSize(new Dimension(thumbnailImageWidth,
             * thumbnailImageHeight + thumbnailTextHeight));
             */
            this.setMaximumSize(new Dimension(thumbnailImageWidth + 2 * padding, thumbnailImageHeight + thumbnailTextHeight + 2 * padding));
            this.setSize(thumbnailImageWidth + 2 * padding, thumbnailImageHeight + thumbnailTextHeight + 2 * padding);
            // this.setPreferredSize(new Dimension(thumbnailImageWidth,
            // thumbnailImageHeight + thumbnailTextHeight));
        }

        public void thumbnailLoaded(BufferedImage bitmap)
        {
            this.bitmap = bitmap;
            calculateTransform();

            recalcLayout();
            this.repaint();
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            // draw highlight
            Graphics2D g2d = (Graphics2D) g;
            if (highlight)
            {
                g.setColor(highlightColor);
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
            }
            // draw highlight frame
            if (highlightFrame)
            {
                g.setColor(highlightFrameColor);
                g2d.setStroke(dashedStroke);
                g2d.drawRect(0, 0, this.getWidth(), this.getHeight());
            }

            // draw thumbnail
            if (bitmap != null)
            {
                //
                // g.drawImage(bitmap, x, padding, bitmap.getWidth(),
                // bitmap.getHeight(), Color.DARK_GRAY, null);
                ((Graphics2D) g).drawImage(bitmap, transform, null);
                g2d.setStroke(basicStroke);
                g.setColor(Color.BLACK);
                int bitmapWidth = rotation % 180 == 0 ? bitmap.getWidth() : bitmap.getHeight();
                int bitmapHeight = rotation % 180 == 0 ? bitmap.getHeight() : bitmap.getWidth();
                int x = (thumbnailImageWidth - bitmapWidth) / 2 + padding;
                g.drawRect(x, padding, bitmapWidth - 1, bitmapHeight - 1);
                FontMetrics fm = g.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(text, g);
                x = (thumbnailImageWidth - (int) r.getWidth()) / 2 + padding;
                int y = (thumbnailTextHeight + fm.getAscent()) / 2 + bitmapHeight + padding;
                g.drawString(Integer.toString(pageNo), x, y);
            } else
            {
                g.setColor(Color.WHITE);
                g.fillRect(padding, padding, thumbnailImageWidth, thumbnailImageHeight);
                g.setColor(Color.BLACK);
                g.drawRect(padding, padding, thumbnailImageWidth - 1, thumbnailImageHeight - 1);
                FontMetrics fm = g.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(text, g);
                int x = (thumbnailImageWidth - (int) r.getWidth()) / 2 + padding;
                int y = (thumbnailTextHeight + fm.getAscent()) / 2 + thumbnailImageHeight + padding;
                g.drawString(Integer.toString(pageNo), x, y);
            }
        }

        /**
         * Invoked if a thumbnailItem is clicked. Sets page to the thumbnails
         * page and updates the highlight properties of the thumbnail
         * 
         * @param e
         */
        @Override
        public void mouseClicked(MouseEvent e)
        {
            try
            {
                controller.setPageNo(pageNo);
                thumbnailViewer.updateHighlights(pageNo);
            } catch (IllegalArgumentException e1)
            {
            } catch (PdfViewerException e1)
            {
            }
        }

        private void calculateTransform()
        {
            transform = new AffineTransform();
            transform.translate((thumbnailImageWidth) / 2 + padding,
                    padding + (rotation % 180 == 0 ? bitmap.getHeight() / 2 : bitmap.getWidth() / 2));
            transform.rotate((double) rotation / 180.0 * Math.PI);
            transform.translate(-bitmap.getWidth() / 2, -bitmap.getHeight() / 2);

            int bitmapHeight = rotation % 180 == 0 ? bitmap.getHeight() : bitmap.getWidth();
            this.setMaximumSize(new Dimension(thumbnailImageWidth + 2 * padding, bitmapHeight + thumbnailTextHeight + 2 * padding));
            this.setSize(thumbnailImageWidth + 2 * padding, bitmapHeight + thumbnailTextHeight + 2 * padding);

            // this.setMinimumSize(new Dimension(thumbnailImageWidth,
            // bitmap.getHeight() + thumbnailTextHeight));
            // this.setPreferredSize(new Dimension(thumbnailImageWidth,
            // bitmap.getHeight() + thumbnailTextHeight));
        }

        public void loadThumbnail()
        {
            if (bitmap == null)
            {
                Rectangle r = this.getBounds();
                Rectangle v = scrollPane.getViewport().getViewRect();
                if (v.intersects(r))
                {
                    try
                    {
                        controller.loadThumbnail(pageNo, true);
                    } catch (PdfViewerException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void setHighlight(boolean highlight)
        {
            if (this.highlight != highlight)
            {
                this.highlight = highlight;
                this.repaint();
            }
        }

        public void setHighlightFrame(boolean highlightFrame)
        {
            if (this.highlightFrame != highlightFrame)
            {
                this.highlightFrame = highlightFrame;
                this.repaint();
            }
        }

        @Override
        public void onRotationChanged(int newRotation)
        {
            rotation = newRotation;
            if (bitmap != null)
            {
                calculateTransform();
                recalcLayout();
                this.repaint();
            }

        }

        @Override
        public void mousePressed(MouseEvent e)
        {
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
        }
        
        public int getPageNo()
        {
            return pageNo;
        }

        private PdfThumbnailView thumbnailViewer;
        private AffineTransform transform;
        private int pageNo;
        private int rotation = 0;
        private BufferedImage bitmap = null;
        private String text;
        private boolean highlight = false;
        private boolean highlightFrame = false;

    }

    private static int thumbnailImageWidth = 100;
    private static int thumbnailImageHeight = 100;
    private static int thumbnailTextHeight = 20;
    private static int padding = 5;
    private static Color backgroundColor = Color.LIGHT_GRAY;
    private static Color highlightFrameColor = Color.BLACK;
    private static Color highlightColor = new Color(0x0F8FDF);
    private static Stroke basicStroke = new BasicStroke();
    private static Stroke dashedStroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 1 }, 0);





}
