package com.basava.framework.testng.reporters;

import org.testng.IRetryAnalyzer;
import org.testng.ITestNGListener;
import org.testng.ITestResult;

import com.basava.smart.framework.lib.common.NetworkUtil;

/**
 * @author Basavaraj M
 *
 */
public class EnvAwareRetryAnalyzer implements IRetryAnalyzer , ITestNGListener 
{
	private int retryCount = 0;
	private int maxRetryCount = Integer.valueOf(System.getProperty("maxRetryCount", "3"));
	private int retryWaitSeconds = Integer.valueOf(System.getProperty("retryWaitSeconds", "180"));
	
	/**
	 * If a testcase is failed and if baseUrl is not pingable, retry the test case.
	 */
	@Override
	public boolean retry(ITestResult testResult) 
	{
		System.out.println("@EnvAwareRetryAnalyzer retry");
		if ( ! testResult.isSuccess() && ( retryCount++ < maxRetryCount ))
		{
			String url = System.getProperty("baseUrl", "http://qa.search.yahoo.com");
			if ( ! NetworkUtil.ping( url, 1000) )
			{
				System.out.println("@EnvAwareRetryAnalyzer... retrying failed testcase - " + testResult.getName() );
				System.out.println("Pinging url : " + System.getProperty("baseUrl"));
				// site is down, sleep for 10 minutes MAX and retry
				System.err.println("Website " + url + " is down, poll-waiting for 10 minutes at max!");
				long start = System.currentTimeMillis();
				while ( ( System.currentTimeMillis() - start ) < ( retryWaitSeconds *1000) )
				{
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
					if ( NetworkUtil.ping(  System.getProperty("baseUrl"), 1000 ) ) 
					{
						testResult.setStatus(ITestResult.SKIP);
						return true;
					}
				}
				testResult.setStatus(ITestResult.SKIP);
				return true;
			}
		}
		return false;
	}

}
