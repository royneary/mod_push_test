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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private static final String SAVE_DIR = "gcmquickstart";
    private static final String REG_TOKEN_FILE = "token.txt";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private BroadcastReceiver mNotificationBroadcastReceiver;
    //private ProgressBar mRegistrationProgressBar;
    private TextView mInformationTextView;
    private TextView mNotificationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            //    //mRegistrationProgressBar.setVisibility(ProgressBar.GONE);

                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                String token = sharedPreferences.getString(QuickstartPreferences.GCM_REG_TOKEN, "");

                if( token.isEmpty() ) {
                    mInformationTextView.setText( getString( R.string.token_error_message ) );
                } else {
                    File sdCard = Environment.getExternalStorageDirectory();
                    String absolutePath = sdCard.getAbsolutePath() + "/" + SAVE_DIR;
                    File dir = new File( absolutePath );
                    dir.mkdirs();
                    File file = new File(dir, REG_TOKEN_FILE);
                    String msg;

                    try {
                        FileOutputStream f = new FileOutputStream( file );
                        f.write( token.getBytes() );
                        f.close();
                        msg = "saved token to " + absolutePath + "/" + REG_TOKEN_FILE;
                    }
                    catch( Exception e ) {
                        msg = "Could not save token to file: " + e.getMessage();
                    }
                    mInformationTextView.setText( msg );
                }
            
            }
        };
        mInformationTextView = (TextView) findViewById(R.id.informationTextView);

        mNotificationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String messageCount = intent.getStringExtra("message-count");
                String lastMessageSender = intent.getStringExtra("last-message-sender");
                String lastMessageBody = intent.getStringExtra("last-message-body");
                String subscriptionCount = intent.getStringExtra("pending-subscription-count");
       
                String msg = makeNotificationMessage(messageCount,
                                                     lastMessageSender,
                                                     lastMessageBody,
                                                     subscriptionCount);

                mNotificationTextView.setText(msg);
            }
        };
        mNotificationTextView = (TextView) findViewById(R.id.notificationTextView);
        if( savedInstanceState != null )
        {
            String msg = savedInstanceState.getString("currentNotification");
            if( msg != null )
            {
                mNotificationTextView.setText( msg );
            }
        }
        else
        {
            Bundle extras = getIntent().getExtras();
            String messageCount = extras.getString("message-count");
            String lastMessageSender = extras.getString("last-message-sender");
            String lastMessageBody = extras.getString("last-message-body");
            String subscriptionCount = extras.getString("pending-subscription-count");
            mNotificationTextView.setText( makeNotificationMessage(messageCount,
                                                                   lastMessageSender,
                                                                   lastMessageBody,
                                                                   subscriptionCount) );
        }
        
        //SharedPreferences sharedPreferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
        //String savedText = sharedPreferences.getString("currentNotification", "");
        //mNotificationTextView.setText(savedText);
        //if( savedInstanceState != null )
        //{
        //    String savedText = savedInstanceState.getString("currentNotification");
        //    if( savedText != null )
        //    {
        //        mNotificationTextView.setText(savedText);
        //    }
        //}

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent regIntent = new Intent(this, RegistrationIntentService.class);
            startService(regIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mNotificationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.INCOMING_NOTIFICATION));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNotificationBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("currentNotification", mNotificationTextView.getText().toString());
        super.onSaveInstanceState(outState);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private String makeNotificationMessage(String messageCount,
                                           String lastMessageSender,
                                           String lastMessageBody,
                                           String subscriptionCount)
    {
        if( messageCount != null && lastMessageSender != null && lastMessageBody != null &&
            subscriptionCount != null )
        {
            return
                "pending subscriptions: " + "\t" + subscriptionCount + "\n" +
                "message count: " + "\t" + messageCount + "\n" +
                "last sender: " + "\t" + lastMessageSender + "\n" +
                "last message: " + "\t" + lastMessageBody;
        }
        else
        {
            return "";
        }
    }
}
