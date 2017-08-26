package com.example.muhta.whereareyou;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link notifications.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link notifications#newInstance} factory method to
 * create an instance of this fragment.
 */
public class notifications extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    ArrayList<String> names=new ArrayList<String>();
    String[] uid;
    ArrayList<String> times=new ArrayList<String>();
    ArrayList<Bitmap> images=new ArrayList<Bitmap>();
    private boolean debug=false;
    private OnFragmentInteractionListener mListener;
    ListView lv;
    public notifications() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment notifications.
     */
    // TODO: Rename and change types and number of parameters
    public static notifications newInstance(String param1, String param2) {
        notifications fragment = new notifications();

        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(debug)Toast.makeText(getActivity().getApplicationContext(),"Notifications",Toast.LENGTH_SHORT).show();
        View view=inflater.inflate(R.layout.fragment_notifications, container, false);
        lv=(ListView)view.findViewById(R.id.listViewNotification);

        nullList adapter2 = new
                nullList((Activity) getContext(),new String[]{"Please wait..."});

        lv.setAdapter(adapter2);

        final DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("Location").
                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Request");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if(debug)Toast.makeText(getActivity().getApplicationContext(),"ashse",Toast.LENGTH_LONG).show();
                if(dataSnapshot.equals("null") || dataSnapshot==null){return;}
                uid=UIDBreaker(dataSnapshot.getValue()+"");
                if(uid==null){

                    nullList adapter2 = new
                            nullList((Activity) getContext(),new String[]{"No requests..."});
                    lv.setAdapter(adapter2);
                    return;

                }
                readDatabase(uid);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });












//        final DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("Location").
//                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Request");
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                    uid=UIDBreaker(dataSnapshot.getValue()+"");
//                    if(uid==null){return;}
//                    readDatabase(uid);
//                    ref.removeEventListener(this);
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });








        return view;

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void readDatabase(final String[] friends){
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("Location");




        for(int i=0;i<friends.length;i++){
            DatabaseReference ref2=ref.child(friends[i]+"");

            ref2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Toast.makeText(getActivity().getApplicationContext(),"asas",Toast.LENGTH_LONG).show();

                    if(mListener!=null){names.add(dataSnapshot.child("dName").getValue()+"");
                    try{Long l=Long.parseLong(dataSnapshot.child("time").getValue()+"");
                    l=(System.currentTimeMillis()/1000/60)-l;
                    times.add(l+"");}
                    catch (Exception rr){times.add("z");}
                    try{images.add(getImage(dataSnapshot.getKey()));}
                    catch (Exception r){
                        Log.d("====",r.getMessage());
                        images.add(BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.profile));
                    }

                    if(times.size()==friends.length){

                        Log.d("====","Size :"+names.size()+"");
                        if(names.size()>0){accountsAdapter adapter = new
                                accountsAdapter((Activity) getContext(), arrayConverter(names), arrayConverter(times),
                                arrayConverter2(images),uid,3);

                        lv.setAdapter(adapter);
                            names.clear();images.clear();times.clear();uid=null;
                        }
                        else{

                            nullList adapter2 = new
                                    nullList((Activity) getContext(),new String[]{"No requests..."});

                            lv.setAdapter(adapter2);
                            names.clear();images.clear();times.clear();uid=null;
                        }


                    }

                }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }



    }

    private String[] arrayConverter (ArrayList<String> r){
        String[] result=new String[r.size()];
        for(int i=0;i<result.length;i++){
            result[i]=r.get(i);
        }
        return result;
    }
    private Bitmap[] arrayConverter2 (ArrayList<Bitmap> r){
        Bitmap[] result=new Bitmap[r.size()];
        for(int i=0;i<result.length;i++){
            result[i]=r.get(i);
        }
        return result;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
            result=bmp;

        }
        catch (Exception rr){

            Log.d("===","(Default image selected) Error : "+rr.getMessage());
            Bitmap icon = BitmapFactory.decodeResource(this.getResources(),R.drawable.profile);
            result=icon;


        }
        return  result;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    private String[] UIDBreaker(String s) {

        //takes the whole frndlist String
        //return after breaking them up into UID
        if(s.length()<=1){return  null;}
        if(debug)Toast.makeText(getActivity().getApplicationContext(),"Request String : " +s,Toast.LENGTH_LONG).show();
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
