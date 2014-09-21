package com.basava.framework.lib.xmlhandling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.openqa.selenium.By;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Basavaraj M
 *
 */
public class LocatorHelper 
{
	private XMLParser parser = new XMLParser();
	private File locatorFile ;
	private Document parsedDocument;
	
	public LocatorHelper(String file) throws SAXException, IOException 
	{
		locatorFile = new File(file);
		if ( !locatorFile.exists() )
		{
			throw new FileNotFoundException("File not present : " + locatorFile.getAbsolutePath() );
		}
		parsedDocument = parser.parse(locatorFile);
	}
	
	/**
	 * 
	 * @param page
	 * @param element
	 * @param which
	 * @return
	 */
	public String getLocatorExpression(String page, String element, String which)  
	{
		String result = null;
		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression =  "/pages/page[@name=\"" + page + "\"]//uielements/element[@name=\"" + element + "\"]" ;//"/Employees/Employee[@emplid='3333']/email";
		String cssResult = null;
		try {
			cssResult = xPath.compile(expression + "/CSSLocator").evaluate(parsedDocument);
		} catch (XPathExpressionException e) {
			;
		}
		
		String xpathResult = null;
		{
			try {
				xpathResult = xPath.compile(expression + "/XPathLocator").evaluate(parsedDocument);
			} catch (XPathExpressionException e) {
				;
			}
		}
		
		String classNameResult = null;
		{
			try {
				classNameResult = xPath.compile(expression + "/ClassNameSelector").evaluate(parsedDocument);
			} catch (XPathExpressionException e) {
				;
			}
		}
		
		if ( null == which )
		{
			if ( null != cssResult )
			{
				result = cssResult;
			} else if ( null != xpathResult ) {
				result = xpathResult;
			} else {
				result = classNameResult;
			}
			return result;
		}
		
		if ( which.contains("css"))
		{
				result = cssResult;
		}
		else if ( which.contains("xpath"))
		{
				result = xpathResult;
		}
		else if ( which.contains("class"))
		{
				result = classNameResult;
		}
		return result;
	}
	
	/**
	 * 
	 * @param page
	 * @param element
	 * @return
	 */
	public By getLocator(String page, String element)  
	{
		By by = null;
		String result = null;
		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression =  "/pages/page[@name=\"" + page + "\"]//uielements/element[@name=\"" + element + "\"]" ;//"/Employees/Employee[@emplid='3333']/email";
		try {
			result = xPath.compile(expression + "/CSSLocator").evaluate(parsedDocument);
			by = By.cssSelector(result);
		} catch (XPathExpressionException e) {
			;
		}
		
		if ( null == result || result.trim().length() <1)
		{
			try {
				result = xPath.compile(expression + "/XPathLocator").evaluate(parsedDocument);
				by = By.xpath(result);
			} catch (XPathExpressionException e) {
				;
			}
		}
		
		if ( null == result || result.trim().length() <1 )
		{
			try {
				result = xPath.compile(expression + "/ClassNameSelector").evaluate(parsedDocument);
				by = By.className(result);
			} catch (XPathExpressionException e) {
				;
			}
		}
		
		return by;
	}
	
	/**
	 * 
	 * @param page
	 * @param element
	 */
	public Map<String, String> getUISpecs(String page, String element) 
	{
		Map<String, String> uiMap = null;
		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression =  "/pages/page[@name=\"" + page + "\"]//uielements/element[@name=\"" + element + "\"]" ;
		try {
			NodeList nodes = (NodeList) xPath.compile(expression + "/uispec").evaluate(parsedDocument, XPathConstants.NODESET);
			if ( null != nodes & nodes.getLength() > 0 )
			{
				uiMap = new HashMap<String, String>();
				for ( int i = 0 ; i < nodes.getLength() ; i++ )
				{
					Node item = nodes.item(i);
					NamedNodeMap nameNodesMap = item.getAttributes();
					Node namedItem = nameNodesMap.getNamedItem("name");
					String name = namedItem.getNodeName();
					String key = namedItem.getNodeValue();
					String value = item.getTextContent();
					System.out.println(key + " : " + value );
					uiMap.put(key, value);
				}
			}
		} catch (XPathExpressionException e) {
			;
		} catch (NullPointerException ne){
			System.err.println("Configure elementLocator properly with 'name' attribute for " + page + ">" + element);
		}
		
		return uiMap;
	}
	
	public String getLocatorByXPath(String xpath)  
	{
		String result = null;
		XPath xPath = XPathFactory.newInstance().newXPath();
		try {
			result = xPath.compile(xpath).evaluate(parsedDocument);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return result;
	}
}
