package com.example.swt369.simplemusicplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final int CODE_ADD = 0x100;
    private SimplePlayerApplication application;
    private BroadcastReceiver receiver;
    private ListView listView;
    private TextView textView;
    private int songCount;
    private boolean mBound = false;
    private MyService.MyBinder binder;
    private ArrayList<String> listViewContents;
    private ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        application = (SimplePlayerApplication)getApplication();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals(MyService.ACTION_UPDATE_TEXTVIEW)){
                    textView.setText(application.getCurSongName());
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyService.ACTION_UPDATE_TEXTVIEW);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver,intentFilter);

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

        listViewContents = new ArrayList<>();
        listView = (ListView)findViewById(R.id.list_view);
        initializeSongsToListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position < songCount){
                    textView.setText(application.songNames.get(position));
                    binder.playSong(position);
                }else {
                    Intent intent = new Intent(MainActivity.this,FileChooser.class);
                    startActivityForResult(intent,CODE_ADD);
                }
            }
        });

        Intent intent = new Intent(MainActivity.this,MyService.class);
        bindService(intent,connection,BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
        unbindService(connection);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CODE_ADD && resultCode == FileChooser.CODE_RESULT){
            String path = data.getStringExtra("path");
            addSongToListView(path);
        }
    }

    private void initializeSongsToListView() {
        listViewContents.clear();
        listViewContents.addAll(application.songNames);
        songCount = listViewContents.size();
        listViewContents.add("+");
        adapter = new ArrayAdapter<>(this,R.layout.list_view_item_layout,listViewContents);
        listView.setAdapter(adapter);
    }

    private void addSongToListView(String path){
        File file = new File(path);
        if(!file.exists()){
            return;
        }
        application.songPaths.add(path);
        String name = file.getName();
        application.songNames.add(name.substring(0,name.length() - 4));
        listViewContents.add(listViewContents.size() - 1,name);
        songCount++;
        adapter.notifyDataSetChanged();
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
