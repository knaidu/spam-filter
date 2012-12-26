package filter.bayesian.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import filter.bayesian.common.Message;
import filter.bayesian.exceptions.SpamFilterException;
import filter.bayesian.handler.ClientHandler;
import filter.bayesian.storage.Feature;
import filter.bayesian.utility.ProbabiltyCalculator;

/**
 * The spam filter based on Bayseian probabilistic classifier.
 * 
 * @author Kunal Mehrotra
 * 
 */
public class SpamFilter {

	private volatile int processedSpamMessages = 0;

	private volatile int processedHamMessages = 0;

	private FeatureManager featureManager = new FeatureManager();

	private ProbabiltyCalculator featureCalculator = new ProbabiltyCalculator();

	// the entire model is in here..
	private Map<String, Feature> featureMap = null;

	private volatile int numberOfSpamsClassifiedAsHams;

	private volatile int numberOfHamsClassifiedAsSpams;

	private ClientHandler clientHandler = null;

	private RandomAccessFile raf = null;

	public SpamFilter(ClientHandler clientHandler) {
		this.clientHandler = clientHandler;
	}

	public SpamFilter() {

	}

	public void getStartUpStats(String spamDirectoryName,
			String hamDirectoryName, String testSpamDirectoryName,
			String testHamDirectoryName) throws SpamFilterException {
		File spamD = new File(spamDirectoryName);
		File hamD = new File(hamDirectoryName);
		File testSpamD = new File(testSpamDirectoryName);
		File testHamD = new File(testHamDirectoryName);

		if (!spamD.isDirectory() || !hamD.isDirectory()
				|| !testSpamD.isDirectory() || !testHamD.isDirectory()) {
			throw new SpamFilterException("Directory not found");
		}

		String stats = spamD.list().length + ";" + hamD.list().length + ";"
				+ testSpamD.list().length + ";" + testHamD.list().length;

		byte[] message = Message.getDataMessage(stats,
				Message.DATA_START_UP_STATS);

		try {
			clientHandler.yellToClient(message);
		} catch (IOException io) {
			throw new SpamFilterException(io.getMessage());
		}

	}

