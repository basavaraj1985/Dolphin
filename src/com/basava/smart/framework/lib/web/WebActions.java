package com.basava.smart.framework.lib.web;

import static com.basava.smart.framework.lib.common.LoggerUtil.log;
import static com.basava.smart.framework.lib.common.LoggerUtil.logDEBUG;
import static com.basava.smart.framework.lib.common.LoggerUtil.logERROR;
import static com.basava.smart.framework.lib.common.LoggerUtil.logINFO;
import static com.basava.smart.framework.lib.common.LoggerUtil.logWARNING;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.CompositeConfiguration;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.basava.framework.lib.xmlhandling.LocatorHelper;
import com.basava.smart.framework.IConstants;
import com.basava.smart.framework.lib.common.StringUtil;

/**
 * @author Basavaraj M
 *
 */
public class WebActions implements IAction
{
	WebDriver driver;
	LocatorHelper locator;
	CompositeConfiguration configuration;
	
	public WebActions(WebDriver dri, LocatorHelper helper, CompositeConfiguration config) 
	{
		driver = dri;
		locator = helper;
		configuration = config;
	}
	
	/**
	 * Opens search page for the query. The url is prepared considering baseUrl, relativePath, additionalParams, query, bucket
	 * @author basavar
	 * @param query
	 */
	@Override
	public void openSRPForQuery(String query)
	{
		String url = configuration.getString(IConstants.BASE_URL) 
						+ configuration.getString(IConstants.RELATIVE_PATH)  + query  
							+ configuration.getString(IConstants.ADDNL_PARAMS) ; 
		logINFO("Going to : " + url);
		driver.get(url);
		waitForPage();
		String currentBucket = getText("SRP", "CurrentBucket", false);
		logINFO("Landed in : " + driver.getCurrentUrl() + " \t & Bucket: " + currentBucket );
	}
	
	/**
	 * Opens search page for the query. The url is prepared considering baseUrl, relativePath, additionalParams, query, bucket.
	 * And after opening the page handles alerts with @param handleAlerts value
	 * @author basavar
	 * @param query
	 */
	@Override
	public  void openSRPForQuery(String query, boolean handleAlerts)
	{
		openSRPForQuery(query);
		handleAlerts(handleAlerts);
	}
	
	/**
	 * Opens given url
	 * @author basavar
	 * @param url
	 * @return  - time taken in milliseconds
	 */
	@Override
	public long openPage(String url)
	{
		long start = System.currentTimeMillis();
		driver.get(url);
		long end = System.currentTimeMillis();
		return (end - start);
	}

	/**
	 * Goes to <code>baseUrl</code> configured
	 * @author basavar
	 */
	@Override
	public  void gotoBaseUrl() 
	{
		logINFO("Going to : " + configuration.getString("baseUrl"));
		driver.get(configuration.getString("baseUrl"));
		logINFO("Landed in : " + driver.getCurrentUrl() );
	}
	
	/**
	 * Handles alerts with the 'OK' or 'Cancel'  according to the accept parameter value
	 * @param accept true for OK/Agree/Yes, true for cancel
	 *  @author basavar
	 */
	@Override
	public void handleAlerts(boolean accept)
	{
		Alert alert = null;
		try {
			alert = driver.switchTo().alert();
			if ( null != alert )
			{
				log("Handling alert : " + alert.getText());
				if ( accept )
				{
					alert.accept();
					log("accepted!");
				}
				else
				{
					alert.dismiss();
					log("alert dismissed!");
				}
				log("Landed on: " + driver.getCurrentUrl() );
			}
		} catch (Exception e) {
			if ( e.getMessage().toLowerCase().contains("no alert"))
			{
				return;
			}
			e.printStackTrace();
			logERROR("Could not handle alert, exception happened \n" + StringUtil.limitLength(e.getMessage(), 120));
			return;
		}
		handlePopups(driver.getWindowHandle());
	}
	
	/**
	 * Maximizes the window to fit screen
	 */
	public void maximizeWindow()
	{
		driver.manage().window().maximize();
	}

	/**
	 * Checks if element is present
	 * @author basavar
	 * @param page
	 * @param element
	 * @return true if web-element is present on the current page, else false. 
	 */
	@Override
	public boolean isElementPresent(String page, String element) 
	{
		By by = locator.getLocator(page, element);
		boolean result = isElementPresent(by);
		if ( !result )
		{
			logERROR("Element not found : " + page + ">" + element + " locator:" + by.toString() );
		}
		return result;
	}
	
