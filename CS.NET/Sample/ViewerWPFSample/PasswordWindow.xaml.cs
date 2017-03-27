using System.Windows;

namespace ViewerWPFSample
{
    /// <summary>
    /// Interaktionslogik für PasswordWindow.xaml
    /// </summary>
    public partial class PasswordWindow : Window
    {
        public PasswordWindow()
        {
            InitializeComponent();
            PasswordBox.Focus();
        }

        /// <summary>
        /// Is the Password which the user chose.
        /// </summary>
        public string Password { get; set; }



        private void Ok_Click(object sender, RoutedEventArgs e)
        {
            Password = PasswordBox.Password;
            DialogResult = true;
            this.Close();
        }
        private void Cancel_Click(object sender, RoutedEventArgs e)
        {
            Password = "";
            DialogResult = false;
            this.Close();
        }
    }
}
