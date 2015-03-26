package fr.jackdaw.adapters;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import fr.jackdaw.secretplaces.FragmentListPlaces;
import fr.jackdaw.secretplaces.FragmentMap;

public class MainPageAdapter extends FragmentPagerAdapter {
	
	private List<Fragment> fragments;

	public MainPageAdapter(android.support.v4.app.FragmentManager fragmentManager, Activity activity) {
		super(fragmentManager);
		this.fragments = new ArrayList<Fragment>();
		fragments.add(FragmentMap.newInstance());
		fragments.add(FragmentListPlaces.newInstance());
	}

	@Override
	public Fragment getItem(int position) {
		return fragments.get(position);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}

}
