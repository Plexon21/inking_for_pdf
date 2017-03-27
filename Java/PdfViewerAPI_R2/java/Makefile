
K = com.pdf_tools.pdfviewer
P = com/pdf_tools/pdfviewer
F = com_pdf_tools_pdfviewer

SEP = :

CLP = -classpath ".$(SEP)../../libpdf/java"

all : headers jar

.SUFFIXES: .class .java

.java.class:
	echo javac $<
	javac -encoding ISO-8859-1 -target 1.5 -source 1.5 $(CLP) $<

SRC = $P/Model/PdfDestination.java \
$P/Model/IPdfCommon.java \
$P/Model/PdfViewerException.java \
$P/SwingAPI/PdfViewerComponent.java \


CLASSFILES = \
$P/converter/Color.class \
$P/converter/Converter.class \
$P/converter/geom/IShape.class \
$P/converter/geom/RectangularShape.class \
$P/converter/geom/Rectangle.class \
$P/converter/geom/Point.class \
$P/converter/geom/Dimension.class \
$P/Annotations/APdfAnnotation.class \
$P/Annotations/APdfMarkupAnnotation.class \
$P/Annotations/APdfTextMarkupAnnotation.class \
$P/Annotations/PdfGenericAnnotation.class \
$P/Annotations/PdfGenericMarkupAnnotation.java \
$P/Annotations/PdfHighlightAnnotation.class \
$P/Annotations/PdfLinkAnnotation.class \
$P/Annotations/PdfPopupAnnotation.class \
$P/Annotations/PdfQuadPoints.class \
$P/Annotations/PdfTempAnnotation.class \
$P/Annotations/PdfTextAnnotation.class \
$P/Annotations/PdfWidgetAnnotation.class \
$P/Model/DebugLogger.class \
$P/Model/IPdfCanvas.class \
$P/Model/IPdfControllerCallbackManager.class \
$P/Model/IPdfViewerController.class \
$P/Model/PdfCanvas.class \
$P/Model/PdfAdvancedDestination.class \
$P/Model/PdfDestination.class \
$P/Model/PdfOutlineItem.class \
$P/Model/PdfSearcher.class \
$P/Model/PdfTextFragment.class \
$P/Model/PdfUtils.class \
$P/Model/PdfViewerController.class \
$P/Model/PdfViewerException.class \
$P/caching/AlwaysRememberCache.class \
$P/caching/IGenericCache.class \
$P/DocumentManagement/IPdfDocument.class \
$P/DocumentManagement/IPdfDocumentManager.class \
$P/DocumentManagement/PdfDocument.class \
$P/DocumentManagement/PdfDocumentManagerMultithreaded.class \
$P/DocumentManagement/PdfDocumentManagerWorker.class \
$P/Requests/APdfRequest.class \
$P/Requests/IPdfRequest.class \
$P/Requests/PdfCloseRequest.class \
$P/Requests/PdfCreateAnnotationRequest.class \
$P/Requests/PdfDeleteAnnotationRequest.class \
$P/Requests/PdfDrawRequest.class \
$P/Requests/PdfEvent.class \
$P/Requests/PdfGetAnnotationsRequest.class \
$P/Requests/PdfGetOpenActionDestinationRequest.class \
$P/Requests/PdfGetOutlinesRequest.class \
$P/Requests/PdfGetPageLayoutRequest.class \
$P/Requests/PdfGetTextFragmentsRequest.class \
$P/Requests/PdfGetThumbnailRequest.class \
$P/Requests/PdfOpenRequest.class \
$P/Requests/PdfPageRangeRequest.class \
$P/Requests/PdfSaveRequest.class \
$P/Requests/PdfUpdateAnnotationRequest.class \
$P/SwingAPI/PdfAnnotationView.class \
$P/SwingAPI/PdfOutlineView.class \
$P/SwingAPI/PdfThumbnailView.class \
$P/SwingAPI/PdfViewerPane.class \
$P/SwingAPI/PdfViewerScrollBar.class \
$P/SwingAPI/PdfViewerComponent.class 

$P/DocumentManagement/IPdfDocument.class : $P/Model/IPdfCanvas.class
$P/Model/IPdfCanvas.class : $P/DocumentManagement/IPdfDocumentManager.class
$P/DocumentManagement/IPdfDocumentManager.class : $P/Model/PdfTextFragment.class
$P/Model/NativeObject.class : $P/SwingAPI/PdfAnnotationView.class
$P/SwingAPI/PdfViewerComponent.class : $P/SwingAPI/PdfViewerScrollBar.class
$P/SwingAPI/PdfAnnotationView.class : $P/SwingAPI/PdfViewerComponent.class

HEADERS = com_pdf_tools_pdfviewer_Model_NativeObject.h

LIBFILES = ../../libpdf/java/com/pdftools/NativeLibrary.class \
../../libpdf/java/com/pdftools/NativeLibrary.java \
../../libpdf/java/com/pdftools/Stream.class \
../../libpdf/java/com/pdftools/Stream.java

$P/DocumentManagement/IPdfDocument.class : $(LIBFILES)

DEL = - DEL /Q /F
CP  = - COPY
D = \ 

TITLE = 3-Heights&trade; PDF Viewer API R2

headers : $(HEADERS)

com_pdf_tools_pdfviewer_Model_NativeObject.h : $P/Model/NativeObject.class
	$(DEL) $@
	javah -jni $(CLP) com.pdf_tools.pdfviewer.Model.NativeObject

jar : PdfViewerAWT.jar

PdfViewerAWT.jar : $(CLASSFILES) $(LIBFILES)
	$(DEL) ../../libpdf/java/com/pdf_tools/*.scc
	jar cvf PdfViewerAWT.jar com/pdf_tools/pdfviewer/*/*.class
	cd ../../libpdf/java && jar uvf ../../PdfViewerAPI_R2/java/PdfViewerAWT.jar com/pdf_tools/*.class

javadoc : html/index.html

html : ; mkdir $@

html$Dstylesheet.css : ..$D..$Dresources$Dstylesheet.css
	$(CP) $? $@

html/index.html : $(SRC) html html$Dstylesheet.css
	javadoc -encoding ISO-8859-1 -windowtitle "$(TITLE)" -doctitle "$(TITLE)" -public -sourcepath . -d html -stylesheetfile html/stylesheet.css $(SRC)

clean :
	rm -f PdfViewerAWT.jar $P/*/*.class
