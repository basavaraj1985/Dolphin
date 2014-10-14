package com.javasbar.framework.testng.dpextension;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
 
/**
 * @see Copied : http://www.lysergicjava.com/?p=165 and improvised
 *
 */
public class DataProviderUtils
{
    public static Map<String, String> resolveDataProviderArguments(Method testMethod) throws Exception
    {
        if (testMethod == null)
            throw new IllegalArgumentException("Test Method context cannot be null.");
 
        DataProviderArguments args = testMethod.getAnnotation(DataProviderArguments.class);
        if (args == null)
        {
        	throw new IllegalArgumentException("Test Method context has no DataProviderArguments annotation - " + testMethod.getName() );
        }
        if (args.value() == null || args.value().length == 0)
        {
        	throw new IllegalArgumentException("Test Method context has a malformed DataProviderArguments annotation, testMethod: " + testMethod.getName() );
        }
        Map<String, String> arguments = new HashMap<String, String>();
        for (int i = 0; i < args.value().length; i++)
        {
            String[] parts = args.value()[i].split("=");
            arguments.put(parts[0].trim(), parts[1]);
        }
        return arguments;
    }
}