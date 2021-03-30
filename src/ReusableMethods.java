import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.*;

import jdk.jfr.Timespan;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

public class ReusableMethods {
	File screenShotFolder = new File("./SS");
	PropertyLoader config = new PropertyLoader("./config.properties");
	PropertyLoader securityConfig = new PropertyLoader("./security.properties");
	WebDriver driver;
	FrameworkAssert fa;
	final String home = config.getProperty("url");
	String tcName;
	String tcClass;
	String tcModule = ""; //TODO : get this set somewhere
	String tcStatus;
	String htmlString;
	static String[] htmlSplit;
	String xmlString;
	static String[] xmlSplit;
	TestCaseStatus testCaseStatus;
	static int passedCount;
	static int failedCount;
	static int debugCount;
	static File newHtmlFile;
	static File newRerunListFile;

	@BeforeSuite
	public void beforeSuite() {
		passedCount = 0;
		failedCount = 0;
		debugCount = 0;
		try {
			File htmlTemplateFile = new File("./htmlReportTemplate.html");
			File rerunTemplateFile = new File("./rerunTemplate.xml");
			htmlString = FileUtils.readFileToString(htmlTemplateFile, "UTF-8");
			xmlString = FileUtils.readFileToString(rerunTemplateFile,"UTF-8");
			String app = config.getProperty("appName");
			String timestamp = timestamp();
			htmlString = htmlString.replace("@Application@", app);
			htmlString = htmlString.replace("@timestamp@", timestamp);
			htmlSplit = htmlString.split("<!--replace-->");
			xmlSplit = xmlString.split("<!--replace-->");
			newHtmlFile = new File("./new.html");
			newRerunListFile = new File("./rerun.xml");
			FileUtils.writeStringToFile(newHtmlFile, htmlSplit[0], "UTF-8", false);
			FileUtils.writeStringToFile(newRerunListFile, xmlSplit[0], "UTF-8", false);
			String[] oldScreenshots = screenShotFolder.list();
			for(String oldFile : oldScreenshots) {
				File currentFile = new File(screenShotFolder.getPath(),oldFile);
				currentFile.delete();
			}
		} catch (Exception e) {e.printStackTrace(); }
	}
	
	@BeforeTest
	public void setBrowserDriver() {
		DriverFactory b = new DriverFactory(config.getProperty("browser"));
		driver = b.getDriver();
		fa = new FrameworkAssert(driver);
	}
	
	@BeforeTest
	public void beforeTest(final ITestContext testContext) {
		testCaseStatus = new TestCaseStatus();
		tcName = testContext.getName();
	}
	
	@BeforeClass
	public void beforeClass() {
		tcClass = this.getClass().getName();
		testCaseStatus.putTestClass(tcClass);
	}
	
	public void launchBrowser() {
		launchBrowser(config.getProperty("home"));
	}
	
	public void verifyText(String text1, String text2, String elementName) {
		String verifyStatus;
		if(text1.equals(text2)) {
			verifyStatus = "Passed";
		} else {
			verifyStatus = "Failed";
		}
		logStep("Verify Element Text of ["+elementName+"]: element ["+text1+"] and expected ["+text2+"]", verifyStatus);
	}
	
	public void launchBrowser(String url) {
		driver.get(url);
		driver.manage().window().maximize();
	}
	
	public WebElement findElement(By by) {
		WebDriverWait wait = new WebDriverWait(driver, Integer.parseInt(config.getProperty("waitTime")));
		WebElement element = null;
		try {
			if(debug()) {
				logStep("Find Element Locator: ["+by+"]", "Debug");
			}
			wait.until(ExpectedConditions.visibilityOfElementLocated(by));
			element = driver.findElement(by);
		} catch(Exception e) { 
			logStep(e.getMessage(), "Debug");
			logStep("Element Not Found ["+by+"]", "Failed");
			element = null;
		}
		return element;
	}
	
	public String getElementText(By by) {
		WebElement e = findElement(by);
		return getElementText(e);
	}
	
	public String getElementText(WebElement e) {
		scrollToElement(e);
		return e.getAttribute("innerText");
	}
	
	public void scrollToElement(WebElement e) {
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", e);
	}
	
	public void inputText(By by, String s, String elementName) {
		WebElement e = findElement(by);
		if(debug()) {
			logStep("Input Text Locator: ["+by+"]", "Debug");
		}
		if(e != null) {
			inputText(e, s, elementName);
			logStep("Input Text ["+s+"] in Element: ["+elementName+"]", "Passed");
		} else {
			logStep("Unable to find Element ["+elementName+"] to input Text: ["+by.toString()+"]", "Failed");
		}
	}
	
	public void inputText(WebElement e, String s, String elementName) {
		scrollToElement(e);
		clickElement(e, elementName);
		e.sendKeys(s);
	}
	
	public void clickElement(By by, String elementName) {
		WebElement e = findElement(by);
		if(debug()) {
			logStep("Click Element Locator: ["+by+"]", "Debug");
		}
		if(e != null) {
			clickElement(e, elementName);
			logStep("Clicked Element: ["+elementName+"]", "Passed");
		} else {
			logStep("Unable to find Element ["+elementName+"] to Click: ["+by.toString()+"]", "Failed");
		}
	}
	
	public void clickElement(WebElement e, String elementName) {
		e.click();
	}
	
