package com.application.csproject6.smartalarmwalkietalkie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class wtActivity extends Activity {
    TextView name;
    ParseObject group;

    private user_Adapter adapter;
    private ListView listview;
    SampleApplication my_app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wt);
        name = (TextView) findViewById(R.id.wtName);
        my_app=(SampleApplication)getApplication();
        name.setText(my_app.getCurrent_group().get("name").toString());
        Button recordVoiceBtn = (Button) findViewById(R.id.recordVoiceBtn);

        recordVoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "go to recordVoice", Toast.LENGTH_LONG).show();
                Intent nextActivity = new Intent(getApplicationContext(), RecordVoiceActivity.class);
                startActivity(nextActivity);
            }
        });
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
        public View getView(int position, final View convertView, ViewGroup parent) {
            final int pos = position;
            final Context context = parent.getContext();
            ParseUser user = user_List.get(position);
            user.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {

                    TextView name = (TextView) convertView.findViewById(R.id.sleepUser);

                }
            });

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
        Date current = new Date();


        group = ((SampleApplication) getApplication()).getCurrent_group();

        List<ParseUser> users = group.getList("member");
        adapter = new user_Adapter();
        listview = (ListView) findViewById(R.id.userList);
        my_app.LazyUserList = new ArrayList<>();

        if (users != null) {
            int i = 0;
            for (ParseUser user : users) {


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
