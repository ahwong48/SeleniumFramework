import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;

import jdk.jfr.Timespan;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

public class ReusableMethods {
	File screenShotFolder = new File("./SS");
	File downloadsFolder = new File("./DL");
	PropertyLoader config = new PropertyLoader("./config.properties");
	PropertyLoader securityConfig = new PropertyLoader("./security.properties");
	PropertyLoader regEx = new PropertyLoader("./src/References/regEx.properties");
	WebDriver driver;
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
	public void beforeSuite(ITestContext context) {
		passedCount = 0;
		failedCount = 0;
		debugCount = 0;
		try {
			context.getSuite().getXmlSuite().setThreadCount(Integer.parseInt(config.getProperty("thread-count")));
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
			deleteFilesInFolder(screenShotFolder);
			deleteFilesInFolder(downloadsFolder);
		} catch (Exception e) {e.printStackTrace(); }
	}
	
	public void deleteFilesInFolder(File file) {
		try {
			String[] oldFiles = file.list();
			for(String oldFile : oldFiles) {
				File currentFile = new File(file.getPath(),oldFile);
				currentFile.delete();
			}
		} catch(Exception e) { System.out.println("WARNING: No Files to Delete");}
	}
	
	@BeforeTest
	public void setBrowserDriver() {
		DriverFactory b = new DriverFactory(config.getProperty("browser"));
		driver = b.getDriver();
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
	
	public void changeUrl(String url) {
		driver.get(url);
	}
	
	public void hoverElement(By by, String elementName) {
		if(debug()) {
			logStep("Hover Element Locator ["+by+"]", "Debug");
		}
		WebElement webElement = driver.findElement(by);
		hoverElement(webElement, elementName);
	}
	
	public void hoverElement(WebElement webElement, String elementName) {
		Actions action = new Actions(driver);
		action.moveToElement(webElement).perform();
		logStep("Mouse Hover Element ["+elementName+"]", "Passed");
	}
	
	public void switchToChildWindow() {
		ArrayList<String> windowList = new ArrayList<String>(driver.getWindowHandles());
	    driver.switchTo().window(windowList.get(windowList.size()-1));
	    logStep("Switched to Child window ["+driver.getTitle()+"]", "Passed", takeScreenshot("WindowSwitch"));
	}
	
	public void switchBackToParentWindow() {
		ArrayList<String> windowList = new ArrayList<String>(driver.getWindowHandles());
		driver.close();
	    driver.switchTo().window(windowList.get(0));
	    logStep("Switched back to Parent window ["+driver.getTitle()+"]", "Passed", takeScreenshot("WindowSwitch"));
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
		if(e.isDisplayed()) {
			clickElement(e, elementName);
		} else {
			logStep("Unable to find Element ["+elementName+"] to Click: ["+by.toString()+"]", "Failed");
		}
	}
	
	public void clickElement(WebElement e, String elementName) {
		e.click();
		logStep("Clicked Element: ["+elementName+"]", "Passed");
	}
	
	public void selectElement(String selectType, By by, String selectValue, String elementName) {
		WebElement e = findElement(by);
		if(debug()) {
			logStep("Select Element Locator: ["+by+"]", "Debug");
		}
		if(e != null) {
			switch(selectType) {
				case "index":
					selectElementIndex(e, selectValue, elementName);
					break;
				case "text":
				default:
					selectElementText(e, selectValue, elementName);
					break;
			}
		} else {
			logStep("Unable to find Element ["+elementName+"] or Select Value/Index ["+selectValue+"] not found/out of bounds: ["+by.toString()+"]", "Failed");
		}
	}
	
	public void selectElementText(WebElement e, String selectValue, String elementName) {
		Select select = new Select(e);
		select.selectByVisibleText(selectValue);
		logStep("Select Element: ["+elementName+"] | Element Selected ["+selectValue+"]", "Passed");
	}
	
	public void selectElementIndex(WebElement e, String index, String elementName) {
		Select select = new Select(e);
		int idx = Integer.parseInt(index);
		select.selectByIndex(idx);
		logStep("Select Element: ["+elementName+"] | Index Selected ["+index+"]", "Passed");
	}
	
	public void dragAndDrop(By start, By end, String startElementName, String endElementName) {
		WebElement s = findElement(start);
		WebElement e = findElement(end);
		
		if(s.isDisplayed() && e.isDisplayed()) {
			dragAndDrop(s, e, startElementName, endElementName);
		} else {
			logStep("Unable to find Elements [start:"+startElementName+" | end:"+endElementName+"]", "Failed");
		}
	}
	
	public void dragAndDrop(WebElement start, WebElement end, String startElementName, String endElementName) {
		if(debug()) {
			logStep("Is Start Element Available:"+ start.isDisplayed(), "Debug");
			logStep("Is End Element Available:"+ end.isDisplayed(), "Debug");
		}
		logStepSS("Drag and Drop: Before Drag and Drop", "Passed", "PreDnD");
		Actions act = new Actions(driver);
		act.dragAndDrop(start, end).build().perform();
//		String xto=Integer.toString(end.getLocation().x);
//	    String yto=Integer.toString(end.getLocation().y);
//	    ((JavascriptExecutor)driver).executeScript("function simulate(f,c,d,e){var b,a=null;for(b in eventMatchers)if(eventMatchers[b].test(c)){a=b;break}if(!a)return!1;document.createEvent?(b=document.createEvent(a),a==\"HTMLEvents\"?b.initEvent(c,!0,!0):b.initMouseEvent(c,!0,!0,document.defaultView,0,d,e,d,e,!1,!1,!1,!1,0,null),f.dispatchEvent(b)):(a=document.createEventObject(),a.detail=0,a.screenX=d,a.screenY=e,a.clientX=d,a.clientY=e,a.ctrlKey=!1,a.altKey=!1,a.shiftKey=!1,a.metaKey=!1,a.button=1,f.fireEvent(\"on\"+c,a));return!0} var eventMatchers={HTMLEvents:/^(?:load|unload|abort|error|select|change|submit|reset|focus|blur|resize|scroll)$/,MouseEvents:/^(?:click|dblclick|mouse(?:down|up|over|move|out))$/}; " +
//	    "simulate(arguments[0],\"mousedown\",0,0); simulate(arguments[0],\"mousemove\",arguments[1],arguments[2]); simulate(arguments[0],\"mouseup\",arguments[1],arguments[2]); ",
//	    start,xto,yto);
		logStepSS("Drag and Drop Element: [start:"+startElementName+"|end:"+endElementName+"]", "Passed", "PostDnD");
	}
	
	public void dragAndDropCss(String cssLocStart, String cssLocEnd, String startElementName, String endElementName) {
		try {
			if(debug()) {
				logStep("Css Start Selector ["+cssLocStart+"] | Css End Selector ["+cssLocEnd+"]", "Debug");
			}
			File file = new File("./dependencies/js/dragAndDrop.js");
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			fis.close();
			String dndjs = new String(data, "UTF-8");
			//----------------------------
			logStepSS("Drag and Drop: Before Drag and Drop", "Passed", "PreDnD");
			//----------------------------
			((JavascriptExecutor) driver).executeScript(dndjs+"$('"+cssLocStart+"').simulateDragDrop({ dropTarget: '"+cssLocEnd+"'});");
			//----------------------------
			logStepSS("Drag and Drop Element: [start:"+startElementName+"|end:"+endElementName+"]", "Passed", "PostDnD");
		} catch (Exception e) {
			logStep("Drag and Drop CSS failed ["+e.getMessage()+"]", "Failed");
		}
		
	}
	
	public void switchIFrame(int i) {
		driver.switchTo().frame(i);
		logStep("Switch IFrame to frame ["+i+"]", "Passed");
	}
	
	public void switchIFrameParent() {
		driver.switchTo().parentFrame();
		logStep("Switch IFrame to Parent Frame", "Passed");
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
	
	public void logStepSS(String testStep, String status, String ssID) {
		TestStepStatus tss = new TestStepStatus(testStep, status);
		String ssPath = takeScreenshot(ssID);
		tss.addScreenShot(ssPath);
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
	
	public void verifyGrid() {
		String tableBasePath = "";
		String rowBasePath = "";
		String columnIds = ";;;";
	}
	
	public void validateRegEx(String validate, String regExName, String elementName) {
		String pattern = regEx.getProperty(regExName);
		if(debug()) {
			System.out.println(pattern);
			logStep("Validate RegEx: ["+regExName+": '"+pattern+"'] on ["+elementName+"] with value ["+validate+"]", "Debug");
		}
		if(validate.matches(pattern)) {
			logStep("Element: ["+elementName+": '"+validate+"'] Matched with RegEx: ["+regExName+": '"+pattern+"']", "Passed");
		} else {
			logStep("Element: ["+elementName+": '"+validate+"'] Failed Match with RegEx: ["+regExName+": '"+pattern+"']", "Failed");
		}
		
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
		driver.quit();
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
				+ "		<a href=\""+ssPath+"\" target=\"_blank\">\r\n"
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
