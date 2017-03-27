package com.pdf_tools.pdfviewer.Model;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class DebugLogger
{

    private static boolean logging = false;
    private static boolean started = false;
    private static Writer writer;

    public static synchronized void log(String s)
    {
        if (logging)
        {
            if (!started)
            {
                try
                {
                    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("DebugLog.log"), "utf-8"));
                } catch (UnsupportedEncodingException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (FileNotFoundException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                started = true;
            }
            try
            {
                writer.write(s + "\n");
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static synchronized void saveLog()
    {
        try
        {
            writer.close();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
