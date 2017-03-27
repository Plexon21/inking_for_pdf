using System.Windows;

namespace ViewerWPFSample
{
    public partial class MessageBoxWindow : Window
    {
        public MessageBoxWindow(string text, string title)
        {
            InitializeComponent();
            Title = title;
            TextContent.Text = text;
            CloseButton.Focus();
        }

        /// <summary>
        /// Is the Password which the user chose.
        /// </summary>
        public string Password { get; set; }



        private void CopyToClipBoard_Click(object sender, RoutedEventArgs args)
        {
            Clipboard.SetText(TextContent.Text);
        }

        private void Close_Click(object sender, RoutedEventArgs args)
        {
            this.Close();
        }
    }
}
