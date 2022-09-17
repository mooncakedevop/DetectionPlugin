package com.detectionplugin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        String str = Build.MODEL;
        String res = "cat /proc/cpuinfo";
        TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String imei = "";
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imei = manager.getImei();
                DokitApplication.count++;
                Log.i("detection Point", new Throwable().getStackTrace().toString());
        } else {
             imei = manager.getDeviceId();
        }
    }
}
