package com.javasbar.framework.testng.reporters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import com.javasbar.smart.framework.lib.common.IOUtil;

/**
 * @author Basavaraj M
 *
 */
public class SmartReporter implements IReporter
{
	public static String LOG_LINK = "logLink";
	private static String INTL_KEY = "intl";
	private static String fileName = "briefSummary.properties";
	private static String persistedHistory = "history.csv"; 
	private static String prepend = "";
	private static String intl = "";
	private static String previousStaticReportKey="prev";
	private static File previous = null;
	private static String buildNumber;
	
	private String logLinkFile ;
	
	public SmartReporter()
	{
		super();
		System.out.println("DEBUG: SmartReporter() constructor");
		String prevReport = System.getProperty(previousStaticReportKey, null);
		logLinkFile = System.getProperty(LOG_LINK);
		buildNumber = System.getProperty("build.number", null);
		if ( prevReport != null )
		{
			File prevReportFile = new File(prevReport);
			if ( prevReportFile.exists() )
			{
				System.out.println("SUCCESS: Threshold failures considered!");
				previous = prevReportFile;
			}
			else
			{
				System.err.println("ERROR: Could not locate file : " + prevReportFile.getAbsolutePath());
			}
		}
		
		if ( logLinkFile != null )
		{
			System.out.println("SUCCESS : Log deep linking to smart report considered!");
			try {
				URL url = new URL(logLinkFile);
				URLConnection connection = url.openConnection();
				System.out.println("SUCCESS : Log link verified to be valid!");
			}  catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
			String testNGoutputDirectory) 
	{
		String outputDirectory = "./Reports/MetaData/";
		File dir = new File(outputDirectory);
		if ( !dir.exists() )
		{
			dir.mkdirs();
		}
		StringBuffer buf  = new StringBuffer();
		buf.append("#Smart reporting ....").append("\n");
		buf.append("#outputDir=").append(outputDirectory).append("\n");
		
		intl = System.getProperty(INTL_KEY);
		buf.append("#What did I get from System.getProperty(\"intl\") : " + intl);
		if ( intl != null && intl.trim().length() > 1 )
		{
			fileName = intl + "_" + fileName;
			prepend = intl + "_" ;
		}
		else
		{
			buf.append("#No intl could be retrieved for intl key 'intl' from  env").append("\n");
		}
		buf.append("##intl = " + intl).append("\n");
		deriveAndCompute(suites, buf, outputDirectory);
		buf.append("#Writing file to: " + outputDirectory + "/" + fileName );
		writeToFile( outputDirectory + "/" + fileName, buf.toString() );
	}

	private void deriveAndCompute(List<ISuite> suites, StringBuffer buf, String outputDirectory) 
	{
		List<String> historyLines = null;
		int prevBuildNumber = -1;
		if ( null != intl )
		{
			persistedHistory = intl + "_" + persistedHistory ;
		}
		try {
			historyLines = IOUtil.readAllLinesFromFileAsList(outputDirectory + "/" + persistedHistory, "#" );
			if ( null == historyLines || ( null != historyLines && historyLines.size() < 1) )
			{
				File historyFile = new File(outputDirectory + "/" + persistedHistory);
				BufferedWriter hbw = new BufferedWriter(new FileWriter(historyFile));
				hbw.write("#Smart Reporting - history");
				hbw.write("\n");
				hbw.write(System.getProperty("line.separator"));
				hbw.write("BuildNo, totalRunTCcount, totalRunTestMethodCount, totalPassedMethods, " +
						"totalFailedMethods, totalSkippedMehtods, totalPassedCount, totalFailedCount," +
						" totalSkippedCount, redDeltalCount, greenDeltaCount, redMethodDeltaCount, " +
						"greenMethodDeltaCount");
				hbw.write("\n");
				hbw.write(System.getProperty("line.separator"));
				hbw.flush();
				hbw.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int lastLineNumber = 0 ;
		if ( null != historyLines )
		{
			lastLineNumber = ( historyLines.size() - 1 ) >= 0 ? historyLines.size() - 1 : 0 ;
		}
		if ( lastLineNumber > 0 )
		{
			String lastLine = historyLines.get( lastLineNumber );
			String buildNumberString = lastLine.substring(0, (  lastLine.indexOf(",") == -1  )? 0 : lastLine.indexOf(",") );
			try {
				prevBuildNumber = Integer.valueOf(buildNumberString).intValue();
			} catch (NumberFormatException e) {
				e.printStackTrace();
				System.err.println("Ignoring NumberFormatException : " + e.getMessage() );
			}
		}
		if ( prevBuildNumber < 0 )
		{
			prevBuildNumber = Integer.valueOf(System.getProperty("build.number", "0")).intValue();
		}
		
		int passed = 0, totalPassedMethods = 0, failed = 0, totalFailedMethods = 0, skipped = 0, totalSkippedMethods = 0, total = 0, totalNumberOfMethods = 0;
		StringBuffer includedGroupsBuf = new StringBuffer();
		StringBuffer excludedGroupsBuf = new StringBuffer();
		StringBuffer failedMethodBuff = new StringBuffer();
		StringBuffer passedMethodBuff = new StringBuffer();
		List<String> currentFailList = new ArrayList<String>();
		List<String> currentPassList = new ArrayList<String>();
		Map<String, String> failReasonBasedGrouping = new HashMap<String, String>();
      //failReason, commaSepdCases 
		
		for ( ISuite su : suites )
		{
			for (ISuiteResult sr : su.getResults().values()) 
			{
		        ITestContext testContext = sr.getTestContext();
		        String intlFromCtxt = (String) testContext.getAttribute("intl");
		        if ( null != intlFromCtxt && intlFromCtxt.length() > 1 )
		        {
		        	if( intlFromCtxt.equalsIgnoreCase("qa") ||  intlFromCtxt.equalsIgnoreCase("qa2") )
		        	{
		        		intlFromCtxt = "us";
		        	}
		        }
		        buf.append("#intl from context : " + intlFromCtxt ).append("\n");
		        if( prepend.length() < 1 && intlFromCtxt != null && intlFromCtxt.length() > 0 )
		        {
		        	prepend = intlFromCtxt + "_";
		        }
		        passed += testContext.getPassedTests().size();
		        failed += testContext.getFailedTests().size();
		        skipped += testContext.getSkippedTests().size();
		        
		        String[] includedGroups = testContext.getIncludedGroups();
		        String[] excludedGroups = testContext.getExcludedGroups();
		        for ( String s : includedGroups )
		        {
		        	includedGroupsBuf.append(s).append(",");
		        }
		        for ( String s : excludedGroups )
		        {
		        	excludedGroupsBuf.append(s).append(",");
		        }
		        
		        buf.append("IncludedGroups =" + includedGroupsBuf.toString()).append("\n");
				buf.append("ExcludedGroups =" + excludedGroupsBuf.toString()).append("\n\n");
				
				ITestNGMethod[] allTestMethods = testContext.getAllTestMethods();
				totalNumberOfMethods += allTestMethods.length; 
				
		        IResultMap passedTests = testContext.getPassedTests();
				int passedMethods = getMethodSet(passedTests).size();
				totalPassedMethods += passedMethods;
				Set<ITestResult> allPassedResults = passedTests.getAllResults();
				Iterator<ITestResult> ptestIterator = allPassedResults.iterator();
				while ( ptestIterator.hasNext() )
				{
					ITestResult next = ptestIterator.next();
					String className = next.getMethod().getRealClass().getName();
					String testCaseName = next.getName();
//				    int invocationCount = next.getMethod().getInvocationCount();
				    Object[] parameters = next.getParameters();
				    StringBuffer paraBuff = new StringBuffer();
				    for ( Object obj : parameters )
				    {
				    	if ( obj instanceof String )
				    	{
				    		obj = (String) obj;
				    	} else if ( obj instanceof Integer)
				    	{
				    		obj = (Integer) obj;
				    	}
				    	if ( obj.toString().contains("@") || obj.toString().contains("org.testng") )
				    	{
				    		paraBuff.append(".");
				    		continue;
				    	}
				    	paraBuff.append("\t" + obj.toString()).append(" | ");
				    }
				    String classNMethod = className + "." + testCaseName ;
				    if ( passedMethodBuff.indexOf(classNMethod) == -1 )
				    {
				    	passedMethodBuff.append(classNMethod).append("-->").append(paraBuff).append(",");
				    	currentPassList.add(className + "." + testCaseName + "->" + paraBuff );
				    	passedMethodBuff.append("<br/>");
				    }
				}
				
				int failedMethods = getMethodSet(testContext.getFailedTests()).size();
				totalFailedMethods += failedMethods;
				
				IResultMap failedTests = testContext.getFailedTests();
				Set<ITestResult> allFailedResults = failedTests.getAllResults();
				Iterator<ITestResult> ftestIterator = allFailedResults.iterator();
				while ( ftestIterator.hasNext() )
				{
					ITestResult next = ftestIterator.next();
					String className = next.getMethod().getRealClass().getName();
					String testCaseName = next.getName();
//				    int invocationCount = next.getMethod().getInvocationCount();
				    Object[] parameters = next.getParameters();
				    StringBuffer paraBuff = new StringBuffer();
				    for ( Object obj : parameters )
				    {
				    	if ( obj instanceof String )
				    	{
				    		obj = (String) obj;
				    	} else if ( obj instanceof Integer)
				    	{
				    		obj = (Integer) obj;
				    	}
				    	if ( obj.toString().contains("@") || obj.toString().contains("org.testng") )
				    	{
//				    		paraBuff.append(".");
				    		continue;
				    	}
				    	paraBuff.append( obj.toString()).append(" | ");
				    }
				    
				    String classNMethod = className + "." + testCaseName;
				    if ( failedMethodBuff.indexOf(classNMethod) == -1 )
				    {
				    	failedMethodBuff.append(classNMethod).append("-->").append(paraBuff).append(",");
				    	currentFailList.add(className + "." + testCaseName + "->" + paraBuff );
				    	failedMethodBuff.append("<br/>");
				    }
				    
				    String message = next.getThrowable().getMessage();
				    String toBePut = "<font size=\"=1\">" + className + "</font>" + "<b>." + testCaseName + "</b>"+ "|" + paraBuff.toString() ;
				    if ( null != failReasonBasedGrouping.get(message) )
				    {
				    	String existingListOfFails = failReasonBasedGrouping.get(message);
				    	String listOfFailsUpdated = null;
				    	if ( existingListOfFails.contains(toBePut) )
				    	{
				    		listOfFailsUpdated = existingListOfFails.replace(toBePut, toBePut +"+");
				    	}
				    	else
				    	{
				    		listOfFailsUpdated = existingListOfFails + ",," +  toBePut ;
				    	}
				    	failReasonBasedGrouping.put(message, listOfFailsUpdated);
				    } 
				    else
				    {
				    	failReasonBasedGrouping.put(message, toBePut );
				    }
				}
				
				int skippedMethods = getMethodSet(testContext.getSkippedTests()).size();
				totalSkippedMethods += skippedMethods;
		    }
		}
		total = passed + failed + skipped ;
		
		buf.append("passed=" + passed + "\n");
		buf.append("failed=" + failed + "\n");
		buf.append("skipped="+ skipped + "\n");
		buf.append("total="+total + "\n\n");
		
		buf.append("passedMethods=" + totalPassedMethods + "\n");
		buf.append("failedMethods=" + totalFailedMethods + "\n");
		buf.append("skippedMethods=" + totalSkippedMethods + "\n");
		buf.append("totalMethodCount=" + totalNumberOfMethods+ "\n");
		
		buf.append("FailedMethodDetails =" + failedMethodBuff.toString()).append("\n");
		
		float overallFailPercentage = 0, methodFailPercentage = 0;
		if ( 0 == total )
		{
			total = 1;  // to avoid arithmetic /0 error
		}
		if ( 0 == totalNumberOfMethods )
		{
			totalNumberOfMethods = 1;
		}
		overallFailPercentage = ( ( failed * 100 )/ total );
		methodFailPercentage = ( totalFailedMethods * 100 ) / totalNumberOfMethods ;

		buf.append("OverallFailurePercentage =" + overallFailPercentage ).append("\n");
		buf.append("MethodFailurePercentage =" + methodFailPercentage ).append("\n\n");
		
		
		if ( null == previous )
		{
			buf.append("#Threshold failures NOT taken into account - prev=" + System.getProperty(previousStaticReportKey, null ) ).append("\n");
			previous = new File( outputDirectory + "/" + fileName );
		}
		else
		{
			buf.append("#Threshold failures taken into account - prev=" + System.getProperty(previousStaticReportKey, null ) ).append("\n");
		}
		
		buf.append("#Previous report expected : " + previous.getAbsolutePath() + " Exists: " + previous.getAbsoluteFile().exists() ).append("\n");
		if ( ! previous.getAbsoluteFile().exists() )
		{
			buf.append("buildNumber=1").append("\n");
			buf.append("#No previous report!").append("\n");
			return;
		}
		
		Properties previousProperties = IOUtil.loadFileIntoProperties(previous.getAbsolutePath());
		
		if ( prevBuildNumber == 0 )
		{
			prevBuildNumber = Integer.valueOf(previousProperties.getProperty("buildNumber", "0").trim()).intValue();
		}
		buf.append("buildNumber=" + (prevBuildNumber+1) ).append("\n");
		
		int totalCountDelta = 0, methodCountDelta = 0;
		String pTotalCount = previousProperties.getProperty("total");
		totalCountDelta = total - Integer.valueOf(pTotalCount);
		
		String pMethodTotalCount = previousProperties.getProperty("totalMethodCount");
		methodCountDelta = totalNumberOfMethods - Integer.valueOf(pMethodTotalCount);
		
		buf.append("TotalCountDelta=" + totalCountDelta ).append("\n");
		buf.append("TotalMethodCountDelta=" + methodCountDelta).append("\n");
		
		int redDeltaCount = 0, greenDeltaCount = 0;
		//1. Calculate Red Delta count
		String pFailCount = previousProperties.getProperty("failed");
		redDeltaCount = failed - Integer.valueOf(pFailCount);						
		if ( redDeltaCount < 0 )
		{
			greenDeltaCount = Math.abs(redDeltaCount);
			redDeltaCount = 0;
		}
		
		//2. Calculate Green Delta count
		String pPassCount = previousProperties.getProperty("passed");
		greenDeltaCount = passed - Integer.valueOf(pPassCount);
		if ( greenDeltaCount < 0 )	
		{
//			rDeltaCount = rDeltaCount + Math.abs(gDeltaCount);
			greenDeltaCount = 0;
		}
		
		
		//3. Calculate Red Delta method count
		int redMethodDeltaCount = 0, greenMethodDeltaCount = 0;
		String pFailMethodCount = previousProperties.getProperty("failedMethods");
//		redMethodDeltaCount = totalFailedMethods - Integer.valueOf(pFailMethodCount);
//		if ( redMethodDeltaCount < 0 )
//		{
//			greenMethodDeltaCount = Math.abs(redDeltaCount);
//			redMethodDeltaCount = 0;
//		}
 
		//4. Calculate Green Delta method count
		String pPassMethodCount = previousProperties.getProperty("passedMethods");
		greenMethodDeltaCount = totalPassedMethods - Integer.valueOf(pPassMethodCount);
		if ( greenMethodDeltaCount < 0 )
		{
			greenMethodDeltaCount = 0;
		}
		
		//5. Derive Red Delta methods
		StringBuffer redDeltaMethodList = new StringBuffer();
		redDeltaMethodList.append("<table border=\"2\" cellspacing=\"0\" cellpadding=\"2\" class=\"param\" width=\"100%\">");
		redDeltaMethodList.append("<tr> <th bgcolor=\"red\" colspan=\"2\">Red Delta [PASS-to-FAIL]  Count : " +  "$redMethodDeltaCount" + " </th></tr>");
		redDeltaMethodList.append("<tr bgcolor=\"red\">");
		redDeltaMethodList.append("<th>Test Case</th>");
		redDeltaMethodList.append("<th>Parameters</th>");
		redDeltaMethodList.append("</tr>");
		
		StringBuffer stagnantFailureList = new StringBuffer();
		stagnantFailureList.append("<table border=\"2\" cellspacing=\"0\" cellpadding=\"2\" class=\"param\" width=\"100%\">");
		stagnantFailureList.append("<tr> <th bgcolor=\"red\" colspan=\"2\">Stagnant failures [FAIL-remained-FAIL]</th></tr>");
		stagnantFailureList.append("<tr bgcolor=\"red\">");
		stagnantFailureList.append("<th>Test Case</th>");
		stagnantFailureList.append("<th>Parameters</th>");
		stagnantFailureList.append("</tr>");
		
		StringBuffer failBasedReportingBuff = new StringBuffer();
		failBasedReportingBuff.append("<table border=\"2\" cellspacing=\"0\" cellpadding=\"2\" class=\"param\" width=\"100%\">");
		failBasedReportingBuff.append("<tr> <th bgcolor=\"#F74040\" colspan=\"3\">Failure Based report Count :" +  failReasonBasedGrouping.size() + " </th></tr>");
		failBasedReportingBuff.append("<tr bgcolor=\"#F45C4B\">");
		failBasedReportingBuff.append("<th>Fail Reason</th>");
		failBasedReportingBuff.append("<th>Fail Cases</th>");
		failBasedReportingBuff.append("<th>Fail Count</th>");
		failBasedReportingBuff.append("</tr>");
		
		Set<String> keySet = failReasonBasedGrouping.keySet();
		Iterator<String> failBasedMapKeyIterator = keySet.iterator();
		while ( failBasedMapKeyIterator.hasNext() )
		{
			String failReason = failBasedMapKeyIterator.next(); // is a failReason and a key
			String casesData = failReasonBasedGrouping.get(failReason);
			if ( null != failReason )
			{
				failReason  = failReason.replaceAll("\n", " ").replaceAll("\\n", " ").replaceAll("\r", " ").replaceAll("\\r", " ");
			}
			String[] failCaseList = casesData.split(",,");
			int sameTCFails = countSubstrings(casesData, "+");
			int numberOfFailsForThisReason = failCaseList.length + sameTCFails; 
			
			failBasedReportingBuff.append("<tr>");
			failBasedReportingBuff.append("<td>").append(failReason).append("</td>");
			failBasedReportingBuff.append("<td>");
			String temp = "";
			for ( String testCase : failCaseList )
			{
				// format : classNMethod + " | " + paraBuff.toString() ;
				String[] nameNParas = testCase.split("\\|");
				if ( temp.contains(nameNParas[0]) )
				{
					failBasedReportingBuff.append("+");
					continue;
				}
				temp = temp + nameNParas[0] +",";
				failBasedReportingBuff.append(nameNParas[0]);
				failBasedReportingBuff.append("<br/>");
			}
			failBasedReportingBuff.append("</td>");
			failBasedReportingBuff.append("<td>").append(numberOfFailsForThisReason).append("</td>");
		}
		failBasedReportingBuff.append("</table>");
		
		String pFailedMethodList = previousProperties.getProperty("FailedMethodDetails", "nil");
		// convert to array[class.methodName.count]
		buf.append("PreviousFailedMethodDetails =" + pFailedMethodList).append("\n");
		
		String[] pFailsList = pFailedMethodList.split(",");
		List<String> previousFailsArrList = new ArrayList<String>();
		for ( String f : pFailsList )
		{
			String pCurated = f.replaceAll("-->", "").replaceAll("<br/>", "");
			previousFailsArrList.add(pCurated);
		}
		
		for ( String currentFail : currentFailList )
		{
			if ( pFailedMethodList == null )
			{
				break;
			}
			
			String[] failParts = currentFail.split("->");
			String methodName = failParts[0].substring(failParts[0].lastIndexOf(".")+ 1);
			if( !pFailedMethodList.contains(failParts[0]) )
			{
				redDeltaMethodList.append("<tr>");
//				redDeltaMethodList.append(currentFail);
				if ( null != logLinkFile )
				{
					redDeltaMethodList.append("<td>").append("<a href=\""+ logLinkFile + "#" + methodName + "\" >").append(failParts[0]).append("</a>").append("</td>");
				}
				else 
				{
					redDeltaMethodList.append("<td>").append(failParts[0]).append("</td>");
				}
				if ( failParts.length > 1 )
				{
					redDeltaMethodList.append("<td>").append(failParts[1]).append("</td>");
				}
				else
				{
					redDeltaMethodList.append("<td>").append("-").append("</td>");
				}
				redDeltaMethodList.append("</tr>");
				redMethodDeltaCount++;
			}
			else
			{
				stagnantFailureList.append("<tr>");
				if ( null != logLinkFile )
				{
					stagnantFailureList.append("<td>").append("<a href=\""+ logLinkFile + "#" + methodName + "\" >").append(failParts[0]).append("</a>").append("</td>");
				}
				else 
				{
					stagnantFailureList.append("<td>").append(failParts[0]).append("</td>");
				}
				if ( failParts.length > 1 )
				{
					stagnantFailureList.append("<td>").append(failParts[1]).append("</td>");
				}
				else
				{
					stagnantFailureList.append("<td>").append("-").append("</td>");
				}
				stagnantFailureList.append("</tr>");
			}
		}
		redDeltaMethodList.append("</table>");
		String rDeltaMList = redDeltaMethodList.toString();
		String rDCount = Integer.toString(redMethodDeltaCount);
		System.out.println("============>> redDeltaMethod count is : " + rDCount);
		rDeltaMList = rDeltaMList.replace("$redMethodDeltaCount", rDCount );
		redDeltaMethodList = new StringBuffer();
		redDeltaMethodList.append(rDeltaMList);
		stagnantFailureList.append("</table>");
		
		//6. Derive Green Delta methods - the previous fails which got converted to passes i.e. found in  CurrentPassList but not found in currentFailList
		StringBuffer greenDeltaMethodList = new StringBuffer();
		greenDeltaMethodList.append("<table border=\"2\" cellspacing=\"0\" cellpadding=\"2\" class=\"param\" width=\"100%\">");
		greenDeltaMethodList.append("<tr> <th bgcolor=\"green\">Green Delta [ FAIL-to-PASS ] - " + "$greenMethodDeltaCount" + " </th></tr>");
		
		for ( String previousFail : pFailedMethodList.split(",") )
		{
			String[] prevFailParts = previousFail.split("->");
			String previousFailure = prevFailParts[0]; //
			if( passedMethodBuff.indexOf(previousFailure) != -1 && failedMethodBuff.indexOf(previousFailure) == -1 )
			{
				greenDeltaMethodList.append("<tr><td>");
				greenDeltaMethodList.append(previousFail);
				greenDeltaMethodList.append("</td></tr>");
				greenMethodDeltaCount++;
			}
		}
		greenDeltaMethodList.append("</table>");
		String gDeltaMList = greenDeltaMethodList.toString();
		String gDCount = Integer.toString(greenMethodDeltaCount);
		gDeltaMList = gDeltaMList.replace("$greenMethodDeltaCount", gDCount );
		greenDeltaMethodList = new StringBuffer();
		greenDeltaMethodList.append(gDeltaMList);
		
		buf.append("RedDeltaCountOverall =" + redDeltaCount ).append("\n");
		buf.append("GreenDeltaCountOverall =" + greenDeltaCount ).append("\n");
		buf.append("RedDeltaMethodCount =" + redMethodDeltaCount ).append("\n");
		buf.append("GreenDeltaMethodCount =" + greenMethodDeltaCount ).append("\n").append("\n");
		buf.append("RedDeltaMethods =" + redDeltaMethodList ).append("\n").append("\n");
		buf.append("GreenDeltaMethods =" + greenDeltaMethodList ).append("\n").append("\n");
		buf.append("StagnantFailures =" + stagnantFailureList ).append("\n").append("\n");
		buf.append("FailureBasedReport =" + failBasedReportingBuff ).append("\n").append("\n");
		if ( redMethodDeltaCount > 0 )
		{
			buf.append("X-Priority=1").append("\n");
			buf.append("importance=1").append("\n");
		}
		else if( redMethodDeltaCount == 0 && greenMethodDeltaCount > 0 )
		{
			buf.append("X-Priority=5").append("\n");
			buf.append("importance=5").append("\n");
		}
		else if( failed > 0 )
		{
			buf.append("X-Priority=3").append("\n");
			buf.append("importance=3").append("\n");
		}
		else
		{
			buf.append("X-Priority=5").append("\n");
			buf.append("importance=5").append("\n");
		}
		
		try {
			historyLines = IOUtil.readAllLinesFromFileAsList(outputDirectory + "/" + persistedHistory, "#" );
			if ( null != historyLines )
			{
				File historyFile = new File(outputDirectory + "/" + persistedHistory);
				BufferedWriter hbw = new BufferedWriter(new FileWriter(historyFile, true));
//				hbw.write("BuildNo, totalRunTCcount, totalRunTestMethodCount, totalPassedMethods, " +
//						"totalFailedMethods, totalSkippedMehtods, totalPassedCount, totalFailedCount," +
//						" totalSkippedCount, redDeltalCount, greenDeltaCount, redMethodDeltaCount, " +
//						"greenMethodDeltaCount");
				hbw.write( String.valueOf(prevBuildNumber+1) + "," + String.valueOf( total)  + "," 
						+ String.valueOf(totalNumberOfMethods) + "," + String.valueOf(totalPassedMethods) 
						+ "," + String.valueOf(totalFailedMethods) + "," + String.valueOf(totalSkippedMethods) 
						+ "," + String.valueOf(passed) + "," + String.valueOf(failed) + ","
						+ String.valueOf(skipped) + "," + String.valueOf(redDeltaCount) + "," 
						+ String.valueOf(greenDeltaCount) + "," + String.valueOf(redMethodDeltaCount)
						+ "," + String.valueOf(greenMethodDeltaCount));
				hbw.write("\n");
				hbw.flush();
				hbw.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		System.out.println(countSubstrings("abcabcdababc", "abc"));
		System.out.println(System.getProperty("line.separator") + "555");
	}

	public static int countSubstrings(String inputData, String searchString) 
	{
		if( searchString.length() > inputData.length() )
		{
			return 0;
		}
		
		int count = 0;
		for ( int i=0 ; i < inputData.length() ; i++)
		{
			if ( inputData.indexOf(searchString, i) == i )
			{
				count++;
			}
		}
		return count;
	}

	private Collection<ITestNGMethod> getMethodSet(IResultMap tests) 
	{
	    Set<ITestNGMethod> r = new TreeSet<ITestNGMethod>(new TestSorter<ITestNGMethod>());
	    r.addAll(tests.getAllMethods());
	    return r;
	}	
	
	private class TestSorter<T extends ITestNGMethod> implements Comparator<T> 
	{
		/** Arranges methods by classname and method name */
		public int compare(T o1, T o2) {
			int r = ((T) o1).getTestClass().getName().compareTo(((T) o2).getTestClass().getName());
			if (r == 0) {
				r = ((T) o1).getMethodName().compareTo(((T) o2).getMethodName());
			}
			return r;
		}
	}
	
	/**
	 * @param fileToWrite - in which file to write - filename with location
	 * @param string - what to write
	 */
	public static void writeToFile(String fileToWrite, String stringToWrite)
	{
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter( new FileWriter(fileToWrite, false));
			writer.write(stringToWrite);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not write report! " + e.getLocalizedMessage());
			System.exit(-1);
		}
	}
}
