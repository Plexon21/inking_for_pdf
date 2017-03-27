package com.pdf_tools;

public interface Stream
{
    long getLength() throws java.io.IOException;

    boolean seek(long position) throws java.io.IOException;

    long tell() throws java.io.IOException;

    int read(byte[] buffer, int offset, int length) throws java.io.IOException;

    void write(byte[] buffer, int offset, int length) throws java.io.IOException;
}