	/**
	 * Checks if element is present
	 * @author basavar
	 * @param locator
	 * @return true if web-element is present on the current page, else false.
	 */
	private boolean isElementPresent(By locator) 
	{
		WebElement found = null;
		try {
			found = driver.findElement(locator);
		} catch (NoSuchElementException e) {
			e.printStackTrace();
			logERROR("Element not found, locator:" + locator.toString());
		}
		return ( null != found );
	}
	
	/**
	 * Clicks on element
	 * @author basavar
	 * @param page
	 * @param element
	 */
	@Override
	public void clickElement(String page, String element) 
	{
		By elementLocator = locator.getLocator(page, element);
		try {
			WebElement elementToClick = driver.findElement(elementLocator);
//			assertTrue( isElementVisible(page, element), "Element not visible! " + page + " > " + element );
			logINFO("Clicking " + page + "->" + element + ":" + elementLocator );
			assertTrue( clickWithRetries(elementToClick, IConstants.MAX_STALE_ELEMENT_RETRIES),
					"Click element not successful even after retries. Element: " + page + ">" + element + ":" + elementLocator.toString());
		} catch ( WebDriverException wEx) {
			wEx.printStackTrace();
			fail("Could not click element : " + page + "->" + element + 
					" Reason: " + StringUtil.limitLength(wEx.getMessage(), 120) );
		}
		catch (Exception e) {
			e.printStackTrace();
			fail("Could not click element : " + page + "->" + element + " Reason: " + StringUtil.limitLength(e.getMessage(), 120));
		}
	}
	
	/**
	 * To be used when there are multiple occurrences of an element. </br>
	 * Example: There are 10 checkboxes with same locator expression, and 5th one needs to be clicked. </br>
	 * Pass 'page', 'checkBoxElement', 5.
	 * @author basavar
	 * @param page
	 * @param element
	 * @param n  n > 0 
	 */
	@Override
	public void clickElementNth(String page, String element, int n)
	{
		By by = locator.getLocator(page, element);
		try {
			List<WebElement> elements = driver.findElements(by);
			if ( elements.size() < n )
			{
				fail("Cannot click " + n + "th element, because there are only " + elements.size() + " elements! locator: " + by.toString() );
			}
//			assertTrue( isElementVisible(page, element), "Element not visible! " + page + " > " + element );
			WebElement elementToClick = elements.get(n - 1);
			logINFO("Clicking " + page + "->" + element + ":(" + n + ")"+ by  );
			clickWithRetries(elementToClick, IConstants.MAX_STALE_ELEMENT_RETRIES );
			logINFO("Current url : " + driver.getCurrentUrl() );
		} catch ( WebDriverException wEx) {
			wEx.printStackTrace();
			fail("Could not click element : " + page + "->" + element + 
					" Reason: " + StringUtil.limitLength(wEx.getMessage(), 120) );
		}
		catch (Exception e) {
			e.printStackTrace();
			fail("Could not click element : " + page + "->" + element + " Reason: " + StringUtil.limitLength(e.getMessage(), 120) );
		}
	}
	
