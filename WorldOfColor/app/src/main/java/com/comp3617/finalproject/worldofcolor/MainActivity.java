package com.comp3617.finalproject.worldofcolor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private static FragmentManager mFm;
    private static FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFm = getSupportFragmentManager();
        Fragment fragment = mFm.findFragmentById(R.id.fragment_container);
        if (fragment == null){
            fragment = new CardsListFragment();
            mFm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (mFm.getBackStackEntryCount() == 0){
            super.onBackPressed();
        }

        mFm.popBackStackImmediate();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
