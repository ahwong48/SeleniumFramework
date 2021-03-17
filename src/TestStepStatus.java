
public class TestStepStatus {
	String testStep;
	String status;
	
	public TestStepStatus(String testStep, String status) {
		this.testStep = testStep;
		this.status = status;
	}
	
	public String getTestStep() {
		return testStep;
	}
	
	public String getStepStatus() {
		return status;
	}
	
	public String getHTML() {
		String html = "<p style=\"color:@1@;\"><b>@2@</b></p>";
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
		return html;
	}
}
