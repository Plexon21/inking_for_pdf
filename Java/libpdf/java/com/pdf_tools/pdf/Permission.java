/****************************************************************************
 *
 * File:            Permission.java
 *
 * Description:     PDF Permission Enumeration
 *
 * Author:          PDF Tools AG
 * 
 * Copyright:       Copyright (C) 2001 - 2016 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *
 ***************************************************************************/

package com.pdf_tools.pdf;

import com.pdf_tools.Flag;

public enum Permission implements Flag
{
    PRINT(0x00000004),
    MODIFY(0x00000008),
    COPY(0x00000010),
    ANNOTATE(0x00000020),
    FILL_FORMS(0x00000100),
    SUPPORT_DISABILITIES(0x00000200),
    ASSEMBLE(0x00000400),
    DIGITAL_PRINT(0x00000800);

    Permission(int flag)
    {
        this.flag = flag;
    }

    public int getFlag()
    {
        return flag;
    }

    private final int flag;
}
