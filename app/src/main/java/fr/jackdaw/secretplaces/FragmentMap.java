package fr.jackdaw.secretplaces;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import fr.jackdaw.utils.Constants;

public class FragmentMap extends Fragment {

	public MapView mMapView;
	private GoogleMap googleMap;
	private Location myLocation;
	private boolean premierAffichage = true;

    public static final FragmentMap newInstance()
    {
        FragmentMap fragment = new FragmentMap();
        return fragment ;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_map, container, false);

		mMapView = (MapView) v.findViewById(R.id.mapView);
		mMapView.onCreate(savedInstanceState);

		mMapView.onResume();

		try {
			MapsInitializer.initialize(getActivity().getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}
		googleMap = mMapView.getMap();
		centerMapOnMyLocation();

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();

	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();

	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mMapView.onLowMemory();
	}

	public GoogleMap getMap(){
		return googleMap;
	}

	private void centerMapOnMyLocation() {

		googleMap.setMyLocationEnabled(true);
		
		LocationManager locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

		if(premierAffichage ){
			CameraPosition cameraPosition = new CameraPosition.Builder()
			.target(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())).zoom(Constants.MAP_ZOOM).build();
			googleMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
			premierAffichage = false;
		}


	}
}
