package com.example.swt369.simplemusicplayer;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by swt369 on 2017/7/23.
 */

public final class SimplePlayerApplication extends Application {
    File destDir;
    ArrayList<String> songPaths;
    ArrayList<String> songNames;
    Stack<Integer> listened;
    int curPos;
    @Override
    public void onCreate() {
        super.onCreate();

        initializeDir();

        songPaths = new ArrayList<>();
        songNames = new ArrayList<>();
        listened = new Stack<>();
        File[] downloadSongs = destDir.listFiles();
        for(File file : downloadSongs){
            addNewSong(file);
        }
    }

    String getCurSongName(){
        return songNames.get(curPos);
    }

    String getCurSongPath(){
        return songPaths.get(curPos);
    }

    void addNewSong(File file){
        if(file.exists()){
            songPaths.add(file.getAbsolutePath());
            String name = file.getName();
            songNames.add(name.substring(0,name.length() - 4));
        }
    }

    void addNewSong(String path,String name){
        File file = new File(path);
        if(file.exists()){
            songPaths.add(path);
            songNames.add(name);
        }
    }

    private void initializeDir(){
        destDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath());
        SharedPreferences preference = getSharedPreferences("count",MODE_PRIVATE);
        int count = preference.getInt("count",0);
        if(count == 0){
            SDUtils.WriteRawToSDCard(this,R.raw.anima,destDir.getAbsolutePath(),"anima.mp3");
            SDUtils.WriteRawToSDCard(this,R.raw.intersectthunderbolt,destDir.getAbsolutePath(),"IntersectThunderbolt.mp3");
            SDUtils.WriteRawToSDCard(this,R.raw.zhongaiqingyou,destDir.getAbsolutePath(),"ZhongAiQingYou.mp3");
        }
        SharedPreferences.Editor editor = preference.edit();
        editor.putInt("count",++count);
        editor.apply();
    }

}
