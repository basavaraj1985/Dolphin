package com.basava.smart.framework;

import static com.basava.smart.framework.lib.common.LoggerUtil.log;
import static com.basava.smart.framework.lib.common.LoggerUtil.logDEBUG;
import static com.basava.smart.framework.lib.common.LoggerUtil.logINFO;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.xml.sax.SAXException;

import com.basava.framework.lib.xmlhandling.LocatorHelper;
import com.basava.framework.testng.dpextension.DataProviderUtils;
import com.basava.smart.framework.lib.common.IOUtil;
import com.basava.smart.framework.lib.common.LoggerUtil;
import com.basava.smart.framework.lib.common.StringUtil;
import com.basava.smart.framework.lib.web.WebActions;

/**
 * @author Basavaraj M
 *
 */
public abstract class CommonTest
{
	protected CompositeConfiguration configuration;
	protected LocatorHelper locatorHelper = null;
	boolean javaScriptErrorTestEnabled = false;
	protected ThreadLocal<String> testDataItem = new ThreadLocal<String>();
	
	/**
	 * Return test name, will be used for logging.
	 * @return
	 */
	public abstract String getTestGroupName() ;
	
	/**
	 * Open the browser after configuring. i.e. initialze the WebActions object.
	 */
	public abstract void configureBrowser() ;
	
	/**
	 * Return initialized WebActions object, on which browser controlling operations are invoked.
	 * @return
	 */
	public abstract WebActions actions();
	
	/**
	 * Why is it BeforeClass and not BeforeTest? </br>
	 *  To enable multithreaded runs at both test level and class level. If it were BeforeTest, then the test class instances wouldnt get testng configuration, when there multiple
	 *  test classes to run. 
	 */
	@BeforeClass( alwaysRun = true )
	@Parameters({ IConstants.CONFIG_FILE,IConstants.LOCATOR_FILE, IConstants.INTL, IConstants.BASE_URL, IConstants.BROWSER, IConstants.BROWSER_WIDTH, IConstants.BROWSER_HEIGHT,
		IConstants.USER_AGENT, IConstants.ADDNL_PARAMS, IConstants.LOG_FILE} )
	public void initialize(String configFile , String locatorFile, @Optional() String intlcode , @Optional() String baseURL, 
			String browzer,  @Optional() String bWidth, @Optional() String bHeight,
			@Optional() String userAgent, @Optional() String additionalParams, @Optional String logFile 
			) 
	{
		Thread.currentThread().setName(StringUtil.appendSpacesToMakeLength("[" + getTestGroupName() + "]", 25));
		configuration = new CompositeConfiguration();
		configuration.addConfiguration(new SystemConfiguration());
		try {
			configuration.addConfiguration(new PropertiesConfiguration(configFile));
		} catch (ConfigurationException e) {
			e.printStackTrace();
			System.out.println("Cannot load configuration : " + configFile);
			System.err.println("Cannot load configuration : " + configFile);
			System.exit(-1);
		}
//		shareLoggingRequiredParamsAsSystemProps(browzer, bWidth, bHeight);
		LoggerUtil.initialize();
		
		try {
			locatorHelper = new LocatorHelper(locatorFile);
		} catch (SAXException e1) {
			e1.printStackTrace();
			System.exit(-1);
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(-2);
		}
		
		/*
		 * Overriding configuration from testng.xml 
		 * testng.xml param values take priority over config.properties values
		 */
		overrideAllProperties(configFile, locatorFile, intlcode, baseURL, browzer, bWidth, bHeight, userAgent, additionalParams, logFile );
		configureBrowser();
		log("Configuration done : ");
		log("url:" + configuration.getString(IConstants.BASE_URL) + " | "
				+ "config:" + configuration.getString(IConstants.CONFIG_FILE)
				+ " | " + "intlCode:"
				+ configuration.getString(IConstants.INTL) + " | " + "browser:"
				+ configuration.getString(IConstants.BROWSER));
		log("width:" + configuration.getString(IConstants.BROWSER_WIDTH)
				+ " | " + "height:"
				+ configuration.getString(IConstants.BROWSER_HEIGHT) + " | "
				+ "addnlParams:"
				+ configuration.getString(IConstants.ADDNL_PARAMS) + " | "
				+ "logFile:" + configuration.getString(IConstants.LOG_FILE));
		log(userAgent);
	}

