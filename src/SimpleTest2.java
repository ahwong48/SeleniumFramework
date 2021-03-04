

import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

public class SimpleTest2 extends ReusableMethods {
	
	@Test
	public void FirstTest() {
        // declaration and instantiation of objects/variables
		Browser b = new Browser("Chrome");
		driver = b.getDriver();
		
		//comment the above 2 lines and uncomment below 2 lines to use Chrome
		//System.setProperty("webdriver.chrome.driver","G:\\chromedriver.exe");
		//WebDriver driver = new ChromeDriver();
    	
        String baseUrl = prop.getProperty("url");
        String expectedTitle = "Most"; // Powerful Cross Browser Testing Tool Online | LambdaTest";
        String actualTitle = "";
 
        // launch Chrome and direct it to the Base URL
        driver.get(baseUrl);
 
        // get the actual value of the title
        actualTitle = driver.getTitle();
 
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
       
        org.testng.Assert.assertEquals(actualTitle, expectedTitle);
        //close Fire fox
       
    }
}
