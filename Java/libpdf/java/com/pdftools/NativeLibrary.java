package com.pdftools;

/**
 * The NativeLibrary class provides the basic enums for the functions in the java package.
 */
public class NativeLibrary
{	
    public static final String VERSION = "4.9.0.0";

    public static class DITHERINGMODE
    {    
        public final static int eDitherNone           = 0;
        public final static int eDitherFloydSteinberg = 1; 
        public final static int eDitherHalftone       = 2;
        public final static int eDitherPattern        = 3;
        public final static int eDitherG3Optimized    = 4;
        public final static int eDitherG4Optimized    = 5;
        public final static int eDitherAtkinson       = 6;
    }
	
	public static class DITHERING
    {    
        public final static int eDitheringNone           = 0;
        public final static int eDitheringFloydSteinberg = 1; 
        public final static int eDitheringHalftone       = 2;
        public final static int eDitheringPattern        = 3;
        public final static int eDitheringG3Optimized    = 4;
        public final static int eDitheringG4Optimized    = 5;
        public final static int eDitheringAtkinson       = 6;
    }
    
    public static class ROTATEMODE
    {    
        /**
         * Do not rotate the page; do not consider the viewing rotation
         * attribute of the PDF page.
         */
        public final static int eRotateNone      = 0;
        /**
         * Set the rotation to the viewing rotation attribute of the PDF page,
         * i.e. rendering the page with the same rotation as it is displayed in
         * a PDF Viewer.
         */
        public final static int eRotateAttribute = 1;
        /** Rotate page to portrait. */
        public final static int eRotatePortrait  = 2;
        /** Rotate page to landscape. */
        public final static int eRotateLandscape = 3;
    }
    
    public static class RENDEREROPTION
    {
        /** Convert bitmaps to the JPEG format if supported.   */
        public final static int eOptionJPEG             = 0x00000001;
        /** Convert bitmaps to the PNG format if supported.    */
        public final static int eOptionPNG              = 0x00000002;
        /** Deprecated. */
        public final static int eOptionTransparency     = 0x00000004;
        /** Use banding to split bitmaps into 64k pieces.      */
        public final static int eOptionBanding          = 0x00000008;
        /** Convert simple OpenType to Type1 fonts.            */
        public final static int eOptionType1            = 0x00000010;
        /** Send a pre-rendered bitmap to the physical device. */
        public final static int eOptionBitmap           = 0x00000020;
        /** Convert CFF and Type1 fonts to TrueType.           */
        public final static int eOptionTrueType         = 0x00000040;
        /** Use high quality rendering.                        */
        public final static int eOptionHighQuality      = 0x00000080;
        /** Use bilinear interpolation mode.                   */
        public final static int eOptionBilinear         = 0x00000100;
        /** Use bicubic interpolation mode.                    */
        public final static int eOptionBicubic          = 0x00000200;
        /** Don't use embedded fonts.                          */
        public final static int eOptionNoEmbedded       = 0x00000400;
        /** Use Windows 9x compatible printer driver interface. */
        public final static int eOptionWindows9x        = 0x00000800;
        /** Convert CFF and Type1 fonts to graphics outlines (GDI+ only). */
        public final static int eOptionOutlines         = 0x00001000;
        /** Convert Type1 to OpenType fonts.  */
        public final static int eOptionOpenType         = 0x00002000;
        /** Disable PostScript injection. */
        public final static int eOptionDisablePS        = 0x00004000;
        /** Disable patterns; use light gray instead. */
        public final static int eOptionDisablePatterns  = 0x00008000;
        /** Use fast mode image rendering even in accurate mode. */
        public final static int eOptionUseFastImages    = 0x00010000;
        /** Do not print digital signature appearances. */
        public final static int eOptionDoNotPrintSig    = 0x00020000;
        /** Print the signature appearance only (without any status appearances). */
        public final static int eOptionPrintOnlySig     = 0x00040000;
        /** Use pre-installed fonts wherever possible. */
        public final static int eOptionPreInstalled     = 0x00080000;
        /** Disable transparency backbuffer. */
        public final static int eOptionDisableBuffer    = 0x00100000;
        /** Use Unicodes instead of GlyphIds. */
        public final static int eOptionUseUnicodes      = 0x00200000;
        /** Draw popup annotations. */
        public final static int eOptionDrawPopups       = 0x00400000;
        /** Disable black point compensation (BPC) */
        public final static int eOptionDisableBPC       = 0x00800000;
        /** Convert line strokes to a filled path */
        public final static int eOptionFillStrokes      = 0x01000000;
        /** Disable the drawing of annotations (including Popups) */
        public final static int eOptionDisableAnnots    = 0x02000000;
        /** Disable the drawing of the page content and draw annotations only */
        public final static int eOptionDisableContent   = 0x04000000;
        /** Enable the blending of overprinted colorants */
        public final static int eOptionEnableOverprint  = 0x08000000;
        /** Simulate printing */
        public final static int eOptionPrint            = 0x10000000;
        /** Don't paint white background. */
        public final static int eOptionNoBackground     = 0x20000000;
        /** Use PostScript language level 2 (printing only, default is 3). */
        public final static int eOptionPSLevel2         = 0x20000000;
        /** Disable image filtering. */
        public final static int eOptionDisableFilter    = 0x40000000;
        /** Automatically switch to accurate mode if necessary, e.g. to render transparent content. */
        public final static int eOptionAutoAccurateMode = 0x80000000;

    } 
    
    public static class RENDEREROPTION2
    {
        /** Don't anti-aliasing in rasterization. */
        public final static int eOptionNoAntialiasing   = 0x00000001;
        /** Don't use interplation filtering for images. */
        public final static int eOptionNoInterpolation  = 0x00000002;
        /** Don't use low-pass filtering for images. */
        public final static int eOptionNoLowPassFilter  = 0x00000004;
        /** Don't use hinting for glyph rendering. */
        public final static int eOptionNoHinting        = 0x00000008;
        /** Render in printing mode. */
        public final static int eOptionPrintingMode     = 0x00000010;
        /** Don't use black point compensation (BPC). */
        public final static int eOptionNoBPC            = 0x00000020;
        /** Fit clipping paths to pixel grid. */
        public final static int eOptionFitPaths         = 0x00000040;
        /** Use a box filter instead of a Gauss filter. */
        public final static int eOptionUseBoxFilter     = 0x00000080;
    }
        
    public static class RENDERINGMODE
    {    
        /**
         * The fast mode uses the Windows GDI for rendering. This mode is
         * generally faster than the accurate mode. Use this mode for high
         * resolution (600 dpi).
         */
        public final static int eModeFast     = 0;
        /**
         * The accurate mode uses the Windows GDI+ for rendering. This mode
         * allows for image filtering, sub-pixel rendering and anti-aliasing.
         */
        public final static int eModeAccurate = 1;
        /** This mode is deprecated. */
        public final static int eModeDirect   = 2;
    }
    
    
    public static class COMPRESSION
    {
        /** No compression (raw).                     */
        public final static int eComprRaw       = 0;
        /** Lossy DCT (Discrete Cosine Transform, JPEG) compression. */
        public final static int eComprJPEG      = 1;
        /** Lossless flate (ZIP) compression.         */
        public final static int eComprFlate     = 2;
        /** Lossless LZW (Lempel-Ziff-Welch) compression. */
        public final static int eComprLZW       = 3;
        /** Lossless CCITT Fax Group3 compression.   */
        public final static int eComprGroup3    = 4;
        /** Lossless CCITT Fax Group3 (2D) compression. */
        public final static int eComprGroup3_2D = 5;
        /** Lossless CCITT Fax Group4 compression.   */
        public final static int eComprGroup4    = 6;
        /** Lossless JBGI2 compression.              */
        public final static int eComprJBIG2     = 7;
        /** Lossy JPEG2000 compression.              */
        public final static int eComprJPEG2000  = 8;
        /** Lossy TIFF embedded JPEG (6) compression */
        public final static int eComprTIFFJPEG  = 9;
        public final static int eComprUnknown   = 10;
        public final static int eComprDefault   = 11;
    } 
    
    public static class COLORSPACE 
    {
        /** One channel gray. */
        public final static int eColorGray      = 0;
        /** Two channels gray and alpha. */
        public final static int eColorGrayA     = 1;
        /** Three channels red, green, blue. */
        public final static int eColorRGB       = 2;
        /** Four channels red, green, blue, alpha. */
        public final static int eColorRGBA      = 3;
        /** Four channels cyan, magenta, yellow, black. */
        public final static int eColorCMYK      = 4;
        /** Three channels Luminance (Y) and Chroma (Cb, Cr) */
        public final static int eColorYCbCr     = 5;
        /** Four channels Luminance (Y), Chroma (Cb, Cr) and black. */
        public final static int eColorYCbCrK    = 6;
        /** One channel palette indices (into an RGB color table). */
        public final static int eColorPalette   = 7;
        /** Three channels CIE LAB. */
        public final static int eColorLAB       = 8;
        /** Four channels CMYK where only K is used. */
        public final static int eColorCMYK_Konly= 9;
        /** Five channels CMYK including alpha. */
        public final static int  eColorCMYKA    = 10;
        /** Not one of the above. */
        public final static int eColorOther     = 11;
    }
    
    public static class ORIENTATION 
    {
        public final static int eOrientationUndef       = 0;
        public final static int eOrientationTopLeft     = 1; 
        public final static int eOrientationTopRight    = 2; 
        public final static int eOrientationBottomRight = 3; 
        public final static int eOrientationBottomLeft  = 4;
        public final static int eOrientationLeftTop     = 5; 
        public final static int eOrientationRightTop    = 6; 
        public final static int eOrientationRightBottom = 7; 
        public final static int eOrientationLeftBottom  = 8;
    }

    public static class PERMISSION 
    {
        /**
         * Do not apply encryption. This enumeration shall not be combined with
         * another enumeration. When using this enumeration set both passwords
         * to an empty string or null.
         */
        public final static int ePermNoEncryption           = -1;
        public final static int ePermNone                   = 0x00000000;
        /** Allow low resolution printing. */
        public final static int ePermPrint                  = 0x00000004;
        /** Allow changing the document. */
        public final static int ePermModify                 = 0x00000008;
        /** Allow content copying or extraction. */
        public final static int ePermCopy                   = 0x00000010;
        /** Allow annotations. */
        public final static int ePermAnnotate               = 0x00000020;
        /** Allow filling of form fields. */
        public final static int ePermFillForms              = 0x00000100;
        /** Allow support for disabilities. */
        public final static int ePermSupportDisabilities    = 0x00000200;
        /** Allow document assembly. */
        public final static int ePermAssemble               = 0x00000400;
        /** Allow high resolution printing. */
        public final static int ePermDigitalPrint           = 0x00000800;
        /** Grant all permissions. */
        public final static int ePermAll                    = 0x00000F3C;
    }

    public static class FONTTYPE
    {
        /** PostScript Type1 font program. */
        public final static int eFontType1                  = 1;
        /** TrueType font program. */
        public final static int eFontTrueType               = 2;
        /** PostScript Compact Font Format (CFF) font program. */
        public final static int eFontCFF                    = 4;
        /** PDF font program. */
        public final static int eFontType3                  = 8;
    } 

    public static class STRIPTYPE 
    {
        /** Thread, Bead dictionaries. */
        public final static int eStripThreads               = 0x001;
        /** Metadata dictionaries. */
        public final static int eStripMetadata              = 0x002;
        /** PieceInfo dictionaries. */
        public final static int eStripPieceInfo             = 0x004;
        /** StructTree dictionaries. */
        public final static int eStripStructTree            = 0x008;
        /** Thumb dictionaries. */
        public final static int eStripThumb                 = 0x010;
        /** Spider dictionaries. */
        public final static int eStripSpider                = 0x020;
        /** Alternate image dictionaries. */
        public final static int eStripAlternates            = 0x040;
        /** Strip and flatten form fields. */
        public final static int eStripForms                 = 0x080;
        /** Strip and flatten link annotations. */
        public final static int eStripLinks                 = 0x100;
        /** Strip and flatten all annotations except form fields and links. */
        public final static int eStripAnnots                = 0x200;
        /** Strip and flatten all annotations including form fields. */
        public final static int eStripFormsAnnots           = 0x380;
        /** Output intents. */
        public final static int eStripOutputIntents         = 0x400;
        /** Strip (and flatten) all of the above. */
        public final static int eStripAll                   = 0xfff;
    } 

    public static class COMPRESSIONATTEMPT
    {
        /** Exclude from processing.                  */
        public final static int eComprAttemptNone      = 0;
        /** No compression (raw).                     */
        public final static int eComprAttemptRaw       = 1;
        /** Lossy DCT (Discrete Cosine Transform, JPEG) compression. */
        public final static int eComprAttemptJPEG      = 2;
        /** Lossless flate (ZIP) compression.         */
        public final static int eComprAttemptFlate     = 4;
        /** Lossless LZW (Lempel-Ziff-Welch) compression. */
        public final static int eComprAttemptLZW       = 8;
        /** Lossless CCITT Fax Group3 compression.   */
        public final static int eComprAttemptGroup3    = 16;
        /** Lossless CCITT Fax Group3 (2D) compression. */
        public final static int eComprAttemptGroup3_2D = 32;
        /** Lossless CCITT Fax Group4 compression.   */
        public final static int eComprAttemptGroup4    = 64;
        /** Lossless JBGI2 compression.              */
        public final static int eComprAttemptJBIG2     = 128;
        /** Lossy JPEG2000 compression.              */
        public final static int eComprAttemptJPEG2000  = 256;
        /** Mixed Raster Content (MRC) optimization. */
        public final static int eComprAttemptMRC       = 512;
        /** Use same compression as source image */
        public final static int eComprAttemptSource    = 1024;
    } 

    public static class COLORCONVERSION 
    {
        /** No conversion. */
        public final static int eConvNone                   = 0;
        /** Color conversion to the ICE sRGB color space (Web). */
        public final static int eConvRGB                    = 1;
        /** Color conversion to the CMYK color space (Printing). */
        public final static int eConvCMYK                   = 2;
        /** Color conversion to the Gray color space (B/W TV compatible). */
        public final static int eConvGray                   = 3;
    }

    public static class OPTIMIZATIONPROFILE
    {
        /** Minimal optimization */
        public final static int eOptimizationProfileDefault = 0;
        /** Optimize for the web */
        public final static int eOptimizationProfileWeb     = 1;
        /** Optimize for printing */
        public final static int eOptimizationProfilePrint   = 2;
        /** Optimize file size as much as possible */
        public final static int eOptimizationProfileMax     = 3;
        /** Make MRC optimization */
        public final static int eOptimizationProfileMRC     = 4;
    }

