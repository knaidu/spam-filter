package filter.bayesian.common;

import java.util.Date;

/**
 * The class represents a test result of a patient.
 * 
 * 
 * @author Kunal Mehrotra
 * 
 */
public class TestResult {

	// the name of the test result - calorie, fat...
	private String name;

	// the result of the test - in most the cases its numeral value.
	private String result;

	// the unit
	private String unit;

	// the test date
	private Date testDate;

	// the user that fed in the details
	private String receivedFrom;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getReceivedFrom() {
		return receivedFrom;
	}

	public void setReceivedFrom(String receivedFrom) {
		this.receivedFrom = receivedFrom;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Date getTestDate() {
		return testDate;
	}

	public void setTestDate(Date testDate) {
		this.testDate = testDate;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	
}