	/**
	 * Retries at most @param maxStaleElementRetries to click the element, catching stale exceptions
	 * @author basavar
	 * @param elementToClick
	 * @param maxStaleElementRetries
	 */
	private boolean clickWithRetries(WebElement elementToClick, int maxStaleElementRetries) 
	{
		boolean isSuccess = false;
		int count = 0 ;
		while ( count < maxStaleElementRetries )
		{
			try 
			{
				elementToClick.click();
				waitForPage();
				count = count + maxStaleElementRetries;
				isSuccess = true;
			} 
			catch (StaleElementReferenceException e) 
			{
				System.err.println( e.getMessage());
				count++;
			} 
			catch ( WebDriverException wex) 
			{
				handleAlerts(false);
				logDEBUG("Webdriver exception while clicking element - " + StringUtil.limitLength(wex.getMessage(), 120) + "...");
				if ( wex.getMessage().contains("JS error"))
				{
					logWARNING("JS error in webdriver exception, retrying... exception: " + wex.getMessage() );
					count++;
					continue;
				}
				else if ( wex.getMessage().contains("stale element") )
				{
					logWARNING("Stale Element exception, retrying...  exception: " + StringUtil.limitLength(wex.getMessage(), 120));
					count++;
					continue;
				}
				else if ( wex.getMessage().contains("element not visible") && isElementVisible(elementToClick ))
				{
					logWARNING("Element not visible error in webdriver exception, retrying... exception: " + wex.getMessage() );
					count++;
					continue;
				}
				else
				{
					try {
						clickUsingJavaScript(elementToClick);
						return true;
					} catch (Exception e) {
						handleAlerts(false);
						e.printStackTrace();
						logWARNING(e.getMessage());
						count++;
						continue;
					}
				}
				
				/*
				Dimension elementSize = elementToClick.getSize();
				Dimension viewPortSize = getViewPortSize();
				if ( elementSize.height > viewPortSize.height ||  elementSize.width > viewPortSize.width )
				{
					clickUsingJavaScript(elementToClick);
					return true;
				}
				*/
//				else if ( wex.getMessage().contains("unknown error"))
//				{
//					logWARNING("Unknown error in webdriver exception, retrying... exception: " + wex.getMessage() );
//					count++;
//					continue;
//				}
				
				
//				else
//				{
//					throw wex;
//				}
			}
		}
		
		if ( count == maxStaleElementRetries && !isSuccess)
		{
			try {
				clickUsingJavaScript(elementToClick);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				logWARNING(e.getMessage());
			}
		}
		return isSuccess;
	}
	
	
	/**
	 * @author basavar
	 * @param elementToClick
	 */
	private void clickUsingJavaScript(WebElement elementToClick)
	{
		logDEBUG("Clicking by JS execution.. ");
		Object executeJavaScript = executeJavaScript("return arguments[0].click()", elementToClick);
		if ( null != executeJavaScript )
		{
			logINFO("JS Click result : " +  (String)executeJavaScript);
		}
	}
	
	/**
	 * Clicks element by js execution - arguments[0].click()
	 * @author basavar
	 * @param page
	 * @param element
	 */
	@Override
	public void clickUsingJavaScript(String page, String element) 
	{
		By by = locator.getLocator(page, element);
		WebElement elementToClick = driver.findElement(by);
		logDEBUG("Clicking by JS execution.. ");
		Object executeJavaScript = executeJavaScript("return arguments[0].click()", elementToClick);
		if ( null != executeJavaScript )
		{
			logINFO("JS Click result : " +  (String)executeJavaScript);
		}
	}
	
	/**
	 * Gets Viewports size </br>
	 * Note that view port size is not same as window size
	 * @author basavar
	 * @return
	 */
	public Dimension getViewPortSize()
	{
		Long height = (Long) executeJavaScript("return document.documentElement.clientHeight;");
		Long width = (Long) executeJavaScript("return document.documentElement.clientWidth;");
		return new Dimension(width.intValue(), height.intValue());
	}
	
	/**
	 * Bug in webdriver, always returns true unless there is style attribute with disabled=true
	 * @author basavar
	 * @param page
	 * @param element
	 * @return
	 */
	@Override
	public boolean isElementEnabled(String page, String element)
	{
		boolean enabled = false;
		By by = locator.getLocator(page, element);
		try {
			WebElement uiElement = driver.findElement(by);
			enabled = uiElement.isEnabled();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception while finding element enabled or not : " + page + "->" + element + " Error: " + StringUtil.limitLength(e.getMessage(), 120) );
		}
		return enabled;
	}
	
