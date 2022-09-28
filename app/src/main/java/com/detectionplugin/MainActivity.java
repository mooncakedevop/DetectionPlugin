package com.detectionplugin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("11","test finish");
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
        } else {
             imei = manager.getDeviceId();
        }
    }
}
