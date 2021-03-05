import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;

import jdk.jfr.Timespan;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

public class ReusableMethods {
	PropertyLoader prop = new PropertyLoader();
	WebDriver driver;
	FrameworkAssert fa;
	final String home = prop.getProperty("url");

	
	@BeforeTest
	public void setBrowserDriver() {
		Browser b = new Browser(prop.getProperty("browser"));
		driver = b.getDriver();
		fa = new FrameworkAssert(driver);
	}
	
	public void launchBrowser() {
		launchBrowser(prop.getProperty("home"));
	}
	
	public void launchBrowser(String url) {
		driver.get(url);
		driver.manage().window().maximize();
	}
	
	public String getElementText(By by) {
		WebElement e = driver.findElement(by);
		scrollToElement(e);
		return e.getAttribute("innerText");
	}
	
	public String getElementText(WebElement e) {
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", e);
		return e.getAttribute("innerText");
	}
	
	public void scrollToElement(WebElement e) {
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", e);
	}
	
	
	// assert to be used when validating a mandatory step is required
	// verify to be used when validating a step that will not stop workflow
	public void assertElementText(By by, String verify) {
//		org.testng.Assert.
	}
	
	public void assertFail(String reason) {
		org.testng.Assert.fail(reason);
	}
	
	public void assertFailError(String reason, Exception e) {
		if(prop.getProperty("kofr").equals("true")) {
			driver.close();
		}
		org.testng.Assert.fail(reason, e);
	}
	
	public void waitForPageLoaded() {
		int time = 30;
		while (time > 0 && !((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete")) {
			try { Thread.sleep(1000); } catch(Exception e) {e.printStackTrace(); }
			System.out.println(((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete"));
			time--;
		}
	}
	
	public void wait(int seconds) {
		try { Thread.sleep(1000*seconds); } catch (Exception e) {e.printStackTrace(); }
	}
	
	public void takeScreenshot(WebDriver driver,String ssDesc) {
		File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
	 	try {
	 		FileUtils.copyFile(scrFile, new File("./SS/"+timestamp()+"-SS-"+ssDesc+ ".png"));
	 	} catch (Exception e) { e.printStackTrace(); }
	}
	
	public String timestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyymmdd-HH.mm.ss");    
		Date date = new Date();
		return sdf.format(date);
	}
	
	@AfterMethod
	public void closeBrowser() {
		driver.close();
	}
}
