package com.basava.smart.framework.lib.common;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Basavaraj M
 *
 */
public class StringUtil
{
	public static List<String> forbidden_patterns ;
//	private final static Charset UTF8_CHARSET = Charset.forName("UTF-8");
	
	static 
	{
		forbidden_patterns = new ArrayList<String>();
		forbidden_patterns.add("#");
		forbidden_patterns.add("amp;");
		forbidden_patterns.add("?");
		forbidden_patterns.add("apos;");
	}
	
	/**
	 * Returns the distance between 2 strings, i.e. how far they are from each other to match exactly
	 * Algorithm used - http://www.merriampark.com/ld.htm - Levenshtein Distance
	 * @param s
	 * @param t
	 * @return
	 */
	public static int getLevenshteinDistance (String s, String t) 
	{
		  if (s == null || t == null) {
		    throw new IllegalArgumentException("Strings must not be null");
		  }
				
		  /*
		    The difference between this impl. and the previous is that, rather 
		     than creating and retaining a matrix of size s.length()+1 by t.length()+1, 
		     we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
		     is the 'current working' distance array that maintains the newest distance cost
		     counts as we iterate through the characters of String s.  Each time we increment
		     the index of String t we are comparing, d is copied to p, the second int[].  Doing so
		     allows us to retain the previous cost counts as required by the algorithm (taking 
		     the minimum of the cost count to the left, up one, and diagonally up and to the left
		     of the current cost count being calculated).  (Note that the arrays aren't really 
		     copied anymore, just switched...this is clearly much better than cloning an array 
		     or doing a System.arraycopy() each time  through the outer loop.)

		     Effectively, the difference between the two implementations is this one does not 
		     cause an out of memory condition when calculating the LD over two very large strings.  		
		  */		
				
		  int n = s.length(); // length of s
		  int m = t.length(); // length of t
				
		  if (n == 0) {
		    return m;
		  } else if (m == 0) {
		    return n;
		  }

		  int p[] = new int[n+1]; //'previous' cost array, horizontally
		  int d[] = new int[n+1]; // cost array, horizontally
		  int _d[]; //placeholder to assist in swapping p and d

		  // indexes into strings s and t
		  int i; // iterates through s
		  int j; // iterates through t

		  char t_j; // jth character of t

		  int cost; // cost

		  for (i = 0; i<=n; i++) {
		     p[i] = i;
		  }
				
		  for (j = 1; j<=m; j++) {
		     t_j = t.charAt(j-1);
		     d[0] = j;
				
		     for (i=1; i<=n; i++) {
		        cost = s.charAt(i-1)==t_j ? 0 : 1;
		        // minimum of cell to the left+1, to the top+1, diagonally left and up +cost				
		        d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);  
		     }

		     // copy current distance counts to 'previous row' distance counts
		     _d = p;
		     p = d;
		     d = _d;
		  } 
				
		  // our last action in the above loop was to switch d and p, so p now 
		  // actually has the most recent cost counts
		  return p[n];
		}

	/**
	 * Verifies that no forbidden pattern is present in the 'textTobeVerified' 
	 * @param textTobeVerified - to be verified text
	 * @return - returns null if no forbidden pattern is available, else the 
	 * 			 pattern observed in the text to be verified
	 */
	public static String verifyForbiddenPatternsAbsence(String textTobeVerified)
	{
		for ( String forbiddenString : forbidden_patterns)
		{
			if ( textTobeVerified.contains(forbiddenString) )
			{
				System.out.println(forbiddenString + " was found in the Text to be verified!"); 
				return forbiddenString;
			}
		}
		return null;
	}

	public static String limitLength(String message, int length) 
	{
		if ( null == message )
		{
			return null;
		}
		String result = message.substring(0, message.length() > length ? length : message.length() );
		if ( message.length() > length )
		{
			result = result + "...";
		}
		return result; 
	}

	/**
	 * 
	 * @param targetString
	 * @param wordsToBePresentInASentence
	 * @return null if all of the wordsToBePresentInASentence words are present in targetString,
	 * 			  else, list of words which are not present
	 */
	public static List<String> containsWords(String targetString, String wordsToBePresentInASentence) 
	{
		String[] toBePresentWords = wordsToBePresentInASentence.split("s");
		List<String> absentWords = null;
		for ( String word : toBePresentWords )
		{
			if ( ! targetString.contains(word) )
			{
				if ( null == absentWords )
				{
					absentWords = new ArrayList<String>();
				}
				absentWords.add(word);
			}
		}
		
		return absentWords;
	}
	
	public static String stripOffYLTandYLC(String url )
	{
		int start = url.indexOf(";");
		int end = url.indexOf("?");
		String result = url;
		try {
			result = url.substring(0, start) + url.substring(end, url.length());
		} catch (Exception e) {
			System.err.println("Strip off failed for url : " + url);
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 
	 * @param input
	 * @param expectedLength
	 * @return
	 */
	public static String appendSpacesToMakeLength(String input, int expectedLength)
	{
		if ( input.length() < expectedLength )
		{
			int requiredSpaces = expectedLength - input.length();
			for ( int i = 0 ; i < requiredSpaces - 1 ; i++ )
			{
				input = input + " ";
			}
			input = input + ":";
			return input;
		}
		return input;
	}
	
	/**
	 * 
	 * @param textContent
	 * @return
	 */
	public static List<String> getUrlsFromText(String textContent)
	{
		List<String> result = new ArrayList<String>();
		Pattern urlPattern = Pattern.compile(
		        "(?:^|[\\W])((http|ftp)(s?):\\/\\/|www\\.)"
		                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
		                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
		        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = urlPattern.matcher(textContent);
        while (m.find()) 
        {
        	int matchStart = m.start(1);
            int matchEnd = m.end();
            result.add(textContent.substring(matchStart, matchEnd));
            System.out.println("url found : " + textContent.substring(matchStart, matchEnd));
        }
        return result;
	}
	
	/**
	 * Returns the number extracted from text.  <br>
	 * for example input="91 reviews", "9, photos", "0ratings"... output would be 91, 9, 0 respectively. <br>
	 * if no number is found then null will be returned.
	 * @param input
	 * @return
	 */
	public static Integer getNumberFromText(String input)
	{
		Pattern number = Pattern.compile("\\d+");
		Matcher m = number.matcher(input);
		String resultString = null;
		if( m.find() )
		{
			int start = m.start();
			int end = m.end();
			resultString = input.substring(start, end);
		}
		else {
			return null;
		}
		
		try {
			int result = Integer.valueOf(resultString);
			return result;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns a random generated String of length 5 characters
	 * @return
	 */
	public static String getRandomString()
	{
		return getRandomString(5);
	}
	
	/**
	 * Returns a random generated String of length 'randLength'
	 * @param randLength 
	 * @return
	 */
	public static String getRandomString(int randLength)
	{
		Random r = new Random();
		byte[] bytes = new byte[50];
		String randomString = null;
		String result = null;
		while ( null == randomString || null == result || ( null != result &&  result.length() < 5 ))
		{
			r.nextBytes(bytes);
			try {
				randomString = new String(bytes,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			Pattern an = Pattern.compile("[a-z]*[A-Z]*");
			Matcher matcher = an.matcher(randomString);
			StringBuilder buf = new StringBuilder();
			while ( matcher.find() )
			{
				int start = matcher.start();
				int end = matcher.end();
				buf.append(randomString.substring(start, end ));
			}
			result = buf.toString();
		}
		return result;
	}
	
	public static void main(String[] args) 
	{
		for ( int i = 0 ; i< 100 ; i++ )
		{
			System.out.println(getRandomString());
		}
	}
}