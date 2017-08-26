package com.example.muhta.whereareyou;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.EventListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link friendList.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link friendList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class friendList extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ArrayList<String> names=new ArrayList<String>();
    ArrayList<String> times=new ArrayList<String>();
    ArrayList<Bitmap> images=new ArrayList<Bitmap>();
    String[] uid;
    int i;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private boolean debug=false;
    private OnFragmentInteractionListener mListener;
    ListView lv;
    public friendList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment friendList.
     */
    // TODO: Rename and change types and number of parameters
    public static friendList newInstance(String param1, String param2) {
        friendList fragment = new friendList();
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


        View view=inflater.inflate(R.layout.fragment_friend_list, container, false);


        lv = (ListView) view.findViewById(R.id.listViewFrnds);

        nullList adapter2 = new
                nullList((Activity) getContext(),new String[]{"Please wait..."});

        lv.setAdapter(adapter2);
        final DatabaseReference dref = FirebaseDatabase.getInstance().getReference().
                child("Friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "");

        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (mListener!=null) {

                    uid = UIDBreaker(dataSnapshot.getValue(String.class));

                    if (uid.length == 0 || uid == null) {
                        nullList adapter2 = new
                                nullList((Activity) getContext(), new String[]{"Wow ! No friends"});

                        lv.setAdapter(adapter2);
                    }
                    if (debug)
                        Toast.makeText(getActivity().getApplicationContext(), "-->" + uid[0], Toast.LENGTH_LONG).show();
                    generateView(uid);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        names.add(dataSnapshot.child("dName").getValue() + "");
//        Long l = Long.parseLong(dataSnapshot.child("time").getValue() + "");
//        l = (System.currentTimeMillis() / 1000 / 60) - l;
//        times.add(l + "");
//        images.add(R.drawable.glass);

        return view;
    }

    private void generateView(final String[] uids){
        i=0;
        for (int z=0;z<uids.length;z++) {

            if(debug)Toast.makeText(getActivity().getApplicationContext(),"Genrating view for :"+uids[z],Toast.LENGTH_LONG).show();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Location").child(uids[z]);
            if(debug)Toast.makeText(getActivity().getApplicationContext(),"Total :"+uids.length,Toast.LENGTH_LONG).show();

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    names.add(dataSnapshot.child("dName").getValue() + "");
                    images.add(getImage(dataSnapshot.getKey()+""));

                    Long l=(Long.parseLong(dataSnapshot.child("time").getValue()+""));
                    l = (System.currentTimeMillis() / 1000 / 60) - l;
                    times.add(l+"");

                    if(times.size()==uids.length){show(uids);}

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

//            name.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if(mListener!=null){
//                        names.add(dataSnapshot.getValue() + "");
//                    images.add(getImage(uids[i]));
//                    i++;}
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//            time.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if(mListener!=null){Long l = Long.parseLong(dataSnapshot.getValue() + "");
//                    l = (System.currentTimeMillis() / 1000 / 60) - l;
//                    times.add(l + "");
//
//
//
//                    if(times.size()==uids.length)show(uids);
//
//                    }
//
//
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });






            names.clear();
            images.clear();
            times.clear();


        }
    }

    private void show(String[] uids){



        if(uid.length>0){accountsAdapter adapter = new
                accountsAdapter((Activity) getContext(), arrayConverter(names),
                arrayConverter(times), arrayConverter2(images),
                uids, 2);

        lv.setAdapter(adapter);}
        else{

                nullList adapter2 = new
                        nullList((Activity) getContext(),new String[]{"NO friends found ..."});

                lv.setAdapter(adapter2);

        }


    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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

            Log.d("===","(Default image selected) Error :");
            Bitmap icon = BitmapFactory.decodeResource(this.getResources(),R.drawable.profile);
            result=icon;


        }
        return  result;
    }
    private static String[] UIDBreaker(String s) {

        //takes the whole frndlist String
        //return after breaking them up into UID
        if(s==null || s.equals("null")){return  null;}

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
}
