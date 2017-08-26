package com.example.muhta.whereareyou;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;



public class settings extends AppCompatActivity {
    SQLiteDatabase db;
    RadioButton noBroadcast;
    RadioButton broadcast;
    RadioButton  s10;
    RadioButton s30;
    RadioButton m1;
    RadioButton m5;
    ImageView profPic;
    Button changePic;
    Button changeEMail;
    Button changePass;
    EditText name;
    Bitmap picUri;
    FirebaseUser user;
    Button saveProfile;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    boolean debug=false;
    private int PICK_IMAGE_REQUEST = 1;
    final Context context = this;

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
            checkPermission();
            user=FirebaseAuth.getInstance().getCurrentUser();
            db = openOrCreateDatabase("whereRU", Context.MODE_PRIVATE, null);
            profPic=(ImageView)findViewById(R.id.profPic);
            profPic.setImageBitmap(BitmapFactory.decodeResource(this.getResources(),R.drawable.profile));
            try{downloadImage();}
            catch (Exception rr){
                Toast.makeText(getApplicationContext(),"Error cought",Toast.LENGTH_LONG).show();
            }
            noBroadcast=(RadioButton)findViewById(R.id.noBroadcast);
            broadcast=(RadioButton)findViewById(R.id.broadcast);
            s10=(RadioButton)findViewById(R.id.s10);
            s30=(RadioButton)findViewById(R.id.s30);
            m1=(RadioButton)findViewById(R.id.m1);
            m5=(RadioButton)findViewById(R.id.m5);


                retrieveValues();




            name=(EditText)findViewById(R.id.currentName);

            changePic=(Button)findViewById(R.id.changePicture) ;
            changeEMail=(Button)findViewById(R.id.changeEmail) ;
            changePass=(Button)findViewById(R.id.changePassword) ;
            saveProfile=(Button)findViewById(R.id.saveProfile);

            saveProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    FirebaseDatabase.getInstance().getReference().
                            child("Location").child(user.getUid()).child("picUploadTime").
                            setValue(System.currentTimeMillis());


                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name.getText()+"")
                            .build();

                    try{if(picUri!=null){
                        saveImage(picUri,FirebaseAuth.getInstance().getCurrentUser().getUid());
                        uploadImage(getImageUri(FirebaseAuth.getInstance().getCurrentUser().getUid()));}}
                    catch (Exception rr){
                        Log.d("====",rr.getMessage());
                        Toast.makeText(getApplicationContext(),rr.getMessage(),Toast.LENGTH_LONG).show();}
                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(),"Profile updated",Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(),"Please retry",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });


                }
            });

            changePass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.passchanger);
                    dialog.setTitle("Change Password");
                    Button save=(Button)dialog.findViewById(R.id.editPass);

                    final EditText current=(EditText)dialog.findViewById(R.id.currentPassword);
                    final EditText New=(EditText)dialog.findViewById(R.id.newPassword);
                    final EditText confirmNew=(EditText)dialog.findViewById(R.id.confirmNewPassword);

                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail()+"", current.getText()+"");

                            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(!task.isSuccessful()){
                                        Toast.makeText(getApplicationContext(),"Current password does not match",Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    else {

                                        if (!(New.getText() + "").equals(confirmNew.getText() + "")) {
                                            Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_LONG).show();
                                            return;
                                        }


                                        else{
                                            user.updatePassword(New.getText() + "").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getApplicationContext(), "Password updated"
                                                            , Toast.LENGTH_LONG).show();
                                                    dialog.dismiss();
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Please retry", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });

                                    }
                                    }

                                }
                            });







                        }
                    });

                    dialog.show();
                }
            });

            changeEMail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.emailchanger);
                    dialog.setTitle("Change Email");
                    Button save=(Button)dialog.findViewById(R.id.editEmail);

                    final EditText current=(EditText)dialog.findViewById(R.id.currentEmail);
                    final EditText New=(EditText)dialog.findViewById(R.id.newEmail);
                    final EditText confirmNew=(EditText)dialog.findViewById(R.id.confirmNewEmail);


                        save.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if(!(current.getText()+"").equals(user.getEmail())){
                                    Toast.makeText(getApplicationContext(),"Current email is incorrect",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                else {

                                    if(!(New.getText()+"").equals(confirmNew.getText()+"")){
                                        Toast.makeText(getApplicationContext(),"New emails do not match",Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    else{

                                        user.updateEmail(New.getText()+"").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(getApplicationContext(),"Email updated",Toast.LENGTH_LONG).show();
                                                    dialog.dismiss();
                                                }
                                                else{
                                                    Toast.makeText(getApplicationContext(),"Please retry",Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });

                                    }

                                }





                            }
                        });



                    dialog.show();
                }
            });

            changePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();

                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                }
            });
            noBroadcast.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setBroadcast(0);
                }
            });
            broadcast.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setBroadcast(1);
                }
            });

            s10.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setFrequency(10);
                }
            });
            s30.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setFrequency(30);
                }
            });
            m1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setFrequency(60);
                }
            });
            m5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setFrequency(300);
                }
            });


            DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("Location").
                    child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("dName");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                name.setText(dataSnapshot.getValue(String.class)+"");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

    }

    private void setBroadcast(int i){
        //MapsActivity.broadcast=i;
        try{db.execSQL("UPDATE datas set data='"+i+"' where " +
                "( id='"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"' AND " +
                "variable='broadcast');");}
        catch (Exception rr){Toast.makeText(getApplicationContext(),rr.getMessage(),Toast.LENGTH_LONG).show();

        }

    }

    private void setFrequency(int i){
        //MapsActivity.frequency=i;
        try{db.execSQL("UPDATE datas set data='"+i+"' where " +
                "( id='"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"' AND " +
                "variable='frequency');");}
        catch (Exception rr){Toast.makeText(getApplicationContext(),rr.getMessage(),Toast.LENGTH_LONG).show();}

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void retrieveValues(){
        try{
            Cursor C=db.rawQuery("SELECT * from datas where id='"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"'" +
                ";",null);


        while (C.moveToNext()){


            if(C.getString(1).equals("frequency")){
                if(debug)Toast.makeText(getApplicationContext(),"Frequency "+C.getDouble(2),Toast.LENGTH_LONG).show();
                if(C.getDouble(2)==10){s10.setChecked(true);}
                if(C.getDouble(2)==30){s30.setChecked(true);}
                if(C.getDouble(2)==1 ){m1.setChecked(true);}
                if(C.getDouble(2)==5 ){m5.setChecked(true);}

            }


            if(C.getString(1).equals("broadcast")){
                if(debug)Toast.makeText(getApplicationContext(),"Broadcast "+C.getDouble(2),Toast.LENGTH_LONG).show();
                if(C.getDouble(2)==0){noBroadcast.setChecked(true);}
                if(C.getDouble(2)==1){broadcast.setChecked(true);}}
        }

        if(debug)Toast.makeText(getApplicationContext(),"PhotoURI --:"+user.getPhotoUrl(),Toast.LENGTH_LONG).show();
            Log.d("===",user.getPhotoUrl()+"");




    }
    catch (Exception rr){
        Log.d("===",rr.getMessage());
        Toast.makeText(getApplicationContext(),rr.getMessage(),Toast.LENGTH_LONG).show();}
    }

    private void uploadImage(android.net.Uri link){
        StorageReference ref = FirebaseStorage.getInstance().getReference().
                child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Log.d("===", "Ashse");

          // "/mnt/sdcard/FileName.mp3"


        UploadTask uploadTask = ref.putFile(link);

// Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();

            }
        });
    }

    private void downloadImage() throws IOException {
        final File localFile = File.createTempFile("images", "jpg");

//        StorageReference ref= FirebaseStorage.getInstance().getReference();
//        ref.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
//            @Override
//            public void onSuccess(StorageMetadata storageMetadata) {
//                storageMetadata.getUpdatedTimeMillis();
//            }
//        });

        FirebaseStorage.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        profPic.setImageBitmap(bitmap);

                        Log.d("===", "Success");
                    }
                }).addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                // ...
                Log.d("===", "ERROR :"+exception.getMessage());
            }
        });
    }


    public void checkPermission(){

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }

        }
        else{
            if(debug)Toast.makeText(getApplicationContext(),"granted",Toast.LENGTH_LONG).show();
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                bitmap=getResizedBitmap(bitmap,300);
                picUri=bitmap;
                profPic.setImageBitmap(bitmap);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
    private void saveImage(Bitmap finalBitmap,String uid) {
        finalBitmap=getResizedBitmap(finalBitmap,300);
        if(debug)Toast.makeText(getApplicationContext(),"trying to save",Toast.LENGTH_LONG).show();
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/WhereAreYou");
        if (!myDir.exists()) {
            if(!myDir.mkdirs()){}
        }
        String fname = "Image-"+ uid +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {


            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();


        } catch (Exception e) {
            e.printStackTrace();
            Log.d("====",e.getMessage());
            Toast.makeText(getApplicationContext(),"saving error :"+e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }
    private Uri getImageUri(String uid){
        Uri result;
        try{

            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/WhereAreYou");
            myDir.mkdirs();
            String fname = "Image-"+ uid +".jpg";
            File f = new File (myDir, fname);
            Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
            String s=bmp.toString();
            result= Uri.fromFile(f);

        }
        catch (Exception rr){

            Log.d("===","(Default image selected) Error : "+rr.getMessage());
            result=null;

        }
        return  result;
    }
}
