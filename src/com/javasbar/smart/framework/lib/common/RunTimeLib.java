package com.javasbar.smart.framework.lib.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * @author Basavaraj M
 *
 */
public class RunTimeLib 
{
	/**
	 * @param command
	 * @return
	 */
	public static StringBuffer runCommandBlocking(String command)
	{
		StringBuffer sb = new StringBuffer();
		ProcessBuilder builder = null;
		if ( OSValidator.isWindows() )
		{
			builder = new ProcessBuilder(
		            "cmd.exe", "/c", command);
		}
		else if ( OSValidator.isMac() )
		{
			builder = new ProcessBuilder(
					"/bin/sh", "-c", command);
		}
		builder.redirectErrorStream(true);
		try {
			Process process = builder.start();
			InputStream inputStream = process.getInputStream();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		 BufferedReader reader =  null;
		 try 
	    	{	
		  		Process p = builder.start();
		  		reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		        String line = null;
		        while (true) 
		        {
		            line = reader.readLine();
		            if (line == null) { break; }
		            sb.append(line);
		        }
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if ( null != reader )
					{
						reader.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		return sb;
	}
	
	public static void runCommandInAThread(final String command, final String fileName, final String thredName )
	{
		Thread runnerThread = new Thread(new Runnable() {
			
			public void run() 
			{
				ProcessBuilder builder = null;
				if ( OSValidator.isWindows() )
				{
					builder = new ProcessBuilder(
				            "cmd.exe", "/c", command + " > " + fileName );
				}
				else if ( OSValidator.isMac() )
				{
					builder = new ProcessBuilder(
							"/bin/sh", "-c", command + " > " + fileName );
				}
			    builder.redirectErrorStream(true);
			    BufferedReader reader =  null;
			    BufferedWriter writer = null;
			    	try 
			    	{	
				  		Process p = builder.start();
				  		reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				  		File file = new File(fileName);
				  		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
				        String line = null;
				        while (true) 
				        {
				            line = reader.readLine();
				            if (line == null) { break; }
				            System.out.println(line);
				            writer.write(line);
				            writer.write('\n');
				            writer.flush();
				        }
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							if ( null != reader )
							{
								reader.close();
							}
							if ( null != writer )
							{
								writer.close();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
			}
		}, thredName );
		runnerThread.start();
	}
	
	

	public static void main(String[] args) {
		System.out.println(System.getProperty("user.dir"));
		runCommandInAThread("java -jar ./lib/drivers/ios-server-0.6.3-jar-with-dependencies.jar" +
				" -aut ./resources/iWebDriver.app -simulators  -port 4444", "output.txt", "hhhhhhhh");
	}
}
