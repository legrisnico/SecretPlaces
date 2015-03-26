package fr.jackdaw.secretplaces;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import fr.jackdaw.camera.CameraLollipop;
import fr.jackdaw.utils.Constants;
import fr.jackdaw.utils.SSLCertificateHandler;

@SuppressLint("NewApi")
public class AddPlace extends ActionBarActivity {

    private static String timeStamp;
    private EditText txtTitle;
    private ImageButton btnTakePicture;
    private Button btnAdd;
    private FrameLayout imgPicture;
    protected Date dateActuelle;
    private Button btnCancel;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int MAX_IMAGE_SIZE = 1000;
    public Activity activity;
    private Location myLocation;
    private float longitude;
    private float latitude;
    private Bitmap bmpResized;
    protected String encodedString;
    private ProgressBar progressBar;
    private ProgressDialog progessDialog;
    protected String id;
    private EditText txtDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        SSLCertificateHandler.nuke();

        activity = this;

        initFields();
        initialisationActionBar();

        if(checkCameraHardware(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                final CameraLollipop camera = CameraLollipop.newInstance();
                if (null == savedInstanceState) {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.add_place_img_show_picture, camera)
                            .commit();
                }

                btnTakePicture.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        camera.takePicture();
                    }
                });
            }else{

                btnTakePicture.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                    }
                });

            }
        }else{
            btnTakePicture.setEnabled(false);
        }


        btnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.pull_in_from_top, R.anim.pull_out_to_top);
            }
        });

        btnAdd.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sendPlace(txtTitle.getText().toString(), longitude, latitude);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_place, menu);
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

    @Override
    public void onBackPressed(){
        finish();
        overridePendingTransition(R.anim.pull_in_from_top, R.anim.pull_out_to_top);
    }

    public void initFields(){
        txtTitle = (EditText) findViewById(R.id.add_place_edttext_title);
        txtDescription = (EditText) findViewById(R.id.add_place_txt_description);
        btnTakePicture = (ImageButton) findViewById(R.id.add_place_btn_take_picture);
        imgPicture = (FrameLayout) findViewById(R.id.add_place_img_show_picture);

        btnAdd = (Button) findViewById(R.id.add_place_btn_add);
        btnCancel = (Button) findViewById(R.id.add_place_btn_cancel);
        btnAdd.setEnabled(false);

        /*ViewGroup.LayoutParams params = imgPicture.getLayoutParams();
        params.height = params.width;
        imgPicture.setLayoutParams(params);*/
    }

    public void initialisationActionBar() {
        ActionBar actionbar = getSupportActionBar();

        actionbar.setDisplayShowHomeEnabled(true);
        actionbar.setDisplayShowCustomEnabled(false);
        actionbar.setDisplayHomeAsUpEnabled(false);
        actionbar.setDisplayShowTitleEnabled(true);
    }

    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    public void sendPlace(String name, float longi, float latt) {
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);

        client.setTimeout(999999999);
        JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler() {

            public void onSuccess(int statusCode,org.apache.http.Header[] headers,org.json.JSONObject response) {
                System.out.println("success"+response);
                try {
                    id = response.getString("id");
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                sendPicture();
            }

            public void onFailure(int statusCode,org.apache.http.Header[] headers, Throwable throwable,	org.json.JSONObject response) {
                System.out.println("failure json"+response);
                try {
                    id = response.getString("id");
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                sendPicture();
            }

            public void onFailure(int statusCode,org.apache.http.Header[] headers,String result, Throwable throwable) {
                System.out.println("failure string"+result);
                id = result;
                sendPicture();
            }
        };

        progessDialog = new ProgressDialog(this);
        progessDialog.setMessage("Envoi de votre place secr�te ...");
        progessDialog.setCancelable(false);
        progessDialog.show();

        client.get(Constants.URL_API+"?func=record&titre="+name+"&lat="+latt+"&lon="+longi+"&filename="+(txtTitle.getText().toString()+timeStamp).replaceAll(" ", "")+"&description="+txtDescription.getText().toString(), null, responseHandler);

    }

    public void sendPicture() {
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);

        client.setTimeout(999999999);
        RequestParams params = new RequestParams();

        params.add("image", BitMapToString(bmpResized));
        params.add("filename", (txtTitle.getText().toString()+timeStamp).replaceAll(" ", ""));
        params.add("id", id);

        try {
            params.add("image", URLEncoder.encode(BitMapToString(bmpResized),"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler() {

            public void onSuccess(int statusCode,org.apache.http.Header[] headers,org.json.JSONObject response) {
                System.out.println("success"+response);
                progessDialog.dismiss();
                finish();
                overridePendingTransition(R.anim.pull_in_from_top, R.anim.pull_out_to_top);
            }

            public void onFailure(int statusCode,org.apache.http.Header[] headers, Throwable throwable,	org.json.JSONObject response) {
                System.out.println("failure json"+response);
                progessDialog.dismiss();
                finish();
                overridePendingTransition(R.anim.pull_in_from_top, R.anim.pull_out_to_top);

            }

            public void onFailure(int statusCode,org.apache.http.Header[] headers,String result, Throwable throwable) {
                System.out.println("failure string"+result);
                progessDialog.dismiss();
                finish();
                overridePendingTransition(R.anim.pull_in_from_top, R.anim.pull_out_to_top);
            }
        };

        client.post(Constants.URL_API_IMAGE, params, responseHandler);


    }

    /**
     * @param bitmap
     * @return converting bitmap and return a string
     */
    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,10, baos);
        byte [] b=baos.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }


    /**
     * check if the device has camera
     * @param context
     * @return true if camera exist false else
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        } else {
            return false;
        }
    }

    /**
     * get camera instance
     * @return
     */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
}
