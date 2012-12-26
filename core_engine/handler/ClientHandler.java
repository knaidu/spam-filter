package filter.bayesian.handler;

import filter.bayesian.common.Message;
import filter.bayesian.common.ServerException;
import filter.bayesian.common.SessionExpiredException;
import filter.bayesian.common.Utility;
import filter.bayesian.core.SpamFilter;
import filter.bayesian.exceptions.SpamFilterException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * This class is responsible for handling client data request and invoking
 * appropriate services on the processed request.
 * 
 * @author Kunal Mehrotra
 * 
 */
public class ClientHandler implements Runnable {

	// the socket bound to a specific client.
	private Socket soc = null;

	// the output stream on the socket
	private DataOutputStream out = null;

	// the input stream on the socket
	private DataInputStream dataInputStream = null;

	// the service provider
	private SpamFilter spamFilter = null;

	// keeps the track of number of featurs that has been displayed.
	private long lastPosition = 0;

	public ClientHandler(Socket soc) {
		this.soc = soc;
	}

	public long getLastPosition() {
		return lastPosition;
	}

	public void setLastPosition(long lastPosition) {
		this.lastPosition = lastPosition;
	}

	/**
	 * Processes incoming request so as to invoke services.
	 */
	public void run() {
		boolean done = false;
		Message message = null;

		try {
			spamFilter = new SpamFilter(this);

			out = new DataOutputStream(soc.getOutputStream());
			dataInputStream = new DataInputStream(soc.getInputStream());

			do {
				// waits for the incoming requests, processes it and returns the
				// parsed message.
				message = waitAndProcessIncomingRequest(soc);

				// handle the request now
				handleRequest(message);
			} while (!done);

		} catch (Exception e) {
			try {
				e.printStackTrace();
				yellToClient(Message.SERVER_FATAL_ERROR);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				soc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * Handles the incoming request
	 * 
	 * @param message
	 *            The message to be processed
	 * @throws IOException
	 *             While outputing to the client.
	 * @throws SessionExpiredException
	 *             Incase the session terminates.
	 */
	private void handleRequest(Message message) throws IOException,
			SessionExpiredException, SpamFilterException {
		// incoming request are handled here
		System.out.println("MESSAGE: " + message.getMessage());
		switch (message.getType()) {

		// handle request for authentication
		case Message.DATA_TO_TRAIN:
			String msg = message.getMessage();
			String[] directories = msg.split(";");
			String spamDirectory = directories[0];
			String hamDirectory = directories[1];
			spamFilter.train(spamDirectory, hamDirectory);
			break;
		case Message.DATA_TO_TEST:
			msg = message.getMessage();
			String[] testDirectories = msg.split(";");
			String testSpamDirectory = testDirectories[0];
			String testHamDirectory = testDirectories[1];
			spamFilter.test(testSpamDirectory, testHamDirectory);
			break;
		case Message.REQUEST_FEATURE_DETAILS:
			System.out.println("Received Feature message");
			spamFilter.displayNextSetOfFeatures(lastPosition, 100);
			break;
		case Message.DATA_DIRECTORIES:
			msg = message.getMessage();
			directories = msg.split(";");
			spamFilter.getStartUpStats(directories[0], directories[1],
					directories[2], directories[3]);
			break;
		case Message.DATA_LOAD_INBOX:
			msg = message.getMessage();
			// directories = msg.split(";");
			spamFilter.loadInbox(msg);

			break;
		case Message.DATA_CLASSIFY_MESSAGES:
			msg = message.getMessage();
			spamFilter.emailClassification(msg);
			break;

		case Message.DATA_REPORT_SPAM:
			msg = message.getMessage();
			String[] files = msg.split(":");
			spamFilter.reportSpam(files[0], files[1], files[2]);
			break;
		}
	}

	/**
	 * Outputs data to the client.
	 * 
	 * @param message[]
	 *            The message to output.
	 * @throws IOException
	 */
	public synchronized void yellToClient(byte[] message) throws IOException {
		out.write(message);
		out.flush();
	}

	/**
	 * Outputs data to the client.
	 * 
	 * @param message
	 *            The message to output.
	 * @throws IOException
	 */
	public synchronized void yellToClient(byte message) throws IOException {
		out.write(message);
		out.flush();
	}

	/**
	 * Outputs data to the client
	 * 
	 * @param message
	 *            A string.
	 * @throws IOException
	 */
	public synchronized void yellToClient(String message) throws IOException {
		byte[] bt = message.getBytes();
		out.write(bt);
		out.flush();
	}

	/**
	 * Waits for the incoming request, processes it and returns a parsed
	 * message.
	 * 
	 * @return Message The parsed message
	 * 
	 * @throws IOException
	 *             Exception while waiting/reading from input stream.
	 */
	private Message waitAndProcessIncomingRequest(Socket soc)
			throws IOException {

		Message message = new Message();

		// read the type of the message
		byte type[] = new byte[1];
		dataInputStream.readFully(type);

		if (Message.isData(type[0])) {
			// read the length of the message.
			byte[] length = new byte[4];
			dataInputStream.readFully(length);
			int len = Utility.byteArrayToInt(length);

			// read the payload
			byte[] payload = new byte[len];
			dataInputStream.readFully(payload);
			String msg = new String(payload);

			// encapsulate the data in Message
			message.setType(type[0]);
			message.setLength(len);
			message.setMessage(msg);
		} else {
			message.setType(type[0]);
		}

		return message;
	}
}
