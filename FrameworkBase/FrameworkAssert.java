package FrameworkBase;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.Reporter;
import org.testng.asserts.*;

public class FrameworkAssert extends Assertion{
    
    WebDriver driver;
//    String testname;
    public FrameworkAssert(WebDriver wd
//            , String tn
            ) {
        driver = wd; 
//        testname = tn;
    }
    
    @Override
    public void onAssertFailure(IAssert<?> assertCommand, AssertionError ex) {
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String filepath = separatorCompatibility("/SS/"+timestamp()+"-FAIL-"+assertCommand.getMessage()+ ".png");
         try {
             FileUtils.copyFile(scrFile, new File("."+filepath));
             FileUtils.copyFile(scrFile, new File("./test-output"+filepath));
             Reporter.log("<a title= \"Failed\" href=\"."+filepath+"\">"+
                     "<img width=\"418\" height=\"240\" alt=\"assertFail\" title=\"title\" src=\"."+filepath+"\"></a>");
         }
         catch (Exception e) { e.printStackTrace(); }
    }
    
    @Override
    public void onAssertSuccess(IAssert<?> assertCommand) {
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String filepath = "/SS/"+timestamp()+"-PASS-"+assertCommand.getMessage()+ ".png";
         try {
             FileUtils.copyFile(scrFile, new File("."+filepath));
             FileUtils.copyFile(scrFile, new File("./test-output"+filepath));
             Reporter.log("<a title= \"Passed\" href=\"."+filepath+"\">"+
                     "<img width=\"418\" height=\"240\" alt=\"assertPass\" title=\"title\" src=\"."+filepath+"\"></a>");
         }
         catch (Exception e) { e.printStackTrace(); }
    }
    
    public String timestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMDD-HH.mm.ss");    
        Date date = new Date();
        return sdf.format(date);
    }
    
    public String separatorCompatibility(String filepath) {
        return filepath.replace("/", System.getProperty("file.separator"));
    }
}
