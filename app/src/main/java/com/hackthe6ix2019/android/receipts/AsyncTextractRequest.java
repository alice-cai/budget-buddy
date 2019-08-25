package com.hackthe6ix2019.android.receipts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.textract.AmazonTextractClient;
import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.DetectDocumentTextRequest;
import com.amazonaws.services.textract.model.DetectDocumentTextResult;
import com.amazonaws.services.textract.model.Document;

import java.nio.ByteBuffer;
import java.util.List;

public class AsyncTextractRequest extends AsyncTask<ByteBuffer, Void, String> {

    private static final BasicAWSCredentials credentials =
            new BasicAWSCredentials("AKIASMTWCNOQRPPVZXFP", "zbRVfIYS2Y+6a92/jUtn4+B8fUUi6cgjwuZ5F/Wu");
    private Context context;
    private int costDollars = 0;
    private int costCents = 0;

    public AsyncTextractRequest(Context context) {
        super();
        this.context = context;
    }

    // TODO: remove debug code
    @Override
    protected String doInBackground(ByteBuffer... sourceImageBytes) {
        AmazonTextractClient amazonTextractClient = new AmazonTextractClient(credentials);

        Document source = new Document().withBytes(sourceImageBytes[0]);
        DetectDocumentTextRequest documentTextRequest = new DetectDocumentTextRequest().withDocument(source);
        DetectDocumentTextResult documentTextResult = amazonTextractClient.detectDocumentText(documentTextRequest);

        Log.d("async textract","RESULT: " + documentTextResult);

        List<Block> blockList = documentTextResult.getBlocks();
        for (Block block: blockList) {
            String text = block.getText();

            if (text != null) {
                if (text.contains("total")) {
                    Log.d("async textract","TEXT: " + text);
                } else if (text.startsWith("$")) {
                    Log.d("async textract", "FOUND COST: " + text);

                    // remove all non numeric except period, then split around period
                    String cleanedCost = text.replaceAll("[^\\d.]", "");
                    Log.d("async textract", "CLEANED: " + cleanedCost);

                    String[] splitCost = cleanedCost.split("\\.");
                    Log.d("async textract", "SPLIT SIZE: " + splitCost.length);

                    if (splitCost.length >= 2 && splitCost[0].length() > 0 && splitCost[1].length() > 0) {
                        costDollars = Integer.parseInt(splitCost[0]);
                        costCents = Integer.parseInt(splitCost[1]);
                        Log.d("async textract", "DOLLARS: " + costDollars);
                        Log.d("async textract", "CENTS: " + costCents);
                        break;
                    }
                }
            }

            //Log.d("async textract","TEXT: " + block.getText());
        }

        return documentTextResult + "";
    }


    @Override
    protected void onPostExecute(String result) {
        // execution of result of Long time consuming operation
//        progressDialog.dismiss();
//        finalResult.setText(result);
        //Toast.makeText(this, "success!", Toast.LENGTH_LONG).show();
        Log.d("async textract","ON POST EXECUTE REACHED");

        Intent selectCategories = new Intent(context, SelectCategoryActivity.class);
        Bundle costBundle = new Bundle();
        costBundle.putInt("dollars", costDollars);
        costBundle.putInt("cents", costCents);
        selectCategories.putExtras(costBundle);

        context.startActivity(selectCategories);
    }


    @Override
    protected void onPreExecute() {
        context.startActivity(new Intent(context, LoadingActivity.class));
    }


//    @Override
//    protected void onProgressUpdate(String... text) {
//        //finalResult.setText(text[0]);
//        Toast.makeText(this, documentTextResult + "", Toast.LENGTH_LONG).show();
//    }
}
