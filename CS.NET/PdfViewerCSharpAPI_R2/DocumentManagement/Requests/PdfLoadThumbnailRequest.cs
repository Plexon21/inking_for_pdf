// -----------------------------------------------------------------------
// <copyright file="PdfLoadThumbnailRequest.cs" company="">
// TODO: Update copyright text.
// </copyright>
// -----------------------------------------------------------------------

namespace PdfTools.PdfViewerCSharpAPI.DocumentManagement.Requests
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;
    using System.Windows;
    using PdfTools.PdfViewerCSharpAPI.Utilities;
    using PdfTools.PdfViewerCSharpAPI.Model;
    using System.Windows.Media.Imaging;

    public struct ThumbnailCacheArgs
    {
        public ThumbnailCacheArgs(bool loadPageRect, int rotation, PdfSourceRect sourceRect, int thumbnailWidth, int thumbnailHeight, int page, Resolution resolution)
        {
            this.loadPageRect = loadPageRect;
            this.rotation = rotation;
            this.sourceRect = sourceRect;
            this.thumbnailWidth = thumbnailWidth;
            this.thumbnailHeight = thumbnailHeight;
            this.page = page;
            this.resolution = resolution;
            
        }
        public bool loadPageRect;
        public PdfSourceRect sourceRect;
        public int page, thumbnailWidth, thumbnailHeight, rotation;
        public Resolution resolution;

        public static bool operator ==(ThumbnailCacheArgs a1, ThumbnailCacheArgs a2)
        {
            return (a1.page == a2.page) && (a1.rotation == a2.rotation) && (a1.resolution.xdpi == a2.resolution.xdpi) && (a1.resolution.ydpi == a2.resolution.ydpi);
        }
        public static bool operator !=(ThumbnailCacheArgs a1, ThumbnailCacheArgs a2)
        {
            return !(a1 == a2);
        }

        public override bool Equals(object obj)
        {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            if (!obj.GetType().IsInstanceOfType(this)) return false;
            return this == (ThumbnailCacheArgs) obj;
        }

        public override int GetHashCode()
        {
            int hash = 17;
            hash = hash * 23 + rotation.GetHashCode();
            hash = hash * 23 + thumbnailWidth.GetHashCode();
            hash = hash * 23 + thumbnailHeight.GetHashCode();
            hash = hash * 23 + page.GetHashCode();
            hash = hash * 23 + resolution.GetHashCode();

            return hash;
        }

    }

    /// <summary>
    /// TODO: Update summary.
    /// </summary>
    public class PdfLoadThumbnailRequest : APdfRequest<ThumbnailCacheArgs, WriteableBitmap>
    {



        /// <summary>
        /// Create the Drawrequest
        /// </summary>
        public PdfLoadThumbnailRequest(ThumbnailCacheArgs args)
            : base(args, 8)
        { }

        //public abstract void Execute(IPdfDocument document, IPdfControllerCallbackManager controller);
        public override void Execute(IPdfDocument document, IPdfControllerCallbackManager controller)
        {
            try
            {
                if(arguments.loadPageRect)
                {
                    PdfGetPageRectRequest request = new PdfGetPageRectRequest(arguments.page);
                    request.Execute(document, controller);
                    arguments.sourceRect = request.Wait().output;
                }
                InOutTuple tuple = new InOutTuple(arguments, ExecuteNative(document, arguments));
                this.completedEvent.TriggerEvent(tuple, null);
                triggerControllerCallback(controller, tuple, null);
            }
            catch (PdfViewerException ex)
            {
                this.completedEvent.TriggerEvent(ex);
                triggerControllerCallback(controller, ex);
            }
        }

        protected override WriteableBitmap ExecuteNative(IPdfDocument document, ThumbnailCacheArgs args)
        {

            if (args.sourceRect == null)
            {
                throw new NullReferenceException("source rect is null");
            }
            
            double sourceRectWidth;
            double sourceRectHeight;

            // if the rect was loaded from native code the rectangle isn't rotated
            if (arguments.loadPageRect)
            {
                sourceRectWidth = args.sourceRect.dWidth;
                sourceRectHeight = args.sourceRect.dHeight;
            }
            // rectangle from cache are being rotated when view rotation changes - return them to default orientation
            else
            {
                sourceRectWidth = args.rotation % 180 == 0 ? args.sourceRect.dWidth : args.sourceRect.dHeight;  //get sourcRectWidth of unrotated page
                sourceRectHeight = args.rotation % 180 == 0 ? args.sourceRect.dHeight : args.sourceRect.dWidth; //get sourcRectHeight of unrotated page
            }

            //find out what the targetrect looks like (it needs to be a scaled version of source)
            double wScale = ((double)args.thumbnailWidth) / sourceRectWidth;
            double hScale = ((double)args.thumbnailHeight) / sourceRectHeight;
            Int32Rect targetRect;
            if (wScale < hScale)
            {
                //use wScale
                int tHeight = Math.Max(1, (int)(wScale * sourceRectHeight));
                targetRect = new Int32Rect(0, 0, args.thumbnailWidth, tHeight);
            }
            else
            {
                //use hScale
                int tWidth = Math.Max(1, (int)(hScale * sourceRectWidth));
                targetRect = new Int32Rect(0, 0, tWidth, args.thumbnailHeight);
            }

            WriteableBitmap bitmap = document.LoadThumbnail(sourceRectWidth, sourceRectHeight, targetRect.Width, targetRect.Height, args.page, args.resolution);
            bitmap.Freeze();
            return bitmap;
        }
        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, InOutTuple tuple, PdfViewerException ex)
        {
            controller.OnThumbnailLoaded(tuple.arguments.page, tuple.output, ex);
        }
        protected override void triggerControllerCallback(IPdfControllerCallbackManager controller, PdfViewerException ex)
        {
            controller.OnThumbnailLoaded(this.arguments.page, null, ex);
        }
    }
}