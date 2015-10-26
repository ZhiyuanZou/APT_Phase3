package com.aptPhase3.zhiyuan.android;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.aptdemo.yzhao.androiddemo.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import android.location.Location;
import com.google.android.gms.location.LocationServices;


public class ImageUpload extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int PICK_IMAGE = 1;
    Context context = this;
    private  String stream_id;
    protected MyApplication myApp;
    private String photoCaption;
    private byte[] encodedImage;
    private GoogleApiClient geoClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApp = (MyApplication)this.getApplication();
        setContentView(R.layout.activity_image_upload);
        buildGoogleApiClient();
        Bundle extras = getIntent().getExtras();
        stream_id = extras.getString("stream_id");
        // Choose image from library
        Button chooseFromLibraryButton = (Button) findViewById(R.id.choose_from_library);
        chooseFromLibraryButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // To do this, go to AndroidManifest.xml to add permission
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        // Start the Intent
                        startActivityForResult(galleryIntent, PICK_IMAGE);
                    }
                }
        );
    }

    protected synchronized void buildGoogleApiClient() {
        geoClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_upload, menu);
        return true;
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


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && data != null && data.getData() != null) {
            Uri selectedImage = data.getData();

            // User had pick an image.

            String[] filePathColumn = {MediaStore.Images.ImageColumns.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            // Link to the image

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String imageFilePath = cursor.getString(columnIndex);
            cursor.close();

            // Bitmap imaged created and show thumbnail

            ImageView imgView = (ImageView) findViewById(R.id.thumbnail);
            final Bitmap bitmapImage = BitmapFactory.decodeFile(imageFilePath);
            imgView.setImageBitmap(bitmapImage);

            // Enable the upload button once image has been uploaded

            Button uploadButton = (Button) findViewById(R.id.upload_to_server);
            uploadButton.setClickable(true);

            uploadButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // Get photo caption

                            EditText text = (EditText) findViewById(R.id.upload_message);
                            photoCaption = text.getText().toString();

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            encodedImage = baos.toByteArray();
                            geoClient.connect();
                        }
                    }
            );
        }
    }
//
    @Override
    public void onConnectionSuspended(int i){

    }

    @Override
    public void onConnectionFailed(ConnectionResult result){

    }

    @Override
    public void onConnected(Bundle connectionHint){
        System.out.println("upload onConnected method called!!!!!!!!!!");
        String request_url=myApp.back_end+"android/upload_image?stream_id="+stream_id;
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                geoClient);
        if(mLastLocation!=null){
            String lat = ((Double)mLastLocation.getLatitude()).toString();
            String lon = ((Double)mLastLocation.getLongitude()).toString();
            request_url += "&latitude=" + lat;
            request_url += "&longitude=" + lon;
        }else{
            Toast.makeText(context, "Failed to retrieve location", Toast.LENGTH_SHORT).show();
        }
        System.out.println(request_url);
        RequestParams params = new RequestParams();
        params.put("files",new ByteArrayInputStream(encodedImage));
        params.put("photoCaption",photoCaption);
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(request_url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                Log.w("async", "success!!!!");
                Toast.makeText(context, "Upload Successful", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e("Posting_to_blob","There was a problem in retrieving the url : " + e.toString());
            }
        });
    }

    private void getUploadURL(final byte[] encodedImage, final String photoCaption){
        AsyncHttpClient httpClient = new AsyncHttpClient();

        String request_url=myApp.back_end+"android/upload_image?stream_id="+stream_id;
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                myApp.mGoogleApiClient);
        if(mLastLocation!=null){
            String lat = ((Double)mLastLocation.getLatitude()).toString();
            String lon = ((Double)mLastLocation.getLongitude()).toString();
            request_url += "&latitude=" + lat;
            request_url += "&longitude=" + lon;
        }else{
            Toast.makeText(context, "Failed to retrieve location", Toast.LENGTH_SHORT).show();
        }
        System.out.println(request_url);
            RequestParams params = new RequestParams();
            params.put("files",new ByteArrayInputStream(encodedImage));
            params.put("photoCaption",photoCaption);
            AsyncHttpClient client = new AsyncHttpClient();
            client.post(request_url, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                    Log.w("async", "success!!!!");
                    Toast.makeText(context, "Upload Successful", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                    Log.e("Posting_to_blob","There was a problem in retrieving the url : " + e.toString());
            }
        });

    }


    public void viewAllImages(View view){
        Intent intent= new Intent(this, DisplayImages.class);

        startActivity(intent);
    }
}
