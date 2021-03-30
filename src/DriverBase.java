import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DriverBase {
	private static List<DriverFactory> driverThreadPool = Collections.synchronizedList(new ArrayList<DriverFactory>());
	private static ThreadLocal<DriverFactory> driverThread;
	private static PropertyLoader config = new PropertyLoader("./config.properties");
	
	@BeforeSuite(alwaysRun = true)
	public static void instantiateDriverObject() {
		driverThread = new ThreadLocal<DriverFactory>() {
			@Override
			protected DriverFactory initialValue() {
				DriverFactory driverThread = new DriverFactory(config.getProperty("browser"));
				driverThreadPool.add(driverThread);
				return driverThread;
			}
		};
	}
	
	public static WebDriver getDriver() {
		return driverThread.get().getDriver();
	}
	
	@AfterTest(alwaysRun = true)
	public static void clearCookies() {
		getDriver().manage().deleteAllCookies();
	}
	
	@AfterSuite(alwaysRun = true) 
	public static void closeDriverObjects() {
		for(DriverFactory driverThread : driverThreadPool) {
			driverThread.quitDriver();
		}
	}
}
