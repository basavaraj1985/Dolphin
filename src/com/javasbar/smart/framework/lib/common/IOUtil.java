package com.javasbar.smart.framework.lib.common;


import static org.testng.Assert.fail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Basavaraj M
 *
 */
public class IOUtil 
{
	
	/**
	 * 
	 * @param queriesArray - array from which elements have to be picked
	 * @param count	- how many elements needs to be picked
	 * @param randLimit - if the randomness have to be limited to subset of arrayLenght, pass this. i.e <br>
	 *					<t> if array length is 50 and 5 random queris have to picked within first 20 elements of array <br>
	 *					<t> then, the call would be <code> getQueriesToTest(queriesArray, 5, 20, true )
	 * @param isRandom
	 * @return
	 */
	public static List<String> getQueriesToTest(String[] queriesArray, int count, int randLimit, boolean isRandom)
	{
		List<String> result = new ArrayList<String>();
		if ( queriesArray == null || ( queriesArray != null && queriesArray.length < 1 ) )
		{
			fail("NO DATA! Initialize queriesArray Properly! null?" + ( queriesArray == null ));
		}
		for ( int i = 0 ; ( i < count && i <  queriesArray.length ) ; i++ )
		{
			if ( isRandom )
			{
				int randomnessLimit = ( randLimit > 0 && randLimit < queriesArray.length ) ? randLimit : queriesArray.length;
				int randonQueryIndex = Util.RandomInt(randomnessLimit);
//				result.add( getFormattedQuery(queriesArray[randonQueryIndex - 1 ]) );
				result.add( (queriesArray[randonQueryIndex - 1 ]) );
			}
			else 
			{
//				String formattedQuery = getFormattedQuery(queriesArray[i]);
				String formattedQuery = (queriesArray[i]);
				if ( null != formattedQuery )
				{
					result.add(formattedQuery);
				}
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @param fileToWrite
	 * @param results
	 * @throws IOException
	 */
	public static void writeFile(String fileToWrite, List<String> results) throws IOException 
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileToWrite)));
		for ( String line : results )
		{
			writer.write(line);
			writer.write('\n');
		}
		writer.flush();
		writer.close();
	}
	
	/**
	 * 
	 * @param fileName
	 * @param content
	 * @throws IOException
	 */
	public static void writeFile(String fileName, String content, boolean append) throws IOException 
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName), append));
		writer.write(content);
		writer.write('\n');
		writer.flush();
		writer.close();
	}
	
	/**
	 * 
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public static String[] readInfoFromFile(String filename) throws Exception 
	{
		Vector<String> resultVector;
		BufferedReader bufferedFileReader = null;

		String lineContent;
		String[] stringArray = null;
		if (isFileExists(filename)) 
		{
			try 
			{
				bufferedFileReader = new BufferedReader(new FileReader(filename));
				resultVector = new Vector<String>(16384, 32768);
				while ((lineContent = bufferedFileReader.readLine()) != null ) 
				{
					if ( !lineContent.trim().isEmpty() || lineContent.trim().contains(",,"))
					{
						if ( lineContent.startsWith("#"))
						{
							continue;
						}
						resultVector.add(lineContent.trim());
					}
				}
			} catch (IOException iox) {
				iox.printStackTrace();
				throw new Exception(iox.getMessage());
			} finally {
				bufferedFileReader.close();
			}
			stringArray = new String[resultVector.size()];
			resultVector.toArray(stringArray);
		} else {
			System.out.println("File not Found->" + filename);
			System.err.println("File not Found->" + filename);
			throw new Exception("File not Found->" + filename);
		}
		return stringArray;
	}
	
	/**
	 * Randomly pick an alias from aliasesArray, append it to the queries formed and return 
	 * @param queriesArray
	 * @param aliasesArray
	 * @param number_of_queries_to_test
	 * @param randomLimit
	 * @param isRandom
	 * @return
	 */
	public static List<String> getAliasAppendedQueriesToTest( String[] queriesArray, String[] aliasesArray,
									int number_of_queries_to_test, int randomLimit, boolean isRandom)
	{
		List<String> input = getQueriesToTest(queriesArray, number_of_queries_to_test, randomLimit, isRandom );
		List<String> result = new ArrayList<String>();
		
		for ( String query : input )
		{
			int randomQueryIndex = Util.RandomInt( aliasesArray.length);
			String alias = aliasesArray[randomQueryIndex - 1];
			query =  query + " " +  alias.trim();
			result.add(query);
		}
		return result;
	}

	public static boolean isFileExists(String filename) 
	{
		File file = new File( filename);
		return file.exists();
	}

	/**
	 * 
	 * @param fileName
	 * @param excludeStartingWith
	 * @return
	 */
	public static List<String> readAllLinesFromFileAsList(String fileName, String excludeStartingWith) 
	{
		if ( ! isFileExists(fileName) )
		{
			return null;
		}
		
		List<String> result = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(fileName)));
			String line = null;
			while ( (line = reader.readLine()) != null )
			{
				if ( ! line.startsWith(excludeStartingWith) )
				{
					result.add(line);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @param string - to be searched string
	 * @param filePath - in which file
	 * @return - non zero, non negative line number if found, else -1 
	 * @throws FileNotFoundException
	 */
	public static int fileContainsLine(String string, String filePath) throws FileNotFoundException
	{
		File file = new File(filePath);
		if ( ! file.exists() )
		{
			throw new FileNotFoundException("File " + filePath + " Doesnt exist!");
		}
		
		FileInputStream fis = new FileInputStream( file);
		BufferedReader reader = new BufferedReader( new InputStreamReader(fis ));
		int lineCount = 1;
		try 
		{
			String line = null;
			while ((line = reader.readLine()) != null) 
			{
				if (line.compareTo(string) == 0) 
				{
					System.out.println("String - " + string + " found in "
							+ filePath + " @line " + lineCount);
					return lineCount;
				}
				lineCount++;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not read file : " + filePath );
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}
	
	/**
	 * 
	 * @param fileToRead - reads this file and returns the contents as StringBuffer
	 * @return
	 */
	public static StringBuffer readFileAsBuffer(String fileToRead)
	{
		StringBuffer resultBuffer = new StringBuffer();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(fileToRead)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("Could not find the file : " + fileToRead);
			return null;
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		String line = null;
		try
		{
			while ( ( line = reader.readLine() ) != null )
			{
				resultBuffer.append(line);
				resultBuffer.append('\n');
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return resultBuffer;
	}
	
	/**
	 * Remove duplicate lines from a file, and return the number of duplicates found
	 * @param ipFile
	 * @return
	 * @throws Exception
	 */
	public static int removeDuplicateLines(String ipFile) throws Exception
	{
		int duplicateCount = 0;
		BufferedReader reader = new BufferedReader( new FileReader(ipFile));
		StringBuffer toWriteBuffer = new StringBuffer();
		
		String line = null;
		while ( ( line = reader.readLine()) != null )
		{
			if ( toWriteBuffer.indexOf(line.trim())  == -1 )
			{
				toWriteBuffer.append(line.trim());
				toWriteBuffer.append('\n');
			}
			else
			{
				System.out.println("Duplicate : " + line);
				duplicateCount++;
			}
		}
		reader.close();
		
		BufferedWriter writer = new BufferedWriter( new FileWriter(ipFile));
		writer.write(toWriteBuffer.toString());
		writer.flush();
		writer.close();
		return duplicateCount;
	}

	public static void main(String[] args) {
		List<String> qaLines = readAllLinesFromFileAsList("/Users/basavar/Desktop/otherDDsFound.csv", "#");
		List<String> prodLines = readAllLinesFromFileAsList("/Users/basavar/MyWorkspace/SearchTestRobot/conf/cosmos/cosmosQueries.txt", "#");
		for ( String s : prodLines )
		{
			try {
				int fileContainsLine = fileContainsLine(s, "/Users/basavar/Desktop/otherDDsFound.csv");
				if ( fileContainsLine < 0 )
				{
					System.err.println(s);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param file
	 * @return
	 */
	public static Properties loadFileIntoProperties(String file)
	{
		Properties props = new Properties();
		try {
			props.load(new FileReader(new File(file)));
		} catch (FileNotFoundException e) {
			System.out.println("Could not find file : " + file);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Could not load file : " + file);
			e.printStackTrace();
		}
		return props;
	}
	
	/**
	 * Loads the properties file into properties, with added functionality of loading the files itself for values of the properties mentioned in 
	 * the file
	 * 
	 * i.e. if file=/home/hello.properties which contains
	 *         key1=val1,,val2,,file::test.properties,val3
	 *         key2=a,,b,,c,,file::test.properties
	 *         and test.properties contains - 
	 *         x
	 *         y
	 *         z..
	 *         
	 *         then effecive properties returned will be -
	 *         key1=val1,,val2,,x,,y,,z,,val3
	 *         key2=a,,b,,c,,x,,y,,z
	 *         
	 * @param file
	 * @param fileValueIndicator
	 * @param valueSeparator
	 * @return
	 */
	public static Properties loadProperties(String file, String fileValueIndicator, String valueSeparator )
	{
		Properties props = loadFileIntoProperties(file);
		Set<Entry<Object, Object>> entrySet = props.entrySet();
		for ( Entry<Object, Object> entry : entrySet )
		{
  			String value = (String) entry.getValue();
			if (  value.contains(fileValueIndicator) )
			{
				// the value might have multiple file urls, load each of them append, and remove these file urls on the go
				String[] allValues = value.split(valueSeparator);
				for ( String each : allValues )
				{
					if ( each.contains(fileValueIndicator))
					{
						String fileToBeLoaded = each.split(fileValueIndicator)[1];
						String newValue = getValueListFormatted(fileToBeLoaded,valueSeparator);
						entry.setValue( value.replace(each, newValue));
					}
				}
			}
		}
		return props;
	}
	
	/*
	 * Reads a file filled with values separated by new line characters into 
	 * a string value separated by VALUE SEPARATOR - ,,.
	 * It ignores the lines starting with '#'
	 */
	private static String getValueListFormatted(String fileToBeLoaded, String valueSeparator)
	{
		FileInputStream fstream = null;
		try {
			fstream = new FileInputStream(new File( fileToBeLoaded.trim()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		DataInputStream ds = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(ds));
		Pattern p;            
		Matcher m;
		String strLine;
		String inputText = "";
		try {
			while (  (strLine = br.readLine()) != null )
			{
				if ( ! strLine.startsWith("#") )
				{
					inputText = inputText + strLine + "\n";
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// p = Pattern.compile("(?m)$^|[\\n]+\\z");
		p = Pattern.compile("\n");
		m = p.matcher(inputText);
		String str = m.replaceAll(valueSeparator);
		return str;
	}
	

}
