package filter.bayesian.common;

/**
 * The exception thrown when session expires.
 * 
 * @author Kunal Mehrotra
 * 
 */
public class SessionExpiredException extends Exception {

	public SessionExpiredException() {
		super();
	}

	public SessionExpiredException(String message) {
		super(message);
	}
}
