package com.application.csproject6.smartalarmwalkietalkie;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.parse.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Random;

/**
 * Created by SPCHOI on 2015. 8. 21..
 */
public class SDpleaseWork extends com.parse.ParsePushBroadcastReceiver {


    public SDpleaseWork() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d("spchoi", "onReceive");
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {

        /*
        if(listenVoiceActivity.ScvsDream.equals("ISandIt"))
        {
            return; // already works.
        }
        */

        JSONObject pushData = null;

        try {
            intent.getStringExtra("com.parse.message");
            pushData = new JSONObject(intent.getStringExtra("com.parse.Data"));
        } catch (JSONException var7) {
            Log.d("spchoi", "Unexpected JSONException when receiving push data: ");
        }

        String action = null;
        if(pushData != null) {
            action = pushData.optString("action", (String)null);
        }

        if(action != null) {
            Bundle notification = intent.getExtras();
            Intent broadcastIntent = new Intent();
            broadcastIntent.putExtras(notification);
            broadcastIntent.setAction(action);
            broadcastIntent.setPackage(context.getPackageName());
            context.sendBroadcast(broadcastIntent);
        }

        //Notification notification1 = this.getNotification(context, intent);

        try {
            JSONObject scvsdrem = new JSONObject(intent.getStringExtra("com.parse.Data"));
            Log.d("spchoi" , scvsdrem.getString("alert")); //file byte array.

            //TODO: should make Audio Play Logic. should call Alarm receiver.
            //file로 써야함.
            try {

                byte[] sd_buffer = scvsdrem.getString("alert").getBytes(Charset.forName("UTF-8"));

                String currentFilePath = Environment.getExternalStorageDirectory().getPath()+"/musicfile.wav";
                File temp = new File(currentFilePath);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(temp);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    fos.write(sd_buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                MediaPlayer SDmusic = new MediaPlayer();
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(temp);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                FileDescriptor fd = null;
                try {
                    fd = fis.getFD();
                    SDmusic.setDataSource(fd);
                    SDmusic.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                SDmusic.start();


                Log.d("spchoi", "file wirte is successful");

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (JSONException var3) {
            Log.d("spchoi", "Unexpected JSONException when receiving push data: ");
        }

        Log.d("spchoi", "onPushReceive");
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
    }

    @Override
    protected Class<? extends Activity> getActivity(Context context, Intent intent) {
        return super.getActivity(context, intent);
    }

    @Override
    protected int getSmallIconId(Context context, Intent intent) {
        return super.getSmallIconId(context, intent);
    }

    @Override
    protected Bitmap getLargeIcon(Context context, Intent intent) {
        return super.getLargeIcon(context, intent);
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        return super.getNotification(context, intent);

    }
}