    public static class COMPLIANCE
    {
        public final static int ePDFUnk                     = 0x0000;
        public final static int ePDF10                      = 0x1000;
        public final static int ePDF11                      = 0x1100;
        public final static int ePDF12                      = 0x1200;
        public final static int ePDF13                      = 0x1300;
        public final static int ePDF14                      = 0x1400;
        public final static int ePDF15                      = 0x1500;
        public final static int ePDF16                      = 0x1600;
        public final static int ePDF17                      = 0x1700;
        public final static int ePDFA1b                     = 0x1401;
        public final static int ePDFA1a                     = 0x1402;
        public final static int ePDFA2b                     = 0x1701;
        public final static int ePDFA2u                     = 0x1702;
        public final static int ePDFA2a                     = 0x1703;
        public final static int ePDFA3b                     = 0x1711;
        public final static int ePDFA3u                     = 0x1712;
        public final static int ePDFA3a                     = 0x1713;
    }

    public static class CONVERSIONERROR
    {
        /** None. */
        public final static int ePDFConversionErrorNone             = 0x0000;
        /** Visual differences in output file. */
        public final static int ePDFConversionErrorVisualDiff       = 0x0004;
        /** Resolve name collisions of colorants (PDF/A-2 and PDF/A-3 only). */
        public final static int ePDFConversionErrorColorants        = 0x0008;
        /** Remove optional content groups (layers) (PDF/A-1 only). */
        public final static int ePDFConversionErrorOCGRemoved       = 0x0010;
        /** Remove transparency (PDF/A-1 only). */
        public final static int ePDFConversionErrorTranspRemoved    = 0x0020;
        /** Remove embedded files. */
        public final static int ePDFConversionErrorEFRemoved        = 0x0040;
        /** Remove non convertible XMP metadata. */
        public final static int ePDFConversionErrorXMPRemoved       = 0x0080;
        /** Conversion of signed document forced removal of signatures. */
        public final static int ePDFConversionErrorDocSigned        = 0x0200;
        /** The input document is corrupt. */
        public final static int ePDFConversionErrorCorrupt          = 0x1000;
        /** Font substituted. */
        public final static int ePDFConversionErrorFontSubst        = 0x4000;
        /** Remove interactive elements such as actions or annotations. */
        public final static int ePDFConversionErrorActionRemoved    = 0x8000;
    }

    public static class TEXTEXTCONFIGURATION
    {
        /** Start new text object, if text state changes (font, font size, horiz scaling). */
        public final static int eTECBreakTextState      = 0x01;
        /** Start new text object, if graphics state changes (color). */
        public final static int eTECBreakGraphicsState  = 0x02;
        /** Start new text object, if extracted text contains a space Unicode. */
        public final static int eTECBreakSpaceUnicode   = 0x04;
        /** Merge text tokens that are a single space width apart, insert space. */
        public final static int eTECPosMergeSingleSpace = 0x08;
        /** Merge text tokens that are one or more space widths apart, insert multiple spaces. */
        public final static int eTECPosMergeMultiSpace  = 0x10;
    }

    /** The page mode specifies how the document shall be displayed when opened. */
    public static class PDFPAGELAYOUT
    {
        /** Display one page at a time. */
        public final static int ePageLayoutSinglePage     = 0;
        /** Display the pages in one column. */
        public final static int ePageLayoutOneColumn      = 1;
        /** Display the pages in two columns, with odd-numbered pages on the left. */
        public final static int ePageLayoutTwoColumnLeft  = 2;
        /** Display the pages in two columns, with odd-numbered pages on the right. */
        public final static int ePageLayoutTwoColumnRight = 3;
        /** (PDF 1.5) Display the pages two at a time, with odd-numbered pages on the left. */
        public final static int ePageLayoutTwoPageLeft    = 4;
        /** (PDF 1.5) Display the pages two at a time, with odd-numbered pages on the right. */
        public final static int ePageLayoutTwoPageRight   = 5;
    }

    /** The page mode specifies how the document shall be displayed when opened. */
    public static class PDFPAGEMODE
    {
        /** Neither document outline nor thumbnail images visible. */
        public final static int ePageModeUseNone        = 0;
        /** Document outline visible. */
        public final static int ePageModeUseOutlines    = 1;
        /** Thumbnail images visible. */
        public final static int ePageModeUseThumbs      = 2;
        /** Full-screen mode, with no menu bar, window controls, or any other window visible. */
        public final static int ePageModeFullScreen     = 3;
        /** (PDF 1.5) Optional content group panel visible. */
        public final static int ePageModeUseOC          = 4;
        /** (PDF 1.6) Attachments panel visible. */
        public final static int ePageModeUseAttachments = 5;
    }

    /** The destination mode */
    public static class PDFDESTMODE
    {
        /** "XYZ"      left, top, zoom           <p>The upper left corner of the view is positioned at the coordinate (left, top) with the given zoom factor.*/
        public final static int eDestModeXYZ   = 0;
        /** "Fit"                                <p>The view is such that the whole page is visible.*/
        public final static int eDestModeFit   = 1;
        /** "FitH"     top                       <p>The view is top-aligned with top and shows the whole page width.*/
        public final static int eDestModeFitH  = 2;
        /** "FitV"     left                      <p>The view is left-aligned with left and shows the whole page height.*/
        public final static int eDestModeFitV  = 3;
        /** "FitR"     left, bottom, right, top  <p>The view contains the rectangle specified the two coordinates (left, bottom) and (right, bottom).*/
        public final static int eDestModeFitR  = 4;
        /** "FitB"                               <p>The view is such that the page's bounding box is visible.*/
        public final static int eDestModeFitB  = 5;
        /** "FitBH"    top                       <p>The view is top-aligned with top and shows the whole width of the page's bounding box.*/
        public final static int eDestModeFitBH = 6;
        /** "FitBV"    left                      <p>The view is left-aligned with left and shows the whole height of the page's bounding box.*/
        public final static int eDestModeFitBV = 7;
    }

    public static class PDFCOPYOPTION
    {
        /** Copy links (document internal and external links). */
        public final static int ePdfCopyLinks               = 0x0001;
        /** Copy interactive annotations such as sticky notes or highlight annotations. */
        public final static int ePdfCopyAnnotations         = 0x0002;
        /** Copy interactive form fields. */
        public final static int ePdfCopyFormFields          = 0x0004;
        /** Copy outlines (also called bookmarks). */
        public final static int ePdfCopyOutlines            = 0x0008;
        /** Copy logical structure and tagging information. */
        public final static int ePdfCopyLogicalStructure    = 0x0010;
        /** Copy named destinations. */
        public final static int ePdfCopyNamedDestinations   = 0x0020;
        /** Flatten annotations preserves the visual appearance of annotations, but discards all interactive elements. */
        public final static int ePdfFlattenAnnotations      = 0x0040;
        /** Flatten form fields preserves the visual appearance of form fields, but discards all interactive elements. */
        public final static int ePdfFlattenFormFields       = 0x0080;
        /** Flatten the visual appearance of signed signature fields. */
        public final static int ePdfFlattenSignatures       = 0x0100;
        /** Find and merge redundant resources such as fonts and images. */
        public final static int ePdfOptimizeResources       = 0x0200;
        /** Copy associated files. */
        public final static int ePdfCopyAssociatedFiles     = 0x0400;
        /** Merge compatible optional content groups (layers). */
        public final static int ePdfMergeOCGs               = 0x0800;
        /** Keep AcroForm fields from different files separate even if they are identical. */
        public final static int ePdfSeparateAcroForms       = 0x1000;
    }

    public static class PDFCONFORMANCECATEGORY
    {
        /** The file format (header, trailer, objects, xref, streams) is corrupted. */
        public final static int eConfFormat     = 0x00000001;
        /** The document doesn't conform to the PDF reference  (missing required entries, wrong value types, etc.). */
        public final static int eConfPDF        = 0x00000002;
        /** The file is encrypted and the password was not provided. */
        public final static int eConfEncrypt    = 0x00000004;
        /** The document contains device-specific color spaces. */
        public final static int eConfColor      = 0x00000008;
        /** The document contains illegal rendering hints (unknown intents, interpolation, transfer and halftone functions). */
        public final static int eConfRendering  = 0x00000010;
        /** The document contains alternate information (images). */
        public final static int eConfAlternate  = 0x00000020;
        /** The document contains embedded PostScript code. */
        public final static int eConfPostScript = 0x00000040;
        /** The document contains references to external content (reference XObjects, file attachments, OPI). */
        public final static int eConfExternal   = 0x00000080;
        /** The document contains fonts without embedded font programs or encoding information (CMAPs) */
        public final static int eConfFont       = 0x00000100;
        /** The document contains fonts without appropriate character to Unicode mapping information (ToUnicode maps) */
        public final static int eConfUnicode    = 0x00000200;
        /** The document contains transparency. */
        public final static int eConfTransp     = 0x00000400;
        /** The document contains unknown annotation types. */
        public final static int eConfAnnot      = 0x00000800;
        /** The document contains multimedia annotations (sound, movies). */
        public final static int eConfMultimedia = 0x00001000;
        /** The document contains hidden, invisible, non-viewable or non-printable annotations. */
        public final static int eConfPrint      = 0x00002000;
        /** The document contains annotations or form fields with ambiguous or without appropriate appearances. */
        public final static int eConfAppearance = 0x00004000;
        /** The document contains actions types other than for navigation (launch, JavaScript, ResetForm, etc.) */
        public final static int eConfAction     = 0x00008000;
        /** The document's meta data is either missing or inconsistent or corrupt. */
        public final static int eConfMetaData   = 0x00010000;
        /** The document doesn't provide appropriate logical structure information. */
        public final static int eConfStructure  = 0x00020000;
        /** The document contains optional content (layers). */
        public final static int eConfOptional   = 0x00040000;
    }


    public static class ERRORCODE
    {

        // General codes
        //
        /** {@value}: The operation was completed successfully. */
        public final static int BSE_INFO_SUCCESS                    = 0x00000000;
        /** {@value}: The operation was completed successfully. */
        public final static int PDF_S_SUCCESS                       = 0x00000000;

        // BSE
        //
        /** {@value}: Memory allocation error. */
        public final static int BSE_ERROR_MEMORY_ALLOCATE           = 0x80308001;
        /** {@value}: The value represented by a group of 5 characters is greater than 2^32 - 1. */
        public final static int BSE_ERROR_ASCII85_OVERFLOW          = 0x80300002;
        /** {@value}: A 'z' character occurs in the middle of a group in an ASCII85 stream. */
        public final static int BSE_ERROR_ASCII85_Z                 = 0x80300003;
        /** {@value}: A final partial group contains only one character in an ASCII85 stream. */
        public final static int BSE_ERROR_ASCII85_INCOMPLETE        = 0x80300004;
        /** {@value}: An invalid character was encountered in an ASCII85 stream. */
        public final static int BSE_ERROR_ASCII85_CHAR              = 0x80300005;
        /** {@value}: An EOD code was missing in an ASCII85 stream. */
        public final static int BSE_ERROR_ASCII85_EOS               = 0x80300006;
        /** {@value}: A character in an ASCIIHex stream is invalid. */
        public final static int BSE_ERROR_ASCIIHEX_CHAR             = 0x80300007;
        /** {@value}: An EOD code was missing in an ASCIIHex stream. */
        public final static int BSE_ERROR_ASCIIHEX_EOS              = 0x80300008;
        /** {@value}: The CCITTFax stream contains a line which is too long. */
        public final static int BSE_ERROR_CCITT_LINE                = 0x80300009;
        /** {@value}: The CCITTFax stream contains 1D extensions. */
        public final static int BSE_ERROR_CCITT_1DEXT               = 0x8030000A;
        /** {@value}: The CCITTFax stream contains an invalid 1D huffman code. */
        public final static int BSE_ERROR_CCITT_HUF1D               = 0x8030000B;
        /** {@value}: The CCITTFax stream contains 2D extensions. */
        public final static int BSE_ERROR_CCITT_2DEXT               = 0x8030000C;
        /** {@value}: The CCITTFax stream contains an invalid 2D huffman code. */
        public final static int BSE_ERROR_CCITT_HUF2D               = 0x8030000D;
        /** {@value}: An EOD code was missing in an LZW stream. */
        public final static int BSE_ERROR_LZW_EOS                   = 0x80300019;
        /** {@value}: An invalid code was encountered in an LZW stream. */
        public final static int BSE_ERROR_LZW_CODE                  = 0x8030001A;
        /** {@value}: An EOD code was missing in an RunLength stream. */
        public final static int BSE_ERROR_RLE_EOS                   = 0x8030001B;
        /** {@value}: Failed to establish TCP connection to %host%:%port%. */
        public final static int BSE_E_TCP_CONN                      = 0x8030001D;
        /** {@value}: TCP failed to send %url% (error code %code%). */
        public final static int BSE_E_TCP_RESP                      = 0x8030001E;
        /** {@value}: Failed to send HTTP %verb% request to http://%host%%resource%. */
        public final static int BSE_E_HTTP_REQ                      = 0x8030001F;
        /** {@value}: Failed to send %url%. Server returned HTTP status code %code%. */
        public final static int BSE_E_HTTP_RESP                     = 0x80300020;
        /** {@value}: Path too long: '%path%'. Maximum length is %maxlen%. */
        public final static int BSE_E_MAXPATH                       = 0x80300021;
        /** {@value}: Task '%name%' scheduled. */
        public final static int BSE_I_TASK_SCHEDULED                = 0x00300030;
        /** {@value}: Task '%name%' started. */
        public final static int BSE_I_TASK_STARTED                  = 0x00300031;
        /** {@value}: Task '%name%' finished. */
        public final static int BSE_I_TASK_FINISHED                 = 0x00300032;
        /** {@value}: Task '%name%' failed: %message%. */
        public final static int BSE_E_TASK_FAILED                   = 0x80300033;
        /** {@value}: Invalid number of bytes in stream. */
        public final static int BSE_E_STREAM_LENGTH                 = 0x80300040;
        /** {@value}: Invalid UTF8. */
        public final static int BSE_E_UTF8                          = 0x80300041;
        /** {@value}: Invalid UTF16. */
        public final static int BSE_E_UTF16                         = 0x80300042;
        /** {@value}: Failed to decode data using system code page. */
        public final static int BSE_E_SYSTEMCP                      = 0x80300043;
        /** {@value}: Unknown fatal error. */
        public final static int BSE_ERROR_FATAL                     = 0x8030FFFF;
        /** {@value}: Unknown fatal error. */
        public final static int PDF_E_FATAL                         = 0x8030FFFF;

