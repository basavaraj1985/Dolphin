package com.basava.framework.lib.xmlhandling;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * @author - found on internet and improved
 */
public class XMLParser {

	private DocumentBuilderFactory factory;
	
	private DocumentBuilder builder;
	
	private XPath xPath;
	
	public XMLParser() {	
		factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(false);
		try {
			factory.setFeature("http://apache.org/xml/features/dom/defer-node-expansion",false);
		} catch (ParserConfigurationException pe) {
			System.err.println("Could not set the parser feature");
			pe.printStackTrace();
		}
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Failed to init XMLParser.", e);
		}
		xPath =  XPathFactory.newInstance().newXPath();
	}
	
	public Document parse(File file) throws SAXException, IOException {
		return builder.parse(file);
	}
	
	public Document parse(String file) throws SAXException, IOException {
		File f = new File(file);
		return parse(f);
	}
	
	public Document parse(InputStream is) throws SAXException, IOException {
		return builder.parse(is);
	}
	
	public Document parse(URL url) throws IOException, SAXException {
		InputStream in = new BufferedInputStream(url.openStream());
		return parse(in);
	}
	
	public XPathExpression getXpathExpression(String expression) throws XPathExpressionException {
		return xPath.compile(expression);
	}
	
	public NodeList getXpathNodes(Object parentNode, String xpathExpression) throws XPathExpressionException {
		XPathExpression xpath = getXpathExpression(xpathExpression);
		return (NodeList) xpath.evaluate(parentNode, XPathConstants.NODESET);
	}
	
	/**
	 * Returns map of all the node values, attribute values for given xpath expression
	 * @param xpathExpression
	 * @param parsedDocument
	 * @return
	 */
	public Map<String, String> getValuesForXpathExpressions(String xpathExpression, Document parsedDocument) 
	{
		Map<String, String> resultMap = null;
		XPath xPath = XPathFactory.newInstance().newXPath();
		try {
			NodeList nodes = (NodeList) xPath.compile(xpathExpression).evaluate(parsedDocument, XPathConstants.NODESET);
			if ( null != nodes & nodes.getLength() > 0 )
			{
				resultMap = new HashMap<String, String>();
				for ( int i = 0 ; i < nodes.getLength() ; i++ )
				{
					Node item = nodes.item(i);
					String key = item.getNodeName();
					String value = item.getTextContent();
					System.out.println(key + " : " + value );
					if ( resultMap.get(key)!= null )
					{
						String existing = resultMap.get(key);
						resultMap.put(key, existing + "," + value);
					}
					else
					{
						resultMap.put(key, value);
					}

					NamedNodeMap nameNodesMap = item.getAttributes();
					for( int j=0 ; j < nameNodesMap.getLength() ; j++ )
					{
						Node attribute = nameNodesMap.item(j);
						String attributeName = attribute.getNodeName();
						String attributeValue = attribute.getNodeValue();
						if ( resultMap.get(key+"/"+attributeName) != null )
						{
							String existing = resultMap.get(key+"/"+attributeName);
							resultMap.put(key+"/"+attributeName, existing + "," + attributeValue);
						}
						else
						{
							resultMap.put(key+"/"+attributeName, attributeValue);
						}
						System.out.println(key+"/"+attributeName + " : " + attributeValue );
					}
				}
			}
		} catch (XPathExpressionException e) {
			;
		} catch (NullPointerException ne){
		}
		return resultMap;
	}
}
