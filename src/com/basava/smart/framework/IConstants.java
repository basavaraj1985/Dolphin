package com.basava.smart.framework;

/**
 * @author Basavaraj M
 *
 */
public interface IConstants
{
	public static final String BASE_URL = "baseUrl";
	public static final int MAX_STALE_ELEMENT_RETRIES = 5;
	public static final String IOS_SERVER = "iOSServer";
	public static final String RELATIVE_PATH = "relativePath";
	
	public static final String ADDNL_PARAMS = "additionalParams";
	
	public static final String LOG_LEVEL = "logLevel";
	public static final String HTML_LOGGING = "htmlLogging";
	public static final String LOG_FILE = "logFile";
	
	public static final String CONFIG_FILE = "configFile";
	public static final String LOCATOR_FILE = "locatorFile";
	
	public static final String INTL = "intl";
	public static final String BROWSER = "browser";
	public static final String BROWSER_WIDTH = "browserWidth";
	public static final String BROWSER_HEIGHT = "browserHeight";
	
	// Capabilities
	public static final String VERSION = "version";
	public static final String PLATFORM = "platform";
	
	public static final String USER_AGENT = "userAgent";
	
	// Simulator, iOS server related
	public static final String IOS_SERVER_COMMAND = "iOSServerCommand";
	public static final String IOS_SERVER_PORT = "iOSServerPortNumber";
	
	// remote runs
	public static final String REMOTE_WD_HOST = "remoteWebDriverHost";
	public static final String REMOTE_WD_PORT = "remoteWebDriverPort";
	
	public static final String CAPTURE_SCR_SHOTS_ALWAYS = "CaptureScreenshotsAlways";
	public static final String CAPTURE_SCR_SHOTS = "CaptureScreenshot";

	public static final String FLB = "fightLayoutBugs";
	public static final String RESOURCE_PATH = "resourcesPath";
}