	public void loadInbox(String inboxDirectoryName) throws SpamFilterException {

		File inbox = new File(inboxDirectoryName);

		if (!inbox.isDirectory()) {
			throw new SpamFilterException("Directory not found");
		}

		File inboxFiles[] = inbox.listFiles();
		String emailContent, messageFrom = null, messageSubject = null, messageDate = null;
		byte[] message = Message.getDataMessage(Integer
				.toString(inboxFiles.length), Message.DATA_INBOX);

		try {
			clientHandler.yellToClient(message);
		} catch (IOException io) {
			throw new SpamFilterException(io.getMessage());
		}

		for (int index = 0; index < inboxFiles.length; ++index) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(
						inboxFiles[index].getAbsoluteFile()));
				while ((emailContent = in.readLine()) != null) {
					if (emailContent.contains("Date:")) {
						String date[] = emailContent.split("Date:");
						if (date.length >= 2 && date[1] != null)
							messageDate = date[1];
						continue;
					}
					if (emailContent.contains("From:")) {
						String fromEmailID[] = emailContent.split("From:");
						if (fromEmailID.length >= 2 && fromEmailID[1] != null)
							messageFrom = fromEmailID[1];
						continue;
					}
					if (emailContent.contains("Subject:")) {
						String subject[] = emailContent.split("Subject:");
						if (subject.length >= 2 && subject[1] != null)
							messageSubject = subject[1];
						break;
					}

				}
				message = Message.getDataMessage(inboxFiles[index].getName()
						+ ";" + messageDate + ";" + messageFrom + ";"
						+ messageSubject, Message.DATA_INBOX);
				in.close();

			} catch (FileNotFoundException fnf) {
				throw new SpamFilterException(fnf.getMessage());
			} catch (IOException io) {
				throw new SpamFilterException(io.getMessage());
			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
			}

			try {
				clientHandler.yellToClient(message);
			} catch (IOException io) {
				throw new SpamFilterException(io.getMessage());
			}

		}
	}

	public void emailClassification(String inboxDirectoryName)
			throws SpamFilterException {
		File inbox = new File(inboxDirectoryName);
		if (!inbox.isDirectory()) {
			throw new SpamFilterException("Directory not found");
		}

		File[] fileList = inbox.listFiles();
		int totalEmails = fileList.length;
		byte[] message;
		for (int index = 0; index < totalEmails; ++index) {
			String classification = classify(fileList[index]);
			message = Message.getDataMessage(fileList[index].getName() + ";"
					+ classification, Message.DATA_CLASSIFY);

			try {
				clientHandler.yellToClient(message);
			} catch (IOException io) {
				throw new SpamFilterException(io.getMessage());
			}
		}

	}

	public void reportSpam(String spammyFile, String spamDirectoryName,
			String inBox) throws SpamFilterException {
		File srcAbsoluteSpammyFile = new File(inBox + File.separator
				+ spammyFile);
		String newFileName = "spam-MARKED-" + spammyFile;

		File desNewAbsoluteFileName = new File(spamDirectoryName
				+ File.separator + newFileName);

		try {
			copy(srcAbsoluteSpammyFile, desNewAbsoluteFileName);
		} catch (IOException e) {
			throw new SpamFilterException(e.getMessage());
		}
	}

	/**
	 * Copies src file to dst file. If the dst file does not exist, it is
	 * created
	 */
	void copy(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	/**
	 * Trains the filter on the given spam and ham corpora. The method builds up
	 * the classification model on the given dataset.
	 * 
	 * @param spamDirectoryName
	 *            The absolute path to spam corpus.
	 * @param hamDirectoryName
	 *            The absolute path to ham corpus.
	 * @throws SpamFilterException
	 */
	public void train(String spamDirectoryName, String hamDirectoryName)
			throws SpamFilterException {
		File spamDirectory = new File(spamDirectoryName);
		File hamDirectory = new File(hamDirectoryName);
		this.featureMap = new HashMap<String, Feature>();

		this.processedSpamMessages = 0;
		this.processedHamMessages = 0;
		clientHandler.setLastPosition(0);

		if (!spamDirectory.isDirectory()) {
			throw new SpamFilterException(spamDirectoryName
					+ " is not a directory");
		}

		if (!hamDirectory.isDirectory()) {
			throw new SpamFilterException(hamDirectory + " is not a directory");
		}

		// Here is FeatureBuilder thread for SPAM
		Thread featureBuilderForSpam = new Thread(new FeatureBuilder(
				FeatureManager.SPAM, spamDirectory, featureMap, featureManager,
				this));

		// and here comes the FeatureBuilder thread for HAM
		Thread featureBuilderForHam = new Thread(new FeatureBuilder(
				FeatureManager.HAM, hamDirectory, featureMap, featureManager,
				this));

		// start the feature extraction
		featureBuilderForSpam.start();
		featureBuilderForHam.start();

		// wait till all the extraction is done.
		try {
			featureBuilderForSpam.join();
			featureBuilderForHam.join();
		} catch (InterruptedException e) {
			throw new SpamFilterException(
					"Something seriously went wrong with threads");
		}

		// calculate probabilties for each feature now.
		Set keys = featureMap.keySet(); // reminds me of Florida keys - awesome!

		Iterator<String> iter = keys.iterator();

		int totalHams = hamDirectory.list().length;

		int totalSpams = spamDirectory.list().length;

		System.out
				.println("=======================STATS=======================================");
		System.out.println("Total number of SPAMS:" + totalSpams);
		System.out.println("Total number of HAMS:" + totalHams);
		System.out.println("Total number of features::"
				+ featureMap.keySet().size());

		System.out
				.println("===================Features STATS==================================");

		// Here we should write to a file
		try {
			File file = new File("feature.txt");

			if (file.exists()) {
				file.delete();
			}

			raf = new RandomAccessFile(file, "rw");

			while (iter.hasNext()) {
				String token = iter.next();
				Feature feature = featureMap.get(token);

				featureCalculator.calculateNStoreFeatureProbabilities(feature,
						totalSpams, totalHams, 2);

				System.out.println("Feature:" + token + " Feature details:"
						+ feature);

				// writing to a file here...
				String strMessage = token + "%" + feature.getNumberInSpams()
						+ "%" + feature.getNumberInHams() + "%"
						+ feature.getProbabilitySpamGivenFeature() + "\n";
				raf.writeChars(strMessage);
			}

			// sending net feature set size before details.
			byte[] msg = Message.getDataMessage(keys.size() + "",
					Message.DATA_FEATURE_SIZE);
			clientHandler.yellToClient(msg);

			// Sending first 100 features
			displayNextSetOfFeatures(0, 100);

		} catch (FileNotFoundException e) {
			throw new SpamFilterException(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void displayNextSetOfFeatures(long lastPosition, int setSize)
			throws SpamFilterException {

		try {
			int linesRead = 0;
			raf = new RandomAccessFile("feature.txt", "rw");
			raf.seek(lastPosition);
			char c;
			StringBuffer k = new StringBuffer();

			while (linesRead < setSize) {
				while ((c = raf.readChar()) != '\n') {
					k.append(c);
					lastPosition += 2;
				}
				lastPosition += 2;
				k.append('\n');
				++linesRead;
			}

			clientHandler.setLastPosition(lastPosition);

			byte[] msg = Message.getDataMessage(k.toString(),
					Message.DATA_FEATURE_DETAILS);
			clientHandler.yellToClient(msg);

		} catch (FileNotFoundException e) {
			throw new SpamFilterException(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Tests the filter on the given spam and ham corpora. The method tests the
	 * classification model on the given dataset.
	 * 
	 * @param spamTestDirectoryName
	 *            The absolute path to spam corpus.
	 * @param hamTestDirectoryName
	 *            The absolute path to ham corpus.
	 * @throws SpamFilterException
	 */
	public void test(String spamTestDirectoryName, String hamTestDirectoryName)
			throws SpamFilterException {
		File spamDirectory = new File(spamTestDirectoryName);
		File hamDirectory = new File(hamTestDirectoryName);
		this.numberOfSpamsClassifiedAsHams = 0;
		this.numberOfHamsClassifiedAsSpams = 0;

		if (!spamDirectory.isDirectory()) {
			throw new SpamFilterException(spamTestDirectoryName
					+ " is not a directory");
		}

		if (!hamDirectory.isDirectory()) {
			throw new SpamFilterException(hamTestDirectoryName
					+ " is not a directory");
		}

		Thread spamTester = new Thread(new Tester(this, spamDirectory,
				FeatureManager.SPAM));
		Thread hamTester = new Thread(new Tester(this, hamDirectory,
				FeatureManager.HAM));

		spamTester.start();
		hamTester.start();

		// wait for the testers to evaluate
		try {
			spamTester.join();
			spamTester.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("==================Test Results================");
		System.out.println("Number of SPAMS in Test directory::"
				+ spamDirectory.list().length);
		System.out.println("Number of HAMS in Test directory::"
				+ hamDirectory.list().length);
		System.out
				.println("Number of HAMS misclassified as SPAMS (false positive):: "
						+ numberOfHamsClassifiedAsSpams);
		System.out
				.println("Number of SPAMS misclassified as HAMS (false negative):: "
						+ numberOfSpamsClassifiedAsHams);

	}

	/**
	 * Classfies the given email as spam or not.
	 * 
	 * @param email
	 */
	public String classify(File email) {
		List<String> tokens = featureManager.getFeatures(email,
				FeatureManager.DELIMITERS);
		Iterator<String> iter = tokens.iterator();

		Set<Feature> features = new HashSet<Feature>();

		while (iter.hasNext()) {
			String token = iter.next();
			Feature feature = featureMap.get(token);

			if (feature == null) {
				feature = new Feature(token);
				feature.setProbabilitySpamGivenFeature(0.5);
			}
			features.add(feature);
		}

		// TODO: bad programming - converting from list to set..this needs to be
		// corrected.
		List featuresList = new ArrayList(features);

		features = null;

		Collections.sort(featuresList);

		List top15Features = null;

		if (featuresList.size() > 15) {
			top15Features = featuresList.subList(0, 15);
		} else {
			top15Features = featuresList;
		}

		Iterator it = top15Features.iterator();
		while (it.hasNext()) {
			Feature ft = (Feature) it.next();
			System.out.println("Feature:" + ft.getName() + ", Prob: "
					+ ft.getProbabilitySpamGivenFeature());
		}

		double spamProbability = featureCalculator
				.calcAndGetSpamProbability(top15Features);

		System.out.println("The given emails spam probability = "
				+ spamProbability);

		if (featureCalculator.isAboveThreshHold(spamProbability)) {
			return FeatureManager.SPAM;
		} else {
			return FeatureManager.HAM;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpamFilter sf = new SpamFilter();
		// try {
		// sf
		// .train(
		// "C:\\Documents and Settings\\Kunal Mehrotra\\My
		// Documents\\Downloads\\trec06p.tgz\\trec06p\\trec06p\\train_spam",
		// "C:\\Documents and Settings\\Kunal Mehrotra\\My
		// Documents\\Downloads\\trec06p.tgz\\trec06p\\trec06p\\train_ham");
		//
		// sf
		// .test(
		// "C:\\Documents and Settings\\Kunal Mehrotra\\My
		// Documents\\Downloads\\trec06p.tgz\\trec06p\\trec06p\\test_spam",
		// "C:\\Documents and Settings\\Kunal Mehrotra\\My
		// Documents\\Downloads\\trec06p.tgz\\trec06p\\trec06p\\test_ham");
		//
		// } catch (SpamFilterException ex) {
		// ex.printStackTrace();
		// }

		try {
			sf
					.reportSpam(
							"ham12905",
							"C:\\Documents and Settings\\Kunal Mehrotra\\My Documents\\Downloads\\trec06p.tgz\\trec06p\\trec06p\\test_spam",
							"C:\\Documents and Settings\\Kunal Mehrotra\\My Documents\\Downloads\\trec06p.tgz\\trec06p\\trec06p\\inbox");
			
		} catch (SpamFilterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getProcessedSpamMessages() {
		return processedSpamMessages;
	}

	public void setProcessedSpamMessages(int processedSpamMessages) {
		this.processedSpamMessages = processedSpamMessages;
	}

	public int incrementAndGetProcessedSpamMessages() {
		return ++processedSpamMessages;
	}

	public int getProcessedHamMessages() {
		return processedHamMessages;
	}

	public void setProcessedHamMessages(int processedHamMessages) {
		this.processedHamMessages = processedHamMessages;
	}

	public int incrementAndGetProcessedHamMessages() {
		return ++processedHamMessages;
	}

	public int getNumberOfHamsClassifiedAsSpams() {
		return numberOfHamsClassifiedAsSpams;
	}

	public void setNumberOfHamsClassifiedAsSpams(
			int numberOfHamsClassifiedAsSpams) {
		this.numberOfHamsClassifiedAsSpams = numberOfHamsClassifiedAsSpams;
	}

	public int incrementAndGetNumberOfHamsClassifiedAsSpams() {
		return ++numberOfHamsClassifiedAsSpams;
	}

	public int getNumberOfSpamsClassifiedAsHams() {
		return numberOfSpamsClassifiedAsHams;
	}

	public void setNumberOfSpamsClassifiedAsHams(
			int numberOfSpamsClassifiedAsHams) {
		this.numberOfSpamsClassifiedAsHams = numberOfSpamsClassifiedAsHams;
	}

	public int incrementAndGetNumberOfSpamsClassifiedAsHams() {
		return ++numberOfSpamsClassifiedAsHams;
	}

	public ClientHandler getClientHandler() {
		return clientHandler;
	}

	public void setClientHandler(ClientHandler clientHandler) {
		this.clientHandler = clientHandler;
	}
}

/**
 * The thread builds up the feature set for the files in the given directory.
 * 
 * @author Kunal Mehrotra
 * 
 */
class FeatureBuilder implements Runnable {

	// the type of messages - spam or ham
	private String type;

	// contains files for feature extraction.
	private File directory;

	// the map shared by both
	private Map<String, Feature> featureMap;

	private FeatureManager featureManager = null;

	private SpamFilter filter = null;

	private ClientHandler hanlder = null;

	public FeatureBuilder(String type, File directory,
			Map<String, Feature> featureMap, FeatureManager featureManager,
			SpamFilter filter) {
		this.type = type;
		this.directory = directory;
		this.featureMap = featureMap;
		this.featureManager = featureManager;
		this.filter = filter;
		hanlder = filter.getClientHandler();
	}

	public void run() {

		File[] files = directory.listFiles();
		int count;
		byte[] message = null;

		for (int index = 0; index < files.length; ++index) {

			File email = files[index];
			featureManager.extractFeatureToMapAndUpdateFeatureStats(email,
					type, FeatureManager.DELIMITERS, featureMap);

			if (FeatureManager.SPAM.equals(type)) {
				count = filter.incrementAndGetProcessedSpamMessages();
				// dispatch the count
				message = Message.getDataMessage(count + "",
						Message.DATA_PROCESSED_SPAM_COUNTER);
			} else {
				count = filter.incrementAndGetProcessedHamMessages();
				message = Message.getDataMessage(count + "",
						Message.DATA_PROCESSED_HAM_COUNTER);
			}

			try {
				hanlder.yellToClient(message);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}

/**
 * The prime existence of the class is to load balance the testing among
 * different threads.
 * 
 * @author Kunal Mehrotra
 * 
 */
class Tester implements Runnable {

	// the test directory
	private File testDirectory = null;

	// the filter
	private SpamFilter filter = null;

	// the type of tester - for ham / spam.
	private final String typeOfTester;

	public Tester(SpamFilter filter, File testDirectory, String type) {
		this.testDirectory = testDirectory;
		this.filter = filter;
		this.typeOfTester = type;
	}

	public void run() {
		File[] testFiles = testDirectory.listFiles();
		for (int index = 0; index < testFiles.length; ++index) {
			File email = testFiles[index];
			String emailType = filter.classify(email);

			/*
			 * if you classify an email in spam folder as HAM, then its a case
			 * of false negative
			 */
			if (FeatureManager.SPAM.equalsIgnoreCase(typeOfTester)
					&& FeatureManager.HAM.equalsIgnoreCase(emailType)) {
				filter.incrementAndGetNumberOfSpamsClassifiedAsHams();
			}
			// a case of false positive
			else if (FeatureManager.HAM.equalsIgnoreCase(typeOfTester)
					&& FeatureManager.SPAM.equalsIgnoreCase(emailType)) {
				filter.incrementAndGetNumberOfHamsClassifiedAsSpams();
			}
		}
	}
}