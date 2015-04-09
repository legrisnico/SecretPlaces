package fr.jackdaw.adapters;

import java.util.List;

import fr.jackdaw.modele.Place;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import fr.jackdaw.secretplaces.Map;
import fr.jackdaw.secretplaces.R;
import fr.jackdaw.secretplaces.R.layout;

public class CustomListAdapter extends BaseAdapter{

	private Context mContext;
	private List<Place> placesList;
	private Activity mActivity;
	private DisplayImageOptions defaultOptions;
	private ImageLoaderConfiguration config;
	private ImageLoader imageLoader;

	public CustomListAdapter(Context context, List<Place> places, Activity activity){
		mContext = context;
		placesList = places;
		mActivity = activity;
		
		imageLoader = ImageLoader.getInstance();
		
		defaultOptions = new DisplayImageOptions.Builder()
		.cacheOnDisc(true).cacheInMemory(true)
		.imageScaleType(ImageScaleType.EXACTLY)
		.displayer(new FadeInBitmapDisplayer(300)).build();

		config = new ImageLoaderConfiguration.Builder(
				mContext)
		.defaultDisplayImageOptions(defaultOptions)
		.memoryCache(new WeakMemoryCache())
		.discCacheSize(100 * 1024 * 1024).build();

		imageLoader.init(config);
	}


	private class MyPlaceHolder {
		RelativeLayout layout;
		ImageView imagePlace;
		TextView  titleText, distance;
	}

	@Override
	public int getCount() {
		return placesList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return placesList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}


	@SuppressLint({ "DefaultLocale", "NewApi" })
	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		final Place entry = placesList.get(pos);
		final MyPlaceHolder myPlaceHolder;
		final View mView = convertView;

		if(convertView == null){
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(layout.item_list, null);

			myPlaceHolder = new MyPlaceHolder();

			myPlaceHolder.imagePlace = (ImageView) convertView.findViewById(R.id.item_list_picture);
			myPlaceHolder.titleText = (TextView) convertView.findViewById(R.id.item_list_title);
			myPlaceHolder.distance = (TextView) convertView.findViewById(R.id.item_list_distance);
			myPlaceHolder.layout = (RelativeLayout) convertView.findViewById(R.id.item_list_layout);

			convertView.setTag(myPlaceHolder);
		}else{
			myPlaceHolder = (MyPlaceHolder) convertView.getTag();
			myPlaceHolder.imagePlace.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cadre_photo));
		}
		if (entry.getName().contains("Illuminations")){
			myPlaceHolder.titleText.setTextColor(mActivity.getResources().getColor(R.color.red));
		}else{
            myPlaceHolder.titleText.setTextColor(mActivity.getResources().getColor(R.color.black));
        }
		myPlaceHolder.titleText.setText(entry.getName());
		myPlaceHolder.distance.setText(String.format("%s %s", entry.getDistance(), mActivity.getResources().getString(R.string.meter)));
		
		setPictureCard(entry.getUrlPicture(), myPlaceHolder.imagePlace);
		myPlaceHolder.layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(mContext);
				dialog.setContentView(R.layout.popup_details_place);
				dialog.setTitle(entry.getName());
	 
				// set the custom dialog components - text, image and button
				TextView distance = (TextView) dialog.findViewById(R.id.popup_txt_distance);
				distance.setText(String.format("%s %s", entry.getDistance(), mActivity.getResources().getString(R.string.meter)));
				TextView description = (TextView) dialog.findViewById(R.id.popup_txt_description);
				if(entry.getDescription()==null){
					description.setText(mActivity.getResources().getString(R.string.placeHistoryEmpty));

				}else{
					description.setText(mActivity.getResources().getString(R.string.placeHistory)+entry.getDescription());
				}
				ImageView image = (ImageView) dialog.findViewById(R.id.popup_picture);
				setPictureCard(entry.getUrlPicture(), image);
	 
				Button closeButton = (Button) dialog.findViewById(R.id.popup_btn_close);
				closeButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
	 
				ImageButton showButton = (ImageButton) dialog.findViewById(R.id.popup_btn_show_map);
				showButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Map.current_point_latitude = entry.getCurrent_point_latitude();
						Map.current_point_longitude = entry.getCurrent_point_longitude();
						
						Map.goToTheMap = true;
                        ((Map)mActivity).callOnResume();
						dialog.dismiss();
					
					}
				});
				dialog.show();
			}
		});


		return convertView;
	}

	public void setPictureCard(String url, ImageView view){
		
		//download and display image from url
		view.setTag(url);
		imageLoader.displayImage(url, view, defaultOptions);
	}
}

