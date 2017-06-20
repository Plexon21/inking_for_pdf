using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Input.StylusPlugIns;
using Microsoft.Ink;
using Stroke = System.Windows.Ink.Stroke;

namespace InkPlayground
{
    public class InkPane : Label, IDisposable, INotifyPropertyChanged
    {
        private InkPresenter ip;
        private DynamicRenderer dr;
        private StylusPointCollection stylusPoints;

        public InkPane()
        {
            ip = new InkPresenter();
            this.Content = ip;
            dr = new DynamicRenderer();
            ip.AttachVisuals(dr.RootVisual, dr.DrawingAttributes);
            this.StylusPlugIns.Add(dr);
            this.MouseRightButtonDown += RightMouseDownEventHandler;
            //this.ClipToBounds = true;
        }
        protected override void OnStylusDown(StylusDownEventArgs e)
        {
            //Stylus.Capture(this);

            stylusPoints = new StylusPointCollection();
            StylusPointCollection eventPoints =
                e.GetStylusPoints(this, stylusPoints.Description);

            stylusPoints.Add(eventPoints);
        }
        protected override void OnStylusMove(StylusEventArgs e)
        {
            if (stylusPoints == null)
            {
                return;
            }
            StylusPointCollection newStylusPoints = e.GetStylusPoints(this, stylusPoints.Description);
            stylusPoints.Add(newStylusPoints);
        }
        protected override void OnStylusUp(StylusEventArgs e)
        {
            if (stylusPoints == null)
            {
                return;
            }
            StylusPointCollection newStylusPoints = e.GetStylusPoints(this, stylusPoints.Description);
            stylusPoints.Add(newStylusPoints);

            Stroke stroke = new Stroke(stylusPoints);

            ip.Strokes.Add(stroke);
            stylusPoints = null;

            //Stylus.Capture(null);


        }

        private void RightMouseDownEventHandler(Object sender, MouseEventArgs e)
        {
            using (MemoryStream ms = new MemoryStream())
            {
                ip.Strokes.Save(ms);
                var myInkCollector = new InkCollector();
                var ink = new Ink();
                ink.Load(ms.ToArray());

                using (RecognizerContext context = new RecognizerContext())
                {
                    if (ink.Strokes.Count > 0)
                    {
                        context.Strokes = ink.Strokes;
                        RecognitionStatus status;

                        var result = context.Recognize(out status);

                        if (status == RecognitionStatus.NoError)
                            MessageBox.Show(result.TopString);
                        else
                            MessageBox.Show("Recognition failed");

                    }
                    else
                        MessageBox.Show("No stroke detected");
                }
            }
        }

        public void Dispose()
        {
        }

        public event PropertyChangedEventHandler PropertyChanged;
    }
}
