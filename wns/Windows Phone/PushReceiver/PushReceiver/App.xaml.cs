using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.ApplicationModel;
using Windows.ApplicationModel.Activation;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Animation;
using Windows.UI.Xaml.Navigation;

using Windows.Networking.PushNotifications;
using Windows.ApplicationModel.Background;
using Windows.UI.Core;
using Windows.Storage;
using System.Diagnostics;

// The Blank Application template is documented at http://go.microsoft.com/fwlink/?LinkId=391641

namespace PushReceiver
{

    /// <summary>
    /// Provides application-specific behavior to supplement the default Application class.
    /// </summary>
    public sealed partial class App : Application
    {
        private MainPage rootPage = null;
        public PushNotificationChannel Channel = null;
        private const string RAW_PUSH_TASK_NAME = "SampleBackgroundTask";
        private const string RAW_PUSH_TASK_ENTRY_POINT = "Tasks.SampleBackgroundTask";
        private CoreDispatcher _dispatcher;

        private TransitionCollection transitions;

        /// <summary>
        /// Initializes the singleton application object.  This is the first line of authored code
        /// executed, and as such is the logical equivalent of main() or WinMain().
        /// </summary>
        public App()
        {

            this.InitializeComponent();
            this.Suspending += this.OnSuspending;
        }

        /// <summary>
        /// Invoked when the application is launched normally by the end user.  Other entry points
        /// will be used when the application is launched to open a specific file, to display
        /// search results, and so forth.
        /// </summary>
        /// <param name="e">Details about the launch request and process.</param>
        protected override void OnLaunched(LaunchActivatedEventArgs e)
        {
#if DEBUG
            if (System.Diagnostics.Debugger.IsAttached)
            {
                this.DebugSettings.EnableFrameRateCounter = true;
            }
#endif

            Frame rootFrame = Window.Current.Content as Frame;

            // Do not repeat app initialization when the Window already has content,
            // just ensure that the window is active
            if (rootFrame == null)
            {
                // Create a Frame to act as the navigation context and navigate to the first page
                rootFrame = new Frame();

                // TODO: change this value to a cache size that is appropriate for your application
                rootFrame.CacheSize = 1;

                if (e.PreviousExecutionState == ApplicationExecutionState.Terminated)
                {
                    // TODO: Load state from previously suspended application
                }

                // Place the frame in the current Window
                Window.Current.Content = rootFrame;
                _dispatcher = Window.Current.Dispatcher;

            }

            if (rootFrame.Content == null)
            {
                // Removes the turnstile navigation for startup.
                if (rootFrame.ContentTransitions != null)
                {
                    this.transitions = new TransitionCollection();
                    foreach (var c in rootFrame.ContentTransitions)
                    {
                        this.transitions.Add(c);
                    }
                }

                rootFrame.ContentTransitions = null;
                rootFrame.Navigated += this.RootFrame_FirstNavigated;

                // When the navigation stack isn't restored navigate to the first page,
                // configuring the new page by passing required information as a navigation
                // parameter
                if (!rootFrame.Navigate(typeof(MainPage), e.Arguments))
                {
                    throw new Exception("Failed to create initial page");
                }
            }

            // Ensure the current window is active
            Window.Current.Activate();
        }

        /// <summary>
        /// Restores the content transitions after the app has launched.
        /// </summary>
        /// <param name="sender">The object where the handler is attached.</param>
        /// <param name="e">Details about the navigation event.</param>
        private async void RootFrame_FirstNavigated(object sender, NavigationEventArgs e)
        {
            var rootFrame = sender as Frame;
            rootFrame.ContentTransitions = this.transitions ?? new TransitionCollection() { new NavigationThemeTransition() };
            rootFrame.Navigated -= this.RootFrame_FirstNavigated;

            rootPage = MainPage.Current;

            if (rootPage == null)
            {
                Debug.WriteLine("rootPage is null!");
            }
            else
            {
                // Applications must have lock screen privileges in order to receive raw notifications
                BackgroundAccessStatus backgroundStatus = await BackgroundExecutionManager.RequestAccessAsync();

                // Make sure the user allowed privileges
                if (backgroundStatus != BackgroundAccessStatus.Denied && backgroundStatus != BackgroundAccessStatus.Unspecified)
                {
                    OpenChannelAndRegisterTask();
                }
                else
                {
                    // This event comes back in a background thread, so we need to move to the UI thread to access any UI elements
                    await _dispatcher.RunAsync(CoreDispatcherPriority.Normal, () =>
                    {
                        rootPage.NotifyUser("Lock screen access is denied", MainPage.NotifyType.NotificationMessage);
                    });
                }
            }
        }

