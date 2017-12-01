package com.example.auser.gpsdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Network;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {

    static final int MIN_TIME = 5000;//5秒一次
    static final float MIN_DIS = 5;  //5公尺一次
    LocationManager mgr;
    TextView tv1, tv2, tvshow;
    boolean isGPSEnabled, isNetworkEnabled;//模擬器無法試網路
    EditText edt1, edt2;
    Geocoder geocoder;
    Location myLocation;
    String straddress;
//    Button bt

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv1 = (TextView) findViewById(R.id.textView1);
        tv2 = (TextView) findViewById(R.id.textView2);
        tvshow = (TextView) findViewById(R.id.textView6);
        mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        edt1 = (EditText) findViewById(R.id.editText4);
        edt2 = (EditText) findViewById(R.id.editText5);
        geocoder = new Geocoder(this, Locale.getDefault());

        //check permission
        checkpermission();


    }

    /////////////////////////////////////////////////
    private void checkpermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 200);//透過internet 200
        }

    }
    //=================================================================================

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (grantResults.length >= 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "需要權限定位", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //============================================================
    @Override
    protected void onPause() {//暫停時,關閉定位資訊
        super.onPause();
        //關閉定位更新
        enableLocationUpdated(false);

    }

    ////////////////////////////////////
    private void enableLocationUpdated(boolean isTurOn) {//定位開關
        if (ActivityCompat.checkSelfPermission(this
                , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //如果有開啟
            if (isTurOn) {
                isGPSEnabled = mgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkEnabled = mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (!isGPSEnabled && !isNetworkEnabled) {
                    Toast.makeText(getApplicationContext(), "No Signal!!!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Connection!!!", Toast.LENGTH_SHORT).show();
                    if (isGPSEnabled)
                        mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIS, this);//重新刷一
                    if (isNetworkEnabled)
                        mgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIS, this);//重新刷一
                }
            } else mgr.removeUpdates(this);
        }
    }
//==============================================================================================

    @Override
    protected void onResume() {
        super.onResume();
        tv1.setText("取得定位資訊中1");  //先清空之前資料
        //開啟定位更新功能
        String str = "GPS:" + (isGPSEnabled ? "On" : "Off");
        str += "\nNetwrok:" + (isNetworkEnabled ? "On" : "Off");
        tv1.setText(str);

        enableLocationUpdated(true);
    }

    //////////////////////////////////////////////////////////////////////////////
    public void setup(View v) {
        //啟動定位開關
        Intent it = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(it);

    }

    public void getLocation(View target) {
        if (myLocation != null) {
            edt1.setText(Double.toString(myLocation.getLatitude()));
            edt2.setText(Double.toString(myLocation.getLongitude()));


        } else {
            tvshow.setText("NO data display");
        }

    }

    public void onQuery(View target) {

        String strlat = edt1.getText().toString();
        String strlon = edt1.getText().toString();

        if (strlat.length() == 0 || strlon.length() == 0) {
            return;
        } else {
            double latitue = Double.parseDouble(strlat);
            double lontitue = Double.parseDouble(strlon);
            tvshow.setText("");
            try {
                List<Address> listaddr = geocoder.getFromLocation(latitue, lontitue, 1);
                if (listaddr == null || listaddr.size() == 0) {
                    straddress += "ERROR ADDRESS";
                } else {
                    Address address = listaddr.get(0);
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        straddress += address.getAddressLine(i) + "\n";
                    }
                }

            } catch (Exception e) {
                straddress += "ERROR ADDRESS" + e.toString();
            }
            tvshow.setText(straddress);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;
        String str = "Information: " + location.getProvider();
//        str+=String.format("\n緯度:%.5f\n經度:%.5f\n高度:%.2f"   //十進位
//                ,location.getLatitude(),location.getLongitude(),location.getAltitude());
        //60進位
        str += String.format("\n緯度:%.5s\n經度:%.5s\n高度:%.2f"   //十進位
                , Location.convert(location.getLatitude(), Location.FORMAT_SECONDS)
                , Location.convert(location.getLongitude(), Location.FORMAT_SECONDS)
                , location.getAltitude());
        tv2.setText(str);

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
