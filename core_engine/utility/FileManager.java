package filter.bayesian.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * A utility class that manages IO operations.
 * 
 * @author Kunal Mehrotra
 * 
 */
public class FileManager {

	private static FileManager fileManager = new FileManager();

	public static FileManager getInstance() {
		return fileManager;
	}

	/**
	 * Removes alls the files at a specified location containing the given
	 * literal.
	 * 
	 * @param withText
	 * @param location
	 * @throws IOException
	 */
	public void removeFiles(String withText, String location)
			throws IOException {
		File folder = new File(location);
		String[] files = folder.list();
		int numOfFiles = 0;

		for (int index = 0; index < files.length; ++index) {
			String fileName = location + File.separator + files[index];
			File potentialFileToDelete = new File(fileName);
			FileReader file = new FileReader(potentialFileToDelete);
			Scanner scanner = new Scanner(file);
			String test = scanner.findWithinHorizon(Pattern.compile(withText,
					Pattern.CASE_INSENSITIVE), 0);
			scanner.close();

			if (test != null) {
				++numOfFiles;
				potentialFileToDelete.delete();

			}
		}

		System.out.println(numOfFiles + " files deleted!");
	}

	/**
	 * Fetches the file d
	 * 
	 * @param absoluteFileName
	 * @return
	 */
	public String fetchFileData(String absoluteFileName) {

		File file = new File(absoluteFileName);
		try {
			FileInputStream fis = new FileInputStream(file);
			byte[] val = new byte[fis.available()];
			int n = 0;
			int index = 0;

			while ((n = fis.read()) != -1) {
				val[index] = (byte) n;
				++index;
			}

			return new String(val);
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException io) {
			io.printStackTrace();
		}

		return "";
	}

	/**
	 * Fetches data row wise
	 * 
	 * @param absoluteFileName
	 *            The file from which data is to be fetched
	 * 
	 * @return The complete data in list
	 */
	public List fetchFileDataRowWise(String absoluteFileName) {
		List lines = new ArrayList();
		try {

			FileInputStream inputStream = null;

			inputStream = new FileInputStream(absoluteFileName);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));

			String line = null;

			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}

			// Close our input stream
			inputStream.close();
		} // Catches any error conditions
		catch (IOException e) {
			e.printStackTrace();
		}

		return lines;
	}

	public static void main(String args[]) {
		try {
			getInstance()
					.removeFiles(
							"Content-Transfer-Encoding: base64",
							"C:\\Documents and Settings\\Kunal Mehrotra\\My Documents\\Downloads\\trec06p.tgz\\trec06p\\trec06p\\spam");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
