

import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

public class SimpleTest extends ReusableMethods {
	
	
	@Test(testName="Test1Pass")
	public void FirstTest() {
		
		//comment the above 2 lines and uncomment below 2 lines to use Chrome
		//System.setProperty("webdriver.chrome.driver","G:\\chromedriver.exe");
		//WebDriver driver = new ChromeDriver();
    	
        String expectedTitle = "Most Powerful Cross Browser Testing Tool Online | LambdaTest";
        String actualTitle = "";
 
        // launch Chrome and direct it to the Base URL
        launchBrowser();
 
        // get the actual value of the title
        actualTitle = driver.getTitle();
 
        /*
         * compare the actual title of the page with the expected one and print
         * the result as "Passed" or "Failed"
         */
        fa.assertEquals(actualTitle, expectedTitle);
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
