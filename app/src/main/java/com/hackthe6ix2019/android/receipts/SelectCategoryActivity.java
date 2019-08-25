package com.hackthe6ix2019.android.receipts;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.CreateMoneyMutation;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import type.CreateMoneyInput;

public class SelectCategoryActivity extends AppCompatActivity {

    private int costDollars = 0;
    private int costCents = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_category);
        updateCostMessage();

        int[][] buttons = {
            {R.id.category1, R.string.category_1},
            {R.id.category2, R.string.category_2},
            {R.id.category3, R.string.category_3},
            {R.id.category4, R.string.category_4},
            {R.id.category5, R.string.category_5}
        };
        for (int[] button: buttons) {
            addButtonListener(button[0], button[1]);
        }
    }

    private void updateCostMessage() {
        Bundle costBundle = getIntent().getExtras();
        TextView mCostMessage = (TextView) findViewById(R.id.costMessage);

        String costMessage = "$0.00";
        if (costBundle != null) {
            costDollars = costBundle.getInt("dollars");
            costCents = costBundle.getInt("cents");
            costMessage = String.format("$%d.%02d", costDollars, costCents);
        }

        mCostMessage.setText(costMessage);
    }

    private void addButtonListener(int buttonId, final int stringId) {
        Button button = (Button) findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float cost = (float) (costDollars + (costCents / 100));
                String key = " " + getString(stringId);
                runMutation(v.getContext(), cost, key);
                startActivity(new Intent(v.getContext(), MainActivity.class));
            }
        });
    }

    // Add to database
    public void runMutation(Context context, float item_cost, String item_category){
        AWSAppSyncClient mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(context.getApplicationContext())
                .awsConfiguration(new AWSConfiguration(context.getApplicationContext()))
                .build();

        CreateMoneyInput createMoneyInput = CreateMoneyInput.builder().
                cost(item_cost).
                category(item_category).
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
}
