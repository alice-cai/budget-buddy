package com.hackthe6ix2019.android.receipts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.app.Fragment;

public class MainActivity extends AppCompatActivity implements BudgetFragment.OnFragmentInteractionListener {

    private TextView mTextMessage;
    private static final int Image_Capture_Code = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // defaults to the budget page
        Fragment defaultFragment = new BudgetFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, defaultFragment).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //mTextMessage.setText(R.string.title_home);
                    // TODO: change this to: fragment = new HomeFragment();
                    fragment = new BudgetFragment();
                    break;
                case R.id.navigation_dashboard:
                    //mTextMessage.setText(R.string.title_dashboard);
                    Intent cInt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cInt,Image_Capture_Code);
                    fragment = new BudgetFragment();
                    // TODO: change this to: fragment = new HomeFragment();
                    break;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    // TODO: change this to: fragment = new HomeFragment();
                    break;
                default:
                    return false;
            }

            // TODO: what is this
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment).commit();
            return true;
        }
    };

    public void onFragmentInteraction(Uri uri) {
        // TODO: what
    }
}