        // PDF
        //
        /** {@value}: The file couldn't be opened. */
        public final static int PDF_E_FILEOPEN                      = 0x80410101;
        /** {@value}: The file couldn't be created. */
        public final static int PDF_E_FILECREATE                    = 0x80410102;
        /** {@value}: The file's position couldn't be set. */
        public final static int PDF_E_SETPOS                        = 0x80410103;
        /** {@value}: The file header was not found. */
        public final static int PDF_E_HEADER                        = 0x80410104;
        /** {@value}: The file header must be located at the beginning. */
        public final static int PDF_W_HEADEROFFS                    = 0x00418105;
        /** {@value}: The file header version does not conform to the standard. */
        public final static int PDF_I_VERSION                       = 0x00410106;
        /** {@value}: The comment, classifying the file as containing 8-bit binary data, is missing. */
        public final static int PDF_W_BINARY                        = 0x00418107;
        /** {@value}: The end-of-file marker was not found. */
        public final static int PDF_E_EOF                           = 0x80410108;
        /** {@value}: The last line of the file must contain an end-of-file marker. */
        public final static int PDF_W_EOFOFFS                       = 0x00418109;
        /** {@value}: The 'startxref' keyword or the xref position was not found. */
        public final static int PDF_E_STARTXREF                     = 0x8041010A;
        /** {@value}: The 'xref' keyword was not found or the xref table is malformed. */
        public final static int PDF_E_XREF                          = 0x8041010B;
        /** {@value}: The 'xref' keyword must be located at the given offset. */
        public final static int PDF_W_XREFOFFS                      = 0x0041810C;
        /** {@value}: The xref fields must be separated by a single space and terminated by a single end-of-line marker. */
        public final static int PDF_W_XREFSEP                       = 0x0041810D;
        /** {@value}: The file trailer dictionary is missing or invalid. */
        public final static int PDF_E_TRAILER                       = 0x8041010E;
        /** {@value}: The root object was not found. */
        public final static int PDF_E_ROOT                          = 0x8041010F;
        /** {@value}: The file trailer dictionary must not have an encrypt key. */
        public final static int PDF_W_ENCRYPT                       = 0x00418110;
        /** {@value}: The file trailer dictionary must have an id key. */
        public final static int PDF_W_ID                            = 0x00418111;
        /** {@value}: The authentication failed due to a wrong password. */
        public final static int PDF_E_PASSWORD                      = 0x80410112;
        /** {@value}: The file is corrupt and cannot be repaired. Some of the contents can possibly be recovered. */
        public final static int PDF_E_CORRUPT                       = 0x80410113;
        /** {@value}: The file is corrupt and needs to be repaired. */
        public final static int PDF_W_CORRUPT                       = 0x00418114;
        /** {@value}: The object with the number %n% doesn't exist. */
        public final static int PDF_I_OBJNUMBER                     = 0x00410115;
        /** {@value}: The generation number %n1% of the object doesn't match with the generation number %n2% of the object's reference. */
        public final static int PDF_I_GENNUMBER                     = 0x00410116;
        /** {@value}: The file contains unrendered XFA fields. */
        public final static int PDF_E_XFANEEDSRENDERING             = 0x80410117;
        /** {@value}: The file uses a proprietary security handler. */
        public final static int PDF_E_UNKSECHANDLER                 = 0x80410118;
        /** {@value}: The file uses a security algorithm that is not implemented. */
        public final static int PDF_E_SECALGONIMP                   = 0x80410119;
        /** {@value}: The file is a collection (PDF Portfolio). */
        public final static int PDF_E_COLLECTION                    = 0x8041011A;
        /** {@value}: The file header format does not conform to the standard. */
        public final static int PDF_W_HEADER                        = 0x0041811B;
        /** {@value}: Invalid or inconsistent encryption parameters specified. */
        public final static int PDF_E_ENCPARAM                      = 0x8041011C;
        /** {@value}: The filter %name% is unknown. */
        public final static int PDF_E_FILTER                        = 0x80410201;
        /** {@value}: The image's sample stream's computed length %l1% is different to the actual length %l2%. */
        public final static int PDF_E_IMAGEDATA                     = 0x80410218;
        /** {@value}: Errors in decode filter. */
        public final static int PDF_W_DECODE                        = 0x00418219;
        /** {@value}: The object number is missing. */
        public final static int PDF_E_OBJNO                         = 0x80410301;
        /** {@value}: The generation number is missing. */
        public final static int PDF_E_GENNO                         = 0x80410302;
        /** {@value}: The object's identity %n1% doesn't match with the object's reference identity %n2%. */
        public final static int PDF_E_IDENTITY                      = 0x80410303;
        /** {@value}: The "obj" keyword is missing. */
        public final static int PDF_E_OBJ                           = 0x80410304;
        /** {@value}: The object %obj% is empty (null). */
        public final static int PDF_I_NULL                          = 0x00410305;
        /** {@value}: The "Length" key of the stream object is wrong. */
        public final static int PDF_E_LENGTH                        = 0x80410306;
        /** {@value}: The "endstream" keyword is missing. */
        public final static int PDF_E_ENDSTREAM                     = 0x80410307;
        /** {@value}: The "endobj" keyword is missing. */
        public final static int PDF_E_ENDOBJ                        = 0x80410308;
        /** {@value}: The offset in the xref table is not correct. */
        public final static int PDF_W_XREFOFF                       = 0x00418309;
        /** {@value}: An array contains more than 8191 elements. */
        public final static int PDF_W_ARRAYSIZE                     = 0x00418310;
        /** {@value}: A name is longer than 127 bytes. */
        public final static int PDF_W_NAMELENGTH                    = 0x00418311;
        /** {@value}: An integer value is larger than 2^31-1. */
        public final static int PDF_W_INTEGERVAL                    = 0x00418312;
        /** {@value}: A dictionary contains more than 4095 entries. */
        public final static int PDF_W_DICTCAPACITY                  = 0x00418313;
        /** {@value}: A real value is larger than 2^15-1. */
        public final static int PDF_W_REALVAL                       = 0x00418314;
        /** {@value}: The page boundary %name% is not within the allowed range. */
        public final static int PDF_W_PAGEBOX                       = 0x00418315;
        /** {@value}: A real value's absolute value is larger than 3.403 x 10^38. */
        public final static int PDF_W_REALVAL2                      = 0x00418316;
        /** {@value}: A string length exceeds the limit defined by the standard. */
        public final static int PDF_W_STRINGLENGTH                  = 0x00418317;
        /** {@value}: The size of the graphics state stack exceeds the limit defined by the standard. */
        public final static int PDF_W_GSSTACK                       = 0x00418318;
        /** {@value}: The file has more than 8388607 indirect objects. */
        public final static int PDF_W_OBJCOUNT                      = 0x00418319;
        /** {@value}: The %name% array has %n1% but must have %n2% elements. */
        public final static int PDF_E_ARRAYSIZE                     = 0x8041031A;
        /** {@value}: The function is invalid. */
        public final static int PDF_E_INVFUNC                       = 0x8041031B;
        /** {@value}: Number of colorants %n% exceeds maximum of %nmax%. */
        public final static int PDF_E_DEVICENCOMP                   = 0x8041031C;
        /** {@value}: Maximum depth of graphics state nesting by q and Q operators exceeded. */
        public final static int PDF_E_QNESTING                      = 0x8041031D;
        /** {@value}: Maximum value of a CID exceeded. */
        public final static int PDF_E_CIDMAX                        = 0x8041031E;
        /** {@value}: A number value is larger than 2^15-1. */
        public final static int PDF_W_NUMBERVAL                     = 0x0041831F;
        /** {@value}: A number value's absolute value is larger than 2^31-1. */
        public final static int PDF_W_NUMBERVAL2                    = 0x00418320;
        /** {@value}: The page doesn't exist. */
        public final static int PDF_E_PAGE                          = 0x80410401;
        /** {@value}: The page or page tree node has a missing or invalid "Type" key. */
        public final static int PDF_E_PAGETYPE                      = 0x80410402;
        /** {@value}: The page tree node has a missing or invalid "Kids" key. */
        public final static int PDF_E_PAGEKIDS                      = 0x80410403;
        /** {@value}: The page tree node has a missing or invalid "Count" key. */
        public final static int PDF_E_PAGECOUNT                     = 0x80410404;
        /** {@value}: The page or page tree node has a missing or invalid "Parent" key. */
        public final static int PDF_E_PAGEPARENT                    = 0x80410405;
        /** {@value}: The page has a missing or invalid "Resources" key. */
        public final static int PDF_E_PAGERES                       = 0x80410406;
        /** {@value}: The page has a missing or invalid "MediaBox" key. */
        public final static int PDF_E_PAGEMEDIABOX                  = 0x80410407;
        /** {@value}: The document contains no pages. */
        public final static int PDF_E_NOPAGES                       = 0x80410408;
        /** {@value}: An unexpected token was found. */
        public final static int PDF_E_TOKEN                         = 0x80410501;
        /** {@value}: The content stream contains an invalid operator. */
        public final static int PDF_E_OPERATOR                      = 0x80410502;
        /** {@value}: The operator has an invalid number of operands. */
        public final static int PDF_E_OPERANDS                      = 0x80410503;
        /** {@value}: An operand stack over- or underflow occurred. */
        public final static int PDF_E_OPNDSTACK                     = 0x80410504;
        /** {@value}: The operand must be a string. */
        public final static int PDF_E_OPNDSTRING                    = 0x80410506;
        /** {@value}: The operand must be a name. */
        public final static int PDF_E_OPNDNAME                      = 0x80410507;
        /** {@value}: The operand must be an array. */
        public final static int PDF_E_OPNDARRAY                     = 0x80410508;
        /** {@value}: The operand must be a dictionary. */
        public final static int PDF_E_OPNDDICT                      = 0x80410509;
        /** {@value}: The value %value% of an operand is out of range. */
        public final static int PDF_E_OPNDVALUE                     = 0x8041050A;
        /** {@value}: The name %name% of a font resource is unknown. */
        public final static int PDF_E_UNKFONT                       = 0x8041050B;
        /** {@value}: The name %name% of a color space resource is unknown. */
        public final static int PDF_E_UNKCOLORSPACE                 = 0x8041050C;
        /** {@value}: The name %name% of a graphics state dictionary resource is unknown. */
        public final static int PDF_E_UNKGS                         = 0x8041050D;
        /** {@value}: The name %name% of a pattern dictionary resource is unknown. */
        public final static int PDF_E_UNKPAT                        = 0x8041050E;
        /** {@value}: The name %name% of a xobject resource is unknown. */
        public final static int PDF_E_UNKXOBJ                       = 0x8041050F;
        /** {@value}: A graphics state stack over- or underflow occurred. */
        public final static int PDF_E_GSSTACK                       = 0x80410511;
        /** {@value}: A begin text operator is missing. */
        public final static int PDF_E_BEGINTEXT                     = 0x80410512;
        /** {@value}: An end text operator is missing. */
        public final static int PDF_E_ENDTEXT                       = 0x80410513;
        /** {@value}: A path start operator was missing. */
        public final static int PDF_W_BEGINPATH                     = 0x00418514;
        /** {@value}: A path start operator was missing. */
        public final static int PDF_E_BEGINPATH                     = 0x80410514;
        /** {@value}: The form xobject %name% has an empty or unreadable content stream. */
        public final static int PDF_E_EMPTYXOBJ                     = 0x80410515;
        /** {@value}: An path was constructed but not painted. */
        public final static int PDF_I_UNUSEDPATH                    = 0x00410516;
        /** {@value}: A path painting operator was used on an empty path. */
        public final static int PDF_I_EMPTYPATH                     = 0x00410518;
        /** {@value}: The analysis has been stopped. */
        public final static int PDF_E_STOPPED                       = 0x80410601;
        /** {@value}: The key %key% was ignored. */
        public final static int PDF_I_KEYIGNORED                    = 0x00410602;
        /** {@value}: The value of the key %key% must be of type %type%. */
        public final static int PDF_E_VALTYPE                       = 0x80410603;
        /** {@value}: The key %key% is required but missing. */
        public final static int PDF_W_KEYREQ                        = 0x00418604;
        /** {@value}: The key %key% is required but missing. */
        public final static int PDF_E_KEYREQ                        = 0x80410604;
        /** {@value}: The value of the key %key% must be an indirect object. */
        public final static int PDF_E_VALIND                        = 0x80410605;
        /** {@value}: The value of the key %key% must be a direct object. */
        public final static int PDF_E_VALDIR                        = 0x80410606;
        /** {@value}: The value of the key %key% is %v1% but must be %v2%. */
        public final static int PDF_W_VALREQ                        = 0x00418607;
        /** {@value}: The value of the key %key% is %v1% but must be %v2%. */
        public final static int PDF_E_VALREQ                        = 0x80410607;
        /** {@value}: The dictionary must not contain the key '%key%'. */
        public final static int PDF_W_KEYPROHIB                     = 0x00418608;
        /** {@value}: The key %key% has a value %val% which is prohibited. */
        public final static int PDF_W_VALPROHIB                     = 0x00418609;
        /** {@value}: The key %key% has a value %val% which is prohibited. */
        public final static int PDF_E_VALPROHIB                     = 0x80410609;
        /** {@value}: The key %key% is present but not recommended. */
        public final static int PDF_I_ATTRNOTREC                    = 0x0041060A;
        /** {@value}: The key %key% is recommended. */
        public final static int PDF_I_KEYRECOMM                     = 0x0041060B;
        /** {@value}: The key %key1% is inconsistent with the key %key2%. */
        public final static int PDF_W_INCONSISTENCY                 = 0x0041860C;
        /** {@value}: The value of the key %key% must not be of type %type%. */
        public final static int PDF_E_VALNTYPE                      = 0x8041060D;
        /** {@value}: The name object must be UTF-8 encoded. */
        public final static int PDF_W_NAMEENC                       = 0x0041860E;
        /** {@value}: The embedded file '%name%' is not a PDF file. */
        public final static int PDF_W_EFPDF                         = 0x0041860F;
        /** {@value}: The embedded file '%name%' must be PDF/A compliant. */
        public final static int PDF_W_EFPDFA                        = 0x00418610;
        /** {@value}: ICCBased CMYK color space must not be used with overprint mode 1. */
        public final static int PDF_W_IMPLCCONV                     = 0x00418611;
        /** {@value}: All '%name%' colorants must have the same tint transform and alternate space. */
        public final static int PDF_W_COLORANTMIS                   = 0x00418612;
        /** {@value}: The number of color channels in the JPEG2000 data must not be %n%. */
        public final static int PDF_W_JPXNUMCHAN                    = 0x00418613;
        /** {@value}: JPEG2000 data with multiple color space definitions and incorrect APPROX field. */
        public final static int PDF_W_JPXCOLRAPPROX                 = 0x00418614;
        /** {@value}: The value of the METH entry in the 'colr' box in the JPEG2000 data is invalid. */
        public final static int PDF_W_JPXCOLRMETH                   = 0x00418615;
        /** {@value}: The enumerated colour space %i% must not be used in JPEG2000 data. */
        public final static int PDF_W_JPXENUMCS                     = 0x00418616;
        /** {@value}: JPEG2000 enumerated colour space 19 (CIEJab) shall not be used. */
        public final static int PDF_W_JPXCIEJAB                     = 0x00418617;
        /** {@value}: All channels in JPEG2000 data must have a bit depth in the range from 1 to 38. */
        public final static int PDF_W_JPXBITDEPTH                   = 0x00418618;
        /** {@value}: All color channels in JPEG2000 data must have the same bit depth. */
        public final static int PDF_W_JPXCLRBD                      = 0x00418619;
        /** {@value}: JPEG2000 data contains invalid opacity channels. */
        public final static int PDF_E_JPXOPACITY                    = 0x8041061A;
        /** {@value}: Error in JPEG2000 data. */
        public final static int PDF_E_JPXINVALID                    = 0x8041061B;
        /** {@value}: The page must define a default blending color space. */
        public final static int PDF_W_DEFBLENDCS                    = 0x0041861C;
        /** {@value}: The blending color space is of an invalid type. */
        public final static int PDF_E_BLENDCS                       = 0x0041861D;
        /** {@value}: Two optional content configurations must not have the same name '%name%'. */
        public final static int PDF_W_OCCNAMECOLL                   = 0x0041861E;
        /** {@value}: The order array in the OC configuration '%conf%' must contain '%ocg%'. */
        public final static int PDF_W_OCORDER                       = 0x0041861F;
        /** {@value}: The file must not contain different output intents. */
        public final static int PDF_W_OIMISSMATCH                   = 0x00418620;
        /** {@value}: The appearance must have state dictionaries (subdictionaries to 'N'). */
        public final static int PDF_W_APPNOSTATE                    = 0x00418621;
        /** {@value}: The content stream must have an explicitly associated Resources dictionary. */
        public final static int PDF_W_NORES                         = 0x00418622;
        /** {@value}: The unicode for cid %cid% is unknown. */
        public final static int PDF_W_NOUNICODE                     = 0x00418623;
        /** {@value}: The embedded font program '%font%' cannot be read. */
        public final static int PDF_E_FONTPROG                      = 0x80410701;
        /** {@value}: The attribute %attr% of the font descriptor must match with the font type %type%. */
        public final static int PDF_E_FONTFILE                      = 0x80410702;
        /** {@value}: The CMAP %name% must be embedded. */
        public final static int PDF_E_CMAPEMB                       = 0x80410703;
        /** {@value}: The font %font% must be embedded. */
        public final static int PDF_W_EMBED                         = 0x00418704;
        /** {@value}: Font %f1%: The pre-installed font '%f2%' is used. */
        public final static int PDF_W_SYSTEMFONT                    = 0x00418808;
        /** {@value}: Font %f1%: The font was replaced with '%f2%'. */
        public final static int PDF_W_FONTREPLACE                   = 0x00418809;
        /** {@value}: Font %font% has an OpenType font program that is not allowed. */
        public final static int PDF_W_OTFFONTPROG                   = 0x0041880A;
        /** {@value}: File specification '%name%' not associated with an object. */
        public final static int PDF_W_NOAFREF                       = 0x0041880B;
        /** {@value}: Error loading profile configuration file: %msg% */
        public final static int PDF_E_LOADPROFILE                   = 0x8041080C;

