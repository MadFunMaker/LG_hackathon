package com.application.csproject6.smartalarmwalkietalkie;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;

/*
 <ProgressBar
                android:id = "@+id/startProgress_wt"
                android:layout_width="100dp"
                android:layout_height="100dp"
                android:max = "100"
                android:layout_alignTop="@+id/recBtn"
                android:layout_alignRight="@+id/recBtn"
                android:layout_alignEnd="@+id/recBtn"/>

            <Button
                android:id="@+id/recBtn_wt"
                android:layout_width="100dp"
                android:layout_height="100dp"
                android:layout_centerHorizontal="true"
                android:layout_marginTop="50dp"
                android:background="@drawable/recordicon" />
 */
public class wtActivity extends Activity {
    TextView name;
    ParseObject group;

    private user_Adapter adapter;
    private ListView listview;
    SampleApplication my_app;


    // Copied objects
    soundController voiceRecorder = null;
    ProgressBar progressbar;
    Button recBtn;
    boolean isRecording;
    int recording;

    // Deprecated
//    public static Activity RecordActivity;
//    Timer timer;
//    Thread thread;
//    int progress=0;

    // Intent
    Intent sIntent; //service Intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wt);

        // Actionbar (I don't know why we need this)
        ActionBar bar = getActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(R.layout.cust_actionbar);
        bar.setTitle("Group Broadcast");

        // set Record Activity
//        RecordActivity = wtActivity.this;

        // Progress bar
        progressbar = (ProgressBar)findViewById(R.id.startProgress_wt);
        progressbar.setVisibility(ProgressBar.GONE);

        // recording button
        recBtn = (Button) findViewById(R.id.recBtn_wt);
        voiceRecorder = new soundController();
        isRecording = false;

        // WT UI
        name = (TextView) findViewById(R.id.wtName);
        my_app=(SampleApplication)getApplication();
        name.setText(my_app.getCurrent_group().get("name").toString());

        // Create WtUserList
        my_app.WtUserList= new ArrayList<>();


        // Update List view
        updateListView();

        // Turn on receiver (SDpleaseWorkMKII)
        sIntent = new Intent(getApplicationContext(), SDpleaseWorkMKII.class);
        startService(sIntent);
//        Button recordVoiceBtn = (Button) findViewById(R.id.recordVoiceBtn);
//
//
//        recordVoiceBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "go to recordVoice", Toast.LENGTH_LONG).show();
//                Intent nextActivity = new Intent(getApplicationContext(), RecordVoiceActivity.class);
//                startActivity(nextActivity);
//            }
//        });
    }

    @Override
    public void onRestart() {
        super.onRestart();
        updateListView();
    }

    @Override
    protected void onResume(){
        super.onResume();
        updateListView();

        progressbar.setVisibility(ProgressBar.GONE);

//        timer = new Timer();

        //버튼을 뗄 때 녹음 종료
        recBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (isRecording != true) {
                        Log.i("ButtonUP", "why stopRecording??");
                        return true;
                    } else {
                        isRecording = false;
                        Log.i("ButtonUP", "stopRecording");
//                        timer.cancel();
                        voiceRecorder.stopRecord();
                        progressbar.setVisibility(ProgressBar.GONE);
                        Toast.makeText(getApplicationContext(), "Recording Stop", Toast.LENGTH_SHORT).show();

                        /*********************************************
                         *
                         * Broadcast to group members
                         *
                         *********************************************/
                        // set file
                        final File file = selectFile();

                        // Audio Manager setting
                        AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, 15, 0);

                        // Parse sending
                        ParseUser curUser = ParseUser.getCurrentUser();

                        ParseQuery<ParseObject> soundQuery = ParseQuery.getQuery("voiceMessage");
                        ParseObject current_group = ((SampleApplication) getApplication()).getCurrent_group();
                        soundQuery.whereEqualTo("group", current_group);
                        soundQuery.whereEqualTo("sender", curUser);
                        soundQuery.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> list, ParseException e) {
                                if (list == null || list.isEmpty()) {
                                    Log.i("sending", "Nothing to delete");
                                    return;
                                } else {
                                    for (ParseObject obj : list) {
                                        try {
                                            Log.i("sending", "Something deleted");
                                            obj.delete();
                                        } catch (ParseException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                }
                            }
                        });


                        ArrayList<String> nextUser = ((SampleApplication) getApplicationContext()).WtUserList;
                        ParseObject newVoiceMsg = new ParseObject("voiceMessage");

                        // send Voice to other users
                        Log.e("bsjeon", "nextUser : "+nextUser.size());

                        if (nextUser.size() > 0) {
                            newVoiceMsg.put("sender", curUser);
                            for (String groupUser : nextUser) {
                                newVoiceMsg.add("receiver", groupUser);
                            }

                            // FILE CREATE
//                        File file = selectFile();
                            FileInputStream fis;
                            try {
                                fis = new FileInputStream(file);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                return false;
                            }

                            byte[] buffer = new byte[(int) file.length()];
                            try {
                                fis.read(buffer, 0, buffer.length);
                                fis.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                                return false;
                            }

                            Log.e("bsjeon", "SEND FILE!");
                            ParseFile pfile = new ParseFile("sendFile", buffer);
                            newVoiceMsg.put("message", pfile);
                            newVoiceMsg.put("group", ((SampleApplication) getApplication()).getCurrent_group());
                            newVoiceMsg.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        file.delete();
                                    }
                                }
                            });

                            // PUSH LOGIC
                            /*
                            ParseQuery pushQuery = ParseInstallation.getQuery();
                            pushQuery.whereEqualTo("channels", "Giants");
                            ParsePush push = new ParsePush();
                            push.setQuery(pushQuery);
                            StringBuilder sv = new StringBuilder();
                            for(int i = 0; i < buffer.length - 1 ; i++)
                            {
                                sv.append(buffer[i]);
                            }
                            Log.d("spchoi" , sv.toString());
                            push.setMessage(sv.toString());
                            push.sendInBackground();
                            */
                        }
                        Log.i("listenVoice", "sendBtn to deleteFile");
                        deleteFile();

