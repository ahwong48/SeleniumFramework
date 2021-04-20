import org.openqa.selenium.By;
import org.testng.annotations.Test;

public class SimpleTest3 extends ReusableMethods{

	@Test(testName="Test3")
	public void seleniumTestSite() {
//		launchBrowser("http://jqueryui.com/resources/demos/droppable/default.html");
//		launchBrowser("https://jqueryui.com/droppable/");
//		try{Thread.sleep(1000);} catch(Exception e) {}
//		switchIFrame(0);
//		dragAndDrop(By.xpath("//div[@id='draggable']"), By.xpath("//div[@id='droppable']"), "drag", "drop");
//		switchIFrameParent();
		
//		launchBrowser("https://www.seleniumeasy.com/test/drag-and-drop-demo.html");
//		try{Thread.sleep(1000);} catch(Exception e) {}
//		dragAndDropCss("span[draggable]:nth-child(2)", "#mydropzone", "drag2", "drop");
//		assertElementText(By.xpath("//div[@id='droppedlist']/span"), "Draggable 1", "Dropped Element");
		
		launchBrowser("https://the-internet.herokuapp.com/drag_and_drop");
		try{Thread.sleep(1000);} catch(Exception e) {}
		dragAndDropCss("#column-a", "#column-b", "a", "b");
		validateRegEx("true", "rx_true_yes", "Element Name");
	}
}
