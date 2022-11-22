package FrameworkBase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.testng.TestNG;
import org.testng.xml.SuiteXmlParser;
import org.testng.xml.XmlSuite;

public class TestNGAutomation {

    public static void main(String[] args) throws IOException, FileNotFoundException {
        // args = { tags, debug, rerun, compare, thread-count, waitTime}

        //Generate new config file using default
        System.out.println(System.getProperty("user.dir"));
        PropertyLoader defConfig = new PropertyLoader(separatorCompatibility("./Templates/defaultConfig.properties"));
        String env = "prod";

        Properties defaultConfig = new Properties();
        InputStream in = new FileInputStream(separatorCompatibility("./Templates/defaultConfig.properties"));

        defaultConfig.load(in);
        for(String setProp : args) {
            String[] set = setProp.split("=");
            defaultConfig.setProperty(set[0], set[1]);
            if(set[0].equals("env")) {
                env = set[1];
            }
        }
        defaultConfig.setProperty("home", defConfig.getProperty(env));
        System.out.println("config file: "+env);
        System.out.println(defConfig.getProperty(env));
        try(final OutputStream outputstream
                = new FileOutputStream(separatorCompatibility("./properties/config.properties"));) {
            defaultConfig.store(outputstream,"File Updated");
            outputstream.close();
        }

        PropertyLoader config = new PropertyLoader(separatorCompatibility("./properties/config.properties"));
        String xmlFileName = separatorCompatibility("./testNg.xml");
        if(config.getProperty("rerun").equals("true")) {
            xmlFileName = separatorCompatibility("./rerun/testNgRerun.xml");
            System.out.println("File Used: "+xmlFileName+" - Rerunning Previously Failed Tests");
        }
        else {
            String tags = config.getProperty("tags");
            generateTestNGxml(tags);
            System.out.println("File Created: ./testNg.xml - with tags ["+tags+"] (if not specified - runs all tcs)");
        }
        extractDriverForTest();
        
        // pull data from SQL -> to update the SFPortalInputData.properties file
        // Also add a flag for running this in the config file -> dataPull=true/false
        
        runTestNg(xmlFileName);
    }

    public static void runTestNg(String file) throws FileNotFoundException {
        SuiteXmlParser parser = new SuiteXmlParser();
        XmlSuite xmlSuite = parser.parse(file, new FileInputStream(file), true);
        TestNG testNg = new TestNG();
        testNg.setXmlSuites(Collections.singletonList(xmlSuite));
        testNg.run();
    }

    public static void generateTestNGxml(String tags) {
        try {
            File testNgTemplateFile = new File(separatorCompatibility("./Templates/testNgXmlTemplate.xml"));
            String xmlString = FileUtils.readFileToString(testNgTemplateFile,"UTF-8");
            String[] xmlSplit = xmlString.split("<!--replace-->");
            File testNgXml = new File("."+System.getProperty("file.separator")+"testNg.xml");
            FileUtils.writeStringToFile(testNgXml, xmlSplit[0], "UTF-8", false);
            BufferedReader reader = new BufferedReader(new FileReader(separatorCompatibility("./src/Testcases/testcases.csv")));
            String line;
            String[] tagList = tags.split(";");

            while((line = reader.readLine()) != null) {
                String[] tcLine = line.split(",");
                for(String tag : tagList) {
                    if(tcLine[1].contains(tag) && !tcLine[2].contains("skip")) {
                        String tcXmlString = "<test name = \""+tcLine[0]+"\">\r\n"
                                + "      <classes>\r\n"
                                + "         <class name = \""+tcLine[0]+"\" />     \r\n"
                                + "      </classes>\r\n"
                                + "   </test>\r\n";
                        FileUtils.writeStringToFile(testNgXml, tcXmlString, "UTF-8", true);
                        break;
                    }
                }
            }
            reader.close();
            FileUtils.writeStringToFile(testNgXml, "<!--replace-->", "UTF-8", true);
            FileUtils.writeStringToFile(testNgXml, xmlSplit[1], "UTF-8", true);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void extractDriverForTest() {
        String operatingSystem = System.getProperty("os.name").toUpperCase();
        if(operatingSystem.contains("WINDOWS")) {
            //Setup chromedriver to be extracted from zip file
            String driverDirectory = separatorCompatibility("./dependencies/chromedriver/windows32");
            try {
                String[] oldFiles = new File(driverDirectory).list();
                for(String oldFile : oldFiles) {
                    File currentFile = new File(oldFile);
                    if(currentFile.getName().contains("chromedriver.exe")) {
                        System.out.println("deleted file: "+currentFile.getName());
                        currentFile.delete();
                    }
                }
            } catch(Exception e) {
                System.out.println("WARNING: No Files to Delete");
            }

            String zipFile = separatorCompatibility("./dependencies/chromedriver/chromedriver_win32.zip");
            UnzipUtility unzip = new UnzipUtility();
            try {
                unzip.unzip(zipFile, driverDirectory);
            } catch(Exception e) {
                e.printStackTrace();
            }
        } else if(operatingSystem.contains("MAC") || operatingSystem.contains("LINUX")){
            System.out.println("No need for Extract");
        } else {
            System.out.println("[ERROR] Operating System: "+operatingSystem+" is invalid.");
        }
    }

    public static String separatorCompatibility(String filepath) {
        return filepath.replace("/", System.getProperty("file.separator"));
    }
}