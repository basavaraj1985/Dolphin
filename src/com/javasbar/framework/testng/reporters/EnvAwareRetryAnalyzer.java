package com.javasbar.framework.testng.reporters;

import org.testng.IRetryAnalyzer;
import org.testng.ITestNGListener;
import org.testng.ITestResult;

import com.javasbar.smart.framework.lib.common.NetworkUtil;

/**
 * @author Basavaraj M
 *
 */
public class EnvAwareRetryAnalyzer implements IRetryAnalyzer , ITestNGListener 
{
	private int retryCount = 0;
	private int maxRetryCount ;
	private int retryWaitSeconds;
	
	/**
	 * If a testcase is failed and if baseUrl is not pingable, retry the test case.
	 */
	@Override
	public boolean retry(ITestResult testResult) 
	{
		String maxRetryStr = (String) testResult.getTestContext().getAttribute("maxRetryCount");
		String maxRetryWaitSecondsStr = (String) testResult.getTestContext().getAttribute("retryWaitSeconds");
		if ( null != maxRetryStr )
		{
			try {
				maxRetryCount = Integer.valueOf(maxRetryStr);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				maxRetryCount = 3;
			}
		}
		if ( null != maxRetryWaitSecondsStr )
		{
			try {
				retryWaitSeconds = Integer.valueOf(maxRetryWaitSecondsStr);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				retryWaitSeconds = 180;
			}
		}
		
		System.out.println("@EnvAwareRetryAnalyzer retry");
		if ( ! testResult.isSuccess() && ( retryCount++ < maxRetryCount ))
		{
			String url = (String) testResult.getTestContext().getAttribute("baseUrl");
			if ( null == url ) 
			{
				System.getProperty("baseUrl", "http://google.com");
			}
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
