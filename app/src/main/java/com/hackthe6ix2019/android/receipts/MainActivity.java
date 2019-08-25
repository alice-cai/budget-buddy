package com.hackthe6ix2019.android.receipts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.CreateMoneyMutation;
import com.amazonaws.amplify.generated.graphql.ListMoneysQuery;
import com.amazonaws.amplify.generated.graphql.OnCreateMoneySubscription;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

import type.CreateMoneyInput;

public class MainActivity extends AppCompatActivity implements BudgetFragment.OnFragmentInteractionListener {

    private TextView mTextMessage;
    private static final int Image_Capture_Code = 1;
    private AWSAppSyncClient mAWSAppSyncClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // defaults to the budget page
        Fragment defaultFragment = new BudgetFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, defaultFragment).commit();

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    // Add to database
    public void runMutation(){
        CreateMoneyInput createMoneyInput = CreateMoneyInput.builder().
                cost(1.00).
                category("Entertainment").
                build();

        mAWSAppSyncClient.mutate(CreateMoneyMutation.builder().input(createMoneyInput).build())
                .enqueue(mutationCallback);
    }

    private GraphQLCall.Callback<CreateMoneyMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateMoneyMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateMoneyMutation.Data> response) {
            Log.i("Results", "Added Money");
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error", e.toString());
        }
    };

    // Query Data
    public void runQuery(){
        mAWSAppSyncClient.query(ListMoneysQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(moneysCallback);
    }

    private GraphQLCall.Callback<ListMoneysQuery.Data> moneysCallback = new GraphQLCall.Callback<ListMoneysQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListMoneysQuery.Data> response) {
            Log.i("Results", response.data().listMoneys().items().toString());
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };

    // Subscription
    private AppSyncSubscriptionCall subscriptionWatcher;

    private void subscribe(){
        OnCreateMoneySubscription subscription = OnCreateMoneySubscription.builder().build();
        subscriptionWatcher = mAWSAppSyncClient.subscribe(subscription);
        subscriptionWatcher.execute(subCallback);
    }

    private AppSyncSubscriptionCall.Callback subCallback = new AppSyncSubscriptionCall.Callback() {
        @Override
        public void onResponse(@Nonnull Response response) {
            Log.i("Response", response.data().toString());
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error", e.toString());
        }

        @Override
        public void onCompleted() {
            Log.i("Completed", "Subscription completed");
        }
    };

    // Bottom Navigation
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;

            switch (item.getItemId()) {
                case R.id.navigation_summary:
                    //mTextMessage.setText(R.string.title_home);
                    // TODO: change this to: fragment = new HomeFragment();
                    fragment = new BudgetFragment();
                    break;
                case R.id.navigation_camera:
                    //mTextMessage.setText(R.string.title_dashboard);
                    Intent cInt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cInt,Image_Capture_Code);
                    fragment = new BudgetFragment();
                    // TODO: change this to: fragment = new HomeFragment();
                    break;
//                case R.id.navigation_history:
//                    //mTextMessage.setText(R.string.title_notifications);
//                    fragment = new BudgetFragment();
//                    // TODO: change this to: fragment = new HomeFragment();
//                    break;
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
                bp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                //Toast.makeText(this, byteArray + "", Toast.LENGTH_LONG).show();
                //Log.d("main activity","DATA EXTRAS: " + data.getExtras());

                ByteBuffer sourceImageBytes = ByteBuffer.wrap(byteArray);
                new AsyncTextractRequest(this).execute(sourceImageBytes);

                // aws stuff
                //AmazonRekognition rekognitionClient = new AmazonRekognitionClient(new BasicAWSCredentials("", ""));
//
//                // TODO: remove later
//                BasicAWSCredentials credentials = new BasicAWSCredentials("AKIASMTWCNOQRPPVZXFP", "zbRVfIYS2Y+6a92/jUtn4+B8fUUi6cgjwuZ5F/Wu");
//
//                AmazonTextractClient amazonTextractClient = new AmazonTextractClient(credentials);
//                ByteBuffer sourceImageBytes = ByteBuffer.wrap(byteArray);
//                Document source = new Document().withBytes(sourceImageBytes);
//                DetectDocumentTextRequest documentTextRequest = new DetectDocumentTextRequest().withDocument(source);
//                DetectDocumentTextResult documentTextResult = amazonTextractClient.detectDocumentText(documentTextRequest);
//                System.out.println(documentTextResult);
//                Log.d("main activity","RESULT: " + documentTextResult);
//                Toast.makeText(this, documentTextResult + "", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onFragmentInteraction(Uri uri) {
        // TODO: what
    }
}
