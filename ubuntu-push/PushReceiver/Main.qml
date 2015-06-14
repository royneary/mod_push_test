import QtQuick 2.0
import Ubuntu.Components 1.1

import Ubuntu.Components.ListItems 0.1 as ListItem
import Ubuntu.PushNotifications 0.1

/*!
    \brief MainView with a Label and Button elements.
*/

MainView {
    // objectName for functional testing purposes (autopilot-qt5)
    objectName: "mainView"

    // Note! applicationName needs to match the "name" field of the click manifest
    applicationName: "com.ubuntu.developer.culrich.pushreceiver"

    /*
     This property enables the application to change orientation
     when the device is rotated. The default is false.
    */
    automaticOrientation: false

    // Removes the old toolbar and enables new features of the new header.
    useDeprecatedToolbar: false

    width: units.gu(100)
    height: units.gu(75)

    PushClient {
        id: pushClient
        Component.onCompleted: {
            notificationsChanged.connect(layoutList.handle_notifications)
            error.connect(layoutList.handle_error)
        }
        appId: "com.ubuntu.developer.culrich.pushreceiver_pushreceiver_0.1"
    }

    ListModel {
        id: notificationModel
        ListElement {
            type: "info"
            last_subscription_sender: "N/A"
            subscription_count: 0
            last_message_sender: "N/A"
            message_count: 0
        }
    }

    Page {
        title: i18n.tr("PushReceiver")

        Column {
            id: layoutColumn
            spacing: units.gu(1)
            anchors {
                margins: units.gu(2)
                fill: parent
            }

            Text {
                id: tokenHeadline
                text: "Token"
                font.bold: true
            }

            Text {
                id: tokenText
                anchors.top: tokenHeadline.bottom
                wrapMode: Text.Wrap
                text: pushClient.token
            }

            Text {
                id: notificationHeadline
                text: "Notification"
                anchors.top: tokenText.bottom
                anchors.topMargin: units.gu(2)
                font.bold: true
            }

            ListView {
                id: layoutList
                anchors.top: notificationHeadline.bottom
                model: notificationModel
                delegate: notificationDelegate

                Component {
                    id: notificationDelegate
                    TextEdit {
                        id: notificationText
                        wrapMode: Text.Wrap
                        text:
                            "last_subscription_sender: " + (last_subscription_sender?last_subscription_sender:"N/A") + "\n" +
                            "subscription_count: " + (subscription_count?subscription_count:"N/A") + "\n" +
                            "last_message_sender: " + (last_message_sender?last_message_sender:"N/A") + "\n" +
                            "message_count: " + (message_count?message_count:"N/A")
                    }
                }

                function handle_notifications(list) {
                    list.forEach(function(notification) {
                        var item = JSON.parse(notification)
                        notificationModel.insert(0, item)
                    })
                }

                function handle_error(error) {
                    tokenText.text = error
                }
            }
        }
    }
}

