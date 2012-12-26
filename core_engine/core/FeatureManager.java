package filter.bayesian.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import filter.bayesian.storage.Feature;

/**
 * This class is responsible for:
 * <ol>
 * <li> Extracting features </li>
 * <li> Storing features</li>
 * <li> Calculating the count of the extracted features </li>
 * </ol>
 * 
 * @author Kunal Mehrotra
 * 
 */
public class FeatureManager {

	public static final String DELIMITERS = "[\n\t\r\f\":;,<>!/&%#_'=~ \\{\\}\\[\\]\\+\\-\\(\\)\\*\\?\\$]+";

	public static final String SPAM = "spam";

	public static final String HAM = "ham";

	/**
	 * Extracts all the features(tokens) from the email based on the delimiter
	 * provided. Also, the count of the extracted tokens and the tokens are
	 * maintained in the map.
	 * 
	 * @param email
	 * @param type
	 *            The type of an email
	 * @parm delimiter
	 * @param map
	 */
	public void extractFeatureToMapAndUpdateFeatureStats(File email,
			String type, String delimiter, Map<String, Feature> map) {

		try {
			FileReader reader = new FileReader(email);
			Scanner scanner = new Scanner(reader);
			scanner.useDelimiter(delimiter);

			// scan thru each feature
			while (scanner.hasNext()) {

				String token = scanner.next();
				//
				// System.out.println("File: " + email.getAbsolutePath());
				// System.out.println("Token : " + token);

				Feature feature = null;

				// addition to a hash map shud be atomic.
				synchronized (this) {
					feature = map.get(token);

					if (feature == null) {
						feature = new Feature(token);
						Object val = map.put(token, feature);
						// I have put an assert statment here to make sure by
						// no means we are overwritting a value.

						assert (val == null);
					}
				}

				// increment the feature stat accordingly as you scan thru.
				if (SPAM.equalsIgnoreCase(type)) {
					feature.incrementInSpamCount();
				} else if (HAM.equalsIgnoreCase(type)) {
					feature.incrementInHamCount();
				}
			}

			scanner.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Extracts all the features in a file for the given delimiter.
	 * 
	 * @param email
	 *            The email to scan
	 * @param delimiter
	 * @return
	 */
	public List<String> getFeatures(File file, String delimiter) {
		List features = new ArrayList<String>();

		try {
			FileReader reader = new FileReader(file);
			Scanner scanner = new Scanner(reader);
			scanner.useDelimiter(delimiter);

			// scan thru each feature
			while (scanner.hasNext()) {
				String token = scanner.next();
				if(token != null){
					features.add(token);	
				}
			}

			scanner.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return features;
	}

	public static void main(String args[]) {
		HashMap testMap = new HashMap();
		File email = new File(
				"C:\\Documents and Settings\\Kunal Mehrotra\\My Documents\\Downloads\\trec06p.tgz\\trec06p\\trec06p\\spam\\spam1");
		FeatureManager fm = new FeatureManager();
//		fm.extractFeatureToMapAndUpdateFeatureStats(email, SPAM, DELIMITERS,
//				testMap);
//		fm.extractFeatureToMapAndUpdateFeatureStats(email, HAM, DELIMITERS,
//				testMap);
//
//		Iterator<String> iter = testMap.keySet().iterator();
//
//		while (iter.hasNext()) {
//			String token = iter.next();
//			Feature feature = (Feature) testMap.get(token);
//			if (feature.getNumberInHams() != feature.getNumberInSpams()) {
//				System.out.println("Your code breaks here!!!");
//				System.exit(-1);
//			}
//		}
//
//		System.out.println("Works like a charm!");
		
		System.out.println(fm.getFeatures(email, DELIMITERS));
		
	}
}
