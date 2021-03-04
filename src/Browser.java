import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Browser {
	private WebDriver driver;
	public Browser(String browser) {
		switch(browser) {
		case "Chrome":
			System.setProperty("webdriver.chrome.driver", "./dependencies/chromedriver.exe");
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
	}
	
	public WebDriver getDriver() {
		return driver;
	}
}
