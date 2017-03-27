package com.pdf_tools.pdfviewer.Model;

import java.awt.Toolkit;

import com.pdf_tools.pdfviewer.converter.geom.Point;
import com.pdf_tools.pdfviewer.converter.geom.Rectangle;

public class PdfUtils
{

    private final static int DPI = Toolkit.getDefaultToolkit().getScreenResolution();

    /**
     * Transforms pixel value to canvas coordinates.
     * 
     * @param pixel
     *            Value in pixel.
     * @return Outputs the pixel value in user space coordinates.
     */
    public static double viewportToCanvas(int pixel, double zoomFactor)
    {
        return pixel * 72.0 / DPI / zoomFactor;
    }

    public static Rectangle.Double viewportToCanvas(Rectangle.Integer viewportRect, double zoomFactor)
    {
        return new Rectangle.Double(viewportToCanvas(viewportRect.x, zoomFactor), viewportToCanvas(viewportRect.y, zoomFactor),
                viewportToCanvas(viewportRect.width, zoomFactor), viewportToCanvas(viewportRect.height, zoomFactor));
    }

    public static void normalizeRect(Point.Double start, Point.Double end)
    {
        if (end.x < start.x)
        {
            double t = start.x;
            start.x = end.x;
            end.x = t;
        }
        if (end.y < start.y)
        {
            double t = start.y;
            start.y = end.y;
            end.y = t;
        }

    }

    /**
     * Transforms user space coordinates to pixel coordinates.
     * 
     * @param userUnits
     *            Value in user space coordinates.
     * @return Outputs the value in pixel.
     */
    public static int canvasToPixel(boolean floor, double userUnits, double zoomFactor)
    {
        if (floor)
        {
            return (int) Math.floor(userUnits / 72.0 * DPI * zoomFactor);
        } else
        {
            return (int) Math.ceil(userUnits / 72.0 * DPI * zoomFactor);
        }
    }

    public static int canvasToPixel(double userUnits, double zoomFactor)
    {
        return canvasToPixel(false, userUnits, zoomFactor);
    }

    public static Double[] initDoubleArrayWithNaN(int length)
    {
        Double[] NaNArray = new Double[length];
        for (int i = 0; i < length; i++)
        {
            NaNArray[i] = Double.NaN;
        }
        return NaNArray;
    }

    public static Rectangle.Integer canvasToPixel(Rectangle.Double canvasRect, double zoomFactor)
    {
        return new Rectangle.Integer(canvasToPixel(canvasRect.x, zoomFactor), canvasToPixel(canvasRect.y, zoomFactor),
                canvasToPixel(canvasRect.width, zoomFactor), canvasToPixel(canvasRect.height, zoomFactor));
    }

    public static Rectangle.Integer canvasToViewportClipped(Rectangle.Double rectOnCanvas, double zoomFactor, Rectangle.Integer viewportRect)
    {
        Rectangle.Integer intersected = new Rectangle.Integer();
        Rectangle.intersect(canvasToPixel(rectOnCanvas, zoomFactor), viewportRect, intersected);
        intersected.x -= viewportRect.x;
        intersected.y -= viewportRect.y;
        return intersected;
    }

    public static void rotateRectangleDouble(Rectangle.Double rect, int rotation, double x, double y)
    {
        rotation = (rotation + 360) % 360;
        double temp;

        switch (rotation)
        {
        case 90:
        {
            temp = -(rect.y + rect.height - y) + x;
            rect.y = (rect.x - x) + y;
            rect.x = temp;
            temp = rect.width;
            rect.width = rect.height;
            rect.height = temp;
            break;
        }
        case 180:
        {
            rect.x = x - (rect.getMaxX() - x);
            rect.y = y - (rect.getMaxY() - y);
            break;
        }
        case 270:
        {
            temp = (rect.y - y) + x;
            rect.y = -(rect.getMaxX() - x) + y;
            rect.x = temp;
            temp = rect.width;
            rect.width = rect.height;
            rect.height = temp;
            break;

        }
        }
        return;
    }