        // PDF rendering module
        //
        /** {@value}: The pattern type %type% is unknown or not yet implemented. */
        public final static int PDF_E_PATTERNTYPE                   = 0x81410801;
        /** {@value}: Font %font%: The charproc of glyph '%glyph%' was not found. */
        public final static int PDF_E_CHARPROC                      = 0x81410802;
        /** {@value}: The shading type %type% is unknown or not yet implemented. */
        public final static int PDF_E_SHADINGTYPE                   = 0x81410803;
        /** {@value}: Font %font%: The font program '%fp%' couldn't be installed successfully. */
        public final static int PDF_E_FONTINSTALL                   = 0x81410804;
        /** {@value}: The path is constructed but the painting operator is missing. */
        public final static int PDF_W_PAINTMISSING                  = 0x01418807;
        /** {@value}: GDI+ error. */
        public final static int PDF_E_GDIPLUS                       = 0x8141880A;
        /** {@value}: GDI error. */
        public final static int PDF_E_GDI                           = 0x8141880B;

        // PDF validation module
        //
        /** {@value}: The appearance has state dictionaries (subdictionaries to 'N'). */
        public final static int PDF_W_APPSTATE                      = 0x03418516;
        /** {@value}: The embedded ICC profile couldn't be read. */
        public final static int PDF_E_ICCINVALID                    = 0x83410517;
        /** {@value}: The embedded ICC profile's version is not supported. */
        public final static int PDF_W_ICCVERSION                    = 0x03418518;
        /** {@value}: The document has multiple PDF/A output intents. */
        public final static int PDF_E_MULTIPLEOI                    = 0x83410519;
        /** {@value}: A hexadecimal string contains an odd number of bytes. */
        public final static int PDF_W_HEXODD                        = 0x0341851A;
        /** {@value}: There is data after the EOF marker. */
        public final static int PDF_W_EOFDATA                       = 0x0341851B;
        /** {@value}: The ID in the 1st page and last trailer of a linearized file are different. */
        public final static int PDF_W_LINEARID                      = 0x0341851C;
        /** {@value}: The separator in a xref subsection header must be a single space. */
        public final static int PDF_W_XREFSPACE                     = 0x0341851D;
        /** {@value}: The separator between 'xref' and the subsection must be an EOL. */
        public final static int PDF_W_XREFEOL                       = 0x0341851E;
        /** {@value}: The separator after 'stream' must be CR-LF or LF. */
        public final static int PDF_W_STREAMEOL                     = 0x0341851F;
        /** {@value}: The separator before 'endstream' must be an EOL. */
        public final static int PDF_W_EOLENDSTREAM                  = 0x03418520;
        /** {@value}: The separator between the object and generation number must be a single space. */
        public final static int PDF_W_NUMSPACEGEN                   = 0x03418521;
        /** {@value}: The separator between the generation number and 'obj' must be a single space. */
        public final static int PDF_W_GENSPACEOBJ                   = 0x03418522;
        /** {@value}: The separator before the object number must be an EOL. */
        public final static int PDF_W_EOLOBJNUM                     = 0x03418523;
        /** {@value}: The separator before an 'endobj' must be an EOL. */
        public final static int PDF_W_EOLENDOBJ                     = 0x03418524;
        /** {@value}: The separator after an 'obj' must be an EOL. */
        public final static int PDF_W_OBJEOL                        = 0x03418525;
        /** {@value}: The separator after an 'endobj' must be an EOL. */
        public final static int PDF_W_ENDOBJEOL                     = 0x03418526;
        /** {@value}: The embedded TrueType font %font% contains more than one cmap entries. */
        public final static int PDF_I_TRUETYPECMAP                  = 0x03410527;
        /** {@value}: The embedded TrueType font %font% contains %err% cmap entries. */
        public final static int PDF_W_TRUETYPECMAP                  = 0x03418527;
        /** {@value}: The embedded ICC profile's device class '%cls%' is not supported. */
        public final static int PDF_E_ICCDEVCLASS                   = 0x83410528;
        /** {@value}: The embedded TrueType font %font% has a cmap(%pid%, %eid%) with format %format% that is not allowed. */
        public final static int PDF_W_TTFCMAPFMT                    = 0x03418529;
        /** {@value}: The embedded TrueType font %font% is corrupt and needs to be repaired. */
        public final static int PDF_W_TTFCORRUPT                    = 0x0341852A;
        /** {@value}: The required XMP property '%prefix%:%propname%' is missing. */
        public final static int PDF_E_XMPPROPREQ                    = 0x8341052B;
        /** {@value}: The property '%prop%' is not allowed in this XMP schema. */
        public final static int XMP_W_PROPINV                       = 0x0341852C;
        /** {@value}: The XMP property '%prefix%:%propname%' has the invalid value '%invval%'. Required is '%reqval%'. */
        public final static int PDF_E_XMPVALREQ                     = 0x8341052E;
        /** {@value}: Invalid encoding of XMP packet: %enc%, expected %exp%. */
        public final static int PDF_W_XMPENCODING                   = 0x0341852F;
        /** {@value}: The width for character %cid% in font '%font%' does not match. */
        public final static int PDF_W_WIDTHMISSMATCH                = 0x03418531;
        /** {@value}: The glyph for character %cid% in font '%font%' is missing. */
        public final static int PDF_E_GLYPHMISSING                  = 0x83410531;
        /** {@value}: The encoding for character code %code% in font '%font%' is missing. */
        public final static int PDF_E_NOENCODING                    = 0x83410532;
        /** {@value}: The file contains cross reference streams. */
        public final static int PDF_E_XREFSTREAM                    = 0x03418533;
        /** {@value}: The recommended XMP property '%prefix%:%propname%' for the document information entry '%infoentry%' is missing. */
        public final static int PDF_I_INFOXMPMISS                   = 0x0341053D;
        /** {@value}: The required XMP property '%prefix%:%propname%' for the document information entry '%infoentry%' is missing. */
        public final static int PDF_E_INFOXMPMISS                   = 0x8341053D;
        /** {@value}: The XMP property '%prefix%:%propname%' is not synchronized with the document information entry '%infoentry%'. */
        public final static int PDF_I_INFOXMPNSYNC                  = 0x0341053E;
        /** {@value}: The XMP property '%prefix%:%propname%' is not synchronized with the document information entry '%infoentry%'. */
        public final static int PDF_E_INFOXMPNSYNC                  = 0x8341053E;
        /** {@value}: Conversion errors in PDF to PDF/A conversion. */
        public final static int PDF_E_CONVERSION                    = 0x83410540;
        /** {@value}: Post analysis errors in PDF to PDF/A conversion. */
        public final static int PDF_E_POSTANALYSIS                  = 0x83410541;
        /** {@value}: File cannot be converted to meet this compliance: %msg% */
        public final static int PDF_E_DOWNGRADE                     = 0x83410542;
        /** {@value}: Font '%font%' required but missing in font directories. */
        public final static int PDF_E_MISSINGFONT                   = 0x83410543;
        /** {@value}: Linearization of the file failed. */
        public final static int PDF_E_LINEARIZATION                 = 0x83410544;
        /** {@value}: Failed to add ZUGFeRD invoice file. */
        public final static int PDF_E_ZUGFERDXML                    = 0x83410545;
        /** {@value}: Invalid compliance specified. */
        public final static int PDF_E_INVCOMPLIANCE                 = 0x83410546;
        /** {@value}: The file must not be encrypted to be PDF/A conform. */
        public final static int PDF_W_NOENCRYPTION                  = 0x0341860D;
        /** {@value}: The page reference in a destination is invalid. */
        public final static int PDF_E_DESTPAGE                      = 0x8341060F;
        /** {@value}: The type of a destination is unknown. */
        public final static int PDF_E_DESTTYPE                      = 0x83410610;
        /** {@value}: The number or the type of the destination operands is wrong. */
        public final static int PDF_E_DESTOPNDS                     = 0x83410611;
        /** {@value}: The document does not conform to the requested standard. */
        public final static int PDF_E_CONFORMANCE                   = 0x83410612;
        /** {@value}: The document contains embedded files. */
        public final static int PDF_W_EMBFILES                      = 0x03418613;
        /** {@value}: A device-specific color space (%cs%) without an appropriate output intent is used. */
        public final static int PDF_W_DEVCOLOR                      = 0x03418614;
        /** {@value}: The appearance dictionary doesn't contain an entry. */
        public final static int PDF_W_APPNOENTRY                    = 0x03418615;
        /** {@value}: The content of the stream must not be in an external file. */
        public final static int PDF_W_EXTSTM                        = 0x03418616;
        /** {@value}: The color space is invalid. */
        public final static int PDF_E_INVCS                         = 0x83410617;
        /** {@value}: The value of the CIDSet[%cid%] of font %font% is %v1% but must be %v2%. */
        public final static int PDF_W_CIDSETVAL                     = 0x03418618;
        /** {@value}: The CharSet of the font %font% must contain the name %name%. */
        public final static int PDF_W_CHARSETM                      = 0x03418619;
        /** {@value}: The CharSet of the font %font% must not contain the name %name%. */
        public final static int PDF_W_CHARSETE                      = 0x0341861A;

        // PDF custom validation profile modules
        //
        /** {@value}: The file size exceeds the first limit. */
        public final static int CHK_E_FILESIZE1                     = 0x83510000;
        /** {@value}: The file size exceeds the critical limit. */
        public final static int CHK_E_FILESIZE2                     = 0x83510001;
        /** {@value}: The file's version is %v% but must be %max% or older. */
        public final static int CHK_E_MAXPDFVERS                    = 0x83510002;
        /** {@value}: The file's version is %v% but must be %min% or newer. */
        public final static int CHK_E_MINPDFVERS                    = 0x83510003;
        /** {@value}: The file must %neg%be password protected. */
        public final static int CHK_E_ENCRYPTION                    = 0x83510004;
        /** {@value}: The filter "%filter%" is not allowed. */
        public final static int CHK_E_FILTER                        = 0x83510005;
        /** {@value}: The creator "%crea%" must meet the organization's standard. */
        public final static int CHK_E_CREATOR                       = 0x83510100;
        /** {@value}: The producer "%prod%" must meet the organization's standard. */
        public final static int CHK_E_PRODUCER                      = 0x83510101;
        /** {@value}: The file attachment "%name%" is not allowed. */
        public final static int CHK_E_EFTYPE                        = 0x83510102;
        /** {@value}: The file attachment "%name%" is not allowed. */
        public final static int CHK_E_EF                            = 0x83510103;
        /** {@value}: The page size %width%x%height%mm of page no. %Page No.% is not on the approved list. */
        public final static int CHK_E_PAGESIZE                      = 0x83510180;
        /** {@value}: The page no. %Page No.% must %neg%be blank. */
        public final static int CHK_E_EMPTYPAGE                     = 0x83510181;
        /** {@value}: The scanned image's resolution is %dpi% DPI but must %max% DPI or less. */
        public final static int CHK_E_SCANMAXDPI                    = 0x83510200;
        /** {@value}: The scanned image's resolution is %dpi% DPI but must %min% DPI or more. */
        public final static int CHK_E_SCANMINDPI                    = 0x83510201;
        /** {@value}: The scanned image must %neg%include color. */
        public final static int CHK_E_SCANCLR                       = 0x83510202;
        /** {@value}: The scanned image must %neg%be word searchable. */
        public final static int CHK_E_OCRTEXT                       = 0x83510203;
        /** {@value}: The page no. %Page No.% must not contain colored objects. */
        public final static int CHK_E_CLRUSED                       = 0x83510210;
        /** {@value}: The page no. %Page No.% must not contain transparent objects. */
        public final static int CHK_E_TRANSPARENCYUSED              = 0x83510211;
        /** {@value}: The document must %neg%contain layers. */
        public final static int CHK_E_LAYERS                        = 0x83510220;
        /** {@value}: The document must %neg%contain hidden layers. */
        public final static int CHK_E_HIDDENLAYERS                  = 0x83510221;
        /** {@value}: The font %font% is not on the approved list. */
        public final static int CHK_E_FONT                          = 0x83510300;
        /** {@value}: The font %font% must %neg%be subsetted. */
        public final static int CHK_E_FNTSUB                        = 0x83510301;
        /** {@value}: The font %font% must %neg%be embedded. */
        public final static int CHK_E_FNTEMB                        = 0x83510302;
        /** {@value}: The page no. %Page No.% must not contain %annot%. */
        public final static int CHK_E_ANNOTATION                    = 0x83510600;
        /** {@value}: The document must not contain %action%. */
        public final static int CHK_E_ACTION                        = 0x83510601;
        /** {@value}: The digital signature of "%name%" is invalid: %msg% */
        public final static int CHK_E_SIGVAL                        = 0x83510700;

