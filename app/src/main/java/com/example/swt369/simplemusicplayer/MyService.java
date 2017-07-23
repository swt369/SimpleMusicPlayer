package com.example.swt369.simplemusicplayer;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.Random;

public class MyService extends Service {
    static String ACTION_PRE = "pre";
    static String ACTION_PAUSE = "pause";
    static String ACTION_PLAY = "play";
    static String ACTION_NEXT = "next";
    private BroadcastReceiver receiver;
    private SimplePlayerApplication application;
    private MediaPlayer player = new MediaPlayer();
    private MyBinder binder = new MyBinder();
    private Random random = new Random();
    private boolean prepared = false;

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        application = (SimplePlayerApplication)getApplication();

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nextSong();
            }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int size = application.songPaths.size();
                if(size == 0){
                    return;
                }
                String action = intent.getAction();
                if(action.equals(ACTION_PRE)){
                    if(!application.listened.empty()){
                        int pos = application.listened.pop();
                        binder.playSong(pos,false);
                    }
                }else if(action.equals(ACTION_PAUSE)){
                    if(player != null && player.isPlaying()){
                        player.pause();
                    }
                }else if(action.equals(ACTION_PLAY)){
                    if(player != null && prepared && !player.isPlaying()){
                        player.start();
                    }
                }else if(action.equals(ACTION_NEXT)){
                    nextSong();
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

    private void nextSong(){
        int size = application.songPaths.size();
        if(size == 0){
            return;
        }
        int pos = 0;
        if(size > 1){
            pos = random.nextInt(size);
            while(pos == application.curPos){
                pos = random.nextInt(size);
            }
        }
        binder.playSong(pos);
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
        void playSong(int pos){
            playSong(pos,true);
        }
        void playSong(int pos,boolean needToPush){
            if(player == null){
                player = new MediaPlayer();
            }
            if(player.isPlaying()){
                player.stop();
            }
            prepared = false;
            try {
                player.reset();
                player.setDataSource(application.songPaths.get(pos));
                player.prepare();
                prepared = true;
                player.start();
                if(needToPush){
                    application.listened.push(application.curPos);
                }
                application.curPos = pos;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
