package com.basava.smart.framework.lib.common;

import static com.basava.smart.framework.lib.common.LoggerUtil.logDEBUG;
import static com.basava.smart.framework.lib.common.LoggerUtil.logException;
import static com.basava.smart.framework.lib.common.LoggerUtil.logINFOHighlight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.CompositeConfiguration;

import com.basava.smart.framework.lib.common.IOUtil;
import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereExecutionOutputHandler;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.ssh.SshConnectionBuilder;
import com.xebialabs.overthere.ssh.SshConnectionType;

/**
 * RemoteClient for ssh'ing. 
 * @author Basavaraj M
 */
public class RemoteClient 
{
	/**
	 * These are the keys expected in configuration if initi is done 
	 */
	public static final String HOSTNAME = "_hostName";
	public static final String AUTH_TYPE = "_authenticationType";
	public static final String USER_NAME = "_userName";
	public static final String PASS_WORD = "_password";
	public static final String PRIVATE_KEY_FILE ="_privateKeyFile";
	public static final String PASS_PHRASE = "_passphrase";
	public static final String OS_TYPE = "_os";
	public static final String CONNECTION_TYPE = "_connectionType";
	public static final String LOG_DIR = "_logDirectory";

	private OverthereConnection connection = null;
	private String key;
	private String hostName;
	
	/**
	 * All the authentication, address info related to the m/c for which         <br>
	 * ssh has to be done will be provided in configuration with key names		<br>
	 * starting with "key_"	<br>
	 * For example if key=feed1 then, parameters that would be searched for are; <br>
	 * feed1_userName, feed1_password...	<br>
	 * @param key
	 */
	public RemoteClient(String key, CompositeConfiguration configuration) 
	{
		this.key = key;
		String address = configuration.getString(key+HOSTNAME,"NotConfigured").trim();
		hostName = address;
		String authType = configuration.getString(key+AUTH_TYPE,"NotConfigured").trim();
		String userName = configuration.getString(key+USER_NAME,"NotConfigured").trim();
		String password = configuration.getString(key+PASS_WORD,"NotConfigured").trim();
		String privateKeyFile = configuration.getString(key +PRIVATE_KEY_FILE,"NotConfigured").trim();
		String passPhrase = configuration.getString(key + PASS_PHRASE,"").trim();
		//passPhrase = "vs6MpkB5";
		System.out.println("Connecting to: " + address + " authType:" + authType + " un:" + userName + " privateKeyFile:" + privateKeyFile +" Using Passphrase : "+passPhrase );
		
		ConnectionOptions options = new ConnectionOptions();
		if ( authType.toLowerCase().contains("password"))
		{
			options.set(ConnectionOptions.ADDRESS, address);
			options.set(ConnectionOptions.USERNAME, userName);
			options.set(ConnectionOptions.PASSWORD, password);
			if ( configuration.getString(key+OS_TYPE,"NotConfigured").compareToIgnoreCase("unix") == 0)
			{
				options.set(ConnectionOptions.OPERATING_SYSTEM, OperatingSystemFamily.UNIX);
			}
			else 
			{
				options.set(ConnectionOptions.OPERATING_SYSTEM, OperatingSystemFamily.WINDOWS);
			}
		
			if ( configuration.getString(key + CONNECTION_TYPE,"NotConfigured").compareToIgnoreCase("scp") == 0 )
			{
				options.set(SshConnectionBuilder.CONNECTION_TYPE, SshConnectionType.SCP);
			} 
			else 
			{
				options.set(SshConnectionBuilder.CONNECTION_TYPE, SshConnectionType.SFTP);
			}
			logDEBUG("Connecting to : " + address + ";" + userName + ";" + password + ";"
									+ (String)options.get(SshConnectionBuilder.CONNECTION_TYPE) 
											+ ";" + (String) options.get(ConnectionOptions.OPERATING_SYSTEM));
		}
		else // auth by private/public keys 
		{
			options.set(ConnectionOptions.ADDRESS, address);
			options.set(ConnectionOptions.USERNAME, userName);
			options.set(SshConnectionBuilder.PRIVATE_KEY_FILE, privateKeyFile);
			logDEBUG("Private key file exists? " + IOUtil.isFileExists(privateKeyFile) );
			options.set(SshConnectionBuilder.PASSPHRASE, passPhrase);
			if ( configuration.getString(key+OS_TYPE,"NotConfigured").compareToIgnoreCase("unix") == 0)
			{
				options.set(ConnectionOptions.OPERATING_SYSTEM, OperatingSystemFamily.UNIX);
			}
			else 
			{
				options.set(ConnectionOptions.OPERATING_SYSTEM, OperatingSystemFamily.WINDOWS);
			}
		
			if ( configuration.getString(key + CONNECTION_TYPE,"NotConfigured").compareToIgnoreCase("scp") == 0 )
			{
				options.set(SshConnectionBuilder.CONNECTION_TYPE, SshConnectionType.SCP);
			} 
			else 
			{
				options.set(SshConnectionBuilder.CONNECTION_TYPE, SshConnectionType.SFTP);
			}
			logDEBUG("Connecting to : " + address + ";" + userName + ";" + password + ";"
					+ options.get(SshConnectionBuilder.CONNECTION_TYPE) 
							+ ";" + options.get(ConnectionOptions.OPERATING_SYSTEM));
		}
		try {
			connection = Overthere.getConnection("ssh", options);
		} catch (Exception e) {
			e.printStackTrace();
			logException(e);
		}
		logINFOHighlight("Remote connection established to : " + getHostName() );
	}
	
