package nz.net.catalyst.TrackAndTrace.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nz.net.catalyst.TrackAndTrace.R;
import nz.net.catalyst.TrackAndTrace.Constants;
import nz.net.catalyst.TrackAndTrace.EditPreferences;
import nz.net.catalyst.TrackAndTrace.log.LogConfig;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class SearchFormActivity extends Activity implements OnClickListener, Handler.Callback {
	static final String TAG = LogConfig.getLogTag(SearchFormActivity.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly
	// for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;
	
	// package scope to allow efficient access by inner classes
	final Handler mHandler = new Handler(this);
	private Thread mSearchThread;

	// Location info
	private LocationManager locationmanager = null;
	private LocationListener listener;

	private int boxes;
	private ViewGroup vg;
	
	private SharedPreferences mPrefs;
	
	private void initiateScan () {
		try {
			Intent intent = new Intent(Constants.CONFIG_SCAN_INTENT);
			intent.putExtra("SCAN_MODE", Constants.SEARCH_SCAN_MODE);
			startActivityForResult(intent, 0);
	    } catch (ActivityNotFoundException e) {
        	Toast.makeText(this, getResources().getString(R.string.scan_not_available), Toast.LENGTH_SHORT).show();
	    }
	}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    	mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.search_form);
        
        // Set up click handlers for the text field and button
        ((Button) this.findViewById(R.id.btnSearchGo)).setOnClickListener(this);

        loadSearchBoxes();
    }
	@Override
	protected void onPause() {
		super.onPause();
		removeListeners();
	}

	@Override
	protected void onStop() {
		super.onStop();
		removeListeners();
	}
    
    private void loadSearchBoxes() {
        boxes = Integer.parseInt(mPrefs.getString(getString(R.string.pref_search_boxes_key), 
				Integer.toString(Constants.SEARCH_BOXES)));

    	LayoutInflater infalInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        vg = (ViewGroup) findViewById(R.id.search_box_container);
        if(vg != null) {
    		vg.removeAllViews();
	        vg.invalidate();
        }

        String saved = mPrefs.getString(getString(R.string.pref_saved_key), "");
        String[] s = saved.split(",");

        // Inflate child view (search item)
        for (int i = 0; i < boxes; i++) {
        	View sb = infalInflater.inflate(R.layout.search_form_box, null);

        	// load any save items
            if ( i < s.length ) {
            	if ( s[i].trim().length() > 0 ) {
            		EditText mText = (EditText) sb.findViewById(R.id.searchTerms);
            		mText.setText(s[i]);
    	        
            		CheckBox chk = (CheckBox) sb.findViewById(R.id.saveTerms);
            		chk.setChecked(true);
            	}
            }
        	vg.addView(sb, i);

        }
        vg.invalidate();
    }
    public void onResume() {
        super.onResume();

		loadSearchBoxes();
    }
    
    public void onClick(View v) {
		if (v.getId() == R.id.btnSearchGo) {			
	        EditText mText;

			ArrayList<String> qValues = new ArrayList<String>();
			
	        String saved = "";

	        for (int i = 0; i < vg.getChildCount(); i++) {
	        	View sb = vg.getChildAt(i);
	        	
		        mText = (EditText) sb.findViewById(R.id.searchTerms);
		        if ( mText.getText().toString().trim().length() > 0 ) {
					qValues.add(mText.getText().toString().trim());
		        	if ( DEBUG ) Log.d(TAG, "adding search item: " + mText.getText().toString().trim());

					CheckBox chk = (CheckBox) sb.findViewById(R.id.saveTerms);
			        if ( chk == null )
			        	break;
					if ( chk.isChecked() ) {
						if ( saved.length() > 0 ) 
							saved = saved + ",";
						saved = saved + mText.getText().toString().trim();
			        	if ( DEBUG ) Log.d(TAG, "adding save item, now = " + saved);
					}
		        }
	        }

			// Save items checked
			mPrefs.edit()
				.putString(getString(R.string.pref_saved_key), saved)
				.commit()
			;
			
			// Start the details dialog and pass in the intent containing item details.
	        
        	if ( qValues.size() <= 0 ) {
    			Toast.makeText(this, getString(R.string.search_no_search_terms), Toast.LENGTH_SHORT).show();
        	} else {
            	
        		// Load up the search results intent
		        Intent d = new Intent(this, SearchResultsActivity.class);
				d.putStringArrayListExtra("x", qValues);
				
				startActivity(d);
        	}
		}
	}

	public boolean onSearchRequested() {
		initiateScan();
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, Constants.SCAN, 1, R.string.menu_scan).setIcon(R.drawable.ic_menu_scan);
		menu.add(Menu.NONE, Constants.PREFERENCES, 2, R.string.menu_preferences).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(Menu.NONE, Constants.POSTCODE, 2, R.string.menu_postcode).setIcon(android.R.drawable.ic_menu_view);
		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
			case Constants.SCAN:
				initiateScan();
				break;				
			case Constants.PREFERENCES:
				startActivity(new Intent(this, EditPreferences.class));
				break;	
			case Constants.POSTCODE:
				addListeners();
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}
	public void onActivityResult(int requestCode, int resultCode, Intent intent) { 
		
        if (resultCode == Activity.RESULT_OK) {
        	String contents = intent.getStringExtra("SCAN_RESULT");
        	String formatName = intent.getStringExtra("SCAN_RESULT_FORMAT");
        	
        	if ( DEBUG ) Log.d(TAG, "scanResult: " + contents + " (" + formatName + ")");
        	
        	EditText mText;
	        for (int i = 0; i < vg.getChildCount(); i++) {
	        	View sb = vg.getChildAt(i);
	        	
		        mText = (EditText) sb.findViewById(R.id.searchTerms);
		        if ( mText.getText().toString().trim().length() == 0 ) {
		        	mText.setText(contents);
		        	return;
		        }
	        }
        	Toast.makeText(this, getResources().getString(R.string.scan_no_free_input_field), Toast.LENGTH_SHORT).show();
		} 
	}
    public static Thread findPostcode(final double mLatitude, final double mLongitude, final Handler handler, final Context context) {
        final Runnable runnable = new Runnable() {
            public void run() {

				Geocoder geocoder = new Geocoder(context);
				try {
	
					if (VERBOSE)
						Log.v(TAG, "Looking up address of (" + mLatitude + ", "
								+ mLongitude + ") using Geocoder");
	
					List<Address> addresses = geocoder.getFromLocation(mLatitude, mLongitude, 1);
	
					// addresses should only contain one address (or none). Send it
					// to the UI thread
					if (addresses.size() > 0) {
	
						Address address = addresses.get(0);
	
						if (VERBOSE)
							Log.v(TAG, "Geocoder found address: " + address);
	
						Message msg = handler.obtainMessage(
									Constants.GEOCODER_LOOKUP_RESULT, address.getPostalCode());
						handler.sendMessage(msg);
					}
	
				} catch (IOException e) {
					Log.i(TAG, "IO error obtaining address from geocoder", e);
				}
            }
        };
        // run on background thread.
        return SearchFormActivity.performOnBackgroundThread(runnable);
    }
    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }
    /**
     * Shows the progress UI for a lengthy operation.
     */
    protected void showProgress() {
        showDialog(0);
    }
    /**
     * Hides the progress UI for a lengthy operation.
     */
    protected void hideProgress() {
    	try {
    		dismissDialog(0);
    	} catch ( IllegalArgumentException e ) {
    		// do nothing .. must have gone by itself.
    	}
    }
	public boolean addListeners() {
		if ( ( locationmanager != null) && (listener != null) ) 
			return true;
		
		String location_context = Context.LOCATION_SERVICE;
		locationmanager = (LocationManager) getSystemService(location_context);
		if (! locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER) && 
			! locationmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ) {
        	Toast.makeText(this, getResources().getString(R.string.no_location), Toast.LENGTH_SHORT).show();
			return false;
		}
		listener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// keep checking the location, until we have what we need
				if ( location.hasAccuracy() ) {
					Message msg = mHandler.obtainMessage(
							Constants.LOCATION_FOUND, location);
					mHandler.sendMessage(msg);
				}
			}

			public void onProviderDisabled(String provider) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}
		};
		locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100000, 100, listener);
		locationmanager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100000, 1000, listener);
		return true;
	}
	public void removeListeners() {
		// Log.e(LOG_TAG, "removeListeners");
		if ((locationmanager != null) && (listener != null)) {
			locationmanager.removeUpdates(listener);
		}
		locationmanager = null;
		// Log.d(LOG_TAG, "Removed " + listener.toString());
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case Constants.LOCATION_FOUND:
			Location mLocation = (Location) msg.obj;
			mSearchThread =	findPostcode(mLocation.getLatitude(), 
					 mLocation.getLongitude(), mHandler, this);
			break;
		case Constants.GEOCODER_LOOKUP_RESULT:
			Toast.makeText(SearchFormActivity.this, 
						getResources().getString(R.string.postcode_found, (CharSequence) msg.obj),
						Toast.LENGTH_LONG).show();
			removeListeners();
			break;
		default:
			// the message hasn't been handled
			return false;
		}

		// we have handled the message
		return true;
	}
}