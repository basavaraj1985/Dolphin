package com.javasbar.framework.testng.reporters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

public class ProgressReporter implements ITestListener
{
	private String templateFile = "resources/progressTemplate1.html";
	public static final String PROGRESS_REPORT_FILE = "Reports/ProgressReport.html";
	public static final String PROGRESS_TEMPLATE_FILE_WRITE_TO = "Reports/progressTemplate.html";

	private Object lock = new Object();
	private List<String> testsInProgress = new ArrayList<String>();
	private List<String> testsPending = new ArrayList<String>();
	private List<String> testsCompleted = new ArrayList<String>();
	private List<String> allFailedTestMethods = new ArrayList<String>();
	private List<String> allPassedTestMethods = new ArrayList<String>();
	private List<String> allSkippedTestMethods = new ArrayList<String>();
	private List<String> progressingTestMethods = new ArrayList<String>();
	private Map<String, String> allFailed = new HashMap<String, String>();
	private VelocityContext context ;
	private Template template =  null;
	String pageTitle = "Test Progress page";
	String refreshPage = 
			"#if ($testsPending.size() > 0 )" +  
		    "<meta http-equiv=\"refresh\" content=\"3\" >" + 
			"#end";
	String head = "<head>" +
		     "<title>" + pageTitle + "</title>" +
		        "<style type=\"text/css\">table caption,table.info_table,table.param,table.passed,table.failed {margin-bottom:10px;border:1px solid #000099;border-collapse:collapse;empty-cells:show;}table.info_table td,table.info_table th,table.param td,table.param th,table.passed td,table.passed th,table.failed td,table.failed th {border:1px solid #000099;padding:.25em .5em .25em .5em}table.param th {vertical-align:bottom}td.numi,th.numi,td.numi_attn {text-align:right}tr.total td {font-weight:bold}table caption {text-align:center;font-weight:bold;}table.passed tr.stripe td,table tr.passedodd td {background-color: #00AA00;}table.passed td,table tr.passedeven td {background-color: #33FF33;}table.passed tr.stripe td,table tr.skippedodd td {background-color: #cccccc;}table.passed td,table tr.skippedodd td {background-color: #dddddd;}table.failed tr.stripe td,table tr.failedodd td,table.param td.numi_attn {background-color: #FF3333;}table.failed td,table tr.failedeven td,table.param tr.stripe td.numi_attn {background-color: #DD0000;}tr.stripe td,tr.stripe th {background-color: #E6EBF9;}p.totop {font-size:85%;text-align:center;border-bottom:2px black solid}div.shootout {padding:2em;border:3px #4854A8 solid} body {background-color:lightpurple}</style>" +
		        "<link rel=\"stylesheet\" type=\"text/css\" href=\"chrome-extension://lfjamigppmepikjlacjdpgjaiojdjhoj/css/menu.css\">" +
		        refreshPage +
		    "</head>" ; 
	String progressTable = 
			"#if ($progressingTestMethods.size() > 0 )" +
			"<table border=\"2\" cellspacing=\"0\" cellpadding=\"2\" class=\"param\" width=\"50%\">" +
    		"<tbody>" +
    		"<tr> " +
    			"<th bgcolor=\"#b39ddb\" colspan=\"1\">Progressing test cases Count : $progressingTestMethods.size() </th>" +
    		"</tr>" +
    		"<tr bgcolor=\"#d1c4e9\">" +
    			"<th>Progressing Test Cases...</th>" +
    		"</tr>" +
    		"#foreach( $pro in $progressingTestMethods )" +
    		"<tr>" +
    			"<td>$pro</td>" +
    		"</tr>" +
    		"#end" +
    		"</tbody>" +
    		"</table>" +
    		"#end";
	String pendingTable =
			"#if ($testsPending.size() > 0 )"+
			"<table border=\"2\" cellspacing=\"0\" cellpadding=\"2\" class=\"param\" width=\"50%\">" +
    		"<tbody>" +
    		"<tr> " +
    			"<th bgcolor=\"#b0bec5\" colspan=\"1\">Pending test cases Count : $testsPending.size() </th>" +
    		"</tr>" +
    		"<tr bgcolor=\"#cfd8dc\">" +
    			"<th>Pending Test Cases</th>" +
    		"</tr>" +
    		"#foreach( $pending in $testsPending )" +
    		"<tr>" +
    			"<td>$pending</td>" +
    		"</tr>" +
    		"#end" +
    		"</tbody>" +
    		"</table>"  + 
    		"#end";
	String failedTable = 
			"#if ( $allFailed.size() > 0 )" +
			"<table border=\"2\" cellspacing=\"0\" cellpadding=\"2\" class=\"param\" width=\"100%\">" +
    		"<tbody>" +
    		"<tr> " +
    			"<th bgcolor=\"#e51c23\" colspan=\"2\">Failed test cases Count : $allFailedTestMethods.size(), Failure Reason Based Count : $allFailed.size() </th>" +
    		"</tr>" +
    		"<tr bgcolor=\"#e84e40\">" +
	    		"<th>Fail Reason</th>" +
    			"<th>Fail Cases</th>" +
    		"</tr>" +
    		"#foreach( $reason in $allFailed.keySet() )" +
    		"<tr>" +
    			"<td>$reason</td>" +
    			"<td>$allFailed.get($reason)<br></td>" +
    		"</tr>" +
    		"#end" +
    		"</tbody>" +
    		"</table>" +
    		"#end";
	String skippedTable = 
			"#if ( $allSkippedTestMethods.size() > 0 )" +
			"<table border=\"2\" cellspacing=\"0\" cellpadding=\"2\" class=\"param\" width=\"50%\">" +
    			"<tbody>" +
    				"<tr> " +
    					"<th bgcolor=\"#ffeb3b\" colspan=\"1\">Skipped test cases Count : $allSkippedTestMethods.size() </th>" +
    				"</tr>" +
    				"<tr bgcolor=\"#fff176\">" +
    					"<th>Skipped Test Cases</th>" +
    				"</tr>" +
    				"#foreach( $skip in $allSkippedTestMethods )" +
    					"<tr>" +
    						"<td>$skip</td>" +
    					"</tr>" +
    				"#end" +
    			"</tbody>" +
    		"</table>" + 
    		"#end";
	String passedTable =
			"#if ( $allPassedTestMethods.size() > 0 ) " +
			"<table border=\"2\" cellspacing=\"0\" cellpadding=\"2\" class=\"param\" width=\"50%\">" +
    		"<tbody>" +
    		"<tr> " +
    			"<th bgcolor=\"#2baf2b\" colspan=\"1\">Passed test cases Count : $allPassedTestMethods.size() </th>" +
    		"</tr>" +
    		"<tr bgcolor=\"#42bd41\">" +
    			"<th>Passed Test Cases</th>" +
    		"</tr>" +
    		"#foreach( $pass in $allPassedTestMethods )" +
    		"<tr>" +
    			"<td>$pass</td>" +
    		"</tr>" +
    		"#end" +
    		"</tbody>" +
    		"</table>" +
    		"#end";
	String customMessageBlock = 
			"$customMessage" ;
	
