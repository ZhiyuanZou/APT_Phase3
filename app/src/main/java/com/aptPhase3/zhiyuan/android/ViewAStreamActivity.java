package com.aptPhase3.zhiyuan.android;
import android.app.Dialog;
import android.content.Context;

import android.content.Intent;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.aptdemo.yzhao.androiddemo.R;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;



public class ViewAStreamActivity extends ActionBarActivity implements ConnectionCallbacks, View.OnClickListener{
    Context context = this;
    private String TAG  = "Display Images";
    protected MyApplication myApp;
    private String stream_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_astream);
        myApp = (MyApplication)this.getApplication();
        Bundle extras = getIntent().getExtras();
        stream_id = extras.getString("stream_id");
        // set clickable
        Button uploadButton = (Button) findViewById(R.id.upload_button);
        System.out.println("!!!!!!!!!!"+uploadButton);
        uploadButton.setClickable(true);

        final String request_url = "http://apt-phase3.appspot.com/android/view_single_stream?user_id="+myApp.userName+"&stream_id="+stream_id;
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.get(request_url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                final ArrayList<String> imageURLs = new ArrayList<String>();
                final ArrayList<String> imageCaps = new ArrayList<String>();
                try {
                    JSONObject jObject = new JSONObject(new String(response));
//                    System.out.println(jObject.toString());
                    JSONArray displayImages = jObject.getJSONArray("displayImages");
//                    JSONArray displayCaption = jObject.getJSONArray("imageCaptionList");
                    for(int i=0;i<displayImages.length();i++) {
                        JSONObject singleImage = new JSONObject(displayImages.getString(i));
                        String tmp = singleImage.getString("url");
                        imageURLs.add(tmp);
//                        imageCaps.add(displayCaption.getString(i));
//                        System.out.println(displayImages.getString(i));
                    }
                    GridView gridview = (GridView) findViewById(R.id.gridview);
                    gridview.setAdapter(new ImageAdapter(context,imageURLs));
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {

//                            Toast.makeText(context, imageCaps.get(position), Toast.LENGTH_SHORT).show();

                            Dialog imageDialog = new Dialog(context);
                            imageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            imageDialog.setContentView(R.layout.thumbnail);
                            ImageView image = (ImageView) imageDialog.findViewById(R.id.thumbnail_IMAGEVIEW);

                            Picasso.with(context).load(imageURLs.get(position)).into(image);

                            imageDialog.show();
                        }
                    });
                }
                catch(JSONException j){
                    System.out.println("JSON Error");
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "There was a problem in retrieving the url : " + e.toString());
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display_images, menu);
        return true;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Reaching onConnected means we consider the user signed in.
        Log.i(TAG, "onConnected");
        System.out.println("onConnected call");
        Button uploadButton = (Button) findViewById(R.id.upload_button);
        uploadButton.setClickable(true);

        uploadButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("upload_button_clicked");
                        Intent intent = new Intent(context, ImageUpload.class);
                        intent.putExtra("stream_id", stream_id);
                        startActivity(intent);
                    }
                }
        );
    }

    @Override
    public void onClick(View v) {

            switch (v.getId()) {
                case R.id.upload_button:
                    System.out.println("upload_button_clicked");
                    Intent intent = new Intent(context, ImageUpload.class);
                    intent.putExtra("stream_id", stream_id);
                    startActivity(intent);
                    break;
            }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        // We call connect() to attempt to re-establish the connection or get a
        // ConnectionResult that we can attempt to resolve.
        myApp.mGoogleApiClient.connect();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
