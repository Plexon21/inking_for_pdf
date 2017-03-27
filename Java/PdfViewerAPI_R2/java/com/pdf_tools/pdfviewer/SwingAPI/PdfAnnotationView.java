package com.pdf_tools.pdfviewer.SwingAPI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation;
import com.pdf_tools.pdfviewer.Annotations.APdfMarkupAnnotation;
import com.pdf_tools.pdfviewer.Model.IPdfControllerCallbackManager.IAnnotationViewerListener;
import com.pdf_tools.pdfviewer.converter.Converter;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.IOnAnnotationDeletedListener;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.IOnAnnotationUpdatedListener;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.IOnCloseCompletedListener;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.IOnOpenCompletedListener;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.IOnVisiblePageRangeChangedListener;
import com.pdf_tools.pdfviewer.Model.PdfViewerController;
import com.pdf_tools.pdfviewer.Model.PdfViewerException;

public class PdfAnnotationView extends JPanel implements IAnnotationViewerListener, IOnAnnotationDeletedListener, IOnCloseCompletedListener,
        IOnAnnotationUpdatedListener, IOnOpenCompletedListener, IOnVisiblePageRangeChangedListener, ChangeListener, ActionListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JTextArea textArea;
    private JPanel buttonPanel;
    private JScrollPane scrollPane;
    private JButton okButton;
    private PdfViewerController controller;
    private APdfMarkupAnnotation annotation;

    public PdfAnnotationView(PdfViewerController controller)
    {
        this.controller = controller;
        controller.registerOnCloseCompleted(this);
        controller.registerOnOpenCompleted(this);
        controller.registerOnVisiblePageRangeChanged(this);
        controller.registerOnTextAnnotationClicked(this);
        controller.registerOnNoAnnotationSelected(this);
        controller.registerOnAnnotationDeleted(this);
        controller.registerOnAnnotationUpdated(this);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setMinimumSize(new Dimension(200, 1));

        okButton = new JButton("Save");
        okButton.addActionListener(this);
        buttonPanel = new JPanel();
        buttonPanel.add(okButton);

        textArea = new JTextArea(14, 1);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(true);
        textArea.setMargin(new Insets(1, 3, 1, 3));

        textArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 5), "author"));

        scrollPane = new JScrollPane(textArea);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 1200));

        this.add(scrollPane);
        this.add(buttonPanel);

        scrollPane.setVisible(false);
        buttonPanel.setVisible(false);

    }

    public void setController(PdfViewerController _controller)
    {
        controller = _controller;
        controller.registerOnCloseCompleted(this);
        controller.registerOnOpenCompleted(this);
        controller.registerOnVisiblePageRangeChanged(this);
        controller.registerOnTextAnnotationClicked(this);
        controller.registerOnNoAnnotationSelected(this);
        controller.registerOnAnnotationDeleted(this);
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        // TODO Auto-generated method stub
        // textArea.setSize(getWidth()-10, 20);
        // panel.updateUI();
    }

    @Override
    public void onAnnotationDeleted(int page)
    {
        this.annotation = null;
        clearAndHideUI();
    }

    @Override
    public void onVisiblePageRangeChanged(int firstPage, int lastPage)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onOpenCompleted(PdfViewerException ex)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCloseCompleted(PdfViewerException ex)
    {
        this.annotation = null;
        clearAndHideUI();
    }

    public void clearAndHideUI()
    {
        textArea.setText(null);
        textArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 5), "author"));
        scrollPane.setVisible(false);
        buttonPanel.setVisible(false);
    }

    @Override
    public void onMarkupAnnotationClicked(APdfMarkupAnnotation annotation)
    {
        // if it's a text annotation it has a content which we can
        // display in the textarea
        scrollPane.setVisible(true);
        buttonPanel.setVisible(true);
        if (this.annotation != annotation)
        {
            this.annotation = annotation;
            textArea.setText(annotation.getContent());
            textArea.setBorder(
                    BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Converter.createAWTColor(annotation.getColor()), 5), annotation.getAuthor()));
            textArea.setCaretPosition(0);
        }
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (annotation == null)
            return;

        annotation.setContent(textArea.getText());
        try
        {
            controller.udpateAnnotation(annotation);
        } catch (PdfViewerException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        onMarkupAnnotationClicked(this.annotation);
    }

    @Override
    public void onNoAnnotationSelected()
    {
        this.annotation = null;
        clearAndHideUI();
    }

    @Override
    public void onAnnotationUpdated(APdfAnnotation annotation)
    {
        if (annotation == this.annotation)
        {
            textArea.setText(null);
            textArea.setText(((APdfMarkupAnnotation) annotation).getContent());
            textArea.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Converter.createAWTColor(annotation.getColor()), 5), 
                                                   annotation.getAuthor()));
        }

    }

}
