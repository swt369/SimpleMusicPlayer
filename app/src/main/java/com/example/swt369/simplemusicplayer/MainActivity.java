package com.example.swt369.simplemusicplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private SimplePlayerApplication application;
    private ListView listView;
    private TextView textView;
    private int curlen;
    private boolean mBound = false;
    private MyService.MyBinder binder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        application = (SimplePlayerApplication)getApplication();

        Button buttonPre = (Button)findViewById(R.id.button_pre);
        buttonPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(MyService.ACTION_PRE);
                sendBroadcast(intent);
            }
        });

        Button buttonPause = (Button)findViewById(R.id.button_pause);
        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(MyService.ACTION_PAUSE);
                sendBroadcast(intent);
            }
        });

        Button buttonPlay = (Button)findViewById(R.id.button_play);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(MyService.ACTION_PLAY);
                sendBroadcast(intent);
            }
        });

        Button buttonNext = (Button)findViewById(R.id.button_next);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(MyService.ACTION_NEXT);
                sendBroadcast(intent);
            }
        });

        ScrollView scrollView= (ScrollView)findViewById(R.id.scroll_view);
        textView = new TextView(this);
        textView.setGravity(Gravity.CENTER);
        scrollView.addView(textView);

        listView = (ListView)findViewById(R.id.list_view);
        addSongsToListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                textView.setText(application.songNames.get(position));
                binder.playSong(position);
            }
        });

        Intent intent = new Intent(MainActivity.this,MyService.class);
        bindService(intent,connection,BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(connection);
    }

    private void addSongsToListView() {
        curlen = application.songNames.size();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.list_view_item_layout,application.songNames);
        listView.setAdapter(adapter);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (MyService.MyBinder)service;
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };
}