	/*
	 * Overriding configuration from testng.xml 
	 * testng.xml param values take priority over config.properties values
	 */
	private void overrideAllProperties( String configFile , String locatorFile, @Optional() String intlcode , @Optional() String baseURL, 
			String browzer, 
			@Optional() String bWidth, @Optional() String bHeight,
			@Optional() String userAgent, @Optional() String additionalParams, @Optional String logFile 
			) 
	{
			SystemConfiguration systemConfiguration = new SystemConfiguration();
			if ( null != systemConfiguration.getString(IConstants.CONFIG_FILE))
			{
				logDEBUG("Overriding with system property value - " + systemConfiguration.getString(IConstants.CONFIG_FILE)) ;
				overrideProperty(IConstants.CONFIG_FILE, systemConfiguration.getString(IConstants.CONFIG_FILE));
			}
			else
			{
				overrideProperty(IConstants.CONFIG_FILE, configFile);
			}
			if ( null != systemConfiguration.getString(IConstants.BASE_URL))
			{
				logDEBUG("Overriding with system property value - " + systemConfiguration.getString(IConstants.BASE_URL)) ;
				overrideProperty(IConstants.BASE_URL, systemConfiguration.getString(IConstants.BASE_URL));
			}
			else
			{
				overrideProperty(IConstants.BASE_URL, baseURL);
			}
			if ( null != systemConfiguration.getString(IConstants.INTL))
			{
				logDEBUG("Overriding with system property value - " + systemConfiguration.getString(IConstants.INTL) ) ;
				overrideProperty(IConstants.INTL, systemConfiguration.getString(IConstants.INTL));
			}
			else
			{
				overrideProperty(IConstants.INTL, intlcode);
			}
			if ( null != systemConfiguration.getString(IConstants.USER_AGENT))
			{
				logDEBUG("Overriding with system property value - " + StringUtil.limitLength(systemConfiguration.getString(IConstants.USER_AGENT), 60) );
				overrideProperty(IConstants.USER_AGENT, systemConfiguration.getString(IConstants.USER_AGENT));
			}
			else
			{
				overrideProperty(IConstants.USER_AGENT, userAgent);
			}
			if ( null != systemConfiguration.getString(IConstants.BROWSER))
			{
				logDEBUG("Overriding with system property value - " + systemConfiguration.getString(IConstants.BROWSER) ) ;
				overrideProperty(IConstants.BROWSER, systemConfiguration.getString(IConstants.BROWSER));
			}
			else
			{
				overrideProperty(IConstants.BROWSER, browzer);
			}
			if ( null != systemConfiguration.getString(IConstants.BROWSER_WIDTH))
			{
				logDEBUG("Overriding with system property value - " + systemConfiguration.getString(IConstants.BROWSER_WIDTH));
				overrideProperty(IConstants.BROWSER_WIDTH, systemConfiguration.getString(IConstants.BROWSER_WIDTH));
			}
			else
			{
				overrideProperty(IConstants.BROWSER_WIDTH, bWidth);
			}
			if ( null != systemConfiguration.getString(IConstants.BROWSER_HEIGHT))
			{
				logDEBUG("Overriding with system property value - " + systemConfiguration.getString(IConstants.BROWSER_HEIGHT));
				overrideProperty(IConstants.BROWSER_HEIGHT, systemConfiguration.getString(IConstants.BROWSER_HEIGHT));
			}
			else
			{
				overrideProperty(IConstants.BROWSER_HEIGHT, bHeight);
			}
			if ( null != systemConfiguration.getString(IConstants.ADDNL_PARAMS))
			{
				logDEBUG("Overriding with system property value - " + systemConfiguration.getString(IConstants.ADDNL_PARAMS) );
				overrideProperty(IConstants.ADDNL_PARAMS, systemConfiguration.getString(IConstants.ADDNL_PARAMS));
			}
			else
			{
				overrideProperty(IConstants.ADDNL_PARAMS, additionalParams);
			}
			if ( null != systemConfiguration.getString(IConstants.LOG_FILE))
			{
				logDEBUG("Overriding with system property value - " + systemConfiguration.getString(IConstants.LOG_FILE));
				overrideProperty(IConstants.LOG_FILE, systemConfiguration.getString(IConstants.LOG_FILE));
			}
			else
			{
				overrideProperty(IConstants.LOG_FILE, logFile);
			}
	}

	/*
	 * 
	 */
	private void shareLoggingRequiredParamsAsSystemProps(String browser, String bWidth, String bHeight)
	{
		System.setProperty(IConstants.LOG_LEVEL, configuration.getString(IConstants.LOG_LEVEL));
		System.setProperty(IConstants.HTML_LOGGING, configuration.getString(IConstants.HTML_LOGGING));
		System.setProperty(IConstants.LOG_FILE, configuration.getString(IConstants.LOG_FILE));
		
		if ( null != browser && browser.trim().length() > 1 )
		{
			System.setProperty(IConstants.BROWSER, browser );
		}
		else
		{
			System.setProperty(IConstants.BROWSER, configuration.getString(IConstants.BROWSER, "FF"));
		}
		
		if ( null != bWidth && bWidth.trim().length() > 1 )
		{
			System.setProperty(IConstants.BROWSER_WIDTH, bWidth );
		}
		else 
		{
			System.setProperty(IConstants.BROWSER_WIDTH, configuration.getString(IConstants.BROWSER_WIDTH, "0"));
		}
		
		if ( null != bHeight && bHeight.trim().length() > 1 )
		{
			System.setProperty(IConstants.BROWSER_HEIGHT, bHeight );
		}
		else 
		{
			System.setProperty(IConstants.BROWSER_HEIGHT, configuration.getString(IConstants.BROWSER_HEIGHT, "0"));
		}
	}

