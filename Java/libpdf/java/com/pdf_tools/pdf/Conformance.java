/****************************************************************************
 *
 * File:            Conformance.java
 *
 * Description:     PDF Conformance Enumeration
 *
 * Author:          PDF Tools AG
 * 
 * Copyright:       Copyright (C) 2001 - 2016 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *
 ***************************************************************************/

package com.pdf_tools.pdf;

import java.util.HashMap;

public enum Conformance
{
    UNKNOWN(0x0000),
    PDF_1_0(0x1000),
    PDF_1_1(0x1100),
    PDF_1_2(0x1200),
    PDF_1_3(0x1300),
    PDF_1_4(0x1400),
    PDF_1_5(0x1500),
    PDF_1_6(0x1600),
    PDF_1_7(0x1700),
    PDFA_1B(0x1401),
    PDFA_1A(0x1402),
    PDFA_2B(0x1701),
    PDFA_2U(0x1702),
    PDFA_2A(0x1703),
    PDFA_3B(0x1711),
    PDFA_3U(0x1712),
    PDFA_3A(0x1713);

    Conformance(int value)
    {
        this.value = value;
    }

    static Conformance fromValue(int value)
    {
        Conformance conformance = valueMap.get(value);
        if (conformance == null)
            throw new IllegalArgumentException("Illegal Conformance value " + value);

        return conformance;
    }

    public int getValue()
    {
        return value;
    }

    private int value;

    private static final HashMap<Integer, Conformance> valueMap = new HashMap<Integer, Conformance>();

    static
    {
        for (Conformance c : Conformance.values())
            valueMap.put(c.getValue(), c);
    }

}
