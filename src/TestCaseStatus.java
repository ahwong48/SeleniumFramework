import java.util.ArrayList;

public class TestCaseStatus {
	private ArrayList<TestStepStatus> tssList;
	private String latestFailedStep = "";
	private int testCaseStatus = 0;
	private String ssPath;
	private String testClass;
	
	public TestCaseStatus() {
		tssList = new ArrayList<TestStepStatus>(); 
		testCaseStatus = 0;
		ssPath = "";
	}
	
	public void addTestStepStatus(TestStepStatus tss) {
		tssList.add(tss);
		if(tss.getStepStatus().equals("Failed")) {
			latestFailedStep = tss.getTestStep();
			testCaseStatus=-1;
		} else if(tss.getStepStatus().equals("Passed") && testCaseStatus >= 0) {
			testCaseStatus=1;
		}
	}
	
	public ArrayList<TestStepStatus> getTestStepList() {
		return tssList;
	}
	
	public boolean passed() {
		if(testCaseStatus == 1) {
			return true;
		} else {
			return false;
		} 
	}
	
	public void setSSPath(String path) {
		ssPath = path;
	}
	
	public String getSSPath() {
		return ssPath;
	}
	
	public void putTestClass(String className) {
		testClass = className;
	}
	
	public String getTestClass() {
		return testClass;
	}
	
	public String getLatestFailedStep() {
		return latestFailedStep;
	}
}
