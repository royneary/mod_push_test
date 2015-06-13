/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gcm.play.android.samples.com.gcmquickstart;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    private static final String notificationMessage = "new XMPP event";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        //String message = data.getString("message");
        //Log.d(TAG, "From: " + from);
        //Log.d(TAG, "Message: " + message);

        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */
        Intent incomingNotification = new Intent(QuickstartPreferences.INCOMING_NOTIFICATION);
        addPayload( data, incomingNotification );
        LocalBroadcastManager.getInstance(this).sendBroadcast(incomingNotification);

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        addPayload( data, mainActivityIntent );
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
        sendNotification(notificationMessage, mainActivityIntent);
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message, Intent mainActivityIntent) {
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, uniqueInt /* Request code */,
                mainActivityIntent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void addPayload(Bundle data, Intent intent)
    {
        String messageCount = data.getString("message_count", "");
        String pendingSubscriptionCount = data.getString("pending_subscription_count", "");
        String lastMessageSender = data.getString("last_message_sender", "");
        String lastMessageBody = data.getString("last_message_body", "");

        intent.putExtra("message-count", messageCount);
        intent.putExtra("pending-subscription-count", pendingSubscriptionCount);
        intent.putExtra("last-message-sender", lastMessageSender);
        intent.putExtra("last-message-body", lastMessageBody);
    }
}
