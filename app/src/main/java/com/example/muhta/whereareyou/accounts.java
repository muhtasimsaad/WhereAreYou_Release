package com.example.muhta.whereareyou;


import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class accounts extends AppCompatActivity implements notifications.OnFragmentInteractionListener
,friendList.OnFragmentInteractionListener,search.OnFragmentInteractionListener{


    final Fragment fragment1 = new search();
    final Fragment fragment2 = new friendList();
    final Fragment fragment3 = new notifications();
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {




                case R.id.navigation_home:

                    final FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.content, fragment1).commit();
                    return true;
                case R.id.navigation_dashboard:

                    final FragmentManager fragmentManager2 = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                    fragmentTransaction2.replace(R.id.content, fragment2).commit();
                    return true;
                case R.id.navigation_notifications:
                    final FragmentManager fragmentManager3 = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction3 = fragmentManager3.beginTransaction();
                    fragmentTransaction3.replace(R.id.content, fragment3).commit();
                    return true;

            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);


//Remove notification bar


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        final FragmentManager fragmentManager4 = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction4 = fragmentManager4.beginTransaction();
        fragmentTransaction4.replace(R.id.content, fragment1).commit();


    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
