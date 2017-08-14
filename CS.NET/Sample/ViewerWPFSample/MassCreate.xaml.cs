using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;
using Xceed.Wpf.Toolkit;

namespace ViewerWPFSample
{
    /// <summary>
    /// Interaction logic for MassCreate.xaml
    /// </summary>
    public partial class MassCreate : Window
    {
        public event Action<int?, int?> ValuesChanged;

        public MassCreate()
        {
            InitializeComponent();
        }

        private void Send_Click(object sender, RoutedEventArgs e)
        {
            if (ValuesChanged == null) return;
            ValuesChanged(AnnotCount.Value, PointCount.Value);
            this.Close();
        }
    }
}