    public static Rectangle.Double CalculateRectOnCanvas(Rectangle.Double rectOnPage, Rectangle.Double pageOnCanvasRect, int rotation)
    {
        Rectangle.Double rectOnCanvas = (Rectangle.Double) rectOnPage.clone(); // To
                                                                                   // not
                                                                                   // modify
                                                                                   // original
        rectOnCanvas.y = ((rotation % 180 == 0) ? pageOnCanvasRect.height : pageOnCanvasRect.width) - rectOnCanvas.y - rectOnCanvas.height;// transform
                                                                                                                                           // from
                                                                                                                                           // origin
                                                                                                                                           // botleft
                                                                                                                                           // to
                                                                                                                                           // topleft
        double widthHalf = pageOnCanvasRect.width / 2.0;
        double heightHalf = pageOnCanvasRect.height / 2.0;
        switch (rotation % 360)
        {
        case -270:
        case 90:
            rotateRectangleDouble(rectOnCanvas, rotation, widthHalf, widthHalf);
            break;
        case -180:
        case 180:
            rotateRectangleDouble(rectOnCanvas, rotation, widthHalf, heightHalf);
            break;
        case -90:
        case 270:
            rotateRectangleDouble(rectOnCanvas, rotation, heightHalf, heightHalf);
            break;
        default:
            break;
        }
        rectOnCanvas.x += pageOnCanvasRect.x;
        rectOnCanvas.y += pageOnCanvasRect.y;

        return rectOnCanvas;
    }

    public static Rectangle.Double IntersectSourceRects(Rectangle.Double r1, Rectangle.Double r2)
    {
        double x = Math.max(r1.x, r2.x);
        double y = Math.max(r1.y, r2.y);
        double w = Math.min(r1.getMaxX(), r2.getMaxX()) - x;
        double h = Math.min(r1.getMaxY(), r2.getMaxY()) - y;
        return new Rectangle.Double(x, y, w, h);
    }

    public static void ShrinkSourceRect(Rectangle.Double toShrink, double zoom)
    {
        double shave = (1.0 - zoom) / 2.0;
        toShrink.x += toShrink.width * shave;
        toShrink.y += toShrink.height * shave;
        toShrink.width *= zoom;
        toShrink.height *= zoom;
    }

    public static Point.Double CalcOnPageCoordinates(Point.Double onCanvasCoordinates, Rectangle.Double pageRect, int rotation)
    {
        Point.Double origin; // origin of page in canvas coordinates (botleft
                               // corner of unrotated page)
        switch (rotation)
        {
        case 0:
            origin = new Point.Double(pageRect.x, pageRect.getMaxY());
            break;
        case 90:
            origin = new Point.Double(pageRect.x, pageRect.y);
            break;
        case 180:
            origin = new Point.Double(pageRect.getMaxX(), pageRect.y);
            break;
        case 270:
            origin = new Point.Double(pageRect.getMaxX(), pageRect.getMaxY());
            break;
        default:
            throw new IllegalArgumentException("rotation " + rotation + " was not one of the valid values: [0, 90, 180, 270]");
        }

        Point.Double onPage = new Point.Double(onCanvasCoordinates.x - origin.x, onCanvasCoordinates.y - origin.y);
        RotateAroundOrigin(onPage, 360 - rotation);
        onPage.y = -onPage.y;
        return onPage;
    }

    public static void RotateAroundOrigin(Point.Double point, int rotation)
    {
        rotation = (rotation + 360) % 360;
        switch (rotation)
        {
        case 0:
            break;
        case 90:
            double x = point.x;
            point.x = -point.y;
            point.y = x;
            break;
        case 180:
            point.x = -point.x;
            point.y = -point.y;
            break;
        case 270:
            double xx = point.x;
            point.x = point.y;
            point.y = -xx;
            break;
        default:
            throw new IllegalArgumentException("rotation " + rotation + " was not one of the valid values: [0, 90, 180, 270]");
        }
    }

    public static double ShortestDistanceSquared(Rectangle.Double rect, Point.Double point)
    {
        double l = rect.x - point.x;
        double r = point.x - rect.getMaxX();
        double t = rect.y - point.y;
        double b = point.y - rect.getMaxY();
        double h = Math.max(0.0, Math.max(l, r));
        double v = Math.max(0.0, Math.max(t, b));
        return v * v + h * h;
    }
}
