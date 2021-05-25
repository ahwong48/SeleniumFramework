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
    	Properties defaultConfig = new Properties();
    	InputStream in = new FileInputStream("./Templates/defaultConfig.properties");
    	defaultConfig.load(in);
    	for(String setProp : args) {
    		String[] set = setProp.split("=");
    		defaultConfig.setProperty(set[0], set[1]);
    	}
    	try (final OutputStream outputstream 
                = new FileOutputStream("./properties/config.properties");) {
			defaultConfig.store(outputstream,"File Updated");
			outputstream.close();
		}
    	
    	PropertyLoader config = new PropertyLoader("./properties/config.properties");
    	String xmlFileName = "./testNg.xml";
    	if(config.getProperty("rerun").equals("true")) {
    		xmlFileName = "./rerun/testNgRerun.xml"; 
	    	System.out.println("File Used: ./rerun/testNgRerun.xml - Rerunning Previously Failed Tests");
		}
    	else {
    		String tags = config.getProperty("tags");
	        generateTestNGxml(tags);
	    	System.out.println("File Created: ./testNg.xml - with tags ["+tags+"] (if not specified - runs all tcs)");
    	}
    	runTestNg(xmlFileName);
    }
    
    public static void runTestNg(String file) throws FileNotFoundException{
    	SuiteXmlParser parser = new SuiteXmlParser();
    	XmlSuite xmlSuite = parser.parse(file, new FileInputStream(file), true);
    	TestNG testNg = new TestNG();
    	testNg.setXmlSuites(Collections.singletonList(xmlSuite));
    	testNg.run();
    }
    
    public static void generateTestNGxml(String tags) {
    	
    	try { 
	    	File testNgTemplateFile = new File("./Templates/testNgXmlTemplate.xml");
	        String xmlString = FileUtils.readFileToString(testNgTemplateFile,"UTF-8");
	        String[] xmlSplit = xmlString.split("<!--replace-->");
	    	File testNgXml = new File("./testNg.xml");
	        FileUtils.writeStringToFile(testNgXml, xmlSplit[0], "UTF-8", false);
	    	BufferedReader reader = new BufferedReader(new FileReader("./src/Testcases/testcases.csv"));
	    	String line;
	    	String[] tagList = tags.split(";");
    	
	    	while((line = reader.readLine()) != null) {
	    		String[] tcLine = line.split(",");
	    		for(String tag : tagList) {
	    			if(tcLine[1].contains(tag)) {
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
	    	FileUtils.writeStringToFile(testNgXml, xmlSplit[1], "UTF-8", true);
    	} catch (IOException e) {e.printStackTrace();}
    	
    }

}
