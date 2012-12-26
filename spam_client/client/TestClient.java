package filter.bayesian.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import filter.bayesian.common.Message;
import filter.bayesian.common.Utility;

public class TestClient {

	// the output stream on the socket
	private DataOutputStream out = null;

	// the input stream on the socket
	private DataInputStream dataInputStream = null;

	// the socket bound to a specific client.
	private Socket soc = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new TestClient().run();
		HashMap map = new HashMap();

	}

	private void run() {

		try {
			// soc = new Socket("storm.cise.ufl.edu", 3030);
			soc = new Socket("localhost", 3030);

			out = new DataOutputStream(soc.getOutputStream());
			dataInputStream = new DataInputStream(soc.getInputStream());
			String path = "C:\\Documents and Settings\\Kunal Mehrotra\\My Documents\\Downloads\\trec06p.tgz\\trec06p\\trec06p\\train_spam"
					+ ";C:\\Documents and Settings\\Kunal Mehrotra\\My Documents\\Downloads\\trec06p.tgz\\trec06p\\trec06p\\train_ham";

//			String path2 = path
//					+ ";C:\\Documents and Settings\\Kunal Mehrotra\\My Documents\\Downloads\\trec06p.tgz\\trec06p\\trec06p\\test_spam"
//					+ ";C:\\Documents and Settings\\Kunal Mehrotra\\My Documents\\Downloads\\trec06p.tgz\\trec06p\\trec06p\\test_ham";
//
//			byte[] msg = Message.getDataMessage(path2, Message.DATA_DIRECTORIES);
//			
//			yellToClient(msg);
//			Message incoming = waitAndProcessIncomingRequest(dataInputStream);
//			
//			if(incoming.getType() == Message.DATA_START_UP_STATS){
//				System.out.println(incoming.getMessage());
//			}
			
			// String path = "/adobra/amit2/spam;/adobra/amit2/ham";

			byte[] msg = Message.getDataMessage(path, Message.DATA_TO_TRAIN);
			yellToClient(msg);

			Message incoming;
			
			while (true) {
				incoming = waitAndProcessIncomingRequest(dataInputStream);
				if (incoming.getType() == Message.DATA_PROCESSED_HAM_COUNTER) {
					System.out.println("HAM::::" + incoming.getMessage());
				} else if (incoming.getType() == Message.DATA_PROCESSED_SPAM_COUNTER) {
					System.out.println("SPAM::::" + incoming.getMessage());
				}
//				} else if (incoming.getType() == Message.DATA_FEATURE_DETAILS) {
//
//					System.out.println("Printing ..100 records..");
//
//					String[] featureRecords = incoming.getMessage().split("\n");
//					for (int index = 0; index < featureRecords.length; ++index) {
//						String str = featureRecords[index];
//						String[] featureDetails = str.split("%");
//						String token = featureDetails[0];
//						String spamCount = featureDetails[1];
//						String hamCount = featureDetails[2];
//						String featureProb = featureDetails[3];
//
//						System.out.println(token + ":" + ":" + spamCount + ":"
//								+ hamCount + ":" + featureProb);
//
//					}
//
//				} else if (incoming.getType() == Message.REQUEST_FEATURE_DETAILS) {
//					yellToClient(Message.REQUEST_FEATURE_DETAILS);
//					Thread.currentThread().sleep(6000);
//					yellToClient(Message.REQUEST_FEATURE_DETAILS);
//
//				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

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
	private Message waitAndProcessIncomingRequest(
			DataInputStream dataInputStream) throws IOException {

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

}
