package nz.net.catalyst.TrackAndTrace;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.google.zxing.integration.android.IntentIntegrator;

import nz.net.catalyst.TrackAndTrace.R;
import nz.net.catalyst.TrackAndTrace.log.LogConfig;
import nz.net.catalyst.TrackAndTrace.search.SearchFormActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class EditPreferences extends PreferenceActivity implements OnClickListener {
	static final String TAG = LogConfig.getLogTag(EditPreferences.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		
		menu.add(Menu.NONE, Constants.RESET, 1, R.string.menu_reset).setIcon(android.R.drawable.ic_menu_revert);
		menu.add(Menu.NONE, Constants.SCAN, 2, R.string.menu_scan).setIcon(R.drawable.ic_menu_scan);

		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
			case Constants.RESET:
				resetToDefaults();
				return true;
			case Constants.SCAN:
				startScan();
		    	return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	private void startScan() {
		//mMessage.setText(getString(R.string.qr_waiting));
		if (VERBOSE)
			Log.v(TAG, "Initiate scanning ...");
		AlertDialog dialog = IntentIntegrator.initiateScan(this);
		if (dialog == null) {
			if (VERBOSE)
				Log.v(TAG, "User already has Barcode Scanner installed.");
				//zxingInstalled = true;
		} else {
			if (VERBOSE)
				Log.v(TAG, "User has not installed Barcode Scanner, displaying dialog...");
			dialog.setCancelable(false);
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, null, this );
			dialog.show();
		}
	}
	
	public boolean onSearchRequested() {
		startActivity(new Intent(this, SearchFormActivity.class));
		finish();
		return true;
	}
	
	
	private void resetToDefaults() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		// clear the preferences
		prefs.edit().clear().commit();
		// reset defaults
		PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
		
		// refresh displayed values by restarting activity (a hack, but apparently there
		// isn't a nicer way)
		finish();
		startActivity(getIntent());
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) { 
		
        if (resultCode == Activity.RESULT_OK) {
        	String contents = intent.getStringExtra("SCAN_RESULT");
        	String formatName = intent.getStringExtra("SCAN_RESULT_FORMAT");
        	
        	if ( DEBUG ) Log.d(TAG, "scanResult: " + contents + " (" + formatName + ")");
        	
        	ConfigXMLHandler cx = null;
        	
        	if ( contents.toLowerCase().startsWith("http://") ) {
        		try {
					cx = new ConfigXMLHandler(this, new URL(contents).openStream());
				} catch (MalformedURLException e) {
					Toast.makeText(this, getResources().getString(R.string.load_config_download_error), Toast.LENGTH_SHORT).show();		
					e.printStackTrace();
				} catch (IOException e) {
					Toast.makeText(this, getResources().getString(R.string.load_config_download_error), Toast.LENGTH_SHORT).show();		
					e.printStackTrace();
				}
        	} else { 
    			cx = new ConfigXMLHandler(this, new ByteArrayInputStream( contents.getBytes() ) );
        	}
        	
			if ( cx.parseConfig() ) {
	        	Toast.makeText(this, getResources().getString(R.string.load_config_success), Toast.LENGTH_SHORT).show();
	    		finish();
	    		startActivity(getIntent());
			} else {
				Toast.makeText(this, getResources().getString(R.string.load_config_error), Toast.LENGTH_SHORT).show();		
			}
		} 
	}
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if ( which == DialogInterface.BUTTON_NEGATIVE )
			finish();
	}
	
	public static class ConfigXMLHandler extends DefaultHandler {

		// Number of config items to process
		private static final int CONFIG_LIMIT = 50;
		
		SharedPreferences mPrefs;
		String curKey = "";
		String curValue = "";
		int count = 0;
		
		SAXParserFactory spf;
		SAXParser sp;
		XMLReader xr;
		Context ctx;
		InputSource is;

		URL url; 
		
		public ConfigXMLHandler(Context context, InputStream ins) {
			ctx = context;
			mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
			
			is = new InputSource(ins);
			try {
				spf = SAXParserFactory.newInstance();
				sp = spf.newSAXParser();
				xr = sp.getXMLReader();
				xr.setContentHandler(this);
			} catch (SAXException e) {
				Log.e(TAG, "ConfigXMLHandler: SAXException: " + e.toString());
			} catch (ParserConfigurationException e) {
				Log.e(TAG, "ConfigXMLHandler: ParserConfigurationException: " + e.toString());
			}
		}
		public Boolean parseConfig() {
			try {
				xr.parse(is);
				return true;
			} catch (IOException e) {
				return false;
			} catch (SAXException e) {
				return false;
			}		
		}

		public void startElement(String uri, String name, String qName,
				Attributes atts) throws SAXException {
			if( mPrefs.contains(name.trim()) ) 
				curKey = name.trim();
			//else
			//	Log.d(TAG, "startElement ignoring " + name.trim());
			count++;
			
			// Lets check if we've hit our limit on number of Records
			if (count > CONFIG_LIMIT)
				throw new SAXException();
		}

		public void endElement(String uri, String name, String qName) {

			if ( name.trim().equals(curKey) ) {
				if (curValue.length() > 0) {
					if ( DEBUG ) Log.d(TAG, curKey + ": " + curValue);
					mPrefs.edit()
						.putString(curKey, curValue)
						.commit()
					;
				}
			}
			else
				if ( DEBUG ) Log.d(TAG, "endElement ignoring " + name.trim());

			curKey = curValue = "";
		}

		public void characters(char ch[], int start, int length) throws SAXException {
			String chars = new String(ch, start, length);
			if (curKey != "") 
				curValue = curValue + chars.trim();
		}
	}
}
