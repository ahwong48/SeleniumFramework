package FrameworkBase;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.*;

import Locators.LocGeneralSFP;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

public class ReusableMethods {
    File screenShotFolder = new File(separatorCompatibility("./SS"));
    File downloadsFolder = new File(separatorCompatibility("./DL"));
    File compareFolder = new File(separatorCompatibility("./rerun"));
    File allureReportsFolder = new File("./allure-results");
    protected PropertyLoader config = new PropertyLoader("./properties/config.properties");
//    protected PropertyLoader securityConfig = new PropertyLoader("./properties/security.properties");
    protected PropertyLoader inputData =  new PropertyLoader("./properties/inputData.properties");
    protected PropertyLoader regEx = new PropertyLoader("./src/References/regEx.properties");
    protected PropertyLoader security = new PropertyLoader("./properties/security.properties");
    protected WebDriver driver;
    protected String home = config.getProperty("home");
    String tcName;
    String tcClass;
    String tcModule = ""; //TODO : get this set somewhere
    String tcStatus;
    String htmlString;
    static String[] htmlSplit;
    String xmlString;
    static String[] xmlSplit;
    protected TestCaseStatus testCaseStatus;
    static int passedCount;
    static int failedCount;
    static int debugCount;
    static File newHtmlFile;
    static File newFailedItemsFile;
    static File newRerunListFile;
    static File newRerunBase;
    static File newComparePass;
    static File newCompareFail;
    static File lastRunCounts;
    long startTime;
    long endTime;

    @BeforeSuite
    public void beforeSuite(ITestContext context) {
        if(!rerun()) {
            deleteFilesInFolder(screenShotFolder);
            deleteFilesInFolder(downloadsFolder);
            deleteFilesInFolder(compareFolder);
            deleteFilesInFolder(allureReportsFolder);
        }

        startTime = System.nanoTime();
        passedCount = 0;
        failedCount = 0;
        debugCount = 0;
        try {
            context.getSuite().getXmlSuite().setThreadCount(Integer.parseInt(config.getProperty("thread-count")));
            File htmlTemplateFile = new File(separatorCompatibility("./Templates/htmlReportTemplate.html"));
            File rerunTemplateFile = new File(separatorCompatibility("./Templates/testNgXmlTemplate.xml"));
            String app = config.getProperty("appName");
            String timestamp = timestamp();
            newHtmlFile = new File(separatorCompatibility("./"+config.getProperty("reportName")+".html"));
            newFailedItemsFile = new File(separatorCompatibility("./"+config.getProperty("reportName")+"_OnlyFailures.html"));
            newRerunListFile = new File(separatorCompatibility("./rerun/testNgRerun.xml"));
            newRerunBase = new File(separatorCompatibility("./rerun/rerunReportBase.html"));
            newComparePass = new File(separatorCompatibility("./rerun/comparePass.txt"));
            newCompareFail = new File(separatorCompatibility("./rerun/compareFail.txt"));
            lastRunCounts = new File(separatorCompatibility("./rerun/lastRunCount.txt"));

            xmlString = FileUtils.readFileToString(rerunTemplateFile,"UTF-8");
            xmlSplit = xmlString.split("<!--replace-->");
            System.out.println(xmlSplit[0]);
            FileUtils.writeStringToFile(newRerunListFile, xmlSplit[0], "UTF-8", false);
            if(!rerun()) {
                htmlString = FileUtils.readFileToString(htmlTemplateFile, "UTF-8");
                htmlString = htmlString.replace("@Application@", app);
                FileUtils.writeStringToFile(newComparePass, "", "UTF-8", false);
                FileUtils.writeStringToFile(newCompareFail, "", "UTF-8", false);
            } else {
                htmlString = FileUtils.readFileToString(newRerunBase, "UTF-8");
                htmlSplit = htmlString.split("<!--replace-->");
            }
            htmlSplit = htmlString.split("<!--replace-->");
            FileUtils.writeStringToFile(newRerunBase, htmlSplit[0], "UTF-8", false);
            htmlSplit[0] = htmlSplit[0].replace("@timestamp@", timestamp);
            FileUtils.writeStringToFile(newHtmlFile, htmlSplit[0], "UTF-8", false);
            FileUtils.writeStringToFile(newFailedItemsFile, htmlSplit[0], "UTF-8", false);
            newCompareFail.createNewFile();
            FileUtils.writeStringToFile(newCompareFail, "", "UTF-8", false);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteFilesInFolder(File file) {
        try {
            String[] oldFiles = file.list();
            for(String oldFile : oldFiles) {
                File currentFile = new File(file.getPath(),oldFile);
                currentFile.delete();
            }
        } catch(Exception e) {
            System.out.println("WARNING: No Files to Delete");
        }
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

    public WebDriver getWebDriver() {
        return driver;
    }

    public void launchBrowser() {
        launchBrowser(config.getProperty("home"));
    }

    public void verifyText(String textString, String expectedString, String elementName) {
        String verifyStatus;
        if(textString.equals(expectedString)) {
            verifyStatus = "Passed";
        } else {
            verifyStatus = "Failed";
        }
        logStep("Verify ["+elementName+"] Text: element ["+textString+"] and expected ["+expectedString+"]", verifyStatus);
    }

    public void launchBrowser(String url) {
        System.out.println(url);
        driver.get(url);
        driver.manage().window().maximize();
        logStep("Opened URL: "+url, "Passed");
        sleep(3000);
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

    public WebElement findElementDisplay(By by) {
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
            element = null;
        }
        return element;
    }

    public ArrayList<WebElement> findElements(By by) {
        if(isElementDisplayed(by, "find element")) {
            return (ArrayList<WebElement>) driver.findElements(by);
        } else {
            return null;
        }
    }

    public String getElementTextNoTextAlt(By text, By noTextAlt) {
        if(isElementDisplayed(noTextAlt, "Non-text Locator ["+noTextAlt+"]")) {
            return "";
        } else {
            return getElementText(text);
        }
    }

    public String getHighchartsText (By by) {
        if(waitShortVisible(by, "getHighchartsText")) {
            WebElement element1 = driver.findElement(by);
            return getHighchartsText(element1);
        } else {
            return "[ERROR]:NO ELEMENT VISIBLE";
        }
    }

    public String getHighchartsText (WebElement e) {
        scrollToCenterElement(e);
        return e.getText();
    }

    public String getElementText(By by) {
        if(waitShortVisible(by, "getElementText")) {
            WebElement e = findElement(by);
            return getElementText(e);
        } else {
            logStep("[ERROR]: ELEMENT ["+by+"] NOT VISIBLE", "Failed");
            return "[ERROR]:NO ELEMENT VISIBLE";
        }
    }

    public String getElementText(WebElement e) {
        scrollToElement(e);
        return e.getAttribute("innerText");
    }
    
    public String getElementAttribute(By by, String attribute) {
        return getElementAttribute(findElement(by), attribute);
    }
    
    public String getElementAttribute(WebElement we, String attribute) {
        return we.getAttribute(attribute);
    }

    public String getInputElementText(By by) {
        if(waitShortVisible(by, "getInputElementText")) {
            WebElement e = findElement(by);
            return getInputElementText(e);
        } else {
            logStep("[ERROR]: ELEMENT ["+by+"] NOT VISIBLE", "Failed");
            return "[ERROR]: NO ELEMENT VISIBLE";
        }
    }

    public String getInputElementText(WebElement e) {
        scrollToElement(e);
        return e.getAttribute("value");
    }

    public String getElementHyperlink(By by) {
        if(waitShortVisible(by, "getElementHyperlink")) {
            WebElement e = findElement(by);
            return getElementHyperlink(e);
        } else {
            return "[ERROR]:NO ELEMENT VISIBLE";
        }
    }

    public String getElementHyperlink(WebElement e) {
        scrollToElement(e);
        return e.getAttribute("href");
    }

    public String getCSSElementText(WebElement e) {
        scrollToElement(e);
        return e.getAttribute("innerHTML");
    }

    public void scrollToElement(WebElement e) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", e);
    }

    public void scrollToCenterElement(By by) {
        scrollToCenterElement(findElement(by));
    }

    public void scrollToCenterElement(WebElement e) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoViewIfNeeded(true);", e);
    }

    public void scrollHoriz250(By scrollableGridLocator) {
        scrollHoriz250(driver.findElement(scrollableGridLocator));
    }

    public void scrollHoriz250(WebElement e) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollLeft -= 250", e);
    }

