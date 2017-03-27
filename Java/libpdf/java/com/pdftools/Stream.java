package com.pdftools;


/**
 * Unified stream interface for reading and writing
 */
public interface Stream
{
    /**
     * Get the length of the stream in bytes
     * @return the length of the stream  in bytes
     */
    long getLength() throws java.io.IOException;

    /**
     * Set byte position
     * @param position The new position of the stream (-1 for EOS)
     * @return true if successful
     */
    boolean seek(long position) throws java.io.IOException;

    /**
     * Get current byte position
     * @return byte position, -1 if position unknown
     */
    long tell() throws java.io.IOException;

    /**
     * Read from the stream
     * @param buffer The buffer where the data is written
     * @param offset The starting element in the buffer
     * @param length The maximum number of bytes to be read
     * @return The actual number of bytes read (-1 if EOS)
     */
    int read(byte[] buffer, int offset, int length) throws java.io.IOException;

    /**
     * Write to the stream
     * @param buffer The buffer where the data lies
     * @param offset The starting element in the buffer
     * @param length The maximum number of bytes to be written
     */
    void write(byte[] buffer, int offset, int length) throws java.io.IOException;
}
