

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SimpleTest2 extends ReusableMethods {
	
	@Test(testName="Test2Pass")
	public void InputTestAndAssertions() {
        
        String expectedTitle = "Google"; // Powerful Cross Browser Testing Tool Online | LambdaTest";
        String actualTitle = "";
        
        // launch Chrome and direct it to the Base URL
        launchBrowser();
 
        // get the actual value of the title
        actualTitle = driver.getTitle();
//		wait(5);

//        waitForPageLoaded();
        
        takeScreenshot(actualTitle);
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
//       logStep("testStep2", "Failed");
       verifyText(actualTitle, expectedTitle, "Page Title");
       clickElement(Locators.googleSignInButton, "Sign In Button");
       inputText(Locators.googleEmailInput, securityConfig.getProperty("login"), "Login Text Box");
       clickElement(Locators.googleNextbutton, "Next button");
//       inputText(Locators.loginPassword, securityConfig.getProperty("pw"));
//       clickElement(Locators.loginButton);
       //close Fire fox
//       try{Thread.sleep(5000);}catch(Exception e) {}
//       String[] replace = {config.getProperty("login"),"white-bold user"};
//       String compare = getElementText(dynXpath(Locators.loginConfirmDyn, replace));
//       fa.assertEquals(compare, prop.getProperty("login")+"\s");
//       fa.assertTrue(compare.contains(config.getProperty("login")));
       
    }
}
