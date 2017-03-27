// -----------------------------------------------------------------------
// <copyright file="DebugLogger.cs" company="">
// TODO: Update copyright text.
// </copyright>
// -----------------------------------------------------------------------

namespace PdfTools.PdfViewerCSharpAPI.Utilities
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;
    using System.IO;
    using System.Diagnostics;

    /// <summary>
    /// TODO: Update summary.
    /// </summary>
    public static class Logger
    {
        private static bool autoFlush = true;
        private static String filePath = null;
        private static StringBuilder log = new StringBuilder();


        public static String FileName
        {
            set
            {
                filePath = value;
                using (StreamWriter s = File.CreateText(filePath))
                {
                    s.WriteLine("Time, Type, Message, Calling class, calling method");
                    
                    
                }
                LogInfo("Enabled Logging to logfile " + value);
            }
            get
            {
                return filePath;
            }
        }

        public static void LogInfo(String message, int up = 0)
        {
            Log(message, "Info", 1 + up);
        }
        public static void LogWarning(String message, int up = 0)
        {
            Log(message, "Warning", 1 + up);
        }
        public static void LogError(String message, int up = 0)
        {
            Log(message, "Error", 1 + up);
        }
        public static void LogException(Exception ex, int up = 0)
        {
            Log("\"" + ex.Message + "\" " + ex.ToString(), "Exception", 1 + up);
        }

        public static string ListToString<T>(ICollection<T> list) 
        {
            StringBuilder b = new StringBuilder();
            foreach (T obj in list)
            {
                IFormattable ifmt = obj as IFormattable;
                b.Append(String.Format("{0}{1}", (b.Length > 0) ? ", " : "" , obj));
            }
            return b.ToString();
        }

        private static void Log(String message, String type, int up = 0)
        {
            if (filePath == null)
                return;
            StackTrace stackTrace = new StackTrace();
            String callerMethod = stackTrace.GetFrame(1 + up).GetMethod().Name;
            String callerClass = stackTrace.GetFrame(1 + up).GetMethod().ReflectedType.Name;

            lock(log){
                log.AppendFormat("{0}, {1}, {2}, {3}\n",
                              DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss"),
                              type,
                              message.Replace(',',';'),
                              callerClass, callerMethod);
                if (autoFlush)
                {
                    using (StreamWriter s = File.AppendText(filePath))
                    {
                        s.WriteLine(log.ToString());
                    }
                    log.Clear();
                }
            }
        }

        public static void Log(String line)
        {
            Log(line, "-");
        }

        public static void SaveLog()
        {
            if (filePath == null)
                return;
            using (StreamWriter outfile = new StreamWriter(filePath))
            {
                lock (log)
                {
                    outfile.Write(log.ToString());
                }
            }
        }
    }
}
