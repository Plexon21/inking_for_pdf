package com.pdf_tools.pdfviewer.Model;

import java.util.List;

/**
 * @author fwe
 * Exception which passes information about issues opening and reading a pdf.
 */
/**
 * @author fwe
 *
 */
public class PdfViewerException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = 7972336167506966108L;
    private String message;

    /**
     * Create exception
     * 
     * @param message
     *            cause of exception
     */
    public PdfViewerException(String message)
    {
        this.message = message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage()
    {
        return message;
    }

    /**
     * @author fwe Exception indicating that an operation could not be executed,
     *         because there is no file open
     */
    public static class NoFileOpenedException extends PdfViewerException
    {
        private static final long serialVersionUID = -7611356292076531076L;

        public NoFileOpenedException()
        {
            super("No file opened to perform this operation");
        }
    }

    /**
     * @author fwe Exception indicating that there was an error for drawing the
     *         image of the pdf file
     */
    public static class GeneralDrawException extends PdfViewerException
    {
        private static final long serialVersionUID = -78416870602744270L;

        public GeneralDrawException(String message)
        {
            super(message);
        }
    }

    /**
     * @author fwe Exception indicating that the file that was requested to be
     *         opened could not be found
     */
    public static class PdfFileNotFoundException extends PdfViewerException
    {
        private static final long serialVersionUID = 8112096691738611681L;

        public PdfFileNotFoundException(String message)
        {
            super(message);
        }
    }

    /**
     * @author fwe Exception indicating that the opened file is password
     *         protected and no valid password has been provided
     */
    public static class PdfPasswordException extends PdfViewerException
    {
        private static final long serialVersionUID = 4334206169115389345L;

        public PdfPasswordException(String message)
        {
            super(message);
        }
    }

    /**
     * @author fwe Exception indicating that there was an internal caching error
     */
    public static class PageNotCachedException extends PdfViewerException
    {
        private static final long serialVersionUID = 661698484905384626L;

        public PageNotCachedException(List<Integer> missingPages, String message)
        {
            super(message);
            this.missingPages = missingPages;
        }

        public List<Integer> missingPages;
    }

    /**
     * @author fwe Exception indicating that the given license key is not valid
     */
    public static class PdfLicenseInvalidException extends PdfViewerException
    {
        private static final long serialVersionUID = 9013387892314346488L;

        public PdfLicenseInvalidException(String message)
        {
            super(message);
        }
    }
    
    /**
     * @author pgl
     * Exception when saving failed due to no file access
     */
    public static class PdfNoFileAccessException extends PdfViewerException
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public PdfNoFileAccessException(String message)
        {
            super(message);
        }
    }

    /**
     * @author fwe Exception indicating that the opened file has no valid pdf
     *         header and is thus either corrupt or not a pdf file at all
     */
    public static class PdfFileCorruptException extends PdfViewerException
    {
        private static final long serialVersionUID = -7088022809740319555L;

        public PdfFileCorruptException(String message)
        {
            super(message);
        }
    }

    /**
     * @author fwe Exception indicating that the opened pdf tries to use
     *         features that are not yet supported by the rendering engine
     */
    public static class PdfUnsupportedFeatureException extends PdfViewerException
    {
        private static final long serialVersionUID = 3860519604963591630L;

        public PdfUnsupportedFeatureException(String message)
        {
            super(message);
        }
    }
}
