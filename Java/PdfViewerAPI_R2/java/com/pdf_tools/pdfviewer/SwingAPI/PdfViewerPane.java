/***************************************************************************
 *
 * File:            PdfViewerPane.java
 * 
 * Package:         3-Heights(TM) PDF Java Viewer
 *
 * Description:     The PDF Viewer pane.
 *
 * @author          Christian Hagedorn, PDF Tools AG   
 * 
 * Copyright:       Copyright (C) 2001 - 2008 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *                  
 ***************************************************************************/

package com.pdf_tools.pdfviewer.SwingAPI;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation.TPdfAnnotationType;
import com.pdf_tools.pdfviewer.Annotations.APdfMarkupAnnotation;
import com.pdf_tools.pdfviewer.Annotations.PdfPopupAnnotation;
import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation;
import com.pdf_tools.pdfviewer.Model.*;
import com.pdf_tools.pdfviewer.Model.IPdfViewerController.PdfTextWithinSelectionResult;
import com.pdf_tools.pdfviewer.Model.PdfViewerException.NoFileOpenedException;
import com.pdf_tools.pdfviewer.converter.Color;
import com.pdf_tools.pdfviewer.converter.Converter;
import com.pdf_tools.pdfviewer.converter.geom.Point;
import com.pdf_tools.pdfviewer.converter.geom.Rectangle;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.*;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager.IInternalOnSearchCompletedListener;

/**
 * The PDF viewer pane where everything is drawn.
 */
