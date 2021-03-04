import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;

public class ReusableMethods {
	PropertyLoader prop = new PropertyLoader();
	WebDriver driver;
	
	public void launchBrowser(String url) {
		driver.get(url);
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
	
	@AfterMethod
	public void closeBrowser() {
		driver.close();
	}
}
