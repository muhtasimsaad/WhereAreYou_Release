package com.example.muhta.whereareyou;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class register extends AppCompatActivity {
    private FirebaseAuth auth;
    String user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        final ProgressDialog progress = new ProgressDialog(this);
        Button login=(Button)findViewById(R.id.email_sign_in_button);
        final TextView email=(TextView)findViewById(R.id.email);
        final TextView password=(TextView)findViewById(R.id.password);
        final TextView username=(TextView)findViewById(R.id.username);
        progress.setMessage("Registering...");

        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                progress.show();
                auth= FirebaseAuth.getInstance();
                final FirebaseUser user = auth.getCurrentUser();
                if (user == null) {

                    auth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString()).
                            addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
//                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//                                                .setDisplayName(username.getText()+"").build();
//                                        user.updateProfile(profileUpdates);
//                                        Intent i=new Intent(register.this,login.class);
//                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                        startActivity(i);

                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(),"Registration Failed",Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                }
                            });
                    //DatabaseReference dref= FirebaseDatabase.getInstance().getReference().child("Location");

                    auth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString()).
                            addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){

                                        final DatabaseReference dref = FirebaseDatabase.getInstance().getReference().
                                                child("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "")
                                                .child("Request");
                                        dref.setValue("");

                                        final DatabaseReference dref3 = FirebaseDatabase.getInstance().getReference().
                                                child("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "")
                                                .child("dName");
                                        dref3.setValue(username.getText().toString());

                                        final DatabaseReference dref4 = FirebaseDatabase.getInstance().getReference().
                                                child("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "")
                                                .child("sentRequest");
                                        dref4.setValue("");

                                        final DatabaseReference dref5 = FirebaseDatabase.getInstance().getReference().
                                                child("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "")
                                                .child("picUploadTime");
                                        dref5.setValue("");


                                        final DatabaseReference dref6 = FirebaseDatabase.getInstance().getReference().
                                                child("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "")
                                                .child("lat");
                                        dref6.setValue("0");


                                        final DatabaseReference dref7 = FirebaseDatabase.getInstance().getReference().
                                                child("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "")
                                                .child("lon");
                                        dref7.setValue("0");

                                        final DatabaseReference dref8 = FirebaseDatabase.getInstance().getReference().
                                                child("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "")
                                                .child("time");
                                        dref8.setValue(""+System.currentTimeMillis());


                                        final DatabaseReference dref2 = FirebaseDatabase.getInstance().getReference().
                                                child("Friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "");

                                        dref2.setValue("");


                                        Toast.makeText(getApplicationContext(),"Registration Completed.",
                                                Toast.LENGTH_LONG).show();
                                        progress.dismiss();
                                        Intent i=new Intent(register.this,login.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);

                                    }
                                    else{
                                        progress.dismiss();
                                        Toast.makeText(getApplicationContext(),"Registration Failed",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                } else {

                }


            }
        });


    }
}
