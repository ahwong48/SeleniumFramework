import org.openqa.selenium.By;

public class Locators {
	public static final By locator1 = By.xpath("//legend");
	public static final By loginUserName=By.xpath("//input[@ng-model='auth.credential.username']");
	public static final By loginPassword=By.xpath("//input[@ng-model='auth.credential.password']");
	public static final By loginButton=By.xpath("//button[@ng-click='auth.login()']");
	public static final By loginConfirmDyn=By.xpath("//div[@class='@2@' and contains(text(),'@1@')]");
}