	/**
	 * 
	 * @return
	 */
	public LocatorHelper getLocatorHelper()
	{
		return locatorHelper;
	}
	
	/**
	 * 
	 * @return
	 */
	public CompositeConfiguration getConfiguration()
	{
		if ( null == configuration )
		{
			System.err.println("Configuration is null!!! " + Thread.currentThread().getName() + " Exiting with error code -200!");
			System.exit(-200);
		}
		return configuration;
	}
	
	/**
	 * Overrides existing property 'key' with 'valueToSet',if 'valueToSet' is not null and not empty.
	 * @param key
	 * @param valueToSet
	 */
	public void overrideProperty(String key, String valueToSet) 
	{
		if ( null != valueToSet && ! valueToSet.trim().isEmpty() )
		{
			String existing = configuration.getString(key);
			logINFO("Overriding property " + key + " existingValue: " + existing + " withNewValue:" + valueToSet );
			configuration.clearProperty(key);
			configuration.setProperty(key, valueToSet);
		}
	}

    /**
     * Data provider
     * Reads 'fileName' file, returns 'count' number of queries/lines, with 'random' attribute
     * if 'fileName' file doesn't exist and if you want to read a file with different intl, 
     * provide 'default_intl' parameter with default value.
     * Ex: If mx_NorthAds.csv is not present and if you want framework to read us_NorthAds.csv,
     * provide 'default_intl=us'
     * These parameters have to be passed Using @DataProviderUtils annotation as string array
     * @param ctxt
     * @param testMethod
     * @return
     */
	@DataProvider(name="getQueriesFromFile")
	public Object[][] getQueriesFromFile(ITestContext ctxt, Method testMethod)
	{
		int count = 1;
		String fileName = "whichFileToRead";
		boolean random =  configuration.getBoolean("random", false);
		
		Map<String, String> arguments = null;
		try {
			arguments = DataProviderUtils.resolveDataProviderArguments(testMethod);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if ( null != arguments && arguments.get("count") != null )
		{
			try {
				String countString = arguments.get("count");
				count = Integer.valueOf(countString).intValue();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		
		if ( null != arguments && arguments.get("count") == null && configuration.getInt("count", 0) != 0 )
		{
			count = configuration.getInt("count");
		}
		
		if ( null != arguments && arguments.get("fileName") != null )
		{
			fileName = arguments.get("fileName"); //		ex:	$[intl]_NorthAdsQueries.csv
			while ( fileName.contains("$"))
			{
				String parameter = (String) fileName.subSequence(fileName.indexOf("$")+2, fileName.indexOf("]"));
				String value = (String) configuration.getString(parameter,"noValueFoundFor_"+parameter+"_parameterInConfig");
				fileName = fileName.replaceAll("\\$\\["+parameter+"\\]", value);
			}
		}
		
		if ( null != arguments && arguments.get("random") != null )
		{
			String randomString = arguments.get("random");
			random = Boolean.parseBoolean(randomString.trim());
		}
		
		String[] queries = null;
		try {
			queries = IOUtil.readInfoFromFile(getConfiguration().getString(IConstants.RESOURCE_PATH)+ "data/" + fileName);
		} catch (Exception e) {
			e.printStackTrace();
			LoggerUtil.logERROR("Could not read the input file - " + getConfiguration().getString(IConstants.RESOURCE_PATH)+ "data/" + fileName);
			if ( null != arguments && arguments.get("fileName") != null 
					&& arguments.get("fileName").contains("intl")  && arguments.get("default_intl") != null)
			{
				String defaultIntl = arguments.get("default_intl") ;
				LoggerUtil.logERROR("Retrying with 'intl' param value as " + defaultIntl);
				fileName = arguments.get("fileName"); //		ex:	$[intl]_NorthAdsQueries.csv
				while ( fileName.contains("$"))
				{
					String parameter = (String) fileName.subSequence(fileName.indexOf("$")+2, fileName.indexOf("]"));
					String value = (String) configuration.getString(parameter,"noValueFoundFor_"+parameter+"_parameterInConfig");
					if ( parameter.equals("intl"))
					{
						fileName = fileName.replaceAll("\\$\\["+parameter+"\\]", defaultIntl);
					}
					else 
					{
						fileName = fileName.replaceAll("\\$\\["+parameter+"\\]", value);
					}
				}
				try {
					queries = IOUtil.readInfoFromFile(getConfiguration().getString(IConstants.RESOURCE_PATH)+ "data/" + fileName);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		};
		List<String> queriesToReturn = IOUtil.getQueriesToTest(queries, Integer.MAX_VALUE, -1, random);
		if ( count > queriesToReturn.size() )
		{
			count = queriesToReturn.size();
		}
		
		Object[][] result = new Object[count][2];
		for ( int index = 0 ; index < count ; index++ )
		{
			result[index][0] = queriesToReturn.get(index);
			result[index][1] = ctxt; 
		}
		return result;
	}
}
