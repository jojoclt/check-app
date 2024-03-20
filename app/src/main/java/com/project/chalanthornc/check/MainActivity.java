package com.project.chalanthornc.check;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    TextView mac;
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private GoogleApiClient googleApiClient;
    protected Context context;
    public double lat, lon;
    TextView txtLat;
    double mainlat = 13.721270, mainlon = 100.522508;
    String status;
    float[] distance;
    Button btnin, btnout;

    FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button in = (Button) findViewById(R.id.btnin);
        Button out = (Button) findViewById(R.id.btnout);
        in.setOnClickListener(this);
        out.setOnClickListener(this);
        // init firebase
        db = FirebaseDatabase.getInstance();

        //mac = (TextView) findViewById(R.id.textView);
        //mac.setText();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_ACCESS_FINE_LOCATION);
        }


        googleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).addApi(AppIndex.API).build();
//        out(distance(lat, lon, mainlat, mainlon));
        //out(latitude);



    }

//    public double distance(double lat1, double lon1, double lat2, double lon2){
//        double p = 0.017453292519943295;
//        double a = 0.5 - Math.cos((lat2 - lat1) * p)/2 +
//                Math.cos(lat1 * p) * Math.cos(lat2 * p) * (
//                        1 - Math.cos((lon2 - lon1) * p))/2;
//        return 12742 * Math.asin(Math.sqrt(a));
//    }

//    public double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2){
//        double earthRadius = 6371; // meter
//        double dLat = Math.toRadians(lat2 - lat1);
//        double dLon = Math.toRadians(lon2 - lon1);
//        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
//                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
//                        Math.sin(dLon/2) * Math.sin(dLon);
//
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
//        double d = earthRadius * c;
//        return d;
//
//    }

//    public void distanceBetween (double startLatitude,
//                          double startLongitude,
//                          double endLatitude,
//                          double endLongitude,
//                          float[] results) {
//        Location locationA = new Location("point A");
//        locationA.setLatitude(startLatitude);
//        locationA.setLongitude(startLongitude);
//        Location locationB = new Location("point B");
//        locationB.setLatitude(endLatitude);
//        locationB.setLongitude(endLongitude);
//        float distance = locationA.distanceTo(locationB);
//        return results[];
//    }




    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnin:
                mac = (TextView) findViewById(R.id.textView);
                mac.setText("in");
                status = "checkIns";
                firebase();
                break;

            case R.id.btnout:
                mac = (TextView) findViewById(R.id.textView);
                mac.setText("out");
                status = "checkOuts";
                firebase();
                break;

            default:
                break;
        }
    }

//    @Override
//    public void onPause() {
//        //Log.i(("Lat Long"), lat + " " + lon);
//        Toast.makeText(this, lat + " " + lon, Toast.LENGTH_SHORT).show();
//        super.onPause();
//    }


    public void putTimestampIn(String macAddress, long timeStamp) {

        DatabaseReference myRef = db.getReference(status);
        myRef.child(macAddress).push().setValue(timeStamp);
    }

    public void firebase() {
        long timestamp = System.currentTimeMillis();
        if (lat != 0 && lon != 0) {
            String macAddress = getMacAddr();
            putTimestampIn(macAddress, timestamp);
            FirebaseDatabase database = db.getInstance();

            mac = (TextView) findViewById(R.id.textView);
            mac.setText(macAddress);
        }
        Toast.makeText(this, "WRITE TO DATABASE SUCESSFULLY", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                    //
                    Intent i = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage(getBaseContext().getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    finish();
                    startActivity(i);
                } else {
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {

                    if (b > 15) {
                        res1.append(Integer.toHexString(b & 0xFF) + ":");
                    } else {
                        res1.append("0" + Integer.toHexString(b & 0xFF) + ":");
                    }
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    public static void out(Object msg) {
        Log.w("infolog", msg.toString());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }


    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(MainActivity.class.getSimpleName(), "Connected to Google Play Services!");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            lat = lastLocation.getLatitude();
            lon = lastLocation.getLongitude();
            txtLat = (TextView) findViewById(R.id.textView1);
            txtLat.setText(lat + " " + lon);
            //out(lat);
//            firebase();
        }


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(MainActivity.class.getSimpleName(), "Can't connect to Google Play Services!");
    }
}

