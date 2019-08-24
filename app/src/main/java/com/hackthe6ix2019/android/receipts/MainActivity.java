package com.hackthe6ix2019.android.receipts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClient;
import com.amazonaws.services.textract.model.DetectDocumentTextRequest;
import com.amazonaws.services.textract.model.DetectDocumentTextResult;
import com.amazonaws.services.textract.model.Document;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Image_Capture_Code) {
            if (resultCode == RESULT_OK) {
                Bitmap bp = (Bitmap) data.getExtras().get("data");
                // convert to byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                //Toast.makeText(this, byteArray + "", Toast.LENGTH_LONG).show();
                Log.d("main activity","DATA EXTRAS: " + data.getExtras());

                // aws stuff
                //AmazonRekognition rekognitionClient = new AmazonRekognitionClient(new BasicAWSCredentials("", ""));

                // TODO: remove later
                BasicAWSCredentials credentials = new BasicAWSCredentials("AKIASMTWCNOQRPPVZXFP", "zbRVfIYS2Y+6a92/jUtn4+B8fUUi6cgjwuZ5F/Wu");

                AmazonTextractClient amazonTextractClient = new AmazonTextractClient(credentials);
                ByteBuffer sourceImageBytes = ByteBuffer.wrap(byteArray);
                Document source = new Document().withBytes(sourceImageBytes);
                DetectDocumentTextRequest documentTextRequest = new DetectDocumentTextRequest().withDocument(source);
                DetectDocumentTextResult documentTextResult = amazonTextractClient.detectDocumentText(documentTextRequest);
                System.out.println(documentTextResult);
                Log.d("main activity","RESULT: " + documentTextResult);
                Toast.makeText(this, documentTextResult + "", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onFragmentInteraction(Uri uri) {
        // TODO: what
    }
}
