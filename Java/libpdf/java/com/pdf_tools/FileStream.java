package com.pdf_tools;

public class FileStream implements Stream
{
    /**
     * Create a new file stream from a file
     * This constructor works exactly like the same constructor of the underlying java.io.RandomAccessFile
     * 
     * ATTENTION: Opening the file in "rw" mode does not delete the current content of the file,
     * so be sure that you delete it yourself if needed (which is mostly the case).
     * 
     * @param file The file to be opened
     * @param mode The open mode of the file
     * @see java.io.RandomAccessFile#RandomAccessFile(java.io.File, String)
     */
    public FileStream(java.io.File file, String mode) throws java.io.FileNotFoundException
    { raFile = new java.io.RandomAccessFile(file, mode); }

    /**
     * Create a new file stream from a filename
     * This constructor works exactly like the same constructor of the underlying java.io.RandomAccessFile
     * 
     * ATTENTION: Opening the file in "rw" mode does not delete the current content of the file,
     * so be sure that you delete it yourself if needed (which is mostly the case).
     * 
     * @param filename The filename of the file to be opened
     * @param mode The open mode of the file
     * @see java.io.RandomAccessFile#RandomAccessFile(String, String)
     */
    public FileStream(String filename, String mode) throws java.io.FileNotFoundException
    { raFile = new java.io.RandomAccessFile(filename, mode); }

    /**
     * Create a new file stream from a RandomAccessFile
     * 
     * ATTENTION: Opening a RandomAccessFile in "rw" mode does not delete the current content of the file,
     * so be sure that you delete it yourself if needed (which is mostly the case).
     * 
     * @param raFile The underlying RandomAccessFile
     * @see java.io.RandomAccessFile
     */
    public FileStream(java.io.RandomAccessFile raFile)
    { this.raFile = raFile; }

    /**
     * Get the underlying RandomAccessFile
     * @return The underlying RandomAccessFile
     */
    public java.io.RandomAccessFile GetRandomAccessFile()
    { return raFile; }

    /**
     * Get the length of the stream in bytes
     * @return the length of the stream  in bytes
     */
    public long getLength() throws java.io.IOException
    { return raFile.length(); }

    /**
     * Set byte position
     * @param position The new position of the stream (-1 for EOS)
     * @return true if successful
     */
    public boolean seek(long position) throws java.io.IOException
    { if (position >= 0)raFile.seek(position); else raFile.seek(raFile.length()); return true; }

    /**
     * Get current byte position
     * @return byte position, -1 if position unknown
     */
    public long tell() throws java.io.IOException
    { return raFile.getFilePointer(); }

    /**
     * Read from the stream
     * @param buffer The buffer where the data is written
     * @param offset The starting element in the buffer
     * @param length The maximum number of bytes to be read
     * @return The actual number of bytes read (-1 if EOS)
     */
    public int read(byte[] buffer, int offset, int length) throws java.io.IOException
    { return raFile.read(buffer, offset, length); }

    /**
     * Write to the stream
     * @param buffer The buffer where the data lies
     * @param offset The starting element in the buffer
     * @param length The maximum number of bytes to be written
     */
    public void write(byte[] buffer, int offset, int length) throws java.io.IOException
    { raFile.write(buffer, offset, length); }

    /**
     * Copy the content of another stream
     * @param stream The stream of which the content is copied
     */
    public void copy(Stream stream) throws java.io.IOException
    {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = stream.read(buffer, 0, 8192)) > 0)
            write(buffer, 0, read);
    }

    /**
     * Close the file stream
     * This closes also the underlying RandomAccessFile
     */
    public void close() throws java.io.IOException
    { raFile.close(); }

    private java.io.RandomAccessFile raFile = null;
}
