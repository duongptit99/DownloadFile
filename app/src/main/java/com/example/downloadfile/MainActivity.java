package com.example.downloadfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private IBroadcastReciver reciver;
    private Button btn_down;
    private EditText edt_url;
    private static final int REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_down = findViewById(R.id.button1);
        edt_url = findViewById(R.id.edit_url);

        reciver = new IBroadcastReciver();
        IntentFilter intentFilter = new IntentFilter("Success");
        registerReceiver(reciver,intentFilter);

        edt_url.setText("https://c1-ex-swe.nixcdn.com/Singer_Audio5/SuThatSauMotLoiHua-ChiDan-3316709.mp3?st=Y3tpeInoQqQywhx2vxRk7g&e=1625581497&download=true");
        btn_down.setOnClickListener(v -> {
            checkPermission();
        });


    }

    private void clickStartService() {
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("url_file",edt_url.getText().toString());
        startService(intent);
    }


    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission, REQUEST_CODE);
            } else {
                clickStartService();
            }
        } else {
            clickStartService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                clickStartService();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(reciver);
    }
}