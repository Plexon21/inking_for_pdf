package com.pdftools;

import java.util.LinkedList;
import java.util.ListIterator;

public class MemoryStream implements Stream
{
    /**
     * Create a new memory stream
     * @param initialCapacity The initial capacity of the stream. The length of the stream is still 0
     * @param blockSize The size of the memory blocks used to store the data
     */
    public MemoryStream(long initialCapacity, int blockSize)
    {
        this.blockSize = blockSize;
        this.list = new LinkedList();
        this.setMinCapacity(initialCapacity);
    }

    /**
     * Create a new memory stream with block size 8192 Bytes
     * @param initialCapacity The initial capacity of the stream. The length of the stream is still 0
     */
    public MemoryStream(long initialCapacity)
    {
        this(initialCapacity, 8192);
    }

    /**
     * Create a new memory stream with initial capacity 0 and block size 8192 Bytes
     */
    public MemoryStream()
    {
        this(0);
    }

    /**
     * Create a new memory stream by copying from a buffer
     * @param buffer The buffer from which the initial data is copied
     * @param offset The offset where the first byte from the buffer is copied
     * @param length The number of bytes that are copied from the buffer
     * @param blockSize The size of the memory blocks used to store the data
     */
    public MemoryStream(byte[] buffer, int offset, int length, int blockSize)
    {
        this(length, blockSize);
        this.write(buffer, offset, length);
    }

    /**
     * Create a new memory stream with block size 8192 by copying from a buffer
     * @param buffer The buffer from which the initial data is copied
     * @param offset The offset where the first byte from the buffer is copied
     * @param length The number of bytes that are copied from the buffer
     */
    public MemoryStream(byte[] buffer, int offset, int length)
    {
        this(buffer, offset, length, 8192);
    }

    /**
     * Create a new memory stream by copying a stream
     * @param stream The stream from which the initial data is copied
     * @param blockSize The size of the memory blocks used to store the data
     */
    public MemoryStream(Stream stream, int blockSize) throws java.io.IOException
    {
        this(stream.getLength(), blockSize);
        byte[] buffer = new byte[blockSize];
        int read;
        while ((read = stream.read(buffer, 0, blockSize)) > 0)
            write(buffer, 0, read);
    }

    /**
     * Create a new memory with block size 8192 stream by copying a stream
     * @param stream The stream from which the initial data is copied
     */
    public MemoryStream(Stream stream) throws java.io.IOException
    {
        this(stream, 8192);
    }

    /**
     * Get the length of the stream in bytes
     * @return the length of the stream  in bytes
     */
    public long getLength()
    {
        return this.length;
    }

    /**
     * Set byte position
     * @param position The new position of the stream (-1 for EOS)
     * @return true if successful
     */
    public boolean seek(long position)
    {
        this.position = position;
        return true;
    }

    /**
     * Get current byte position
     * @return byte position, -1 if position unknown
     */
    public long tell()
    {
        return this.position;
    }

    /**
     * Read from the stream
     * @param buffer The buffer where the data is written
     * @param offset The starting element in the buffer
     * @param length The maximum number of bytes to be read
     * @return The actual number of bytes read (-1 if EOS)
     */
    public int read(byte[] buffer, int offset, int length)
    {
        if (buffer == null)
            throw new NullPointerException();

        if (offset < 0 || offset + length > buffer.length)
            throw new IndexOutOfBoundsException();

        // EOS
        if (this.position == this.length)
            return -1;

        ListIterator it = this.list.listIterator((int)(this.position / this.blockSize));
        int currPos = offset;
        while (length > 0 && it.hasNext() && this.position < this.length)
        {
            int blockPos = (int)(this.position % this.blockSize);
            int currLength = Math.min(length, Math.min(this.blockSize - blockPos, (int)(this.length - this.position)));
            System.arraycopy(it.next(), blockPos, buffer, currPos, currLength);
            currPos += currLength;
            this.position += currLength;
            length -= currLength;
        }
        int read = currPos - offset;
        return read;
    }

    /**
     * Write to the stream
     * @param buffer The buffer where the data lies
     * @param offset The starting element in the buffer
     * @param length The maximum number of bytes to be written
     */
    public void write(byte[] buffer, int offset, int length)
    {
        if (buffer == null)
            throw new NullPointerException();

        if (offset < 0 || offset + length > buffer.length)
            throw new IndexOutOfBoundsException();

        this.setMinLength(this.position + length);

        ListIterator it = this.list.listIterator((int)(this.position / this.blockSize));
        int currPos = offset;
        while (length > 0 && it.hasNext())
        {
            int blockPos = (int)(this.position % this.blockSize);
            int currLength = Math.min(length, this.blockSize - blockPos);
            System.arraycopy(buffer, currPos, it.next(), blockPos, currLength);
            currPos += currLength;
            this.position += currLength;
            length -= currLength;
        }
    }

    private boolean setMinLength(long number)
    {
        if (number < 0)
            return false;

        if (!this.setMinCapacity(number))
            return false;

        if (this.length < number)
            this.length = number;

        return true;
    }

    private boolean setMinCapacity(long number)
    {
        if (number < 0)
            return false;

        return this.setMinBlockNumber((int)((number + this.blockSize - 1) / this.blockSize));
    }

    private boolean setMinBlockNumber(int number)
    {
        if (number < 0)
            return false;

        for (int change = number - this.list.size(); change > 0; change--)
            this.list.addLast(new byte[this.blockSize]);

        return true;
    }

    private LinkedList list = null;
    private long length = 0;
    private long position = 0;
    int blockSize;
}