	/**
	 * 
	 * @param address - host address
	 * @param userName - userName to be used for login
	 * @param password - password to be used for login
	 * @param os	- OS name - use OperatingSystemFamily.UNIX / OperatingSystemFamily.WINDOWS
	 * @param connectionType - SshConnectionType.SFTP / .SCP / .SFTP_CYGWIN / .INTERACTIVE_SUDOJ / .SUDO / .TUNNEL
	 * @param sftp
	 */
	public RemoteClient(String address, String userName, String password, 
			OperatingSystemFamily os, SshConnectionType sftp)
	{
		ConnectionOptions options = new ConnectionOptions();
		hostName = address;
		options.set(ConnectionOptions.ADDRESS, address);
		options.set(ConnectionOptions.USERNAME, userName);
		options.set(ConnectionOptions.PASSWORD, password);
		options.set(ConnectionOptions.OPERATING_SYSTEM, os);
		options.set(SshConnectionBuilder.CONNECTION_TYPE, sftp);
		logDEBUG("Connecting to : " + address + ";" + userName + ";" + password + ";" + os + ";" + sftp);
		System.out.println("Connecting to : " + address + ";" + userName + ";" + password + ";" + os + ";" + sftp);
		connection = Overthere.getConnection("ssh", options);
	}
	
	/**
	 * 
	 * @param address	- host address
	 * @param userName	- username to be used for login
	 * @param privateKeyFileLocation - location of private key file ex: ~/.ssh/id_rsa
	 * @param passPhrase - passphrase used during ssh keygen
	 * @param os	- os name 
	 * @param connectionType
	 */
	public RemoteClient(String address, String userName, String privateKeyFileLocation, 
			String passPhrase, OperatingSystemFamily os, SshConnectionType connectionType )
	{
		ConnectionOptions options = new ConnectionOptions();
		hostName = address;
		options.set(ConnectionOptions.ADDRESS, address);
		options.set(ConnectionOptions.USERNAME, userName);
		options.set(SshConnectionBuilder.PRIVATE_KEY_FILE, privateKeyFileLocation);
		options.set(SshConnectionBuilder.PASSPHRASE, passPhrase);
		options.set(ConnectionOptions.OPERATING_SYSTEM, os);
		options.set(SshConnectionBuilder.CONNECTION_TYPE, connectionType);
		logDEBUG("Connecting to : " + address + ";" + userName + ";" + privateKeyFileLocation + ";" + passPhrase + ";" + os + ";" + connectionType);
		System.out.println("Connecting to : " + address + ";" + userName + ";" + privateKeyFileLocation + ";" + passPhrase + ";" + os + ";" + connectionType);
		connection = Overthere.getConnection("ssh", options);
	}
	
