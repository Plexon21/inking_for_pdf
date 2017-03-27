package com.pdf_tools.samples.pdfviewerapi;

/***************************************************************************
 *
 * File:            PdfViewer.java
 * 
 * Package:         3-Heights(TM) PDF Java Viewer
 *
 * Description:     The PDF Viewer Application.
 *
 * @author          Christian Hagedorn, PDF Tools AG   
 * 
 * Copyright:       Copyright (C) 2001 - 2008 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *                  
 ***************************************************************************/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.NumberFormatter;

import com.pdf_tools.pdfviewer.Model.PdfViewerException;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.*;
import com.pdf_tools.pdfviewer.SwingAPI.PdfViewerComponent;

/**
 * @author fwe A runnable that gets executed in the dispatcher thread
 */
public final class PdfViewer implements IOnZoomCompletedListener,
		IOnVisiblePageRangeChangedListener, IOnOpenCompletedListener,
		IOnCloseCompletedListener, IOnSaveCompletedListener,
		IOnPageLayoutModeChangedListener, IOnFitModeChangedListener, 
		IOnTextExtractedListener, IOnMouseModeChangedListener, WindowListener {

	private class ExecuteAtStart implements Runnable {
		private String[] args;

        public ExecuteAtStart(PdfViewerComponent controller, String[] args) {
			this.controller = controller;
			this.args = args;
		}

		@Override
		public void run() {
			try {
			    if (args.length > 0)
			    {
			        try
                    {
                        openFile(args[0]);
                    } catch (Exception e)
                    {
                        // TODO: handle exception
                    }
			    }
//			    openFile("C:\\Users\\pgl\\Documents\\2pages.pdf");
//				openFile("E:\\tmp\\somehighlight.pdf");
//				controller.setPageLayoutMode(TPageLayoutMode.TwoColumnRight);
//				controller.setPageNo(4);

				SwingWorker<Void, Void> s = new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						Robot robot = new Robot();
						Random rand = new Random();
						robot.setAutoDelay(200);
						robot.mouseMove(400, 300);
						robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
						robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
						robot.keyPress(KeyEvent.VK_CONTROL);
						robot.keyPress(KeyEvent.VK_F);
						robot.keyRelease(KeyEvent.VK_F);
						robot.keyRelease(KeyEvent.VK_CONTROL);
						StringSelection searchString = new StringSelection("(This is the last page. ){3}+");

						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						clipboard.setContents(searchString, searchString);

						robot.keyPress(KeyEvent.VK_CONTROL);
						robot.keyPress(KeyEvent.VK_V);
						robot.keyRelease(KeyEvent.VK_V);
						robot.keyRelease(KeyEvent.VK_CONTROL);

						robot.keyPress(KeyEvent.VK_ENTER);
						robot.keyRelease(KeyEvent.VK_ENTER);
						/*
						 * controller.setMouseMode(PdfMouseMode.HIGHLIGHT);
						 * for(int i = 0; i < 2; i++){ Thread.sleep(1000); int
						 * rot = rand.nextInt(100)*90 - 50*90;
						 * controller.setRotation(rot); System.out.println(rot);
						 * Thread.sleep(200); robot.mouseMove(400, 300);
						 * robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
						 * robot.mouseMove(1200, 800);
						 * robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); }
						 */
						return null;
					}
				};
				// s.execute();
			} catch (Exception e) {
				System.out.println("robot threw Exception:");
				e.printStackTrace();
				System.exit(-1);
			}
		}

		private PdfViewerComponent controller;
	}

	public static void main(String[] args) {
		new PdfViewer(args);
	}


    
	PdfViewer(String[] args) {
		// Create PdfViewer and register this class for receiving callbacks from
		// the PdfViewer
        		// GUI setup
		frame = new JFrame("Pdf Viewer");
		frame.addWindowListener(this);
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setPreferredSize(new Dimension((int) (0.9 * screenSize.width),
				(int) (0.75 * screenSize.height)));
		frame.setLocation(new Point((int) (0.05 * screenSize.width),
				(int) (0.05 * screenSize.height)));

		viewer = new PdfViewerComponent(true, frame);
        controller = viewer;

        currentFile = null;

		addMenuBar();
		addTopPanel();
		addStatusBar();
		setPasswordPane();
		addPdfViewerComponent();

		//register callbacks
		controller.registerOnCloseCompleted(this);
		controller.registerOnSaveCompleted(this);
		controller.registerOnOpenCompleted(this);
		controller.registerOnZoomCompleted(this);
		controller.registerOnVisiblePageRangeChanged(this);
		controller.registerOnPageLayoutModeChanged(this);
		controller.registerOnFitModeChanged(this);
		controller.registerOnTextExtracted(this);
		controller.registerOnMouseModeChanged(this);
		
		frame.pack();
		frame.setVisible(true);

		ExecuteAtStart e = new ExecuteAtStart(controller, args);
		SwingUtilities.invokeLater(e);
	}
	
	
	private void addMenuBar() {
		itemOpen = new JMenuItem(new OpenAction("Open", KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK)));
		itemSave = new JMenuItem(new SaveAction("Save as", KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK)));
		itemClose = new JMenuItem(new CloseAction("Close", KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK)));
		itemExit = new JMenuItem(new ExitAction("Exit", KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK)));
		itemTextSearch = new JMenuItem(new TextSearchAction("Search Text", KeyStroke.getKeyStroke(KeyEvent.VK_F,
				InputEvent.CTRL_MASK)));
		itemFirst = new JMenuItem(new NavigationAction("First Page", KeyStroke.getKeyStroke("HOME"),
				NavigationType.FIRST));
		itemNext = new JMenuItem(new NavigationAction("Last Page", KeyStroke.getKeyStroke("END"), NavigationType.LAST));
		itemPrevious = new JMenuItem(new NavigationAction("Next Page", KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,
				InputEvent.CTRL_MASK), NavigationType.NEXT));
		itemLast = new JMenuItem(new NavigationAction("Previous Page", KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,
				InputEvent.CTRL_MASK), NavigationType.PREVIOUS));
		itemZoomIn = new JMenuItem(new ZoomFactorAction("Zoom In", null, ZoomFactorType.ZOOM_IN));
		itemZoomOut = new JMenuItem(new ZoomFactorAction("Zoom Out", null, ZoomFactorType.ZOOM_OUT));
		itemFitWidth = new JCheckBoxMenuItem(new ZoomModeAction("Fit Width", null,
				TFitMode.FIT_WIDTH));
		itemFitPage = new JCheckBoxMenuItem(
				new ZoomModeAction("Fit Page", null, TFitMode.FIT_PAGE));
		itemActualSize = new JCheckBoxMenuItem(new ZoomModeAction("Actual Size", null,
				TFitMode.ACTUAL_SIZE));
		itemRotateLeft = new JMenuItem(new RotationAction("Rotate Left", null, RotationType.ROTATE_LEFT));
		itemRotateRight = new JMenuItem(new RotationAction("Rotate Right", null, RotationType.ROTATE_RIGHT));
		itemIgnoreDocumentPreferences = new JCheckBoxMenuItem(new IgnorePreferencesAction(
				"Ignore preferences embedded in the document", null));

		itemPageLayoutSinglePage = new JCheckBoxMenuItem(new PageLayoutModeAction("SinglePage", null,
				TPageLayoutMode.SinglePage));
		itemPageLayoutTwoPageLeft = new JCheckBoxMenuItem(new PageLayoutModeAction("TwoPageLeft", null,
				TPageLayoutMode.TwoPageLeft));
		itemPageLayoutTwoPageRight = new JCheckBoxMenuItem(new PageLayoutModeAction("TwoPageRight", null,
				TPageLayoutMode.TwoPageRight));
		itemPageLayoutOneColumn = new JCheckBoxMenuItem(new PageLayoutModeAction("OneColumn", null,
				TPageLayoutMode.OneColumn));
		itemPageLayoutTwoColumnLeft = new JCheckBoxMenuItem(new PageLayoutModeAction("TwoColumnLeft", null,
				TPageLayoutMode.TwoColumnLeft));
		itemPageLayoutTwoColumnRight = new JCheckBoxMenuItem(new PageLayoutModeAction("TwoColumnRight", null,
				TPageLayoutMode.TwoColumnRight));

		itemSidePanelOutlinesEnabled = new JCheckBoxMenuItem();
		itemSidePanelOutlinesEnabled.setSelected(true);
		itemSidePanelOutlinesEnabled.setAction(new SidePanelOutlinesAction());
		itemSidePanelThumbnailsEnabled = new JCheckBoxMenuItem();
		itemSidePanelThumbnailsEnabled.setSelected(true);
		itemSidePanelThumbnailsEnabled.setAction(new SidePanelThumbnailsAction());
		itemSidePanelAnnotationsEnabled = new JCheckBoxMenuItem();
		itemSidePanelAnnotationsEnabled.setSelected(true);
		itemSidePanelAnnotationsEnabled.setAction(new SidePanelAnnotationAction());

		itemMoveMode = new JCheckBoxMenuItem(new MouseModeAction("Move", null, PdfMouseMode.MOVE));
		itemZoomMode = new JCheckBoxMenuItem(new MouseModeAction("Zoom", null, PdfMouseMode.ZOOM));
		itemHighlightMode = new JCheckBoxMenuItem(new MouseModeAction("Highlight Text within rectangle", null,
				PdfMouseMode.HIGHLIGHT));
		itemSelectMode = new JCheckBoxMenuItem(new MouseModeAction("Select text for highlighting/extraction", null,
				PdfMouseMode.SELECT));

		// Menu items disabled when no document is opened
		enableMenuItemsForOpenDocuments(false);

		JMenuBar menuBar = new JMenuBar();

		JMenu menu = new JMenu("File");
		menu.add(itemOpen);
		menu.add(itemSave);
		menu.add(itemClose);
		menu.addSeparator();
		menu.add(itemTextSearch);
		menu.addSeparator();
		menu.add(itemExit);
		menuBar.add(menu);

		menu = new JMenu("Navigate");
		menu.add(itemFirst);
		menu.add(itemPrevious);
		menu.add(itemNext);
		menu.add(itemLast);
		menuBar.add(menu);

		menu = new JMenu("View");
		menu.add(itemRotateRight);
		menu.add(itemRotateLeft);
		menu.addSeparator();
		menu.add(itemZoomIn);
		menu.add(itemZoomOut);

		menu.addSeparator();

		menu.add(itemActualSize);
		menu.add(itemFitWidth);
		menu.add(itemFitPage);
		menuBar.add(menu);

		menu = new JMenu("PageLayout");
		menu.add(itemPageLayoutSinglePage);
		menu.add(itemPageLayoutTwoPageLeft);
		menu.add(itemPageLayoutTwoPageRight);
		menu.add(itemPageLayoutOneColumn);
		menu.add(itemPageLayoutTwoColumnLeft);
		menu.add(itemPageLayoutTwoColumnRight);
		menuBar.add(menu);

		menu = new JMenu("Mouse");
		menu.add(itemZoomMode);
		menu.add(itemHighlightMode);
		menu.add(itemMoveMode);
		menu.add(itemSelectMode);
		menuBar.add(menu);

		menu = new JMenu("SidePanel");
		menu.add(itemSidePanelOutlinesEnabled);
		menu.add(itemSidePanelThumbnailsEnabled);
		menu.add(itemSidePanelAnnotationsEnabled);
		menuBar.add(menu);

		// Stuff only used for internal debugging
		menu = new JMenu("Debug");
		menu.add(itemIgnoreDocumentPreferences);
		menuBar.add(menu);

		frame.setJMenuBar(menuBar);
	}

	/**
	 * Invoked on close and open actions to enable or disable menu items, If the
	 * document is closed, these items are disabled, since they have no effect
	 * without an open document.
	 * 
	 * @param enable
	 */
	private void enableMenuItemsForOpenDocuments(boolean enable) {
		itemClose.setEnabled(enable);
		itemTextSearch.setEnabled(enable);
		itemFirst.setEnabled(enable);
		itemNext.setEnabled(enable);
		itemPrevious.setEnabled(enable);
		itemLast.setEnabled(enable);
		itemSave.setEnabled(enable);
	}
	
	

	private void addTopPanel() {
		buttonOpen = new PanelButton(new OpenAction(ResourceManager.ICON_OPEN));
		buttonSave = new PanelButton(new SaveAction(ResourceManager.ICON_SAVE, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK))); 
		buttonFirst = new PanelButton(new NavigationAction(NavigationType.FIRST, ResourceManager.ICON_FIRST), false);
		buttonPrevious = new PanelButton(new NavigationAction(NavigationType.PREVIOUS, ResourceManager.ICON_PREVIOUS),
				false);
		buttonNext = new PanelButton(new NavigationAction(NavigationType.NEXT, ResourceManager.ICON_NEXT), false);
		buttonLast = new PanelButton(new NavigationAction(NavigationType.LAST, ResourceManager.ICON_LAST), false);
		buttonZoomIn = new PanelButton(new ZoomFactorAction(ZoomFactorType.ZOOM_IN, ResourceManager.ICON_ZOOMIN), true);
		buttonZoomOut = new PanelButton(new ZoomFactorAction(ZoomFactorType.ZOOM_OUT, ResourceManager.ICON_ZOOMOUT),
				true);
		buttonFitPage = new PanelButton(new ZoomModeAction(TFitMode.FIT_PAGE,
				ResourceManager.ICON_FITPAGE));
		buttonFitWidth = new PanelButton(new ZoomModeAction(TFitMode.FIT_WIDTH,
				ResourceManager.ICON_FITWIDTH));
		buttonActualSize = new PanelButton(new ZoomModeAction(TFitMode.ACTUAL_SIZE,
				ResourceManager.ICON_ACTSIZE));
		buttonRotateRight = new PanelButton(new RotationAction(RotationType.ROTATE_RIGHT, ResourceManager.ICON_ROTATE));

		// Only icons which are not intuitive have a tool tip
		buttonFitPage.setToolTipText("Fit Page");
		buttonFitWidth.setToolTipText("Fit Width");
		buttonActualSize.setToolTipText("Actual Size");

		final Border grayBorderLine = BorderFactory.createLineBorder(Color.GRAY);

		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		topPanel.add(buttonOpen);
		topPanel.add(buttonSave);

		// Navigation panel
		JPanel navigationPanel = new JPanel();
		navigationPanel.setBorder(grayBorderLine);
		navigationPanel.add(buttonFirst);
		navigationPanel.add(buttonPrevious);

		labelPageNo = new JLabel("Page:");
		labelPageNo.setFont(COMMON_FONT);
		navigationPanel.add(labelPageNo);
		NumberFormatter formatter = new NumberFormatter(NumberFormat.getInstance());
		formatter.setMinimum(1);
		formatter.setValueClass(Integer.class);
		textFieldPageNo = new JFormattedTextField(formatter);
		textFieldPageNo.setColumns(3);
		textFieldPageNo.setAction(new NavigationAction(NavigationType.SET_PAGE_NO));
		navigationPanel.add(textFieldPageNo);
		JLabel labelSlash = new JLabel("/");
		navigationPanel.add(labelSlash);
		labelPageCount = new JLabel("0");
		labelPageCount.setFont(COMMON_FONT);
		navigationPanel.add(labelPageCount);
		topPanel.add(navigationPanel);

		navigationPanel.add(buttonNext);
		navigationPanel.add(buttonLast);
		topPanel.add(navigationPanel);

		// Zoom Level
		JPanel zoomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		zoomPanel.setBackground(COLOR_LIGHTGRAY);
		zoomPanel.setBorder(grayBorderLine);
		zoomPanel.add(buttonZoomOut);
		textFieldZoom = new JFormattedTextField(DECIMAL_FORMAT);
		textFieldZoom.setColumns(4);
		textFieldZoom.setAction(new ZoomFactorAction(ZoomFactorType.SET_ZOOM));
		zoomPanel.add(textFieldZoom);
		zoomPanel.add(buttonZoomIn);
		topPanel.add(zoomPanel);

		topPanel.add(buttonFitPage);
		topPanel.add(buttonFitWidth);
		topPanel.add(buttonActualSize);

		// Rotation
		topPanel.add(buttonRotateRight);

		enableTopPanelForOpenDocuments(false);

		frame.getContentPane().add(topPanel, BorderLayout.NORTH);
	}

	/**
	 * Invoked on close and open actions to enable or disable panel buttons, If
	 * the document is closed, these buttons are disabled, since they have no
	 * effect without an open document.
	 * 
	 * @param enable
	 */
	private void enableTopPanelForOpenDocuments(boolean enable) {
		buttonFirst.setEnabled(enable);
		buttonNext.setEnabled(enable);
		buttonPrevious.setEnabled(enable);
		buttonLast.setEnabled(enable);
		textFieldPageNo.setEnabled(enable);
		buttonSave.setEnabled(enable);
	}
	
	/**
	 * Creates a box with a JPasswordField
	 */
	private void setPasswordPane() {
		passwordPane = Box.createHorizontalBox();
		passwordField = new JPasswordField();
		passwordField.addAncestorListener( new RequestFocusListener());
		passwordPane.add(passwordField);
	}
	
	/**
	 * Getting the focus on the JPassword field when the JOptionFrame
	 * is loading
	 * @author pgl
	 *
	 */
	
	public class RequestFocusListener implements AncestorListener
	{
		/*
		 *  Convenience constructor. The listener is only used once and then it is
		 *  removed from the component.
		 */
		public RequestFocusListener() {}

		@Override
		public void ancestorAdded(AncestorEvent e)
		{
			JComponent component = e.getComponent();
			component.requestFocusInWindow();
		}

		@Override
		public void ancestorMoved(AncestorEvent e) {}

		@Override
		public void ancestorRemoved(AncestorEvent e) {}
	}

	/**
	 * A class creating navigation panel buttons and add an action to each
	 * button
	 * 
	 * @author cha
	 *
	 */
	private class PanelButton extends JButton {
		private static final long serialVersionUID = 1L;

		private final Dimension largeIcon = new Dimension(36, 32);
		private final Dimension smallIcon = new Dimension(24, 20);

		/**
		 * Add a small button with an icon.
		 * 
		 * @param icon
		 *            The icon image.
		 * @param isGray
		 *            Set to true of the background should be gray. Default
		 *            otherwise.
		 */
		PanelButton(AbstractAction action, boolean isGray) {
			setPreferredSize(smallIcon);
			if (isGray) {
				setBorder(null);
				setBackground(COLOR_LIGHTGRAY);
			}
			this.setAction(action);
		}

		/**
		 * Add a large button with an icon.
		 * 
		 * @param icon
		 *            The icon image
		 */
		PanelButton(AbstractAction action) {
			setPreferredSize(largeIcon);
			this.setAction(action);
		}
	}

	private void addStatusBar() {
		// Status bar
		JPanel statusBar = new JPanel();
		statusBar.setPreferredSize(new Dimension(frame.getWidth(), 20));
		statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
		statusBar.setBackground(Color.WHITE);

		// Page specific labels
		JLabel page = new JLabel(" Page: ");
		labelPageCountStatus = new JLabel("0");
		JLabel pageBetween = new JLabel(" / ");
		page.setHorizontalAlignment(SwingConstants.LEFT);
		labelPageNoStatus = new JLabel("0");
		statusBar.add(page);
		statusBar.add(labelPageNoStatus);
		statusBar.add(pageBetween);
		statusBar.add(labelPageCountStatus);
		statusBar.add(Box.createRigidArea(SPACE_STATUS_BAR));

		// Zoom factor specific labels
		JLabel zoomFactor = new JLabel("Zoom Factor: ");
		labelZoomFactorStatus = new JLabel("0.0");
		statusBar.add(zoomFactor);
		statusBar.add(labelZoomFactorStatus);
		statusBar.add(Box.createRigidArea(SPACE_STATUS_BAR));

		// Zoom mode specific labels
		JLabel zoomMode = new JLabel("Zoom Mode: ");
		labelZoomModeStatus = new JLabel("-");
		statusBar.add(zoomMode);
		statusBar.add(labelZoomModeStatus);
		statusBar.add(Box.createRigidArea(SPACE_STATUS_BAR));

		// Rotation specific labels
		JLabel rotation = new JLabel("Rotation: ");
		labelRotationStatus = new JLabel("0");
		statusBar.add(rotation);
		statusBar.add(labelRotationStatus);
		statusBar.add(Box.createRigidArea(SPACE_STATUS_BAR));

		// Layout mode specific labels
		JLabel layoutMode = new JLabel("Layout Mode: ");
		labelLayoutModeStatus = new JLabel("-");
		statusBar.add(layoutMode);
		statusBar.add(labelLayoutModeStatus);
		statusBar.add(Box.createRigidArea(SPACE_STATUS_BAR));

		// Mouse mode specific labels
		JLabel mouseMode = new JLabel("Mouse Mode: ");
		labelMouseModeStatus = new JLabel("-");
		statusBar.add(mouseMode);
		statusBar.add(labelMouseModeStatus);
		statusBar.add(Box.createRigidArea(SPACE_STATUS_BAR));

		// Path specific labels
		JLabel path = new JLabel("Path: ");
		labelPathStatus = new JLabel("-");
		statusBar.add(path);
		statusBar.add(labelPathStatus);
		

		frame.getContentPane().add(statusBar, BorderLayout.SOUTH);
	}

	private void addPdfViewerComponent() {
		frame.getContentPane().add(viewer, BorderLayout.CENTER);
	}

	public void onCloseCompleted(PdfViewerException ex) {
		textFieldPageNo.setText(String.valueOf(0));
		labelPageCountStatus.setText(String.valueOf(0));
		labelPageCount.setText(String.valueOf(0));
		labelPageNoStatus.setText(String.valueOf(controller.getPageNo()));
		textFieldZoom.setText(String.valueOf(controller.getZoom()));
		labelZoomFactorStatus.setText(String.valueOf(controller.getZoom()));
		labelZoomModeStatus.setText("-");
		labelLayoutModeStatus.setText("-");
		labelRotationStatus.setText(String.valueOf(controller.getRotation()));
		labelPathStatus.setText("-");
		labelMouseModeStatus.setText("-");
	}

	@Override
	public void onVisiblePageRangeChanged(int firstPage, int lastPage) {
		textFieldPageNo.setText(String.valueOf(firstPage));
		labelPageNoStatus.setText(String.valueOf(firstPage));
	}

	@Override
	public void onMouseModeChanged(PdfMouseMode newMouseMode) {
		itemZoomMode.setSelected(false);
		itemMoveMode.setSelected(false);
		itemHighlightMode.setSelected(false);
		itemSelectMode.setSelected(false);
		switch (newMouseMode) {
		case ZOOM: {
			itemZoomMode.setSelected(true);
			break;
		}
		case MOVE: {
			itemMoveMode.setSelected(true);
			break;
		}
		case HIGHLIGHT: {
			itemHighlightMode.setSelected(true);
			break;
		}
		case SELECT: {
			itemSelectMode.setSelected(true);
			break;
		}
		default: {
			break;
		}
		}

		labelMouseModeStatus.setText(newMouseMode.toString());

	}
	
	@Override
	public void onSaveCompleted(PdfViewerException ex) {
		if (ex != null)
		{
			showMessageDialog(ex.getMessage());
		}
	}

	@Override
	public void onOpenCompleted(PdfViewerException ex) {
		
		filename = controller.getFileName();
		if (ex != null) {
			if (ex instanceof PdfViewerException.PdfLicenseInvalidException) {
				try {
					PdfViewerComponent.getLicenseIsValid();
				} catch (PdfViewerException exception) {
					if (tryObtainValidLicense(exception)) {
						openFile(filename);
					}
				}
			
			} else if (ex instanceof PdfViewerException.PdfPasswordException) {
				passwordField.setText("");
				int x;
				if (firstTry) {
					x = JOptionPane.showConfirmDialog(null,passwordPane	,"Please enter the password:", JOptionPane.OK_CANCEL_OPTION);
					firstTry = false;
				}
				else {
					x = JOptionPane.showConfirmDialog(null,passwordPane, "Wrong password, try again.", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
				}
					
				if(x == JOptionPane.OK_OPTION) {
					openFile(filename, passwordField.getPassword());
				}
				else {
				firstTry = true;
				}
				return;

			} else if (ex instanceof PdfViewerException.PdfFileNotFoundException)
			{
				JOptionPane.showMessageDialog(frame, "File \"" + filename + "\" was not found.", "Error", JOptionPane.ERROR_MESSAGE);
				
			} else if (ex instanceof PdfViewerException.PdfFileCorruptException)
			{
				JOptionPane.showMessageDialog(frame, "File \"" + filename + "\" is either corrupt or not a pdf:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				
			} else if (ex instanceof PdfViewerException.PdfUnsupportedFeatureException) 
            {
				JOptionPane.showMessageDialog(frame, "The opened file \"" + filename + "\" uses features that are not supported by the rendering engine:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            else {
				exceptionOccured(ex);
				return;
			}
		}
		firstTry = true;
		enableMenuItemsForOpenDocuments(true);
		enableTopPanelForOpenDocuments(true);
		// Set labels to initial values of the document
		try {
			pageCount = controller.getPageCount();
		} catch (PdfViewerException e) {
			System.out.println("pageCount could not be read, due to file missing:");
//			e.printStackTrace();
		}
		textFieldPageNo.setText(String.valueOf(controller.getPageNo()));
		labelPageCountStatus.setText(String.valueOf(pageCount));
		labelPageCount.setText(String.valueOf(pageCount));
		labelPageNoStatus.setText(String.valueOf(controller.getPageNo()));
		textFieldZoom.setText(String.valueOf(String.valueOf(DECIMAL_FORMAT.format(controller.getZoom() * 100))));
		labelZoomFactorStatus
				.setText(String.valueOf(String.valueOf(DECIMAL_FORMAT.format(controller.getZoom() * 100))));
		labelRotationStatus.setText(String.valueOf(controller.getRotation()));
		labelPathStatus.setText(filename);
	}

	public void onZoomCompleted(double zoomFactor) {
		textFieldZoom.setText(String.valueOf(DECIMAL_FORMAT.format(zoomFactor * 100)));
		labelZoomFactorStatus.setText(String.valueOf(DECIMAL_FORMAT.format(zoomFactor * 100)));
		if (zoomFactor == 1) {
			itemActualSize.setSelected(true);
			labelZoomModeStatus.setText("Actual Size");
		} else {
			itemActualSize.setSelected(false);
			itemFitPage.setSelected(false);
			itemFitWidth.setSelected(false);
			labelZoomModeStatus.setText("-");
		}
	}

	public void onPageLayoutModeChanged(TPageLayoutMode newMode) {
		itemPageLayoutSinglePage.setSelected(false);
		itemPageLayoutTwoPageLeft.setSelected(false);
		itemPageLayoutTwoPageRight.setSelected(false);
		itemPageLayoutOneColumn.setSelected(false);
		itemPageLayoutTwoColumnLeft.setSelected(false);
		itemPageLayoutTwoColumnRight.setSelected(false);

		switch (newMode) {
		case SinglePage: {
			itemPageLayoutSinglePage.setSelected(true);
			break;
		}
		case TwoPageLeft: {
			itemPageLayoutTwoPageLeft.setSelected(true);
			break;
		}
		case TwoPageRight: {
			itemPageLayoutTwoPageRight.setSelected(true);
			break;
		}
		case OneColumn: {
			itemPageLayoutOneColumn.setSelected(true);
			break;
		}
		case TwoColumnLeft: {
			itemPageLayoutTwoColumnLeft.setSelected(true);
			break;
		}
		case TwoColumnRight: {
			itemPageLayoutTwoColumnRight.setSelected(true);
			break;
		}
		default:
			break;
		}

	}

	public void onFitModeChanged(TFitMode newMode) {
		itemActualSize.setSelected(false);
		itemFitPage.setSelected(false);
		itemFitWidth.setSelected(false);
		switch (newMode) {
		case ACTUAL_SIZE: {
			itemActualSize.setSelected(true);
			break;
		}
		case FIT_PAGE: {
			itemFitPage.setSelected(true);
			break;
		}
		case FIT_WIDTH: {
			itemFitWidth.setSelected(true);
			break;
		}
		default: {
			break;
		}
		}
		labelZoomModeStatus.setText(newMode.toString());
	}

	@Override
	public void onTextExtracted(String extractedText) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		JOptionPane.showMessageDialog(frame, "<html><body><p style='width: " + screenSize.width / 2 + "px;'>"
				+ extractedText + "</p></body></html>");
	}

	/**
	 * All user actions inherit from this class, which specifies shared behavior
	 * 
	 * @author cha
	 *
	 */
	abstract private class UserActions extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -871805495300203588L;

		UserActions() {
		}

		UserActions(ImageIcon icon) {
			this.putValue(Action.SMALL_ICON, icon);
		}
		
		UserActions(ImageIcon icon, KeyStroke keyStroke)
		{
			this.putValue(Action.SMALL_ICON, icon);
			if (keyStroke != null)
				this.putValue(ACCELERATOR_KEY, keyStroke);
		}

		/**
		 * Creates an action with a description and a shortcut. The shortcut can
		 * be executed from everywhere inside the program. If keyStorke is null,
		 * then no shortcut is registrated;
		 * 
		 * @param description
		 * @param keyStroke
		 */
		UserActions(String description, KeyStroke keyStroke) {
			this.putValue(NAME, description);
			if (keyStroke != null)
				this.putValue(ACCELERATOR_KEY, keyStroke);
		}
	}

	/**
	 * Action performed for open request
	 * 
	 * @author cha
	 *
	 */
	private class OpenAction extends UserActions {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1425165899265917385L;

		OpenAction(ImageIcon icon) {
			super(icon);
		}

		OpenAction(String description, KeyStroke keyStroke) {
			super(description, keyStroke);
		}

		public void actionPerformed(ActionEvent e) {
			final JFileChooser fc = new JPDFFileChooser(currentFile);
			if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
				itemClose.doClick();
				currentFile = fc.getSelectedFile();
				filename = currentFile.getAbsolutePath();
				openFile(filename);
				itemActualSize.setSelected(true);
			}
		}
	}
	
	private class SaveAction extends UserActions {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        
        SaveAction(ImageIcon icon, KeyStroke keyStroke)
        {
            super(icon, keyStroke);
        }
        
        SaveAction(String string, KeyStroke keyStroke)
        {
        	super(string, keyStroke);
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
			final JFileChooser fc = new JPDFFileChooser(currentFile){
				@Override
			    public void approveSelection(){
			        File f = getSelectedFile();
			        if(f.exists()){
			            int result = JOptionPane.showConfirmDialog(this,"The file exists, overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
			            switch(result){
			                case JOptionPane.YES_OPTION:
			                    super.approveSelection();
			                    return;
			                case JOptionPane.NO_OPTION:
			                    return;
			                case JOptionPane.CLOSED_OPTION:
			                    return;
			                case JOptionPane.CANCEL_OPTION:
			                    cancelSelection();
			                    return;
			            }
			        }
			        super.approveSelection();
			    } 
			};
			if (fc.showDialog(frame, "Save") == JFileChooser.APPROVE_OPTION) {
				currentFile = fc.getSelectedFile();
				filename = currentFile.getAbsolutePath();
				if (!filename.endsWith(".pdf") && !filename.endsWith(".fdf"))
				{
					// if not specified save as pdf
					filename += ".pdf";
				}
				saveFile(filename);
			}
		}
	}

	/**
	 * Call openFile(String, char[]) with a null char[] array
	 * @param filename
	 */
	protected void openFile(String filename) {
		openFile(filename, null);
	}
	
	protected void saveFile(String filename)
	{
	    try {
			controller.saveFile(filename);
		} catch (PdfViewerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void openFile(String filename, char[] password) {
		this.filename = filename;
		try {
			controller.open(filename, password);
		} catch (PdfViewerException ex) {
			if (tryObtainValidLicense(ex))
				openFile(filename, password);
		}
	}
	
	private boolean tryObtainValidLicense(PdfViewerException ex) {
		if (!(ex instanceof PdfViewerException.PdfLicenseInvalidException)) {
			exceptionOccured(ex);
			return false;
		}
		String licenseKey = JOptionPane.showInputDialog(frame, ex.getMessage() + " Insert License key manually:",
				"Error", JOptionPane.QUESTION_MESSAGE);
		if (licenseKey.length() <= 0)
			return false;
		try {
			PdfViewerComponent.setLicenseKey(licenseKey);
			return true;
		} catch (PdfViewerException e) {
			return tryObtainValidLicense(e);
		}
	}

	
	/**
	 * Action performed for close request
	 * 
	 * @author cha
	 *
	 */
	private class CloseAction extends UserActions {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3746102371615018125L;

		CloseAction(String description, KeyStroke keyStroke) {
			super(description, keyStroke);
		}

		public void actionPerformed(ActionEvent e) {
			controller.close();
			enableMenuItemsForOpenDocuments(false);
			enableTopPanelForOpenDocuments(false);
			labelPageCount.setText("0");
			textFieldPageNo.setText("");
		}
	}

	/**
	 * Action performed for exit request
	 * 
	 * @author cha
	 *
	 */
	private class ExitAction extends UserActions {
		/**
		 * 
		 */
		private static final long serialVersionUID = -401072009820884203L;

		ExitAction(String description, KeyStroke keyStroke) {
			super(description, keyStroke);
		}

		public void actionPerformed(ActionEvent e) {
			controller.close();
			System.exit(0);
		}
	}

	/**
	 * Action performed for a text search request
	 * 
	 * @author cha
	 *
	 */
	private class TextSearchAction extends UserActions {
		/**
		 * 
		 */
		private static final long serialVersionUID = -401072009820884203L;

		TextSearchAction(String description, KeyStroke keyStroke) {
			super(description, keyStroke);
		}

		public void actionPerformed(ActionEvent e) {
			if (dialog != null)
				dialog.dispose();

			dialog = new SearchDialog(frame);
			dialog.pack();
			dialog.setVisible(true);
		}

		private class SearchDialog extends JDialog implements PropertyChangeListener, ActionListener, ItemListener,
				IOnSearchCompletedListener {
			SearchDialog(Frame frame) {
				setAlwaysOnTop(true);
				setTitle("Find Text");
				textField = new JTextField();

				checkBoxPrevious = new JCheckBox("Search backwards", false);
				checkBoxPrevious.setMnemonic(KeyEvent.VK_B);
				checkBoxPrevious.addItemListener(this);
				checkBoxMatchCase = new JCheckBox("Match case", false);
				checkBoxMatchCase.setMnemonic(KeyEvent.VK_C);
				checkBoxMatchCase.addItemListener(this);
				checkBoxWrap = new JCheckBox("Wrap around", true);
				checkBoxWrap.setMnemonic(KeyEvent.VK_W);
				checkBoxWrap.addItemListener(this);
				checkBoxUseRegex = new JCheckBox("Use regular expressions", false);
				checkBoxUseRegex.setMnemonic(KeyEvent.VK_R);
				checkBoxUseRegex.addItemListener(this);

				String info = "Find: ";
				findButton = new JButton("Find");
				findButton.setMultiClickThreshhold(10);
				String buttonCancel = "Cancel";
				result = new JLabel("-");
				result.setHorizontalAlignment(JLabel.RIGHT);

				Object[] array = { info, textField, result, checkBoxPrevious, checkBoxMatchCase, checkBoxWrap,
						checkBoxUseRegex };

				Object[] options = { findButton, buttonCancel };
				findButton.addActionListener(this);

				optionPane = new JOptionPane(array, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null,
						options, options[0]);
				setContentPane(optionPane);

				optionPane.addPropertyChangeListener(this);
				setLocationRelativeTo(frame);

				// Load old search request
				textField.setText(lastEntry);
				currentEntry = "";
				controller.registerOnSearchCompleted(this);
			}

			/** This method handles events for the text field. */
			public void actionPerformed(ActionEvent e) {
				optionPane.setValue(findButton);
			}

			public void itemStateChanged(ItemEvent e) {
				controller.configureSearcher(checkBoxMatchCase.isSelected(), checkBoxWrap.isSelected(),
						checkBoxPrevious.isSelected(), checkBoxUseRegex.isSelected());
			}

			@Override
			public void propertyChange(PropertyChangeEvent e) {
				String prop = e.getPropertyName();

				if (isVisible() && (e.getSource() == optionPane) && (JOptionPane.VALUE_PROPERTY.equals(prop))) {
					Object value = optionPane.getValue();

					if (value == JOptionPane.UNINITIALIZED_VALUE) {
						System.out.println("uninitialized");
						return;
					}

					// Check for new search request
					currentEntry = textField.getText();
					if (!currentEntry.equals(lastEntry))
						pageNo = controller.getPageNo();

					// Save last entry to show again on new search request
					lastEntry = currentEntry;

					// Do this to reset property, otherwise user can't click
					// "Find" anymore
					optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

					if (!value.equals(findButton)) {
						textField.setText(null);
						setVisible(false);
					} else {
						try {
							controller.search(textField.getText(), pageNo, index);
						} catch (PdfViewerException ex) {
							System.out.println("Search on empty document");
							ex.printStackTrace();
						}
					}
				}
			}

			@Override
			public void onSearchCompleted(int page, int index, Map<Integer, List<Rectangle2D.Double>> rects) {
				if (rects == null) {
					result.setText("No match found");
					result.setForeground(Color.RED);
				} else {
					pageNo = page;
					this.index = index;
					result.setText("Match found on page " + page);
					result.setForeground(Color.GRAY);
				}
			}

			/**
			 * 
			 */
			private static final long serialVersionUID = -8515470914762372404L;

			private JLabel result;
			private JTextField textField;
			private JOptionPane optionPane;
			private JCheckBox checkBoxPrevious, checkBoxMatchCase, checkBoxWrap, checkBoxUseRegex;
			private JButton findButton;
			private int pageNo;
			private int index;
		}

		private SearchDialog dialog;
		private String lastEntry;
		private String currentEntry;

	}

	/**
	 * Action performed when the page number is changed. The type is specified
	 * by NavigationType
	 * 
	 * @author cha
	 *
	 */
	private class NavigationAction extends UserActions {
		/**
		 * 
		 */
		private static final long serialVersionUID = -969569478509202668L;

		private NavigationType type;

		NavigationAction(NavigationType type) {
			this.type = type;
		}

		NavigationAction(NavigationType type, ImageIcon icon) {
			super(icon);
			this.type = type;
		}

		NavigationAction(String description, KeyStroke keyStroke, NavigationType type) {
			super(description, keyStroke);
			this.type = type;
		}

		public void actionPerformed(ActionEvent e) {
			int pageNo = controller.getPageNo();

			switch (type) {
			case FIRST: {
				pageNo = 1;
				break;
			}
			case LAST: {
				pageNo = pageCount;
				break;
			}
			case NEXT: {
				switch (controller.getPageLayoutMode().horizontalScrollPosition(pageNo)) {
				case -1:
					pageNo += 2;
					break;
				default:
					pageNo++;
					break;
				}
				break;
			}
			case PREVIOUS: {
				switch (controller.getPageLayoutMode().horizontalScrollPosition(pageNo)) {
				case -1:
					pageNo -= 2;
					break;
				case 0:
					pageNo -= 1;
					break;
				case 1:
					pageNo -= 3;
					break;
				}
				break;
			}
			case SET_PAGE_NO: {
				String pageText = textFieldPageNo.getText();
				if (pageText.length() <= 0)
					return;
				try {
					int newPageNo = Integer.parseInt(pageText);
					pageNo = newPageNo;
				} catch (NumberFormatException ex) {
					return;
				}
				break;
			}
			}

			pageNo = Math.max(1, Math.min(pageCount, pageNo));

			try {
				controller.setPageNo(pageNo);
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			} catch (PdfViewerException e1) {
				e1.printStackTrace();
			}
			String textPageNo = String.valueOf(controller.getPageNo());
			textFieldPageNo.setText(textPageNo);
			labelPageNoStatus.setText(textPageNo);
		}

	}

	/**
	 * Action performed when the zoom factor is changed. The type is specified
	 * by ZoomFactorType
	 * 
	 * @author cha
	 *
	 */
	private class ZoomFactorAction extends UserActions {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6356134751319133286L;

		private ZoomFactorType type;

		ZoomFactorAction(ZoomFactorType type) {
			this.type = type;
		}

		ZoomFactorAction(ZoomFactorType type, ImageIcon icon) {
			super(icon);
			this.type = type;
		}

		ZoomFactorAction(String description, KeyStroke keyStroke, ZoomFactorType type) {
			super(description, keyStroke);
			this.type = type;
		}

		public void actionPerformed(ActionEvent e) {
			double zoomFactor = controller.getZoom();

			switch (type) {
			case ZOOM_IN: {
				double newZoomFactor = zoomFactor * ZOOM_STEP_FACTOR;
				if (newZoomFactor > MAX_ZOOM)
					zoomFactor = MAX_ZOOM;
				else
					zoomFactor = newZoomFactor;
				break;
			}
			case ZOOM_OUT: {
				double newZoomFactor = zoomFactor / ZOOM_STEP_FACTOR;
				if (newZoomFactor < MIN_ZOOM)
					zoomFactor = MIN_ZOOM;
				else
					zoomFactor = newZoomFactor;
				break;
			}
			case SET_ZOOM: {
				String zoomText = textFieldZoom.getText();
				if (zoomText.length() <= 0)
					return;
				try {
					double newZoomFactor = Double.parseDouble(zoomText) / 100.0;
					if (newZoomFactor == zoomFactor)
						return;
					else if (newZoomFactor < MIN_ZOOM)
						newZoomFactor = MIN_ZOOM;
					else if (newZoomFactor > MAX_ZOOM)
						newZoomFactor = MAX_ZOOM;
					zoomFactor = newZoomFactor;
				} catch (NumberFormatException ex) {
					return;
				}
				break;
			}
			}
			try {
				controller.setZoom(zoomFactor);
			} catch (IllegalArgumentException e1) {
				exceptionOccured(e1);
			} catch (PdfViewerException e1) {
				exceptionOccured(e1);
			}
			textFieldZoom.setText(String.valueOf(DECIMAL_FORMAT.format(controller.getZoom() * 100)));
			labelZoomFactorStatus.setText(String.valueOf(DECIMAL_FORMAT.format(controller.getZoom() * 100)));
			if (zoomFactor == 1) {
				itemActualSize.setSelected(true);
				labelZoomModeStatus.setText("Actual Size");
			} else {
				itemActualSize.setSelected(false);
				itemFitPage.setSelected(false);
				itemFitWidth.setSelected(false);
				labelZoomModeStatus.setText("-");
			}
		}
	}

	/**
	 * Action performed when the zoom mode is changed. The type is specified by
	 * IPdfViewer.TFitMode
	 * 
	 * @author cha
	 *
	 */
	private class ZoomModeAction extends UserActions {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2401141259437278678L;

		private TFitMode type;

		ZoomModeAction(TFitMode type, ImageIcon icon) {
			super(icon);
			this.type = type;
		}

		ZoomModeAction(String description, KeyStroke keyStroke, TFitMode type) {
			super(description, keyStroke);
			this.type = type;
		}

		public void actionPerformed(ActionEvent e) {
			try {
				controller.setFitMode(type);
			} catch (IllegalArgumentException e1) {
				exceptionOccured(e1);
				return;
			} catch (PdfViewerException e1) {
				exceptionOccured(e1);
				return;
			}
		}
	}

	/**
	 * Action performed when the mouse mode is changed. The type is specified by
	 * IPdfViewer.PdfMouseMode
	 * 
	 * @author cha
	 *
	 */
	private class MouseModeAction extends UserActions {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2401141259437278678L;

		private PdfMouseMode type;

		MouseModeAction(String description, KeyStroke keyStroke, PdfMouseMode type) {
			super(description, keyStroke);
			this.type = type;
		}

		public void actionPerformed(ActionEvent e) {
			controller.setMouseMode(type);
		}
	}

	/**
	 * Action performed when the canvas mode is changed. The type is specified
	 * by IPdfViewer.PdfCanvasMode
	 * 
	 * @author cha
	 *
	 */
	private class PageLayoutModeAction extends UserActions {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2401141259437278678L;

		private TPageLayoutMode type;

		PageLayoutModeAction(String description, KeyStroke keyStroke, TPageLayoutMode type) {
			super(description, keyStroke);
			this.type = type;
		}

		public void actionPerformed(ActionEvent e) {
			try {
				controller.setPageLayoutMode(type);
			} catch (IllegalArgumentException e1) {
				exceptionOccured(e1);
				return;
			} catch (PdfViewerException e1) {
				exceptionOccured(e1);
				return;
			}
			labelLayoutModeStatus.setText(type.toString());
		}

	}

	private class SidePanelThumbnailsAction extends UserActions {
		private static final long serialVersionUID = 7230827831092519307L;

		SidePanelThumbnailsAction() {
			super("Show Thumbnails", null);
		}

		public void actionPerformed(ActionEvent e) {
			// the setting of the select is done by swing
			boolean willBeSelected = itemSidePanelThumbnailsEnabled.isSelected();
			viewer.showThumbnails(willBeSelected);
		}
	}

	private class SidePanelOutlinesAction extends UserActions {
		private static final long serialVersionUID = 4485753367008461072L;

		SidePanelOutlinesAction() {
			super("Show Outlines", null);
		}

		public void actionPerformed(ActionEvent e) {
			// the setting of the select is done by swing
			boolean willBeSelected = itemSidePanelOutlinesEnabled.isSelected();
			viewer.showOutlines(willBeSelected);
		}
	}
	
	private class SidePanelAnnotationAction extends UserActions
	{

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public SidePanelAnnotationAction()
        {
            super("Show Annotations", null);
        }
        
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            boolean willBeSelected = itemSidePanelAnnotationsEnabled.isSelected();
            viewer.showAnnotations(willBeSelected);
            // TODO Auto-generated method stub
            
        }
	    
	}

	private class IgnorePreferencesAction extends UserActions {
		private static final long serialVersionUID = -6855104905202444204L;

		IgnorePreferencesAction(String description, KeyStroke keyStroke) {
			super(description, keyStroke);
		}

		public void actionPerformed(ActionEvent e) {
			boolean ignoring = controller.getIgnoringPreferences();
			itemIgnoreDocumentPreferences.setSelected(!ignoring);
			controller.setIgnoringPreferences(!ignoring);
		}
	}

	/**
	 * Action performed when the rotation is changed. The type is specified by
	 * RotationType
	 * 
	 * @author cha
	 *
	 */
	private class RotationAction extends UserActions {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7371415882262583301L;

		private RotationType type;

		RotationAction(RotationType type, ImageIcon icon) {
			super(icon);
			this.type = type;
		}

		RotationAction(String description, KeyStroke keyStroke, RotationType type) {
			super(description, keyStroke);
			this.type = type;
		}

		public void actionPerformed(ActionEvent e) {
			int rotation = controller.getRotation();
			switch (type) {
			case ROTATE_LEFT: {
				rotation = (rotation + 270) % 360;
				break;
			}
			case ROTATE_RIGHT: {
				rotation = (rotation + 90) % 360;
				break;
			}
			}
			try {
				controller.setRotation(rotation);
			} catch (IllegalArgumentException e1) {
				exceptionOccured(e1);
				return;
			} catch (PdfViewerException e1) {
				exceptionOccured(e1);
				return;
			}
			labelRotationStatus.setText(String.valueOf(controller.getRotation()));
		}
	}

	/**
	 * File chooser for OpenAction
	 * 
	 * @author cha
	 *
	 */
	private class JPDFFileChooser extends JFileChooser {
		/**
	     * 
	     */
		private static final long serialVersionUID = -2404225736312327991L;

		JPDFFileChooser(File actFile) {
			super(actFile);
			FileFilter act = getFileFilter();
			addChoosableFileFilter(new SupportedFilesFilter());
			addChoosableFileFilter(new PdfFilter());
			setFileFilter(act);
		}

		private class PdfFilter extends FileFilter {
			public boolean accept(File arg0) {
				return (arg0.isDirectory() || arg0.getName().toUpperCase().endsWith("PDF") || arg0.getName().toUpperCase().endsWith("FDF"));
			}

			public String getDescription() {
				return "PDF";
			}
		}

		private class SupportedFilesFilter extends FileFilter {
			public boolean accept(File arg0) {
				return (arg0.isDirectory() || arg0.getName().toUpperCase().endsWith("PDF")
						|| arg0.getName().toUpperCase().endsWith("BMP") || arg0.getName().toUpperCase().endsWith("GIF")
						|| arg0.getName().toUpperCase().endsWith("PNG") || arg0.getName().toUpperCase().endsWith("TIF")
						|| arg0.getName().toUpperCase().endsWith("TIFF")
						|| arg0.getName().toUpperCase().endsWith("JPG")
						|| arg0.getName().toUpperCase().endsWith("JPEG")
						|| arg0.getName().toUpperCase().endsWith("JP2") || arg0.getName().toUpperCase().endsWith("JB2")
						|| arg0.getName().toUpperCase().endsWith("JPX") || arg0.getName().toUpperCase().endsWith("DIB"));
			}

			public String getDescription() {
				return "Supported Files (PDF, Imageformats)";
			}
		}

	}

	private void exceptionOccured(Exception ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		String message = "Exception occured: " + (ex.getMessage()!=null?ex.getMessage():ex.toString()) + "\n Origin: " + sw.toString();
		showMessageDialog(message);
	}
	
	private void showMessageDialog(String message)
	{
		JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	// API references
	private PdfViewerComponent viewer;
	private PdfViewerComponent controller; //TODO remove
	// private IPdfTextSearch textSearch;

	// Application Window
	private JFrame frame;

	private PanelButton buttonOpen, buttonSave;
	private PanelButton buttonFirst, buttonNext, buttonPrevious, buttonLast;
	private PanelButton buttonZoomIn, buttonZoomOut, buttonFitPage, buttonFitWidth, buttonActualSize;
	private PanelButton buttonRotateRight;

	private JMenuItem itemOpen, itemSave, itemClose, itemExit;
	private JMenuItem itemTextSearch;
	private JMenuItem itemFirst, itemNext, itemPrevious, itemLast;
	private JMenuItem itemZoomIn, itemZoomOut;
	private JMenuItem itemRotateLeft, itemRotateRight;
	private JCheckBoxMenuItem itemIgnoreDocumentPreferences;

	private JCheckBoxMenuItem itemFitWidth, itemFitPage, itemActualSize;
	private JCheckBoxMenuItem itemPageLayoutSinglePage, itemPageLayoutTwoPageLeft, itemPageLayoutTwoPageRight,
			itemPageLayoutOneColumn, itemPageLayoutTwoColumnLeft, itemPageLayoutTwoColumnRight;
	private JCheckBoxMenuItem itemMoveMode, itemZoomMode, itemHighlightMode, itemSelectMode;
	private JCheckBoxMenuItem itemSidePanelOutlinesEnabled, itemSidePanelThumbnailsEnabled, itemSidePanelAnnotationsEnabled;
	private JFormattedTextField textFieldPageNo, textFieldZoom;

	private JLabel labelPageNo, labelPageCount;
	private JLabel labelZoomFactorStatus, labelZoomModeStatus;
	private JLabel labelPageNoStatus, labelPageCountStatus;
	private JLabel labelRotationStatus;
	private JLabel labelLayoutModeStatus;
	private JLabel labelPathStatus;
	private JLabel labelMouseModeStatus;
	
	private File currentFile;
	private String filename;
	private boolean firstTry = true;


	private final static Color COLOR_LIGHTGRAY = new Color(238, 238, 238);
	private final static Font COMMON_FONT = new Font("Verdana", Font.PLAIN, 12);

	// Space between labels on status bar
	private final static Dimension SPACE_STATUS_BAR = new Dimension(50, 0);

	private int pageCount;
	private JPasswordField passwordField;
	private Box passwordPane;

	// Format for zoom text field: Only 2 decimals
	private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
	private final static double MIN_ZOOM = 0.01;
	private final static double MAX_ZOOM = 100;
	private final static double ZOOM_STEP_FACTOR = 1.2;

	private enum NavigationType {
		FIRST, NEXT, PREVIOUS, LAST, SET_PAGE_NO
	}

	private enum ZoomFactorType {
		ZOOM_IN, ZOOM_OUT, SET_ZOOM
	}

	private enum RotationType {
		ROTATE_RIGHT, ROTATE_LEFT
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		controller.close();
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}
}
