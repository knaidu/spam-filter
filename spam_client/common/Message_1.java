package filter.bayesian.common;

/**
 * The class defines the attributes of the message.
 * 
 * @author Kunal Mehrotra
 * 
 */
public class Message_1 {

	// request message type for client test results
	public final static byte DATA_TO_TRAIN = 0;

	// request message type for client test results
	public final static byte DATA_TO_TEST = 1;

	// request message type for client test results
	public final static byte DATA_TRAINING_RESULTS = 2;

	// request message type for client test results
	public final static byte DATA_PROCESSED_SPAM_COUNTER = 3;

	// request message type for client test results
	public final static byte DATA_PROCESSED_HAM_COUNTER = 4;

	// request message type for client test results
	public final static byte REQUEST_TO_STOP = 5;

	// fatal error at server
	public final static byte SERVER_FATAL_ERROR = 6;

	// request message type for client test results
	public final static byte DATA_FEATURE_DETAILS = 7;

	// request message type for client test results
	public final static byte REQUEST_FEATURE_DETAILS = 8;

	// request message type for client test results
	public final static byte DATA_DIRECTORIES = 9;

	// request message type for client test results
	public final static byte DATA_START_UP_STATS = 10;

	// feature size
	public final static byte DATA_FEATURE_SIZE = 11;

	// The type of the message
	private byte type;

	// the length of the message
	private int length;

	// the payload
	private String message;

	/**
	 * Fetches an array of byte message from a character message and its type.
	 * 
	 * @param msg
	 *            message that needs to be converted into bytes
	 * @param type
	 *            the type of the message.
	 * @return byte[] The message : type + length + data.
	 */
	public static byte[] getDataMessage(String msg, byte type) {
		byte payload[] = msg.getBytes();
		byte[] message = new byte[payload.length + 5];

		// constructing the data message
		message[0] = (byte) type;
		byte[] size = Utility.intToByteArray(payload.length);
		System.arraycopy(size, 0, message, 1, 4);

		System.arraycopy(payload, 0, message, 5, payload.length);

		return message;
	}

	/**
	 * Finds if the given type is the data type for a message.
	 * 
	 * @param type
	 *            The type of the message
	 * @return
	 */
	public static boolean isData(byte type) {
		return (type == DATA_TRAINING_RESULTS || type == DATA_TO_TEST
				|| type == DATA_TO_TRAIN || type == DATA_FEATURE_DETAILS
				|| type == DATA_PROCESSED_HAM_COUNTER
				|| type == DATA_PROCESSED_SPAM_COUNTER
				|| type == DATA_START_UP_STATS || type == DATA_DIRECTORIES || type == DATA_FEATURE_SIZE);
	}

	/**
	 * Finds if the given type is the request type for a message.
	 * 
	 * @param type
	 *            The type of the message
	 * @return
	 */
	public static boolean isRequest(byte type) {
		return (type == REQUEST_TO_STOP || type == REQUEST_FEATURE_DETAILS);
	}

	/**
	 * Gets the length of the message
	 * 
	 * @return
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Sets the length of the message
	 * 
	 * @param length
	 */
	public void setLength(int length) {
		this.length = length;
	}

	/**
	 * Gets the message
	 * 
	 * @return
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message
	 * 
	 * @param message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

}
