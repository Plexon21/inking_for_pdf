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
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.*;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;
import com.pdf_tools.pdfviewer.SwingAPI.PdfViewerComponent;

/**
 * @author fwe A runnable that gets executed in the dispatcher thread
 */
public final class PdfMinimalViewer implements IOnOpenCompletedListener, WindowListener {

	public static void main(String[] args) {
		new PdfMinimalViewer(args);
	}

	PdfMinimalViewer(String[] args) {
		
		// GUI setup
		frame = new JFrame("Pdf Viewer");
		frame.addWindowListener(this);
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setPreferredSize(new Dimension((int) (0.5 * screenSize.width), (int) (0.7 * screenSize.height)));
		
		// Create PdfViewer and register this class for receiving callbacks from
		// the PdfViewer
		viewer = new PdfViewerComponent(true, frame);
		controller = viewer;
		try {
			controller.setPageLayoutMode(TPageLayoutMode.OneColumn);
		} catch (Exception e) {
			exceptionOccured(e);
			return;
		}


		addMenuBar();
		addPdfViewerComponent();

		// register callbacks
		controller.registerOnOpenCompleted(this);

		frame.pack();
		frame.setVisible(true);
	}

	private void addMenuBar() {
		itemOpen = new JMenuItem(new OpenAction("Open", KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK)));
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.add(itemOpen);
		menuBar.add(menu);
		frame.setJMenuBar(menuBar);
	}

	private void addPdfViewerComponent() {
		frame.getContentPane().add(viewer, BorderLayout.CENTER);
	}

	public void onOpenCompleted(PdfViewerException ex) {
		if (ex != null) {
				exceptionOccured(ex);
				return;
		}
	}
	/**
	 * Action performed for open request
	 * 
	 * @author cha
	 *
	 */
	private class OpenAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1425165899265917385L;

		OpenAction(String description, KeyStroke keyStroke) {
			this.putValue(NAME, description);
			if (keyStroke != null)
				this.putValue(ACCELERATOR_KEY, keyStroke);
		}

		public void actionPerformed(ActionEvent e) {
			File currentFile = null;
			final JFileChooser fc = new JFileChooser(currentFile);
			if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
				currentFile = fc.getSelectedFile();
				filename = currentFile.getAbsolutePath();
				password = null;
				try {
					controller.open(filename, password);
				} catch (PdfViewerException ex) {
					exceptionOccured(ex);
				}
			}
		}
	}

	private void exceptionOccured(Exception ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		JOptionPane.showMessageDialog(frame,
				"Exception occured: " + (ex.getMessage() != null ? ex.getMessage() : ex.toString()) + "\n Origin: "
						+ sw.toString(), "Error", JOptionPane.ERROR_MESSAGE);
	}

	// API references
	private PdfViewerComponent viewer;
	private PdfViewerComponent controller;

	// Application Window
	private JFrame frame;

	private JMenuItem itemOpen;

	private String filename;
	private char[] password;

	public void windowClosing(WindowEvent e) {
		controller.close();
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}
}