        // PDF signature handler
        //
        /** {@value}: Signature creation error. */
        public final static int PDF_E_SIGCREA                       = 0x85410001;
        /** {@value}: Signature validation error. */
        public final static int PDF_E_SIGVAL                        = 0x85410002;
        /** {@value}: Signature length is zero. */
        public final static int PDF_E_SIGGETLENGTH                  = 0x85410003;
        /** {@value}: Signature creation returns a length of %l1% which should not exceed %l2%. */
        public final static int PDF_E_SIGLENGTH                     = 0x85410004;
        /** {@value}: Input document must not be signed. */
        public final static int PDF_E_INPSIG                        = 0x85410005;
        /** {@value}: Signature would destroy PDF/A compliance. */
        public final static int PDF_E_PDFASIG                       = 0x85410006;
        /** {@value}: Unable to open signature background image '%path%'. */
        public final static int PDF_E_SIGABG                        = 0x85410007;

        // JBIG2 compression module
        //
        /** {@value}: The JB2 stream issued a message: '%msg%'. */
        public final static int BSE_INFO_JB2                        = 0x0A030013;
        /** {@value}: The JB2 stream caused a warning: '%msg%'. */
        public final static int BSE_WARNING_JB2                     = 0x0A038014;
        /** {@value}: The JB2 stream caused an error: '%msg%'. */
        public final static int BSE_ERROR_JB2                       = 0x8A030015;

        // DCT compression module
        //
        /** {@value}: Error in DCT stream: Bogus message code %d. */
        public final static int JPEG_E_JMSG_NOMESSAGE               = 0x8A040000;
        /** {@value}: Error in DCT stream: ALIGN_TYPE is wrong, please fix. */
        public final static int JPEG_E_JERR_BAD_ALIGN_TYPE          = 0x8A040001;
        /** {@value}: Error in DCT stream: MAX_ALLOC_CHUNK is wrong, please fix. */
        public final static int JPEG_E_JERR_BAD_ALLOC_CHUNK         = 0x8A040002;
        /** {@value}: Error in DCT stream: Bogus buffer control mode. */
        public final static int JPEG_E_JERR_BAD_BUFFER_MODE         = 0x8A040003;
        /** {@value}: Error in DCT stream: Invalid component ID %d in SOS. */
        public final static int JPEG_E_JERR_BAD_COMPONENT_ID        = 0x8A040004;
        /** {@value}: Error in DCT stream: Invalid crop request. */
        public final static int JPEG_E_JERR_BAD_CROP_SPEC           = 0x8A040005;
        /** {@value}: Error in DCT stream: DCT coefficient out of range. */
        public final static int JPEG_E_JERR_BAD_DCT_COEF            = 0x8A040006;
        /** {@value}: Error in DCT stream: DCT scaled block size %dx%d not supported. */
        public final static int JPEG_E_JERR_BAD_DCTSIZE             = 0x8A040007;
        /** {@value}: Error in DCT stream: Component index %d: mismatching sampling ratio %d:%d, %d:%d, %C. */
        public final static int JPEG_E_JERR_BAD_DROP_SAMPLING       = 0x8A040008;
        /** {@value}: Error in DCT stream: Bogus Huffman table definition. */
        public final static int JPEG_E_JERR_BAD_HUFF_TABLE          = 0x8A040009;
        /** {@value}: Error in DCT stream: Bogus input colorspace. */
        public final static int JPEG_E_JERR_BAD_IN_COLORSPACE       = 0x8A04000A;
        /** {@value}: Error in DCT stream: Bogus JPEG colorspace. */
        public final static int JPEG_E_JERR_BAD_J_COLORSPACE        = 0x8A04000B;
        /** {@value}: Error in DCT stream: Bogus marker length. */
        public final static int JPEG_E_JERR_BAD_LENGTH              = 0x8A04000C;
        /** {@value}: Error in DCT stream: Wrong JPEG library version: library is %d, caller expects %d. */
        public final static int JPEG_E_JERR_BAD_LIB_VERSION         = 0x8A04000D;
        /** {@value}: Error in DCT stream: Sampling factors too large for interleaved scan. */
        public final static int JPEG_E_JERR_BAD_MCU_SIZE            = 0x8A04000E;
        /** {@value}: Error in DCT stream: Invalid memory pool code %d. */
        public final static int JPEG_E_JERR_BAD_POOL_ID             = 0x8A04000F;
        /** {@value}: Error in DCT stream: Unsupported JPEG data precision %d. */
        public final static int JPEG_E_JERR_BAD_PRECISION           = 0x8A040010;
        /** {@value}: Error in DCT stream: Invalid progressive parameters Ss=%d Se=%d Ah=%d Al=%d. */
        public final static int JPEG_E_JERR_BAD_PROGRESSION         = 0x8A040011;
        /** {@value}: Error in DCT stream: Invalid progressive parameters at scan script entry %d. */
        public final static int JPEG_E_JERR_BAD_PROG_SCRIPT         = 0x8A040012;
        /** {@value}: Error in DCT stream: Bogus sampling factors. */
        public final static int JPEG_E_JERR_BAD_SAMPLING            = 0x8A040013;
        /** {@value}: Error in DCT stream: Invalid scan script at entry %d. */
        public final static int JPEG_E_JERR_BAD_SCAN_SCRIPT         = 0x8A040014;
        /** {@value}: Error in DCT stream: Improper call to JPEG library in state %d. */
        public final static int JPEG_E_JERR_BAD_STATE               = 0x8A040015;
        /** {@value}: Error in DCT stream: JPEG parameter struct mismatch: library thinks size is %u, caller expects %u. */
        public final static int JPEG_E_JERR_BAD_STRUCT_SIZE         = 0x8A040016;
        /** {@value}: Error in DCT stream: Bogus virtual array access. */
        public final static int JPEG_E_JERR_BAD_VIRTUAL_ACCESS      = 0x8A040017;
        /** {@value}: Error in DCT stream: Buffer passed to JPEG library is too small. */
        public final static int JPEG_E_JERR_BUFFER_SIZE             = 0x8A040018;
        /** {@value}: Error in DCT stream: Suspension not allowed here. */
        public final static int JPEG_E_JERR_CANT_SUSPEND            = 0x8A040019;
        /** {@value}: Error in DCT stream: CCIR601 sampling not implemented yet. */
        public final static int JPEG_E_JERR_CCIR601_NOTIMPL         = 0x8A04001A;
        /** {@value}: Error in DCT stream: Too many color components: %d, max %d. */
        public final static int JPEG_E_JERR_COMPONENT_COUNT         = 0x8A04001B;
        /** {@value}: Error in DCT stream: Unsupported color conversion request. */
        public final static int JPEG_E_JERR_CONVERSION_NOTIMPL      = 0x8A04001C;
        /** {@value}: Error in DCT stream: Bogus DAC index %d. */
        public final static int JPEG_E_JERR_DAC_INDEX               = 0x8A04001D;
        /** {@value}: Error in DCT stream: Bogus DAC value 0x%x. */
        public final static int JPEG_E_JERR_DAC_VALUE               = 0x8A04001E;
        /** {@value}: Error in DCT stream: Bogus DHT index %d. */
        public final static int JPEG_E_JERR_DHT_INDEX               = 0x8A04001F;
        /** {@value}: Error in DCT stream: Bogus DQT index %d. */
        public final static int JPEG_E_JERR_DQT_INDEX               = 0x8A040020;
        /** {@value}: Error in DCT stream: Empty JPEG image (DNL not supported). */
        public final static int JPEG_E_JERR_EMPTY_IMAGE             = 0x8A040021;
        /** {@value}: Error in DCT stream: Read from EMS failed. */
        public final static int JPEG_E_JERR_EMS_READ                = 0x8A040022;
        /** {@value}: Error in DCT stream: Write to EMS failed. */
        public final static int JPEG_E_JERR_EMS_WRITE               = 0x8A040023;
        /** {@value}: Error in DCT stream: Didn't expect more than one scan. */
        public final static int JPEG_E_JERR_EOI_EXPECTED            = 0x8A040024;
        /** {@value}: Error in DCT stream: Input file read error. */
        public final static int JPEG_E_JERR_FILE_READ               = 0x8A040025;
        /** {@value}: Error in DCT stream: Output file write error --- out of disk space?. */
        public final static int JPEG_E_JERR_FILE_WRITE              = 0x8A040026;
        /** {@value}: Error in DCT stream: Fractional sampling not implemented yet. */
        public final static int JPEG_E_JERR_FRACT_SAMPLE_NOTIMPL    = 0x8A040027;
        /** {@value}: Error in DCT stream: Huffman code size table overflow. */
        public final static int JPEG_E_JERR_HUFF_CLEN_OVERFLOW      = 0x8A040028;
        /** {@value}: Error in DCT stream: Missing Huffman code table entry. */
        public final static int JPEG_E_JERR_HUFF_MISSING_CODE       = 0x8A040029;
        /** {@value}: Error in DCT stream: Maximum supported image dimension is %u pixels. */
        public final static int JPEG_E_JERR_IMAGE_TOO_BIG           = 0x8A04002A;
        /** {@value}: Error in DCT stream: Empty input file. */
        public final static int JPEG_E_JERR_INPUT_EMPTY             = 0x8A04002B;
        /** {@value}: Error in DCT stream: Premature end of input file. */
        public final static int JPEG_E_JERR_INPUT_EOF               = 0x8A04002C;
        /** {@value}: Error in DCT stream: Cannot transcode due to multiple use of quantization table %d. */
        public final static int JPEG_E_JERR_MISMATCHED_QUANT_TABLE  = 0x8A04002D;
        /** {@value}: Error in DCT stream: Scan script does not transmit all data. */
        public final static int JPEG_E_JERR_MISSING_DATA            = 0x8A04002E;
        /** {@value}: Error in DCT stream: Invalid color quantization mode change. */
        public final static int JPEG_E_JERR_MODE_CHANGE             = 0x8A04002F;
        /** {@value}: Error in DCT stream: Not implemented yet. */
        public final static int JPEG_E_JERR_NOTIMPL                 = 0x8A040030;
        /** {@value}: Error in DCT stream: Requested feature was omitted at compile time. */
        public final static int JPEG_E_JERR_NOT_COMPILED            = 0x8A040031;
        /** {@value}: Error in DCT stream: Arithmetic table 0x%02x was not defined. */
        public final static int JPEG_E_JERR_NO_ARITH_TABLE          = 0x8A040032;
        /** {@value}: Error in DCT stream: Backing store not supported. */
        public final static int JPEG_E_JERR_NO_BACKING_STORE        = 0x8A040033;
        /** {@value}: Error in DCT stream: Huffman table 0x%02x was not defined. */
        public final static int JPEG_E_JERR_NO_HUFF_TABLE           = 0x8A040034;
        /** {@value}: Error in DCT stream: JPEG datastream contains no image. */
        public final static int JPEG_E_JERR_NO_IMAGE                = 0x8A040035;
        /** {@value}: Error in DCT stream: Quantization table 0x%02x was not defined. */
        public final static int JPEG_E_JERR_NO_QUANT_TABLE          = 0x8A040036;
        /** {@value}: Error in DCT stream: Not a JPEG file: starts with 0x%02x 0x%02x. */
        public final static int JPEG_E_JERR_NO_SOI                  = 0x8A040037;
        /** {@value}: Error in DCT stream: Insufficient memory (case %d). */
        public final static int JPEG_E_JERR_OUT_OF_MEMORY           = 0x8A040038;
        /** {@value}: Error in DCT stream: Cannot quantize more than %d color components. */
        public final static int JPEG_E_JERR_QUANT_COMPONENTS        = 0x8A040039;
        /** {@value}: Error in DCT stream: Cannot quantize to fewer than %d colors. */
        public final static int JPEG_E_JERR_QUANT_FEW_COLORS        = 0x8A04003A;
        /** {@value}: Error in DCT stream: Cannot quantize to more than %d colors. */
        public final static int JPEG_E_JERR_QUANT_MANY_COLORS       = 0x8A04003B;
        /** {@value}: Error in DCT stream: Invalid JPEG file structure: %S before SOF. */
        public final static int JPEG_E_JERR_SOF_BEFORE              = 0x8A04003C;
        /** {@value}: Error in DCT stream: Invalid JPEG file structure: two SOF markers. */
        public final static int JPEG_E_JERR_SOF_DUPLICATE           = 0x8A04003D;
        /** {@value}: Error in DCT stream: Invalid JPEG file structure: missing SOS marker. */
        public final static int JPEG_E_JERR_SOF_NO_SOS              = 0x8A04003E;
        /** {@value}: Error in DCT stream: Unsupported JPEG process: SOF type 0x%02x. */
        public final static int JPEG_E_JERR_SOF_UNSUPPORTED         = 0x8A04003F;
        /** {@value}: Error in DCT stream: Invalid JPEG file structure: two SOI markers. */
        public final static int JPEG_E_JERR_SOI_DUPLICATE           = 0x8A040040;
        /** {@value}: Error in DCT stream: Failed to create temporary file %S. */
        public final static int JPEG_E_JERR_TFILE_CREATE            = 0x8A040041;
        /** {@value}: Error in DCT stream: Read failed on temporary file. */
        public final static int JPEG_E_JERR_TFILE_READ              = 0x8A040042;
        /** {@value}: Error in DCT stream: Seek failed on temporary file. */
        public final static int JPEG_E_JERR_TFILE_SEEK              = 0x8A040043;
        /** {@value}: Error in DCT stream: Write failed on temporary file --- out of disk space?. */
        public final static int JPEG_E_JERR_TFILE_WRITE             = 0x8A040044;
        /** {@value}: Error in DCT stream: Application transferred too few scanlines. */
        public final static int JPEG_E_JERR_TOO_LITTLE_DATA         = 0x8A040045;
        /** {@value}: Error in DCT stream: Unsupported marker type 0x%02x. */
        public final static int JPEG_E_JERR_UNKNOWN_MARKER          = 0x8A040046;
        /** {@value}: Error in DCT stream: Virtual array controller messed up. */
        public final static int JPEG_E_JERR_VIRTUAL_BUG             = 0x8A040047;
        /** {@value}: Error in DCT stream: Image too wide for this implementation. */
        public final static int JPEG_E_JERR_WIDTH_OVERFLOW          = 0x8A040048;
        /** {@value}: Error in DCT stream: Read from XMS failed. */
        public final static int JPEG_E_JERR_XMS_READ                = 0x8A040049;
        /** {@value}: Error in DCT stream: Write to XMS failed. */
        public final static int JPEG_E_JERR_XMS_WRITE               = 0x8A04004A;
        /** {@value}: Copyright (C) 2011, Thomas G. Lane, Guido Vollbeding. */
        public final static int JPEG_E_JMSG_COPYRIGHT               = 0x8A04004B;
        /** {@value}: 8c  16-Jan-2011. */
        public final static int JPEG_E_JMSG_VERSION                 = 0x8A04004C;
        /** {@value}: Error in DCT stream: Caution: quantization tables are too coarse for baseline JPEG. */
        public final static int JPEG_E_JTRC_16BIT_TABLES            = 0x8A04004D;
        /** {@value}: Error in DCT stream: Adobe APP14 marker: version %d, flags 0x%04x 0x%04x, transform %d. */
        public final static int JPEG_E_JTRC_ADOBE                   = 0x8A04004E;
        /** {@value}: Error in DCT stream: Unknown APP0 marker (not JFIF), length %u. */
        public final static int JPEG_E_JTRC_APP0                    = 0x8A04004F;
        /** {@value}: Error in DCT stream: Unknown APP14 marker (not Adobe), length %u. */
        public final static int JPEG_E_JTRC_APP14                   = 0x8A040050;
        /** {@value}: Error in DCT stream: Define Arithmetic Table 0x%02x: 0x%02x. */
        public final static int JPEG_E_JTRC_DAC                     = 0x8A040051;
        /** {@value}: Error in DCT stream: Define Huffman Table 0x%02x. */
        public final static int JPEG_E_JTRC_DHT                     = 0x8A040052;
        /** {@value}: Error in DCT stream: Define Quantization Table %d  precision %d. */
        public final static int JPEG_E_JTRC_DQT                     = 0x8A040053;
        /** {@value}: Error in DCT stream: Define Restart Interval %u. */
        public final static int JPEG_E_JTRC_DRI                     = 0x8A040054;
        /** {@value}: Error in DCT stream: Freed EMS handle %u. */
        public final static int JPEG_E_JTRC_EMS_CLOSE               = 0x8A040055;
        /** {@value}: Error in DCT stream: Obtained EMS handle %u. */
        public final static int JPEG_E_JTRC_EMS_OPEN                = 0x8A040056;
        /** {@value}: Error in DCT stream: End Of Image. */
        public final static int JPEG_E_JTRC_EOI                     = 0x8A040057;
        /** {@value}: Error in DCT stream:         %3d %3d %3d %3d %3d %3d %3d %3d. */
        public final static int JPEG_E_JTRC_HUFFBITS                = 0x8A040058;
        /** {@value}: Error in DCT stream: JFIF APP0 marker: version %d.%02d, density %dx%d  %d. */
        public final static int JPEG_E_JTRC_JFIF                    = 0x8A040059;
        /** {@value}: Error in DCT stream: Warning: thumbnail image size does not match data length %u. */
        public final static int JPEG_E_JTRC_JFIF_BADTHUMBNAILSIZE   = 0x8A04005A;
        /** {@value}: Error in DCT stream: JFIF extension marker: type 0x%02x, length %u. */
        public final static int JPEG_E_JTRC_JFIF_EXTENSION          = 0x8A04005B;
        /** {@value}: Error in DCT stream:     with %d x %d thumbnail image. */
        public final static int JPEG_E_JTRC_JFIF_THUMBNAIL          = 0x8A04005C;
        /** {@value}: Error in DCT stream: Miscellaneous marker 0x%02x, length %u. */
        public final static int JPEG_E_JTRC_MISC_MARKER             = 0x8A04005D;
        /** {@value}: Error in DCT stream: Unexpected marker 0x%02x. */
        public final static int JPEG_E_JTRC_PARMLESS_MARKER         = 0x8A04005E;
        /** {@value}: Error in DCT stream:         %4u %4u %4u %4u %4u %4u %4u %4u. */
        public final static int JPEG_E_JTRC_QUANTVALS               = 0x8A04005F;
        /** {@value}: Error in DCT stream: Quantizing to %d = %d*%d*%d colors. */
        public final static int JPEG_E_JTRC_QUANT_3_NCOLORS         = 0x8A040060;
        /** {@value}: Error in DCT stream: Quantizing to %d colors. */
        public final static int JPEG_E_JTRC_QUANT_NCOLORS           = 0x8A040061;
        /** {@value}: Error in DCT stream: Selected %d colors for quantization. */
        public final static int JPEG_E_JTRC_QUANT_SELECTED          = 0x8A040062;
        /** {@value}: Error in DCT stream: At marker 0x%02x, recovery action %d. */
        public final static int JPEG_E_JTRC_RECOVERY_ACTION         = 0x8A040063;
        /** {@value}: Error in DCT stream: RST%d. */
        public final static int JPEG_E_JTRC_RST                     = 0x8A040064;
        /** {@value}: Error in DCT stream: Smoothing not supported with nonstandard sampling ratios. */
        public final static int JPEG_E_JTRC_SMOOTH_NOTIMPL          = 0x8A040065;
        /** {@value}: Error in DCT stream: Start Of Frame 0x%02x: width=%u, height=%u, components=%d. */
        public final static int JPEG_E_JTRC_SOF                     = 0x8A040066;
        /** {@value}: Error in DCT stream:     Component %d: %dhx%dv q=%d. */
        public final static int JPEG_E_JTRC_SOF_COMPONENT           = 0x8A040067;
        /** {@value}: Error in DCT stream: Start of Image. */
        public final static int JPEG_E_JTRC_SOI                     = 0x8A040068;
        /** {@value}: Error in DCT stream: Start Of Scan: %d components. */
        public final static int JPEG_E_JTRC_SOS                     = 0x8A040069;
        /** {@value}: Error in DCT stream:     Component %d: dc=%d ac=%d. */
        public final static int JPEG_E_JTRC_SOS_COMPONENT           = 0x8A04006A;
        /** {@value}: Error in DCT stream:   Ss=%d, Se=%d, Ah=%d, Al=%d. */
        public final static int JPEG_E_JTRC_SOS_PARAMS              = 0x8A04006B;
        /** {@value}: Error in DCT stream: Closed temporary file %S. */
        public final static int JPEG_E_JTRC_TFILE_CLOSE             = 0x8A04006C;
        /** {@value}: Error in DCT stream: Opened temporary file %S. */
        public final static int JPEG_E_JTRC_TFILE_OPEN              = 0x8A04006D;
        /** {@value}: Error in DCT stream: JFIF extension marker: JPEG-compressed thumbnail image, length %u. */
        public final static int JPEG_E_JTRC_THUMB_JPEG              = 0x8A04006E;
        /** {@value}: Error in DCT stream: JFIF extension marker: palette thumbnail image, length %u. */
        public final static int JPEG_E_JTRC_THUMB_PALETTE           = 0x8A04006F;
        /** {@value}: Error in DCT stream: JFIF extension marker: RGB thumbnail image, length %u. */
        public final static int JPEG_E_JTRC_THUMB_RGB               = 0x8A040070;
        /** {@value}: Error in DCT stream: Unrecognized component IDs %d %d %d, assuming YCbCr. */
        public final static int JPEG_E_JTRC_UNKNOWN_IDS             = 0x8A040071;
        /** {@value}: Error in DCT stream: Freed XMS handle %u. */
        public final static int JPEG_E_JTRC_XMS_CLOSE               = 0x8A040072;
        /** {@value}: Error in DCT stream: Obtained XMS handle %u. */
        public final static int JPEG_E_JTRC_XMS_OPEN                = 0x8A040073;
        /** {@value}: Warning in DCT stream: Unknown Adobe color transform code %d. */
        public final static int JPEG_E_JWRN_ADOBE_XFORM             = 0x8A040074;
        /** {@value}: Warning in DCT stream: Corrupt JPEG data: bad arithmetic code. */
        public final static int JPEG_E_JWRN_ARITH_BAD_CODE          = 0x8A040075;
        /** {@value}: Warning in DCT stream: Inconsistent progression sequence for component %d coefficient %d. */
        public final static int JPEG_E_JWRN_BOGUS_PROGRESSION       = 0x8A040076;
        /** {@value}: Warning in DCT stream: Corrupt JPEG data: %u extraneous bytes before marker 0x%02x. */
        public final static int JPEG_E_JWRN_EXTRANEOUS_DATA         = 0x8A040077;
        /** {@value}: Warning in DCT stream: Corrupt JPEG data: premature end of data segment. */
        public final static int JPEG_E_JWRN_HIT_MARKER              = 0x8A040078;
        /** {@value}: Warning in DCT stream: Corrupt JPEG data: bad Huffman code. */
        public final static int JPEG_E_JWRN_HUFF_BAD_CODE           = 0x8A040079;
        /** {@value}: Warning in DCT stream: Warning: unknown JFIF revision number %d.%02d. */
        public final static int JPEG_E_JWRN_JFIF_MAJOR              = 0x8A04007A;
        /** {@value}: Warning in DCT stream: Premature end of JPEG file. */
        public final static int JPEG_E_JWRN_JPEG_EOF                = 0x8A04007B;
        /** {@value}: Warning in DCT stream: Corrupt JPEG data: found marker 0x%02x instead of RST%d. */
        public final static int JPEG_E_JWRN_MUST_RESYNC             = 0x8A04007C;
        /** {@value}: Warning in DCT stream: Invalid SOS parameters for sequential JPEG. */
        public final static int JPEG_E_JWRN_NOT_SEQUENTIAL          = 0x8A04007D;
        /** {@value}: Warning in DCT stream: Application transferred too many scanlines. */
        public final static int JPEG_E_JWRN_TOO_MUCH_DATA           = 0x8A04007E;

