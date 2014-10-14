package sampleTests;

import org.testng.annotations.Test;

import com.javasbar.framework.testng.reporters.EnvAwareRetryAnalyzer;
import com.javasbar.smart.framework.WebDriverCommonTest;

import static org.testng.Assert.assertTrue;

/**
 * @author Basavaraj M
 *
 */
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
