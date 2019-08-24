package com.hackthe6ix2019.android.receipts;

import android.os.AsyncTask;
import android.util.Log;
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

    @Override
    protected String doInBackground(ByteBuffer... sourceImageBytes) {
        AmazonTextractClient amazonTextractClient = new AmazonTextractClient(credentials);

        Document source = new Document().withBytes(sourceImageBytes[0]);
        DetectDocumentTextRequest documentTextRequest = new DetectDocumentTextRequest().withDocument(source);
        DetectDocumentTextResult documentTextResult = amazonTextractClient.detectDocumentText(documentTextRequest);

        Log.d("async textract","RESULT: " + documentTextResult);

        List<Block> blockList = documentTextResult.getBlocks();
        for (Block block: blockList) {
            Log.d("async textract","TEXT: " + block.getText());
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
    }


    @Override
    protected void onPreExecute() {
        // setup
    }


//    @Override
//    protected void onProgressUpdate(String... text) {
//        //finalResult.setText(text[0]);
//        Toast.makeText(this, documentTextResult + "", Toast.LENGTH_LONG).show();
//    }
}
