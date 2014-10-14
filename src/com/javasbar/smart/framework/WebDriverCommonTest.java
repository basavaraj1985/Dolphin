package com.javasbar.smart.framework;

import static com.javasbar.smart.framework.lib.common.LoggerUtil.log;
import static com.javasbar.smart.framework.lib.common.LoggerUtil.logALink;
import static com.javasbar.smart.framework.lib.common.LoggerUtil.logERROR;
import static com.javasbar.smart.framework.lib.common.LoggerUtil.logINFOHighlight;
import static com.javasbar.smart.framework.lib.common.LoggerUtil.logWARNING;
import static com.javasbar.smart.framework.lib.common.LoggerUtil.logWithTag;
import static com.javasbar.smart.framework.lib.common.LoggerUtil.simpleLog;
import static com.javasbar.smart.framework.lib.common.LoggerUtil.simplyLogAscreenshotURL;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.jsourcerer.webdriver.jserrorcollector.JavaScriptError;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.uiautomation.ios.IOSCapabilities;

import com.googlecode.fightinglayoutbugs.LayoutBug;
import com.javasbar.smart.framework.lib.common.LoggerUtil;
import com.javasbar.smart.framework.lib.common.OSValidator;
import com.javasbar.smart.framework.lib.common.RunTimeLib;
import com.javasbar.smart.framework.lib.common.ScreenshotUtil;
import com.javasbar.smart.framework.lib.common.StringUtil;
import com.javasbar.smart.framework.lib.web.WebActions;

/**
 * @author Basavaraj M
 *
 */
public abstract class WebDriverCommonTest extends CommonTest 
{
	protected WebDriver driver = null;
	protected WebActions actions = null;
	protected ScreenshotUtil camera = null;
	
	private boolean screenshotsEnabled = true;
	private static boolean startiOSServerFlag = false;
	
	FirefoxProfile ffProfile = new FirefoxProfile();
	List<JavaScriptError> javaScriptErrors = new ArrayList<JavaScriptError>();
	
	public abstract String getTestGroupName();
	
	/**
	 * 
	 * @return
	 */
	public WebDriver driver()
	{
		if ( null != driver )
		{
			return driver;
		}
		else
		{
			configureBrowser();
		}
		return driver;
	}
	