    public void scrollHorizNeg250(By scrollableGridLocator) {
        scrollHorizNeg250(driver.findElement(scrollableGridLocator));
    }

    public void scrollHorizNeg250(WebElement e) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollLeft -= 250", e);
    }

    public void scrollHorizFront(By scrollableGridLocator) {
        scrollHorizFront(driver.findElement(scrollableGridLocator));
    }

    public void scrollHorizFront(WebElement e) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollLeft = 0", e);
    }

    public void scrollHorizEnd(By scrollableGridLocator) {
        scrollHorizEnd(driver.findElement(scrollableGridLocator));
    }

    public void scrollHorizEnd(WebElement e) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollLeft = arguments[0].offsetWidth", e);
    }

    public void inputText(By by, String s, String elementName) {
        if(debug()) {
            logStep("Input Text Locator: ["+by+"]", "Debug");
        }
        if(waitShortVisible(by, elementName)) {
            WebElement e = findElement(by);
            inputText(e, s, elementName);
            logStep("Input Text ["+s+"] in Element: ["+elementName+"]", "Passed");
        } else {
            logStep("Unable to find Element ["+elementName+"] to input Text: ["+by.toString()+"]", "Failed");
        }
    }

    public void inputText(WebElement e, String s, String elementName) {
        scrollToElement(e);
        e.clear();
        clickElement(e, elementName);
        e.sendKeys(s);
    }

    public void inputTextNoClear(By by, String s, String elementName) {
        if(debug()) {
            logStep("Input Text Without Clear Locator: ["+by+"]", "Debug");
        }
        if(waitShortVisible(by, elementName)) {
            WebElement e = findElement(by);
            inputTextNoClear(e, s, elementName);
            logStep("Input Text Without Clear ["+s+"] in Element: ["+elementName+"]", "Passed");
        } else {
            logStep("Unable to find Element ["+elementName+"] to input Text without Clear: ["+by.toString()+"]", "Failed");
        }
    }

    public void inputTextNoClear(WebElement e, String s, String elementName) {
        scrollToElement(e);
        clickElement(e, elementName);
        e.sendKeys(s);
    }

    public void handleAlert(boolean accept) {
        if(accept) {
            driver.switchTo().alert().accept();
        } else {
            driver.switchTo().alert().dismiss();
        }
    }

    public void clickElement(By by, String elementName) {
        System.out.println("Click Element Locator: ["+by+"]");
        if(debug()) {
            logStep("Click Element Locator: ["+by+"]", "Debug");
        }
        if(waitShortVisible(by, elementName)) {
            WebElement e = findElement(by);
            clickElement(e, elementName);
        } else {
            logStep("Unable to find Element ["+elementName+"] to Click: ["+by.toString()+"]", "Failed");
        }
    }

    public void clickElement(WebElement e, String elementName) {
        scrollToElement(e);
        e.click();
        logStep("Clicked Element: ["+elementName+"]", "Passed");
    }
    
    public void clickElementPopup(By by, String elementName, boolean accept) {
        System.out.println("Click Element Locator: ["+by+"]");
        if(debug()) {
            logStep("Click Element Locator: ["+by+"]", "Debug");
        }
        if(waitShortVisible(by, elementName)) {
            WebElement e = findElement(by);
            scrollToElement(e);
            e.click();
            handleAlert(accept);
            logStep("Clicked Element: ["+elementName+"]", "Passed");
        } else {
            logStep("Unable to find Element ["+elementName+"] to Click: ["+by.toString()+"]", "Failed");
        }
    }
    
    public void clickElementNoScroll(WebElement e, String elementName) {
        e.click();
        logStep("Clicked Element: ["+elementName+"]", "Passed");
    }

    public void doubleClickElement(By by, String elementName) {
        if(debug()) {
            logStep("Click Element Locator: ["+by+"]", "Debug");
        }
        if(waitShortVisible(by, elementName)) {
            WebElement e = findElement(by);
            doubleClickElement(e, elementName);
        } else {
            logStep("Unable to find Element ["+elementName+"] to Click: ["+by.toString()+"]", "Failed");
        }
    }

    public void doubleClickElement(WebElement e, String elementName) {
        scrollToElement(e);
        Actions act = new Actions(driver);
        act.doubleClick(e).perform();
        logStep("Double Clicked Element: ["+elementName+"]", "Passed");
    }

    public void selectElement(String selectType, By by, String selectValue, String elementName) {
        if(debug()) {
            logStep("Select Element Locator: ["+by+"]", "Debug");
        }
        if(waitShortVisible(by, elementName)) {
            WebElement e = findElement(by);
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
        scrollToElement(e);
        Select select = new Select(e);
        select.selectByVisibleText(selectValue);
        logStep("Select Element: ["+elementName+"] | Element Selected ["+selectValue+"]", "Passed");
    }

    public void selectElementIndex(WebElement e, String index, String elementName) {
        scrollToElement(e);
        Select select = new Select(e);
        int idx = Integer.parseInt(index);
        select.selectByIndex(idx);
        logStep("Select Element: ["+elementName+"] | Index Selected ["+index+"]", "Passed");
    }
    
    public void sfpSelectElement(String selectType, String ngModel, String position, String selectValue, String elementName) {
        /*
         * selectType - chooe between 'text' or 'index'
         * ngModel - please only provide the ng-model attribute
         * position - eg. for cashflow page there are 10 entries possible, but default if you don't know should be 1
         * selectValue - index: should be a number | text: should be an exact text string of element
         * elementName - name of the element
         */
        String[] replace = {ngModel, position};
        By dropdownDyn = dynXpath(By.xpath("(//sfp-select[@ng-model='@1@'])[@2@]"),replace);
        if(debug()) {
            logStep("Select Element Locator: ["+dropdownDyn+"]", "Debug");
        }
        if(waitShortVisible(dropdownDyn, elementName)) {
            clickElement(dropdownDyn, "Element: ["+elementName+"] sfpSelect ["+ngModel+"]");
            String[] rep = {ngModel, position, selectValue};
            switch(selectType) {
                case "index":
                    dropdownDyn = By.xpath("((//sfp-select[@ng-model='@1@'])[@2@]//div[@ng-click='select(item);'])[@3@]");
                    clickElement(dynXpath(dropdownDyn, rep), "Element: ["+elementName+"] index: ["+selectValue+"]");
                    break;
                case "text":
                default:
                    dropdownDyn = By.xpath("((//sfp-select[@ng-model='@1@'])[@2@]//div[text()=\"@3@\"])");
                    clickElement(dynXpath(dropdownDyn, rep), "Element: ["+elementName+"] text: ["+selectValue+"]");
                    break;
            }
        } else {
            logStep("Unable to find Element ["+elementName+"] or Select Value/Index ["+selectValue+"] not found/out of bounds: ["+dropdownDyn.toString()+"]", "Failed");
        }
    }

    public void mdSelectDropdownOption(By dropdownMenu, By dropdownOption, String[] elements) {
        mdSelectDropdownOption(dropdownMenu, dropdownOption, LocGeneralSFP.footer, elements); // if no hoverAway element defined, use footer
    }

    /**
     * Method for selecting a given option from an md-select html element.
     * @param dropdownMenu xPath of the dropdown menu
     * @param dropdownOption xpath of the option in the dropdown menu
     * @param hoverAway xpath of an onscreen element to hover over in case an overlapping menu is opened by hovering
     * @param elementDescriptions an array of 2 strings. The first describes the dropdown menu and the second describes the option being selected
     */
    public void mdSelectDropdownOption(By dropdownMenu, By dropdownOption, By hoverAway, String[] elementDescriptions) {
        /* open dropdown menu and wait for option to be visible */
        clickElement(dropdownMenu, elementDescriptions[0]);
        if(waitShortVisible(dropdownOption, elementDescriptions[1])) {
            WebElement e = findElement(dropdownOption);
            scrollToCenterElement(e);
            hoverElement(hoverAway, "hover over element to close any opened overlapping menus");
            e.click();
            logStep("Clicked Element: ["+elementDescriptions[1]+"]", "Passed");
        } else {
            logStep("Unable to find Element ["+elementDescriptions[1]+"] to Click: ["+dropdownOption.toString()+"]", "Failed");
        }
    }

    /**
     * Method for selecting an option from an sfp-select
     * @param openSelect xpath of the sfp-select
     * @param option xpath of the option to be selected
     * @param elementDescriptions array of two strings describing previous two params respectively
     */
    public void sfpSelectElement(By openSelect, By option, String[] elementDescriptions) {
        clickElement(openSelect, elementDescriptions[0]);
        clickElement(option, elementDescriptions[1]);
    }

    public void dragAndDrop(By start, By end, String startElementName, String endElementName) {
        if(waitShortVisible(start, startElementName) && waitShortVisible(end, endElementName)) {
            WebElement s = findElement(start);
            WebElement e = findElement(end);
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
//        String xto=Integer.toString(end.getLocation().x);
//        String yto=Integer.toString(end.getLocation().y);
//        ((JavascriptExecutor)driver).executeScript("function simulate(f,c,d,e){var b,a=null;for(b in eventMatchers)if(eventMatchers[b].test(c)){a=b;break}if(!a)return!1;document.createEvent?(b=document.createEvent(a),a==\"HTMLEvents\"?b.initEvent(c,!0,!0):b.initMouseEvent(c,!0,!0,document.defaultView,0,d,e,d,e,!1,!1,!1,!1,0,null),f.dispatchEvent(b)):(a=document.createEventObject(),a.detail=0,a.screenX=d,a.screenY=e,a.clientX=d,a.clientY=e,a.ctrlKey=!1,a.altKey=!1,a.shiftKey=!1,a.metaKey=!1,a.button=1,f.fireEvent(\"on\"+c,a));return!0} var eventMatchers={HTMLEvents:/^(?:load|unload|abort|error|select|change|submit|reset|focus|blur|resize|scroll)$/,MouseEvents:/^(?:click|dblclick|mouse(?:down|up|over|move|out))$/}; " +
//        "simulate(arguments[0],\"mousedown\",0,0); simulate(arguments[0],\"mousemove\",arguments[1],arguments[2]); simulate(arguments[0],\"mouseup\",arguments[1],arguments[2]); ",
//        start,xto,yto);
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
        } catch(Exception e) {
            logStep("Drag and Drop CSS failed ["+e.getMessage()+"]", "Failed");
        }
    }
    
    public void switchIFrame(By by) {
        switchIFrame(findElement(by));
    }

    public void switchIFrame(WebElement we) {
        driver.switchTo().frame(we);
        logStep("Switch IFrame to frame ["+we+"]", "Passed");
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

    public By dynXpath(By by, String replaceString, String searchString) {
        String xpath = by.toString();
        xpath = xpath.substring(10);
        xpath = xpath.replace(searchString, replaceString);
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
//        org.testng.Assert.
        String text1 = getElementText(by);
        verifyText(text1, verify, elementName);
    }

    // maybe take an inputstring -> # of columns + name the columns (eg. "Column1;Column2;Column3;Column4")
//    public void verifyTable(String gridName, String columnHeaders, String verificationValues) {
//        String tableBasePath = "";
//        String rowBasePath = "";
//        String columnPath = "";
//        String[] verify = verificationValues.split(";");
//
//        String xpath = tableBasePath + rowBasePath;
//        for(String v : verify) {
//            xpath += (columnPath.replace("@1@", v));
//        }
//        if(isElementDisplayed(By.xpath(xpath), gridName)) {
//
//        }
//    }

    public String generateTableXpathHeader(String metadata) {
        String gen = "//th";
        String following = "//following-sibling::th";
        String[] md = metadata.split(";");
        return generateTableXpathGeneral(gen, following, md);
    }

    public String generateTableXpathRow(String metadata) {
        String gen = "//td";
        String following = "//following-sibling::td";
        String[] md = metadata.split(";");
        return generateTableXpathGeneral(gen, following, md);
    }

    public String generateTableXpathGeneral(String gen, String following, String[] md) {
        for(int i = 0; i < md.length; i++) {
            String meta = md[i];
            if(i != 0) {
                gen += following;
            }
            gen += "["+meta+"]";
            i++;
        }
        return gen;
    }

    public boolean waitForElementDisappear(By by, String elementName) {
        WebDriverWait wait = new WebDriverWait(driver, 4*Integer.parseInt(config.getProperty("waitTime")));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
        int itemsFound = driver.findElements(by).size();
        if(debug()) { logStep("#"+itemsFound+" of Elements found for ["+elementName+" | "+by.toString()+"]", "Debug"); }
        if(itemsFound <= 0) {
            return true;
        } else {
            if(debug()) {
                logStep("Element ["+elementName+" | "+by.toString()+"] was still visible", "Failed");
            }
            return false;
        }
    }

    public boolean waitShortDisappear(By by, String elementName) {
        WebDriverWait wait = new WebDriverWait(driver, Integer.parseInt(config.getProperty("waitTime")));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
        int itemsFound = driver.findElements(by).size();
        if(debug()) {
            logStep("#"+itemsFound+" of Elements found for ["+elementName+" | "+by.toString()+"]", "Debug");
        }
        if(itemsFound <= 0) {
            return true;
        } else {
            if(debug()) {
                logStep("Element ["+elementName+" | "+by.toString()+"] was still visible", "Failed");
            }
            return false;
        }
    }

    public boolean waitForElementVisible(By by, String elementName) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, 4*Integer.parseInt(config.getProperty("waitTime")));
            wait.until(ExpectedConditions.visibilityOfElementLocated(by));
            int itemsFound = driver.findElements(by).size();
            if(debug()) {
                logStep("#"+itemsFound+" of Elements found for ["+elementName+" | "+by.toString()+"]", "Debug");
            }
            return (itemsFound > 0);
        } catch(Exception e) {
            if(debug()) {
                logStep("Element ["+elementName+" | "+by.toString()+"] was not found", "Failed");
            }
            System.out.println("Element ["+elementName+"] was not found - timed out");
            e.printStackTrace();
            return false;
        }
    }

    public boolean waitShortVisible(By by, String elementName) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Integer.parseInt(config.getProperty("waitTime")));
            wait.until(ExpectedConditions.visibilityOfElementLocated(by));
            int itemsFound = driver.findElements(by).size();
            if(debug()) {
                logStep("#"+itemsFound+" of Elements found for ["+elementName+" | "+by.toString()+"]", "Debug");
            }
            return (itemsFound > 0);
        } catch(Exception e) {
            if(debug()) {
                logStep("Element ["+elementName+" | "+by.toString()+"] was not found", "Failed");
            }
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean isDev() {
        return config.getProperty("env").equals("dev");
    }

    public boolean isElementDisplayed(By by, String elementName) {
        return isElementDisplayed(findElementDisplay(by), elementName);
    }

    public boolean isElementDisplayed(WebElement we, String elementName) {
        try {
            if(we != null) {
                scrollToElement(we);
                return true;
            } else {
                return false;
            }
        } catch(Exception e) {
            return false;
        }
    }

    public boolean isElementDisplayedVerification(By by, String elementName) {
        if(isElementDisplayed(by, elementName)) {
            logStep("Element ["+elementName+"] was found", "Passed");
            return true;
        } else {
            logStep("DEBUG: "+by, "DEBUG");
            System.out.println("Failed to find DEBUG: "+by);
            logStep("Element ["+elementName+"] was not found", "Failed");
            return false;
        }
    }

    public boolean isElementRemovedVerification(By by, String elementName) {
        if(isElementDisplayed(by, elementName)) {
            logStep("Element ["+elementName+"] was found", "Failed");
            return false;
        } else {
            logStep("Element ["+elementName+"] was not found", "Passed");
            return true;
        }
    }

    /**
     * Method for checking an md-checkbox's state.
     * @param checkboxLoc xpath of the checkbox
     * @param checkboxDescriptor a string describing the checkbox
     */
    public boolean isCheckboxChecked(By checkboxLoc, String checkboxDescriptor) {
        WebElement checkbox = driver.findElement(checkboxLoc);
        return isCheckboxChecked(checkbox, checkboxDescriptor);
    }

    public boolean isCheckboxChecked(WebElement e, String checkboxDescriptor) {
        String classes = e.getAttribute("class");
        if(!(classes.equals("") || classes == null)) {
            for(String c : classes.split(" ")) {
                if(c.equals("md-checked") || c.equals("ng-not-empty")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Method for setting a checkbox's state.
     * @param checkboxLoc xpath of the checkbox
     * @param desiredState a boolean that represents the desired end state of the checkbox
     * @param checkboxDescriptor a string describing the checkbox
     */
    public void setCheckboxState(By checkboxLoc, boolean desiredState, String checkboxDescriptor) {
        boolean currentState = isCheckboxChecked(checkboxLoc, checkboxDescriptor);
        if(currentState != desiredState) {
            clickElement(checkboxLoc, checkboxDescriptor);
            currentState = isCheckboxChecked(checkboxLoc, checkboxDescriptor); // update currentState after clicking
            if(currentState == desiredState){
                logStep("["+checkboxDescriptor+"] Checkbox successfully set to ["+desiredState+"]", "Passed");
            } else {
                logStep("Failed to set ["+checkboxDescriptor+"] Checkbox to ["+desiredState+"]", "Failed");
            }
        } else {
            logStep("["+checkboxDescriptor+"] Checkbox already set to ["+desiredState+"]", "Passed");
        }
    }

    public void displayMoreTableRows(By tableNumPerPage, By tableNumPerPageOption, By tableRows, String selection) {
        mdSelectDropdownOption(
            tableNumPerPage,
            dynXpath(tableNumPerPageOption, selection),
            new String[] {"number per page selection dropdown", "\""+selection+"\" rows per page option"}
        );
        waitForElementVisible(tableRows, "updated table rows");
    }

    public void changeTablePage(int pageNum, By tablePageNums) {

        clickElement(
            dynXpath(tablePageNums, Integer.toString(pageNum)),
            "table page \""+pageNum+"\""
        );
    }

    public void changeTablePage(String direction, By tableChange, By tableRows) {
        clickElement(
            dynXpath(tableChange, direction),
            "table navigation - \""+direction+"\""
        );
        waitShortVisible(tableRows, "updated table rows");
    }

    public boolean tableFilterSearch(By tableSearchInput, By tableResult, String searchTerm) {
        /* enter search term */
        inputText(tableSearchInput, searchTerm, "table search bar");
        /* check if any result is displayed */
        boolean resultFound = isElementDisplayed(tableResult, "table filter result");
        return resultFound;
    }

    public boolean isColumnChooserOpen(By columnChooserLoc) {
        boolean isColumnChooserDisplayed = isElementDisplayed(columnChooserLoc, "column chooser");
        return isColumnChooserDisplayed;
    }

    /**
     * gets the text of selected or available columns in the column chooser
     * @param columnChooserFieldsLoc xpath of columns in the column chooser on the selected or available side
     * @return array of columns
     */
    public String[] getColumnChooserColumnNames(By columnChooserFieldsLoc) {
        ArrayList<WebElement> columns = findElements(columnChooserFieldsLoc);
        ArrayList<String> columnNames = new ArrayList<String>();
        for(WebElement column : columns) {
            columnNames.add(column.getText());
        }
        return columnNames.toArray(new String[columnNames.size()]);
    }

    /**
     * add or remove columns from a table column chooser
     * @param columnChooserFieldsLoc xpath of a column in the column chooser on the selected or available side for removal or addition respectively
     * @param changingColumns an array of the columns that will be either added or removed
     */
    public void columnChooserAddRemoveColumns(By columnChooserFieldsLoc, String[] changingColumns) {
        ArrayList<WebElement> columns = findElements(columnChooserFieldsLoc);
        int numColumns = columns.size();
        ArrayList<String> addColumns = new ArrayList<String>(Arrays.asList(changingColumns));
        for(int i=0; i<numColumns; i++) {
            if(debug()) {
                System.out.println("i="+i+" column: "+columns.get(i).getText());
            }
            if(addColumns.contains(columns.get(i).getText())) {
                clickElement(columns.get(i), "Column chooser column \""+columns.get(i).getText()+"\"");
                doubleClickElement(columns.get(i), "Column chooser column \""+columns.get(i).getText()+"\"");
                i--;
                numColumns--;
            }
        }
    }
    
    public String regExDevOrNot(String devRegex, String nonDevRegex) {
        if(isDev()) {
            return devRegex;
        } else {
            return nonDevRegex;
        }
    }

    public void validateRegEx(String validate, String regExName, String elementName) {
        String pattern = regEx.getProperty(regExName);
        if(validateRegExLogFailureOnly(validate, regExName, elementName)) {
            logStep("Element: ["+elementName+": '"+validate+"'] Matched with RegEx: ["+regExName+": '"+pattern+"']", "Passed");
        }
    }

    public boolean validateRegExLogFailureOnly(String validate, String regExName, String elementName) {
        try {
            String pattern = regEx.getProperty(regExName);
            if(pattern==null) {
                logStep("regex pattern ["+regExName+"] not found", "Failed");
                return false;
            }
            if(debug()) {
                System.out.println(pattern);
                logStep("Validate RegEx: ["+regExName+": '"+pattern+"'] on ["+elementName+"] with value ["+validate+"]", "Debug");
                if(regExName.equals("rx_TODO")) {
                    logStep("Element: ["+elementName+"] RegEx: ["+regExName+"] - TODO - PLEASE UPDATE THIS WITH VALID RegEx", "Failed");
                }
            }
            if(!validate.matches(pattern)) {
                logStep("Element: ["+elementName+": '"+validate+"'] Failed Match with RegEx: ["+regExName+": '"+pattern+"']", "Failed");
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(e.getLocalizedMessage().contains("null")) {
                logStep(e.getLocalizedMessage()+" exception: check if regEx is correct", "Failed");
            }
            else {
                logStep(e.getLocalizedMessage()+" exception", "Failed");
            }
            return false;
        }
    }

    public void waitForPageLoaded() {
        int time = 30;
        while(time > 0 && !((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete")) {
            try {
                Thread.sleep(1000);
            } catch(Exception e) {
                e.printStackTrace();
            }
            System.out.println(((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete"));
            time--;
        }
    }

    public void wait(int seconds) {
        try {
            Thread.sleep(1000*seconds);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String takeScreenshot(String ssDesc) {
        File scrFile = ((TakesScreenshot) this.driver).getScreenshotAs(OutputType.FILE);
        String filePath = separatorCompatibility("./SS/"+tcName+"-"+timestamp()+"-SS-"+ssDesc+ ".png");
        try {
            FileUtils.copyFile(scrFile, new File(filePath));
        } catch(Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }

    public String timestamp(String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Date date = new Date();
        return sdf.format(date);
    }

    public String timestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH.mm.ss");
        Date date = new Date();
        return sdf.format(date);
    }

    public String timestampYYYYMMDD() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdds");
        Date date = new Date();
        return sdf.format(date);
    }

    public String timestamp_ddMMMyyyy() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
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
        testCaseStatus.endTestCase();

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
            try {
                FileUtils.writeStringToFile(newRerunListFile, rerunString, "UTF-8", true);
            } catch(Exception e) {
                e.printStackTrace();
            }
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
                + "    <td>"+testModule+"</td>\r\n"
                + "    <td>"+testCase+"</td>\r\n"
                + "    <td>\r\n"
                + "        <button class=\""+collapsible+"\"><b>"+passFail+":</b> "+lastStep+"</button>\r\n"
                + "        <div class=\"content\">\r\n"
                + testStepsHtml
                + "        </div>\r\n"
                + "    </td>\r\n"
                + "    <td>\r\n"
                + "        <a href=\""+ssPath+"\" target=\"_blank\">\r\n"
                + "            <img src=\""+ssPath+"\"/>\r\n"
                + "        </a>\r\n"
                + "    </td>\r\n"
                + "    <td>"+convertNanoToTime(testCaseStatus.getElapsedTime())
                + "    </td>\r\n"
                + "  </tr>"+"\r\n";
        if(testCaseStatus.passed()) {
            passedCount++;
            try {
                FileUtils.writeStringToFile(newRerunBase, htmlRowEntry, "UTF-8", true);
                FileUtils.writeStringToFile(newComparePass, testCase+",PASS\n", "UTF-8", true);
            } catch(Exception e) {
                e.printStackTrace();
            }
        } else {
            failedCount++;
            try {
                FileUtils.writeStringToFile(newFailedItemsFile, htmlRowEntry, "UTF-8", true);
                FileUtils.writeStringToFile(newCompareFail, testCase+",FAIL\n", "UTF-8", true);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        try {
            FileUtils.writeStringToFile(newHtmlFile, htmlRowEntry, "UTF-8", true);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @AfterSuite
    public void postSuite() {
        try {
            FileUtils.writeStringToFile(newRerunBase, "<!--replace-->", "UTF-8", true); // for splitting next run
            FileUtils.writeStringToFile(newRerunBase, htmlSplit[1], "UTF-8", true);
            endTime = System.nanoTime();
            String postHtml = htmlSplit[1];
            if(rerun()) {
                FileReader fr = new FileReader(lastRunCounts);
                BufferedReader br = new BufferedReader(fr);
                String[] passed = br.readLine().split(";");
                String[] failed = br.readLine().split(";");
                br.close();
                int fail =Integer.parseInt(failed[1]);
                fail = fail - passedCount;
                passedCount += Integer.parseInt(passed[1]);
                if(fail >= 0) {failedCount = fail;}
                
            }
            postHtml = postHtml.replace("$$", ""+failedCount);
            postHtml = postHtml.replace("@elapsedTime@", convertNanoToTime(endTime-startTime));
            FileUtils.writeStringToFile(newFailedItemsFile, postHtml, "UTF-8", true);
            postHtml = postHtml.replace("##", ""+passedCount);
            postHtml = postHtml.replace("%%", ""+debugCount);
            FileUtils.writeStringToFile(newHtmlFile, postHtml, "UTF-8", true);
            FileUtils.writeStringToFile(newRerunListFile, xmlSplit[1], "UTF-8", true);
            FileUtils.writeStringToFile(lastRunCounts, "Passed;"+passedCount+"\nFailed;"+failedCount+"\n", "UTF-8", false);

            File compareOutput = new File(separatorCompatibility("./rerun/compareOutput.csv"));
            FileUtils.writeStringToFile(compareOutput, "Test Case,Status\n", "UTF-8", false);
            String input = "";
            if(newComparePass.exists()) {
                input = FileUtils.readFileToString(newComparePass, "UTF-8");
                FileUtils.writeStringToFile(compareOutput, input, "UTF-8", true);
            }
            if(newCompareFail.exists()) {
                input = FileUtils.readFileToString(newCompareFail, "UTF-8");
                FileUtils.writeStringToFile(compareOutput, input, "UTF-8", true);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @AfterSuite @BeforeSuite
    public void createEmptyFiles() { // used to populate the folders
        File emptyDLFile = new File(separatorCompatibility("./DL/empty.txt"));
        File emptySSFile = new File(separatorCompatibility("./SS/empty.txt"));
        File emptyCompareFile = new File(separatorCompatibility("./rerun/empty.txt"));
        try {
            FileUtils.writeStringToFile(emptyDLFile, "", "UTF-8", false);
            FileUtils.writeStringToFile(emptySSFile, "", "UTF-8", false);
            FileUtils.writeStringToFile(emptyCompareFile, "", "UTF-8", false);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch(Exception e) {
            e.printStackTrace();
            logStep("Wait for ["+milliseconds+"] milliseconds Failed", "Failed");
        }
    }

    public boolean debug() {
        return config.getProperty("debug").equals("true");
    }

    public boolean rerun() {
        return config.getProperty("rerun").equals("true");
    }

    public String convertNanoToTime(long nano) {
        int hour = 0;
        int mins = 0;
        int secs = 0;
        double seconds = (double)nano / 1_000_000_000.0;
        mins = (int) Math.floor(seconds/60);
        secs = (int) seconds - (mins*60);
        hour = (int) Math.floor(mins/60);
        mins -= (hour*60);
        return String.format("%02d", hour)+":"+String.format("%02d", mins)+":"+String.format("%02d", secs);
    }

    public void sendEnter(By by, String elementName) {
        sendEnter(driver.findElement(by), elementName);
    }

    public void sendEnter(WebElement we, String elementName) {
        we.sendKeys(Keys.RETURN);
        logStep("Enter Key Pressed for ["+elementName+"]", "Passed");
    }

    public String getInputData(String propertyName) {
        return inputData.getProperty(propertyName);
    }

    public String getCurrentURL() {
        return driver.getCurrentUrl();
    }

    public String[] getTableConfigFromReferenceFile(String filename) {
        List<String> lines = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = null;
            while((line = br.readLine()) != null) {
                lines.add(line);
            }
            if(debug()) {
                System.out.println("printing out tableColumns from file");
                for(String string: lines.toArray(new String[lines.size()])) {
                    System.out.println(string);
                }
            }
            br.close();
        } catch(Exception e) {
            e.printStackTrace();
            lines.add(e.getMessage());
            logStepSS(e.getMessage(), "Failed", "Error[file read in]");
        }
        return lines.toArray(new String[lines.size()]);
    }

    public String removeNBSP(String string) {
        string = string.replaceAll("\u00a0", "");
        return string;
    }

    public String replaceNBSP(String string) {
        string = string.replaceAll("\u00a0", " ");
        return string;
    }

    public void popupDialogBox(Boolean accept) {
        if(accept) {
            popupDialogBoxAccept();
        } else {
            popupDialogBoxCancel();
        }
    }

    public void popupDialogBoxAccept() {
        WebDriverWait wait = new WebDriverWait(driver, 5);
        Alert a = wait.until(ExpectedConditions.alertIsPresent());
//        Alert a = driver.switchTo().alert();
        String popupText = a.getText();
        a.accept();
        logStep("Popup Dialog - Accept ["+popupText+"]", "Passed");
    }

    public void popupDialogBoxCancel() {
        Alert a = driver.switchTo().alert();
        String popupText = a.getText();
        a.dismiss();
        logStep("Popup Dialog - Cancel ["+popupText+"]", "Passed");
    }

    public void fileDownloadVerification(String filename, String filetype) {
        try {
            Path path = Paths.get(separatorCompatibility(System.getProperty("user.dir")+"/DL/"+filename));
            long fileSize = Files.size(path);
            long blankFileSize;
            switch(filetype) {
                case "pdf":
                    blankFileSize = 5000;
                    break;
                case "csv":
                default:
                    blankFileSize = 0;
                    break;
            }
            if(fileSize > blankFileSize) {
                logStep("A file size of ["+fileSize+"]bytes for ["+filename+"|"+filetype+"] was found "
                        + "passing the blank file threshold of ["+blankFileSize+"]", "Passed");
            } else {
                logStep("A file size of ["+fileSize+"]bytes for ["+filename+"|"+filetype+"] was found "
                        + "below or equal to the blank file threshold of ["+blankFileSize+"]", "Failed");
            }
        } catch(Exception e) {
            e.printStackTrace();
            logStep("Failed to find valid File ["+filename+"] with FileType ["+filetype+"]", "Failed");
        }
    }

    public String separatorCompatibility(String filepath) {
        return filepath.replace("/", System.getProperty("file.separator"));
    }

    public void refreshPage() {
        driver.navigate().refresh();
        sleep(5000);
        logStep("Refresh Page ["+driver.getCurrentUrl()+"]", "Passed");
    }
}