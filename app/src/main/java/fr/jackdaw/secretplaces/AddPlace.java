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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import fr.jackdaw.utils.Constants;
import fr.jackdaw.utils.SSLCertificateHandler;

@SuppressLint("NewApi")
public class AddPlace extends ActionBarActivity {

	private static String timeStamp;
	private EditText txtTitle;
	private ImageButton btnTakePicture;
	private Button btnAdd;
	private ImageView imgPicture;
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

		btnTakePicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

				fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
				intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

				// start the image capture Intent
				startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			}
		});

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


	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type){
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "MyCameraApp");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE){
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"IMG_"+ timeStamp + ".jpg");
		} else if(type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"VID_"+ timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
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
		imgPicture = (ImageView) findViewById(R.id.add_place_img_show_picture);

		btnAdd = (Button) findViewById(R.id.add_place_btn_add);
		btnCancel = (Button) findViewById(R.id.add_place_btn_cancel);
		btnAdd.setEnabled(false);
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

		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Bitmap bmp = BitmapFactory.decodeFile(fileUri.getPath());
				ExifInterface ei = null;
				try {
					ei = new ExifInterface(fileUri.getPath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				bmpResized = scaleDown(bmp, MAX_IMAGE_SIZE, true);
				int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

				Matrix matrix = new Matrix();

				switch(orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					System.out.println("90");
					matrix.postRotate(90);
					bmpResized = Bitmap.createBitmap(bmpResized , 0, 0, bmpResized .getWidth(), bmpResized .getHeight(), matrix, true);
					imgPicture.setImageBitmap(bmpResized);
					//imgPicture.setRotation(90);
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					System.out.println("180");
					matrix.postRotate(180);
					bmpResized = Bitmap.createBitmap(bmpResized , 0, 0, bmpResized .getWidth(), bmpResized .getHeight(), matrix, true);
					imgPicture.setImageBitmap(bmpResized);
					//imgPicture.setRotation(180);
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					System.out.println("270");
					matrix.postRotate(270);
					bmpResized = Bitmap.createBitmap(bmpResized , 0, 0, bmpResized .getWidth(), bmpResized .getHeight(), matrix, true);
					imgPicture.setImageBitmap(bmpResized);
					//imgPicture.setRotation(270);
					break;
				}

				btnAdd.setEnabled(true);

				LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
				myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
				longitude = (float) myLocation.getLongitude();
				latitude = (float) myLocation.getLatitude();
			} else if (resultCode == RESULT_CANCELED) {

			} else {

			}
		}
	}

	public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
		float ratio = Math.min(
				(float) maxImageSize / realImage.getWidth(),
				(float) maxImageSize / realImage.getHeight());
		int width = Math.round((float) ratio * realImage.getWidth());
		int height = Math.round((float) ratio * realImage.getHeight());

		Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
				height, filter);
		return newBitmap;
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
		progessDialog.setMessage("Envoi de votre place secréte ...");
		progessDialog.setCancelable(false);
		progessDialog.show();

        RequestParams params = new RequestParams();
        params.add(Constants.TITLE ,name);
        params.add(Constants.DESCRIPTION ,txtDescription.getText().toString());
        // TODO mettre URL de l'image
        params.add(Constants.URLIMAGE , "");
        params.add(Constants.LATITUDE , String.valueOf(latt));
        params.add(Constants.LONGITUDE , String.valueOf(longi));

        //client.put(Constants.URL_API+"?func=record&titre="+name+"&lat="+latt+"&lon="+longi+"&filename="+(txtTitle.getText().toString()+timeStamp).replaceAll(" ", "")+"&description="+txtDescription.getText().toString(), null, responseHandler);
        client.post(Constants.URL_API, params, responseHandler);


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
}