        // JPX compression module
        //
        /** {@value}: Error in JPX stream: Fatal. */
        public final static int JPX_ERROR_0                         = 0x8A057F9C;
        /** {@value}: Error in JPX stream: License_Level_Too_Low. */
        public final static int JPX_ERROR_1                         = 0x8A057FA5;
        /** {@value}: Error in JPX stream: Invalid_License. */
        public final static int JPX_ERROR_2                         = 0x8A057FA6;
        /** {@value}: Error in JPX stream: Invalid_Marker. */
        public final static int JPX_ERROR_3                         = 0x8A057FB7;
        /** {@value}: Error in JPX stream: Incompatible_Format. */
        public final static int JPX_ERROR_4                         = 0x8A057FB8;
        /** {@value}: Error in JPX stream: Invalid_Header. */
        public final static int JPX_ERROR_5                         = 0x8A057FB9;
        /** {@value}: Error in JPX stream: Invalid_Label. */
        public final static int JPX_ERROR_6                         = 0x8A057FBA;
        /** {@value}: Error in JPX stream: Maximum_Box_Size_Exceeded. */
        public final static int JPX_ERROR_7                         = 0x8A057FBB;
        /** {@value}: Error in JPX stream: Lossless_Compression_Mode. */
        public final static int JPX_ERROR_8                         = 0x8A057FBC;
        /** {@value}: Error in JPX stream: Invalid_Region. */
        public final static int JPX_ERROR_9                         = 0x8A057FBD;
        /** {@value}: Error in JPX stream: Transcode_Scale_Palette_Images. */
        public final static int JPX_ERROR_10                        = 0x8A057FBE;
        /** {@value}: Error in JPX stream: Transcoding_Finished. */
        public final static int JPX_ERROR_11                        = 0x8A057FBF;
        /** {@value}: Error in JPX stream: Use_SetPalette. */
        public final static int JPX_ERROR_12                        = 0x8A057FC0;
        /** {@value}: Error in JPX stream: Invalid_Channel_Definition. */
        public final static int JPX_ERROR_13                        = 0x8A057FC1;
        /** {@value}: Error in JPX stream: Invalid_Component_Mapping. */
        public final static int JPX_ERROR_14                        = 0x8A057FC2;
        /** {@value}: Error in JPX stream: Missing_Component_Mapping. */
        public final static int JPX_ERROR_15                        = 0x8A057FC3;
        /** {@value}: Error in JPX stream: Invalid_Palette. */
        public final static int JPX_ERROR_16                        = 0x8A057FC4;
        /** {@value}: Error in JPX stream: Missing_Palette. */
        public final static int JPX_ERROR_17                        = 0x8A057FC5;
        /** {@value}: Error in JPX stream: Use_SetLAB_Function. */
        public final static int JPX_ERROR_18                        = 0x8A057FC6;
        /** {@value}: Error in JPX stream: Use_SetICC_Function. */
        public final static int JPX_ERROR_19                        = 0x8A057FC7;
        /** {@value}: Error in JPX stream: Invalid_ICC_Profile. */
        public final static int JPX_ERROR_20                        = 0x8A057FC8;
        /** {@value}: Error in JPX stream: Invalid_Color_Spec_Index. */
        public final static int JPX_ERROR_21                        = 0x8A057FC9;
        /** {@value}: Error in JPX stream: Invalid_Meta_Data_Box_Index. */
        public final static int JPX_ERROR_22                        = 0x8A057FCA;
        /** {@value}: Error in JPX stream: JPX_File_Format_Required. */
        public final static int JPX_ERROR_23                        = 0x8A057FCB;
        /** {@value}: Error in JPX stream: File_Format_Required. */
        public final static int JPX_ERROR_24                        = 0x8A057FCC;
        /** {@value}: Error in JPX stream: Decompression_Cancelled. */
        public final static int JPX_ERROR_25                        = 0x8A057FCD;
        /** {@value}: Error in JPX stream: More_Bytes_Required. */
        public final static int JPX_ERROR_26                        = 0x8A057FCE;
        /** {@value}: Error in JPX stream: Max_Number_Of_ROIs_Reached. */
        public final static int JPX_ERROR_27                        = 0x8A057FD3;
        /** {@value}: Error in JPX stream: Invalid_Resolution_Type. */
        public final static int JPX_ERROR_28                        = 0x8A057FD5;
        /** {@value}: Error in JPX stream: Invalid_Resolution_Unit. */
        public final static int JPX_ERROR_29                        = 0x8A057FD6;
        /** {@value}: Error in JPX stream: Invalid_Resolution. */
        public final static int JPX_ERROR_30                        = 0x8A057FD7;
        /** {@value}: Error in JPX stream: Scale_Factor_Is_Too_Large. */
        public final static int JPX_ERROR_31                        = 0x8A057FD8;
        /** {@value}: Error in JPX stream: ROI_Shift_Failed. */
        public final static int JPX_ERROR_32                        = 0x8A057FD9;
        /** {@value}: Error in JPX stream: Invalid_Precinct_Dimensions. */
        public final static int JPX_ERROR_33                        = 0x8A057FDA;
        /** {@value}: Error in JPX stream: Invalid_Quantization_Filter_Pair. */
        public final static int JPX_ERROR_34                        = 0x8A057FDB;
        /** {@value}: Error in JPX stream: Trial_Time_Expired. */
        public final static int JPX_ERROR_35                        = 0x8A057FDC;
        /** {@value}: Error in JPX stream: Not_Yet_Supported. */
        public final static int JPX_ERROR_36                        = 0x8A057FDD;
        /** {@value}: Error in JPX stream: Invalid_Sample_Rate. */
        public final static int JPX_ERROR_37                        = 0x8A057FDE;
        /** {@value}: Error in JPX stream: Requested_File_Size_Too_Small. */
        public final static int JPX_ERROR_38                        = 0x8A057FDF;
        /** {@value}: Error in JPX stream: Byte_Compression_Mode. */
        public final static int JPX_ERROR_39                        = 0x8A057FE0;
        /** {@value}: Error in JPX stream: Cannot_Find_Suitable_Grid. */
        public final static int JPX_ERROR_40                        = 0x8A057FE1;
        /** {@value}: Error in JPX stream: Read_Callback_Undefined. */
        public final static int JPX_ERROR_41                        = 0x8A057FE2;
        /** {@value}: Error in JPX stream: Write_Callback_Undefined. */
        public final static int JPX_ERROR_42                        = 0x8A057FE3;
        /** {@value}: Error in JPX stream: Input_Callback_Undefined. */
        public final static int JPX_ERROR_43                        = 0x8A057FE4;
        /** {@value}: Error in JPX stream: Bits_Per_Sample_Too_High. */
        public final static int JPX_ERROR_44                        = 0x8A057FE5;
        /** {@value}: Error in JPX stream: Compression_Only_Property. */
        public final static int JPX_ERROR_45                        = 0x8A057FE6;
        /** {@value}: Error in JPX stream: Decompression_Only_Property. */
        public final static int JPX_ERROR_46                        = 0x8A057FE7;
        /** {@value}: Error in JPX stream: Quality_Compression_Mode. */
        public final static int JPX_ERROR_47                        = 0x8A057FE8;
        /** {@value}: Error in JPX stream: Set_Only_Property. */
        public final static int JPX_ERROR_48                        = 0x8A057FE9;
        /** {@value}: Error in JPX stream: Read_Only_Property. */
        public final static int JPX_ERROR_49                        = 0x8A057FEA;
        /** {@value}: Error in JPX stream: Single_Value_For_All_Tiles. */
        public final static int JPX_ERROR_50                        = 0x8A057FEB;
        /** {@value}: Error in JPX stream: Single_Value_For_All_Components. */
        public final static int JPX_ERROR_51                        = 0x8A057FEC;
        /** {@value}: Error in JPX stream: Invalid_Stream. */
        public final static int JPX_ERROR_52                        = 0x8A057FED;
        /** {@value}: Error in JPX stream: Invalid_Wavelet_Filter_Combination. */
        public final static int JPX_ERROR_53                        = 0x8A057FEE;
        /** {@value}: Error in JPX stream: Invalid_Resolution_Level. */
        public final static int JPX_ERROR_54                        = 0x8A057FEF;
        /** {@value}: Error in JPX stream: Invalid_Tile_Index. */
        public final static int JPX_ERROR_55                        = 0x8A057FF0;
        /** {@value}: Error in JPX stream: Invalid_Component_Dimensions. */
        public final static int JPX_ERROR_56                        = 0x8A057FF1;
        /** {@value}: Error in JPX stream: Invalid_Colorspace. */
        public final static int JPX_ERROR_57                        = 0x8A057FF2;
        /** {@value}: Error in JPX stream: Invalid_Tile_Arrangement. */
        public final static int JPX_ERROR_58                        = 0x8A057FF3;
        /** {@value}: Error in JPX stream: Invalid_Bits_Per_Sample. */
        public final static int JPX_ERROR_59                        = 0x8A057FF4;
        /** {@value}: Error in JPX stream: Invalid_Height. */
        public final static int JPX_ERROR_60                        = 0x8A057FF5;
        /** {@value}: Error in JPX stream: Invalid_Width. */
        public final static int JPX_ERROR_61                        = 0x8A057FF6;
        /** {@value}: Error in JPX stream: Invalid_Property_Key. */
        public final static int JPX_ERROR_62                        = 0x8A057FF7;
        /** {@value}: Error in JPX stream: Invalid_Property_Value. */
        public final static int JPX_ERROR_63                        = 0x8A057FF8;
        /** {@value}: Error in JPX stream: Invalid_Component_Index. */
        public final static int JPX_ERROR_64                        = 0x8A057FF9;
        /** {@value}: Error in JPX stream: Invalid_Number_Of_Components. */
        public final static int JPX_ERROR_65                        = 0x8A057FFA;
        /** {@value}: Error in JPX stream: Invalid_Pointer. */
        public final static int JPX_ERROR_66                        = 0x8A057FFB;
        /** {@value}: Error in JPX stream: Invalid_Handle. */
        public final static int JPX_ERROR_67                        = 0x8A057FFC;
        /** {@value}: Error in JPX stream: Failure_Write. */
        public final static int JPX_ERROR_68                        = 0x8A057FFD;
        /** {@value}: Error in JPX stream: Failure_Read. */
        public final static int JPX_ERROR_69                        = 0x8A057FFE;
        /** {@value}: Error in JPX stream: Failure_Malloc. */
        public final static int JPX_ERROR_70                        = 0x8A057FFF;

