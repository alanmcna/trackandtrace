package nz.net.catalyst.TrackAndTrace;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.ListIterator;

import nz.net.catalyst.TrackAndTrace.Event;
import android.os.Parcel;
import android.os.Parcelable;

/*
 * 
 * <trackingresults xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
 *   <results>
 *     <result>
 *       <trackingnumber>CP700396872NZ</trackingnumber>
 *       <shortdescription>Delivered</shortdescription>
 *       <detaileddescription>Your item has been successfully delivered and was signed for by S SHAI.</detaileddescription>
 *       <eventhistory>
 *         <eventhistory>
 *           <eventdate>14/10/10</eventdate>
 *           <eventtime>04.11 PM</eventtime>
 *           <flag>C</flag>
 *           <description>Accepted at Mail Centre</description>
 *         </eventhistory>
 *         <eventhistory>
 *           <eventdate>18/10/10</eventdate>
 *           <eventtime>03.37 PM</eventtime>
 *           <flag>F</flag>
 *           <description>Delivery Complete</description>
 *         </eventhistory>
 *       </eventhistory>
 *     </result>
 *     <result>
 *       <trackingnumber>SA433298265NZ</trackingnumber>
 *       <shortdescription>Error - We don't appear to have a record of that item number (SA433298265NZ). 
 *                       However the item details may not have been received into our tracking system yet. 
 *                       Please try again later or call 0800 501501 should you require further assistance.</shortdescription>
 *       <detaileddescription>&lt;P&gt;Sorry, but your tracking number is not valid. Please ensure your tracking numbers are:&lt;/P&gt;&lt;UL&gt;&lt;LI&gt;Accurately typed&lt;/LI&gt;&lt;LI&gt;In the format XX123456789XX&lt;/LI&gt;&lt;UL&gt;&lt;P&gt;If you have any questions please check out our &lt;A href="http://www.nzpost.co.nz/faq"&gt;frequently asked questions.&lt;/A&gt;&lt;/P&gt;</detaileddescription>
 *       <eventhistory/>
 *       </result>
 *     </results>
 *   </trackingresults>
 * 
 */

public class Result extends Object implements Parcelable {
	private String trackingnumber;
	private String shortdescription;
	private String detaileddescription;
	private ArrayList<Event> eventhistory = new ArrayList<Event>();
	
	public Result clone() {
		Result r = new Result();
		r.trackingnumber = this.trackingnumber;
		r.shortdescription = this.shortdescription;
		r.detaileddescription = this.detaileddescription;
		r.eventhistory = this.eventhistory;
		return r;
	}
	
	public String getTrackingNumber() {
		return this.trackingnumber;
	}
	public String getShortDescription() {
		return this.shortdescription;
	}
	public String getDetailedDescription() {
		return this.detaileddescription;
	}
	public ArrayList<Event> getEventHistory() {
		return this.eventhistory;
	}
	public void setTrackingNumber(String t) {
		this.trackingnumber = t;
	}
	public void setShortDescription(String d) {
		this.shortdescription= d;
	}
	public void setDetailedDescription(String d) {
		this.detaileddescription= d;
	}
	public void setEventHistory(ArrayList<Event> e) {
		this.eventhistory = e;
	}
	public void addEventHistory(Event e) {
		this.eventhistory.add(e);
	}
	public String getGroup() {
		// In the meantime just set the article ID, i.e force no grouping
		return this.trackingnumber;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(this.trackingnumber);
		dest.writeString(this.shortdescription);
		dest.writeString(this.detaileddescription);
        dest.writeTypedList(this.eventhistory);
	}
		
	/**
	 * Required for Parcelables
	 */
	public static final Parcelable.Creator<Result> CREATOR
			= new Parcelable.Creator<Result>() {
		public Result createFromParcel(Parcel in) {
			return new Result(in);
		}

		public Result[] newArray(int size) {
			return new Result[size];
		}
	};
	/**
	 * For use by CREATOR
	 * @param in
	 */
	private Result(Parcel in) {
		this.trackingnumber = in.readString();
		this.shortdescription = in.readString();
		this.detaileddescription = in.readString();
		in.readTypedList(eventhistory, Event.CREATOR);
	}

	public Result() {
		// TODO Auto-generated constructor stub
	}
}

