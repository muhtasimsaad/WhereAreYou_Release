package com.example.muhta.whereareyou;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import static android.R.id.message;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    boolean dataReady =false;
    boolean debug=false;
    GPSTracker gps;
    Queue<Bitmap> q;
    Queue<String> n;
    ListView lv;
    SQLiteDatabase db;
    boolean clickable=false;
    Button search;
    static Long lastUpdated;
    Button accounts;
    Button logout;
    Button settings;
    LinearLayout layout;
    FirebaseAuth auth;
    String frndlstStrng="";
    static double frequency=0;
    static double broadcast=0;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    int markerCounter=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();

        }

        if(!isInternetConnected()){
            showInternetDialog();
        }

        try{auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            if (user == null) {

                Intent i = new Intent(MapsActivity.this, login.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);

            }
        }
        catch (Exception rr){

            Intent i = new Intent(MapsActivity.this, login.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
        readVariables();
        lastUpdated=(System.currentTimeMillis()/1000)-10;



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        q=new LinkedList<>();
        n=new LinkedList<>();
        lastUpdated=(System.currentTimeMillis()/1000)-1000;
        lv = (ListView) findViewById(R.id.listView);
        search = (Button) findViewById(R.id.search_buttom);
        accounts = (Button) findViewById(R.id.accounts);
        logout = (Button) findViewById(R.id.logout);
        settings=(Button) findViewById(R.id.settings);
        layout=(LinearLayout)findViewById(R.id.containerLayout);
        turnListOff();


        //checkLocationPermission();

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);

                gps=new GPSTracker(MapsActivity.this,mMap);

                if(gps.isGPSEnabled){
                    //
                    LatLng latLng = new LatLng(gps.getLatitude(),gps.getLongitude());
                    CameraUpdate location2 = CameraUpdateFactory.newLatLngZoom(
                            latLng, 15);
                    mMap.animateCamera(location2);
                }
                else{
                    gps.showSettingsAlert();
                }

            }
        }
        else {


            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            gps=new GPSTracker(MapsActivity.this,mMap);
            if(gps.isGPSEnabled){
                //
                LatLng latLng = new LatLng(gps.getLatitude(),gps.getLongitude());
                CameraUpdate location2 = CameraUpdateFactory.newLatLngZoom(
                        latLng, 15);
                mMap.animateCamera(location2);
            }
            else{
                gps.showSettingsAlert();
            }

        }


        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                turnOpacityOn();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!clickable){turnOpacityOn();return;}
                auth.signOut();
                Intent i = new Intent(MapsActivity.this, login.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);

            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!clickable){turnOpacityOn();return;}
                Intent i=new Intent(MapsActivity.this,settings.class);
                startActivity(i);
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!clickable){turnOpacityOn();return;}
                turnListOn();


            }
        });


        accounts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!clickable){turnOpacityOn();return;}
                Intent i=new Intent(MapsActivity.this,accounts.class);
                startActivity(i);

            }
        });


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                clickable=false;
                turnListOff();
            }
        });

        validateFriendList();



    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();

        }


    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int MY_PERMISSIONS_WRITE = 100;

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {

        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        gps=new GPSTracker(MapsActivity.this,mMap);
                        if(gps.canGetLocation){
                            //
                            LatLng latLng = new LatLng(gps.getLatitude(),gps.getLongitude());
                            CameraUpdate location2 = CameraUpdateFactory.newLatLngZoom(
                                    latLng, 15);
                            mMap.animateCamera(location2);
                            if(debug)Toast.makeText(getApplicationContext(),"Can get location --"+gps.getLongitude(),Toast.LENGTH_LONG).show();
                        }else{
                            if(debug)Toast.makeText(getApplicationContext(),"Cant get location",Toast.LENGTH_LONG).show();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED){
                        clearQueue();

                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    if(debug)Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }
    private void updateImage(String s){

        try {
            if (debug)
                Toast.makeText(getApplicationContext(), "Checking images", Toast.LENGTH_LONG).show();
            db = openOrCreateDatabase("whereRU", Context.MODE_PRIVATE, null);

            //checking metadata for recent changes
            //then downloading the updated images


            String id = FirebaseAuth.getInstance().getCurrentUser().getUid() + "";

                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().
                        child("Location").child(s).
                        child("picUploadTime");

                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {


                        try {
                             if(logic(ref.getParent().getKey() + "")) {
                                 if (debug)Toast.makeText(getApplicationContext(), ref.getParent().getKey() + "", Toast.LENGTH_LONG).show();

                                 downloadImage(ref.getParent().getKey() + "");
                            }
                            } catch (Exception rr) {
                        }
                    ref.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


//
//
//
        }
        catch (Exception rr){
            if(rr.getMessage().contains("Firebase")){
                Intent i = new Intent(MapsActivity.this, login.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);}
        }


    }
    private void turnOpacityOn(){

        search.getBackground().setAlpha(255);
        accounts.getBackground().setAlpha(255);
        settings.getBackground().setAlpha(255);
        logout.getBackground().setAlpha(255);
        layout.getBackground().setAlpha(1);

        clickable=true;

    }
    private void downloadImage(String s) throws IOException {

            final File localFile = File.createTempFile("images", "jpg");

            if(debug)Toast.makeText(getApplicationContext(),"Downloading for :"+s,Toast.LENGTH_LONG).show();

            Log.d("===","Download called");

            FirebaseStorage.getInstance().getReference().child(
                    s).getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            //Log.d("===", "Looking for : "+uid);
                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            //Toast.makeText(getApplicationContext(),taskSnapshot.getStorage().getName()+"",Toast.LENGTH_LONG).show();
                            Log.d("====",taskSnapshot.getStorage().getName()+"");
                            saveImage(bitmap,taskSnapshot.getStorage().getName());


                        }
                    }).addOnFailureListener(new OnFailureListener() {

                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle failed download
                    // ...
                    //Log.d("===", "UID : "+uid+"  ERROR :"+exception.getMessage());
                }
            });
    }
    private void saveImage(Bitmap finalBitmap,String uid) {


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED  ){
            q.add(finalBitmap);
            n.add(uid);
            if(debug)Toast.makeText(getApplicationContext(),"Download Queued",Toast.LENGTH_LONG).show();

            return;
        }
        else{


        if(debug)Toast.makeText(getApplicationContext(),"trying to save , -->"+uid,Toast.LENGTH_LONG).show();
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/WhereAreYou");
        myDir.mkdirs();
        String fname = "Image-"+ uid +".jpg";

        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {

            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            db.execSQL("UPDATE datas set data='"+System.currentTimeMillis()+"'" +
                    " where id='"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"' AND variable='picUploadTime';");
            Log.d("===","UploadTime updated"+ System.currentTimeMillis());

            if(debug)Toast.makeText(getApplicationContext(),"Image saved",Toast.LENGTH_LONG).show();

        } catch (Exception e) {

            Log.d("saad",e.getMessage());
            Toast.makeText(getApplicationContext(),"saving error :"+e.getMessage(),Toast.LENGTH_LONG).show();
        }
        }
    }
    private void clearQueue(){
        if(debug)Toast.makeText(getApplicationContext(),"Clearing Queue",Toast.LENGTH_LONG).show();
        for(int i=0;i<q.size();i++){
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/WhereAreYou");
        myDir.mkdirs();
        String fname = "Image-"+ n.poll() +".jpg";

        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {

            FileOutputStream out = new FileOutputStream(file);
            q.poll().compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            db.execSQL("UPDATE datas set data='"+System.currentTimeMillis()+"'" +
                    " where id='"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"' AND variable='picUploadTime';");
            Log.d("===","UploadTime updated"+ System.currentTimeMillis());

            Toast.makeText(getApplicationContext(),"Image saved",Toast.LENGTH_LONG).show();

        } catch (Exception e) {

            Log.d("saad",e.getMessage());
            Toast.makeText(getApplicationContext(),"saving error :"+e.getMessage(),Toast.LENGTH_LONG).show();
        }

        }
    }
    private void turnListOff() {
        logout.setVisibility(View.VISIBLE);
        search.setVisibility(View.VISIBLE);
        accounts.setVisibility(View.VISIBLE);
        settings.setVisibility(View.VISIBLE);
        layout.setVisibility(View.VISIBLE);


        logout.getBackground().setAlpha(45);
        search.getBackground().setAlpha(45);
        accounts.getBackground().setAlpha(45);
        settings.getBackground().setAlpha(45);
        layout.getBackground().setAlpha(1);
        clickable=false;
        lv.setVisibility(View.GONE);

    }
    private void turnListOn() {
        logout.setVisibility(View.GONE);
        search.setVisibility(View.GONE);
        accounts.setVisibility(View.GONE);
        settings.setVisibility(View.GONE);
        layout.setVisibility(View.GONE);
        lv.setVisibility(View.VISIBLE);
        db = openOrCreateDatabase("whereRU", Context.MODE_PRIVATE, null);
        if(dataReady) {

            Cursor cr=db.rawQuery("Select * from friends where userID='"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"';",null);
            String[] names=new String[cr.getCount()];
            String[] times=new String[cr.getCount()];
            Bitmap[] images=new Bitmap[cr.getCount()];
            final Double[] lat=new Double[cr.getCount()];
            final Double[] lon=new Double[cr.getCount()];

            int counter=0;
            while (cr.moveToNext()){

                names[counter]=cr.getString(2);
                times[counter]=timeConverter(Long.parseLong(cr.getString(5)));
                lat[counter]=cr.getDouble(3);
                lon[counter]=cr.getDouble(4);
                images[counter]=getImage(cr.getString(1));
                counter++;
            }
            downloadUpdateDatabase();

            CustomList adapter = new
                    CustomList(MapsActivity.this, names, times, images);

            lv.setAdapter(adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {


                    if(lat[position]!=0 && lon[position]!=0){
                    LatLng latLng = new LatLng(lat[position], lon[position]);
                    CameraUpdate location2 = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                    mMap.animateCamera(location2);
                    turnListOff();

                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"No location found",Toast.LENGTH_LONG).show();
                        turnListOff();
                    }

                    if(debug){ Toast.makeText(MapsActivity.this, "You Clicked at " +
                            position, Toast.LENGTH_SHORT).show();}

                }
            });

        }
    }
    private Bitmap getImage(String uid){
        Bitmap result;
        try{

            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/WhereAreYou");
            myDir.mkdirs();
            String fname = "Image-"+ uid +".jpg";
            File f = new File (myDir, fname);

            Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
            String s=bmp.toString();
            bmp=getResizedBitmap(bmp,80);
            result=bmp;

        }
        catch (Exception rr){

            Log.d("===","(Default image selected) Error : "+rr.getMessage());
            Bitmap icon = BitmapFactory.decodeResource(this.getResources(),R.drawable.profile);
            icon=getResizedBitmap(icon,80);
            result=icon;



        }
        return  getResizedBitmap(result,150);
    }
    private void readVariables(){
        try{
            db = openOrCreateDatabase("whereRU", Context.MODE_PRIVATE, null);
            Cursor C=db.rawQuery("SELECT * from datas where id='"+
                    FirebaseAuth.getInstance().getCurrentUser().getUid()+"'" +
                    ";",null);


            while (C.moveToNext()){


                if(C.getString(1).equals("frequency")){
                    frequency=C.getDouble(2);
                    if(debug)Toast.makeText(getApplicationContext(),"Frequency(M):"+C.getDouble(2),Toast.LENGTH_LONG).show();
                }


                if(C.getString(1).equals("broadcast")){
                    broadcast=C.getDouble(2);
                    if(debug)Toast.makeText(getApplicationContext(),"Broadcast(M):"+C.getDouble(2),Toast.LENGTH_LONG).show();
                }
            }


        }
        catch (Exception rr){
            if(debug)Toast.makeText(getApplicationContext(),rr.getMessage()+"",Toast.LENGTH_LONG).show();
        }
    }
    private void downloadString(){

        final DatabaseReference dref = FirebaseDatabase.getInstance().getReference().
                child("Friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "");

        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                frndlstStrng = dataSnapshot.getValue(String.class);

                db = openOrCreateDatabase("whereRU", Context.MODE_PRIVATE, null);
                String id=FirebaseAuth.getInstance().getCurrentUser().getUid();
                try{db.execSQL("UPDATE friendList set id='"
                        +id+"',friends='"+frndlstStrng+"' where id='"+id+"';");}
                catch (Exception r){
                    if(debug){  Toast.makeText(getApplicationContext(),r.getMessage(),Toast.LENGTH_LONG).show();}

                }
//
//
//
                String[] frnds=UIDBreaker(frndlstStrng);
                if(debug)Toast.makeText(getApplicationContext(),"Friends Found: "+frnds.length,Toast.LENGTH_LONG).show();
                if(frnds==null){
                    if(debug){  Toast.makeText(getApplicationContext(),"Null Found",Toast.LENGTH_LONG).show();}
                    return;}

                createRows(frnds);
                downloadUpdateDatabase();



            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("--", "Failed to read value.", error.toException());
            }
        });





    }
    private void downloadUpdateDatabase(){
        db = openOrCreateDatabase("whereRU", Context.MODE_PRIVATE, null);

        markerCounter=0;

        String id=auth.getCurrentUser().getUid()+"";
        final Cursor c=db.rawQuery("SELECT * from friends where userID='"+id+"'",null);


        while(c.moveToNext()){
            final String s=c.getString(1)+"";

            DatabaseReference dref = FirebaseDatabase.getInstance().getReference().
                    child("Location").child(s);

            dref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.

                    try {
                        if(debug)Toast.makeText(getApplicationContext(),"Downloading data for :"+dataSnapshot.child("dName").
                                getValue(String.class),Toast.LENGTH_LONG).show();
                        if(debug)Toast.makeText(getApplicationContext(),"picUploadTime: "+
                                dataSnapshot.child("picUploadTime").getValue(),Toast.LENGTH_LONG).show();

                        db.execSQL(
                                "UPDATE friends set " + "" +
                                        "name='"+dataSnapshot.child("dName").getValue(String.class)+"'," +
                                        "lat='" + dataSnapshot.child("lat").getValue(Double.class) + "'" +
                                        ",lon='" + dataSnapshot.child("lon").getValue(Double.class) + "'" +
                                        ",time='" + dataSnapshot.child("time").getValue(String.class) + "'" +
                                        ",picUploadTime='" + dataSnapshot.child("picUploadTime").getValue() + "'" +
                                        "  where id='" + s + "';");

                        if(debug){ Toast.makeText(getApplicationContext(),"UPDATED: "+
                                dataSnapshot.child("lat").getValue()+"",Toast.LENGTH_LONG).show();}
                    }
                    catch (Exception r){
                        if(debug)Toast.makeText(getApplicationContext(),"-->"+r.getMessage(),Toast.LENGTH_LONG).show();}

                    //Log.d("saad",counter+"-----"+c.getCount());
                    updateImage(dataSnapshot.getKey());

                    markerCounter++;
                    if(markerCounter==c.getCount()){
                         refreshMap();
                    }

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w("--", "Failed to read value.", error.toException());
                }
            });

            dataReady=true;
        }

    }
    private static String[] UIDBreaker(String s) {

        //takes the whole frndlist String
        //return after breaking them up into UID
        if(s==null){return  null;}

        String[] result = new String[s.length() - s.replace(",", "").length()];



        for (int a = 0, c = 0; a < s.length(); a++) {

            if (s.charAt(a) == ',') {

                result[c] = s.substring(0, (a));
                System.out.println(result[c]);
                s = s.substring(a + 1);
                //System.out.println("Remains:  "+s);
                c++;
                a = 0;
            }

        }
        return result;
    }
    private void createRows(String[] UID){
        db = openOrCreateDatabase("whereRU", Context.MODE_PRIVATE, null);

        String id=auth.getCurrentUser().getUid();

        db.execSQL("DELETE from friends;");
        for(int c=0;c<UID.length;c++){

            try {

                db.execSQL("INSERT into friends VALUES('" + id + "','" + UID[c] + "','-',0,0,0,'"+(System.currentTimeMillis()-System.currentTimeMillis())+"');");
                //Toast.makeText(getApplicationContext(),"inserting"+UID[c],Toast.LENGTH_LONG).show();
            }
            catch (Exception re){Toast.makeText(getApplicationContext(),re.getMessage(),Toast.LENGTH_LONG).show();}
        }
    }
    private void loginSequence(){


        downloadUpdateDatabase();


    }
    private void friendListChangedSequence(){
        db = openOrCreateDatabase("whereRU", Context.MODE_PRIVATE, null);
        String id=auth.getCurrentUser().getUid();
        try{db.execSQL("DELETE from friends where userID='"+id+"';");}
        catch (Exception r){
            if(debug){  Toast.makeText(getApplicationContext(),r.getMessage(),Toast.LENGTH_LONG).show();}
        }
        downloadString();
    }
    private void validateFriendList(){

        try{
            final DatabaseReference dref = FirebaseDatabase.getInstance().getReference().
                    child("Friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "");
            db = openOrCreateDatabase("whereRU", Context.MODE_PRIVATE, null);
            String id=FirebaseAuth.getInstance().getCurrentUser().getUid();
            Cursor dd=db.rawQuery("Select * from friendList where id='"+id+"';",null);
            dd.moveToNext();

            final String  res=dd.getString(1);
            if(debug){  Toast.makeText(getApplicationContext(),"--->"+res,Toast.LENGTH_LONG).show();}


            dref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    try{frndlstStrng = dataSnapshot.getValue(String.class);

                        if(res.equals(dataSnapshot.getValue(String.class))){
                            loginSequence();
                            if(debug){   Toast.makeText(getApplicationContext(),"Login Sequence activated",Toast.LENGTH_LONG).show();}
                        }
                        else{
                            friendListChangedSequence();
                            if(debug){   Toast.makeText(getApplicationContext(),"Download Sequence activated",Toast.LENGTH_LONG).show();}
                        }}
                    catch (Exception rr){
                        if(debug){ Toast.makeText(getApplicationContext(),rr.getMessage(),Toast.LENGTH_LONG).show();}}
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w("--", "Failed to read value.", error.toException());
                }
            });

        }
        catch (Exception rr){
            if(rr.getMessage().contains("Firebase")){
                Intent i = new Intent(MapsActivity.this, login.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);}
        }


    }
    private void refreshMap(){
        if(debug)Toast.makeText(getApplicationContext(),"Reading database to create markers",Toast.LENGTH_LONG).show();

        db = openOrCreateDatabase("whereRU", Context.MODE_PRIVATE, null);


        String id=auth.getCurrentUser().getUid()+"";
        final Cursor c=db.rawQuery("SELECT * from friends where userID='"+id+"'",null);
        LatLng[] lats=new LatLng[c.getCount()];
        String[] names=new String[c.getCount()];
        String[] times=new String[c.getCount()];
        String[] uid=new String[c.getCount()];

        int counter=0;
        while(c.moveToNext()){

            try{


                names[counter]=c.getString(2);
                lats[counter]=new LatLng(c.getDouble(3),c.getDouble(4));
                Long l=Long.parseLong(c.getString(5));
                uid[counter]=c.getString(1);
                times[counter]=timeConverter(l);


            }
            catch (Exception rr)
            {
                if(debug)Toast.makeText(getApplicationContext(),"-->"+rr.getMessage(),Toast.LENGTH_LONG).show();
            }
            counter++;
        }

        mMap.clear();
        for(int i=0;i<lats.length;i++){

             if( lats[i].latitude!=0 && lats[i].longitude!=0){mMap.addMarker(new MarkerOptions().position(lats[i])
                    .title(names[i])
                    .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(uid[i])))
                    .snippet(times[i]));}
        }
    }
    private String timeConverter(Long user){



        String s="";

        Long temp= System.currentTimeMillis()/60000;
        temp=temp-user;

        if(debug){  Toast.makeText(getApplicationContext(),"Temp: "+temp,Toast.LENGTH_LONG).show();}
        //Toast.makeText(getApplicationContext(),"Temp: "+temp,Toast.LENGTH_LONG).show();
        if(temp<60){s=temp+" Min(s) ago";}
        else{
            temp=temp/60;
            if(temp<24){s=temp+" Hour(s) ago";}
            else{
                temp=temp/24;
                if(temp<365){s=temp+" Day(s) ago";}
                else
                {

                    Long year=temp/365;

                    s=year +" Year(s) ago";

                }
            }
        }
        //if(debug){  Toast.makeText(getApplicationContext(),"Returning: "+s,Toast.LENGTH_LONG).show();}
        return s;
    }
    private Bitmap getMarkerBitmapFromView(String uid) {

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.custom_marker_image);
        markerImageView.setImageBitmap(getImage(uid));
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }
    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void showInternetDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please connect to internet")
                .setCancelable(false)
                .setPositiveButton("Connect to Wifi", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("Connect to Data", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    private boolean logic(String s) {
        db = openOrCreateDatabase("whereRU", Context.MODE_PRIVATE, null);

        //checking metadata for recent changes
        //then downloading the updated images
         String id = FirebaseAuth.getInstance().getCurrentUser().getUid() + "";

        //fetching the friends' id from database
        try{
            Cursor c = db.rawQuery("SELECT * from friends where userID='" + id + "' AND id='"+s+"'", null);
             c.moveToNext();
            Long userTime=c.getLong(6);
            Log.d("====","-->"+userTime);
            c = db.rawQuery("SELECT * from datas where id='" + id + "' AND variable='picUploadTime'", null);
            c.moveToNext();
            Long lastUpdated=c.getLong(2);

        if(userTime<lastUpdated){
            if(debug)Toast.makeText(getApplicationContext(),"False because Logic :"+ (lastUpdated-userTime),Toast.LENGTH_LONG).show();
            Log.d("====","False because Logic :"+ (lastUpdated-userTime));
            return  false;}
        else {
            if(debug)Toast.makeText(getApplicationContext(),"Returned true",Toast.LENGTH_LONG).show();
            Log.d("====","Returned true");
            return true;}}
        catch (Exception rr){
            if(debug)Toast.makeText(getApplicationContext(),"False because error : "+rr.getMessage(),Toast.LENGTH_LONG).show();
            return false;}
    }
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED  ) {


                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            return false;
        } else {
            return true;
        }
    }
}