        // XML module
        //
        /** {@value}: XML line %line%:%column%: %message%. */
        public final static int XML_I_GENERAL                       = 0x0A080000;
        /** {@value}: XML line %line%:%column%: %message%. */
        public final static int XML_W_GENERAL                       = 0x0A088000;
        /** {@value}: XML line %line%:%column%: %message%. */
        public final static int XML_E_GENERAL                       = 0x8A080000;

        // XMP module
        //
        /** {@value}: The RDF feature '%feature%' is not implemented. */
        public final static int RDF_E_FEATNIMPL                     = 0x8A090001;
        /** {@value}: rdf:li is not allowed as attribute, only as element. */
        public final static int RDF_E_ATTRLI                        = 0x8A090002;
        /** {@value}: The RDF feature '%feature%' has been removed from the standard. */
        public final static int RDF_E_FEATREM                       = 0x8A090003;
        /** {@value}: The RDF feature '%feature%' is not allowed. */
        public final static int RDF_W_FEATNALLOW                    = 0x0A098004;
        /** {@value}: Invalid use of the reserved RDF name 'rdf:%n1%' as %n2%. */
        public final static int RDF_E_KEYWORD                       = 0x8A090005;
        /** {@value}: The RDF namespace should not be used outside of the RDF syntax (rdf:%name%). */
        public final static int RDF_I_RDFNS                         = 0x0A090006;
        /** {@value}: RDF resources should have only one identifier (rdf:resource, rdf:nodeID, rdf:about, rdf:ID, ...). */
        public final static int RDF_W_MULTID                        = 0x0A098007;
        /** {@value}: rdf:parseType='%type%' not recognized. */
        public final static int RDF_E_PARSETYPE                     = 0x8A090008;
        /** {@value}: Property '%property%' without namespace is ignored. */
        public final static int RDF_I_NONS                          = 0x0A090009;
        /** {@value}: Invalid URI(-reference) &lt;%uri%&gt;. */
        public final static int RDF_E_URI                           = 0x8A09000A;
        /** {@value}: The unqualified RDF attribute '%attr1%' is deprecated. Use 'rdf:%attr2%' instead. */
        public final static int RDF_I_UNQUAL                        = 0x0A09000B;
        /** {@value}: Invalid encoding of XMP packet: %enc%, expected %exp%. */
        public final static int XMP_W_PACKET_ENC                    = 0x0A09C001;
        /** {@value}: Additional non-XMP content is not allowed in XMP packet. */
        public final static int XMP_E_PACKET_ADDCONT                = 0x8A094002;
        /** {@value}: XMP packet header missing. */
        public final static int XMP_W_PACKET_NOHEAD                 = 0x0A09C003;
        /** {@value}: XMP packet trailer missing. */
        public final static int XMP_W_PACKET_NOTRAIL                = 0x0A09C004;
        /** {@value}: Missing attribute %attr% in XMP packet %packet%. */
        public final static int XMP_W_PACKET_ATTRMISS               = 0x0A09C005;
        /** {@value}: Deprecated content of XMP packet %packet% attribute '%attr%': '%content%'. */
        public final static int XMP_I_PACKET_ATTRCONT               = 0x0A094006;
        /** {@value}: Invalid content of XMP packet %packet% attribute '%attr%': '%content%'. */
        public final static int XMP_W_PACKET_ATTRCONT               = 0x0A09C006;
        /** {@value}: Invalid order of XMP packet %packet% attribute '%attr%'. */
        public final static int XMP_W_PACKET_ATTRORD                = 0x0A09C007;
        /** {@value}: Invalid spacing of XMP packet %attr% attributes. There must be exactly one space between each attribute. */
        public final static int XMP_W_PACKET_ATTRSPACE              = 0x0A09C008;
        /** {@value}: XMP packet %packet% attribute '%attr%' is deprecated. */
        public final static int XMP_I_PACKET_ATTR                   = 0x0A094009;
        /** {@value}: XMP packet %packet% attribute '%attr%' is forbidden. */
        public final static int XMP_W_PACKET_ATTR                   = 0x0A09C009;
        /** {@value}: Duplicate attribute '%attr%' in XMP packet %packet%. */
        public final static int XMP_I_PACKET_ATTRDUP                = 0x0A09400A;
        /** {@value}: The rdf:RDF element must not be omitted in XMP. */
        public final static int XMP_W_META_NORDFELEM                = 0x0A09C401;
        /** {@value}: The x:xmpmeta element must not contain any elements other than rdf:RDF. */
        public final static int XMP_E_META_ADDCONT                  = 0x8A094402;
        /** {@value}: There is only one RDF resource allowed in XMP. */
        public final static int XMP_W_META_MULTRES                  = 0x0A09C403;
        /** {@value}: There is only one RDF resource allowed in XMP. */
        public final static int XMP_E_META_MULTRES                  = 0x8A094403;
        /** {@value}: Toplevel typed nodes are not allowed in XMP (%prefix%:%name%). */
        public final static int XMP_W_META_MAINTYPED                = 0x0A09C404;
        /** {@value}: '%prefix%:%name%' is not allowed in arrays. The elements must be rdf:li or rdf:_N, where N is a positive number. */
        public final static int XMP_E_META_INVARRELEM               = 0x8A094405;
        /** {@value}: The x:xapmeta element is deprecated (but still allowed for compatibility). use x:xmpmeta instead. */
        public final static int XMP_I_META_XAPMETA                  = 0x0A094406;
        /** {@value}: Node type '%prefix%:%name%' is not allowed in XMP. The only node types allowed are rdf:Bag, rdf:Seq, and rdf:Alt. */
        public final static int XMP_W_META_INVTYPE                  = 0x0A09C407;
        /** {@value}: Typed literals (with rdf:datatype) are not allowed in XMP. */
        public final static int XMP_W_META_DATATYPE                 = 0x0A09C408;
        /** {@value}: Use of invalid namespace URI: &lt;%uri%&gt; does not end with '/' or '#'. */
        public final static int XMP_I_META_INVNSURI                 = 0x0A09440B;
        /** {@value}: Use of invalid namespace URI: &lt;%uri%&gt; does not end with '/' or '#'. */
        public final static int XMP_W_META_INVNSURI                 = 0x0A09C40B;
        /** {@value}: Anonymous RDF resources (rdf:Description without rdf:about attribute) are not allowed in XMP Metadata. */
        public final static int XMP_W_META_ANONRES                  = 0x0A09C40C;
        /** {@value}: Circular references in RDF resources are not allowed in XMP Metadata. */
        public final static int XMP_E_META_LOOP                     = 0x8A09440D;
        /** {@value}: Multiple occurrences of property '%prefix%:%name%'. */
        public final static int XMP_W_META_MULTPROP                 = 0x0A09C40E;
        /** {@value}: Invalid namespace '%ns%' for element &lt;rdf:RDF&gt;. Required is 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'. */
        public final static int XMP_W_META_INVRDFNS                 = 0x0A09C40F;
        /** {@value}: Namespace URI missing in schema description for '%schema%'. */
        public final static int XMP_E_SPARS_NOSCHEMURI              = 0x8A094841;
        /** {@value}: Multiple schema descriptions for schema namespace '%uri%'. */
        public final static int XMP_I_SPARS_MULTSCHEMURI            = 0x0A094842;
        /** {@value}: Multiple schema descriptions for schema namespace '%uri%'. */
        public final static int XMP_E_SPARS_MULTSCHEMURI            = 0x8A094842;
        /** {@value}: Property description without name found in schema '%schema%'. */
        public final static int XMP_E_SPARS_PROPNONAME              = 0x8A094843;
        /** {@value}: Value type missing in property description for '%prop%' in schema '%schema%'. */
        public final static int XMP_W_SPARS_PROPNOTYPE              = 0x0A09C844;
        /** {@value}: Value type description without name found in schema '%schema%'. */
        public final static int XMP_E_SPARS_TYPENONAME              = 0x8A094845;
        /** {@value}: Field description without name found in value type '%type%' in schema '%schema%'. */
        public final static int XMP_E_SPARS_FDNONAME                = 0x8A094846;
        /** {@value}: Value type missing in field description for '%field%' in value type '%type%' in schema '%schema%'. */
        public final static int XMP_W_SPARS_FDNOTYPE                = 0x0A09C847;
        /** {@value}: The schema description for namespace '%prefix%:' (%schema%) is missing. */
        public final static int XMP_W_SVAL_NOSCHEMA                 = 0x0A09C881;
        /** {@value}: The property '%prefix%:%name%' is not defined in schema '%schema%'. */
        public final static int XMP_W_SVAL_PROPNDEF                 = 0x0A09C882;
        /** {@value}: The property '%prefix%:%name%' is deprecated. Use '%reqprefix%:%reqname%' instead. */
        public final static int XMP_I_SVAL_PROPDEPR                 = 0x0A094883;
        /** {@value}: The property '%prefix%:%name%' is deprecated and thus forbidden. Use '%reqprefix%:%reqname%' instead. */
        public final static int XMP_W_SVAL_PROPDEPR                 = 0x0A09C883;
        /** {@value}: %XmpPath% :: Wrong value type. Expected type '%type%'. */
        public final static int XMP_W_SVAL_TYPE                     = 0x0A09C884;
        /** {@value}: The value type '%type%' used in an XMP extension schema is undefined. */
        public final static int XMP_W_SVAL_TYPENDEF                 = 0x0A09C885;
        /** {@value}: %XmpPath% :: The field '%field%' is not defined in value type '%type%'. */
        public final static int XMP_W_SVAL_FDNDEF                   = 0x0A09C886;
        /** {@value}: Unusual prefix '%prefix%:' for namespace '%ns%'. The preferred prefix is '%pref%:'. */
        public final static int XMP_I_SVAL_PREFIX                   = 0x0A094887;
        /** {@value}: Wrong prefix '%prefix%:' for namespace '%ns%'. The required prefix is '%req%:'. */
        public final static int XMP_W_SVAL_PREFIX                   = 0x0A09C887;
        /** {@value}: Wrong namespace '%found%' for value type '%type%'. The required namespace is '%exp%'. */
        public final static int XMP_W_SVAL_FDNS                     = 0x0A09C888;
        /** {@value}: %XmpPath% :: Missing required field '%field%' in value type '%type%'. */
        public final static int XMP_W_SVAL_FDMISS                   = 0x0A09C889;
        /** {@value}: %XmpPath% :: Missing language qualifier. */
        public final static int XMP_W_SVAL_NOLANG                   = 0x0A09C88A;
        /** {@value}: %XmpPath% :: Value removed. */
        public final static int XMP_I_SREP_RM                       = 0x0A0948C1;
        /** {@value}: %XmpPath% :: Value removed. */
        public final static int XMP_W_SREP_RM                       = 0x0A09C8C1;
        /** {@value}: %XmpPath% :: Property renamed to '%prefix%:%name%'. */
        public final static int XMP_I_SREP_MVPROP                   = 0x0A0948C2;
        /** {@value}: Changed prefix for schema '%schema%' from '%oldprefix%' to '%newprefix%'. */
        public final static int XMP_I_SREP_CHPREFIX                 = 0x0A0948C3;
        /** {@value}: %XmpPath% :: Changed field namespace from '%oldns%' to '%newns%'. */
        public final static int XMP_I_SREP_CHNS                     = 0x0A0948C4;
        /** {@value}: %XmpPath% :: Changed array type from '%oldarr%' to '%newarr%'. */
        public final static int XMP_I_SREP_CHARR                    = 0x0A0948C5;
        /** {@value}: %XmpPath% :: Wrapped value in array of type '%arrtype%'. */
        public final static int XMP_I_SREP_GENARR                   = 0x0A0948C6;
        /** {@value}: %XmpPath% :: Added xml:lang qualifier. */
        public final static int XMP_I_SREP_LANG                     = 0x0A0948C7;
        /** {@value}: %XmpPath% :: Changed literal value from '%oldvalue%' to '%newvalue%'. */
        public final static int XMP_I_SREP_CHVAL                    = 0x0A0948C8;
        /** {@value}: %XmpPath% :: Reordered array elements. */
        public final static int XMP_I_SREP_CHORD                    = 0x0A0948C9;
        /** {@value}: %XmpPath% :: Nodes have different types: '%type1%' vs. '%type2%'. */
        public final static int XMP_W_COMP_TYPE                     = 0x0A09CC01;
        /** {@value}: %XmpPath% :: Literals have different content: '%content1%' vs. '%content2%'. */
        public final static int XMP_W_COMP_LITCONT                  = 0x0A09CC02;
        /** {@value}: %XmpPath% :: Literals have different language: '%language1%' vs. '%language2%'. */
        public final static int XMP_W_COMP_LITLANG                  = 0x0A09CC03;
        /** {@value}: %XmpPath% :: Arrays have different size: '%size1%' vs. '%size2%'. */
        public final static int XMP_W_COMP_ARRSIZE                  = 0x0A09CC04;
        /** {@value}: %XmpPath% :: Node %lr% missing qualifier '%prefix%:%name%'. */
        public final static int XMP_W_COMP_QUALIFIER                = 0x0A09CC05;
        /** {@value}: Metadata %lr% missing property '%prefix%:%name%'. */
        public final static int XMP_W_COMP_PROPERTY                 = 0x0A09CC06;
        /** {@value}: %XmpPath% :: Properties/fields have different prefix: '%prefix1%' vs. '%prefix2%'. */
        public final static int XMP_W_COMP_PREFIX                   = 0x0A09CC07;
        /** {@value}: Metadata objects have different URIs: '%uri1%' vs. '%uri2%'. */
        public final static int XMP_W_COMP_METAURI                  = 0x0A09CC08;
        /** {@value}: %XmpPath% :: Structure %lr% missing field '%prefix%:%name%'. */
        public final static int XMP_W_COMP_FIELD                    = 0x0A09CC09;
        /** {@value}: %XmpPath% :: Structures have different field namespaces: '%ns1%' vs. '%ns2%'. */
        public final static int XMP_W_COMP_FIELDNS                  = 0x0A09CC0A;

