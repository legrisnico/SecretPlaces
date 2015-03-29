package fr.jackdaw.secretplaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import modele.Place;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import fr.jackdaw.adapters.CustomListAdapter;
import fr.jackdaw.adapters.MainPageAdapter;
import fr.jackdaw.utils.Constants;
import fr.jackdaw.utils.SSLCertificateHandler;

public class Map extends ActionBarActivity {

	private final int DISTANCE = 1;
	private MainPageAdapter pageAdapter;
	private ViewPager mViewPager;
	private Button btnMap;
	private Button btnList;
	private int currentPage;
	private Date date_actuelle;
	private Location myLocation;
	private float longitude;
	private float latitude;
	private Map activity;
	private Map context;
	public static double current_point_longitude = 0, current_point_latitude = 0;
	public static boolean goToTheMap = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		SSLCertificateHandler.nuke();
		
		activity = this;
		context = this;

		initialisationActionBar();
		initFields();

		/** Implémentation et initialisation du viewPager des cartes */

		mViewPager = (ViewPager) findViewById(R.id.map_viewpager);
		System.out.println(getSupportFragmentManager());
		System.out.println(this);
		pageAdapter = new MainPageAdapter(getSupportFragmentManager(), this);
		mViewPager.setAdapter(pageAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub

				if (mViewPager.getCurrentItem() == 0) {	
					currentPage = 0;
					mViewPager.setCurrentItem(currentPage);
					btnMap.setBackgroundResource(R.drawable.btn_menu_selected);;
					btnList.setBackgroundResource(R.drawable.btn_menu);
				} else if (mViewPager.getCurrentItem() == 1) {	
					currentPage = 1;
					mViewPager.setCurrentItem(currentPage);
					btnMap.setBackgroundResource(R.drawable.btn_menu);
					btnList.setBackgroundResource(R.drawable.btn_menu_selected);

				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}
			@Override
			public void onPageScrollStateChanged(int arg0) {}
		});

		btnMap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				currentPage = 0;
				mViewPager.setCurrentItem(currentPage);
				btnMap.setBackgroundResource(R.drawable.btn_menu_selected);;
				btnList.setBackgroundResource(R.drawable.btn_menu);
			}
		});

		btnList.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				currentPage = 1;
				mViewPager.setCurrentItem(currentPage);
				btnMap.setBackgroundResource(R.drawable.btn_menu);
				btnList.setBackgroundResource(R.drawable.btn_menu_selected);
			}
		});

		refreshPlaces();
	}
	
	public void callOnResume(){
		if (goToTheMap == true){
			currentPage = 0;
			mViewPager.setCurrentItem(currentPage);
			btnMap.setBackgroundResource(R.drawable.btn_menu_selected);;
			btnList.setBackgroundResource(R.drawable.btn_menu);
			
			goToTheMap = false;
		}
		
		if (current_point_latitude != 0 && current_point_longitude != 0){
			FragmentMap fragmentMap = (FragmentMap)pageAdapter.getItem(0);
			CameraPosition cameraPosition = new CameraPosition.Builder()
			.target(new LatLng(current_point_latitude, current_point_longitude)).zoom(Constants.MAP_ZOOM_SELECTED).build();
			fragmentMap.getMap().animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
			
			current_point_latitude = 0;
			current_point_longitude = 0;
			
		}
	}
	
	public void onResume(){
		super.onResume();
		refreshPlaces();
		
		if (goToTheMap == true){
			currentPage = 0;
			mViewPager.setCurrentItem(currentPage);
			btnMap.setBackgroundResource(R.drawable.btn_menu_selected);;
			btnList.setBackgroundResource(R.drawable.btn_menu);
			
			goToTheMap = false;
		}
		
		if (current_point_latitude != 0 && current_point_longitude != 0){
			FragmentMap fragmentMap = (FragmentMap)pageAdapter.getItem(0);
			CameraPosition cameraPosition = new CameraPosition.Builder()
			.target(new LatLng(current_point_latitude, current_point_longitude)).zoom(Constants.MAP_ZOOM_SELECTED).build();
			fragmentMap.getMap().animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
			
			current_point_latitude = 0;
			current_point_longitude = 0;
			
		}
	}


	public void initFields(){
		btnMap = (Button) findViewById(R.id.map_button_map);
		btnList = (Button) findViewById(R.id.map_button_list);
	}

	public void initVars(){
		currentPage = 0;
	}

	public void initialisationActionBar() {
		ActionBar actionbar = getSupportActionBar();

		LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflator.inflate(R.layout.action_bar_custom, null);
		actionbar.setCustomView(v);	
		actionbar.setDisplayShowHomeEnabled(true);
		actionbar.setDisplayShowCustomEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(false);
		actionbar.setDisplayShowTitleEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_refresh) {
			refreshPlaces();
		}
		if (id == R.id.action_add_place) {
			Intent intent = new Intent(Map.this, AddPlace.class);
			startActivity(intent);
			overridePendingTransition(R.anim.pull_in_from_top, R.anim.pull_out_to_top);
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		String pathToOurFile = Environment.getExternalStorageDirectory().getPath() + "/foldername/" + date_actuelle.toString();
		//System.out.println("photo prise");
	}

	public void refreshPlaces(){
		LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
		myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if(myLocation != null) {
            longitude = (float) myLocation.getLongitude();
            latitude = (float) myLocation.getLatitude();

            getPlaces(DISTANCE, longitude, latitude);
        }else{
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.gps_disabled))
                    .setMessage(getResources().getString(R.string.turn_on_gps))
                    .setPositiveButton(getResources().getString(R.string.activate), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
	}

	public void getPlaces(int distance, float longi, float latt) {
		AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);

		client.setTimeout(999999999);
		JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler() {

			private CustomListAdapter adapter;

			public void onSuccess(int statusCode,org.apache.http.Header[] headers,org.json.JSONObject response) {
				System.out.println("success"+response);
				FragmentMap fragmentMap = (FragmentMap)pageAdapter.getItem(0);
				FragmentListPlaces fragmentList = (FragmentListPlaces)pageAdapter.getItem(1);
				List<Place> listePlaces = new ArrayList<Place>();
				for(int i = 0; i<response.length(); i++){
					try {
						JSONObject place = response.getJSONObject(i+"");
						
						//map

						fragmentMap.getMap().addMarker(new MarkerOptions()
						.position(new LatLng(place.getDouble("2"), place.getDouble("3")))
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.bird))
						.title(place.getString("1")));
						
						Place myPlace = new Place(place.getString("6"), place.getString("1"), place.getString("4"), place.getString("7"));
						myPlace.setCurrent_point_latitude(place.getDouble("2"));
						myPlace.setCurrent_point_longitude(place.getDouble("3"));

						//liste
						listePlaces.add(myPlace);

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				Collections.sort(listePlaces, new Place.ComparateurPLace());
				adapter = new CustomListAdapter(activity, listePlaces, context);
				fragmentList.getList().setAdapter(adapter);
                fragmentMap.places = listePlaces;
			}

			public void onFailure(int statusCode,org.apache.http.Header[] headers, Throwable throwable,	org.json.JSONObject response) {
				System.out.println("failure json"+response);

			}

			public void onFailure(int statusCode,org.apache.http.Header[] headers,String result, Throwable throwable) {
				System.out.println("failure string"+result);
			}
		};

		System.out.println(Constants.URL_API+"?func=places&distance="+distance+"&lat="+latt+"&lon="+longi);
		client.get(Constants.URL_API+"?func=places&distance="+distance+"&lat="+latt+"&lon="+longi, null, responseHandler);

	}
}