        /// <summary>
        /// Invoked when application execution is being suspended.  Application state is saved
        /// without knowing whether the application will be terminated or resumed with the contents
        /// of memory still intact.
        /// </summary>
        /// <param name="sender">The source of the suspend request.</param>
        /// <param name="e">Details about the suspend request.</param>
        private void OnSuspending(object sender, SuspendingEventArgs e)
        {
            var deferral = e.SuspendingOperation.GetDeferral();

            // TODO: Save application state and stop any background activity
            deferral.Complete();
        }

        private async void OpenChannelAndRegisterTask()
        {
            // Open the channel. See the "Push and Polling Notifications" sample for more detail
            try
            {
                if (Channel == null)
                {
                    PushNotificationChannel channel = await PushNotificationChannelManager.CreatePushNotificationChannelForApplicationAsync();
                    string uri = channel.Uri;
                    Channel = channel;
                    // This event comes back in a background thread, so we need to move to the UI thread to access any UI elements
                    await _dispatcher.RunAsync(CoreDispatcherPriority.Normal, () =>
                    {
                        //OutputToTextBox(uri);
                        var escapedUri = Uri.EscapeUriString(uri);
                        Debug.WriteLine("Channel request succeeded (not escaped): " + uri);
                        Debug.WriteLine("Channel request succeeded (escaped): " + escapedUri);
                        rootPage.NotifyUser("Channel request succeeded:\n" + escapedUri, MainPage.NotifyType.ChannelMessage);
                    });
                }

                // Clean out the background task just for the purpose of this sample
                UnregisterBackgroundTask();
                RegisterBackgroundTask();
                Debug.WriteLine("Task registered");
            }
            catch (Exception ex)
            {
                rootPage.NotifyUser("Could not create a channel. Error number:" + ex.Message, MainPage.NotifyType.ChannelMessage);
            }
        }

        private void RegisterBackgroundTask()
        {
            Debug.WriteLine("in RegisterBackgroundTask!");

            BackgroundTaskBuilder taskBuilder = new BackgroundTaskBuilder();
            PushNotificationTrigger trigger = new PushNotificationTrigger();
            taskBuilder.SetTrigger(trigger);

            // Background tasks must live in separate DLL, and be included in the package manifest
            // Also, make sure that your main application project includes a reference to this DLL
            taskBuilder.TaskEntryPoint = RAW_PUSH_TASK_ENTRY_POINT;
            taskBuilder.Name = RAW_PUSH_TASK_NAME;

            try
            {
                BackgroundTaskRegistration task = taskBuilder.Register();
                task.Completed += BackgroundTaskCompleted;
            }
            catch (Exception ex)
            {
                rootPage.NotifyUser("Registration error: " + ex.Message, MainPage.NotifyType.NotificationMessage);
                UnregisterBackgroundTask();
            }
        }

        private bool UnregisterBackgroundTask()
        {
            Debug.WriteLine("in UnregisterBackgroundTask!");

            foreach (var iter in BackgroundTaskRegistration.AllTasks)
            {
                IBackgroundTaskRegistration task = iter.Value;
                if (task.Name == RAW_PUSH_TASK_NAME)
                {
                    task.Unregister(true);
                    return true;
                }
            }
            return false;
        }

        private async void BackgroundTaskCompleted(BackgroundTaskRegistration sender, BackgroundTaskCompletedEventArgs args)
        {
            await _dispatcher.RunAsync(CoreDispatcherPriority.Normal, () =>
            {
                string message = "Notification received:\n" + ApplicationData.Current.LocalSettings.Values[RAW_PUSH_TASK_NAME];
                rootPage.NotifyUser(message, MainPage.NotifyType.NotificationMessage);
            });
        }
    }
}