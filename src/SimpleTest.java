

import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

public class SimpleTest extends ReusableMethods {
	
	
	@Test
	public void FirstTest() {
        // declaration and instantiation of objects/variables
		Browser b = new Browser("Chrome");
		driver = b.getDriver();
		
		//comment the above 2 lines and uncomment below 2 lines to use Chrome
		//System.setProperty("webdriver.chrome.driver","G:\\chromedriver.exe");
		//WebDriver driver = new ChromeDriver();
    	
        String baseUrl = prop.getProperty("url");
        String expectedTitle = "Most Powerful Cross Browser Testing Tool Online | LambdaTest";
        String actualTitle = "";
 
        // launch Chrome and direct it to the Base URL
        launchBrowser(baseUrl);
 
        // get the actual value of the title
        actualTitle = driver.getTitle();
 
        /*
         * compare the actual title of the page with the expected one and print
         * the result as "Passed" or "Failed"
         */
        if (actualTitle.contentEquals(expectedTitle)){
            System.out.println("Test Passed!");
            org.testng.Assert.assertEquals(actualTitle, expectedTitle);
        } else {
            System.out.println("Test Failed");
            org.testng.Assert.fail("Test Failed");
        }
       
        //close Fire fox
       
    }
}
