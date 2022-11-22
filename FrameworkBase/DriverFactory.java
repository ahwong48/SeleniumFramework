package FrameworkBase;
import java.util.HashMap;

import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

public class DriverFactory {

    private final String operatingSystem = System.getProperty("os.name").toUpperCase();
    private final String systemArchitecture = System.getProperty("os.arch").toUpperCase();
    private WebDriver driver;

    public DriverFactory(String browser) {
        System.out.println("Current Operating System: "+operatingSystem);
        System.out.println("Current System Architecture: "+systemArchitecture);
        System.out.println("Current Working Directory: "+System.getProperty("user.dir"));
        String downloadFilePath = separatorCompatibility(System.getProperty("user.dir")+"/DL/");
        switch(browser) {
            case "Chrome":
                if(operatingSystem.contains("WIN")) {
                    System.setProperty("webdriver.chrome.driver", separatorCompatibility("./dependencies/chromedriver/windows64/chromedriver.exe"));
                } else if(operatingSystem.contains("MAC")) {
                    System.setProperty("webdriver.chrome.driver", separatorCompatibility("./dependencies/chromedriver/mac64/chromedriver"));
                } else if(operatingSystem.contains("NIX") || operatingSystem.contains("NUX")) {
                    System.setProperty("webdriver.chrome.driver", separatorCompatibility("./dependencies/chromedriver/linux64/chromedriver"));
                } else {
                    System.out.println("ERROR: OS:"+operatingSystem+" NOT COMPATIBLE WITH CHROME BROWSER");
                }
    //            System.out.println(downloadFilePath);
                ChromeOptions options = new ChromeOptions();
                HashMap<String, Object> prefs = new HashMap<String, Object>();
                prefs.put("download.default_directory", downloadFilePath);
                prefs.put("download.prompt_for_download", false);
                prefs.put("profile.default_content_settings.popups", 0);
                System.out.println(prefs.toString());
                options.setExperimentalOption("prefs", prefs);

                driver = new ChromeDriver(options);
                break;
            case "Edge":
                if(operatingSystem.contains("win")) {
                    System.setProperty("webdriver.edge.driver", separatorCompatibility("./dependencies/MicrosoftWebDriver.exe"));
                } else {
                    System.out.println("ERROR: OS:"+operatingSystem+" NOT COMPATIBLE WITH EDGE BROWSER");
                }
                driver = new EdgeDriver();
                break;
            case "Firefox":
                if(operatingSystem.contains("win")) {
                    System.setProperty("webdriver.gecko.driver", separatorCompatibility("./dependencies/geckodriver.exe"));
                } else {
                    System.out.println("ERROR: OS:"+operatingSystem+" NOT COMPATIBLE WITH FIREFOX BROWSER");
                }
                FirefoxProfile profile=new FirefoxProfile();
                profile.setPreference("browser.helperApps.neverAsk.openFile", "application/octet-stream");
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
        if(null != driver) {
            driver.close();
            driver.quit();
            driver=null;
        }
    }

    public String separatorCompatibility(String filepath) {
        return filepath.replace("/", System.getProperty("file.separator"));
    }
}