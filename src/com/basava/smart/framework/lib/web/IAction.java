package com.basava.smart.framework.lib.web;

import java.util.HashMap;
import java.util.List;

/**
 * @author Basavaraj M
 * This is the interface that should be implemented by the class/library that provides interactions with browser/application under test.
 */
public interface IAction
{
	/**
	 * Open Search result page for the query
	 * @param query
	 */
	public void openSRPForQuery(String query);
	
	/**
	 * 
	 * @param query
	 * @param handleAlerts
	 */
	public void openSRPForQuery(String query, boolean handleAlerts);
	
	/**
	 * 
	 * @param url
	 */
	public long openPage(String url);
	
	/**
	 * 
	 */
	public  void gotoBaseUrl();
	
	/**
	 * 
	 * @param accept
	 */
	public void handleAlerts(boolean accept);

	/**
		 * Checks if element is present
		 * @author basavar
		 * @param page
		 * @param element
		 * @return true if web-element is present on the current page, else false. 
		 */
	public boolean isElementPresent(String page, String element);
		
		
		/**
		 * Clicks on element
		 * @author basavar
		 * @param page
		 * @param element
		 */
		public void clickElement(String page, String element);
		
		
		/**
		 * To be used when there are multiple occurrences of an element. </br>
		 * Example: There are 10 checkboxes with same locator expression, and 5th one needs to be clicked. </br>
		 * Pass 'page', 'checkBoxElement', 5.
		 * @author basavar
		 * @param page
		 * @param element
		 * @param n  n > 0 
		 */
		public void clickElementNth(String page, String element, int n);
		
		
		/**
		 * Clicks element by js execution - arguments[0].click()
		 * @author basavar
		 * @param page
		 * @param element
		 */
		public void clickUsingJavaScript(String page, String element);
		
		/**
		 * Bug in webdriver, always returns true unless there is style attribute with disabled=true
		 * @author basavar
		 * @param page
		 * @param element
		 * @return
		 */
		public boolean isElementEnabled(String page, String element);
		
		/**
		 * Verifies the element is present and visible. Visibility is checked based on CSS properties visibility, opacity, display and 
		 * also elements location wrt to window location is considered.
		 * @author basavar
		 * @param page
		 * @param element
		 */
		public boolean isElementVisible(String page, String element);

		/**
		 * Returns the count of occurrences of the element on the current page
		 * @author basavar
		 * @param page
		 * @param element
		 */
		public int findNumberOfOccurrences(String page, String element);
		
		/**
		 * Gets text of the element located by page>element
		 * @author basavar
		 * @param page
		 * @param element
		 * @param fail - if true, then calling testcase will be failed if text could not be read. 
		 * 				Else, null will be returned without failing calling test case. 
		 */
		public String getText(String page, String element, boolean fail);
		
		/**
		 * Gets text of 'n'th occurence of element </br>
		 * Useful for search assist similar  conditions
		 * 
		 * @author basavar
		 * @param page
		 * @param element
		 */
		public String getTextNthElement(String page, String element, int n);
		
		/**
		 * Returns current URL
		 * @author basavar
		 * @return
		 */
		public String getCurrentURL();
		
		/**
		 * Executes javascript
		 * 
		 * @author basavar
		 * @param script
		 * @return
		 */
		public Object executeJavaScript(String script);
		
		/**
		 * Waits for page load, based on document.readyState
		 * @author basavar
		 */
		public void waitForPage();
		
		/**
		 * 
		 * @param page
		 * @param element
		 */
		public void submit(String page, String element);
		
		/**
		 * 
		 * @return
		 */
		public String scrollToBottom(); 
		
		/**
		 * 
		 * @return
		 */
		public String scrollToTop(); 
		
		/**
		 * 
		 * @param page
		 * @param element
		 * @return
		 */
		public boolean scrollToElement(String page, String element);
		
		/**
			 * 
			 * @param page
			 * @param element
			 * @param property
			 * @return
		 */
		public String getCSSProperty(String page, String element, String property);
		
		/**
		 * 
		 * @param page
		 * @param element
		 * @param timeOutSeconds
		 */
		public void waitForElement(String page, String element, int timeOutSeconds);
		
		/**
		 * Gets values of the <code>attributesList</code> for all/a element/s located by page->element
		 * @param page
		 * @param element
		 * @param attribute
		 * @return - returns list of maps containing all values of attributesList keys, for all found elements located.
		 */
		public List<HashMap<String, String>> getAttributesValues(String page, String element, List<String> attributesList);
		
		/**
		 * Returns <code>attribute</code>'s value for the element located by page->element, 'elementIndex'th element is considered if 
		 * there are many elements located by same locator. If a single element is expected to be located, send <code>elementIndex</code>=0
		 * @param page		- "SRP"
		 * @param element   - "pivotBarAllLinks"
		 * @param attribute - example: href, data-bak,..
		 * @param elementIndex - >= 0
		 */
		public String getAttributeValue(String page, String element, String attribute, int elementIndex) ;
		
		/**
			 * 
			 * @param page
			 * @param element
			 */
		public StringBuffer verifyUISpecForElement(String page, String element) throws UnsupportedOperationException;	
		
		/**
		 * Navigates to back page
		 */
		public void back();
		
		/**
		 * Navigates to back page
		 */
		public void refresh();
		
		/**
		 * To be used when page->element points to a list of elements, and specific element to be clicked could be 
		 * identified by an attribute's value. </br>
		 * If index of the specific element is known, then <code>clickElementNth</code> could be used.
		 * @param page
		 * @param element
		 * @param attribute
		 * @param value
		 */
		public void clickElementMatchingAttribValue(String page, String element,
				String attribute, String value);
				
		/**
			 * 
			 * @param page
			 * @param element
			 * @return
			 */
		public boolean isElementClickable(String page, String element);
		
		/**
		 * Type text / send key's to the element located by page->element
		 * @param page
		 * @param element
		 * @param text
		 */
		public void typeTextIn(String page, String element, String text); 

		/**
		 * Close all windows except the window handle passed
		 * @param mainWindow
		 */
		public void handlePopups(String mainWindow);
		
		/**
		 * Returns current window handle
		 * @return
		 */
		public String getCurrentWindowHandle();
		
		/**
		 * 
		 * @param i
		 */
		public void waitForSeconds(int n); 
		
		public String getCurrentlySelectedOptionInDropDown(String page, String element);
		/**
		 * Returns text of first selected option in dropDown(Select tag)
		 * @return
		 */
}