public class PdfViewerPane extends JDesktopPane implements IOnRotationChangedListener, IInternalOnSearchCompletedListener, IOnClosingListener,
        IOnMarkupAnnotationHoverListener, IOnAnnotationUpdatedListener, IOnMarkupAnnotationActionListener, IOnOpenCompletedListener,
        IOnAnnotationDeletedListener, IOnDrawCompletedListener, InternalFrameListener, ComponentListener
{
    /**
	 * Creates a new viewer pane with the associated controller reference to
	 * listen to.
	 * 
	 * @param controller
	 *            The associated controller of this pane.
	 */
    public PdfViewerPane(final PdfViewerController controller)
    {
        this.setBackground(java.awt.Color.DARK_GRAY);
	    this.controller = controller;     
        controller.registerOnOpenCompleted(this);
		mouseModeChangedListenerList = new ArrayList<IOnMouseModeChangedListener>();
		MouseHandler mouseHandler = new MouseHandler();
        middleMouseScrollingTimer = new Timer(10, new ActionListener()
        {
			@Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
					controller.scroll((end.x - pressed.x) / 10, (end.y - pressed.y) / 10);
					setCursor(new Cursor(Cursor.MOVE_CURSOR));
                } catch (PdfViewerException ex)
                {
					// cant do anything then
				}
			}
		});

		middleMouseScrollingTimer.stop();
		markedRects = new HashMap<Integer, List<Rectangle.Double>>();
		highlightedRects = new HashMap<Integer, List<Rectangle.Double>>();
		 
        // setLayout(new BorderLayout());
		
		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);
		addMouseWheelListener(mouseHandler);
        addComponentListener(new ComponentListener()
        {
			@Override
            public void componentShown(ComponentEvent e)
            {
			}

			@Override
            public void componentResized(ComponentEvent e)
            {
                try
                {
					PdfViewerPane.this.controller.setSize(getWidth(), getHeight());
                } catch (PdfViewerException e1)
                {
					// Dont do anything then
				}
			}

			@Override
            public void componentMoved(ComponentEvent e)
            {
			}

			@Override
            public void componentHidden(ComponentEvent e)
            {
			}
		});
		
        popupMenu = new JPopupMenu();
		
        menuItemDeleteAnnotation = new JMenuItem("Delete Annotation");
        menuItemDeleteAnnotation.addActionListener(new ActionListener()
        {
		
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (annotationToDelete == null)
                    return;
                try
                {
                    for (PopupFrame frame : popupFrameList)
                    {
                        if (frame.m_annotation == annotationToDelete)
                        {
                            frame.dispose();
                        }
                    }
                    controller.deleteAnnotation(annotationToDelete.getPage(), annotationToDelete.getHandle());
                } catch (PdfViewerException e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                recentHoverAnnotation = null;
            }
        });
        popupMenu.add(menuItemDeleteAnnotation);

        menuItemCreateHighlight = new JMenuItem("Add Highlight Annotation");
        menuItemCreateHighlight.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!markedRects.isEmpty())
                {
                    createHighlightAnnotation();
                    setMouseMode(PdfMouseMode.SELECT);
                } else
                {
                    setMouseMode(PdfMouseMode.HIGHLIGHT);
                }
            }
        });

        popupMenu.add(menuItemCreateHighlight);

        menuItemCreateStickyAnnot = new JMenuItem("Add Sticky Annotation");
        menuItemCreateStickyAnnot.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Point.Double onPage = new Point.Double();
                int page = 1;
                try
                {
                    page = controller.transformOnScreenToOnPage(pressed, onPage);
                } catch (PdfViewerException e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                if (page < 1)
                    return;
                Double[] dRect = { onPage.getX(), onPage.getY(), 0.0, 0.0 };
                try
                {
                    controller.createAnnotation(TPdfAnnotationType.eAnnotationText, page, dRect);
                } catch (PdfViewerException e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }
        });
        popupMenu.add(menuItemCreateStickyAnnot);

        menuItemFreehandAnnot = new JMenuItem("Add Freehand Annotation");
        menuItemFreehandAnnot.addActionListener(new ActionListener()
        {
            
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setMouseMode(PdfMouseMode.FREEHAND);                
            }
        });
        popupMenu.add(menuItemFreehandAnnot);

		menuItemHighlight = new JMenuItem("Highlight");
        menuItemHighlight.addActionListener(new ActionListener()
        {
			@Override
            public void actionPerformed(ActionEvent e)
            {
				moveMarkedRectsToHighlightedRects();
			}
		});
        // TODO: Proper text highlight mechanism
        // popupMenu.add(menuItemHighlight);
		menuItemRemH = new JMenuItem("Remove all Highlights");
        menuItemRemH.addActionListener(new ActionListener()
        {
			@Override
            public void actionPerformed(ActionEvent e)
            {
				removeHighlightedRects();
			}
		});
        // popupMenu.add(menuItemRemH);
		menuItemCopy = new JMenuItem("Copy selected Text");
        menuItemCopy.addActionListener(new ActionListener()
        {
			@Override
            public void actionPerformed(ActionEvent e)
            {
				copySelectedText();
			}
		});
		menuItemCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		popupMenu.add(menuItemCopy);

		annotationHoverPopupMenu = new JPopupMenu();	
        annotationHoverTextArea = new JTextArea(5, 20);
		annotationHoverTextArea.setLineWrap(true);
		annotationHoverTextArea.setWrapStyleWord(true);
		annotationHoverPopupMenu.add(annotationHoverTextArea);
		
		annotMoveRect = new AnnotationRectangle();
		
		popupFrameList = new ArrayList<PdfViewerPane.PopupFrame>();
        
        vertexList = new ArrayList<Double>();
        pageVertexList = new ArrayList<Integer>();
		addComponentListener(this);
		
		controller.registerOnRotationChanged(this);
		controller.registerOnClosing(this);
		controller.registerOnAnnotationHover(this);
		controller.registerOnAnnotationAction(this);
        controller.registerOnAnnotationDeleted(this);
        controller.registerOnAnnotationUpdated(this);
        controller.registerInternalOnSearchCompleted(this);
        controller.registerOnDrawCompleted(this);
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK),
                "CopyHighlightedText");
		this.getActionMap().put("CopyHighlightedText", new CopyAction());

	}
	
    @Override
    public void componentHidden(ComponentEvent e)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void componentMoved(ComponentEvent e)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void componentResized(ComponentEvent e)
    {
        for (PopupFrame popupFrame : popupFrameList)
        {
            Point.Integer pos = Converter.createPointI(popupFrame.getLocation());
            int width = popupFrame.getWidth();
            int height = popupFrame.getHeight();
            if (pos.x < 0)
                popupFrame.setLocation(0, pos.y);
            if (pos.x + width > this.getWidth())
                popupFrame.setLocation(this.getWidth() - width, pos.y);
            
            if (pos.y < 0)
                popupFrame.setLocation(pos.x, 0);
            if (pos.y + height > this.getHeight())
                popupFrame.setLocation(pos.x, this.getHeight() - height);
            
        }
    }

    @Override
    public void componentShown(ComponentEvent e)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent arg0)
    {
        return;
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent arg0)
    {
        return;
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent arg0)
    {
        PopupFrame frame = (PopupFrame) arg0.getInternalFrame();
        frame.setOpen(false);
        popupFrameList.remove(frame);
        return;
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent arg0)
    {
        grabFocus();
    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent arg0)
    {
        return;
    }

    @Override
    public void internalFrameIconified(InternalFrameEvent arg0)
    {
        return;
    }

    @Override
    public void internalFrameOpened(InternalFrameEvent arg0)
    {
        return;
    }
    
    private class AnnotationRectangle
    {
        APdfMarkupAnnotation m_annotation;
        Rectangle.Integer m_rect;
        Rectangle.Double m_pageRect;
        Rectangle.Integer initialPosition;
        int dx, dy;
        private boolean movingAnnotation = false;
        
        public void initialize(APdfMarkupAnnotation annotation, MouseEvent e)
        {
            m_annotation = annotation;
            try
            {
                m_pageRect = controller.getPageRect(m_annotation.getPage());
            } catch (PdfViewerException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            try
            {
                m_rect = controller.transformRectPageToViewport(m_annotation.getRect(), m_annotation.getPage());
                initialPosition = (Rectangle.Integer) m_rect.clone();
                dx = m_rect.x - e.getX();
                dy = m_rect.y - e.getY();
            } catch (PdfViewerException ex)
            {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }
        }
        
        public Rectangle.Integer getRect()
        {
            return m_rect;
        }
        
        public void setLocation(Point.Integer p)
        {
            m_rect.x = dx + p.x;
            m_rect.y = dy + p.y;
        }

        public boolean isMovingAnnotation()
        {
            return movingAnnotation;
        }

        public void setMovingAnnotation(boolean movingAnnotation)
        {
            this.movingAnnotation = movingAnnotation;
        }

        public Point.Integer getLocation()
        {
            return new Point.Integer(m_rect.x, m_rect.y + m_rect.height - 1);
        }

        public void reset()
        {
            m_rect.x = initialPosition.x;
            m_rect.y = initialPosition.y;
        }
    }
    
    private class PopupFrame extends JInternalFrame implements ActionListener, ComponentListener
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        
        private PdfPopupAnnotation m_popupAnnotation;
        private APdfMarkupAnnotation m_annotation;
        private JTextArea internalFrameText;
        private int x, y;
        
        public PopupFrame(APdfMarkupAnnotation annotation)
        {
            super("Annotation", true, true, false);
            m_annotation = annotation;
            m_popupAnnotation = annotation.getPopup();
            
            JPanel internalPanel = new JPanel();
            Border padding = BorderFactory.createEmptyBorder(5, 5, 5, 5);
            internalPanel.setBorder(padding);
            internalPanel.setLayout(new BoxLayout(internalPanel, BoxLayout.Y_AXIS));
            
            JButton internalButton = new JButton("OK");
            internalButton.addActionListener(this);
            Rectangle.Integer popupRect = null;
            try
            {
                popupRect = controller.transformRectPageToViewport(m_popupAnnotation.getRect(), annotation.getPage());
            } catch (PdfViewerException e1)
            {
                e1.printStackTrace();
            }
            if (popupRect != null)
            {
                int width = PdfUtils.canvasToPixel(popupRect.getWidth(), 1 / controller.getZoom());
                int height = PdfUtils.canvasToPixel(popupRect.getHeight(), 1 / controller.getZoom());
                setPreferredSize(new Dimension(width, height));
                if (popupRect.x < 0)
                    popupRect.x = 0;
                if (popupRect.x + width > PdfViewerPane.this.getWidth())
                {
                    popupRect.x = PdfViewerPane.this.getWidth() - width;
                }
                x = popupRect.x;
                y = popupRect.y;
                setLocation(x, y);
            } else
            {
                setLocation(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
            }
            internalFrameText = new JTextArea(5, 20);
            internalFrameText.setLineWrap(true);
            internalFrameText.setWrapStyleWord(true);
            internalFrameText.setEditable(true);
            internalFrameText.setMargin(new Insets(1, 3, 1, 3));
            JScrollPane scrollbar = new JScrollPane(internalFrameText);
            internalPanel.add(scrollbar);
            internalPanel.add(internalButton);
            add(internalPanel);
            addComponentListener(this);
            internalFrameText.setText(annotation.getContent());
            internalFrameText.setCaretPosition(0);
            internalPanel.setBackground(Converter.createAWTColor(annotation.getColor()));
            setVisible(true);
            setOpen(true);
            
            pack();
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }
        
        private void setOpen(boolean isOpen)
        {
            m_popupAnnotation.setOpen(isOpen);
        }
       
        public void setText(String content)
        {
            internalFrameText.setText(content);
        }

        public APdfMarkupAnnotation getAnnotation()
        {
            return m_annotation;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            m_annotation.setContent(internalFrameText.getText());

            try
            {
                controller.udpateAnnotation(m_annotation);
            } catch (PdfViewerException e1)
            {
                e1.printStackTrace();
            }
        }

        @Override
        public void componentHidden(ComponentEvent e)
        {
            setOpen(false);
        }

        @Override
        public void componentMoved(ComponentEvent e)
        {
            // if (this.getLocation().x < 0)
            // this.setLocation(0, this.getLocation().y);
//            
            // if (this.getLocation().y < 0)
            // this.setLocation(this.getLocation().x, 0);
//            
            // if (this.getLocation().x + this.getWidth() >
            // PdfViewerPane.this.getWidth())
            // this.setLocation(PdfViewerPane.this.getWidth()-this.getWidth(),
            // this.getLocation().y);
//            
            // PdfViewerPane.this.repaint();
            
            // TODO: update Rectangle.Integer position correctly after
            // moving the frame.
            // Point.Double onPage = new Point.Double();
            // Point.Integer onScreen = getLocation();
            // onScreen.y += this.getHeight();
            // try
            // {
            // controller.transformOnScreenToOnPage(onScreen, onPage);
            // } catch (PdfViewerException e1)
            // {
            // e1.printStackTrace();
            // }
            // if (onPage != null)
            // {
            // m_popupAnnotation.setRectPosition(onPage.x, onPage.y);
            // }
            return;
            
        }

        @Override
        public void componentResized(ComponentEvent e)
        {
            return;
        }

        @Override
        public void componentShown(ComponentEvent e)
        {
            return;
        }

    }

    @Override
    public void onOpenCompleted(PdfViewerException ex)
    {
        getAnnotationsOnPage(controller.getPageNo(), false);
    }
	
    private class CopyAction extends AbstractAction
    {
		private static final long serialVersionUID = -4919800086069035946L;

		@Override
        public void actionPerformed(ActionEvent e)
        {
			copySelectedText();
		}
	}

    private int count = 0;
    
	@Override
    protected void paintComponent(Graphics g)
    {
        if (outBitmap == null) 
        {
            g.setColor(java.awt.Color.DARK_GRAY);
            g.drawRect(0, 0, controller.getViewport().width, controller.getViewport().height);
        } else 
        {
            g.drawImage(outBitmap, 0, 0, controller.getViewport().width, controller.getViewport().height,
                    java.awt.Color.DARK_GRAY, null);
        }
		Rectangle.Integer r;

        for (Entry<Integer, List<Rectangle.Double>> rectsOnPage : markedRects.entrySet())
        {
			if (!controller.getPageLayoutMode().isScrolling()
                    && (rectsOnPage.getKey() < controller.getPageNo() || rectsOnPage.getKey() > controller.getLastPageNo()))
				continue; // The Rectangle.Integer is on a page, that is not shown -> do
							// not render it
            for (Rectangle.Double rectOnUnrotatedPage : rectsOnPage.getValue())
            {
                try
                {
					r = controller.transformRectPageToViewport(rectOnUnrotatedPage, rectsOnPage.getKey());
                } catch (PdfViewerException e)
                {
					e.printStackTrace();
					continue;
				}
				g.setColor(markedStrokeColor);
				g.drawRect(r.x, r.y, r.width, r.height);
				g.setColor(markedFillColor);
				g.fillRect(r.x, r.y, r.width, r.height);
			}
		}
        for (Entry<Integer, List<Rectangle.Double>> rectsOnPage : highlightedRects.entrySet())
        {
			if (!controller.getPageLayoutMode().isScrolling()
                    && (rectsOnPage.getKey() < controller.getPageNo() || rectsOnPage.getKey() > controller.getLastPageNo()))
				continue; // The Rectangle.Integer is on a page, that is not shown -> do
							// not render it
            for (Rectangle.Double rectOnUnrotatedPage : rectsOnPage.getValue())
            {
                try
                {
					r = controller.transformRectPageToViewport(rectOnUnrotatedPage, rectsOnPage.getKey());
                } catch (PdfViewerException e)
                {
					e.printStackTrace();
					continue;
				}
				g.setColor(highlightStrokeColor);
				g.drawRect(r.x, r.y, r.width, r.height);
				g.setColor(highlightFillColor);
				g.fillRect(r.x, r.y, r.width, r.height);
			}
		}
        
        if (pageVertexList != null && pageVertexList.size() > 4)
        {
            for (int i = 0; i<pageVertexList.size()-2; i+=2)
            {
                g.setColor(java.awt.Color.BLACK);
                g.drawLine(pageVertexList.get(i), pageVertexList.get(i+1), pageVertexList.get(i+2), pageVertexList.get(i+3));
            }
        }
		
		if (annotMoveRect.isMovingAnnotation())
		{
    		Graphics2D g2 = (Graphics2D) g;
    		g2.setColor(java.awt.Color.BLACK);
    		g2.draw(Converter.createAWTRect(annotMoveRect.getRect()));
		}
		// get mouse position within the viewer pane
		java.awt.Point mousePosition = MouseInfo.getPointerInfo().getLocation();
		SwingUtilities.convertPointFromScreen(mousePosition, this);
		getAnnotationsUnderCursor(Converter.createPointI(mousePosition));
		
        /**
         * For debugging only uncommend if needed
         */
//		  char[] c = String.valueOf(count++).toCharArray();
//        g.setColor(new Color(255, 0, 0, 255));
//        g.drawChars(c, 0, c.length, 200, 200);
//		
//        if (annotations != null)
//        {
//            for (APdfAnnotation annot : annotations)
//            {
//                Graphics2D g2 = (Graphics2D) g;
//                g2.setColor(java.awt.Color.BLACK);
//                try
//                {
//                    if (annot.isTextMarkup())
//                    {
//                        if (annot.getQuadPoints() != null)
//                        {
//                            List<Rectangle.Double> quadRectList = annot.getQuadPoints().getQuadPointRectList();
//                            for (Rectangle.Double quadRect : quadRectList)
//                            {
//                                g2.draw(controller.transformRectPageToViewport(quadRect, annot.getPage()));
//                            }
//                        }
//
//                    } else
//                    {
//                        g2.draw(controller.transformRectPageToViewport(annot.getRect(), annot.getPage()));
//                    }
//
//                } catch (PdfViewerException e)
//                {
//
//                }
//            }
//        }
        /** End of debug part **/
	}

	/**
     * Get the annotations that are located under the mouse cursor For this the
     * annotations for this page have to be loaded. Once loaded check if the
     * mouse cursor position is within the annotation Rectangle.Integer and add it to
     * the list of annotationsUnderCursor if they are indeed under the cursor.
     * 
	 * @param mousePosition
	 */
	
	private void getAnnotationsUnderCursor(Point.Integer mousePosition)
	{
		
        Point.Double mouseOnPage = new Point.Double(0, 0);
        pageOfMouseLocation = -1;
		try
		{
			// get page over which the mouse cursor is located
            pageOfMouseLocation = controller.transformOnScreenToOnPage(mousePosition, mouseOnPage);
		} catch (NoFileOpenedException e)
		{
			// No file opened, nothing to do
			return;
		} catch (PdfViewerException e)
		{
			// Something else went wrong, let's see
			e.printStackTrace();
		}
		
        getAnnotationsOnPage(pageOfMouseLocation, false);
	
		// check if the mouse position is within the Rectangle.Integer of an annotation
		// annotations can be null if cursor is outside page
        if (annotations == null)
            return;

		    annotationsUnderCursor.clear();
			for (APdfAnnotation annot : annotations)
			{
            if (annot.isTextMarkup() || annot.isLink())
				{
                
                if (annot.getQuadPoints() != null)
                {
                    List<Rectangle.Double> qPointsList =  annot.getQuadPoints().getQuadPointRectList();
                    for (Rectangle.Double rect : qPointsList)
                    {
                        if (rect.contains(mouseOnPage))
                        {
					annotationsUnderCursor.add(annot);
                            break;
				}
			}
                } else if (annot.getRect().contains(mouseOnPage))
                {
                    annotationsUnderCursor.add(annot);
		}

            } else if (annot.getRect().contains(mouseOnPage))
            {
                annotationsUnderCursor.add(annot);
	}
        }
    }
	
    private void getAnnotationsOnPage(int page, boolean forced)
	{
        // Trying to reduce the number of times that annotations get loaded
        // (from cache and parsing)
        if (cursorOnPage != page || forced)
        {
            cursorOnPage = page;
            try
            {
                annotations = controller.loadAnnotationsOnPage(page);
            } catch (PdfViewerException e)
            {
                // Something else, let's see
                e.printStackTrace();
            }
        }
	    
	}

	/**
	 * Sets the mouse mode of the viewport to the specified mode and enables its
	 * functionality.
	 * 
	 * @param mouseMode
	 *            The new mouse mode of the viewport
	 */
    public void setMouseMode(PdfMouseMode mouseMode)
    {
        
        if (this.mouseMode == mouseMode)
			return;

		this.mouseMode = mouseMode;
        switch (mouseMode)
        {
        case MOVE:
        {
            currentCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
			break;
		}
        case HIGHLIGHT:
        {
            currentCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
			break;
		}
        case SELECT:
        {
            currentCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
			break;
		}
        case FREEHAND:
            currentCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            break;
        case ZOOM:
        {
            currentCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
			break;
		}
		}
		setCursor(currentCursor);
		fireMouseModeChanged(mouseMode);
	}

    public PdfMouseMode getMouseMode()
    {
		return this.mouseMode;
	}
	
    @Override
    public void onDrawCompleted(BufferedImage bitmap) 
    {
        outBitmap = bitmap;
    }

    @Override
    public void onMarkupAnnotationAction(APdfMarkupAnnotation annotation)
    {
        PdfPopupAnnotation popupAnnotation = annotation.getPopup();
        if (popupAnnotation != null && !popupAnnotation.isOpen())
        {
            PopupFrame popupFrame = new PopupFrame(annotation);
            popupFrame.addInternalFrameListener(this);
            popupFrameList.add(popupFrame);
            
            add(popupFrame);
        }
    }
	
    @Override
    public void onTextAnnotationHover(APdfMarkupAnnotation annotation)
    {
        // if the annotation is different from the last one we displayed we show
        // the hover
        // menu with new content. 
        
        if (recentHoverAnnotation != annotation)
        {
            if (annotation.getType() == TPdfAnnotationType.eAnnotationFreeText)
                return;
            if (annotation.getContent() != null)
            {
                annotationHoverTextArea.setText(annotation.getContent());
                annotationHoverPopupMenu.setLocation(MouseInfo.getPointerInfo().getLocation().x + 30,
                        MouseInfo.getPointerInfo().getLocation().y);
                annotationHoverPopupMenu.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                        BorderFactory.createLineBorder(Converter.createAWTColor(annotation.getColor()), 5)));
                annotationHoverPopupMenu.pack();
                annotationHoverPopupMenu.setVisible(true);
                recentHoverAnnotation = annotation; 
            }

        }
    }

    @Override
    public void onAnnotationUpdated(APdfAnnotation annotation)
    {
        if (annotation == null)
            annotMoveRect.reset();
        else
        {
            for (PopupFrame frame : popupFrameList)
            {
                if (frame.getAnnotation() == annotation)
                {
                    frame.setText(((APdfMarkupAnnotation) annotation).getContent());
                }
            }
        }
    }

    @Override
    public void onAnnotationDeleted(int page)
    {
        // getAnnotationsOnPage(page, true);
    }
	
	@Override
    public void onRotationChanged(int newRoatation)
    {
		markedRects.clear();
	}

	@Override
	public void internalOnSearchCompleted(int page, int index, Map<Integer, List<Rectangle.Double>> rects) 
	{
        if (rects != null)
			markedRects = rects;
		repaint();
	}

    @Override
    public void onClosing()
    {
        cursorOnPage = 0;
        annotationsUnderCursor.clear();
        for (PopupFrame popupFrame : popupFrameList)
        {
            popupFrame.dispose();
        }
        popupFrameList.clear();
        highlightedRects.clear();
        markedRects.clear();

    }

	/**
	 * Private class to handle all mouse events on the viewer pane.
	 * 
	 * @author cha
	 *
	 */
    private class MouseHandler extends MouseAdapter implements MouseWheelListener, MouseMotionListener
    {
        private long timeStamp;
        private int pressedOnPage;
        private Rectangle.Integer pageRectInViewPort;
        private int pressedInViewportX;
        private int pressedInViewportY;
        private boolean dragMode = false;

		@Override
        public void mousePressed(MouseEvent e)
        {
            timeStamp = e.getWhen();
            java.awt.Point p = e.getPoint();
			pressed = Converter.createPointI(e.getPoint());
			assert(p.x == pressed.x && p.y == pressed.y);
			dragMode = true;
            try
            {
                pressedOnPage = controller.transformOnScreenToOnPage(pressed, new Point.Double());
            } catch (PdfViewerException e2)
            {
                // no document open
            }
            try
            {
                controller.setTextSelectionStartPoint(pressed);
            } catch (PdfViewerException ex)
            {
                // well then there is nothing to do?
            }
            if (SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isMiddleMouseButton(e))
            {
                if (mouseMode == PdfMouseMode.SELECT)
                {
				    if (annotationsUnderCursor.size() > 0)
                    {
				        APdfAnnotation stickyAnnot = null;
                        for (int i = annotationsUnderCursor.size() - 1; i >= 0; i--)
                        {
                            if (annotationsUnderCursor.get(i).isMoveable())
                            {
                                stickyAnnot = annotationsUnderCursor.get(i);
                                break;
                            }
                        }

				        if (stickyAnnot != null)
                        {
                            annotationHoverPopupMenu.setVisible(false);
                            annotMoveRect.initialize((APdfMarkupAnnotation) stickyAnnot, e);
                            annotMoveRect.setLocation(Converter.createPointI(e.getPoint()));
                            annotMoveRect.setMovingAnnotation(true);
                            markedRects.clear();
                            repaint();
                        }
                    } else
                    {

                        return;
                    }
				}
                if (!middleMouseScrollingTimer.isRunning() && SwingUtilities.isMiddleMouseButton(e))
                {
					middleMouseScrollingTimer.start();
					end = pressed;
                } else
                {
					middleMouseScrollingTimer.stop();
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
				if (mouseMode == PdfMouseMode.ZOOM)
					paintDescription("Zoom To");

            } else if (SwingUtilities.isRightMouseButton(e))
			{
                pressed = Converter.createPointI(getMousePosition());
                annotationHoverPopupMenu.setVisible(false);
				Point.Double onPage = new Point.Double();
				int page = 0;
                try
                {
					page = controller.transformOnScreenToOnPage(Converter.createPointI(e.getPoint()), onPage);
                } catch (PdfViewerException e1)
                {
                    return; // without the pageRect, we cant detect which
                            // highlighted/marked rects are selected
				}
				boolean pointIsInMarkedRect = false;
                if (markedRects.containsKey(page))
                {
                    for (Rectangle.Double rect : markedRects.get(page))
				{
                        if (rect.contains(onPage))
					{
							pointIsInMarkedRect = true;
							break;
						}
					}
				}
				boolean pointIsInHighlightedRect = false;
                if (highlightedRects.containsKey(page))
				{
                    for (Rectangle.Double rect : highlightedRects.get(page))
					{
                        if (rect.contains(onPage))
						{
							pointIsInHighlightedRect = true;
							break;
						}
                    }
                }
                boolean pointIsInPage = false;
                if (page > 0)
                {
                    pointIsInPage = true;
                }

                boolean pointIsOverAnnot = false;
                if (annotationsUnderCursor.size() > 0)
                {
                    for (int i = annotationsUnderCursor.size() - 1; i >= 0; i--)
                    {
                        APdfAnnotation annot = annotationsUnderCursor.get(i);
                        if (annot.isMarkup())
                        {
                            pointIsOverAnnot = true;
                            annotationToDelete = annot;
                            break;
                        }
                        annotationToDelete = null;
					}
				}

                menuItemCreateStickyAnnot.setEnabled(pointIsInPage);
                menuItemDeleteAnnotation.setEnabled(pointIsOverAnnot);
				menuItemHighlight.setEnabled(pointIsInMarkedRect);
				menuItemRemH.setEnabled(pointIsInHighlightedRect);
				menuItemCopy.setEnabled(pointIsInMarkedRect);
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		@Override
        public void mouseReleased(MouseEvent e)
        {
		    dragMode = false;
			if (!SwingUtilities.isLeftMouseButton(e))
				return;
			end = Converter.createPointI(e.getPoint());
			rectPrev = null;
			// Calculate rectangle
			int x = Math.min(pressed.x, end.x) + controller.getViewportOrigin().x;
			int y = Math.min(pressed.y, end.y) + controller.getViewportOrigin().y;
			int width = Math.abs(pressed.x - end.x);
			int height = Math.abs(pressed.y - end.y);
            switch (mouseMode)
            {
            case ZOOM:
            {
				paintDescription("Zoom To");
				if (width == 0 || height == 0)
					return;
                try
                {
					controller.setZoomRectangle(x, y, width, height);
                } catch (PdfViewerException ex)
                {
					DebugLogger.log(ex.toString());
				}
				break;
			}
            case HIGHLIGHT:
            {
                createHighlightAnnotation();
				repaint();
                setMouseMode(PdfMouseMode.SELECT);
				break;
			}
            case FREEHAND:
            {
                if (vertexList.size() > 2)
                {
                    Double[] inkList = new Double[vertexList.size()];
                    vertexList.toArray(inkList);
                    createInkAnnotation(inkList);
                }
                sampleCount = 0;
                vertexList.clear();
                pageVertexList.clear();
                setMouseMode(PdfMouseMode.SELECT);
                
            }
            case SELECT:
            {
			    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				DebugLogger.log(_selectedText);
	            Point.Double onPage = new Point.Double();
	            int new_page = -1;

	            if (annotMoveRect != null && annotMoveRect.isMovingAnnotation())
	            {
	                try
	                {
	                    new_page = controller.transformOnScreenToOnPage(annotMoveRect.getLocation(), onPage);
	                } catch (PdfViewerException e1)
	                {
	                    e1.printStackTrace();
	                }
	                annotMoveRect.setMovingAnnotation(false);
	                if (new_page > 0 && annotMoveRect.m_annotation.getPage() == new_page)
	                {

                        annotMoveRect.m_annotation.action(0);
                        if (Math.abs(pressed.x - e.getX()) >= 1 || 
                                Math.abs(pressed.y - e.getY()) >= 1)
                        {
                            annotMoveRect.m_annotation.setRectPosition(controller.getRotation(), onPage.x, onPage.y);
	                    try
	                    {
	                        controller.udpateAnnotation(annotMoveRect.m_annotation);
	                    } catch (PdfViewerException e1)
	                    {
	                        e1.printStackTrace();
	                    }
	                }
                    }
	            }

				break;
			}
            default:
            {
				return;
			}
			}
			// we need to calculate the rectangle
		}

		@Override
        public void mouseWheelMoved(MouseWheelEvent e)
        {
		    switch(mouseMode)
		    {
		    case FREEHAND:
		        if (dragMode) break;
		    case SELECT:
		        if (dragMode) markHighlights(e);
	        default:
	            try
	            {
	                int direction = e.getWheelRotation();
	                // Point.Integer Point.Integer = e.getPoint();
	                if (e.isControlDown())
	                {
	                    // Zoom with ctrl & mousewheel
	                    // TODO: zoom in over mouse cursor
	                    if (direction < 0)
	                    {
	                        controller.setZoom(controller.getZoom() * 1.25 * (-direction));
	                    } else
	                    {
	                        controller.setZoom(controller.getZoom() / 1.25 / direction);
	                    }
	                } else if (e.isShiftDown())
	                {   
	                    controller.scroll(PIXEL_WHEEL * direction, 0);
	                } else
	                {
	                    controller.scroll(0, PIXEL_WHEEL * direction);
	                }
	            } catch (PdfViewerException ex)
	            {
	                DebugLogger.log(ex.toString());
	            }
		    }
            
		}

		@Override
        public void mouseDragged(MouseEvent e)
        {
            try
            {
				Point.Integer newPoint = Converter.createPointI(e.getPoint());
				int distanceX = pressed.x - newPoint.x;
				int distanceY = pressed.y - newPoint.y;
                Point.Double onPage = new Point.Double();
                end = Converter.createPointI(e.getPoint());
                switch (mouseMode)
                {
				case ZOOM:

					// Zoom with rectangle
                    rectCur = new Rectangle.Integer(Math.min(pressed.x, end.x), Math.min(pressed.y, end.y),
                            Math.abs(end.x - pressed.x), Math.abs(end.y - pressed.y));

					if (rectPrev == null)
						rectPrev = new Rectangle.Integer(rectCur.x, rectCur.y, 0, 0);

					paintMarkRect(rectPrev != null);
					rectPrev = rectCur;
					break;
                case HIGHLIGHT:
                    markHighlights(e);
                    break;
                case SELECT:
                {
				    if (annotMoveRect.isMovingAnnotation())
				    {
                        onPage = new Point.Double();
                        pressedOnPage = controller.transformOnScreenToOnPage(Converter.createPointI(e.getPoint()), onPage);
                        if (pressedOnPage != annotMoveRect.m_annotation.getPage())
				        {
				            setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        } else
				        {
				            setCursor(new Cursor(Cursor.MOVE_CURSOR));
				        }
				        annotMoveRect.setLocation(Converter.createPointI(e.getPoint()));
				        repaint();
                    } else
				    {
                        markHighlights(e);
				    }
					break;
				}
                case FREEHAND:
                {
                    sampleCount++;
                    // convert mouse Point.Integer to page coordinates                  
                    int page = controller.transformOnScreenToOnPage(Converter.createPointI(e.getPoint()), onPage);
                    if (page < 1)
                        return;
                    if (sampleCount % sampleRate == 0)
                    {
                        pageVertexList.add(e.getPoint().x);
                        pageVertexList.add(e.getPoint().y);
                        vertexList.add(onPage.getX());
                        vertexList.add(onPage.getY());
                        repaint();
                    }
                    break;
                }
                case MOVE:
                {
					pressed = newPoint;
					controller.scroll(distanceX, distanceY);
					break;
				}
				}
            } catch (PdfViewerException ex)
            {
				DebugLogger.log(ex.toString());
			}
		}
		
		private void markHighlights(MouseEvent e)
		{
		    markedRects.clear();
            end = Converter.createPointI(e.getPoint());
            PdfTextWithinSelectionResult textWithinSelectionResult = null;
            try
            {
                textWithinSelectionResult = controller.getTextWithinSelection(null, end);
            } catch (PdfViewerException e1)
            {
            }

            if (textWithinSelectionResult == null || textWithinSelectionResult.fragments == null)
            {
                _selectedText = "";
                return;
            }
            List<PdfTextFragment> frags = textWithinSelectionResult.fragments;
            StringBuilder textBuilder = new StringBuilder();

            // special treatment for first and last element
            PdfTextFragment first = frags.get(0);
            PdfTextFragment last = frags.get(frags.size() - 1);
            int firstIndex = first.getIndexOfClosestGlyph(textWithinSelectionResult.startX);
            int lastIndex = last.getIndexOfClosestGlyph(textWithinSelectionResult.endX);
            if (frags.size() == 1)
            {
                // It might be that start and end Point.Integer are in wrong
                // order, as they are only ordered by textfragment
                // and
                // there is only one in this collection
                if (lastIndex < firstIndex)
                {
                    int tmp = lastIndex;
                    lastIndex = firstIndex;
                    firstIndex = tmp;
                }
                List<Rectangle.Double> rects = new ArrayList<Rectangle.Double>();
                rects.add(first.getRectOnUnrotatedPage(firstIndex, lastIndex));
                markedRects.put(first.getPageNo(), rects);
                textBuilder.append(first.getText().substring(firstIndex, lastIndex)).append(" ");
            } else
            {
                List<Rectangle.Double> rects = new ArrayList<Rectangle.Double>();
                rects.add(first.getRectOnUnrotatedPage(firstIndex, Integer.MAX_VALUE));
                markedRects.put(first.getPageNo(), rects);
                textBuilder.append(first.getText().substring(firstIndex, first.getText().length()))
                        .append(" ");
                if (first.getPageNo() != last.getPageNo())
                    markedRects.put(last.getPageNo(), new ArrayList<Rectangle.Double>());
                markedRects.get(last.getPageNo()).add(last.getRectOnUnrotatedPage(0, lastIndex));

                // remove first and last
                frags.remove(0);
                frags.remove(frags.size() - 1);
                for (PdfTextFragment frag : frags)
                {
                    if (!markedRects.containsKey(frag.getPageNo()))
                        markedRects.put(frag.getPageNo(), new ArrayList<Rectangle.Double>());
                    markedRects.get(frag.getPageNo()).add(frag.getRectOnUnrotatedPage());
                    textBuilder.append(frag.getText()).append(" ");
                }
                textBuilder.append(last.getText().substring(0, lastIndex));
            }
            // make any amount of whitespaces to one whitespace
            _selectedText = textBuilder.toString().replaceAll("\\s+", " ");
            repaint();
		}

        public void mouseMoved(MouseEvent e)
        {
            lastCursor = currentCursor; 
            if (middleMouseScrollingTimer.isRunning())
            {
				end = Converter.createPointI(e.getPoint());
            } else
            {
                Point.Double onPage = new Point.Double();
                try
                {
                    controller.transformOnScreenToOnPage(Converter.createPointI(e.getPoint()), onPage);
                } catch (PdfViewerException e1)
                {
                    // TODO Auto-generated catch block
			}
				getAnnotationsUnderCursor(Converter.createPointI(e.getPoint()));
                // handleCursor();
				if (annotationsUnderCursor.size() > 0)
				{
                    // annotationsUnderCursor is sorted in the way they appear
                    // in the document
					// annot[0] is under annot[1] and so on.
					// only show the hover action for the top annotation
                    APdfAnnotation annot = annotationsUnderCursor.get(annotationsUnderCursor.size() - 1);
					TPdfAnnotationType type = annot.getType();
                    switch (type)
                    {
                    case eAnnotationLink:
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        break;
                    case eAnnotationText:
                        // setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                        // break;
                    default:
//                        setMouseMode(PdfMouseMode.SELECT);
                        break;
                    }
					if (!annot.isOpen())
					{
					    annot.hover();
	                    recentHoverAnnotation = annot;
					}
  					
                } else
                {
				    setCursor(lastCursor);

					recentHoverAnnotation = null;
					if (annotationHoverPopupMenu != null)
					{
					    annotationHoverPopupMenu.setVisible(false);
					}
				}
			}
		}
	
		@Override
		public void mouseClicked(MouseEvent e)
		{
		    switch (mouseMode)
		    {
		    case SELECT:
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    markedRects.clear();
                    if (annotationsUnderCursor.size() > 0)
                    {
                        APdfAnnotation annot = annotationsUnderCursor.get(annotationsUnderCursor.size() - 1);
                        try
                        {
                            annotationHoverPopupMenu.setVisible(false);
                            annot.action(e.getClickCount());
                        } catch (PdfViewerException e1)
                        {
                            e1.printStackTrace();
                        }
                    }
                    // no annotations, hide annotation in side bar
                    else
                    {
                        annotationHoverPopupMenu.setVisible(false);
                        controller.fireOnNoAnnotationSelected();
                    }
                    repaint();
                }
            case FREEHAND:
            default:
                break;
            }

        }
    }
				
    private void createHighlightAnnotation()
    {
        Point.Double startOnPage = new Point.Double();
        Point.Double endOnPage = new Point.Double();
        int sPage = -1;
        int ePage = -1;
        if (markedRects != null)
        {
            Iterator it = markedRects.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry pair = (Map.Entry) it.next();
                Integer page = (Integer) pair.getKey();
                List<Rectangle.Double> textRects = (List<Rectangle.Double>) pair.getValue();
                int numberOfRects = textRects.size();
                // Each Rectangle.Integer will have 4 coordinates
                Double[] rects = new Double[numberOfRects * 4];
                for (int i = 0; i < numberOfRects; i++)
                {
                    Rectangle.Double rectList = textRects.get(i);
                    int firstIndex = i * 4;
                    rects[firstIndex] = rectList.getMinX();
                    rects[firstIndex + 1] = rectList.getMinY();
                    rects[firstIndex + 2] = rectList.getMaxX();
                    rects[firstIndex + 3] = rectList.getMaxY();
                }

                try
                {
                    controller.createAnnotation(TPdfAnnotationType.eAnnotationHighlight, page, rects);
                } catch (PdfViewerException e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        } else if (rectCur != null)
        {
            try
            {
                sPage = controller.transformOnScreenToOnPage(pressed, startOnPage);
                ePage = controller.transformOnScreenToOnPage(end, endOnPage);
            } catch (PdfViewerException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            PdfUtils.normalizeRect(startOnPage, endOnPage);
            Double[] dRect = { startOnPage.getX(), startOnPage.getY(), endOnPage.getX(), endOnPage.getY() };
            try
            {
                if (sPage > 0 && sPage == ePage)
                    controller.createAnnotation(TPdfAnnotationType.eAnnotationHighlight, sPage, dRect);
            } catch (PdfViewerException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
			}
		}
		
        markedRects.clear();
	}

    private void createInkAnnotation(Double[] inkList)
    {
        try
        {
            controller.createAnnotation(TPdfAnnotationType.eAnnotationInk, cursorOnPage, inkList);
        } catch (PdfViewerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

	private String _selectedText = "";

    public String getSelectedText()
    {
		return _selectedText;
	}

	/**
	 * Paint a transparent (xor) marking Rectangle.Integer with a solid border to mark
	 * an area (i.e. the zoom to area).
	 * 
	 * @param ConsiderPrevious
	 *            A boolean value that removes the previously drawn rectangle
	 *            again. Set this to true while dragging, set this to false to
	 *            remove the drawn Rectangle.Integer completely.
	 */
    private void paintMarkRect(boolean considerPrevious)
    {
		Graphics g = this.getGraphics();
		g.setXORMode(java.awt.Color.WHITE);

		g.setColor(new java.awt.Color(248, 250, 255));
        if (considerPrevious)
			g.fillRect(rectPrev.x, rectPrev.y, rectPrev.width, rectPrev.height);
		g.fillRect(rectCur.x, rectCur.y, rectCur.width, rectCur.height);

		g.setColor(java.awt.Color.BLUE);
        if (considerPrevious)
			g.drawRect(rectPrev.x, rectPrev.y, rectPrev.width, rectPrev.height);
		g.drawRect(rectCur.x, rectCur.y, rectCur.width, rectCur.height);
	}

	/**
	 * Paint a descriptive transparent (xor) text.
	 * 
	 * @param description
	 *            The text to be drawn.
	 * @param pt
	 *            The position.
	 */
    private void paintDescription(String description)
    {
		Graphics g = this.getGraphics();
		g.setXORMode(java.awt.Color.WHITE);
		g.setFont(new Font("Helvetica", Font.ITALIC, 18));
		g.setColor(java.awt.Color.BLUE);
		g.drawString(description, pressed.x, pressed.y);
	}

    private void moveMarkedRectsToHighlightedRects()
    {
        for (Map.Entry<Integer, List<Rectangle.Double>> list : markedRects.entrySet())
        {
			if (!highlightedRects.containsKey(list.getKey()))
				highlightedRects.put(list.getKey(), list.getValue());
			else
				highlightedRects.get(list.getKey()).addAll(list.getValue());
		}
		markedRects.clear();
		repaint();
	}

    private void removeHighlightedRects()
    {
		// TODO ideally i would remember which rects belong together and only
		// remove the highlight i clicked on
		highlightedRects.clear();
		repaint();
	}

    private void copySelectedText()
    {
		Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
		c.setContents(new StringSelection(_selectedText), null);
	}
	
    public void registerOnMouseModeChanged(IOnMouseModeChangedListener listener)
    {
		mouseModeChangedListenerList.add(listener);
		listener.onMouseModeChanged(getMouseMode()); 
	}
	
    public void fireMouseModeChanged(PdfMouseMode mouseMode)
    {
        for (IOnMouseModeChangedListener l : mouseModeChangedListenerList)
			l.onMouseModeChanged(mouseMode);
	}

    public void onLostFocus()
    {
        recentHoverAnnotation = null;
        annotationHoverPopupMenu.setVisible(false);
    }
    
   
       
	/**
	 * 
	 */
	private static final long serialVersionUID = -3993187478568332683L;
	private static final int PIXEL_WHEEL = 40;
	
	private BufferedImage outBitmap;

	private ArrayList<IOnMouseModeChangedListener> mouseModeChangedListenerList;
	private PdfViewerController controller;
	private Point.Integer pressed;
	private Point.Integer end;

	private Rectangle.Integer rectCur;
	private Rectangle.Integer rectPrev;
	
	private Cursor currentCursor, lastCursor;
    private int pageOfMouseLocation;
    private List<Double> vertexList;
    private List<Integer> pageVertexList;
    private static final int sampleRate = 1;
    private int sampleCount = 0;

	private Timer middleMouseScrollingTimer;
	private PdfMouseMode mouseMode = PdfMouseMode.SELECT;

	private Map<Integer, List<Rectangle.Double>> markedRects;
	private Map<Integer, List<Rectangle.Double>> highlightedRects;
	private List<APdfAnnotation> annotations;
    private List<APdfAnnotation> annotationsUnderCursor = new ArrayList<APdfAnnotation>(10); // make it 10 so reallocation shouldn't be needed
	private int cursorOnPage;
	private APdfAnnotation recentHoverAnnotation;
    private APdfAnnotation annotationToDelete;
	
	private JPopupMenu annotationHoverPopupMenu;
	private JTextArea annotationHoverTextArea;
    private AnnotationRectangle annotMoveRect, annotSelectedRect;
	private List<PopupFrame> popupFrameList;

    public static final int STICKYWIDTH = 20;
    public static final int STICKYHEIGHT = 18;
	
	private static java.awt.Color highlightFillColor = new java.awt.Color(230, 230, 0, 100);
	private static java.awt.Color highlightStrokeColor = new java.awt.Color(230, 230, 0, 100);
	private static java.awt.Color markedFillColor = new java.awt.Color(0, 150, 200, 100);
	private static java.awt.Color markedStrokeColor = new java.awt.Color(0, 150, 200, 100);
	
	private JPopupMenu popupMenu;
	private JMenuItem menuItemHighlight, menuItemRemH, menuItemCopy;
    private JMenuItem menuItemCreateStickyAnnot;
    private JMenuItem menuItemDeleteAnnotation;
    private JMenuItem menuItemCreateHighlight;
    private JMenuItem menuItemFreehandAnnot;


}
