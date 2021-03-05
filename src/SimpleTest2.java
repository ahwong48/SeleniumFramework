

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

public class SimpleTest2 extends ReusableMethods {
	
	@Test(testName="Test2Pass")
	public void FirstTest() {
        
        String expectedTitle = "Dashboard"; // Powerful Cross Browser Testing Tool Online | LambdaTest";
        String actualTitle = "";
        
        // launch Chrome and direct it to the Base URL
        launchBrowser();
 
        // get the actual value of the title
        actualTitle = driver.getTitle();
//		wait(5);

//        waitForPageLoaded();
        
        takeScreenshot(driver, actualTitle);
        /*
         * compare the actual title of the page with the expected one and print
         * the result as "Passed" or "Failed"
         */
//        if (actualTitle.contentEquals(expectedTitle)){
//            System.out.println("Test Passed!");
//        } else {
//            System.out.println("Test Failed");
//            assertFail("Failed to get actual title:" +actualTitle+" | " +expectedTitle);
//        }
       
        fa.assertEquals(actualTitle, expectedTitle);
        //close Fire fox
       
    }
}
