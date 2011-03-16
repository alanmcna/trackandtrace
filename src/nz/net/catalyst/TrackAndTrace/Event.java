package nz.net.catalyst.TrackAndTrace;

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

public class Event extends Object implements Parcelable {
	private String event_date;
	private String event_time;
	private String flag;
	private String description;
	
	public Event clone() {
		Event e = new Event();
		e.event_date = this.event_date;
		e.event_time = this.event_time;
		e.flag = this.flag;
		e.description = this.description;
		return e;
	}
	
	public String getEventDate() {
		return this.event_date;
	}
	public String getEventTime() {
		return this.event_time;
	}
	public String getFlag() {
		return this.flag;
	}
	public String getDescription() {
		return this.description;
	}
	public void setEventDate(String d) {
		this.event_date = d;
	}
	public void setEventTime(String t) {
		this.event_time = t;
	}
	public void setDescription(String d) {
		this.description= d;
	}
	public void setFlag(String f) {
		this.flag= f;
	}
	public String getGroup() {
		// In the meantime just set the article ID, i.e force no grouping
		return this.event_date + " " + this.event_time;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(this.event_date);
		dest.writeString(this.event_time);
		dest.writeString(this.description);
		dest.writeString(this.flag);
	}
		
	/**
	 * Required for Parcelables
	 */
	public static final Parcelable.Creator<Event> CREATOR
			= new Parcelable.Creator<Event>() {
		public Event createFromParcel(Parcel in) {
			return new Event(in);
		}

		public Event[] newArray(int size) {
			return new Event[size];
		}
	};
	/**
	 * For use by CREATOR
	 * @param in
	 */
	private Event(Parcel in) {
		this.event_date = in.readString();
		this.event_time = in.readString();
		this.description = in.readString();
		this.flag = in.readString();
	}

	public Event() {
		// TODO Auto-generated constructor stub
	}
}