	/**
	 * Get connection object.
	 * @return
	 */
	public OverthereConnection getConnection()
	{
		return connection;
	}
	
	/**
	 * Returns host name
	 * @return - host name
	 */
	public String getHostName()
	{
		StringBuffer buffer = new StringBuffer();
		executeCommand(buffer, "hostname");
		return buffer.toString();
	}
	
	/**
	 * Executes command and returns output in StringBuffer / List<String> object passed as argument.
	 * @param lineList		- list that will hold the response
	 * @param commandAndArguments
	 * @return - returns command execution exit code
	 */
	public int executeCommand(List<String> lineList, String... commandAndArguments)
	{
		int exitStatus = Integer.MIN_VALUE;
		OEOutputHandler outputHandler = new OEOutputHandler(false);
		OEOutputHandler errorHandler = new OEOutputHandler(true);
		StringBuffer bf = new StringBuffer();
		for ( String s : commandAndArguments )
		{
			bf.append(s);
		}
		System.out.println("Executing - " + hostName + " : " + bf.toString() );
		logDEBUG("Executing - " + hostName + " : " + bf.toString() );
		exitStatus = connection.execute(outputHandler, errorHandler, CmdLine.build(commandAndArguments));
		if ( null != lineList )
		{
			lineList.add("Exit status : " + String.valueOf(exitStatus));
			lineList.addAll(outputHandler.lineBuffer);
			lineList.addAll(errorHandler.lineBuffer);
		}
		return exitStatus;
	}
	
	/**
	 * 
	 * @param buffer
	 * @param commandAndArguments
	 * @return
	 */
	public int executeCommand(StringBuffer buffer, String... commandAndArguments)
	{
		int exitStatus = Integer.MIN_VALUE;
		OEOutputHandler outputHandler = new OEOutputHandler(false);
		OEOutputHandler errorHandler = new OEOutputHandler(true);
		StringBuffer bf = new StringBuffer();
		for ( String s : commandAndArguments )
		{
			bf.append(s);
		}
		System.out.println("Executing - " + hostName + " : " + bf.toString() );
		logDEBUG("Executing - " + hostName + " : " + bf.toString() );
		exitStatus = connection.execute(outputHandler, errorHandler, CmdLine.build(commandAndArguments));
		if ( null != buffer )
		{
//			buffer.append("Exit status : " + String.valueOf(exitStatus));
			buffer.append(outputHandler.lineBuffer.toString());
			buffer.append(errorHandler.lineBuffer.toString());
		}
		return exitStatus;
	}
	