	/**
	 * Verifies the element is present and visible. Visibility is checked based on CSS properties visibility, opacity, display and 
	 * also elements location wrt to window location is considered.
	 * @author basavar
	 * @param uiElement
	 */
	private boolean isElementVisible(WebElement uiElement )
	{
		/*
		 * to -do USE getCss property / 
		 *  1. display : none - should not be
		 *  2. visibility : hidden - should not be
		 *  3. Opacity should be > 0
		 *  4. Point of element should be within viewport boundary
		 */
		boolean displayed = false;
		try {
			displayed = uiElement.isDisplayed();
			
			String display = uiElement.getCssValue("display");
			if ( display.equals("none"))
			{
				logDEBUG("UI element hidden with display:none");
				return false;
			}
			String visibility = uiElement.getCssValue("visibility");
			if ( visibility.equals("hidden") || visibility.equals("collapse"))
			{
				logDEBUG("UI element hidden with visibility:" + visibility );
				return false;
			}
			String opacity = uiElement.getCssValue("opacity");
			Float opacityF = 0.5f;
			try {
				opacityF = Float.valueOf(opacity);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			if ( opacityF <= 0)
			{
				logDEBUG("UI element hidden with opacity:" + opacity);
				return false;
			}
			
			Point elementLocation = uiElement.getLocation();
			
			/*
			 * Allowing -5 for y, because i see an example of hamburger icon with y:-3, still visible and works fine
			 */
			if ( elementLocation.getX() < 0 || elementLocation.getY() < -5 )
			{
				logDEBUG("UI element is outside view port, with negative co-ordinates : " + elementLocation.getX() + "," + elementLocation.getY() );
				return false;
			}
			
			Dimension elementSize = uiElement.getSize();
			if ( elementSize.height == 0 || elementSize.width == 0 )
			{
				logDEBUG("UI element is not visible with zero size height:" + elementSize.getHeight() + ", width:" + elementSize.getWidth() );
				return false;
			}
			
			// Sometimes the element size is in 1000's of pixels, for scrollable pages/elments.
			// Example, cuttlefish clicked srp - yui-bd-mask
			/*
			 *  if ( ! iPhone )
			Dimension elementSize = uiElement.getSize();
			Point windowPosition = driver.manage().window().getPosition();
			Dimension windowSize = driver.manage().window().getSize();
			if ( ( elementLocation.getX() + elementSize.width ) > ( windowPosition.getX() + windowSize.getWidth() )  ||
					( elementLocation.getY() + elementSize.height ) > ( windowPosition.getY() + windowSize.getHeight() )  )
			{
				logDEBUG("UI element is outside view port, with elementLocation: " + elementLocation.getX() + "," + 
						elementLocation.getY() + " elementSize: " + elementSize.getWidth() + "," 
						+ elementSize.getHeight() + " windowLocation:" + windowPosition.getX()+","+windowPosition.getY() 
						+ " windowSize:" + windowSize.width + "," + windowSize.height );
				return false;
			}
			*/
		  } catch (WebDriverException wex) {
			wex.printStackTrace();
			fail("Exception while finding element visible or not! Error: " + StringUtil.limitLength(wex.getMessage(), 120) );
		 } catch ( Exception e ) {
			e.printStackTrace();
		}
		return displayed;
	}
	
	/**
	 * Verifies the element is present and visible. Visibility is checked based on CSS properties visibility, opacity, display and 
	 * also elements location wrt to window location is considered.
	 * @author basavar
	 * @param by - by locator
	 */
	private boolean isElementVisible(By by)
	{
		boolean displayed = false;
		try {
			WebElement uiElement = driver.findElement(by);
			displayed = isElementVisible(uiElement);
		  } catch (WebDriverException wex) {
			wex.printStackTrace();
			logWARNING("Exception while finding element visible or not! Error: " + StringUtil.limitLength(wex.getMessage(), 120) );
			return false;
		 }
		return displayed;
	}
	
	/**
	 * Verifies the element is present and visible. Visibility is checked based on CSS properties visibility, opacity, display and 
	 * also elements location wrt to window location is considered.
	 * @author basavar
	 * @param page
	 * @param element
	 */
	@Override
	public boolean isElementVisible(String page, String element)
	{
		By by = locator.getLocator(page, element);
		return isElementVisible(by);
	}
	
	/**
	 * Returns the count of occurrences of the element on the current page
	 * @author basavar
	 * @param page
	 * @param element
	 */
	@Override
	public int findNumberOfOccurrences(String page, String element) 
	{
		By by = locator.getLocator(page, element);
		try {
			List<WebElement> elementList = driver.findElements(by);
			if ( null != elementList )
			{
				return elementList.size();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * Gets text of the element located by page>element
	 * @author basavar
	 * @param page
	 * @param element
	 * @param fail - if true, then calling testcase will be failed if text could not be read. 
	 * 				Else, null will be returned without failing calling test case. 
	 */
	@Override
	public String getText(String page, String element, boolean fail)
	{
		String result = null;
		By by = locator.getLocator(page, element);
		try {
			WebElement webElement = driver.findElement(by);
			result = getTextOfElement(webElement, IConstants.MAX_STALE_ELEMENT_RETRIES);
		} catch (Exception e) {
			e.printStackTrace();
			if ( fail )
			{
				fail("Could not get element text, element: " + by.toString() + " Reason: " +  
						StringUtil.limitLength(e.getMessage(), 120) );
			}
		}
		return result;
	}
	
	/*
	 * Gets text, trying every possible way, including reading the html itself  
	 */
	private String getTextOfElement( WebElement element, int max_stale_retries )
	{
		String result = null;
		int count = 0;
		
		while ( count < max_stale_retries )
		{
			try {
				result = element.getAttribute("value");
				if ( null == result ||  ( null != result && result.compareTo("0") == 0 ) || result.trim().length() < 1 )
				{
					result = element.getText();
					if ( null == result ||  ( null != result && result.compareTo("0") == 0 ) || ( null != result && result.trim().length() < 1 ) )
					{
						result = element.getAttribute("data-text");
					}
					/*
					 * Getting text from reading the innerHTML of the element node itself
					 */
					if ( null == result ||  ( null != result && result.compareTo("0") == 0 ) || ( null != result && result.trim().length() < 1 ) )
					{
						String htmlContentOfElementNode = getInnerHTML(element);
						result = htmlContentOfElementNode.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ");
						return result;
					}
				}
				count = count + max_stale_retries;
				
			} catch (StaleElementReferenceException e) {
				count++;
			}
		}
		
		return result;
	}
	
	/**
	 * Gets innerHTML of the element
	 * @author basavar
	 * @param element
	 * @return
	 */
	private String getInnerHTML(WebElement element)
	{
		return (String) executeJavaScript("return arguments[0].innerHTML;", element);
	}
	
	/**
	 * Gets text of 'n'th occurence of element </br>
	 * Useful for search assist similar  conditions
	 * 
	 * @author basavar
	 * @param page
	 * @param element
	 */
	@Override
	public String getTextNthElement(String page, String element, int n)
	{
		String result = null;
		By by = locator.getLocator(page, element);
		try {
			List<WebElement> webElements = driver.findElements(by);
			if ( webElements.size() < n )
			{
				fail("Cannot get text of " + n + "th element, because there are only " + webElements.size() + " elements! locator: " + by.toString() );
			}
			WebElement elementToRead = webElements.get( n - 1);
			result = getTextOfElement(elementToRead, IConstants.MAX_STALE_ELEMENT_RETRIES);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not get element text, element: " + by.toString() + " Reason: " + StringUtil.limitLength(e.getMessage(), 120) );
		}
		return result;
	}
	
	/**
	 * Returns current URL
	 * @author basavar
	 * @return
	 */
	@Override
	public String getCurrentURL() 
	{
		return driver.getCurrentUrl();
	}
	
	/**
	 * Executes js with uiElement as an argument
	 * 
	 * @author basavar
	 * @param script
	 * @param uiElementTarget
	 * @return
	 */
	private Object executeJavaScript(String script, WebElement uiElementTarget)
	{
		JavascriptExecutor executor = (JavascriptExecutor) driver;
	    Object executeScript = (java.lang.String) executor.executeScript(script, uiElementTarget);
	    return executeScript;
	}
	
	/**
	 * Executes javascript
	 * 
	 * @author basavar
	 * @param script
	 * @return
	 */
	@Override
	public Object executeJavaScript(String script)
	{
		JavascriptExecutor executor = (JavascriptExecutor) driver;
	    Object executeScript = executor.executeScript(script);
	    return executeScript;
	}
	
	/**
	 * Waits for page load, based on document.readyState
	 * @author basavar
	 */
	@Override
	public void waitForPage() 
	{
		boolean readyStateComplete = false;
		while (!readyStateComplete) {
			String executeScript = (String) executeJavaScript("return document.readyState");
//		    System.out.println(executeScript);
		    readyStateComplete = executeScript.equalsIgnoreCase("complete");
		}
	}
	
	private WebElement getUIElement(String page, String element)
	{
		By by = locator.getLocator(page, element);
		WebElement findElement = null;
		try {
			findElement = driver.findElement(by);
		} catch (Exception e) {
			e.printStackTrace();
			fail( "Element not found " + page + ">" + element + "By:" +by.toString() + " Reason: " + StringUtil.limitLength(e.getMessage(), 120));
		}
		return findElement;
	}

	/**
	 * 
	 * @param page
	 * @param element
	 */
	@Override
	public void submit(String page, String element) 
	{
		try {
			WebElement uiElement = getUIElement(page, element);
			uiElement.submit();
		} catch (WebDriverException e) {
			e.printStackTrace();
			fail("Failed to submit on element : " + page + ">" + element + " Reason: " + StringUtil.limitLength(e.getMessage(), 120));
		}
	}
	
	/**
	 * 
	 */
	@Override
	public String scrollToBottom() 
	{
		String scrollDownJS = "return window.scroll(0, document.body.scrollHeight)";
		String result = (String) executeJavaScript(scrollDownJS);
		return result;
	}
	
	/**
	 * 
	 */
	@Override
	public String scrollToTop() 
	{
		String scrollDownJS = "return window.scroll(0,0)";
		String result = (String) executeJavaScript(scrollDownJS);
		return result;
	}
	
	/**
	 * 
	 */
	@Override
	public boolean scrollToElement(String page, String element)
	{
		WebElement uiElement = getUIElement(page, element);
		try {
			Actions builder = new Actions(driver);
			builder.moveToElement(uiElement);
			builder.perform();
		} catch (WebDriverException e) {
			e.printStackTrace();
			return false;
		}
		logINFO("Scrolled to " + page + ">" + element );
		return true;
	}

	/**
	 * 
	 * @param page
	 * @param element
	 * @param property
	 * @return
	 */
	@Override
	public String getCSSProperty(String page, String element, String property) 
	{
		WebElement uiElement = getUIElement(page, element);
		String attribValue = uiElement.getAttribute(property);
		logDEBUG( "Found attrib value, " + page + ">" + element + ":'" + property + "'=" + attribValue );
		return attribValue;
	}

	/**
	 * 
	 * @param page
	 * @param element
	 * @param timeOutSeconds
	 */
	@Override
	public void waitForElement(String page, String element, int timeOutSeconds) 
	{
		WebDriverWait wait = new WebDriverWait(driver, timeOutSeconds);
		wait.until(ExpectedConditions.visibilityOfElementLocated( locator.getLocator(page, element)));
	}
	
	/**
	 * Gets values of the <code>attribute</code> for a/all element/s located by page->element
	 * @param page
	 * @param element
	 * @param attribute
	 */
	public List<String> getAttributeValues(String page, String element, String attribute) 
	{
		List<String> result = new ArrayList<String>();
		By by = locator.getLocator(page, element);
		List<WebElement> elementList = null;
		try {
			elementList = driver.findElements(by);
		} catch (Exception e) {
			e.printStackTrace();
			fail( "Element not found " + page + ">" + element + "By:" +by.toString() + " Reason: " + StringUtil.limitLength(e.getMessage(), 120));
		}
		
		for ( WebElement ele : elementList )
		{
			String value = ele.getAttribute(attribute);
			if ( null != value )
			{
				result.add(value);
			}
		}
		return result;
	}
	
	/**
	 * Gets values of the <code>attributesList</code> for all/a element/s located by page->element
	 * @param page
	 * @param element
	 * @param attribute
	 * @return - returns list of maps containing all values of attributesList keys, for all found elements located.
	 */
	@Override
	public List<HashMap<String, String>> getAttributesValues(String page, String element, List<String> attributesList) 
	{
		By by = locator.getLocator(page, element);
		List<WebElement> elementList = null;
		try {
			elementList = driver.findElements(by);
		} catch (Exception e) {
			e.printStackTrace();
			fail( "Element not found " + page + ">" + element + "By:" +by.toString() + " Reason: " + StringUtil.limitLength(e.getMessage(), 120));
		}
		
		List<HashMap<String, String>> resultingListOfMaps = new ArrayList<HashMap<String, String>>();
		for ( WebElement ele : elementList )
		{
			HashMap<String, String> mapOfValues = new HashMap<String, String>();
			for ( String attrib : attributesList )
			{
				String value = ele.getAttribute(attrib);
				if ( null != value )
				{
					mapOfValues.put(attrib, value);
				}
			}
			if ( ! mapOfValues.isEmpty() )
			{
				resultingListOfMaps.add(mapOfValues);
			}
		}
		return resultingListOfMaps;
	}
	
	/**
	 * Returns <code>attribute</code>'s value for the element located by page->element, 'elementIndex'th element is considered if 
	 * there are many elements located by same locator. If a single element is expected to be located, send <code>elementIndex</code>=0
	 * @param page		- "SRP"
	 * @param element   - "pivotBarAllLinks"
	 * @param attribute - example: href, data-bak,..
	 * @param elementIndex - >= 0
	 */
	@Override
	public String getAttributeValue(String page, String element, String attribute, int elementIndex)
	{
		By by = locator.getLocator(page, element);
		List<WebElement> elementList = null;
		try {
			elementList = driver.findElements(by);
		} catch (Exception e) {
			e.printStackTrace();
			fail( "Element not found " + page + ">" + element + "By:" +by.toString() + " Reason: " + StringUtil.limitLength(e.getMessage(), 120));
		}
		
		if ( elementIndex < 0 || elementIndex >= elementList.size() )
		{
			logERROR("Invalid index " + elementIndex + " ; expected between 0 to " + (elementList.size()-1) );
			return "Invalid index";
		}
		WebElement webElement = elementList.get(elementIndex);
		String result = webElement.getAttribute(attribute);
		return result;
	}

	/**
	 * Returns null if UISpec validation is passed, else returns failure reasons in buffer
	 * @param page
	 * @param element
	 */
	@Override
	public StringBuffer verifyUISpecForElement(String page, String element) throws UnsupportedOperationException
	{
		StringBuffer buffer = new StringBuffer();
		Map<String, String> uiSpecs = locator.getUISpecs(page,element);
		WebElement uiElement = getUIElement(page, element);
		assertNotNull( uiSpecs, "No specs to verify!");
		assertTrue( uiSpecs.size() > 0 , "No specs to verify!");
		
		Set<String> keySet = uiSpecs.keySet();
		Iterator<String> iterator = keySet.iterator();
		while ( iterator.hasNext() )
		{
			String key = iterator.next();  // font-size
			String actualValue = uiElement.getCssValue(key);
			if ( null == actualValue || ( null != actualValue && actualValue.trim().length() == 0 ) )
			{
				actualValue = uiElement.getAttribute(key);
			}
			String expectedValue = uiSpecs.get(key);
			if ( null == actualValue || null == expectedValue )
			{
				buffer.append(key + ":" + actualValue + " expected:" + expectedValue + " ");
			}
			else if ( ! actualValue.trim().equals(expectedValue.trim() ))
			{
				buffer.append(" " + key + ":" + actualValue + " expected:" + expectedValue + " ");
			}
		}
//		getCSSProperty(page, element, property)
		if ( buffer.length() != 0 )
		{
			fail(page + "->" + element + " : " + buffer.toString());
		}
		return buffer;
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public String getConfigurationString(String key) 
	{
		return configuration.getString(key, null);
	}

	/**
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getConfigurationString(String key, String defaultValue) 
	{
		return configuration.getString(key, defaultValue);
	}
	
	/**
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public Integer getConfigurationInteger(String key, int defaultValue) 
	{
		return configuration.getInteger(key, defaultValue);
	}
	
	/**
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public Boolean getConfigurationBoolean(String key) 
	{
		return configuration.getBoolean(key);
	}

	/**
	 * Navigates to back page
	 */
	@Override
	public void back() 
	{
		driver.navigate().back();
	}
	
	/**
	 * Navigates to back page
	 */
	@Override
	public void refresh() 
	{
		driver.navigate().refresh();
	}

	/**
	 * To be used when page->element points to a list of elements, and specific element to be clicked could be 
	 * identified by an attribute's value. </br>
	 * If index of the specific element is known, then <code>clickElementNth</code> could be used.
	 * @param page
	 * @param element
	 * @param attribute
	 * @param value
	 */
	@Override
	public void clickElementMatchingAttribValue(String page, String element,
			String attribute, String value)
	{
		By by = locator.getLocator(page, element);
		List<WebElement> elements = driver.findElements(by);
		WebElement elementToClick = null;
		for ( WebElement ele: elements )
		{
			String actualValue = ele.getAttribute(attribute);
			if( actualValue.equals(value) )
			{
				elementToClick = ele;
				break;
			}
		}
		elementToClick.click();
	}

	/**
	 * This is returning true all the time, need to find out proper implementation
	 * @param page
	 * @param element
	 * @return
	 */
	
	@Deprecated
	public boolean isElementClickable(String page, String element) 
	{
		By by = locator.getLocator(page, element);
		WebElement ele = driver.findElement(by);
		WebDriverWait wait = new WebDriverWait(driver, 60);
		WebElement clickableElement;
		try {
			clickableElement = wait.until(ExpectedConditions.elementToBeClickable(by));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if ( null != clickableElement )
		{
			return true;
		}
		
		return false;
	}

	/**
	 * Type text / send key's to the element located by page->element
	 * @param page
	 * @param element
	 * @param text
	 */
	@Override
	public void typeTextIn(String page, String element, String text) 
	{
		WebElement uiElement = getUIElement(page, element);
		try {
			uiElement.sendKeys(text);
		} catch (Exception e) {
			e.printStackTrace();
			logERROR("Error while typing in text element: " + page + ">" + element + " text: " + text);
			logERROR("Error " + e.getMessage());
			fail("Error while sending text to element, " + page + ">" + element 
				+ " text: " + text + " Error: " + StringUtil.limitLength(e.getMessage(), 120));
		}
	}

	/**
	 * Close all windows except the window handle passed
	 * @param mainWindow
	 */
	@Override
	public void handlePopups(String mainWindow) 
	{
		Set<String> windowHandles = driver.getWindowHandles();
		if ( windowHandles.size() == 1 )
		{
			return;
		}
			
		Iterator<String> iterator = windowHandles.iterator();
		while ( iterator.hasNext() )
		{
			String next = iterator.next();
			if ( ! next.equals(mainWindow) )
			{
				driver.switchTo().window( next );
				driver.close();
			}
		}
		driver.switchTo().window(mainWindow);
	}
	
	/**
	 * Return the currently selected option in a normal select.
	 * @author surendar
	 * @param page
	 * @param element
	 */
	@Override
	public String getCurrentlySelectedOptionInDropDown(String page, String element)
	{
		By by = locator.getLocator(page, element);
		WebElement webElement = driver.findElement(by);
		Select dropdown = new Select(webElement);
		return dropdown.getFirstSelectedOption().getText();
	}

	/**
	 * Returns current window handle
	 * @return
	 */
	@Override
	public String getCurrentWindowHandle() 
	{
		return driver.getWindowHandle();
	}

	/**
	 * 
	 * @param i
	 */
	@Override
	public void waitForSeconds(int n) 
	{
		try {
			Thread.sleep( n * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Switch to new window/tab openend
	 * @param mainWindow - window handle of main page
	 * @throws Exception
	 */
	public void switchToNewWindow(String mainWindow) throws Exception
	{
		Set<String> windowHandles = driver.getWindowHandles();
		if ( windowHandles.size() == 1 )
		{
			throw new Exception("No new window/tab open! There is only 1 window/tab open!");
		}
		Iterator<String> iterator = windowHandles.iterator();
		while ( iterator.hasNext() )
		{
			String next = iterator.next();
			if ( ! next.equals(mainWindow) )
			{
				driver.switchTo().window( next );
			}
		}
	}

	/**
	 * Close the current window/tab
	 */
	public void closeWindow() 
	{
		driver.close();
	}

	/**
	 * Switch to window with given window handle string
	 * @param windowHandle
	 */
	public void switchToWindow(String windowHandle) 
	{
		driver.switchTo().window(windowHandle);
	}

	/**
	 * Select 'i'th option under Select drop down located by page->element  
	 * @param page
	 * @param element
	 * @param i
	 */
	public String selectOption(String page, String element, int i) 
	{
		Select select = new Select(getUIElement(page, element));
		select.selectByIndex(i);
		WebElement selected = select.getAllSelectedOptions().get(0);
		return getTextOfElement(selected, 3);
	}

	/**
	 * Returns window title
	 */
	public String getWindowTitle() 
	{
		return driver.getTitle();
	}
}
