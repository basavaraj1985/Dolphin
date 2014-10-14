package com.javasbar.smart.framework.lib.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

/**
 * @author Basavaraj M
 *
 */
public class Util 
{
	
	/**
	 * returns date and time now with default format
	 * @return
	 */
	public static String now() 
	{
		return now("yyyy/MM/dd HH:mm:ss");
	}
	
	/**
	 * Returns date and time now with the format specified
	 * @param format - example : yyyy-MM-dd HH-mm-ss
	 * @return
	 */
	public static String now(String format) 
	{
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(format);
	    return sdf.format(cal.getTime());
	}
	
	public static int RandomInt() {
		int randomInt = 1;
		Random randomGenerator;
		try {
			randomGenerator = new Random();
			randomInt = randomGenerator.nextInt(6);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return randomInt + 1;
	}

	/**
	 * Returns non zero random integer upto 'x' 
	 * @param x
	 * @return
	 */
	public static int RandomInt(int x) {
		int randomInt = 1;
		Random randomGenerator;
		try {
			randomGenerator = new Random();
			randomInt = randomGenerator.nextInt(x);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return randomInt + 1;
	}
	
	public static void main(String[] args) {
		for ( int i = 0 ; i < 50 ; i++ )
		{
			System.out.println( i + " : " + RandomInt(i));
		}
	}

	/**
	 * i milliseconds sleep
	 * @param i
	 */
	public static void sleep(int i) 
	{
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
		}
	}


}
