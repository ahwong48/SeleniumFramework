package Testcases;
import Locators.*;
import FrameworkBase.*;
import Locators.*;
import org.testng.annotations.Test;

public class SimpleTest extends ReusableMethods {
    
    
    @Test(testName="Test1Pass", groups= {"sanity"})
    public void FirstTest() {
        
        //comment the above 2 lines and uncomment below 2 lines to use Chrome
        //System.setProperty("webdriver.chrome.driver","G:\\chromedriver.exe");
        //WebDriver driver = new ChromeDriver();
        
        String expectedTitle = "Google";
        String actualTitle = "";
 
        // launch Chrome and direct it to the Base URL
        launchBrowser();
 
        // get the actual value of the title
        actualTitle = driver.getTitle();
        inputText(Locators.googleSearchBar, "pokemon", "Google Search Box");
        logStep("testStep1a", "Failed");
        logStep("testStep1b", "Debug", takeScreenshot("testStep1b-Debug"));
        logStep("testStep1c", "Passed");
        /*
         * compare the actual title of the page with the expected one amd
         * ccnd print
         * the result as "Passed" or "Failed"
         */
//        fa.assertEquals(actualTitle, expectedTitle);
        verifyText(actualTitle, expectedTitle, "Page Title");
        
//        if (actualTitle.contentEquals(expectedTitle)){
//            System.out.println("Test Passed!");
//            ;
//        } else {
//            System.out.println("Test Failed");
//            fa.fail("Test Failed");
//            
//        }
       
        //close Fire fox
       
    }
}
