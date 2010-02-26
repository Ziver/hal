package zutil.network.ssdp;

import java.util.Date;
import java.util.UUID;

/**
 * This class contains information about a service from
 * or through the SSDP protocol
 * 
 * @author Ziver
 */
public class SSDPServiceInfo {
	private String location;
	private String st;
	private String usn;
	private long expiration_time;
	
	/**
	 * @param l is the value to set the Location variable
	 */
	public void setLocation(String l) {
		location = l;
	}
	
	/**
	 * @param st is the value to set the SearchTarget variable
	 */
	public void setST(String st) {
		this.st = st;
	}	

	/**
	 * @param usn is the value to set the USN variable
	 */
	protected void setUSN(String usn) {
		this.usn = usn;
	}
	
	/**
	 * @param time sets the expiration time of values in this object
	 */
	protected void setExpirationTime(long time) {
		expiration_time = time;
	}
	
	/**
	 * @return The URL to the Service, e.g. "http://192.168.0.1:80/index.html"
	 */
	public String getLocation(){
		return location;
	}
	
	/**
	 * @return the Search Target, e.g. "upnp:rootdevice"
	 */
	public String getSearchTarget(){
		return st;
	}
	
	/**
	 * @return the expiration time for the values in this object
	 */
	public long getExpirationTime(){
		return expiration_time;
	}
	
	/**
	 * @return the USN value, e.g. "uuid:abcdefgh-7dec-11d0-a765-00a0c91e6bf6 "
	 */
	public String getUSN(){
		if( usn==null )
			usn = genUSN();
		return usn;
	}
	
	/**
	 * Generates an unique USN for the service
	 * 
	 * @param searchTarget is the service ST name
	 * @return an unique string that corresponds to the service
	 */
	private String genUSN(){
		return "uuid:" + UUID.nameUUIDFromBytes( (st+location).getBytes() ) +"::"+st;
	}
	
	public String toString(){
		return "USN: "+usn+"\nLocation: "+location+"\nST: "+st+"\nExpiration-Time: "+new Date(expiration_time);
	}
}