	public By dynXpath(By by, String replaceString) {
		String xpath = by.toString();
		xpath = xpath.substring(10);
		xpath = xpath.replace("@1@", replaceString);
		System.out.println(xpath);
		return By.xpath(xpath);
	}
	
	public By dynXpath(By by, String[] replaceStrings) {
		String xpath = by.toString();
		xpath = xpath.substring(10);
		for(int i = 0; i < replaceStrings.length; i++) {
			String replace = "@"+(i+1)+"@";
			xpath = xpath.replace(replace, replaceStrings[i]);
		}
		System.out.println(xpath);
		return By.xpath(xpath);
	}
	
	public void logStep(String testStep, String status) {
		TestStepStatus tss = new TestStepStatus(testStep, status);
		if(debug() || status.equals("Failed")) {
			String ssPath = takeScreenshot("DEBUGSCREENSHOT");
			tss.addScreenShot(ssPath);
		}
		log(tss);
	}
	
	public void logStep(String testStep, String status, String ssPath) {
		TestStepStatus tss = new TestStepStatus(testStep, status, ssPath);
		log(tss);
	}
	
	public void log(TestStepStatus tss) {
		testCaseStatus.addTestStepStatus(tss);
		if(tss.getStepStatus().equals("Debug")) {
			debugCount++;
		} else if(tss.getStepStatus().equals("Failed")) {
			if(config.getProperty("killOnFirstRun").equals("true")) {
				Assert.fail(tss.getTestStep());
			}
		}
	}
	
	// assert to be used when validating a mandatory step is required
	// verify to be used when validating a step that will not stop workflow
	public void assertElementText(By by, String verify, String elementName) {
//		org.testng.Assert.
		String text1 = getElementText(by);
		verifyText(text1, verify, elementName);
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
	
	public String takeScreenshot(String ssDesc) {
		File scrFile = ((TakesScreenshot) this.driver).getScreenshotAs(OutputType.FILE);
		String filePath = "./SS/"+tcName+"-"+timestamp()+"-SS-"+ssDesc+ ".png";
	 	try {
	 		FileUtils.copyFile(scrFile, new File(filePath));
	 	} catch (Exception e) { e.printStackTrace(); }
	 	return filePath;
	}
	
	public String timestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH.mm.ss");    
		Date date = new Date();
		return sdf.format(date);
	}
	
	@AfterMethod
	public void closeBrowser() {
		String ssPath = takeScreenshot(tcName);
		testCaseStatus.setSSPath(ssPath);
		driver.close();
	}
	
	@AfterTest
	public void writeTest() {
		String testModule = tcModule;
		String testCase = tcName;
		String testClass = testCaseStatus.getTestClass();
		ArrayList<TestStepStatus> tssl = testCaseStatus.getTestStepList();
		String testStepsHtml = "";
		String collapsible;
		String passFail;
		String lastStep = "";
		String ssPath = testCaseStatus.getSSPath();
		
		if(testCaseStatus.passed()) { 
			collapsible = "collapsiblePass";
			passFail = "Passed";
		} else { 
			collapsible = "collapsibleFail";
			passFail = "Failed";
			String rerunString = "<test name = \""+testCase+"\">\r\n"
					+ "      <classes>\r\n"
					+ "         <class name = \""+testClass+"\" />     \r\n"
					+ "      </classes>\r\n"
					+ "   </test>\r\n";
			try{ FileUtils.writeStringToFile(newRerunListFile, rerunString, "UTF-8", true); } catch (Exception e) { e.printStackTrace(); }
		}
		if(tssl.size() > 0) {
			if(testCaseStatus.passed()) {
				lastStep = tssl.get(tssl.size()-1).getTestStep();
			} else {
				lastStep = testCaseStatus.getLatestFailedStep();
			}
			for(TestStepStatus tss : tssl) {
				testStepsHtml += tss.getHTML() + "\r\n";
			}
		} else {
			lastStep = "No Test Steps Logged";
		}
		String htmlRowEntry = "<tr>\r\n"
				+ "	<td>"+testModule+"</td>\r\n"
				+ "	<td>"+testCase+"</td>\r\n"
				+ "	<td>\r\n"
				+ "		<button class=\""+collapsible+"\"><b>"+passFail+":</b> "+lastStep+"</button>\r\n"
				+ "		<div class=\"content\">\r\n"
				+ testStepsHtml
				+ "		</div>\r\n"
				+ "	</td>\r\n"
				+ "	<td>\r\n"
				+ "		<a href=\""+ssPath+"\">\r\n"
				+ "			<img src=\""+ssPath+"\"/>\r\n"
				+ "		</a>\r\n"
				+ "	</td>\r\n"
				+ "  </tr>"+"\r\n<!--replace-->";
		if(testCaseStatus.passed()) {
			passedCount++;
		} else {
			failedCount++;
		}
		try {
			FileUtils.writeStringToFile(newHtmlFile, htmlRowEntry, "UTF-8", true);
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	@AfterSuite
	public void postSuite(){
		try {
			String postHtml = htmlSplit[1];
			postHtml = postHtml.replace("##", ""+passedCount);
			postHtml = postHtml.replace("$$", ""+failedCount);
			postHtml = postHtml.replace("%%", ""+debugCount);
			FileUtils.writeStringToFile(newHtmlFile, postHtml, "UTF-8", true);
			FileUtils.writeStringToFile(newRerunListFile, xmlSplit[1], "UTF-8", true);
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public boolean debug() {
		return config.getProperty("debug").equals("true");
	}
}
