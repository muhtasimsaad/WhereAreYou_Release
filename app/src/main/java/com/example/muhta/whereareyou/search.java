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
import android.support.v4.app.Fragment;
import android.support.v4.app.TaskStackBuilder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
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
 * {@link search.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link search#newInstance} factory method to
 * create an instance of this fragment.
 */
public class search extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private boolean debug=false;

    // TODO: Rename and change types of parameters
    private String mParam1;
    SQLiteDatabase db;
    int[] sentRqst;
    private String mParam2;
    EditText search;
    ArrayList<String> names=new ArrayList<String>();
    ArrayList<String> uid=new ArrayList<String>();
    ArrayList<String> times=new ArrayList<String>();
    ArrayList<Bitmap> images=new ArrayList<Bitmap>();
    private OnFragmentInteractionListener mListener;
    ListView lv;

    public search() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment search.
     */
    // TODO: Rename and change types and number of parameters
    public static search newInstance(String param1, String param2) {
        search fragment = new search();
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
        View view=inflater.inflate(R.layout.fragment_search, container, false);
        if(debug)Toast.makeText(getActivity().getApplicationContext(),"Search",Toast.LENGTH_SHORT).show();


        search=(EditText)view.findViewById(R.id.headerTextSearch);
        lv=(ListView)view.findViewById(R.id.listViewSearch);

    nullList adapter2 = new
            nullList((Activity) getContext(), new String[]{"Please wait..."});

    lv.setAdapter(adapter2);

    if (debug)
        Toast.makeText(getActivity().getApplicationContext(), names.size() + "", Toast.LENGTH_LONG).show();

    search.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            //Log.d("====",names.get(0)+"-----"+charSequence);

            ArrayList<String> namesTemp=new ArrayList<String>();
            ArrayList<String> uidTemp=new ArrayList<String>();
            ArrayList<String> timesTemp=new ArrayList<String>();
            ArrayList<Bitmap> imagesTemp=new ArrayList<Bitmap>();

            lv.setAdapter(null);

            for(int z=0;z<names.size();z++)
            {
                //Log.d("====",names.get(z)+"-----"+charSequence);
                if(names.get(z).contains(charSequence+""))
                {
                    namesTemp.add(names.get(z));
                    uidTemp.add(uid.get(z));
                    timesTemp.add(times.get(z));
                    imagesTemp.add(images.get(z));

                }
            }
            accountsAdapter adapter = new
                    accountsAdapter((Activity) getContext(), arrayConverter(namesTemp), arrayConverter(timesTemp),
                    arrayConverter2(imagesTemp), arrayConverter(uidTemp), 1,
                    sentRqst);


            lv.setAdapter(adapter);




        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    });


    final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Location");
        names.clear();
        images.clear();
        times.clear();
        uid.clear();
    ref.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d("====", "ashse");
            if (mListener != null) {
                Log.d("====", "dhukse");
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    // TODO: handle the post


                    if (debug) Toast.makeText(getActivity().getApplicationContext(),
                            "Found ID: " + postSnapshot.getKey() + "", Toast.LENGTH_LONG).show();

                    //to prevent showing myself in search
                    if(mListener!=null) {
                        if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(postSnapshot.getKey())) {

                            uid.add(postSnapshot.getKey() + "");
                            names.add(postSnapshot.child("dName").getValue() + "");
                            try {
                                Long l = Long.parseLong(postSnapshot.child("time").getValue() + "");
                                l = (System.currentTimeMillis() / 1000 / 60) - l;
                                times.add(l + "");
                            } catch (Exception r) {
                                times.add("z");
                            }
                            images.add(getImage(postSnapshot.getKey()));
                        }
                    }
                }
                ref.removeEventListener(this);

                 sentRqst=checkSent(dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().
                        getUid()).child("sentRequest").getValue(String.class), arrayConverter(uid));

                    accountsAdapter adapter = new
                            accountsAdapter((Activity) getContext(), arrayConverter(names), arrayConverter(times),
                            arrayConverter2(images), arrayConverter(uid), 1,sentRqst
                            );


                lv.setAdapter(adapter);

            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });

    uid.clear();
    names.clear();
    images.clear();
    times.clear();

        return view;

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
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    private String[] arrayConverter (ArrayList<String> r){
        String[] result=new String[r.size()];
        for(int i=0;i<result.length;i++){
            result[i]=r.get(i);
        }
        return result;
    }

    private int[] checkSent(String s,String[] uids){


        Log.d("===","checking if the user already sent a request or if the user already have him in friend list.");

        db = getActivity().openOrCreateDatabase("whereRU", Context.MODE_PRIVATE, null);
        Cursor c=db.rawQuery("Select * from friends where userID='"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"';",null);
        String[] frnds=new String[c.getCount()];
        int counter=0;
        Log.d("==---","UID Total : "+c.getCount());
        while (c.moveToNext()){
            Log.d("==---","UID added : "+c.getString(1));
            frnds[counter]=c.getString(1);counter++;}

        int[] flags=new int[uids.length];
        Log.d("==---","UID String : "+s);
        if(s==null || s.equals("null")){
            if(counter==0){
            Log.d("==---","UID Shit");
            return  flags;}}

        String[] sent=UIDBreaker(s);

        int inncerCounter=0;

        if (s==null && c.getCount()!=0){
            inncerCounter=c.getCount();
        }
        if (s!=null && c.getCount()==0){
            inncerCounter=sent.length;
        }
        if (s==null && c.getCount()==0){
            return flags;
        }
        if (s!=null && c.getCount()!=0){
            if(s.length()>c.getCount()){inncerCounter=s.length();}
            else{inncerCounter=c.getCount();}
        }


        for(int i=0;i<uids.length;i++){
            //Log.d("====","UID found "+sent[i]);
            Log.d("==--","UID :"+i);
            for(int j=0;j<inncerCounter;j++){
                  try{

                    if(uids[i].equals(sent[j])){

                    flags[i]=1;
                    }
                    }
                catch (Exception r){Log.d("==---","Cought at sent Index : "+i);}

                try {

                    if (uids[i].equals(frnds[j]))  {
                        flags[i] = 2;

                        }
                }
                catch (Exception r){Log.d("==---","Cought at friend  Index : "+i+ "ERROR :"+r.getMessage());}
                }

            }


        return flags;
    }
    private String[] UIDBreaker(String s) {

        //takes the whole frndlist String
        //return after breaking them up into UID
         if(s==null){return  null;}

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
    private Bitmap[] arrayConverter2 (ArrayList<Bitmap> r){
        Bitmap[] result=new Bitmap[r.size()];
        for(int i=0;i<result.length;i++){
            result[i]=r.get(i);
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
