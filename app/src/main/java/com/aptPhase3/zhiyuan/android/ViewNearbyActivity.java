package com.aptPhase3.zhiyuan.android;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.aptdemo.yzhao.androiddemo.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

public class ViewNearbyActivity extends ActionBarActivity
        implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{
    Context context = this;
    protected MyApplication myApp;
    private GoogleApiClient geoClient;
    private String TAG  = "View Nearby Streams";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_nearby);
        myApp = (MyApplication)this.getApplication();
        buildGoogleApiClient();
        geoClient.connect();
    }



    protected synchronized void buildGoogleApiClient() {
        geoClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Reaching onConnected means we consider the user signed in.
        System.out.println("onConnected call");

        String request_url=myApp.back_end+"android/view_nearby?";
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                geoClient);
        if(mLastLocation!=null){
            String lat = ((Double)mLastLocation.getLatitude()).toString();
            String lon = ((Double)mLastLocation.getLongitude()).toString();
            request_url += "latitude=" + lat;
            request_url += "&longitude=" + lon;
        }else{
            Toast.makeText(context, "Failed to retrieve location", Toast.LENGTH_SHORT).show();
        }
        System.out.println(request_url);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(request_url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                System.out.println("Post successful!!!!!!!");
                final ArrayList<String> imageURLs = new ArrayList<String>();
                final ArrayList<String> allStreamId = new ArrayList<String>();
                try {
                    JSONObject jObject = new JSONObject(new String(response));
                    JSONArray displayImages = jObject.getJSONArray("displayImages");
                    JSONArray displayStreamId = jObject.getJSONArray("streamId");
                    for(int i=0;i<displayImages.length();i++) {

                        imageURLs.add(displayImages.getString(i));
                        allStreamId.add(displayStreamId.getString(i));
//                        System.out.println(displayImages.getString(i));
                    }
                    GridView gridview = (GridView) findViewById(R.id.gridview);
                    gridview.setAdapter(new ImageAdapter(context,imageURLs));
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {

//                            Toast.makeText(context, imageCaps.get(position), Toast.LENGTH_SHORT).show();
//
//                            Dialog imageDialog = new Dialog(context);
//                            imageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                            imageDialog.setContentView(R.layout.thumbnail);
//                            ImageView image = (ImageView) imageDialog.findViewById(R.id.thumbnail_IMAGEVIEW);
//
//                            Picasso.with(context).load(imageURLs.get(position)).into(image);
//
//                            imageDialog.show();
                            String stream_id = allStreamId.get(position);
                            Intent i = new Intent(getApplicationContext(), ViewAStreamActivity.class);
                            i.putExtra("stream_id",stream_id);
                            startActivity(i);
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
    public void onConnectionSuspended(int i){

    }

    @Override
    public void onConnectionFailed(ConnectionResult result){

    }

}
