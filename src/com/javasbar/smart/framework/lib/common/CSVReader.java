package com.javasbar.smart.framework.lib.common;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.dozer.CsvDozerBeanReader;
import org.supercsv.prefs.CsvPreference;

/**
 * Helps reading csv/tsv/[delimiter]sv files into lists, maps, Java beans.
 * @author Basavaraj M
 * @see http://supercsv.sourceforge.net/readers.html
 * @see http://supercsv.sourceforge.net/examples_reading.html
 */
public class CSVReader 
{
	private String dataFile ;
	private String[] header = null;
	private CsvPreference preference = new CsvPreference.Builder(CsvPreference.STANDARD_PREFERENCE).build();
	private CsvMapReader mapReader = null;
	private CsvListReader listReader = null;
	private CsvDozerBeanReader beanReader = null;
	
	/**
	 * Default preferences considered - quote = ", delimiter = ',', endOfLine = "\n"
	 * @param file - File to load
	 * @throws FileNotFoundException 
	 */
	public CSVReader(String file) throws FileNotFoundException 
	{
		dataFile = file;
	}
	
	/**
	 * 
	 * @param file
	 * @param type
	 * @param quoteCharacter
	 * @param delimiterChar
	 * @param endOfLineStr
	 * @throws FileNotFoundException
	 */
	public CSVReader(String file, char quoteCharacter, char delimiterChar, String endOfLineStr) throws FileNotFoundException 
	{
		preference = new CsvPreference.Builder(quoteCharacter, delimiterChar, endOfLineStr).build() ;
		dataFile = file;
	}

	/*
	 * Initializes map reader, if not initialized.
	 */
	private CsvMapReader getMapReader() throws FileNotFoundException 
	{
		if ( null == mapReader )
		{
			mapReader = new CsvMapReader(new FileReader(dataFile), preference);
		}
		return mapReader;
	}
	
	/**
	 * Reads the csv as list and returns.
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public List<String> getRowAsAList() throws FileNotFoundException, IOException
	{
		List<String> result = null;
		result = getListReader().read();
		return result;
	}
	
	/**
	 * Reads the whole data file and returns as list of lists.
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public List<List<String>> getAllRowsAsLists() throws FileNotFoundException, IOException
	{
		List<List<String>> theWholeTable = new ArrayList<List<String>>();
		List<String> row;
		while ( ( row = getListReader().read() ) != null )
		{
			theWholeTable.add(row);
			System.out.println(String.format("[ListReaderDEBUG] lineNo=%s, rowNo=%s, customerMap=%s", getListReader().getLineNumber(),
					getListReader().getRowNumber(), row));
		}
		return theWholeTable;
	}
	
	/*
	 * Initializes list reader if not done yet.
	 */
	private CsvListReader getListReader() throws FileNotFoundException 
	{
		if ( null == listReader )
		{
			listReader = new CsvListReader(new FileReader(dataFile), preference);
		}
		return listReader;
	}

	/**
	 * Read csv row as a map
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public Map<String, String> getRowAsMap() throws FileNotFoundException, IOException 
	{
		Map<String, String> tableMap = null;
		String[] headers = getHeaders();
		tableMap = getMapReader().read(headers) ;
		return tableMap;
	}

	/**
	 * Reads all rows in the csv file 
	 * @return
	 * @throws IOException
	 */
	public List<Map<String, String>> getAllRowsAsMaps() throws IOException 
	{
		String[] headers = getHeaders();
		List<Map<String,String>> theWholeTable = new ArrayList<Map<String, String>>();
		
		Map<String, String> tableMap;
        while( (tableMap = getMapReader().read(headers)) != null ) 
        {
        	 System.out.println(String.format("[DEBUG-mapReader] lineNo=%s, rowNo=%s, customerMap=%s", getMapReader().getLineNumber(),
        			 getMapReader().getRowNumber(), tableMap));
        	 theWholeTable.add(tableMap);
        }
		return theWholeTable;
	}

	/**
	 * getHeaders as an array of Strings
	 * @return
	 */
	public String[] getHeaders() 
	{
		if ( null != header )
		{
			return header;
		}
		
		try {
			header = getMapReader().getHeader(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return header;
	}
	
	/**
	 * Test Code
	 * @param args
	 */
	public static void main(String[] args) 
	{
		try {
			CSVReader reader = new CSVReader("src/test/resources/data/IngestionValidationTables/YELP.txt",'"', '\t',"\n"	);
			String[] headers = reader.getHeaders();
			for ( String s : headers )
			{
				System.err.println("debug: " +  s);
			}
			
			/*
			 * MapReader
			 */
			List<Map<String,String>> allRowsAsMaps = reader.getAllRowsAsMaps();
			Iterator<Map<String, String>> iterator = allRowsAsMaps.iterator();
			System.out.println("--------------------------------------------------");
			while ( iterator.hasNext() )
			{
				Map<String, String> row = iterator.next();
				System.out.println("I needed inputname and here it is : " + row.get("inputname"));
				Set<Entry<String, String>> entrySet = row.entrySet();
				Iterator<Entry<String, String>> mapIterator = entrySet.iterator();
				while ( mapIterator.hasNext() )
				{
					Entry<String, String> next = mapIterator.next();
					System.out.println(next.getKey() + " --> " + next.getValue());
				}
				System.out.println();
			}
			
			/*
			 * listReader
			 */
			List<List<String>> allRowsAsLists = reader.getAllRowsAsLists();
			for ( List<String> list : allRowsAsLists )
			{
				for ( String s : list )
				{
					System.out.print(s + " ");
				}
				System.out.println();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
