package com.application.csproject6.smartalarmwalkietalkie;

import android.app.IntentService;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by SPCHOI on 2015. 8. 22..
 */
public class SDpleaseWorkMKII extends IntentService {

    static final int UNDEFINED = 0;
    static final int YES = 1;
    static final int NO = 2;

    //process. 1 : UNDEFINED로 출발.
    public static int CurrentState = UNDEFINED;
    public static boolean IsMediaRunning = false;


    public static MediaPlayer SDMP;

    public SDpleaseWorkMKII(){
        super("SDpleaseWorkMKII");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        //if I get Intent I became UNDEFINED
        CurrentState = UNDEFINED;
        ParseQuery<ParseObject> soundQuery = ParseQuery.getQuery("voiceMessage");
        ParseUser user = ParseUser.getCurrentUser();
        ParseObject current_group=((SampleApplication)getApplication()).getCurrent_group();
        while(CurrentState == UNDEFINED)
        {
            ParseUser.getCurrentUser().saveInBackground();
            soundQuery.whereEqualTo("group", current_group);
            soundQuery.whereEqualTo("receiver", user.getObjectId());
            soundQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (list == null || list.isEmpty()) {
                        Log.i("receivingMsg", "emptyList");
                        return;
                    } else {
                        if (e == null) {
                            for (ParseObject object : list) {
                                final ParseObject obj = object;
                                ParseFile parse_file = (ParseFile) object.get("message");
                                parse_file.getDataInBackground(new GetDataCallback() {
                                    @Override
                                    public void done(byte[] bytes, ParseException e) {
                                        if (e == null) {
                                            // data has the bytes for the resume
                                            /*
                                            try {
                                                // create temp file that will hold byte array
                                                ParseUser sender = (ParseUser) obj.get("sender");
                                                SDMP = new MediaPlayer();

                                                SDMP.setDataSource(fd);

                                                fos.write(bytes);
                                                fos.flush();
                                                fos.close();

                                                IsMediaRunning = true;

                                            } catch (IOException ex) {
                                                String s = ex.toString();
                                                ex.printStackTrace();
                                            }
                                            */

                                        }
                                    }
                                });


                                //make toast choose menu;
                                break;
                            }

                        } else {
                            Log.d("score", "Error: " + e.getMessage());
                        }
                    }
                }
            });

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                //SampleApplication.stopMusic();
                return;
            }
        }

    }
}
