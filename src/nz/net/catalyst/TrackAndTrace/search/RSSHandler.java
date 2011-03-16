package nz.net.catalyst.TrackAndTrace.search;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import nz.net.catalyst.TrackAndTrace.Constants;
import nz.net.catalyst.TrackAndTrace.Event;
import nz.net.catalyst.TrackAndTrace.Result;
import nz.net.catalyst.TrackAndTrace.log.LogConfig;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class RSSHandler extends DefaultHandler {
	static final String TAG = LogConfig.getLogTag(RSSHandler.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly
	// for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;

	// Used to define what elements we are currently in
	private boolean inResult = false;
	private boolean inEventHistory = false;
	private boolean inTrackingNumber = false;
	private boolean inShortDescription = false;
	private boolean inLongDescription = false;
	private boolean inEventDate = false;
	private boolean inEventTime = false;
	private boolean inFlag = false;
	private boolean inDescription = false;

	// Feed and Record objects to use for temporary storage
	private Result currentRecord = new Result();
	private Event currentEvent = new Event();

	// Number of Records added so far
    ArrayList<Result> Records = new ArrayList<Result>();

	// Number of Records to download
	private static final int RECORDS_LIMIT = 500;

	// The possible values for targetFlag
	// A flag to know if looking for Records or Feed name
	//private int targetFlag = 5;
	
	public void unparsedEntityDecl (String name, String publicId, String systemId, String notationName) {
		Log.d(TAG, "unparsedEntityDecl: " + name);
	}	
	public void skippedEntity (String name) {
		Log.d(TAG, "skippedEntity: " + name);		
	}
	public void startElement(String uri, String name, String qName,
			Attributes atts) {
		if (name.trim().equals("result"))
			inResult = true;
		else if (name.trim().equals("eventhistory"))
			inEventHistory = true;
		else if (name.trim().equals("trackingnumber"))
			inTrackingNumber = true;
		else if (name.trim().equals("shortdescription"))
			inShortDescription = true;
		else if (name.trim().equals("longdescription"))
			inLongDescription = true;
		else if (name.trim().equals("eventdate"))
			inEventDate = true;
		else if (name.trim().equals("eventtime"))
			inEventTime = true;
		else if (name.trim().equals("flag"))
			inFlag = true;
		else if (name.trim().equals("description"))
			inDescription = true;
	}

	public void endElement(String uri, String name, String qName)
			throws SAXException {

		// Wrap up the event history and move on
		if (name.trim().equals("eventhistory")) {
			inEventHistory = false;
			
			if ( currentEvent.getFlag() != null && currentEvent.getDescription() != null ) {
				currentRecord.addEventHistory(currentEvent);
				Log.d(TAG, "Adding event history [" + currentEvent.getFlag() + "]:" + currentEvent.getDescription());
				currentEvent = new Event();
			}
			return;
		} 
		
		if (name.trim().equals("result")) {
			inResult = false;
			
			//Result a = new Result();
			//a = currentRecord.clone();
			Records.add(currentRecord);
			
			//Log.d(TAG, "Adding: " + a.getTrackingNumber() + " with " + a.getEventHistory().size() + " event history items");
			Log.d(TAG, "Adding: " + currentRecord.getTrackingNumber() + " with " + currentRecord.getEventHistory().size() + " event history items");

			// Lets check if we've hit our limit on number of Records
			if (Records.size() >= RECORDS_LIMIT)
				throw new SAXException();

			currentRecord = new Result();
		} 
		else if (name.trim().equals("trackingnumber"))
			inTrackingNumber = false;
		else if (name.trim().equals("shortdescription"))
			inShortDescription = false;
		else if (name.trim().equals("longdescription"))
			inLongDescription = false;
		else if (name.trim().equals("eventdate"))
			inEventDate = false;
		else if (name.trim().equals("eventtime"))
			inEventTime = false;
		else if (name.trim().equals("flag"))
			inFlag = false;
		else if (name.trim().equals("description"))
			inDescription = false;

	}

	public void characters(char ch[], int start, int length) throws SAXException {
		String chars = new String(ch, start, length);

		try {
			// If not in item, then title/link refers to feed
			if (inResult) {
				if (inTrackingNumber) {
					if ( currentRecord.getTrackingNumber() == null ) 
						currentRecord.setTrackingNumber(chars.trim());
					else 
						currentRecord.setTrackingNumber((currentRecord.getTrackingNumber() + " " + chars.trim()).trim());
				}
				else if (inShortDescription) {
					if ( currentRecord.getShortDescription() == null )
						currentRecord.setShortDescription(chars.trim());
					else
						currentRecord.setShortDescription((currentRecord.getShortDescription() + " " + chars.trim()).trim());
				}
				else if (inLongDescription) {
					if ( currentRecord.getDetailedDescription() == null )
						currentRecord.setDetailedDescription(chars.trim());
					else
						currentRecord.setDetailedDescription((currentRecord.getDetailedDescription() + " " + chars.trim()).trim());
				}
				// Now the event stuff
				else if (inEventHistory) {
					if (inEventDate) {
						if ( currentEvent.getEventDate() == null )
							currentEvent.setEventDate(chars.trim());
						else
							currentEvent.setEventDate((currentEvent.getEventDate() + " " + chars.trim()).trim());
					}
					else if (inEventTime) {
						if ( currentEvent.getEventTime() == null )
							currentEvent.setEventTime(chars.trim());
						else
							currentEvent.setEventTime((currentEvent.getEventTime() + " " + chars.trim()).trim());
					}
					else if (inFlag) {
						if ( currentEvent.getFlag() == null )
							currentEvent.setFlag(chars.trim());
						else
							currentEvent.setFlag((currentEvent.getFlag() + " " + chars.trim()).trim());
					}
					else if (inDescription) {
						if ( currentEvent.getDescription() == null )
							currentEvent.setDescription(chars.trim());
						else
							currentEvent.setDescription((currentEvent.getDescription() + " " + chars.trim()).trim());
					}
				}
				//else if ( chars.trim().length() > 0 )
				//	Log.d(TAG, "Unwanted chars: " + chars.trim());
				
			}
		} catch (StringIndexOutOfBoundsException e) {
			Log.e(TAG, "characters" + e.toString());
		}
	}

	public ArrayList<Result> getItems(Context ctx, URL url) throws IOException {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(this);
			
		    URLConnection conn = url.openConnection();
		    InputSource is;
		    		
		    is = new InputSource(conn.getInputStream());
			xr.parse(is);
			return Records;
			
		} catch (IOException e) {
			Log.e(TAG, "getItems: IOException: " + e.toString());
			throw new IOException("Connection failed.");
		} catch (SAXException e) {
			Log.e(TAG, "getItems: SAXException: " + e.toString());
		} catch (ParserConfigurationException e) {
			Log.e(TAG, "getItems: ParserConfigurationException: " + e.toString());
		}
		return Records;
	}
}
