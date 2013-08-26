package com.bsb.hike.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bsb.hike.HikeConstants;
import com.bsb.hike.R;
import com.bsb.hike.utils.HikeAppStateBaseFragmentActivity;
import com.bsb.hike.utils.Utils;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ShareLocation extends HikeAppStateBaseFragmentActivity {

	private GoogleMap map;
	private SupportMapFragment MapFragment;
	private boolean fullScreenFlag = true;
	private LocationManager locManager;
	private LocationListener locListener;
	private Location myLocation = null;
	private boolean gpsDialogShown = false;
	private Marker userMarker;
	private Marker lastMarker;
	// places of interest
	private Marker[] placeMarkers;
	private String searchStr;
	// max
	private final int MAX_PLACES = 20;// most returned from google
	// marker options
	private MarkerOptions[] places;
	private ArrayList<ItemDetails> list;
	private ListView listview;
	private ItemListBaseAdapter adapter;
	private Dialog alert;
	private int currentLocationDevice;
	private boolean isTextSearch = false;
	private final int GPS_ENABLED = 1;
	private final int GPS_DISABLED = 2;
	private final int NO_LOCATION_DEVICE_ENABLED = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_location);
		gpsDialogShown = savedInstanceState != null
				&& savedInstanceState
						.getBoolean(HikeConstants.Extras.GPS_DIALOG_SHOWN);
		initMyLocationManager();
		listview = (ListView) findViewById(R.id.itemListView);
		list = new ArrayList<ItemDetails>();
		adapter = new ItemListBaseAdapter(this, list);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				lastMarker.setVisible(false);
				Marker currentMarker = adapter.getMarker(position);
				currentMarker.setVisible(true);
				currentMarker.setIcon(BitmapDescriptorFactory
						.fromResource(R.drawable.yellow_point));
				lastMarker = currentMarker;
				map.animateCamera(CameraUpdateFactory.newLatLng(currentMarker
						.getPosition()));
			}
		});
		if (map == null) {
			/*
			 * if isGooglePlayServicesAvailable method returns
			 * 2=ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED this implies
			 * we need to update our playservice library if it returns
			 * 0=ConnectionResult.SUCCESS this implies we have correct version
			 * and working playservice api
			 */
			Log.d(getClass().getSimpleName(),
					"is play service available = "
							+ Integer
									.valueOf(
											GooglePlayServicesUtil
													.isGooglePlayServicesAvailable(this))
									.toString());

			MapFragment = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map);
			map = MapFragment.getMap();

			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			map.getUiSettings().setZoomControlsEnabled(false);
			map.getUiSettings().setCompassEnabled(false);
			map.getUiSettings().setMyLocationButtonEnabled(true);
			map.setTrafficEnabled(false);

			places = new MarkerOptions[MAX_PLACES];
			placeMarkers = new Marker[MAX_PLACES + 1];
			updateMyLocation();

			if (savedInstanceState != null) {
				isTextSearch = savedInstanceState
						.getBoolean(HikeConstants.Extras.IS_TEXT_SEARCH);
				searchStr = savedInstanceState
						.getString(HikeConstants.Extras.HTTP_SEARCH_STR);
				new GetPlaces().execute(searchStr);
			} else {
				Log.d(getClass().getSimpleName(),
						"savedInstanceState is null updating nearby places");
				if (myLocation != null)
					updateNearbyPlaces();
			}

		}

		Button fullScreenButton = (Button) findViewById(R.id.full_screen_button);
		fullScreenButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// View mapView = (View) findViewById(R.id.map);
				if (fullScreenFlag) {
					fullScreenFlag = false;
					((View) findViewById(R.id.frame))
							.setLayoutParams(new TableLayout.LayoutParams(
									LayoutParams.MATCH_PARENT,
									LayoutParams.MATCH_PARENT, 0f));
				} else {
					fullScreenFlag = true;
					((View) findViewById(R.id.frame))
							.setLayoutParams(new TableLayout.LayoutParams(
									LayoutParams.MATCH_PARENT,
									LayoutParams.MATCH_PARENT, 1.5f));
				}
			}
		});

		Button searchButton = (Button) findViewById(R.id.search_button);
		searchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					String searchString = ((EditText) findViewById(R.id.search))
							.getText().toString();
					if (!searchString.equals("")) {
						searchString = URLEncoder.encode(searchString, "UTF-8");
						double lat = myLocation.getLatitude();
						double lng = myLocation.getLongitude();
						searchStr = "https://maps.googleapis.com/maps/api/place/textsearch/"
								+ "json?query="
								+ searchString
								+ "&location="
								+ lat
								+ ","
								+ lng
								+ "radius=2000&sensor=true"
								+ "&key="
								+ getResources().getString(
										R.string.places_api_key);// ADD
																	// KEY

						isTextSearch = true;

						new GetPlaces().execute(searchStr);
					}
				} catch (UnsupportedEncodingException e) {
					Log.w(getClass().getSimpleName(),
							"in nearby search url encoding", e);
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.share_location_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (item.getItemId() == R.id.sendLocation) {
			sendSelectedLocation();
		}

		return true;
	}
	
	public void sendSelectedLocation() {
		if (lastMarker == null) {
			Toast.makeText(getApplicationContext(), R.string.select_location,
					Toast.LENGTH_SHORT).show();
			return;
		}
		Intent result = new Intent();
		result.putExtra(HikeConstants.Extras.ZOOM_LEVEL, HikeConstants.DEFAULT_ZOOM_LEVEL);
		result.putExtra(HikeConstants.Extras.LATITUDE,
				lastMarker.getPosition().latitude);
		result.putExtra(HikeConstants.Extras.LONGITUDE,
				lastMarker.getPosition().longitude);
		setResult(RESULT_OK, result);

		finish();
	}
	
	protected void onSaveInstanceState(Bundle outState) {

		outState.putBoolean(HikeConstants.Extras.IS_TEXT_SEARCH, isTextSearch);
		outState.putString(HikeConstants.Extras.HTTP_SEARCH_STR, searchStr);
		outState.putBoolean(HikeConstants.Extras.GPS_DIALOG_SHOWN,
				gpsDialogShown);
		super.onSaveInstanceState(outState);
	}

	public void onTitleIconClick(View v) {
		if (myLocation == null) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.select_location), Toast.LENGTH_SHORT)
					.show();
			return;
		}
		Intent result = new Intent();
		result.putExtra(HikeConstants.Extras.ZOOM_LEVEL, 15);
		result.putExtra(HikeConstants.Extras.LATITUDE,
				lastMarker.getPosition().latitude);
		result.putExtra(HikeConstants.Extras.LONGITUDE,
				lastMarker.getPosition().longitude);
		setResult(RESULT_OK, result);

		finish();
	}

	private void initMyLocationManager() {
		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		locListener = new LocationListener() {
			public void onLocationChanged(Location newLocation) {
				if (myLocation != null) {
					userMarker.setPosition(new LatLng(
							newLocation.getLatitude(), newLocation
									.getLongitude()));
					Log.d(getClass().getSimpleName(),
							"is Location changed = "
									+ Double.valueOf(
											myLocation.distanceTo(newLocation))
											.toString());
					if ((currentLocationDevice == GPS_ENABLED && myLocation
							.distanceTo(newLocation) > 300)
							|| (currentLocationDevice == GPS_DISABLED && myLocation
									.distanceTo(newLocation) > 1000)) {

						myLocation = newLocation;
						userMarker.setPosition(new LatLng(newLocation
								.getLatitude(), newLocation.getLongitude()));
						updateLocationAddress(myLocation.getLongitude(),
								myLocation.getLatitude(), userMarker);
						// do something on location change
						Log.d(getClass().getSimpleName(),
								"my longi in loc listener = "
										+ Double.valueOf(
												newLocation.getLongitude())
												.toString());
						Log.d(getClass().getSimpleName(),
								"my lati in loc listener = "
										+ Double.valueOf(
												newLocation.getLatitude())
												.toString());
						if (!isTextSearch)
							updateNearbyPlaces();
					}
				} else {
					myLocation = newLocation;
					setMyLocation(newLocation);
					updateNearbyPlaces();
				}
			}

			public void onProviderDisabled(String arg0) {
			}

			public void onProviderEnabled(String arg0) {
			}

			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			}
		};
		try {
			locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
					50, locListener);
			locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					0, 1000, locListener);

		} catch (IllegalStateException e) {
			Log.d(getClass().getSimpleName(), "No listener found");
			showLocationDialog();
		}
	}

	private void updateMyLocation() {
		// get location manager
		showLocationDialog();
		myLocation = null;
		Log.d(getClass().getSimpleName(), "inside updateMyLocation");

		if (currentLocationDevice == GPS_ENABLED) {
			Log.d(getClass().getSimpleName(), "gps provider getting location");
			myLocation = locManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (myLocation == null) {
				Log.d(getClass().getSimpleName(),
						"gps enabled but gps not working properly");
				currentLocationDevice = GPS_DISABLED;
			}
		}
		if (currentLocationDevice == GPS_DISABLED) {
			Log.d(getClass().getSimpleName(), "gps disabled");
			myLocation = locManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (myLocation == null) {
				Log.d(getClass().getSimpleName(),
						"gps disabled but Network provider not working properly");
				currentLocationDevice = NO_LOCATION_DEVICE_ENABLED;
			}
		}
		if (currentLocationDevice == NO_LOCATION_DEVICE_ENABLED) {
			return;
		}

		setMyLocation(myLocation);
	}

	private void setMyLocation(Location loc) {
		double lat = loc.getLatitude();
		double lng = loc.getLongitude();
		// create LatLng
		LatLng myLatLng = new LatLng(lat, lng);

		// remove any existing marker
		if (userMarker != null)
			userMarker.remove();
		// create and set marker properties
		userMarker = map.addMarker(new MarkerOptions()
				.position(myLatLng)
				.title("My Location")
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.yellow_point)));

		lastMarker = userMarker;
		updateLocationAddress(lat, lng, userMarker);

		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(myLatLng) // Sets the center of the map to Mountain View
				.zoom(HikeConstants.DEFAULT_ZOOM_LEVEL) // Sets the zoom
				.build(); // Creates a CameraPosition from the builder
		Log.d(getClass().getSimpleName(), "stting up camera in set my location");
		map.animateCamera(
				CameraUpdateFactory.newCameraPosition(cameraPosition), 3000,
				null);

	}

	private void updateNearbyPlaces() {
		// build places query string
		String types = "shopping_mall|airport|bank|bus_station|gas_station|hospital|museum|police|post_office|school|zoo|restaurant";

		String typesStr;
		try {
			typesStr = URLEncoder.encode(types, "UTF-8");
			if (searchStr == null) {
				searchStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/"
						+ "json?location="
						+ myLocation.getLatitude()
						+ ","
						+ myLocation.getLongitude()
						+ "&types="
						+ typesStr
						+ "&radius=1000&sensor=true"
						+ "&key="
						+ getResources().getString(R.string.places_api_key);
				;
				isTextSearch = false;
			}
			new GetPlaces().execute(searchStr);

		} catch (UnsupportedEncodingException e) {
			Log.w(getClass().getSimpleName(), "in text search url encoding", e);
		}
	}

	private class GetPlaces extends AsyncTask<String, Void, Integer> {

		@Override
		protected Integer doInBackground(String... placesURL) {
			// fetch places
			Log.d(getClass().getSimpleName(),
					"GetPlaces Async Task do in background");
			JSONObject resultObject = null;
			for (String placeSearchURL : placesURL) {
				resultObject = Utils.getJSONfromURL(placeSearchURL);
			}

			JSONArray placesArray = null;
			try {
				placesArray = resultObject.getJSONArray("results");
				// loop through places
				for (int p = 0; p < placesArray.length(); p++) {
					// parse each place
					// if any values are missing we won't show the marker
					boolean missingValue = false;
					LatLng placeLL = null;
					String placeName = "";
					String address = "";
					try {
						// attempt to retrieve place data values
						missingValue = false;
						// get place at this index
						JSONObject placeObject = placesArray.getJSONObject(p);
						// get location section
						JSONObject loc = placeObject.getJSONObject("geometry")
								.getJSONObject("location");
						// read lat lng
						placeLL = new LatLng(Double.valueOf(loc
								.getString("lat")), Double.valueOf(loc
								.getString("lng")));

						Log.d(getClass().getSimpleName(),
								Integer.valueOf(p).toString() + " = "
										+ (String) placeObject.get("name"));

						// vicinity
						if (!isTextSearch)
							address = placeObject.getString("vicinity");
						else
							address = placeObject
									.getString("formatted_address");

						// name
						placeName = placeObject.getString("name");
					} catch (JSONException jse) {
						Log.v(getClass().getSimpleName(),
								"Places missing value");
						missingValue = true;
						jse.printStackTrace();
					}
					// if values missing we don't display
					if (missingValue)
						places[p] = null;
					else {
						places[p] = new MarkerOptions()
								.position(placeLL)
								.title(placeName)
								.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.yellow_point))
								.snippet(address);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return placesArray == null ? 0 : placesArray.length();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			findViewById(R.id.progress_dialog).setVisibility(View.VISIBLE);
		}

		// process data retrieved from doInBackground
		protected void onPostExecute(Integer totalPlaces) {
			for (int pm = 1; pm < placeMarkers.length; pm++) {
				if (placeMarkers[pm] != null) {
					placeMarkers[pm].remove();
				}
			}
			Log.d(getClass().getSimpleName(), "list length before = "
					+ Integer.valueOf(list.size()).toString());
			int listSize = list.size();
			for (int i = listSize - 1; i > 0; i--) {
				Log.d(getClass().getSimpleName(), Integer.valueOf(i).toString()
						+ " = " + list.get(i).getName());
				list.remove(i);
			}
			adapter.notifyDataSetChanged();
			Log.d(getClass().getSimpleName(), "list length after = "
					+ Integer.valueOf(list.size()).toString());
			for (int p = 0; p < totalPlaces; p++) {
				if (places[p] != null) {
					placeMarkers[p] = map.addMarker(places[p]);
					addItemToAdapter(places[p].getTitle(),
							places[p].getSnippet(), placeMarkers[p], false);
					placeMarkers[p].setVisible(false);
					adapter.notifyDataSetChanged();
				}
			}
			findViewById(R.id.progress_dialog).setVisibility(View.GONE);
		}
	}

	private void addItemToAdapter(String str1, String str2, Marker mark,
			boolean isMyLocation) {
		ItemDetails item = new ItemDetails();
		item.setName(str1);
		item.setItemDescription(str2);
		if (isMyLocation) {
			adapter.setMarker(0, mark);
			list.add(0, item);
		} else {
			adapter.setMarker(adapter.getCount(), mark);
			list.add(item);
		}
	}

	private void showLocationDialog() {
		if (alert != null && alert.isShowing()) {
			return;
		}
		boolean hasGps = getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_LOCATION_GPS);

		if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			currentLocationDevice = GPS_ENABLED;
		} else if (locManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			currentLocationDevice = GPS_DISABLED;
		} else {
			currentLocationDevice = NO_LOCATION_DEVICE_ENABLED;
		}

		/*
		 * Don't show anything if the GPS is already enabled or the device does
		 * not have gps and the network is enabled or the the GPS dialog was
		 * shown once.
		 */
		if (currentLocationDevice == GPS_ENABLED) {
			return;
		} else if (currentLocationDevice == GPS_DISABLED
				&& (!hasGps || gpsDialogShown)) {
			return;
		}

		int messageId = currentLocationDevice == GPS_DISABLED ? R.string.gps_disabled
				: R.string.location_disabled;

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder
				.setMessage(messageId)
				.setCancelable(false)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent callGPSSettingIntent = new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivity(callGPSSettingIntent);
							}
						});
		alertDialogBuilder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						gpsDialogShown = currentLocationDevice == GPS_DISABLED;
						dialog.cancel();
					}
				});
		alertDialogBuilder.setCancelable(true);
		alertDialogBuilder.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				gpsDialogShown = currentLocationDevice == GPS_DISABLED;

			}
		});
		alert = alertDialogBuilder.create();
		if (!ShareLocation.this.isFinishing())
			alert.show();

	}

	public class ItemDetails {

		private String name;
		private String itemDescription;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getItemDescription() {
			return itemDescription;
		}

		public void setItemDescription(String itemDescription) {
			this.itemDescription = itemDescription;
		}
	}

	@SuppressLint("UseSparseArrays")
	public class ItemListBaseAdapter extends BaseAdapter {
		private ArrayList<ItemDetails> itemDetailsrrayList;
		HashMap<Integer, Marker> positionToLocationMap = new HashMap<Integer, Marker>();

		private LayoutInflater l_Inflater;

		public ItemListBaseAdapter(Context context,
				ArrayList<ItemDetails> results) {
			itemDetailsrrayList = results;
			l_Inflater = LayoutInflater.from(context);

		}

		public int getCount() {
			return itemDetailsrrayList.size();
		}

		public Object getItem(int position) {
			return itemDetailsrrayList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {
				convertView = l_Inflater.inflate(R.layout.item_details_view,
						null);
				holder = new ViewHolder();
				holder.txt_itemName = (TextView) convertView
						.findViewById(R.id.name);

				holder.txt_itemDescription = (TextView) convertView
						.findViewById(R.id.itemDescription);
				// holder.itemImage = (ImageView)
				// convertView.findViewById(R.id.photo);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.txt_itemName.setText(itemDetailsrrayList.get(position)
					.getName());
			// if it is My Location than set my location image to the left
			if (position == 0) {
				Drawable dr = (Drawable) getResources().getDrawable(
						R.drawable.my_location);
				Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
				// Scale it to required size
				int width = (int) getResources().getDimension(
						R.dimen.share_my_location_drawable_width);
				Drawable scaled_dr = new BitmapDrawable(getResources(),
						Bitmap.createScaledBitmap(bitmap, width, width, true));

				holder.txt_itemName.setCompoundDrawablesWithIntrinsicBounds(
						scaled_dr, null, null, null);
				holder.txt_itemName
						.setCompoundDrawablePadding((int) getResources()
								.getDimension(
										R.dimen.share_my_location_drawable_padding));
			} else
				holder.txt_itemName.setCompoundDrawablesWithIntrinsicBounds(0,
						0, 0, 0);

			holder.txt_itemDescription.setText(itemDetailsrrayList
					.get(position).getItemDescription());
			// holder.itemImage.setImageResource(itemDetailsrrayList.get(position).getImageNumber());

			return convertView;
		}

		public void setMarker(int position, Marker mark) {
			positionToLocationMap.put(position, mark);
			return;
		}

		public Marker getMarker(int position) {
			return positionToLocationMap.get(position);
		}

		class ViewHolder {
			TextView txt_itemName;
			TextView txt_itemDescription;
			ImageView itemImage;
		}

	}

	private void updateLocationAddress(final double lat, final double lng,
			final Marker userMarker) {
		/*
		 * Getting the address blocks the UI so we run this code in a background
		 * thread.
		 */
		(new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				return getAddressFromPosition(lat, lng, ShareLocation.this);
			}

			@Override
			protected void onPostExecute(String address) {
				userMarker.setSnippet(address);
				if (list.size() > 0)
					list.remove(0);
				addItemToAdapter(userMarker.getTitle(), address, userMarker,
						true);
				adapter.notifyDataSetChanged();
			}
		}).execute();
	}

	public static String getAddressFromPosition(double lat, double lng,
			Context context) {
		String address = "";
		try {
			JSONObject resultObj = Utils
					.getJSONfromURL("http://maps.googleapis.com/maps/api/geocode/json?latlng="
							+ lat + "," + lng + "&sensor=true");
			String Status = resultObj.getString("status");
			if (Status.equalsIgnoreCase("OK")) {
				JSONArray Results = resultObj.getJSONArray("results");
				JSONObject zero = Results.getJSONObject(0);
				address = zero.getString("formatted_address");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return address;
	}

	private void removeMyLocationListeners() {
		if (locManager != null) {
			locManager.removeUpdates(locListener);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		removeMyLocationListeners();
	}

}