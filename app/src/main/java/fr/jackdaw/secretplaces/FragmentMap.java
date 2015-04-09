package fr.jackdaw.secretplaces;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.List;

import fr.jackdaw.utils.Constants;
import fr.jackdaw.modele.Place;

public class FragmentMap extends Fragment {

    public MapView mMapView;
    private GoogleMap googleMap;
    private Location myLocation;
    public List<Place> places;
    private boolean premierAffichage = true;
    private ImageLoader imageLoader;
    private DisplayImageOptions defaultOptions;
    private ImageLoaderConfiguration config;

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

        imageLoader = ImageLoader.getInstance();
        defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        config = new ImageLoaderConfiguration.Builder(
                getActivity())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();

        imageLoader.init(config);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        googleMap = mMapView.getMap();
        centerMapOnMyLocation();

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.popup_details_place);
                dialog.setTitle(marker.getTitle());

                Button closeButton = (Button) dialog.findViewById(R.id.popup_btn_close);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                ImageButton showButton = (ImageButton) dialog.findViewById(R.id.popup_btn_show_map);
                showButton.setVisibility(View.INVISIBLE);
                Place place = null;
                for (int i=0; i< places.size(); i++){
                    Place currentplace = places.get(i);
                    if (currentplace.getCurrent_point_latitude() == marker.getPosition().latitude
                            && currentplace.getCurrent_point_longitude() == marker.getPosition().longitude){
                        place = currentplace;
                        break;
                    }
                }

                if (place != null) {
                    TextView distance = (TextView) dialog.findViewById(R.id.popup_txt_distance);
                    distance.setText(String.format("%s %s", place.getDistance(), getResources().getString(R.string.meter)));
                    TextView description = (TextView) dialog.findViewById(R.id.popup_txt_description);
                    if(place.getDescription()==null){
                        description.setText(getResources().getString(R.string.placeHistoryEmpty));

                    }else{
                        description.setText(getResources().getString(R.string.placeHistory)+place.getDescription());
                    }
                    ImageView image = (ImageView) dialog.findViewById(R.id.popup_picture);
                    setPictureCard(place.getUrlPicture(), image);
                }

                dialog.show();
                return true;
            }
        });
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

        if(myLocation != null) {
            if(premierAffichage ){
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())).zoom(Constants.MAP_ZOOM).build();
                googleMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
                premierAffichage = false;
            }
        }else{
            new AlertDialog.Builder(getActivity())
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

    public void setPictureCard(String url, ImageView view){

        //download and display image from url
        view.setTag(url);
        imageLoader.displayImage(url, view, defaultOptions);
    }
}
