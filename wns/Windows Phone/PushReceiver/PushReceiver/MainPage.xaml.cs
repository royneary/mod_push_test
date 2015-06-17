using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;

using Windows.UI.Notifications;
using Windows.Data.Xml.Dom;
using Windows.Networking.PushNotifications;
using Windows.ApplicationModel.Background;
using System.Diagnostics;

using Windows.UI.Core;
using Windows.ApplicationModel.Core;

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=391641

namespace PushReceiver
{


    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class MainPage : Page
    {
        public static MainPage Current;

        public MainPage()
        {
            Current = this;            

            this.InitializeComponent();

            this.NavigationCacheMode = NavigationCacheMode.Required;

        }

        /// <summary>
        /// Invoked when this page is about to be displayed in a Frame.
        /// </summary>
        /// <param name="e">Event data that describes how this page was reached.
        /// This parameter is typically used to configure the page.</param>
        protected override void OnNavigatedTo(NavigationEventArgs e)
        {

        }

        private void tokenTextBlock_SelectionChanged(object sender, RoutedEventArgs e)
        {
            
        }

        private void notificationTextBlock_SelectionChanged(object sender, RoutedEventArgs e)
        {

        }

        public void NotifyUser(string strMessage, NotifyType type)
        {
            switch(type)
            {
                case NotifyType.ChannelMessage:
                    channelTextBlock.Text = strMessage;
                    break;

                case NotifyType.NotificationMessage:
                    notificationTextBlock.Text = strMessage;
                    break;
            }
        }

        public enum NotifyType
        {
            ChannelMessage,
            NotificationMessage
        };
    }
}
