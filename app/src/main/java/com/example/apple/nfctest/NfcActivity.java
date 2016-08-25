package com.example.apple.nfctest;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class NfcActivity extends AppCompatActivity {
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";

    private TextView mTextView;
    private NfcAdapter mNfcAdapter;
    //private ImageView  mImageView;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        mTextView = (TextView) findViewById(R.id.textView_explanation);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        //mImageView = (ImageView) findViewById(R.id.imageView);
        //mImageView.setImageResource(R.drawable.TagNotice);

        if (mNfcAdapter == null) {

            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;

        }
        if (!mNfcAdapter.isEnabled()) {
            mTextView.setText("Please open NFC");

        } else {
            mTextView.setText(R.string.explanation);


        }
        handleIntent(getIntent());










        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


                //startActivity(new Intent(NfcActivity.this, MainActivity1.class));
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume(){
        super.onResume();


        setupForegroundDispatch(this,mNfcAdapter);
    }


    @Override
    protected void onPause(){

        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }
    @Override
    protected void onNewIntent(Intent intent){

        handleIntent(intent);

    }

    private  void handleIntent(Intent intent){
        String action = intent.getType();
        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)){

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)){

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            }else {
                Log.d(TAG, "wrong mime type: " + type);

            }

        }else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)){

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList){
                if (searchedTech.equals(tech)){
                    new NdefReaderTask().execute(tag);

                    break;

                }

            }

        }

    }
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter){
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        //Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);

        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        }   catch (IntentFilter.MalformedMimeTypeException e){

            throw new RuntimeException("check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);

    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter){

        adapter.disableForegroundDispatch(activity);

    }

    private class NdefReaderTask extends AsyncTask<Tag, Void, String>{

        @Override
        protected String doInBackground(Tag... params){

        Tag tag = params[0];

         Ndef ndef = Ndef.get(tag);

            if(ndef == null){
                //NDEF is not supported by this Tag
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();

            for (NdefRecord ndefRecord : records){

                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)){

                    try {
                        return readText(ndefRecord);
                    }catch (UnsupportedEncodingException e){

                        Log.e(TAG,"Unspported Encoding", e);
                    }


                }


            }

            return null;

        }

        private  String readText(NdefRecord record) throws UnsupportedEncodingException{

            byte[] payload = record.getPayload();

            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            int languageCodeLength = payload[0] & 0063;

            return  new String(payload,languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);

        }

        @Override
        protected  void  onPostExecute(String result){

            if(result != null){


                mTextView.setText("Read content" + result);

            }


        }




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nfc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Nfc Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.apple.nfctest/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Nfc Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.apple.nfctest/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
