import org.openqa.selenium.By;

public class Locators {
	public static final By googleSearchBar = By.xpath("//input[@aria-label='Search']");
	public static final By googleSearchButton = By.xpath("//div[not(@jsname)]/center/input[@aria-label='Google Search' and @type='submit']");
	public static final By googleSignInButton = By.xpath("//a[text()='Sign in']");
	public static final By googleEmailInput = By.xpath("//input[@id='identifierId']");
	public static final By googleNextbutton = By.xpath("//span[text()='Next']/following-sibling::div");
}
