package com.javasbar.smart.framework.lib.common;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Basavaraj M
 *
 */
public class NetworkUtil 
{
	
	/**
	 * Pings a HTTP URL. This effectively sends a HEAD/GET request and returns <code>true</code> if the response code is in 
	 * the 200-399 range.
	 * @param url The HTTP URL to be pinged.
	 * @param timeout The timeout in millis for both the connection timeout and the response read timeout. Note that
	 * the total timeout is effectively two times the given timeout.
	 * @return <code>true</code> if the given HTTP URL has returned response code 200-399 on a HEAD request within the
	 * given timeout, otherwise <code>false</code>.
	 */
	public static boolean ping(String url, int timeout) 
	{
		if ( null == url )
		{
			return false;
		}
	    url = url.replaceFirst("https", "http"); // Otherwise an exception may be thrown on invalid SSL certificates.
	    try 
	    {
	        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
	        connection.setConnectTimeout(timeout);
	        connection.setReadTimeout(timeout);
	        connection.setRequestMethod("GET");
	        int responseCode = connection.getResponseCode();
	        System.out.println("pinging " + url + " Resulted: " + responseCode);
	        return (200 <= responseCode && responseCode <= 399);
	    } catch (IOException exception) 
	    {
	        return false;
	    }
	}
	
	public static void main(String[] args) {
		System.out.println(ping("http://us.qa.local.yahoo.com", 800));;
	}

}
