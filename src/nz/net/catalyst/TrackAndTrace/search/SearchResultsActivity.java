package nz.net.catalyst.TrackAndTrace.search;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ListIterator;

import nz.net.catalyst.TrackAndTrace.Event;
import nz.net.catalyst.TrackAndTrace.R;
import nz.net.catalyst.TrackAndTrace.Constants;
import nz.net.catalyst.TrackAndTrace.EditPreferences;
import nz.net.catalyst.TrackAndTrace.Result;
import nz.net.catalyst.TrackAndTrace.log.LogConfig;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class SearchResultsActivity extends Activity {
	static final String TAG = LogConfig.getLogTag(SearchResultsActivity.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly
	// for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;
	
	// application preferences
	private SharedPreferences mPrefs;

	ArrayList<Result> items = new ArrayList<Result>();
	private Bundle m_extras;

	private Thread mSearchThread;
    private final Handler mHandler = new Handler();
    
	ExpandableListAdapter adapter = new ExpandableListAdapter(this, new ArrayList<String>(), 
			new ArrayList<ArrayList<Result>>());
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        setContentView(R.layout.search_results);
        ExpandableListView listview = (ExpandableListView) findViewById(R.id.listView);
        
        m_extras = getIntent().getExtras();
        if (m_extras == null) {
			Toast.makeText(this, getString(R.string.search_bad_request), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
        }
        
		ArrayList<String> qValues = m_extras.getStringArrayList("x");
		
    	if ( qValues.size() <= 0 ) {
			Toast.makeText(this, getString(R.string.search_no_search_terms), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
    	}

    	String mURL = mPrefs.getString(getResources().getString(R.string.pref_base_url_key).toString(),
				getResources().getString(R.string.base_url).toString());
    	mURL = mURL + getResources().getString(R.string.api_key).toString() + "/" + getLocalIpAddress() + "/";
		
    	String qStr = "";
		Iterator<String> qItr = qValues.iterator(); 
		while ( qItr.hasNext() ) {
			//  + "CP700396872NZ,SA433298265NZ"
			String q = qItr.next();
			if ( qStr.length() > 0 )
				qStr = qStr + ",";
			qStr = qStr + Uri.encode(q.trim());
		}
		// Finally add the query string
		mURL = mURL + qStr;
		
        showProgress();
        // Start search
        mSearchThread =	runSearch(mURL, listview, mHandler, this);
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
     public static Thread runSearch(final String u, final ExpandableListView elv, final Handler handler, final Context context) {
        final Runnable runnable = new Runnable() {
            public void run() {
                search(u, elv, handler, context);
            }
        };
        // run on background thread.
        return SearchResultsActivity.performOnBackgroundThread(runnable);
    }
	protected static void search(String u, ExpandableListView elv, Handler handler, Context context) {
		ArrayList<Result> results = null;
		try {
			RSSHandler rh = new RSSHandler();
			Log.d(TAG, "URL = " + u);
			results = rh.getItems(context, new URL(u));
		} catch (MalformedURLException e) {
			Log.e(TAG, "Malfomed URL: " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "Connection error: " + e.getMessage());
		}
        sendResult(results, elv, handler, context);
	}
    private static void sendResult(final ArrayList<Result> result, final ExpandableListView listview, final Handler handler, final Context context) {
        if (handler == null || context == null) {
            return;
        }
        handler.post(new Runnable() {
            public void run() {
                ((SearchResultsActivity) context).onSearchResult(result, listview);
            }
        });
    }
    /*
     * {@inheritDoc}
     */
	private void onSearchResult(ArrayList<Result> results, ExpandableListView listview) {
		hideProgress();
        // Initialize the adapter with blank groups and children
        // We will be adding children on a thread, and then update the ListView
		
		if ( results == null ) {
			Toast.makeText(this, getResources().getString(R.string.search_failure), 
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		if ( results.isEmpty() ) {
			Toast.makeText(this, getResources().getString(R.string.search_no_results), 
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		for (Iterator<Result> it = results.iterator(); it.hasNext(); ) { 
				Result a = it.next();
				adapter.addItem(a);
		}

        // Set this blank adapter to the list view
		listview.setAdapter(adapter);
	}
    /*
     * {@inheritDoc}
     */
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		menu.add(0, Constants.SEARCH, 1, R.string.menu_search).setIcon(android.R.drawable.ic_menu_search);
		menu.add(0, Constants.PREFERENCES, 2, R.string.menu_preferences).setIcon(android.R.drawable.ic_menu_preferences);
		return result;
	}
    /*
     * {@inheritDoc}
     */
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case Constants.SEARCH:
				finish();
				break;
			case Constants.PREFERENCES:
				startActivity(new Intent(this, EditPreferences.class));
				break;
			default:
				return super.onOptionsItemSelected(item);			
		}
		return true;
	}
    /*
     * {@inheritDoc}
     */
	public boolean onSearchRequested() {
		finish();
		return true;
	}
    /*
     * {@inheritDoc}
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getText(R.string.search_inprogress));
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Log.i(TAG, "dialog cancel has been invoked");
                if (mSearchThread != null) {
                    mSearchThread.interrupt();
                }
            }
        });
        return dialog;
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
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      setContentView(R.layout.search_results);
      ExpandableListView listview = (ExpandableListView) findViewById(R.id.listView);
      listview.setAdapter(adapter);
    }

	public class ExpandableListAdapter extends BaseExpandableListAdapter {

	    @Override
	    public boolean areAllItemsEnabled()
	    {
	        return true;
	    }

	    private Context context;

	    private ArrayList<String> groups;

	    private ArrayList<ArrayList<Result>> children;

	    public ExpandableListAdapter(Context context, ArrayList<String> groups,
	            ArrayList<ArrayList<Result>> children) {
	        this.context = context;
	        this.groups = groups;
	        this.children = children;
	    }

	    public void addItem(Result rec) {
	        if (!groups.contains(rec.getGroup())) {
	            groups.add(rec.getGroup());
	        }
	        int index = groups.indexOf(rec.getGroup());
	        if (children.size() < index + 1) {
	            children.add(new ArrayList<Result>());
	        }
	        children.get(index).add(rec);
	    }

	    @Override
	    public Object getChild(int groupPosition, int childPosition) {
	        return children.get(groupPosition).get(childPosition);
	    }

	    @Override
	    public long getChildId(int groupPosition, int childPosition) {
	        return childPosition;
	    }
	    
	    // Return a child view. You can load your custom layout here.
	    @Override
	    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
	            View convertView, ViewGroup parent) {
	    	Result rec = (Result) getChild(groupPosition, childPosition);
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    	
	        if (convertView == null) {
	            convertView = infalInflater.inflate(R.layout.search_results_row_child, null);
	        }
	        TextView tv;
	        if ( rec.getShortDescription() != null ) {
		        tv = (TextView) convertView.findViewById(R.id.shortdescription);
		        tv.setText(rec.getShortDescription());
		        tv.setVisibility(View.VISIBLE);
	        }
	        if ( rec.getDetailedDescription() != null ) {
		        tv = (TextView) convertView.findViewById(R.id.detaileddescription);
		        //tv.setText(Html.fromHtml(rec.getDetailedDescription()));
		        tv.setText(rec.getDetailedDescription());
		        tv.setVisibility(View.VISIBLE);
	        }

	        LinearLayout mContainer = (LinearLayout) convertView.findViewById(R.id.eventhistory);
	        if(mContainer != null) {
        		mContainer.removeAllViews();
    	        mContainer.invalidate();
	        }

	        ListIterator<Event> l = rec.getEventHistory().listIterator();
	        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0);
	        
	        while ( l.hasNext() ) {
	        	View eventHistoryView = infalInflater.inflate(R.layout.event_history_row_child, null);
	        	Event event = l.next();
		        tv = (TextView) eventHistoryView.findViewById(R.id.eventdate);
		        tv.setText(event.getEventDate());
		        tv = (TextView) eventHistoryView.findViewById(R.id.eventtime);
		        tv.setText(event.getEventTime());
		        tv = (TextView) eventHistoryView.findViewById(R.id.flag);
		        tv.setText(event.getFlag());
		        tv = (TextView) eventHistoryView.findViewById(R.id.description);
		        tv.setText(event.getDescription());
		        
        		mContainer.addView(eventHistoryView, lp);
                if ( DEBUG ) Log.d(TAG, "showing event history [" + event.getFlag() + "]");

	        }
	        mContainer.invalidate();
	        return convertView;
	    }

	    @Override
	    public int getChildrenCount(int groupPosition) {
	        return children.get(groupPosition).size();
	    }

	    @Override
	    public Object getGroup(int groupPosition) {
	        return groups.get(groupPosition);
	    }

	    @Override
	    public int getGroupCount() {
	        return groups.size();
	    }

	    @Override
	    public long getGroupId(int groupPosition) {
	        return groupPosition;
	    }

	    // Return a group view. You can load your custom layout here.
	    @Override
	    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
	            ViewGroup parent) {
	        String group = (String) getGroup(groupPosition);
	        if (convertView == null) {
	            LayoutInflater infalInflater = (LayoutInflater) context
	                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            convertView = infalInflater.inflate(R.layout.search_results_row, null);
	        }
	        TextView tv = (TextView) convertView.findViewById(R.id.trackingnumber);
	        tv.setText(group);
	        return convertView;
	    }

	    @Override
	    public boolean hasStableIds() {
	        return true;
	    }

	    @Override
	    public boolean isChildSelectable(int arg0, int arg1) {
	        return true;
	    }

	}

	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements() ; ) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements() ; ) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException e) {
			Log.e(TAG, e.toString());
		}
		return null;
	}
}