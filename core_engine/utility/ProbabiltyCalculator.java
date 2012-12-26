package filter.bayesian.utility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import filter.bayesian.storage.Feature;

/**
 * A probability calculator for features.
 * 
 * @author Kunal Mehrotra
 * 
 */
public class ProbabiltyCalculator {

	private static final double THRESHOLD = 0.9;

	/**
	 * Calculates the bayseian probabilities related to the feature.
	 * 
	 * @param feature
	 * @param totalSpams
	 * @param totalHams
	 * @param tweakFactor
	 *            The "k" factor
	 */
	public void calculateNStoreFeatureProbabilities(Feature feature,
			int totalSpams, int totalHams, int tweakFactor) {

		double featureInHams = feature.getNumberInHams();
		double featureInSpams = feature.getNumberInSpams();

		// calculating the conditional probabilty
		double probOfFeatureGivenHam = featureInHams / totalHams;
		double probOfFeatureGivenSpam = featureInSpams / totalSpams;

		feature.setProbOfFeatureGivenHam(probOfFeatureGivenHam);
		feature.setProbOfFeatureGivenSpam(probOfFeatureGivenSpam);

		// calculate the probability that given message is spam, given the
		// feature. Also the tweak factor is also taken into account.
		double probabilitySpamGivenFeature = probOfFeatureGivenSpam
				/ (probOfFeatureGivenSpam + (tweakFactor * probOfFeatureGivenHam));

		// set the probability as 0.01 and max probability as 0.99
		if (probabilitySpamGivenFeature == 0) {
			probabilitySpamGivenFeature = 0.01;
		}
		if (probabilitySpamGivenFeature == 1) {
			probabilitySpamGivenFeature = 0.99;
		}

		feature.setProbabilitySpamGivenFeature(probabilitySpamGivenFeature);

	}

	/**
	 * Calculates the Bayesian probability of top 15 interesting features.
	 * 
	 * @param features
	 * @return The probability the given set of features constitute a spam
	 *         message.
	 */
	public double calcAndGetSpamProbability(List<Feature> features) {

		Iterator<Feature> iter = features.iterator();
		double probabilityProduct = 1;
		double negProbabilityProduct = 1;
		double spamGivenFeatureProbability = 0;
		double spamProbability = 0;
		while (iter.hasNext()) {
			Feature feature = iter.next();
			spamGivenFeatureProbability = feature
					.getProbabilitySpamGivenFeature();
			probabilityProduct *= spamGivenFeatureProbability;
			negProbabilityProduct *= (1 - spamGivenFeatureProbability);
		}

		if (probabilityProduct == 0) {
			return 0;
		}

		spamProbability = probabilityProduct
				/ (probabilityProduct + negProbabilityProduct);

		return spamProbability;
	}

	/**
	 * Checks if the probability is above(inclusive) the threshold value.
	 * 
	 * @param probability
	 *            The probability
	 * @return true if the value is above threshold.
	 */
	public boolean isAboveThreshHold(double probability) {
		if (probability >= THRESHOLD) {
			return true;
		} else {
			return false;
		}
	}

	// //////////Unit testing the code///////////////////////////
	public static void main(String args[]) {
		// Feature feature = new Feature();
		// feature.setNumberInHams(10);
		// feature.setNumberInSpams(10);
		//
		// new
		// ProbabiltyCalculator().calculateNStoreFeatureProbabilities(feature,
		// 100, 100, 2);
		// System.out.println(feature.getProbOfFeatureGivenHam());
		// System.out.println(feature.getProbOfFeatureGivenSpam());
		// System.out.println(feature.getProbabilitySpamGivenFeature());

		Feature feature1 = new Feature("abc");
		feature1.setProbabilitySpamGivenFeature(0.3);

		Feature feature2 = new Feature("ghi");
		feature2.setProbabilitySpamGivenFeature(0.5);

		List list = new ArrayList();
		list.add(feature1);
		list.add(feature2);
		System.out.println(new ProbabiltyCalculator()
				.calcAndGetSpamProbability(list));
	}
}
