package com.example.downloadfile;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;

import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DownloadService extends Service {
    public static final int UPDATE_PROGRESS = 8344;
    public static final String CHANNEL_ID = "channel";
    private String str;
    private URL url;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        str = intent.getStringExtra("url_file");
        downloadUrl();
        return START_NOT_STICKY;

    }

    private void downloadUrl() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
             downloadFile();

                Intent intentBroadcast = new Intent();
                intentBroadcast.setAction("Success");
                intentBroadcast.putExtra("TAG",CHANNEL_ID);
                stopSelf();
                sendBroadcast(intentBroadcast);
            }
        });
        thread.start();
    }


    private void downloadFile() {
        int count;
        int progress;
        try {
            url = new URL(str);
            Log.d("TAG", url.toString());
            URLConnection conection = url.openConnection();
            conection.connect();

            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = conection.getContentLength();


            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            if (input == null) {
                Log.d("TAG", "download khong thanh cong");
            }


            OutputStream output = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()
                    + "/downloadedfile.pdf");
//  android 11
//            ContentResolver contentResolver = getApplicationContext().getContentResolver();
//            ContentValues contentValues = new ContentValues();
//            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "file_pdf");
//            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
//            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
//            Uri uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues);


            byte data[] = new byte[4096];
//            OutputStream output = contentResolver.openOutputStream(uri);

            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                progress = (int) (total*100.0/fileLength);
                Log.d("TTT",progress +" ");
                // writing data to file
                output.write(data, 0, count);
                notificationDownloading(progress,"download");
            }
            Log.d("TAG", "downloadFile: done");
            // flushing output
            output.flush();
            // closing streams
            output.close();
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void notificationDownloading(int progress, String filename){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"download", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if(notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setContentTitle("Download")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setOngoing(true);



        int PROGRESS_MAX = 100;
        int PROGRESS_CURRENT = 0;
        builder.setProgress(PROGRESS_MAX, progress, false);
        notificationManager.notify(1, builder.build());

        if(progress == 100){
            notificationManager.cancelAll();
        }

    }

}