//                        Log.i("listenvoice", "RActivity Finish");
//                        RecordVoiceActivity RActivity = (RecordVoiceActivity)RecordVoiceActivity.RecordActivity;
//                        RActivity.finish();
//                        Log.i("listenvoice", "finish");
//                        finish();
                        // Deprecated
//                        Intent nextActivity = new Intent(getApplicationContext(),listenVoiceActivity.class);
//                        startActivity(nextActivity);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    recording = 0;
                    progressbar.setVisibility(ProgressBar.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Recording Start", Toast.LENGTH_SHORT).show();
                    voiceRecorder.startRecord();
                    isRecording = true;
//                    timer.schedule(new timerTask(), 8000);
                }

                return false;
            }
        });
    }

    public File selectFile(){
        soundController sdc = new soundController();
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File dir = new File(filepath, sdc.AUDIO_RECORDER_FOLDER);
        File[] fileList = dir.listFiles();

        /**
         * SORT FILES
         */
        ArrayList<File> filesArrayList = new ArrayList<File>();
        for (int ii=0; ii<fileList.length; ++ii) {
            filesArrayList.add(fileList[ii]);
        }

        Comparator<File> sortFiles = new Comparator<File>() {
            public int compare(File o1, File o2) {
                return o2.getName().compareTo(o1.getName());
            }
        };
        Collections.sort(filesArrayList, sortFiles); // 위에 설정한 내용대로 정렬(sort!)

        File file = filesArrayList.get(0);
        // Debug
//        for (int ii=0; ii<fileList.length; ++ii) {
//            Log.e("bsjeon", filesArrayList.get(ii).getName());
//        }

        return file;
    }

    public void deleteFile(){
        File file = selectFile();
        file.delete();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wt, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class user_Adapter extends BaseAdapter {
        private ArrayList<ParseUser> user_List;

        public user_Adapter() {
            user_List = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return user_List.size();
        }

        @Override
        public Object getItem(int i) {
            return user_List.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final int pos = position;
            final Context context = parent.getContext();


            if ( convertView == null ) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.user_status, parent, false);

                // TextView  position ? ?

                ParseUser user = user_List.get(position);
                try{
                    user.fetchIfNeeded();
                }
                catch(Exception e){

                }

                boolean isExist=false;
                for (int ii=0; ii<my_app.WtUserList.size(); ++ii) {
                    if (user.getObjectId().compareTo(my_app.WtUserList.get(ii)) == 0) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) my_app.WtUserList.add(user.getObjectId());
                Log.e("bsjeon","end rget view");

            }
            return convertView;

        }


        public void add(ParseUser _msg) {
            user_List.add(_msg);
        }

        public ParseUser get(int position) {
            return user_List.get(position);
        }
    }

    public void updateListView() {


        group = ((SampleApplication) getApplication()).getCurrent_group();
        try{
            group.fetchIfNeeded();
        }
        catch(Exception e){

        }
        List<ParseUser> users = group.getList("member");
        adapter = new user_Adapter();
        listview = (ListView) findViewById(R.id.userList_wt);


        if (users!=null) Log.e("bsjeon","users : " + users.size());
        else Log.e("bsjeon","users : null");

        if (users != null) {
            int i = 0;

            for (ParseUser user : users) {
                try{
                    user.fetchIfNeeded();
                }
                catch(Exception e){

                }
                Log.e("bsjeon","after fetch: ");
                if (!user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                    adapter.add(user);
                    i++;
                }
            }
            if (i != 0) {
                adapter.notifyDataSetChanged();
            }

        }
        listview.setAdapter(adapter);
    }
}