	/**
	 * Initializes <code>WebDriver - driver</code>, <code>WebActions - actions</code> and <code>FirefoxProfile - ffProfile</code> objects
	 */
	public void configureBrowser() 
	{
		if ( null != configuration.getString(IConstants.USER_AGENT) )
		{
			ffProfile.setPreference("general.useragent.override", configuration.getString(IConstants.USER_AGENT) );
		}
		
		try
		{
			if ( null == configuration.getString(IConstants.BROWSER) )
			{
				driver = new FirefoxDriver(ffProfile);
			}
			else if ( configuration.getString(IConstants.BROWSER).compareToIgnoreCase("chrome") == 0)
			{
				ChromeOptions options = new ChromeOptions();
				if ( null != configuration.getString(IConstants.USER_AGENT) )
				{
					options.addArguments("--user-agent=" + configuration.getString(IConstants.USER_AGENT) );
					options.addArguments("--start-maximized=" + configuration.getString("chromeMinimized", "true"));
//					cp.setCapability("--user-agent", configuration.getString(IConstants.USER_AGENT) );
//					cp.setCapability("chrome.switches", Arrays.asList("--user-agent=" + configuration.getString(IConstants.USER_AGENT) ));
				}
				driver = new ChromeDriver(options);
			}
			else if ( configuration.getString(IConstants.BROWSER).contains("remote"))
			{
				String remoteBrowser = configuration.getString(IConstants.BROWSER).split(":").length > 1 ? configuration.getString(IConstants.BROWSER).split(":")[1]: "chrome";
				DesiredCapabilities cp = new DesiredCapabilities();
				cp.setBrowserName(remoteBrowser);
				cp.setJavascriptEnabled(true);
				
				if ( remoteBrowser.compareToIgnoreCase("chrome") == 0 )
				{
					ChromeOptions options = new ChromeOptions();
					if ( null != configuration.getString(IConstants.USER_AGENT) )
					{
						options.addArguments("--user-agent=" + configuration.getString(IConstants.USER_AGENT) );
						options.addArguments("--start-maximized=" + configuration.getString("chromeMinimized", "true"));
						cp.setCapability(ChromeOptions.CAPABILITY, options);
					}
				}
				else if ( remoteBrowser.compareToIgnoreCase("ff") == 0 || remoteBrowser.compareToIgnoreCase("firefox") == 0 )
				{
					if ( null != configuration.getString(IConstants.USER_AGENT) )
					{
						cp.setCapability(FirefoxDriver.PROFILE, ffProfile);
					}
				}
				else 
				{
					logWARNING("In remote/grid mode only chrome/ff browsers are supported for user agent support as of now. Using chrome browser!");
//					ChromeOptions options = new ChromeOptions();
//					if ( null != configuration.getString(IConstants.USER_AGENT) )
//					{
//						options.addArguments("--user-agent=" + configuration.getString(IConstants.USER_AGENT) );
//						options.addArguments("--start-maximized=" + configuration.getString("chromeMinimized", "true"));
//						cp.setCapability(ChromeOptions.CAPABILITY, options);
//					}
				}
				logINFOHighlight(cp.toString());
				logERROR(cp.toString());
				URL server = new URL("http://"+ configuration.getString(IConstants.REMOTE_WD_HOST,"localhost") 
						+ ":" + configuration.getString(IConstants.REMOTE_WD_PORT,"4444")+ "/wd/hub");
				driver = new RemoteWebDriver(server, cp);
			}
			else if ( configuration.getString(IConstants.BROWSER).compareToIgnoreCase("FF") == 0 || configuration.getString(IConstants.BROWSER).compareToIgnoreCase("firefox") == 0 )
			{
				if ( configuration.getBoolean("jsErrorCapture"))  // if config not set, 
				{
					javaScriptErrorTestEnabled = true;
				}
				JavaScriptError.addExtension(ffProfile);
				driver = new FirefoxDriver(ffProfile);
			}
			else if ( configuration.getString(IConstants.BROWSER).compareToIgnoreCase("iphone") == 0 || configuration.getString(IConstants.BROWSER).compareToIgnoreCase("iphoneDriver") == 0 )
			{
//				driver = new IPhoneDriver();
				if ( ! startiOSServerFlag && configuration.getString(IConstants.IOS_SERVER,"localhost").equalsIgnoreCase("localhost") )
				{
					startiOSServer();
					startiOSServerFlag = true;
				}
				DesiredCapabilities safari = IOSCapabilities.iphone("Safari");
				String iOSServer = configuration.getString(IConstants.IOS_SERVER,"localhost");
				String iOSServerPortNumber = configuration.getString( IConstants.IOS_SERVER_PORT, "4444");
				String finalURL = "http://"+ iOSServer.trim() +":" + iOSServerPortNumber.trim() + "/wd/hub";
		        driver = new RemoteWebDriver(new URL( finalURL ), safari);
//				driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), safari);
			}
			else if ( configuration.getString(IConstants.BROWSER).compareToIgnoreCase("ie") == 0 || configuration.getString(IConstants.BROWSER).compareToIgnoreCase("internetExplorer") == 0 )
			{
				driver = new InternetExplorerDriver();
				/*
				 * To do;
				 * find out user agent setting on IE
				 */
			}
			else if ( configuration.getString(IConstants.BROWSER).compareToIgnoreCase("android") == 0 || configuration.getString(IConstants.BROWSER).compareToIgnoreCase("androidDriver") == 0 )
			{
				/*
				 * Forward requests to tcp port
				 * run command to find device id, connect etc..
				 */
//				driver = new AndroidDriver();
				driver = new RemoteWebDriver(DesiredCapabilities.android());
//				driver.manage().window().setSize(new Dimension(configuration.getInt(IConstants.BROWSER_WIDTH, 320), configuration.getInt(IConstants.BROWSER_HEIGHT, 700)));
				/*
				 * To do;
				 */
			}
			else if ( configuration.getString(IConstants.BROWSER).compareToIgnoreCase("htmlunit") == 0 || configuration.getString(IConstants.BROWSER).compareToIgnoreCase("html") == 0 )
			{
				driver = new HtmlUnitDriver(true);
				screenshotsEnabled = false;
				/*
				 * To do:
				 * user agent setting
				 */
			}
			else if ( configuration.getString(IConstants.BROWSER).compareToIgnoreCase("phantom") == 0 || configuration.getString(IConstants.BROWSER).compareToIgnoreCase("phantomjs") == 0 )
			{
				DesiredCapabilities capabilities = new DesiredCapabilities();
				capabilities.setJavascriptEnabled(true);
				capabilities.setCapability("takesScreenshot", false);
				if ( null != configuration.getString(IConstants.USER_AGENT)  )
				{
					capabilities.setCapability("phantomjs.page.settings.userAgent", configuration.getString(IConstants.USER_AGENT) );
				}
				driver = new PhantomJSDriver();
				screenshotsEnabled = false;
//				throw new Exception("Not supporting PhantomJSDriver yet!");
				/*
				 * To do:
				 * user agent setting
				 */
			}
			if ( configuration.getString(IConstants.BROWSER).compareToIgnoreCase("android") != 0  &&
					configuration.getString(IConstants.BROWSER).compareToIgnoreCase("iphone") != 0 && 
						configuration.getString(IConstants.BROWSER).compareToIgnoreCase("ipad") != 0 &&
						   !( configuration.getString(IConstants.BROWSER).compareToIgnoreCase("selastic") == 0 
						   	 ) 
				)
			{
				if ( configuration.getInt(IConstants.BROWSER_WIDTH, 0) > 0 && configuration.getInt(IConstants.BROWSER_HEIGHT, 0) > 0 )
				{
					driver.manage().window().setSize(new Dimension(configuration.getInt(IConstants.BROWSER_WIDTH), configuration.getInt(IConstants.BROWSER_HEIGHT)));
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logERROR(e.getMessage());
			logERROR("Exception happened, exiting!");
			System.exit(-1);
		}
		actions = new WebActions(driver, locatorHelper, configuration );
		camera = new ScreenshotUtil(driver, LoggerUtil.screenshotDirectoryStructure);
	}
	
	private void startiOSServer() 
	{
		String defaultCommand = "java -jar ./lib/drivers/ios-server-0.6.3-jar-with-dependencies.jar" +
						"-simulators  -port 4444";
		String commandToRun = configuration.getString( IConstants.IOS_SERVER_COMMAND, defaultCommand);
		commandToRun = commandToRun.trim() + " -port " + configuration.getString(IConstants.IOS_SERVER_PORT, "4444");
		String directoryStructure = LoggerUtil.screenshotDirectoryStructure.getAbsolutePath()+"/";
		try {
			File dirStruct = new File(directoryStructure);
			dirStruct.mkdirs();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		if ( OSValidator.isMac() || OSValidator.isUnix() )
		{
			RunTimeLib.runCommandBlocking("for x in `ps -ef | grep ios-server | grep \"\\-port\" | cut -d\" \" -f2`; do kill -9 $x; done");
		}
		
		RunTimeLib.runCommandInAThread( commandToRun, directoryStructure + "/"+"iOSServerLog.txt", "iOSServer");
		try {
			Thread.sleep(5*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param status
	 * @return
	 */
	private String status(int status) 
	{
		String result = "";
		switch (status) 
		{
		case ITestResult.SUCCESS :
			result = "PASS";
			break;
			
		case ITestResult.SUCCESS_PERCENTAGE_FAILURE:	
		case ITestResult.FAILURE :
			result = "FAIL";
			break;
			
		case ITestResult.SKIP :
			result = "SKIPPED";
			break;
			
		default:
			logWARNING("Invalid result state : " + status );
			result = "INVALID RESULT STATE";
			break;
		}
		return result;
	}
	
	public ScreenshotUtil camera()
	{
		if ( null == camera )
		{
			camera = new ScreenshotUtil(driver, LoggerUtil.screenshotDirectoryStructure);
		}
		return camera;
	}
	
	@Override
	public WebActions actions()
	{
		if ( null == actions )
		{
			actions = new WebActions(driver, locatorHelper, configuration );
		}
		return actions;
	}
	
	@BeforeMethod(alwaysRun = true)
	public void beforeOfTestCaseMethod(ITestContext ctxt)
	{
		ctxt.setAttribute(IConstants.BASE_URL, configuration.getProperty(IConstants.BASE_URL));
	}
	
	@AfterMethod(alwaysRun = true )
	public void endOfTestCaseMethod(ITestResult result, ITestContext ctxt) 
	{
		String query = this.testDataItem.get();
		if ( null == query )
		{
			query = StringUtil.limitLength(actions.getWindowTitle(), 25 );
		}
		query = query.replaceAll(" ", "+");
		String testCaseName = result.getName() ;
		String description = result.getMethod().getDescription();
		//Delete these lines if you are not using the screenshot feature

		if ( configuration.getBoolean(IConstants.CAPTURE_SCR_SHOTS_ALWAYS, false)  && screenshotsEnabled  )
		{
			try {
				camera.shoot( testCaseName+"_"+query);
			} catch (IOException e) {
				e.printStackTrace();
			}
//			camera.shoot(ctxt.getAttribute("element"), dir, fileName, point, r);
		}
		if(configuration.getBoolean(IConstants.CAPTURE_SCR_SHOTS) && ! result.isSuccess() && screenshotsEnabled )
		{
			try {
				camera.shoot( testCaseName+"_"+query);
				String computername = "localhost";
				try {
					computername = InetAddress.getLocalHost().getHostName();
				} catch (Exception e) {
					e.printStackTrace();
				}
				String screenshotFilePath = "./" + LoggerUtil.screenshotDirName + "/" + testCaseName+"_"+query + ".png" ;
//				if ( computername.contains("hudson") || computername.contains("ci") 
//						|| computername.contains("jenkin") || computername.contains("factory") 
//							|| computername.contains(".corp.") || computername.contains(".yahoo.com"))
//				{
//					screenshotFilePath = "./" + LoggerUtil.screenshotDirName + "/" + testCaseName+"_"+query + ".png" ;
//				}
//				else
//				{
//					screenshotFilePath = "file:///" + LoggerUtil.screenshotDirName+ "/" + testCaseName+"_"+query + ".png";
//				}
				simplyLogAscreenshotURL(screenshotFilePath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if ( configuration.getBoolean(IConstants.FLB) && ! configuration.getString(IConstants.BROWSER).equalsIgnoreCase("iphone"))
		{
			Collection<LayoutBug> uiBugs = camera.analyseUI(new File(LoggerUtil.screenshotDirectory));
			if ( null != uiBugs && uiBugs.size() > 0 )
			{
				logERROR("UI Issues present : " + uiBugs.size() ) ;
				for ( LayoutBug bug : uiBugs )
				{
	//				logERROR("UI issue : " + bug.getHtml() );
					logERROR("Description : " + bug.getDescription() );
					logERROR("UI Error:");
					simplyLogAscreenshotURL("file:///" + bug.getScreenshot() );
				}
			}
		}
		log("[TestCase Description:" + description + "]");
		Throwable throwable = result.getThrowable();
		if ( null != throwable && !result.isSuccess() )
		{
			logALink("Failure page, click here!", actions.getCurrentURL());
			log("Fail Reason: " + throwable.getMessage() );
		}
		
		if ( configuration.getBoolean(IConstants.HTML_LOGGING))
		{
			logWithTag("--------------------------------End of test - " + getTestGroupName() + ":[<b>" + testCaseName + "_" + query +  "</b>]-----------------------------------------" + status(result.getStatus() ), testCaseName);
		}
		else 
		{
			log("--------------------------------End of test - [" + getTestGroupName() + ":" + testCaseName + "_" + query+ "]-----------------------------------------" + status(result.getStatus() ));
		}
		simpleLog("");
	}
	
	@AfterMethod(alwaysRun = true, dependsOnMethods="endOfTestCaseMethod")
	public void collectJSErrors(ITestResult result)
	{
		if ( ! javaScriptErrorTestEnabled )
		{
			return;
		}
		
		List<JavaScriptError> jsErrors = JavaScriptError.readErrors(driver);
		if ( jsErrors.isEmpty() )
		{
			return;
		}
		for ( JavaScriptError error : jsErrors )
		{
			logERROR( result.getName() + " : " + error.getErrorMessage() + " source: " + error.getSourceName() + " line: " + error.getLineNumber() );
		}
		javaScriptErrors.addAll(jsErrors);
	}
	
	@AfterClass(alwaysRun=true)
	public void finalization()
	{
		if ( ( OSValidator.isMac() || OSValidator.isUnix() ) && startiOSServerFlag )
		{
			RunTimeLib.runCommandBlocking("for x in `ps -ef | grep ios-server | grep \"\\-port\" | cut -d\" \" -f2`; do kill -9 $x; done");
		}
		driver.quit();
	}
	
	@AfterClass(alwaysRun = true)
	public void reportJSErrors()
	{
		if ( javaScriptErrors.isEmpty() )
		{
			return;
		}
		log("\n");
		log("-------------------------------------------------------");
		logERROR("JS errors are found : " + javaScriptErrors.size() );
		for ( JavaScriptError error : javaScriptErrors )
		{
			logERROR(error.getErrorMessage() + " source: " + error.getSourceName() + " line: " + error.getLineNumber() );
		}
		fail("JS errors are found : " + javaScriptErrors.size() );
	}

}
