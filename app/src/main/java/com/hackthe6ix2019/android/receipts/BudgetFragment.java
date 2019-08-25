package com.hackthe6ix2019.android.receipts;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amazonaws.amplify.generated.graphql.CreateMoneyMutation;
import com.amazonaws.amplify.generated.graphql.ListMoneysQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;
import type.CreateMoneyInput;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BudgetFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BudgetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BudgetFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ArrayList<String> categories = new ArrayList<>();
    private ArrayList<Float> costs = new ArrayList<>();

    public BudgetFragment() {
        // Requ
        //runQuery(this.getContext());
    }

    public BudgetFragment(Context context) {
        // Requ
        runQuery(context);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BudgetFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BudgetFragment newInstance(String param1, String param2) {
        BudgetFragment fragment = new BudgetFragment();
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

        runQuery(this.getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        PieChartView pieChartView = view.findViewById(R.id.chart);

        Log.i("pie", "running oncreateview");
        // get updated values from api here

        List<SliceValue> pieData = new ArrayList<>();

        String[] colours = {"#82dae0", "#fff0a5", "#e5b0b1", "#b7adc7", "#b0e5ca"};

        Log.i("indicies", costs.size() + " " + categories.size());

        for (int i = 0; i < Math.min(5, costs.size()); i++) {
            Log.i("pie", categories.get(i) + ": " + costs.get(i));
            pieData.add(new SliceValue(costs.get(i), Color.parseColor(colours[i])).setLabel(categories.get(i) + ": " + costs.get(i)));
        }

//        pieData.add(new SliceValue(15, Color.parseColor("#82dae0")).setLabel("Food: $15"));
//        pieData.add(new SliceValue(25, Color.parseColor("#fff0a5")).setLabel("Clothing: $25"));
//        pieData.add(new SliceValue(5, Color.parseColor("#b0e5ca")).setLabel("Bills: $10"));
//        pieData.add(new SliceValue(30, Color.parseColor("#b0e5ca")).setLabel("Health: $30"));
//        pieData.add(new SliceValue(20, Color.parseColor("#b7adc7")).setLabel("Transit: $20"));

        PieChartData pieChartData = new PieChartData(pieData);
        pieChartData.setHasLabels(true).setValueLabelTextSize(12);
        pieChartData.setHasCenterCircle(true)
                .setCenterCircleScale((float)(0.3));
//                .setCenterText1("spending")
//                .setCenterText1FontSize(20)
//                .setCenterText1Color(Color.parseColor("#787878"));
        pieChartView.setPieChartData(pieChartData);

        // Inflate the layout for this fragment
        return view;

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

    // Query Data
    public void runQuery(Context context){
        AWSAppSyncClient mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(context.getApplicationContext())
                .awsConfiguration(new AWSConfiguration(context.getApplicationContext()))
                .build();

        mAWSAppSyncClient.query(ListMoneysQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(moneysCallback);
    }

    private void update() {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        Fragment newFragment = this;
        this.onDestroy();
        ft.remove(this);
        ft.replace(this.getId(), newFragment);
        //container is the ViewGroup of current fragment
        ft.addToBackStack(null);
        ft.commit();
    }

    private GraphQLCall.Callback<ListMoneysQuery.Data> moneysCallback = new GraphQLCall.Callback<ListMoneysQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListMoneysQuery.Data> response) {
            String dataString = response.data().listMoneys().items().toString();
            String[] stringFragments = dataString.split("cost=");

            costs.clear();
            categories.clear();

            for (int i = 1; i < stringFragments.length; i++) {
                //Log.i("Results", "cost: " + Double.parseDouble(stringFragments[i].substring(0, 5).replace("[^\\d.]", "")));
                costs.add(Float.parseFloat(stringFragments[i].substring(0, 2).replace("[^\\d.]", "")));

                int end  = stringFragments[i].indexOf("}");
                Log.i("Results", "cat: " + stringFragments[i].substring(16, end));
                categories.add(stringFragments[i].substring(16, end));
            }
            Log.i("Results", response.data().listMoneys().items().toString());

            update();
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };

    // Subscription
    private AppSyncSubscriptionCall subscriptionWatcher;

//    private void subscribe(){
//        OnCreateMoneySubscription subscription = OnCreateMoneySubscription.builder().build();
//        subscriptionWatcher = mAWSAppSyncClient.subscribe(subscription);
//        subscriptionWatcher.execute(subCallback);
//    }

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
}
