package com.javasbar.smart.framework.lib.common;

import static com.javasbar.smart.framework.lib.common.LoggerUtil.logDEBUG;
import static com.javasbar.smart.framework.lib.common.LoggerUtil.logERROR;
import static com.javasbar.smart.framework.lib.common.LoggerUtil.logINFOHighlight;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.CompositeConfiguration;

import com.javasbar.smart.framework.lib.common.IOUtil;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.ssh.SshConnectionType;

/**
 * DBConnection - a singleton class handling DB Connection, execution of queries, .. 
 * @author Basavaraj M
 *
 */
public class DBConnection 
{
	public static final String DB_CONFIG_FILE = "dbconfig";
	public static final String DB_ADDRESS = "db.hostaddress";
	public static final String DB_DATABASE = "db.database";
	public static final String DB_PORT = "db.port";
	public static final String DB_USERNAME = "db.username";
	public static final String DB_PASSWORD = "db.password";
	
	private static DBConnection thisObject ;
	private Connection connxn;
	public static String dbConfigFile = "resources/configuration/dbconfig.properties";
	private static final Object lockObject = new Object();
	public static final String DB_DRIVER_CLASS  = "driver";
	
	/*
	 * Creates the db connection and establishes the connection.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private DBConnection() throws ClassNotFoundException, SQLException 
	{
		connxn = establishConnection();
	}
	
	/**
	 * Returns DBConnection object, after establishing the connection
	 * @return
	 * @throws SQLException 
	 */
	public static DBConnection getInstance() throws ClassNotFoundException, SQLException
	{
		synchronized (lockObject) 
		{
			if ( null == thisObject )
			{
				thisObject = new DBConnection();
			}
		}
		return thisObject;
	}
	
	/**
	 * Returns DBConnection object, after establishing the connection
	 * @return
	 * @throws SQLException 
	 */
	public static DBConnection getInstance(String dbConfig) throws ClassNotFoundException, SQLException
	{
		dbConfigFile = dbConfig;
		synchronized (lockObject) 
		{
			if (null == thisObject) 
			{
				thisObject = new DBConnection();
			}
		}
		return thisObject;
	}
	
	/**
	 * Returns connection object established
	 * @return
	 */
	public Connection getConnection()
	{
		return connxn;
	}
	
	/*
	 * Establish connection
	 */
	private Connection establishConnection() throws ClassNotFoundException, SQLException
	{
		Properties dbProperties = IOUtil.loadFileIntoProperties(dbConfigFile);
		if ( null == dbProperties )
		{
			System.err.println("DBConfiguration is not provided!");
			return null;
		}
		String dbHostAddress = dbProperties.getProperty(DB_ADDRESS);
		String database = dbProperties.getProperty(DB_DATABASE);
		String dbPort = dbProperties.getProperty(DB_PORT);
		String dbUserName = dbProperties.getProperty(DB_USERNAME);
		String dbPassword = dbProperties.getProperty(DB_PASSWORD);
		String dbUrl = dbHostAddress + ":" +dbPort +"/"+ database;
		
//		Class.forName("oracle.jdbc.driver.OracleDriver");
		String driver = dbProperties.getProperty(DB_DRIVER_CLASS,"oracle.jdbc.driver.OracleDriver");
		Class.forName(driver);
		System.out.println(driver + " JDBC Driver Registered!");
		
		System.out.println("Connecting to : " + dbUrl);
		logINFOHighlight("Connecting to : " + dbUrl);
		try 
		{
			System.out.println("db url : " + dbUrl);
			connxn = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
			System.out.println("db url : " + dbUrl + "DB Connection Established successfully.");
		} 
		catch(SQLException se) 
		{
			se.printStackTrace();
			throw se;
		}
		return connxn;
	}
	
