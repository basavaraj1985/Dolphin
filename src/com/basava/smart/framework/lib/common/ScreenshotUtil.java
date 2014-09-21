package com.basava.smart.framework.lib.common;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.safari.SafariDriver;

import com.googlecode.fightinglayoutbugs.FightingLayoutBugs;
import com.googlecode.fightinglayoutbugs.LayoutBug;
import com.googlecode.fightinglayoutbugs.WebPage;

/**
 * @author Basavaraj M
 *
 */
public class ScreenshotUtil 
{ 
	protected WebDriver driver;
	private File screenshotDirectory ;
	
	/**
	 * This screenshot directory will be used to save all the screenshots
	 * @author basavar
	 * @param driver
	 * @param screenShotDir
	 */
	public ScreenshotUtil(WebDriver driver, File screenShotDir) 
	{ 
		this.driver = driver;
		screenshotDirectory = screenShotDir;
		if ( ! screenshotDirectory.exists() )
		{
			screenshotDirectory.mkdirs();
		}
	} 
	
	/**
	 * Takes screenshot of @param element, and saves with filename=<code>System.currentTimeMillis </code>
	 * @author basavar
	 * @param element
	 * @throws IOException
	 */
	public void shoot(WebElement element) throws IOException 
	{
		shoot( element, String.valueOf(System.currentTimeMillis()) , null , null);
	} 
	
	/**
	 * 
	 * @author basavar
	 * @throws IOException
	 */
	public void shoot() throws IOException 
	{
		shoot( null, String.valueOf(System.currentTimeMillis()) , null , null);
	} 
	
	/**
	 * 
	 * @author basavar
	 * @param fileName
	 * @throws IOException
	 */
	public void shoot(String fileName) throws IOException 
	{
		shoot( null, fileName, null , null);
	}
	
	/**
	 * 
	 * @author basavar
	 * @param element
	 * @param dir
	 * @param fileName
	 * @param point
	 * @param r
	 * @throws IOException
	 */
	public void shoot( WebElement element, String fileName, Point point, Rectangle r) throws IOException
	{ 
		try 
		{
			if ( driver instanceof HtmlUnitDriver )
			{
				System.err.println("Cannot take screenshot with HtmlUnitDriver, a headless browser, storing whole page");
				String pageSource = driver.getPageSource();
				IOUtil.writeFile(screenshotDirectory.getAbsolutePath() + "/" + fileName + ".html", pageSource,false);
				return;
			}
			if ( ! ( driver instanceof FirefoxDriver || driver instanceof ChromeDriver || driver instanceof InternetExplorerDriver 
						 || driver instanceof SafariDriver ) )
			{
				driver = new Augmenter().augment(driver);
			}
		} catch (Exception ignored) 
		{ 
			ignored.printStackTrace();
			System.err.println("There is error while taking screenshot");
			return;
		}
		File screen = ((TakesScreenshot) driver) .getScreenshotAs(OutputType.FILE);
		
		Point p = null;
		Rectangle rect = null;
		if ( null != element )
		{
			p = element.getLocation();
			int width = element.getSize().getWidth();
			int height = element.getSize().getHeight();
			rect = new Rectangle(width, height);
		}
		BufferedImage img = ImageIO.read(screen);
		
		BufferedImage dest = null;
		if( null != p )
		{
			dest = img.getSubimage(p.getX(), p.getY(), rect.width, rect.height);
			if ( null != point && null != r )
			{
				try {
					dest = img.getSubimage(point.getX(), point.getY(), r.width, r.height);
				} catch (Exception e) {
					//ignore
				}
			}
			ImageIO.write(dest, "png", screen);
		}
		else
		{
			if ( null != point && null != r )
			{
				boolean error = false;
				try {
					dest = img.getSubimage(point.getX(), point.getY(), r.width, r.height);
				} catch (Exception e) {
					error = true;
					ImageIO.write(img, "png", screen);
				}
				if ( !error )
				{
					ImageIO.write(dest, "png", screen);
				}
			}
			else
			{
				ImageIO.write(img, "png", screen);
			}
		}
		FileUtils.copyFile(screen, new File(screenshotDirectory.getAbsolutePath() + "/" + fileName + ".png"));
	}
	
	/**
	 * Captures whole screen screenshot
	 * @author basavar
	 * @param fileName
	 * @throws Exception
	 */
	public void captureScreenShot(String fileName) throws Exception 
	{
		driver.switchTo().defaultContent();
		Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit()
				.getScreenSize());
		BufferedImage capture = new Robot().createScreenCapture(screenRect);
		if (OSValidator.isWindows()) 
		{
			ImageIO.write(capture, "jpg", new File(
					LoggerUtil.screenshotDirectory + File.separator
							 + fileName + ".jpg"));
		} 
		else 
		{
			ImageIO.write(capture, "jpg", new File( screenshotDirectory.getAbsolutePath()
					 + fileName + ".jpg"));
		}
	}
	
	/**
	 * Google fighting layout framework - analyses the UI for
	 * @param directory - where screenshots have to be stored 
	 * @return 
	 */
	public Collection<LayoutBug> analyseUI(File directory) 
	{
		FightingLayoutBugs flb = new FightingLayoutBugs();
		if ( null != directory )
		{
			if ( ! directory.exists() )
			{
				directory.mkdirs();
			}
			flb.setScreenshotDir(directory);
		}
		else
		{
			flb.setScreenshotDir(screenshotDirectory);
		}
        Collection<LayoutBug> findLayoutBugsIn = null;
		try {
			findLayoutBugsIn = flb.findLayoutBugsIn(new WebPage(driver));
			System.out.println("found : " + findLayoutBugsIn.size() + " bugs!");
		} catch (Exception e) {
			e.printStackTrace();
		}
        return findLayoutBugsIn;
	}
	
	/**
	 * 
	 * @author basavar
	 * @param element
	 * @param fileName
	 * @throws IOException
	 */
	public void shoot(WebElement element, String fileName) throws IOException 
	{ 
		shoot(element, fileName, null, null);
	} 
}
//- See more at: http://selenium.polteq.com/en/create-a-screenshot-of-webelements-with-webdriver/#sthash.vFAeVzIu.dpuf