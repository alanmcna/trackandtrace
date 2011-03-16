package nz.net.catalyst.TrackAndTrace.search;

import java.util.ArrayList;

import nz.net.catalyst.TrackAndTrace.R;
import nz.net.catalyst.TrackAndTrace.Constants;
import nz.net.catalyst.TrackAndTrace.EditPreferences;
import nz.net.catalyst.TrackAndTrace.log.LogConfig;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class SearchFormActivity extends Activity implements OnClickListener  {
	static final String TAG = LogConfig.getLogTag(SearchFormActivity.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly
	// for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;
	
	SharedPreferences mPrefs;
	
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
    
        // Load saved stuff
        String saved = mPrefs.getString(getString(R.string.pref_saved_key), "");
        String[] s = saved.split(",");
        for (int i = 0; i < s.length; i++) {
        	if ( s[i].trim().length() == 0 )
        		continue;
        	int id = getResources().getIdentifier("searchTerms" + ( i + 1 ), "id", getPackageName());
        	if ( DEBUG ) Log.d(TAG, "loading saved [" + "searchTerms" + ( i + 1 ) + "] - " + id);
	        EditText mText = (EditText) this.findViewById(id);
			CheckBox chk = (CheckBox) findViewById(R.id.saveTerms1);
			mText.setText(s[i]);
			chk.setChecked(true);
        }
    }
    
    public void onClick(View v) {
		if (v.getId() == R.id.btnSearchGo) {			
	        EditText mText;

			ArrayList<String> qValues = new ArrayList<String>();
			
	        String saved = "";

	        for (int i = 0; i < Constants.SEARCH_BOXES; i++) {
	        	int id = getResources().getIdentifier("searchTerms" + ( i + 1 ), "id", getPackageName());
	        	if ( DEBUG ) Log.d(TAG, "getting tracking codes for search [" + "searchTerms" + ( i + 1 ) + "] - " + id);

		        mText = (EditText) this.findViewById(id);
		        if ( mText.getText().toString().trim().length() > 0 ) {
					qValues.add(mText.getText().toString().trim());
		        	if ( DEBUG ) Log.d(TAG, "adding search item: " + mText.getText().toString().trim());

		        	id = getResources().getIdentifier("saveTerms" + ( i + 1 ), "id", getPackageName());
					CheckBox chk = (CheckBox) findViewById(id);
			        if ( chk == null )
			        	break;
					if ( chk.isChecked() ) {
						if ( saved.length() > 0 ) 
							saved = saved + ",";
						saved = mText.getText().toString().trim();
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
	        for (int i = 0; i < Constants.SEARCH_BOXES; i++) {
	        	int id = getResources().getIdentifier("searchTerms" + ( i + 1 ), "id", getPackageName());
		        mText = (EditText) this.findViewById(id);
		        if ( mText.getText().toString().trim().length() == 0 ) {
		        	mText.setText(contents);
		        	return;
		        }
	        }
        	Toast.makeText(this, getResources().getString(R.string.scan_no_free_input_field), Toast.LENGTH_SHORT).show();
		} 
	}
}