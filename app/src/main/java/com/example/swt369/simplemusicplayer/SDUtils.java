package com.example.swt369.simplemusicplayer;

import android.content.Context;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by swt369 on 2017/7/23.
 */

final class SDUtils {
    static void WriteRawToSDCard(Context context, int id, String path,String fileName){
        File dir = new File(path);
        if(!dir.exists()){
            if(!dir.mkdir()){
                Log.i("failure","fail to write resource to SDCard");
                return;
            }
        }

        File file = new File(path + File.separator + fileName);
        if(file.exists()){
            return;
        }

        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = context.getResources().openRawResource(id);
            out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int count;
            while ((count = in.read(buffer)) > 0){
                out.write(buffer,0,count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(in);
            close(out);
        }
    }

    private static void close(Closeable closeable){
        if(closeable == null) return;
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
