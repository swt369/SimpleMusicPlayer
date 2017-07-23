package com.example.swt369.simplemusicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.Switch;

import java.io.IOException;

public class MyService extends Service {
    static String ACTION_PRE = "pre";
    static String ACTION_PAUSE = "pause";
    static String ACTION_PLAY = "play";
    static String ACTION_NEXT = "next";
    private BroadcastReceiver receiver;
    private MediaPlayer player = new MediaPlayer();
    private MyBinder binder = new MyBinder();

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals(ACTION_PRE)){

                }else if(action.equals(ACTION_PAUSE)){
                    if(player != null && player.isPlaying()){
                        player.pause();
                    }
                }else if(action.equals(ACTION_PLAY)){
                    if(player != null && !player.isPlaying()){
                        player.start();
                    }
                }else if(action.equals(ACTION_NEXT)){

                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PRE);
        intentFilter.addAction(ACTION_PAUSE);
        intentFilter.addAction(ACTION_PLAY);
        intentFilter.addAction(ACTION_NEXT);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        registerReceiver(receiver,intentFilter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void createNotification(){
        RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.notification_layout);



        Notification.Builder builder = new Notification.Builder(this);
    }

    class MyBinder extends Binder{
        void playSong(String path){
            if(player != null && player.isPlaying()){
                player.stop();
            }
            try {
                player.reset();
                player.setDataSource(path);
                player.prepare();
                player.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
