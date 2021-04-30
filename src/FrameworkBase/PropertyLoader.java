package FrameworkBase;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class PropertyLoader {
	private Properties prop;
	public PropertyLoader() {
		try {
			File configFile = new File("./config.properties");
			FileReader reader = new FileReader(configFile);
			prop = new Properties();
			prop.load(reader);
			reader.close();
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public PropertyLoader(String filename) {
		try {
			File configFile = new File(filename);
			FileReader reader = new FileReader(configFile);
			prop = new Properties();
			prop.load(reader);
			reader.close();
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public String getProperty(String propName) {
		return prop.getProperty(propName);
	}
	
	public String setProperty(String propName, String value) {
		prop.setProperty(propName, value);
		return prop.getProperty(propName);
	}
}