        // CCITT Fax compression module
        //
        /** {@value}: Invalid entry type in TIFF dictionary. */
        public final static int TIFF_E_ENTRYTYPE                    = 0x8A0D0000;

        // OCR module
        //
        /** {@value}: OCR engine error: '%msg%'. */
        public final static int PDF_E_OCRENGINE                     = 0x8A0E0001;
        /** {@value}: OCR error: '%msg%'. */
        public final static int PDF_E_OCR                           = 0x8A0E0002;
        /** {@value}: OCR credits low: %count% remaining. */
        public final static int PDF_W_OCRCREDITS                    = 0x0A0E8003;

        // Flate compression module
        //
        /** {@value}: Error in Flate stream: stream end. */
        public final static int FLATE_ERROR_0                       = 0x8A110001;
        /** {@value}: Error in Flate stream: need dictionary. */
        public final static int FLATE_ERROR_1                       = 0x8A110002;
        /** {@value}: Error in Flate stream: incompatible version. */
        public final static int FLATE_ERROR_2                       = 0x8A117FFA;
        /** {@value}: Error in Flate stream: buffer error. */
        public final static int FLATE_ERROR_3                       = 0x8A117FFB;
        /** {@value}: Error in Flate stream: insufficient memory. */
        public final static int FLATE_ERROR_4                       = 0x8A117FFC;
        /** {@value}: Error in Flate stream: data error. */
        public final static int FLATE_WARNING_6                     = 0x0A11FFFD;
        /** {@value}: Error in Flate stream: data error. */
        public final static int FLATE_ERROR_5                       = 0x8A117FFD;
        /** {@value}: Error in Flate stream: stream error. */
        public final static int FLATE_ERROR_7                       = 0x8A117FFE;
        /** {@value}: Error in Flate stream: file error. */
        public final static int FLATE_ERROR_8                       = 0x8A117FFF;

        // License management module
        //
        /** {@value}: License management is not initialized. */
        public final static int LIC_E_NOTINIT                       = 0x8A120001;
        /** {@value}: No license set. */
        public final static int LIC_E_NOTSET                        = 0x8A120002;
        /** {@value}: License not found. */
        public final static int LIC_E_NOTFOUND                      = 0x8A120003;
        /** {@value}: Invalid license format. */
        public final static int LIC_E_FORMAT                        = 0x8A120004;
        /** {@value}: License was manipulated. */
        public final static int LIC_E_MANIP                         = 0x8A120005;
        /** {@value}: Unsupported license format version. */
        public final static int LIC_E_VERSION                       = 0x8A120006;
        /** {@value}: The license does not match the product. */
        public final static int LIC_E_PRODUCT                       = 0x8A120007;
        /** {@value}: The license does not match the current platform. */
        public final static int LIC_E_PLATFORM                      = 0x8A120008;
        /** {@value}: The license type does not match. */
        public final static int LIC_E_TYPE                          = 0x8A120009;
        /** {@value}: The license has expired. */
        public final static int LIC_E_EXPIRED                       = 0x8A12000A;
        /** {@value}: The maintainance period has expired. Use an older version of the product. */
        public final static int LIC_E_MEXPIRED                      = 0x8A12000B;
        /** {@value}: The current license does not permit the use of this function. */
        public final static int LIC_E_LEVEL                         = 0x8A12000C;
        /** {@value}: Incorrect type in license store. */
        public final static int LIC_E_STORE_TYPE                    = 0x8A120101;
        /** {@value}: Could not determine product name from key. */
        public final static int LIC_E_STORE_NAME                    = 0x8A120102;

        // Signature module
        //
        /** {@value}: Cannot create a session: %msg% */
        public final static int SIG_CREA_E_SESSION                  = 0x8A130101;
        /** {@value}: Cannot open certificate store. */
        public final static int SIG_CREA_E_STORE                    = 0x8A130102;
        /** {@value}: Certificate not found in store. */
        public final static int SIG_CREA_E_CERT                     = 0x8A130103;
        /** {@value}: Couldn't get response from OCSP server. */
        public final static int SIG_CREA_E_OCSP                     = 0x8A130104;
        /** {@value}: Couldn't get response from Time-stamp server. */
        public final static int SIG_CREA_E_TSP                      = 0x8A130105;
        /** {@value}: Private key not available. */
        public final static int SIG_CREA_E_PRIVKEY                  = 0x8A130106;
        /** {@value}: Server error: %msg% */
        public final static int SIG_CREA_E_SERVER                   = 0x8A130107;
        /** {@value}: Couldn't get response from CRL server. */
        public final static int SIG_CREA_E_CRL                      = 0x8A130108;
        /** {@value}: Invalid identity. */
        public final static int SIG_CREA_E_IDENTITY                 = 0x8A130109;
        /** {@value}: Permission denied. */
        public final static int SIG_CREA_E_PERMISSION               = 0x8A13010A;
        /** {@value}: Invalid certificate: %msg% */
        public final static int SIG_CREA_E_INVCERT                  = 0x8A13010B;
        /** {@value}: Unsupported algorithm found. */
        public final static int SIG_CREA_E_ALGO                     = 0x8A1301FE;
        /** {@value}: Program failure occurred. */
        public final static int SIG_CREA_E_FAILURE                  = 0x8A1301FF;
        /** {@value}: Malformed cryptographic message syntax (CMS). */
        public final static int SIG_VAL_E_CMS                       = 0x8A130201;
        /** {@value}: Digest mismatch (document has been modified). */
        public final static int SIG_VAL_E_DIGEST                    = 0x8A130202;
        /** {@value}: Signer's certificate is missing. */
        public final static int SIG_VAL_E_SIGNERCERT                = 0x8A130203;
        /** {@value}: Signature is not valid. */
        public final static int SIG_VAL_E_SIGNATURE                 = 0x8A130204;
        /** {@value}: None of the certificates was found in the store. */
        public final static int SIG_VAL_W_ISSUERCERT                = 0x0A138205;
        /** {@value}: The trust chain is not embedded. */
        public final static int SIG_VAL_W_NOTRUSTCHAIN              = 0x0A138206;
        /** {@value}: The Time-stamp is invalid. */
        public final static int SIG_VAL_W_TSP                       = 0x0A138207;
        /** {@value}: The Time-stamp certificate was not found in the store. */
        public final static int SIG_VAL_W_TSPCERT                   = 0x0A138208;
        /** {@value}: The Time-stamp is not present. */
        public final static int SIG_VAL_W_NOTSP                     = 0x0A138209;
        /** {@value}: The signature does not conform to the PAdES standard. */
        public final static int SIG_VAL_W_PADES                     = 0x0A13820A;
        /** {@value}: Unsupported algorithm found. */
        public final static int SIG_VAL_E_ALGO                      = 0x8A1302FE;
        /** {@value}: Program failure occurred. */
        public final static int SIG_VAL_E_FAILURE                   = 0x8A1302FF;

        // PDF split and merge module
        //
        /** {@value}: Document is signed. */
        public final static int PDF_SPLMRG_W_DOCSIGNED              = 0x0A148001;
        /** {@value}: XFA stream was not copied. */
        public final static int PDF_SPLMRG_W_RMXFA                  = 0x0A148002;
        /** {@value}: SubmitForm action was not copied. */
        public final static int PDF_SPLMRG_W_RMSUBMIT               = 0x0A148003;
        /** {@value}: Partial SubmitForm action altered to submit all fields. */
        public final static int PDF_SPLMRG_W_PARTSUBMIT             = 0x0A148004;
        /** {@value}: Signature annotation was not copied. */
        public final static int PDF_SPLMRG_W_RMSIGANNOT             = 0x0A148005;
        /** {@value}: Value or default value of field "%field%" was discarded due to field name collision. */
        public final static int PDF_SPLMRG_W_RMVALUE                = 0x0A148006;
        /** {@value}: Renamed field "%field%" to "%newname%" due to field name collision. */
        public final static int PDF_SPLMRG_W_MVFIELD                = 0x8A140007;
        /** {@value}: Cannot create appearance for annotation. */
        public final static int PDF_SPLMRG_E_ANNOTAPPEAR            = 0x8A140008;

        // XFA module
        //
        /** {@value}: The element '%qname%' must contain one single child element. */
        public final static int XFA_W_CHILDROOT                     = 0x0A158001;
        /** {@value}: %XPath% :: Missing child element '%name%' in namespace '%ns%'. */
        public final static int XFA_E_MISSELEM                      = 0x8A150002;
        /** {@value}: The feature '%name%' is not implemented. */
        public final static int XFA_E_FEATNSUP                      = 0x8A150003;
        /** {@value}: Invalid value '%value%' required is '%reqval%'. */
        public final static int XFA_W_INVVAL                        = 0x0A158004;
        /** {@value}: Invalid shortcut '%shortcut%' in SOM expression. */
        public final static int XFA_E_INVSHORTCUT                   = 0x8A150005;
        /** {@value}: Invalid SOM expression '%expression%'. */
        public final static int XFA_E_INVSOM                        = 0x8A150006;
        /** {@value}: SOM expression '%expression%' does not yield a single node. */
        public final static int XFA_E_SNSOM                         = 0x8A150007;

        // PDF Viewer
        //
        /** {@value}: Failed to save file %s. */
        public final static int VIEWER_E_SAVE_FILE                  = 0x8A160001;

        // Image to PDF converter module
        //
        /** {@value}: Created CMYK color profile for PDF/A output intent. */
        public final static int PDF_I2P_W_OUTPUTINTENT              = 0x0A178001;
        /** {@value}: Image soft mask removed in order to meet PDF/A-1 conformance. */
        public final static int PDF_I2P_W_SMASK                     = 0x0A178002;
        /** {@value}: JPEG2000 compression changed to JPEG in order to meet PDF/A-1 conformance. */
        public final static int PDF_I2P_W_JPXDECODE                 = 0x0A178003;
        /** {@value}: Invalid choice of compression. */
        public final static int PDF_I2P_E_COMPRESSION               = 0x0A178004;
        /** {@value}: Invalid value for bits per pixel. */
        public final static int PDF_I2P_E_BITSPERPIXEL              = 0x0A178005;

        // PDF Stamp module
        //
        /** {@value}: Invalid stamp xml data. */
        public final static int PDF_STMP_E_PSXML                    = 0x8A190001;
        /** {@value}: Invalid stamp description in ps:stamp. */
        public final static int PDF_STMP_E_PSSTAMP                  = 0x8A190002;
        /** {@value}: Invalid stamp content operator. */
        public final static int PDF_STMP_E_PSOP                     = 0x8A190003;
        /** {@value}: Stamping error: %msg% */
        public final static int PDF_STMP_E_PS                       = 0x8A190004;

        // PDF Creator module
        //
        /** {@value}: The font program cannot be read. */
        public final static int PDF_CREATOR_E_FONTPROG              = 0x8A1A0001;
        /** {@value}: The font cannot be found. */
        public final static int PDF_CREATOR_E_FONTNOTFOUND          = 0x8A1A0002;

        // PDF optimizer module
        //
        /** {@value}: Cannot create appearance for annotation. */
        public final static int PDF_OPT_E_ANNOTAPPEAR               = 0x8A1B0001;
        /** {@value}: Signature annotation was not copied. */
        public final static int PDF_OPT_W_RMSIGANNOT                = 0x0A1B8002;
    }

}
