package modele;

import java.util.Comparator;

public class Place {
	private String urlPicture, name, distance, description;
	double current_point_longitude, current_point_latitude;

	public Place(String urlPicture, String name, String distance, String description) {
		super();
		this.urlPicture = urlPicture;
		this.name = name;
		this.distance = distance;
		this.description = description;
	}

	public String getUrlPicture() {
		return urlPicture;
	}

	public void setUrlPicture(String urlPicture) {
		this.urlPicture = urlPicture;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}
	
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public double getCurrent_point_longitude() {
		return current_point_longitude;
	}

	public void setCurrent_point_longitude(double current_point_longitude) {
		this.current_point_longitude = current_point_longitude;
	}

	public double getCurrent_point_latitude() {
		return current_point_latitude;
	}

	public void setCurrent_point_latitude(double current_point_latitude) {
		this.current_point_latitude = current_point_latitude;
	}


	public static class ComparateurPLace implements Comparator<Place> {
		public int compare(Place s1, Place s2){
	                //tri desc
			if (Double.valueOf(s1.getDistance()).compareTo(Double.valueOf(s2. getDistance())) == 1) {
				return 1;
			} else if (Double.valueOf(s1.getDistance()).compareTo(Double.valueOf(s2. getDistance())) == -1) {
				return -1;        	
			} else {
				return 0;
			}
		}      
	}
	
}
