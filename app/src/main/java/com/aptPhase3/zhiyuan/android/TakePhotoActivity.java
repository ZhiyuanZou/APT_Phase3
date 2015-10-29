package com.aptPhase3.zhiyuan.android;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.aptdemo.yzhao.androiddemo.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import android.hardware.Camera;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import android.hardware.Camera.PictureCallback;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import java.io.File;
import android.os.Environment;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.FileOutputStream;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.app.Activity;
import android.widget.Toast;
import android.graphics.Matrix;
import java.io.ByteArrayOutputStream;
import org.apache.http.Header;

public class TakePhotoActivity extends ActionBarActivity implements SurfaceHolder.Callback, PictureCallback, View.OnClickListener
        ,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    Context context = this;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Button takePhotoButton;
    private Camera mCamera;
    private boolean mPreviewRunning;
    private View.OnClickListener listener;
    private String mCurrentPhotoPath;
    private File photo;
    private ImageView mImageView;
    private Button submitButton;
    private String streamId;
    private byte[] mImage;
    protected MyApplication myApp;
    private GoogleApiClient geoClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        myApp = (MyApplication)this.getApplication();
        Bundle extras = getIntent().getExtras();
        streamId = extras.getString("stream_id");

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setVisibility(View.VISIBLE);
        listener = this;

        //for geo
        buildGoogleApiClient();

        // for taking picture
        takePhotoButton = (Button) findViewById(R.id.Take_Pic_Button);
        takePhotoButton.setOnClickListener(listener);
        takePhotoButton.setVisibility(View.VISIBLE);
        takePhotoButton.setEnabled(true);
        takePhotoButton.setClickable(true);

        submitButton = (Button) findViewById(R.id.submite_button);
        submitButton.setOnClickListener(listener);
        submitButton.setEnabled(false);
        submitButton.setClickable(false);

        mSurfaceHolder = mSurfaceView.getHolder();

        mSurfaceHolder.addCallback(this);

        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mPreviewRunning = true;
        System.out.println("take photo created!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    protected synchronized void buildGoogleApiClient() {
        geoClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnectionSuspended(int i){

    }

    @Override
    public void onConnectionFailed(ConnectionResult result){

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.Take_Pic_Button:
                System.out.println("take_photo_button_clicked");
                mCamera.takePicture(null,null,this);
                break;
            case R.id.submite_button:
                geoClient.connect();
                break;
            case R.id.imageView:
                System.out.println("imageView clicked");
                showImage();
                break;
        }
    }

    public void showImage(){
        System.out.println("showImage called");
        Dialog imageDialog = new Dialog(context);
        imageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        imageDialog.setContentView(R.layout.thumbnail);
        ImageView image = (ImageView) imageDialog.findViewById(R.id.thumbnail_IMAGEVIEW);

        Picasso.with(context).load(mCurrentPhotoPath).into(image);

        imageDialog.show();
    }

    @Override
    public void onConnected(Bundle connectionHint){
        AsyncHttpClient httpClient = new AsyncHttpClient();
        String request_url=myApp.back_end+"android/upload_image?stream_id="+streamId;
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
        params.put("files",new ByteArrayInputStream(mImage));
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
        finish();
    }

//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        // Ensure that there's a camera activity to handle the intent
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            // Create the File where the photo should go
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                // Error occurred while creating the File
//                ...
//            }
//            // Continue only if the File was successfully created
//            if (photoFile != null) {
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
//                        Uri.fromFile(photoFile));
//                startActivityForResult(takePictureIntent, 1);
//            }
//        }
//    }

    public Bitmap rotaingImageView(int angle , Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();;
        matrix.postRotate(angle);
        System.out.println("angle2=" + angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    public void surfaceCreated(SurfaceHolder holder) {

        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);
    }
       //  mCamera is an Object of the class “Camera”. In the surfaceCreated we “open” the camera. This is how to start it!!

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

            if (mPreviewRunning) {

                mCamera.stopPreview();

            }
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();

            // You need to choose the most appropriate previewSize for your app
            Camera.Size previewSize = previewSizes.get(0);

            parameters.setPreviewSize(previewSize.width, previewSize.height);
            mCamera.setParameters(parameters);
            mCamera.startPreview();


            try {

                mCamera.setPreviewDisplay(holder);

            } catch (IOException e) {

                e.printStackTrace();

            }

            mCamera.startPreview();

            mPreviewRunning = true;

        }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mPreviewRunning = false;
        mCamera.release();
    }
    @Override
    public void onPictureTaken(byte[] data, Camera camera){
        System.out.println("Picture Taken"+data.toString());
        photo = null;
        try{
            photo = createImageFile();
        }catch(IOException ioe){
            System.out.println("File creation failed");
        }

        if (data != null) {
            
            submitButton.setClickable(true);
            submitButton.setEnabled(true);
            mImageView.setClickable(true);
            mImageView.setOnClickListener(this);
            mImageView.setEnabled(true);
            Bitmap m_bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//            rotateImage(m_bitmap);
            if (m_bitmap != null) {
                try{
                    FileOutputStream m_out = new FileOutputStream(photo);
                    m_bitmap = rotaingImageView(90,m_bitmap);
                    m_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, m_out);
                    mImageView.setImageBitmap(m_bitmap);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    m_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    mImage = stream.toByteArray();
                }catch(IOException e){
                    System.out.println("Exception in filecreation");
                }
            }
        }
        mCamera.startPreview();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }


}
