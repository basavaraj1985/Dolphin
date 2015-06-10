package com.javasbar.smart.framework.lib.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.javasbar.smart.framework.IConstants;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
/**
 * @author Basavaraj M
 *
 */
public class LoggerUtil 
{
	public static String LOG_FILE = null;
	public static String fileName = "testRunLog";
//	private static String month = Util.now("MMMM");
//	private static String year = Util.now("yyyy");
//	private static String day = Util.now("dd");
	public static String directory = "./Reports/";
	public static File dirStructure = new File(directory);
//	public static String timeStamp =  System.getProperty("build.number",Util.now("HH.mm.ss"));
	public static String screenshotDirName = "screenshots";
	public static String screenshotDirectory = directory + "/" + screenshotDirName ;
	public static File screenshotDirectoryStructure = new File(screenshotDirectory); 
	
	private static boolean imageCroppingBlocks = false;
	public static boolean initialized = false;
	private static BufferedWriter fileWriter = null;
	
	static
	{
		if (! dirStructure.exists() )
		{
			dirStructure.mkdirs();
		}
		if ( ! screenshotDirectoryStructure.exists() )
		{
			screenshotDirectoryStructure.mkdirs();
		}
		if ( System.getProperty(IConstants.HTML_LOGGING,"true").equalsIgnoreCase("true")  )
		{
			LOG_FILE =	dirStructure.getAbsolutePath() + "/"+ System.getProperty(IConstants.LOG_FILE, fileName ) +".html";
		}
		else
		{
			LOG_FILE =	dirStructure.getAbsolutePath() + "/"+ System.getProperty(IConstants.LOG_FILE, fileName ) +".log";
		}
		try {
			fileWriter = new BufferedWriter(new FileWriter(LOG_FILE, false));
		} catch (IOException e) {
			e.printStackTrace();
		}
		initialize();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		    	try {
					fileWriter.flush();
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		});
	}
	
	/**
	 * Reports will be put at ./Reports/yyyy/MMMM/dd/
	 * with the file name passed in this method as parameter
	 * @param fileName
	 */
	public synchronized static void initialize()
	{
		if (  System.getProperty(IConstants.HTML_LOGGING,"true").equalsIgnoreCase("true") && ! initialized )
		{
			simpleLog("<html>", false);
			simpleLog("<body>", false);
			if ( ( !imageCroppingBlocks ) && ( System.getProperty(IConstants.BROWSER, "").equalsIgnoreCase("ff") 
						||  System.getProperty(IConstants.BROWSER, "").equalsIgnoreCase("firefox") ) )
			{
				imageCroppingBlocks();
			}
			initialized = true;
		}
	}

	private static void imageCroppingBlocks() 
	{
		simpleLog("<script>", false);
		simpleLog("function OnImageLoad(evt) {", false);
		simpleLog("var img = evt.currentTarget;", false);
		simpleLog("var w = $(img).width(, false);", false);
		simpleLog("var h = $(img).height(, false);", false);
		simpleLog("var tw = $(img).parent().width(, false);", false);
		simpleLog("var th = $(img).parent().height(, false);", false);
		simpleLog("var result = ScaleImage(w, h, tw, th, false, false);", false);
		simpleLog("img.width = result.width;", false);
		simpleLog("img.height = result.height;", false);
		simpleLog("$(img).css(\"left\", result.targetleft, false);", false);
		simpleLog("$(img).css(\"top\", result.targettop, false);", false);
		simpleLog("}", false);
		simpleLog("</script>", false);
		simpleLog("<style>", false);
		simpleLog(".crop {", false);
		if ( null != System.getProperty("browserWidth") && ! System.getProperty("browserWidth").startsWith("0") 
				&& ! ( System.getProperty("browser","none").equalsIgnoreCase("iphone") 
						|| System.getProperty("browser","none").equalsIgnoreCase("ipad") ) )
		{
			simpleLog("width: " + System.getProperty("browserWidth") + "px;", false); 
		}
		if ( null != System.getProperty("browserHeight")  && ! System.getProperty("browserHeight").startsWith("0")
				&& !( System.getProperty("browser","none").equalsIgnoreCase("iphone") 
						|| System.getProperty("browser","none").equalsIgnoreCase("ipad")))
		{
			simpleLog("height: "+ System.getProperty("browserHeight")  +"px;", false); 
		}
		if (  !( System.getProperty("browser","none").equalsIgnoreCase("iphone") 
						|| System.getProperty("browser","none").equalsIgnoreCase("ipad")) )
		{
			simpleLog("border: 2px", false); 
			simpleLog("solid #666666;", false); 
		}
		simpleLog("overflow: hidden;", false); 
		simpleLog("position: relative;", false);
		simpleLog("background-size: cover;", false);
		simpleLog("}", false);
		simpleLog("</style>", false);
		imageCroppingBlocks = true;
	}
	
	/**
	 * It writes 'log' record to configured 'LOGFILE' adding timestamp to it.
	 * @param log
	 */
	public static synchronized void logWithTag(String log, String tag)
	{
		String timeStamp = Util.now();
		log = timeStamp + " - " + Thread.currentThread().getName() + " " +  log;
		log = escapeHtml(log);
		simpleLog("<a id=\""+tag+"\" >" + log + " </a>");
	}
	
	/**
	 * It writes 'log' record to configured 'LOGFILE' adding timestamp to it.
	 * @param log
	 */
	public static synchronized void log(String log)
	{
		String timeStamp = Util.now();
		log = timeStamp + " - " + Thread.currentThread().getName() + " " +  log;
		log = escapeHtml(log);
		simpleLog(log);
	}
	
	/**
	 * It writes 'log' record to configured 'LOGFILE' adding timestamp to it.
	 * @param log
	 */
	public static synchronized void log(String log, boolean lineBreakRequired)
	{
		String timeStamp = Util.now();
		log = timeStamp + " - " + Thread.currentThread().getName() + " " +  log;
		log = escapeHtml(log);
		simpleLog(log, lineBreakRequired);
	}
	
	/**
	 * It writes 'log' record to configured 'LOGFILE' adding timestamp and [INFO] to it, </br>
	 * only when configured 'logLevel' is INFO
	 * 
	 * @param log
	 */
	public static synchronized  void logINFO(String log)
	{
		String property = System.getProperty( IConstants.LOG_LEVEL, "INFO");
		if ( null != property && ( property.contains("DEBUG") || property.contains("INFO")  )   )
		{
			log = escapeHtml(log);
			log("[INFO] " + log);
		}
	}
	
	/**
	 * It writes 'log' record to configured 'LOGFILE' adding timestamp and [WARNING] to it, </br>
	 * only when configured 'logLevel' is WARNING and above severe.
	 * @param log
	 */
	public static synchronized  void logINFOHighlight(String log)
	{
		String property = System.getProperty(IConstants.LOG_LEVEL, "INFO");
		if ( null != property && ( property.contains("DEBUG") || property.contains("INFO")  )  )
		{
			if ( System.getProperty(IConstants.HTML_LOGGING,"true").equalsIgnoreCase("true") )
			{
				simpleLog("<font color='blue'>", false);
				log = escapeHtml(log);
				log("[INFO] " + log, false);
				simpleLog("</font>", false);
				simpleLog("",true);
			}
			else
			{
				log("[INFO] " + log);
			}
		}
	}
	
	
	/**
	 * It writes 'log' record to configured 'LOGFILE' adding timestamp and [DEBUG] to it, </br>
	 * only when configured 'logLevel' is DEBUG and above severe.
	 * @param log
	 */
	public static synchronized  void logDEBUG(String log)
	{
		String property = System.getProperty(IConstants.LOG_LEVEL, "DEBUG");
		if ( null != property && ( property.contains("DEBUG")  )  )
		{
			log = escapeHtml(log);
			log("[DEBUG] " + log);
		}
	}
	
	/**
	 * It writes 'log' record to configured 'LOGFILE' adding timestamp and [WARNING] to it, </br>
	 * only when configured 'logLevel' is WARNING and above severe.
	 * @param log
	 */
	public static synchronized  void logWARNING(String log)
	{
		String property = System.getProperty(IConstants.LOG_LEVEL, "WARNING");
		if ( null != property && ( property.contains("DEBUG") || property.contains("INFO") || property.contains("WARNING")  )  )
		{
			log = escapeHtml(log);
			if ( System.getProperty(IConstants.HTML_LOGGING,"true").equalsIgnoreCase("true") )
			{
				simpleLog("<font color='orange'>", false);
				log("[WARNING] " + log, false);
				simpleLog("</font>", false);
				simpleLog("",true);
			}
			else
			{
				log("[WARNING] " + log);
			}
		}
	}
	
	/**
	 * It writes 'log' record to configured 'LOGFILE' adding timestamp and [ERROR] to it, ALWAYS. </br>
	 * And if the input string has new line characters, then the string will be split and put into 
	 * different lines.
	 * @param log
	 */
	public static synchronized  void logERROR(String log)
	{
		//  errors will always be logged
		log = escapeHtml(log);
		String[] logs = log.split("\n");
		
		if ( System.getProperty(IConstants.HTML_LOGGING,"true").equalsIgnoreCase("true") )
		{
			simpleLog("<font color='red'>", false);
			for ( String s : logs )
			{
				if ( System.getProperty(IConstants.HTML_LOGGING,"true").equalsIgnoreCase("true") )
				{
					log("[ERROR] " + s, false);
				}
			}
			simpleLog("</font>", false);
			simpleLog("", true);
		}
		else
		{
			for ( String s : logs )
			{
				log("[ERROR] " + s);
			}
		}
		
	}
	
	public static synchronized void logException(Exception excpn )
	{
		logERROR(excpn.getMessage());
		StackTraceElement[] ste = excpn.getStackTrace();
		for ( StackTraceElement element : ste )
		{
			logERROR("\t\t" + element.toString());
		}
	}

	/**
	 * It writes the raw 'log' record to configured 'LOGFILE', 
	 * @param log
	 */
	public static synchronized  void simpleLog(String log)
	{
		simpleLog(log, true);
	}
	
	/**
	 * Logs a link in log file
	 * @param message
	 * @param url
	 */
	public static synchronized void logALink(String message, String url)
	{
		message = escapeHtml(message);
		log("<a href=\""+ url + "\" "+ "title=\""+ url + "\"" +" target=\"_blank\">" + message + "</a>", true);
	}
	
	public static synchronized void simplyLogAscreenshotURL(String log)
	{
		log("ScreenShot : ", true);
		if (  ( System.getProperty(IConstants.BROWSER, "").equalsIgnoreCase("ff")  ||  System.getProperty(IConstants.BROWSER, "").equalsIgnoreCase("firefox") ) )
		{
			simpleLog("<p class=\"crop\">", false );
			simpleLog("<a href=\"" + log + "\" />", false);
			simpleLog("<img src=\"" + log + "\" onload=\"OnImageLoad(event);\"/>", false);
		}
		else 
		{
			simpleLog("<p>", false );
			simpleLog("<a href=\"" + log + "\" />", false);
			simpleLog("<img src=\"" + log + "\"/>", false);
		}
		simpleLog("</a>", false);
		simpleLog("</p>", false);
	}
	
	/**
	 * It writes the raw 'log' record to configured 'LOGFILE', 
	 * @param log
	 */
	public static synchronized  void simpleLog(String log, boolean lineBreakRequired)
	{
		try {
			fileWriter.append(log);
			if ( lineBreakRequired && System.getProperty(IConstants.HTML_LOGGING,"true").equalsIgnoreCase("true") )
			{
				fileWriter.append("\t\t\t<br>");
			}
			fileWriter.write('\n');
			fileWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
