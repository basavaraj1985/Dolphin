[![Build Status](https://travis-ci.org/basavaraj1985/Dolphin.svg?branch=master)](https://travis-ci.org/basavaraj1985/Dolphin.svg?branch=master)

#Features: 

- **Multithreaded** runs with the ability of running _each thread/test on different env/configuration._ 

- **Layered configuration**; i.e configuraton could be overrided by runtime parameter, testng parameter. 

- Easy OO API's. 

- **UI Specification validation automated**, with as less test code as 2 lines requirement. i.e verify all your css/style attributes of elements by configuration and invoking a method. 
    ```
    <element name="SearchBox">
	    <CSSLocator>input#gbqfq</CSSLocator>
	    <XPathLocator>//input[@id="gbqfq"]</XPathLocator>
	    <uispec name="background-color">rgba(0, 0, 0, 0)</uispec>
	    <uispec name="font-size">16px</uispec>
	 </element>
    ```

- **UI Elements location strategy insulated from test code**. i.e. you could provide CSS or XPATH; and mix and match as per convenience. Test code doesnt depend on locator strategy. 

- Default data provider available with parametrizable filenames; i.e. could be driven from configuration. 

- **HTML logging, Smart Delta and failure based reporting** to reduce automation analysis time. HTML logging enables to see **screenshots embedded**, errors in red and more styling. 

- **EnvAwareRetryAnalyser** : if env under test goes down, your _tests will wait upto configurable time before failing_. 

- **JS error are caught and reported automatically** (for firefox browser only). 

- **Google Fighting Layout framework** is integrated, could be enabled with flag fightLayoutBugs=true. 

And much more.. 

1. On iOSSimulator/Android emulator/real devices automation run support - no code change required. Need to work on provisioning profiles for ios. 
2. Multi threaded - parallel execution - so its faster. 
3. Automation framework is Data driven, randomness enabled, easily configurable. 
4. Multiple Locator strategies supported - xpath, css, class, id... 
TestCode is insulated from the headache. Faster to find locators, if xpath is tricky, use css, vice versa. 
5. Configuration is easy and priority based. testng.xml configuration overrides flat file config, if present. 
6. Environment aware RetryAnalyser - helpful to avoid false failures when qa.syc is down. 
7. JS Errors are caught and reported - when ff driver used. 
8. Can run automation on multiple environments at a time parallely. 
9. Screenshots - more useful, are of mobile dimensions, analysed for UI errors. 
10. Smart reporting enabled - lot lesser time in failure analysis. Delta and failure based reports are provided. 
11. Smart reporting with history trend is provided. You can see red delta trend over number of runs. 
12. HTML Logging - flag controlled. The test log itself has screenshots, with steps, debug info, test description.
13. UI Spec validation is implicitly supported. Just provide expected ui specs along with locators and invoke ui spec validation. 
