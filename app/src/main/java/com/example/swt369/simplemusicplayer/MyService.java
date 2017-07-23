package com.example.swt369.simplemusicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.io.IOException;
import java.util.Random;

public class MyService extends Service {
    private static int CODE_NOTIFICATION_PRE = 0x121;
    private static int CODE_NOTIFICATION_PAUSE = 0x122;
    private static int CODE_NOTIFICATION_PLAY = 0x123;
    private static int CODE_NOTIFICATION_NEXT = 0x124;
    private static int NOTIFICATION_ID = 1;
    static String ACTION_PRE = "pre";
    static String ACTION_PAUSE = "pause";
    static String ACTION_PLAY = "play";
    static String ACTION_NEXT = "next";
    static String ACTION_UPDATE_TEXTVIEW = "textview";
    private BroadcastReceiver receiver;
    private SimplePlayerApplication application;
    private MediaPlayer player = new MediaPlayer();
    private MyBinder binder = new MyBinder();
    private Random random = new Random();
    private NotificationManager manager;
    private Notification.Builder builder;
    private RemoteViews remoteViews;
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

        initializeNotification();
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

    private void initializeNotification(){
        manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        builder = new Notification.Builder(this);

        remoteViews = new RemoteViews(getPackageName(),R.layout.notification_layout);

        Intent intentPre = new Intent();
        intentPre.setAction(ACTION_PRE);
        PendingIntent pendingIntentPre = PendingIntent.getBroadcast(
                MyService.this,
                CODE_NOTIFICATION_PRE,
                intentPre,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_button_pre,pendingIntentPre);

        Intent intentPause = new Intent();
        intentPause.setAction(ACTION_PAUSE);
        PendingIntent pendingIntentPause = PendingIntent.getBroadcast(
                MyService.this,
                CODE_NOTIFICATION_PAUSE,
                intentPause,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        remoteViews.setOnClickPendingIntent(R.id.notification_button_pause,pendingIntentPause);

        Intent intentPlay = new Intent();
        intentPlay.setAction(ACTION_PLAY);
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(
                MyService.this,
                CODE_NOTIFICATION_PLAY,
                intentPlay,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        remoteViews.setOnClickPendingIntent(R.id.notification_button_play,pendingIntentPlay);

        Intent intentNext = new Intent();
        intentNext.setAction(ACTION_NEXT);
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(
                MyService.this,
                CODE_NOTIFICATION_NEXT,
                intentNext,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        remoteViews.setOnClickPendingIntent(R.id.notification_button_next,pendingIntentNext);

        builder.setSmallIcon(R.mipmap.ic_launcher);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setCustomContentView(remoteViews);
        }else {
            builder.setContent(remoteViews);
        }
    }

    private void createNotification(){
        remoteViews.setTextViewText(R.id.notification_text_view,application.getCurSongName());
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_NO_CLEAR;
        manager.notify(NOTIFICATION_ID,builder.build());
    }

    private void sendBroadcastForUpdateTextView(){
        Intent intent = new Intent(ACTION_UPDATE_TEXTVIEW);
        sendBroadcast(intent);
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
            application.curPos = pos;
            try {
                player.reset();
                player.setDataSource(application.getCurSongPath());
                player.prepare();
                prepared = true;
                player.start();
                if(needToPush){
                    application.listened.push(application.curPos);
                }
                sendBroadcastForUpdateTextView();
                createNotification();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
