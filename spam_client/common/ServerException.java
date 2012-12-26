package filter.bayesian.common;

/**
 * The exception thrown when an exception occurs at server.
 * 
 * @author Kunal Mehrotra
 * 
 */
public class ServerException extends Exception {

	public ServerException() {
		super();
	}

	public ServerException(String message) {
		super(message);
	}
}
