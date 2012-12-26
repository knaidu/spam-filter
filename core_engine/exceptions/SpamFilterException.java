package filter.bayesian.exceptions;

/**
 * The exception gets thrown when something goes wrong with spam filter.
 * 
 * @author Kunal Mehrotra
 * 
 */
public class SpamFilterException extends Exception {

	public SpamFilterException(String message) {
		super(message);
	}
}