	StringBuffer customMessage = new StringBuffer().append("<span>...in progress...</span>");
	
	String templateContent = 
		"<html>" +
			head +
			"<body>" +
			"<h1>"+ pageTitle +"</h1>" +
			"<p>" +customMessageBlock + "</p>" +
    			progressTable + "<br>" +
    			pendingTable + "<br>" +
    			failedTable + "<br>" +
    			skippedTable + "<br>" +
    			passedTable + "<br>" +
    		"</body>" + 
    	"</html>";
	
	private long totalActualTimeTaken;
	private long timeTakenByTestMethods;
			
	public ProgressReporter() 
	{
		Properties p = new Properties();
		p.put("runtime.log", "velocity_example.log");
		try {
			Velocity.init(p);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
        context = new VelocityContext();
        context.put("testsInProgress", testsInProgress);
        context.put("testsPending", testsPending);
        context.put("allFailed", allFailed);
        context.put("progressingTestMethods", progressingTestMethods);
        context.put("allSkippedTestMethods", allSkippedTestMethods);
        context.put("allPassedTestMethods", allPassedTestMethods);
        context.put("allFailedTestMethods", allFailedTestMethods);
        context.put("testsCompleted", testsCompleted);
        context.put("testsInProgress", testsInProgress);
        context.put("customMessage", customMessage);
        
        String dir = PROGRESS_REPORT_FILE.substring(0, PROGRESS_REPORT_FILE.lastIndexOf('/'));
        File file = new File(dir);
        if ( !file.exists() )
        {
        	file.mkdirs();
        }
        
        if ( ! new File(templateFile).exists() )
        {
        	File saveTemplateFile  = new File(PROGRESS_TEMPLATE_FILE_WRITE_TO);
        	try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(saveTemplateFile));
				writer.write(templateContent);
				writer.flush();
				writer.close();
				templateFile = PROGRESS_TEMPLATE_FILE_WRITE_TO;
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        try
        {
            template = Velocity.getTemplate(templateFile);
        }
        catch( ResourceNotFoundException rnfe )
        {
            System.out.println("Example : error : cannot find template " + templateFile );
        }
        catch( ParseErrorException pee )
        {
            System.out.println("Example : Syntax error in template " + templateFile + ":" + pee );
        } catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onTestStart(ITestResult result) 
	{
		System.err.println("TestMethod Started :" + getTestMethodNameFromResult(result) );
		progressingTestMethods.add(getTestMethodNameFromResult(result));
		testsPending.remove(getTestMethodNameFromResult(result));
		update();
	}

	@Override
	public void onTestSuccess(ITestResult result) 
	{
		long timeTaken = result.getEndMillis() - result.getStartMillis();
		timeTakenByTestMethods += timeTaken ;
		System.err.println("TestMethod Passed : " + getTestMethodNameFromResult(result) + " time taken : " + timeTaken);
		allPassedTestMethods.add(getTestMethodNameFromResult(result));
		progressingTestMethods.remove(getTestMethodNameFromResult(result));
		testsPending.remove(getTestMethodNameFromResult(result));
		update();
	}

	@Override
	public void onTestFailure(ITestResult result) 
	{
		long timeTaken = result.getEndMillis() - result.getStartMillis();
		timeTakenByTestMethods += timeTaken ;
		System.err.println("TestMethod Passed : " + getTestMethodNameFromResult(result) + " time taken : " + timeTaken);
		allFailedTestMethods.add(getTestMethodNameFromResult(result));
		progressingTestMethods.remove(getTestMethodNameFromResult(result));
		testsPending.remove(getTestMethodNameFromResult(result));
		
		if ( allFailed.containsKey(result.getThrowable().getMessage()))
		{
			allFailed.put(result.getThrowable().getMessage(), allFailed.get(result) + "<br>" + getTestMethodNameFromResult(result));
		} else {
			allFailed.put(result.getThrowable().getMessage(), getTestMethodNameFromResult(result));
		}
		update();
	}

	@Override
	public void onTestSkipped(ITestResult result) 
	{
		System.err.println("TestMethod Skipped : " + getTestMethodNameFromResult(result) );
		allSkippedTestMethods.add(getTestMethodNameFromResult(result));
		progressingTestMethods.remove(getTestMethodNameFromResult(result));
		testsPending.remove(getTestMethodNameFromResult(result));
		update();
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) 
	{
		long timeTaken = result.getEndMillis() - result.getStartMillis();
		timeTakenByTestMethods += timeTaken ;
		System.err.println("TestMethod failed withing Success% : " + result.getTestClass().getName() + "." +result.getMethod().getMethodName());
		testsPending.remove(getTestMethodNameFromResult(result));
		update();
	}
	
	@Override
	public void onStart(ITestContext context) 
	{
		System.err.println("TEST started : " + context.getName() );
		testsInProgress.add(context.getName());
		ITestNGMethod[] allTestMethods = context.getAllTestMethods();
		for ( ITestNGMethod m : allTestMethods )
		{
			String testMethod = m.getTestClass().getName() + "." + m.getMethodName() ;
			testsPending.add(testMethod);
		}
		update();
	}

	@Override
	public void onFinish(ITestContext context) 
	{
		System.err.println("TEST finished : " + context.getName() );
		testsCompleted.add(context.getName());
		testsInProgress.remove(context.getName());
		totalActualTimeTaken = context.getEndDate().getTime() - context.getStartDate().getTime();
		long timeSaved = timeTakenByTestMethods - totalActualTimeTaken ;
		customMessage.replace(0, customMessage.length(), "");
		customMessage.append(  "<span> " + "Sum of time taken for each test method : " + timeTakenByTestMethods/1000 + " seconds!" + "</span><br/>" +
							"<span> " + "Actual total time taken for run : " + totalActualTimeTaken/1000 + " seconds!" + "</span><br/>" +
								"<span>" + "Time saved : " + timeSaved/1000 + " seconds OR " + timeSaved*100 / timeTakenByTestMethods + "%</span>" );
		update();
	}
	
	private String getTestMethodNameFromResult(ITestResult result)
	{
		return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
	}
	
	/*
	 * 
	 */
	private void update()
	{
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(
			        new FileWriter(PROGRESS_REPORT_FILE));
			synchronized (lock ) 
			{
				if ( template != null)
				{
					template.merge(context, writer);
					writer.flush();
					System.out.println("test Progress updated...");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch ( Exception e1 ) {
			e1.printStackTrace();
		} finally {
			if ( null != writer )
			{
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}