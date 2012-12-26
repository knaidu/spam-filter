package filter.bayesian.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A utility class.
 * 
 * @author Kunal Mehrotra
 * 
 */
public class Utility {

	public static final String DELIMTER = "\u00E7";
	
	// a non printable delimiter
	public static final String NOTICE_DELIMTER = "\u00A5";

	/**
	 * Converts a 4 byte array into an integer.
	 * 
	 * @param b
	 * @return
	 */
	public static int byteArrayToInt(byte[] b) {
		if (b.length == 4)
			return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8
					| (b[3] & 0xff);
		else if (b.length == 2)
			return 0x00 << 24 | 0x00 << 16 | (b[0] & 0xff) << 8 | (b[1] & 0xff);

		return 0;
	}

	/**
	 * Converts an integer into a 4 byte array.
	 * 
	 * @param value
	 * @return
	 */
	public static byte[] intToByteArray(int value) {
		return new byte[] { (byte) (value >> 24), (byte) (value >> 16),
				(byte) (value >> 8), (byte) value };
	}

	/**
	 * Constructs the CCR for posting from the ':' delimited test results.
	 * 
	 * @param results
	 *            ':' delimited test results
	 * @return CCR XML String
	 */
	public static String fetchCCRString(String postResults, String username) {
		String header = "<ContinuityOfCareRecord xmlns='urn:astm-org:CCR'><Body><Results>";
		String tail = " </Results> </Body> </ContinuityOfCareRecord>";
		String resultXML = "";
		String[] results = postResults.split(":");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date now = new Date();

		String date = formatter.format(now);

		for (int count = 0; count < results.length; ++count) {
			String[] result = results[count].split("=");
			String category = result[0];
			String value = result[1];

			resultXML += "<Result><Type /><Description /><Status /><Source><Actor><ActorID>"
					+ username
					+ "</ActorID><ActorRole><Text>Patient</Text></ActorRole> </Actor> </Source> <Substance /> <Test> <DateTime> <Type> <Text>Collection start date</Text> </Type> <ExactDateTime>"
					+ date
					+ "</ExactDateTime> </DateTime> <Type /> <Description> <Text>"
					+ category.toLowerCase()
					+ "</Text> <Code> <Value>.</Value> <CodingSystem>Google</CodingSystem> </Code> </Description> <Status /> <TestResult> <ResultSequencePosition>0</ResultSequencePosition> <VariableResultModifier /> <Value>"
					+ value
					+ "</Value> <Units> <Unit /> </Units> </TestResult> <ConfidenceValue /> </Test> </Result>";

		}

		resultXML = header + resultXML + tail;

		return resultXML;
	}
}
