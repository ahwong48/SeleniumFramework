
public class TestStepStatus {
	String testStep;
	String status;
	String ssPath = "";
	
	public TestStepStatus(String testStep, String status) {
		this.testStep = testStep;
		this.status = status;
	}
	
	public TestStepStatus(String testStep, String status, String ssPath) {
		this.testStep = testStep;
		this.status = status;
		this.ssPath = ssPath;
	}
	
	public void addScreenShot(String ssPath) {
		this.ssPath = ssPath;
	}
	
	public String getTestStep() {
		return testStep;
	}
	
	public String getStepStatus() {
		return status;
	}
	
	public String getHTML() {
		String html = "";
		if(ssPath.equals("")) {
			html = "<p style=\"color:@1@;\"><b>@2@</b></p>";
		} else {
			html = "<p><a style=\"color:@1@;\" href=\"@3@\"><b>@2@</b></a></p>";
		}
		if(status.equals("Passed")) {
			html = html.replace("@1@", "DarkGreen");
		} else if(status.equals("Failed")) {
			html = html.replace("@1@", "Maroon");
		} else if(status.equals("Debug")) {
			html = html.replace("@1@", "OrangeRed");
		} else {
			html = html.replace("@1@", "Black");
		}
		html = html.replace("@2@", testStep);
		if(!ssPath.equals("")) {
			html = html.replace("@3@", ssPath);
		}
		return html;
	}
}
