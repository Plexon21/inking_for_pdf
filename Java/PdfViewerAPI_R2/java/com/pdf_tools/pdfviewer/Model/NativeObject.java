package com.pdf_tools.pdfviewer.Model;

import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation;
import com.pdf_tools.pdfviewer.Annotations.APdfAnnotation.TPdfAnnotationType;
import com.pdf_tools.pdfviewer.converter.geom.Rectangle;
import com.pdf_tools.pdfviewer.Annotations.PdfGenericAnnotation;

public abstract class NativeObject
{

    static
    {
        System.loadLibrary("PdfViewerAPI");
        try
        {
            initialize();
        } catch (UnsatisfiedLinkError ue)
        {
            System.loadLibrary("PdfViewerAPI");
            initialize();
        }
    }

    protected static PdfViewerException getLastException()
    {
        String message = getLastErrorMessage();
        TViewerError errorCode = TViewerError.values()[getLastError()];
        switch (errorCode)
        {
        case eLicenseError:
            return new PdfViewerException.PdfLicenseInvalidException(message);
        case eFileNotFoundError:
            return new PdfViewerException.PdfFileNotFoundException(message);
        case eFileCorruptError:
            return new PdfViewerException.PdfFileCorruptException(message);
        case ePasswordError:
            return new PdfViewerException.PdfPasswordException(message);
        case eIllegalArgumentError:
            throw new IllegalArgumentException(message);
        case eUnsupportedFeatureError:
            return new PdfViewerException.PdfUnsupportedFeatureException(message);
        case eNoFileAccess:
            return new PdfViewerException.PdfNoFileAccessException(message);
        default:
            return new PdfViewerException("Unknown Error when trying to open File. Message: \"" + message + "\"");
        }
    }

    protected enum TViewerError
    {
        eLicenseError(0), ePasswordError(1), eFileNotFoundError(2), eUnknownError(3), eIllegalArgumentError(4), eOutOfMemoryError(
                5), eFileCorruptError(6), eUnsupportedFeatureError(7), eNoFileAccess(8);
        private final int id;

        TViewerError(int id)
        {
            this.id = id;
        }

        public int getValue()
        {
            return id;
        }
    }

    // textFragment
    protected native void releaseTextFragment(PdfTextFragment fragment);

    // document:
    protected native long createObject(String filename, String password);

    protected native void destroyObject(long _handle);

    protected native int getPageCount(long _handle);

    protected native Rectangle.Double getPageRect(long _handle, int pageNo);
    
    protected native int getRotation(long _handle, int pageNo);

    protected native boolean draw(long _handle, int pageNo, int width, int height, byte[] buffer, int rotation, int targetRectX,
            int targetRectY, int targetRectWidth, int targetRectHeight, double sourceRectX, double sourceRectY, double sourceRectWidth,
            double sourceRectHeight);

    protected native int getPageLayout(long _handle);

    protected native int getOpenActionDestination(long _handle, int[] page, Double[] dimensions);

    protected native PdfOutlineItem[] getOutlineItems(long _handle, int parentId);

    protected native PdfTextFragment[] getTextFragments(long _handle, int pageNo);

    protected native PdfGenericAnnotation[] getAnnotationsOnPage(long _handle, int pageNo);

    protected native int updateAnnotation(long _handle, APdfAnnotation annotation);

    protected native PdfGenericAnnotation createAnnotation(long _handle, TPdfAnnotationType type, int pageNo, Double[] rect);

    protected native void deleteAnnotation(long _handle, long annotHandle, int page);

    protected native boolean saveAs(long _handle, String path);

    // controller:
    protected static native void initialize();

    protected static native boolean setLicenseKey(String s);

    protected static native boolean getLicenseIsValid();

    protected static native String getProductVersion2();

    // both:
    protected static native int getLastError();

    protected static native String getLastErrorMessage();
}
