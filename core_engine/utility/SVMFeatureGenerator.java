/**
 * 
 */
package filter.bayesian.utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import java.util.List;
import java.util.Scanner;

import filter.bayesian.core.FeatureManager;
import filter.bayesian.storage.SVMFeature;
/**
 * @author shailendra
 * 
 */
public class SVMFeatureGenerator {

	/**
	 * @param args
	 * 
	 */
	static BufferedWriter br = null;
	static HashMap<String, Integer> uniqueFeatureID = new HashMap<String,Integer>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String directoryName = null;
		
		try {
			if (args.length == 1)
				directoryName = args[0];
			else
				System.out.println("Please provide the input directory Name");

			String delimiters = "[\n\t\r\f\":;,<>!/&%#_'=~ \\{\\}\\[\\]\\+\\-\\(\\)\\*\\?\\$]+";

			br = new BufferedWriter(new FileWriter(
					"C:\\Users\\shailendra\\workspace\\test3.txt"));

			File dir = new File(directoryName);
			File[] directoryContent = dir.listFiles();
			if (!dir.isDirectory()) {
				System.out.println("Directory does not exist");
				return;
			}
			System.out.println(dir);
			System.out.println(directoryContent.length);

			for (int index = 0; index < directoryContent.length; ++index) {
				getFeatures(directoryContent[index], delimiters);
				//System.out.println("Extracting Features of " +directoryContent[index].getName());
			}
			br.close();
		} catch (Exception e) {

		}

		System.out.println("Please Check the outputfile located at ");

	}

	public static List<String> getFeatures(File file, String delimiter) {
		List features = new ArrayList<String>();

		HashMap<String, SVMFeature> svmFeature = new HashMap<String, SVMFeature>();
		try {

			FileReader reader = new FileReader(file);
			Scanner scanner = new Scanner(reader);
			scanner.useDelimiter(delimiter);
			SVMFeature feature = null;
			ArrayList list = new ArrayList();
			
			// scan thru each feature
			while (scanner.hasNext()) {
				String token = scanner.next();
				if (token != null) {
					if(!uniqueFeatureID.containsKey(token)){
						feature = new SVMFeature(token);
						uniqueFeatureID.put(token,uniqueFeatureID.size()+1);
					}
					
					SVMFeature feat = svmFeature.get(token);
					
					if(feat == null){
						feat = new SVMFeature(token);
						feat.setInterest(uniqueFeatureID.get(token));
						svmFeature.put(token, feat);
						list.add(feat);
					}
					
					feat.incrementInSpamCount();
				}
			}
			if (file.getName().contains("ham")) {
				br.write("+1 ");
			} else if (file.getName().contains("spam")) {
				br.write("-1 ");
			}

			Collections.sort(list);
			
			Iterator<SVMFeature> iter = list.iterator();
			
			while(iter.hasNext()){
				SVMFeature svm = iter.next();
				br.write((int)svm.getInterest() + ":"+svm.getNumberInSpams() + " ");
			}
			scanner.close();
			br.newLine();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {

		}

		return features;
	}

}
