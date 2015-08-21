package com.application.csproject6.smartalarmwalkietalkie;

import android.app.IntentService;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
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
    String TAG = "SD2";
    static final int UNDEFINED = 0;
    static final int YES = 1;
    static final int NO = 2;

    //process. 1 : UNDEFINED로 출발.
    public static int CurrentState = UNDEFINED;
    public static boolean IsMediaRunning = false;
    final String filepath = Environment.getExternalStorageDirectory().getPath();

    public static MediaPlayer SDMP;

    public SDpleaseWorkMKII(){
        super("SDpleaseWorkMKII");
    }

    // end while
    public static boolean endWhile = true;
    @Override
    protected void onHandleIntent(Intent intent) {
        //if I get Intent I became UNDEFINED
//        CurrentState = UNDEFINED;
        final ParseQuery<ParseObject> soundQuery = ParseQuery.getQuery("voiceMessage");
        ParseUser user = ParseUser.getCurrentUser();
        ParseObject current_group=((SampleApplication)getApplication()).getCurrent_group();
//        while((CurrentState = )== UNDEFINED)
        while (endWhile)
        {
            ParseUser.getCurrentUser().saveInBackground();
            soundQuery.whereEqualTo("group", current_group);
            soundQuery.whereEqualTo("receiver", user.getObjectId());
            soundQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (list == null || list.isEmpty()) {
                        Log.i("SDpleaseWorkMKII", "HI");
                        return;
                    } else {
                        if (e == null) {
                            for (ParseObject object : list) {
                                Log.i("receivingMsg", "savefile");
                                saveFile(object);
                                object.getList("receiver").remove(ParseUser.getCurrentUser().getObjectId());
                                object.saveInBackground();
                            }

                            // song oncepdateSong
//                            ((SampleApplication) getApplicationContext()).sortSongList();
//                            SongSong();

//                            if(firstsong == false) {
//                                SampleApplication.stopMusic();
//                                ((SampleApplication) getApplicationContext()).updateSongList();
//                                Song();
//                            }
//                            else //firstsong_true.
//                            {
//                                ((SampleApplication) getApplicationContext()).updateSongList();
//                            }


                        } else {
                            Log.d("score", "Error: " + e.getMessage());
                        }
                    }
                }
            });

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                //SampleApplication.stopMusic();
                return;
            }
        }

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }


    public void saveFile(ParseObject object){
        final ParseObject obj = object;
        ParseFile parse_file = (ParseFile) object.get("message");
        parse_file.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] bytes, ParseException e) {
                if (e == null) {
                    // data has the bytes for the resume
                    try {
                        // create temp file that will hold byte array
                        ParseUser sender = (ParseUser) obj.get("sender");
                        String filename = sender.getObjectId();
                        String currentFilePath = filepath + "/" + soundController.AUDIO_PLAYING_FOLDER + "/" + filename + ".wav";
                        File temp = new File(currentFilePath);
                        FileOutputStream fos = new FileOutputStream(temp);
                        fos.write(bytes);
                        fos.flush();
                        fos.close();

                        Log.d(TAG, "Saving File");
                        // song once pdateSong
                        ((SampleApplication) getApplicationContext()).sortSongList();
                        SongSong();
                    } catch (IOException ex) {
                        String s = ex.toString();
                        ex.printStackTrace();
                    }

                }
            }
        });
        return;
    }

    public void SongSong(){

        Log.d("spchoi" , "InSong");
        if(!((SampleApplication) getApplicationContext()).songs.isEmpty()){
            try {
                SampleApplication.Song.reset();
                SampleApplication.Song.setDataSource(((SampleApplication) getApplicationContext()).songs.get(0));
                SampleApplication.Song.prepare();
                SampleApplication.Song.start();

            } catch (IOException e) {
                Log.v(getString(R.string.app_name), e.getMessage());
            }
        }

    }

}
