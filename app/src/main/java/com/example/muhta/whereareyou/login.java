package com.example.muhta.whereareyou;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class login extends AppCompatActivity {

private FirebaseAuth auth;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Loggin in...");
        Button login=(Button)findViewById(R.id.email_sign_in_button);
        final TextView email=(TextView)findViewById(R.id.email);
        final TextView password=(TextView)findViewById(R.id.password);
        TextView signup=(TextView)findViewById(R.id.signUpTextView);

        auth=FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        if(user!=null){
            Intent i=new Intent(login.this,MapsActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            createTableAndRows();
            startActivity(i);
        }

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(login.this,register.class);

                startActivity(i);
            }
        });


        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                progress.show();

                if (user == null) {
                auth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString()).
                        addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            createTableAndRows();
                            Intent i=new Intent(login.this,MapsActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            progress.dismiss();
                            startActivity(i);

                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Authentication Failed",Toast.LENGTH_LONG).show();
                            progress.dismiss();
                        }
                    }
                });

                } else {

                }


            }
        });

        if(!isInternetConnected()){showInternetDialog();}


    }


    private void createTableAndRows(){
        db = openOrCreateDatabase("whereRU", Context.MODE_PRIVATE, null);
        try{

            db.execSQL("CREATE TABLE IF NOT EXISTS friends(" +
                "userID VARCHAR,id VARCHAR,name VARCHAR,lat DOUBLE,lon DOUBLE,time String,picUploadTime String);");
            db.execSQL("CREATE TABLE IF NOT EXISTS friendList(" +
                "id VARCHAR,friends VARCHAR);");

            String id=FirebaseAuth.getInstance().getCurrentUser().getUid();



            db.execSQL("CREATE TABLE IF NOT EXISTS datas(" +
                    "id VARCHAR,variable VARCHAR,data DOUBLE);");

            Cursor C=db.rawQuery("Select * from datas where id='"+id+"'",null);
            if(C.getCount()==0) {
                db.execSQL("INSERT into FriendList VALUES ('" + id + "','z')");
                db.execSQL("INSERT into datas VALUES('" + id + "','frequency',10);");
                db.execSQL("INSERT into datas VALUES('" + id + "','broadcast',1);");
                db.execSQL("INSERT into datas VALUES('" + id + "','picUploadTime','"+(System.currentTimeMillis()-System.currentTimeMillis())+"');");
            }

            db.close();}
        catch (Exception r){
            Toast.makeText(getApplicationContext(),"Database ERROR :"+r.getMessage(),Toast.LENGTH_LONG).show();
            Log.d("===",r.getMessage());
        }


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
                .setNegativeButton("Connect to Mobile Data", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
  /* */