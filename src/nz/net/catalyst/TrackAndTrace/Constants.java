package nz.net.catalyst.TrackAndTrace;

public class Constants {
    
	public static final int PREFERENCES = 1;
	public static final int SEARCH = 2;
	public static final int INFO = 3;
	public static final int LOGIN = 4;
	public static final int RESET = 5;
	public static final int SCAN = 6;
	
    public static final String USER_AGENT = "TrackAndTrace/1.0";
	
	public static final String CONFIG_SCAN_INTENT = "com.google.zxing.client.android.SCAN";
	public static final String CONFIG_SCAN_MODE = "QR_CODE_MODE";
	public static final String SEARCH_SCAN_MODE = "PRODUCT_MODE";

	public static final int SEARCH_BOXES = 5;

	public static final int RESP_SUCCESS = 1;
	public static final int RESP_NO_ITEMS = 2;
	public static final int RESP_FAILED = 4;
	
	public static final String API_URL  = "http://services.nzpost.co.nz/NZP/NZPostTracking.svc/TrackIDs/1_0/";
	public static final String API_KEY  = "633c9f34-35ae-40ad-8b79-c84e2b62d087";
	public static final String API_MYIP = "10.0.0.1";
		
		// CP700396872NZ,SA433298265NZ

}
