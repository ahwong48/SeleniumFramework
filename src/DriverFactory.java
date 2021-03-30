import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class DriverFactory {
	
	private final String operatingSystem = System.getProperty("os.name").toUpperCase();
	private final String systemArchitecture = System.getProperty("os.arch").toUpperCase();
	private WebDriver driver;
	
	public DriverFactory(String browser) {
		System.out.println("Current Operating System: "+operatingSystem);
		System.out.println("Current System Architecture: "+systemArchitecture);
		switch(browser) {
		case "Chrome":
			System.setProperty("webdriver.chrome.driver", "./dependencies/chromedriver/chromedriver.exe");
			driver = new ChromeDriver();
			break;
		case "Edge":
			System.setProperty("webdriver.edge.driver", "./dependencies/MicrosoftWebDriver.exe");
			driver = new EdgeDriver();
			break;
		case "Firefox":
			System.setProperty("webdriver.gecko.driver", "./dependencies/geckodriver.exe");
			driver = new FirefoxDriver();
			break;
		default:
			System.out.println("ERROR: No Valid Browser Chosen");
			driver = null;
			break;
		}
		System.out.println("Current Browser: "+ browser.toUpperCase());
	}
	
	public WebDriver getDriver() {
		return driver;
	}
	
	public void quitDriver() {
		if (null != driver) {
			driver.quit();
			driver=null;
		}
	}
}