	/**
	 * Executes given query and returns ResultSet
	 * @param query
	 * @return
	 */
	public ResultSet executeQuery(String query)
	{
		Statement stmt = null;
		ResultSet resultSet = null;
		System.out.println("Executing query : " + query );
		logDEBUG("Executing query : " + query );
		try {
			stmt = connxn.createStatement();
			stmt.setEscapeProcessing(false);
			resultSet = stmt.executeQuery(query);
			return resultSet;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally  {
			; // close stmt if necessary
		}
		return resultSet;
	}
	
	/**
	 * Returns all the values in specified column in a List
	 * @param query
	 * @param columnName
	 * @return - never a null; returns list of values found by column name 
	 */
	public List<String> executeQuery(String query, String columnName)
	{
		List<String> result = new ArrayList<String>();
		Statement stmt = null;
		ResultSet resultSet = null;
		System.out.println("Executing query : " + query );
		logDEBUG("Executing query : " + query );
		try {
			stmt = connxn.createStatement();
			resultSet = stmt.executeQuery(query);
			while ( resultSet.next() )
			{
				String record = resultSet.getString(columnName);
				if ( null != record )
				{
					System.out.println(query + " c: " + columnName + " Result : " + record);
					result.add(record);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally  {
			; // close stmt if necessary
		}
		return result;
	}
	
	/**
	 * Returns all the values in specified column in a List
	 * @param query
	 * @param columnIndex - index starts with 1
	 * @return
	 */
	public List<String> executeQuery(String query, int columnIndex)
	{
		List<String> result = new ArrayList<String>();
		Statement stmt = null;
		ResultSet resultSet = null;
		System.out.println("Executing query : " + query );
		logDEBUG("Executing query : " + query );
		try {
			stmt = connxn.createStatement();
			resultSet = stmt.executeQuery(query);
			while ( resultSet.next() )
			{
				String record = resultSet.getString(columnIndex);
				if ( null != record )
				{
					System.out.println(query + " c: " + columnIndex + " Result : " + columnIndex);
					result.add(record);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally  {
			; // close stmt if necessary
		}
		return result;
	}
	
	/**
	 * 
	 * @param fileToRun = filePath, which has statements to run. Each line should have a single statement.
	 */
	public void runSQL(String fileKey , CompositeConfiguration configuration)
	{
		String fileToRun = configuration.getString(fileKey);
		System.out.println("Queries are picked up from file : "+fileToRun);
		List<String> sqlStatements = IOUtil.readAllLinesFromFileAsList(fileToRun, "#");
		
		for ( String sqlStmt : sqlStatements )
		{
			if ( sqlStmt.toLowerCase().trim().startsWith("update") || sqlStmt.toLowerCase().trim().startsWith("delete") )
			{
				int updateResult = executeUpdate(sqlStmt);
				logDEBUG(sqlStmt + "   result: " + updateResult);
				System.out.println("Execution successful for SQL Statement : "+sqlStmt);
			}
			else if  (sqlStmt.trim().startsWith("_"))
			{
				System.out.println("Code to identify and kill previously running processes begins here..");
				logDEBUG("Identifying and killing previously running processes..");
				sqlStmt = sqlStmt.substring(1);
				ResultSet rs = executeQuery(sqlStmt);
				System.out.println("Execution successful for SQL Statement : "+sqlStmt);
				logDEBUG(sqlStmt + "   result: ");
				String host = "";
				String pid = "";
				
				HashMap<String,String> hostpidmap = new  HashMap<String,String>();
				try {
					while(rs.next())
					{
						host = rs.getString("host");
						pid = String.valueOf(rs.getInt("pid"));
						if (hostpidmap.containsKey(host))
						{
							hostpidmap.put(host,hostpidmap.get(host)+" "+pid);
						}
						else
						{
							hostpidmap.put(host, pid);
						}
					}	
				} catch (SQLException e) 
				{
					e.printStackTrace();
				}
				ArrayList<String> hosts = new ArrayList<String>(hostpidmap.keySet());
				for (String hostname : hosts) 
				{
					try 
					{
						String pidlist = hostpidmap.get(hostname);
						System.out.println("Pids to be killed : " + pidlist
								+ " running on Host : " + hostname);
						RemoteClient rc = new RemoteClient(hostname, "yahoo",
								configuration.getString("yahoo_"
										+ RemoteClient.PRIVATE_KEY_FILE),
								configuration.getString("yahoo_"
										+ RemoteClient.PASS_PHRASE),
								OperatingSystemFamily.UNIX,
								SshConnectionType.SFTP);
						ArrayList<String> commandoutput = null;
						String[] listOfCommandsToExecute = new String[] { "hostname", "pwd",
								"whoami", "ps", "kill", "ps",
								"/home/y/bin/autom-check",
								"/home/y/bin/repl-check" }; 
						for (String command : listOfCommandsToExecute ) 
						{
							commandoutput = new ArrayList<String>();
							System.out.println("Command : " + command);
							if (command.equals("uname")) 
							{
								rc.executeCommand(commandoutput, command, "-a");
							} else if (command.equals("ls")) 
							{
								rc.executeCommand(commandoutput, command, "-al");
							} else if (command.equals("kill")) 
							{
								pidlist = "kill -9 " + pidlist;
								rc.executeCommand(commandoutput,
										pidlist.split(" "));
							} else if (command.equals("ps")) 
							{
								rc.executeCommand(commandoutput, command, "-u",
										"yahoo");
							} else 
							{
								rc.executeCommand(commandoutput, command);
							}
						}
						if (hostname.startsWith("feed1")) {
							rc.executeCommand(commandoutput,
									"/home/y/bin/automs-check");
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
				System.out.println("Code to identify and kill previously running processes ends here..");	
			}
			else if (sqlStmt.trim().length() > 1 && !sqlStmt.trim().startsWith("#"))
			{
				ResultSet rs = executeQuery(sqlStmt);
				System.out.println("Execution successful for SQL Statement : "+sqlStmt);
				logDEBUG(sqlStmt + "   result: ");
				try {
					ResultSetMetaData metaData = rs.getMetaData();
					int columnCount = metaData.getColumnCount();
					while( rs.next() )
					{
						StringBuffer buffer = new StringBuffer();
						for ( int i = 1 ; i <= columnCount ; i++ )
						{
							Object object = rs.getObject(i);
							if  ( null != object )
							{
								buffer.append(object.toString()).append("  |  ");
							}
							else 
							{
								buffer.append("null").append("  |  ");
							}
						}
						logDEBUG(buffer.toString());
					}
				} catch (SQLException e) {
					logERROR("SQL Exception for statement execution: " + sqlStmt);
					logERROR("Error : " + e.getMessage() + " ErrorCode: " + e.getErrorCode() + " state:" + e.getSQLState());
					e.printStackTrace();
				} catch ( Exception e1 ) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Executes an update statement returning number of rows updated. Returns Integer.MIN_VALUE if failed.
	 * @param query
	 * @return
	 */
	public int executeUpdate(String query)
	{
		int updateResult = Integer.MIN_VALUE;
		Statement stmt = null;
		System.out.println("Executing query : " + query );
		logDEBUG("Executing query : " + query );
		try {
			stmt = connxn.createStatement();
			System.out.println("Query being executed : "+query);
			updateResult = stmt.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if ( null != stmt )
			{
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return updateResult;
	}
	
	/**
	 * Returns primary key of a table
	 * @param tableName
	 * @return
	 */
	public String getPrimaryKeyOfTable(String tableName)
	{
		String query = "SELECT cols.column_name FROM all_constraints cons, all_cons_columns cols"
				+ " WHERE cols.table_name ='"+ tableName.toUpperCase() +"' AND cons.constraint_type ='P' AND "
						+ "cons.constraint_name = cols.constraint_name AND cons.owner = cols.owner "
						+ "ORDER BY cols.table_name, cols.position" ;
		List<String> pkList = executeQuery(query, 1);
		if ( null == pkList || pkList.size() == 0 )
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			pkList = executeQuery(query, 1);
			if ( null != pkList && pkList.size() != 0 )
			{
				return pkList.get(0);
			}
			logERROR("Primary key couldnt be queried from the table!");
			return null;
		}
		return pkList.get(0);
	}
	
	/**
	 * Prints result set on console and log file
	 * @param rs
	 */
	public synchronized void printResultSet(ResultSet rs)
	{
		if ( null == rs ) 
		{
			return ;
		}
		System.out.println("Printing DB Results...");
		ResultSetMetaData metaData;
		try {
			metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			while ( rs.next() )
			{
				StringBuffer row = new StringBuffer();
				for ( int i = 1 ; i <= columnCount ; i++ )
				{
					String columnName = rs.getMetaData().getColumnName(i);
					String value = rs.getString(i);
					row.append(columnName + ":" + value + "\t");
				}
				System.out.println(row);
				logDEBUG(row.toString());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Finished Printing DB Results...");
		
	}
	
	/**
	 * Returns the row count of specific table
	 * @param tablename
	 * @return returns Integer.Min if result set is null
	 */
	public int getRowCount(String tablename) throws SQLException
	{
		ResultSet rs = this.executeQuery("select count(1) COUNT from "+tablename);
		if ( null == rs )
		{
			return Integer.MIN_VALUE;
		}
		rs.next();
		return rs.getInt(1);
	}
	
	/**
	 * Returns row count of specific table based on some condition
	 * @param tablename
	 * @param condition 
	 * @return returns Integer.Min if result set is null
	 */
	public int getRowCountWithCondition(String tablename,String condition) throws SQLException
	{
		String query = "select count(1) COUNT from "+tablename +" where "+condition;
		query = query.replaceAll("\\|", ",");
		System.out.println("Query being executed is : "+query);
		
		ResultSet rs = executeQuery(query);
		if ( null == rs )
		{
			return Integer.MIN_VALUE;
		}
		rs.next();
		return rs.getInt(1);
	}
	
	/**
	 * Closes connection
	 * @throws SQLException
	 */
	public void closeConnection() throws SQLException
	{
		connxn.close();
	}
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		DBConnection dbc = DBConnection.getInstance("resources/configuration/dbconfig.properties");
		System.out.println("got " + dbc.getPrimaryKeyOfTable("EMPLOYEE"));
		List<String> results = dbc.executeQuery("select NAME from EMPLOYEE where d='104'", "NAME");
		for ( String re : results )
		{
			System.out.println(re);
		}
		dbc.closeConnection();
	}
}
