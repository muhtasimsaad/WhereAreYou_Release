package com.example.muhta.whereareyou;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.renderscript.Long2;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by muhta on 8/4/2017.
 */

public class accountsAdapter extends ArrayAdapter<String> {
    boolean debug=false;
    private final Activity context;
    private int[] sentRequestFlags;
    private final String[] names;
    private final String[] uid;
    private final int flag;
    private final String[] time;
    private final Bitmap[] imageId;
    public accountsAdapter(Activity context,
                           String[] web,String[] web2, Bitmap[] imageId,String[] uid,int flag) {
        super(context, R.layout.notifications, web);
        this.context = context;
        this.uid=uid;
        this.names = web;
        this.time=web2;
        this.imageId = imageId;
        this.flag=flag;

    }
    public accountsAdapter(Activity context,
                           String[] web,String[] web2, Bitmap[] imageId,String[] uid,int flag,int[] flags) {
        super(context, R.layout.notifications, web);
        this.context = context;
        this.uid=uid;
        this.names = web;
        this.time=web2;
        this.imageId = imageId;
        this.flag=flag;
        this.sentRequestFlags=flags;

    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.notifications, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.textViewName);
        TextView textTime=(TextView)rowView.findViewById(R.id.textViewTime);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        txtTitle.setText(names[position]);
        textTime.setText(timeConverter(time[position]));
        imageView.setImageBitmap(imageId[position]);
        final Button accept=(Button)rowView.findViewById(R.id.accept);
        Button reject=(Button)rowView.findViewById(R.id.reject);

        if(flag==3 && uid!=null) {


                    final DatabaseReference request=FirebaseDatabase.getInstance().getReference().child("Location").
                    child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Request");


                    accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final DatabaseReference own=FirebaseDatabase.getInstance().getReference().child("Friends").
                            child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    final DatabaseReference sender=FirebaseDatabase.getInstance().getReference().child("Friends").
                            child(uid[position]);
                    final DatabaseReference sentRqst= FirebaseDatabase.getInstance().getReference().
                            child("Location").child(uid[position]).child("sentRequest");


                    sentRqst.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String s=dataSnapshot.getValue()+"";
                            s=s.replace(FirebaseAuth.getInstance().getCurrentUser().getUid()+",","");
                            Log.d("----",s);
                            sentRqst.setValue(s);
                            sentRqst.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    own.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            own.setValue(dataSnapshot.getValue()+uid[position]+",");

                            own.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    sender.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            sender.setValue(dataSnapshot.getValue()+FirebaseAuth.getInstance().getCurrentUser().getUid()+",");

                            sender.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    request.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String s=dataSnapshot.getValue()+"";
                            s=s.replace(uid[position]+",","");
                            request.setValue(s);
                            request.removeEventListener(this);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }
            });
            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    request.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String s=dataSnapshot.getValue()+"";
                            s=s.replace(uid[position]+",","");
                            request.setValue(s);
                            request.removeEventListener(this);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



                }
            });
        }
        if(flag==2){

            reject.setVisibility(View.GONE);
            accept.setText("Delete");
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final DatabaseReference frnd= FirebaseDatabase.getInstance().getReference().
                            child("Friends").child(uid[position]+"");
                    final DatabaseReference own= FirebaseDatabase.getInstance().getReference().
                            child("Friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid());


                    frnd.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String s=dataSnapshot.getValue(String.class);
                            s=s.replace(FirebaseAuth.getInstance().getCurrentUser().getUid()+",","");
                            if(debug)Toast.makeText(getContext(),"Deleting :"+uid[position],Toast.LENGTH_LONG).show();
                            frnd.setValue(s);
                            frnd.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    own.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String s=dataSnapshot.getValue(String.class);
                            s=s.replace(uid[position]+",","");
                            if(debug)Toast.makeText(getContext(),"Deleting :"+s,Toast.LENGTH_LONG).show();
                            own.setValue(s);
                            own.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });




                }
            });
        }
        if (flag==1){
            boolean flag1=false;
            boolean flag2=false;
            reject.setVisibility(View.GONE);
            accept.setText("Send Request");
            if(sentRequestFlags[position]==1){

                  accept.setText("Request Sent");accept.setEnabled(false);
            }

            if(sentRequestFlags[position]==2){

                accept.setText("Friend");accept.setEnabled(false);
            }
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(debug)Toast.makeText(context,uid[position]+"",Toast.LENGTH_LONG).show();
                    final DatabaseReference ref= FirebaseDatabase.getInstance().getReference().
                            child("Location").child(uid[position]+"").child("Request");
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ref.setValue(dataSnapshot.getValue()+""+FirebaseAuth.getInstance().getCurrentUser().getUid()+",");
                            ref.removeEventListener(this);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    final DatabaseReference sentRqst= FirebaseDatabase.getInstance().getReference().
                            child("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("sentRequest");
                    sentRqst.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            sentRqst.setValue(dataSnapshot.getValue()+""+uid[position]+",");
                            sentRqst.removeEventListener(this);
                            accept.setText("Request Sent");

                            accept.setEnabled(false);

                             }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



                }
            });

        }


        return rowView;
    }
    private String timeConverter(String s){




        if(s.equals("z")){return "N/A";}
        Long temp= Long.parseLong(s);


        if(debug){  Toast.makeText(context,"Temp: "+temp,Toast.LENGTH_LONG).show();}
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
    private String[] UIDBreaker(String s) {

        //takes the whole frndlist String
        //return after breaking them up into UID
        if(s.length()<=1){return  null;}
        if(debug)Toast.makeText(context,"Request String : " +s,Toast.LENGTH_LONG).show();
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
}