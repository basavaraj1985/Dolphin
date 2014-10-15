[![Build Status](https://travis-ci.org/basavaraj1985/Dolphin.svg?branch=master)](https://travis-ci.org/basavaraj1985/Dolphin.svg?branch=master)

#Features: 

- **Multithreaded** runs with the ability of running _each thread/test on different env/configuration._ 

- **Layered configuration**; i.e configuraton could be overrided by runtime parameter, testng parameter. 

- Easy OO API's. 

- **UI Specification validation automated**, with as less test code as 2 lines requirement. i.e verify all your css/style attributes of elements by configuration and invoking a method. 
  example element locator section:
    ```
    <element name="SearchBox">
	    <CSSLocator>input#gbqfq</CSSLocator>
	    <XPathLocator>//input[@id="gbqfq"]</XPathLocator>
	    <uispec name="background-color">rgba(0, 0, 0, 0)</uispec>
	    <uispec name="font-size">16px</uispec>
	    <uispec name="anyCssAttribute">expectedValue</uispec>
	 </element>
    ```

- **UI Elements location strategy insulated from test code**. i.e. you could provide CSS or XPATH; and mix and match as per convenience. Test code doesnt depend on locator strategy. 
  ```
  assertTrue ( actions.isElementPresent("GoogleSRP", "SearchBox"), 
						"Search box is missing in search result page!");
  actions.verifyUISpecForElement("GoogleSRP", "SearchBox");
	
  ```

- Default data provider available with parametrizable filenames; i.e. could be driven from configuration. 
  

- **HTML logging, Smart Delta and failure based reporting** to reduce automation analysis time. HTML logging enables to see **screenshots embedded**, errors in red and more styling. 

- **EnvAwareRetryAnalyser** : if env under test goes down, your _tests will wait upto configurable time before failing_. 

- **JS error are caught and reported automatically** (for firefox browser only), JSErrorCollector is used, 
  see @ https://github.com/mguillem/JSErrorCollector. 

- **Google Fighting Layout framework** is integrated, could be enabled with flag fightLayoutBugs. 
  ```
  fightLayoutBugs=true
  ```
- **Sample code**
  ```
  import org.testng.annotations.Test;
  
  import com.javasbar.framework.testng.reporters.EnvAwareRetryAnalyzer;
  import com.javasbar.smart.framework.WebDriverCommonTest;

  import static org.testng.Assert.assertTrue;

  public class GoogleSearchTest extends WebDriverCommonTest 
  {
	  @Test ( groups= {"smoke"},
		  	retryAnalyzer = EnvAwareRetryAnalyzer.class)
  	public void testSearchBoxPresent()
	  {
		  actions.openSRPForQuery("hello");
  		assertTrue ( actions.isElementPresent("GoogleSRP", "SearchBox"), 
	  					"Search box is missing in search result page!");
  	}
	
	  @Test ( groups= {"functional"},
		  	retryAnalyzer = EnvAwareRetryAnalyzer.class)
  	public void verifyUISpecs()
	  {
		  actions.verifyUISpecForElement("GoogleSRP", "SearchBox");
  	}

	  @Override
  	public String getTestGroupName() {
	  	return "GoogleSearchTest";
  	}
  }
```
- **Sample testng configuration**
  ```
  <suite name="DolphinTests" parallel="tests" thread-count="10" >
	<parameter name="configFile" value="resources/configuration/config.properties" />
	
	<test verbose="5" name="GoogleSRP" annotations="JDK" parallel="classes" thread-count="6" >
		<parameter name="baseUrl" value="http://google.com"/>  
		<parameter name="locatorFile" value="resources/configuration/elementLocators.xml" />
		<!-- supported browsers: chrome, ff, remote,, safari(no UA), -->
		<parameter name="browser" value="phantom"/>		
		<groups>
      	<run>
		 		  <include name="smoke" />
  		 		<include name="functional" />
	  	 		<exclude name="todo" />
		   		<exclude name="notForDesktop" />
      	</run>        
    	</groups>
    	<classes>
    		<class name="sampleTests.GoogleSearchTest"></class>
		</classes>
	</test>
	
	<test verbose="5" name="GoogleSRPMobile" annotations="JDK" parallel="classes" thread-count="6" >
		<parameter name="baseUrl" value="http://google.com"/>
		<parameter name="locatorFile" value="resources/configuration/mobileElementLocators.xml" />
		<parameter name="userAgent" value="Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3"/>
		<parameter name="browserWidth" value="390"/>
		<parameter name="browserHeight" value="780" />
	  <parameter name="browser" value="ff"/>		
		<groups>
      	<run>
		 		  <include name="smoke" />
  		 		<include name="functional" />
	  	 		<exclude name="todo" />
		   		<exclude name="notForMobile" />
      	</run>        
    	</groups>
    	<classes>
    		<class name="sampleTests.GoogleSearchTest"></class>
		</classes>
	</test>
	
	<listeners>
		<listener class-name="com.javasbar.framework.testng.reporters.SmartReporter" />
		<listener class-name="com.javasbar.framework.testng.reporters.EmailableReporter" />
		<listener class-name="com.javasbar.framework.testng.reporters.ProgressReporter" />
		<listener class-name="org.uncommons.reportng.HTMLReporter" />
		<listener class-name="org.uncommons.reportng.JUnitXMLReporter" />
	</listeners> 
</suite>
```
  And much more.. 
  

to summarize ;
1. On iOSSimulator/Android emulator/real devices automation run support - no code change required. Need to work on provisioning profiles for ios. 
2. Multi threaded - parallel execution - so its faster. 
3. Automation framework is Data driven, randomness enabled, easily configurable. 
4. Multiple Locator strategies supported - xpath, css, class, id... 
TestCode is insulated from the headache. Faster to find locators, if xpath is tricky, use css, vice versa. 
5. Configuration is easy and priority based. testng.xml configuration overrides flat file config, if present. 
6. Environment aware RetryAnalyser - helpful to avoid false failures when env/server is down. 
7. JS Errors are caught and reported - when ff driver used. 
8. Can run automation on multiple environments at a time parallely. 
9. Screenshots - more useful, are of mobile dimensions, analysed for UI errors. 
10. Smart reporting enabled - lot lesser time in failure analysis. Delta and failure based reports are provided. 
11. Smart reporting with history trend is provided. You can see red delta trend over number of runs. 
12. HTML Logging - flag controlled. The test log itself has screenshots, with steps, debug info, test description.
13. UI Spec validation is implicitly supported. Just provide expected ui specs along with locators and invoke ui spec validation. 
