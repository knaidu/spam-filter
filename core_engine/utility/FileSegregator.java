package filter.bayesian.utility;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * Responsible for running through the initial step on the corpus to segregate
 * spam from ham. This utility in all likelihood would not be run again.
 * 
 * @author Kunal Mehrotra
 * 
 */
public class FileSegregator {

	public static final String INDEX_FILE_NAME = "index";

	public static final String SPAM = "spam";

	public static final String HAM = "ham";

	private static String baseDirectory = null;

	private static String absoluteIndexDirectoryPath = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		baseDirectory = args[0];
		absoluteIndexDirectoryPath = null;

		absoluteIndexDirectoryPath = baseDirectory + File.separator + "full"
				+ File.separator;

		String absFileName = absoluteIndexDirectoryPath + INDEX_FILE_NAME;

		FileManager manager = FileManager.getInstance();

		List lines = manager.fetchFileDataRowWise(absFileName);

		Iterator<String> iter = lines.iterator();
		int numberOfSpam = 0;
		int numberOfHams = 0;

		while (iter.hasNext()) {
			String lin = iter.next();
			if (lin.indexOf(SPAM) > -1) {
				moveTo(lin, SPAM, ++numberOfSpam);
			} else {
				moveTo(lin, HAM, ++numberOfHams);
			}
		}

		System.out.println("total number of spam is " + numberOfSpam);
		System.out.println("total number of hams is " + numberOfHams);

	}

	/**
	 * Moves a file to the specified folder
	 * 
	 * @param folderType
	 */
	private static void moveTo(String lin, String folderType, int fileNum) {
		String fileName = lin.substring(lin.indexOf(" ")).trim();
		System.out.println("A " + folderType + fileName);
		File file = new File(absoluteIndexDirectoryPath + fileName);
		// System.out.println(file.getAbsolutePath());
		// System.out.println(file.exists());
		String newFileName = baseDirectory + File.separator + folderType
				+ File.separator + folderType + fileNum;
		// System.out.println(newFileName);
		file.renameTo(new File(newFileName));

	}
}
