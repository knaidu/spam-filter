package filter.bayesian.storage;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class representing a feature and the number of times it occurs in ham and
 * spam messages.
 * 
 * @author Kunal Mehrotra
 */
public class SVMFeature implements Comparable {

	private String name;

	// the number of features in hams
	private AtomicInteger numberInHams = new AtomicInteger(0);

	// the number of features in spams
	private AtomicInteger numberInSpams = new AtomicInteger(0);

	// ---- Rest of the fields wont be accessed concurrently. --------
	// Incase, you feel, they could be than contact the author of the class

	// represents probability that a message is spam, given this feature
	private double probabilitySpamGivenFeature;

	// percerntage of the given feature in spams
	private double probOfFeatureGivenSpam;

	// percerntage of the given feature in hams
	private double probOfFeatureGivenHam;

	// the interestingnes factor
	private double interest;

	public SVMFeature(String name) {
		this.name = name;
	}

	public double getProbabilitySpamGivenFeature() {
		return probabilitySpamGivenFeature;
	}

	public void setProbabilitySpamGivenFeature(
			double probabilitySpamGivenFeature) {
		this.probabilitySpamGivenFeature = probabilitySpamGivenFeature;
		// sets the intestingness on Pf.
		this.interest = Math.abs(0.5 - probabilitySpamGivenFeature);
	}

	public double getProbOfFeatureGivenHam() {
		return probOfFeatureGivenHam;
	}

	public void setProbOfFeatureGivenHam(double probOfFeatureGivenHam) {
		this.probOfFeatureGivenHam = probOfFeatureGivenHam;
	}

	public double getProbOfFeatureGivenSpam() {
		return probOfFeatureGivenSpam;
	}

	public void setProbOfFeatureGivenSpam(double probOfFeatureGivenSpam) {
		this.probOfFeatureGivenSpam = probOfFeatureGivenSpam;
	}

	public int getNumberInHams() {
		return numberInHams.get();
	}

	public void setNumberInHams(int numberInHams) {
		this.numberInHams.set(numberInHams);
	}

	public int getNumberInSpams() {
		return numberInSpams.get();
	}

	public void setNumberInSpams(int numberInSpams) {
		this.numberInSpams.set(numberInSpams);
	}

	public void incrementInSpamCount() {
		numberInSpams.incrementAndGet();
	}

	public void incrementInHamCount() {
		numberInHams.incrementAndGet();
	}

	public double getInterest() {
		return interest;
	}

	public void setInterest(double interest) {
		this.interest = interest;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Compares features on interestingness factor
	 */
	public int compareTo(Object anotherFeature) throws ClassCastException {
		if (!(anotherFeature instanceof SVMFeature))
			throw new ClassCastException("Another feature expected.");
		double anotherInterest = ((SVMFeature) anotherFeature).getInterest();
		if ((this.interest - anotherInterest) < 0) {
			return -1;
		} else if ((this.interest - anotherInterest) > 0) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public String toString() {

		return "name:" + name + "numberInHams:" + numberInHams.get()
				+ " numberInSpams:" + numberInSpams + " probOfFeatureGivenHam:"
				+ probOfFeatureGivenHam + " probOfFeatureGivenSpam:"
				+ probOfFeatureGivenSpam + " probabilitySpamGivenFeature"
				+ probabilitySpamGivenFeature;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SVMFeature)) {
			return false;
		}

		if (((SVMFeature) obj).name.equals(this.name)) {
			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
