package com.basava.framework.testng.dpextension;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
 
import org.apache.commons.io.IOUtils;
import org.testng.annotations.DataProvider;
 
/**
 * @author Basavaraj M
 *
 */
public class FileDataProvider
{
    /**
     * @param testMethod
     * @return
     * @throws Exception
     */
    @DataProvider(name="getDataFromFile")
    public static Iterator<Object[]> getDataFromFile(Method testMethod) throws Exception
    {
        Map<String, String> arguments = DataProviderUtils.resolveDataProviderArguments(testMethod);
        List<String> lines = FileDataProvider.getRawLinesFromFile(arguments.get("filePath"));
        List<Object[]> data = new ArrayList<Object[]>();
        for (String line : lines)
        {
            data.add(new Object[]{line});
        }
        return data.iterator();
    }
 
    public static List<String> getRawLinesFromFile(Method testMethod) throws Exception
    {
        Map<String, String> arguments = DataProviderUtils.resolveDataProviderArguments(testMethod);
        return FileDataProvider.getRawLinesFromFile(arguments.get("filePath"));
    }
 
    public static List<String> getRawLinesFromFile(String filePath) throws Exception
    {
        InputStream is = new FileInputStream(new File(filePath));
        List<String> lines = IOUtils.readLines(is, "UTF-8");
        is.close();
        return lines;
    }
}