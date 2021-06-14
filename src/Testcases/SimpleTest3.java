package Testcases;
import FrameworkBase.*;


import org.openqa.selenium.By;
import org.testng.annotations.Test;

import FrameworkBase.ReusableMethods;

public class SimpleTest3 extends ReusableMethods{

    @Test(testName="Test3", groups= {"sanity", "regression"})
    public void seleniumTestSite() {
//        launchBrowser("http://jqueryui.com/resources/demos/droppable/default.html");
//        launchBrowser("https://jqueryui.com/droppable/");
//        try{Thread.sleep(1000);} catch(Exception e) {}
//        switchIFrame(0);
//        dragAndDrop(By.xpath("//div[@id='draggable']"), By.xpath("//div[@id='droppable']"), "drag", "drop");
//        switchIFrameParent();
        
//        launchBrowser("https://www.seleniumeasy.com/test/drag-and-drop-demo.html");
//        try{Thread.sleep(1000);} catch(Exception e) {}
//        dragAndDropCss("span[draggable]:nth-child(2)", "#mydropzone", "drag2", "drop");
//        assertElementText(By.xpath("//div[@id='droppedlist']/span"), "Draggable 1", "Dropped Element");
        
        launchBrowser("https://the-internet.herokuapp.com/drag_and_drop");
//        try{Thread.sleep(1000);} catch(Exception e) {}
        dragAndDropCss("#column-a", "#column-b", "a", "b");
        changeUrl("https://monkeytype.com");
        validateRegEx("true", "rx_true_yes", "Element Name");
        
        changeUrl("https://business.twitter.com/start-advertising");
        hoverElement(By.xpath("//span[text()='Resources and guides']"), "Resources and Guide Menu");
        clickElement(By.xpath("//span[text()='Agency resources']"), "Agency Resources");
        // considering that there is only one tab opened in that point.
        clickElement(By.xpath("//a[@data-text='Contact sales']"), "Contact Sales");
        switchToChildWindow();
        
        System.out.println("Switched to new Window");
//        try {Thread.sleep(5000);} catch(Exception e) {e.printStackTrace(); }
        verifyText(getElementText(By.xpath("//h6")), "Complete the form below if you are a business or agency interested in advertising on Twitter Ads.", "h6 element in new tab");
        
        switchBackToParentWindow();
        System.out.println("Switched to new Window");
//        try {Thread.sleep(5000);} catch(Exception e) {e.printStackTrace(); }
        
        changeUrl("https://fastest.fish/test-files");
        clickElement(By.xpath("//a[contains(text(), '5MB')]"), "Download 5MB File");
        try {Thread.sleep(3000);} catch(Exception e) {e.printStackTrace(); }
        
        changeUrl("https://google.com");
        if(waitForElementVisible(By.xpath("//a[text()='About']"), "About Link")) {
            inputText(By.xpath("//input[@title='Search']"), "google", "Google Search");
            sendEnter(By.xpath("//input[@title='Search']"), "Google Search");    
        }
//        if(!waitForElementVisible(By.xpath("//a[text()='About']"), "About Link")) {
//            System.out.println("About Link is no longer showing");
//        } else {
//            System.out.println("About Link is still visible");
//        }
            
        
        // Do what you want here, you are in the new tab

        
    }
}
