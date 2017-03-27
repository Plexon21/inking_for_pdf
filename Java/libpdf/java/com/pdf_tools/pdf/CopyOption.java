/****************************************************************************
 *
 * File:            CopyOption.java
 *
 * Description:     PDF Copy Option Enumeration
 *
 * Author:          PDF Tools AG
 * 
 * Copyright:       Copyright (C) 2001 - 2016 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *
 ***************************************************************************/

package com.pdf_tools.pdf;

import com.pdf_tools.Flag;

public enum CopyOption implements Flag
{
    /**
     * Copy links (document internal and external links).
     */
    COPY_LINKS(0x001),
    /**
     * Copy interactive annotations such as sticky notes or highlight
     * annotations.
     */
    COPY_ANNOTATIONS(0x002),
    /**
     * Copy interactive form fields.
     * 
     * <p>
     * Note that when merging multiple documents with form fields, it is
     * important that no two different form fields have the same name. Otherwise
     * one of the fields must be renamed. Consider using
     * {@code FLATTEN_FORM_FIELDS} when merging multiple forms.
     * </p>
     */
    COPY_FORM_FIELDS(0x004),
    /**
     * Copy outlines.
     */
    COPY_OUTLINES(0x008),
    /**
     * Copy logical structure information.
     * 
     * <p>
     * Logical structure information in a PDF defines the structure of content,
     * such as titles, paragraphs, figures, reading order, tables or articles.
     * Logical structure elements can be "tagged" with descriptions or
     * alternative text. E.g. "tagging" allows the contents of an image to be
     * described to the visually impaired.
     * </p>
     * <p>
     * It is recommended to use this option, if all input documents are
     * "tagged". Otherwise this could be deactivated in order to create smaller
     * output files and get a much better performance. This option is required
     * for PDF/A level A conformance (e.g. PDF/A-1a, PDF/A-2a, PDF/A-3a).
     * </p>
     */
    COPY_LOGIGAL_STRUCTURE(0x010),
    /**
     * Copy named destinations.
     * 
     * <p>
     * A document may contain a mapping of names to destinations within the
     * document. These names can then be used in link annotations or outlines in
     * order to refer to destinations within the document.
     * </p>
     * 
     * <p>
     * Links within the document will work regardless of the state of this flag.
     * If {@code COPY_NAMED_DESTINATIONS} is not used, all named destinations of
     * the input document are removed and all internal named destinations
     * converted to regular destinations. This is much faster than copying named
     * destinations.
     * </p>
     * 
     * <p>
     * If a document is split into multiple documents with the intention of
     * merging the pieces back together at a later time, this flag should be
     * used. If the document uses named destinations links between the pieces
     * will work after merging if {@code COPY_NAMED_DESTINATIONS} is used.
     * </p>
     */
    COPY_NAMED_DESTINATIONS(0x020),

    /**
     * Flatten annotations preserves the visual appearance of annotations, but
     * discards all interactive elements.
     */
    FLATTEN_ANNOTATIONS(0x040),

    /**
     * Flatten form fields preserves the visual appearance of form fields, but
     * discards all interactive elements.
     */
    FLATTEN_FORM_FIELDS(0x080),

    /**
     * Flatten the visual appearance of signed signature fields.
     * 
     * <p>
     * A digital signature consists of two parts: First, a cryptographic part
     * that includes a hash value based on the content of the document that is
     * being signed. If the document is modified at a later time, the computed
     * hash value is no longer correct and the signature becomes invalid, i.e.
     * the validation will fail and will report that the document has been
     * modified since the signature has been applied. Second, an optional visual
     * appearance on a page of the PDF document. The signature appearance can be
     * useful to indicate the presence of a digital signature by a particular
     * signer.
     * </p>
     * <p>
     * Processing the PDF with 3-Heights™ PDF Toolbox API breaks the signature,
     * and therefore the cryptographic part needs to be removed. In general, the
     * visual appearance is regarded as worthless without the cryptographic
     * part, it is removed by default. The visual appearance can be preserved by
     * setting the flag {@code FLATTEN_SIGNATURE_APPEARANCES}.
     * </p>
     */
    FLATTEN_SIGNATURE_APPEARANCES(0x100),

    /**
     * Find and merge redundant resources from different input files. Equal
     * fonts, images and color spaces are detected. By activating this feature,
     * much smaller output files are created, if similar files are merged.
     * However, the merging process uses more time and memory resources.
     */
    OPTIMIZE_RESOURCES(0x200);

    CopyOption(int flag)
    {
        this.flag = flag;
    }

    public int getFlag()
    {
        return flag;
    }

    private final int flag;
}