	/**
	 * Starts a proces and returns output in List<String> object passed as parameter.
	 * Returns the process exit status
	 * @param lineList
	 * @param commands
	 * @return
	 */
	public int startProcess(List<String> lineList,String... commands )
	{
		int exitCode = Integer.MIN_VALUE;
		OverthereProcess process = connection.startProcess(CmdLine.build(commands));
//		OutputStream processStdIn = process.getStdin();
		InputStream processStdOut = process.getStdout();
		InputStream procStdError = process.getStderr();
		BufferedReader stdout = new BufferedReader(new InputStreamReader(processStdOut));
		try { 
			String line;
			try {
				while((line = stdout.readLine()) != null) 
				{
					System.err.println(line);
					lineList.add(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			try {
				stdout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		BufferedReader errorReader = new BufferedReader(new InputStreamReader(procStdError));
		try {
			String line;
			try {
				while ( ( line = errorReader.readLine()) != null )
				{
					System.err.println(line);
//					lineList.add(line);
					lineList.add(String.format(line + "\t -->Error!"));
				}
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		} finally {
			try {
				errorReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			exitCode = process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return exitCode;
	}

	/**
	 * 
	 * @param filePathOnRemoteMachine
	 * @param directoryWhereToStoreTheRemoteFile
	 * @return
	 */
	public File getFile(String filePathOnRemoteMachine, String directoryWhereToStoreTheRemoteFile)
	{
		File file = null;
		OverthereFile overthereFile = connection.getFile(filePathOnRemoteMachine);
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(overthereFile.getInputStream()));
		try {
			String line;
			while((line = reader.readLine()) != null) 
			{
				System.err.println(line);
				builder.append(line);
				builder.append(System.getProperty("line.separator"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			IOUtil.writeFile(directoryWhereToStoreTheRemoteFile +"/"+ overthereFile.getName(), builder.toString(), false);
			file = new File( overthereFile.getName() );
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}
	
	/**
	 * Upload sourceFile to targetFile in remote location. The sourceFile and targetFile are file paths.
	 * @param sourceFile
	 * @param targetFile
	 * @throws IOException 
	 */
	public void uploadFile(String sourceFile, String targetFile) throws IOException
	{
		System.out.println("Uploading " + sourceFile + " to " + getHostName() + ":" + targetFile);
		if ( null == sourceFile || null == targetFile )
		{
			throw new IOException("Either source or target file is null!");
		}
		BufferedReader reader = new BufferedReader(new FileReader(new File(sourceFile)));
		OverthereFile motd = connection.getFile(targetFile);
		PrintWriter writer = new PrintWriter(motd.getOutputStream());
		String line = null; 
		while ( ( line = reader.readLine() ) != null )
		{
			writer.println(line);
		}
		writer.flush();
		writer.close();
	}
	
	/**
	 * Returns the 'key' which decides which host to connect to; i.e. key='feed1' then,
	 * connection is made to configured value for the key feed1_hostName.
	 * @return
	 */
	public String getConnectionKey()
	{
		return key;
	}
	
	/**
	 * Closes connection
	 */
	public void closeConnection()
	{
		connection.close();
	}
	
	/**
	 * Output handler implementation for command executions
	 * @author basavar
	 *
	 */
	class OEOutputHandler implements OverthereExecutionOutputHandler
	{
		StringBuffer charBuffer = new StringBuffer();
		ArrayList<String> lineBuffer = new ArrayList<String>();
		boolean isErrorHandling = false;
		
		public OEOutputHandler(boolean isItErrorHandler) 
		{
			isErrorHandling = isItErrorHandler;
		}
		
		public void handleChar(char c) 
		{
			charBuffer.append(c);
		}

		public void handleLine(String line) 
		{
			lineBuffer.add(line);
			if ( isErrorHandling )
			{
				System.err.println(line);
			}
			else 
			{
				System.out.println(line);
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
//		RemoteClient client = new RemoteClient( "feed1.stage.local.gq1.yahoo.com", "basavar", "/Users/basavar/.ssh/id_rsa","hello", OperatingSystemFamily.UNIX, SshConnectionType.SFTP);
		RemoteClient client = new RemoteClient("flproc1.stage.local.gq1.yahoo.com","basavar", "secretPwd",OperatingSystemFamily.UNIX, SshConnectionType.SFTP);
//		RemoteClient client = new RemoteClient( "feed1.stage.local.gq1.yahoo.com", "basavar", "", OperatingSystemFamily.UNIX, SshConnectionType.SFTP);
//		List<String> buf = new ArrayList<String>();
//		client.executeCommand(buf, "ls","-ltra");
//		int startProcess = client.startProcess(buf, "ls","-ltra");
//		System.out.println(startProcess + "----" + buf.toString());
//		List<String> buffer = new ArrayList<String>();
//		String command = "ls /net/lfeeds/dropbox/dapper_unsuper";
//		System.out.println(client.startProcess(buffer, command.split(" ")));
		client.uploadFile("/Users/basavar/Downloads/run.sh.zip", "/home/basavar/azip.zip");
		client.closeConnection();
	}
}
