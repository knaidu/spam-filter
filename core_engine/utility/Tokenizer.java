package filter.bayesian.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Tokenizer {

	public static final String STANDARD_TOKENIZER = "[\n\t\r\f:;,<>!/&%#_'= \\{\\}\\[\\]\\+\\-\\(\\)\"\\*\\?\\$]+";

	/**
	 * Tokenize the given file.
	 * 
	 * @param file
	 *            The file to be tokenzized
	 * @param delimiter
	 *            The regex on which
	 * @return
	 */
	public String[] tokenize(File file, String delimiter) {
		try {
			FileReader reader = new FileReader(file);
			Scanner scanner = new Scanner(reader);
			scanner.useDelimiter(delimiter);
			
			while (scanner.hasNext()) {
				System.out.println("Token:" + scanner.next());
			}
			
			scanner.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		new Tokenizer()
				.tokenize(
						new File(
								"C:\\Documents and Settings\\Kunal Mehrotra\\My Documents\\Downloads\\trec06p.tgz\\trec06p\\trec06p\\spam\\spam1"),
						"[\n\t\r\f:;,<>!/&%#_'= \\{\\}\\[\\]\\+\\-\\(\\)\"\\*\\?\\$]+");
	}
}
