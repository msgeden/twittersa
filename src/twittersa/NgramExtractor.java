package twittersa;

/**
 * 
 */

/**
 * @author Munir Geden
 *
 */
import java.util.*;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class NgramExtractor {

	public static HashMap<String, Integer[]> generateNgramsOfTweets(
			ArrayList<Tweet> tweets, boolean smoothing) {
		// This structure will keep the ngrams with class frequencies:
		// HashMap<Ngram, Integer[Class]>
		HashMap<String, Integer[]> ngrams = new HashMap<String, Integer[]>();
		try {
			int ngramSize = Integer.parseInt(FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG, "1"));
			String statsPath = FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG)
					+ "statistics_of_"
					+ ngramSize + "-grams.tsv";
			File statsFile = new File(statsPath);
			FileUtils.deleteQuietly(statsFile);
			
			// Number of b and tweets traversed
			int tokenIterator = 0;
			int tweetIterator = 0;
			Integer[] ngramCountOfClasses = new Integer[] { 0, 0, 0 };
			Integer[] tweetCountOfClasses = new Integer[] { 0, 0, 0 };
			// 1s are added for smoothing
			if (smoothing)
				tweetCountOfClasses = new Integer[] { 1, 1, 1 };

			Tweet.ClassLabel classInfo;
			// Traverse tweets to be analyzed
			for (Tweet tweet : tweets) {
				tweetIterator++;
				classInfo = tweet.getClassInfo();
				tweetCountOfClasses[classInfo.ordinal()]++;
				// Read tokens of tweets
				List<String> tokens = tweet.getTokens();
				for (int i = 0; i < tokens.size() - (ngramSize-1); i++) {
					StringBuilder sb = new StringBuilder();
					for (int j = 0; j < ngramSize; j++) {
						if (j == ngramSize - 1)
							sb.append(tokens.get(i + j).toLowerCase());
						else
							sb.append(tokens.get(i + j).toLowerCase()
									+ Constants.UNDERSCORE);
						tokenIterator++;
					}
					// Generate ngram as key for hashmap
					String ngram = sb.toString();
					if (ngrams.containsKey(ngram)) {
						Integer[] counts = ngrams.get(ngram);
						counts[classInfo.ordinal()]++;
						ngrams.put(ngram, counts);
						ngramCountOfClasses[classInfo.ordinal()]++;
					} else {
						// 1s are added for smoothing
						Integer[] counts = new Integer[] { 0, 0, 0 };
						if (smoothing)
							counts = new Integer[] { 1, 1, 1 };
						counts[classInfo.ordinal()]++;
						ngrams.put(ngram, counts);
						ngramCountOfClasses[classInfo.ordinal()]++;
					}
				}

			}

			ngrams.put(Constants.COUNT_OF_TWEETS_PER_CLASS, tweetCountOfClasses);

			ngrams.put(Constants.COUNT_OF_NGRAMS_PER_CLASS, ngramCountOfClasses);

			// Print the total number of unique ngrams, tweets processed
			FileUtils.write(statsFile, "number of ngrams: " + ngrams.size()
					+ Constants.SEPERATOR_CHAR +"number of tweets: " 
					+ tweetIterator
					+ Constants.SEPERATOR_CHAR + "number of tokens: " + tokenIterator, true);
			System.out.println("number of ngrams: " + ngrams.size()
					+ Constants.SEPERATOR_CHAR +"number of tweets: " 
					+ tweetIterator
					+ Constants.SEPERATOR_CHAR + "number of tokens: " + tokenIterator);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ngrams;
	}

	public static HashMap<String, Double[]> calculateCondProbsOfNgrams(
			HashMap<String, Integer[]> ngrams) {
		int ngramSize = Integer.parseInt(FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG, "1"));
		String ngramsCondProbPath = FileHandler.readConfigValue(Constants.DATA_PATH_CONFIG)
				+ "conditional_probabilities_of_"+ngramSize+"-grams.tsv";
		File ngramsCondProbFile = new File(ngramsCondProbPath);

		// Try to delete if it exists without exception
		FileUtils.deleteQuietly(ngramsCondProbFile);
		// This will keep the conditional probabilities of each ngram for each
		// class: P(g|S_i)
		HashMap<String, Double[]> ngramsWithCondProbs = new HashMap<String, Double[]>();

		try {

			// Retrieve the number of tweets for each class
			Integer[] tweetCountPerClass = ngrams
					.get(Constants.COUNT_OF_TWEETS_PER_CLASS);

			int numberOfClass = tweetCountPerClass.length;

			double[] totalNumberOfTweetsInClass = new double[tweetCountPerClass.length];
			double totalNumberOfTweets = 0.0;
			for (int i = 0; i < tweetCountPerClass.length; i++) {
				totalNumberOfTweetsInClass[i] = (double) tweetCountPerClass[i];
				totalNumberOfTweets += totalNumberOfTweetsInClass[i];
			}
			// Calculate prior probability for each class: P(S_i)
			Double[] priors = new Double[numberOfClass];
			for (int i = 0; i < priors.length; i++)
				priors[i] = totalNumberOfTweetsInClass[i] / totalNumberOfTweets;

			ngramsWithCondProbs.put(Constants.PRIOR_PER_CLASS, priors);

			// Retrieve the number of ngrams for each class
			Integer[] ngramCountPerClass = ngrams
					.get(Constants.COUNT_OF_NGRAMS_PER_CLASS);
			int vocabularySize = ngrams.size();
			// Iterate ngram hashmap to calculate conditional probabilities for
			// each ngram
			for (Map.Entry<String, Integer[]> entry : ngrams.entrySet()) {

				// Calculate the number of tweets for each classes that owns the
				// given ngram
				Double[] condProbs = new Double[numberOfClass];

				Integer[] values = entry.getValue();

				for (int i = 0; i < numberOfClass; i++)
					condProbs[i] = (((double)values[i] + 1.0) / ((double)ngramCountPerClass[i] + (double)vocabularySize));

				ngramsWithCondProbs.put(entry.getKey(), condProbs);

			}

			FileUtils.write(ngramsCondProbFile,  "NGRAM" + Constants.SEPERATOR_CHAR 
					+ "CONDITIONAL PROBABILITES", true);
			System.out.print("COUNT" + Constants.SEPERATOR_CHAR 
					+ "P(NGRAM|CLASS)" + Constants.SEPERATOR_CHAR 
					+ "CONDITIONAL PROBABILITES");

			int count = 0;

			for (Map.Entry<String, Double[]> entry : ngramsWithCondProbs
					.entrySet()) {
				FileUtils.write(
						ngramsCondProbFile,
						"\n" + entry.getKey(), 
						true);
			
				for (int i = 0; i < numberOfClass; i++) {
					FileUtils.write(
							ngramsCondProbFile,
							Constants.SEPERATOR_CHAR + entry.getValue()[i], true);
					System.out
							.print("\n" + (++count) + Constants.SEPERATOR_CHAR
									+ "P(" + entry.getKey() + "|"
									+ Tweet.ClassLabel.values()[i].name()
									+ ")" + Constants.SEPERATOR_CHAR
									+ entry.getValue()[i]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ngramsWithCondProbs;
	}

	public static HashMap<String, Double[]> readCondProbsOfNgramsFromFile(
			String filePath) {
		
		File ngramsCondProbFile = new File(filePath);
		// This will keep the conditional probabilities of each ngram for each
		// class: P(g|S_i)
		HashMap<String, Double[]> ngramsWithCondProbs = new HashMap<String, Double[]>();

		try {
			List<String> lines = FileUtils.readLines(ngramsCondProbFile);
			int lineNumber = 0;
			for (String line : lines) {
				lineNumber++;
				//Skip header line
				if (line != null && !line.equals("") && lineNumber!=1)
				{
					String [] tokens = line.split(Constants.SEPERATOR_CHAR);
					Double [] probs = new Double[tokens.length-1];
					for (int i=0;i<probs.length;i++)
						probs[i] = Double.valueOf(tokens[i+1]);
					ngramsWithCondProbs.put(tokens[0], probs);
				}		
			}
			System.out.println("Class conditional probabilities of ngrams are read from the file");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ngramsWithCondProbs;
	}

	public static HashMap<String, Double> readDistinctiveNgramsFromFile(
			String filePath) {
		
		File ngramsCondProbFile = new File(filePath);
		// This will keep the scores of each ngram
		HashMap<String, Double> distinctiveNgramsWithScores = new HashMap<String, Double>();

		try {
			List<String> lines = FileUtils.readLines(ngramsCondProbFile);
			int lineNumber = 0;
			for (String line : lines) {
				lineNumber++;
				//Skip header line
				if (line != null && !line.equals("") && lineNumber!=1)
				{
					String [] tokens = line.split(Constants.SEPERATOR_CHAR);
					Double score = Double.valueOf(tokens[1]);
					distinctiveNgramsWithScores.put(tokens[0], score);
				}		
			}
			System.out.println("Distintive ngrams are read from the file.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return distinctiveNgramsWithScores;
	}
	
	public static String extractIGOfNgrams(HashMap<String, Integer[]> ngrams) {

		int ngramSize = Integer.parseInt(FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG, "1"));
		int topRankedSize = Integer.parseInt(FileHandler.readConfigValue(Constants.TOP_RANKED_SIZE_CONFIG,"5000"));

		String rankedNgramsPath =  FileHandler.readConfigValue(Constants.DATA_PATH_CONFIG)
				+ "distinctive_"+ngramSize+"-grams_list_by_information_gain.tsv";
		File rankedNgramsFile = new File(rankedNgramsPath);

		// Try to delete if it exists without exception
		FileUtils.deleteQuietly(rankedNgramsFile);

		try {

			// This will keep the information gain extracted from each ngram
			HashMap<String, Double> ngramsWithIG = new HashMap<String, Double>();

			// This structure will keep the top-ranked ngrams with their
			// information gain
			NGramIGPair[] topRankedNgrams = new NGramIGPair[topRankedSize];
			// This structure is defined to validate the distribution of
			// information gain scores
			NGramIGPair[] randomNgrams = new NGramIGPair[topRankedSize];

			// Retrieve the number of tweets per class from the specific entry
			Integer[] tweetCountPerClass = ngrams
					.get(Constants.COUNT_OF_TWEETS_PER_CLASS);
			double[] totalNumberOfTweetsInClass = new double[tweetCountPerClass.length];
			int numberOfClass = tweetCountPerClass.length;
			double totalNumberOfTweets = 0.0;
			for (int i = 0; i < tweetCountPerClass.length; i++) {
				totalNumberOfTweetsInClass[i] = (double) tweetCountPerClass[i];
				totalNumberOfTweets += totalNumberOfTweetsInClass[i];
			}
			// Remove this specific entry not to interpret as ngram
			// ngrams.remove(Constants.COUNT_OF_TWEETS_PER_CLASS);

			// Iterate ngram hashmap to calculate information gain for each
			// ngram
			for (Map.Entry<String, Integer[]> entry : ngrams.entrySet()) {

				// Calculate the number of tweets for each classes that owns the
				// given ngram
				Integer[] values = entry.getValue();
				// Terms that are needed for information gain calculation
				double[] numberOfTweetsHoldNgramInClass = new double[tweetCountPerClass.length];
				double[] numberOfTweetsDoNotHoldNgramInClass = new double[tweetCountPerClass.length];
				for (int i = 0; i < tweetCountPerClass.length; i++) {
					numberOfTweetsHoldNgramInClass[i] = (double) values[i];
					numberOfTweetsDoNotHoldNgramInClass[i] = (double) (totalNumberOfTweetsInClass[i] - values[i]);
				}
				double totalNumberOfTweetsHoldNgram = 0.0;
				double totalNumberOfTweetsDoNotHoldNgram = 0.0;
				for (int i = 0; i < numberOfTweetsHoldNgramInClass.length; i++) {
					totalNumberOfTweetsHoldNgram += numberOfTweetsHoldNgramInClass[i];
					totalNumberOfTweetsDoNotHoldNgram += numberOfTweetsDoNotHoldNgramInClass[i];
				}

				// Calculation formula of information gain: For details see the
				// paper of Kolter et. al and Reddy et. al.
				double[] informationGainForNgram = new double[tweetCountPerClass.length * 2];
				for (int i = 0; i < tweetCountPerClass.length; i++) {
					informationGainForNgram[(i * 2)] = (numberOfTweetsHoldNgramInClass[i] / totalNumberOfTweetsInClass[i])
							* Math.log((numberOfTweetsHoldNgramInClass[i] / totalNumberOfTweetsInClass[i])
									/ (((totalNumberOfTweetsHoldNgram) / totalNumberOfTweets) * (1.0 / (double) numberOfClass)));
					informationGainForNgram[(i * 2) + 1] = (numberOfTweetsDoNotHoldNgramInClass[i] / totalNumberOfTweetsInClass[i])
							* Math.log((numberOfTweetsDoNotHoldNgramInClass[i] / totalNumberOfTweetsInClass[i])
									/ (((totalNumberOfTweetsDoNotHoldNgram) / totalNumberOfTweets) * (1.0 / (double) numberOfClass)));
				}

				// Skip infinite and NaN terms
				double informationGainForNgramTotal = 0.0;
				for (int i = 0; i < informationGainForNgram.length; i++) {
					if (!(Double.isNaN(informationGainForNgram[i]) || !Double
							.isFinite(informationGainForNgram[i]))) {
						informationGainForNgramTotal += informationGainForNgram[i];
					}
				}
				// Add calculated information gain for the given ngram
				ngramsWithIG.put(entry.getKey(), informationGainForNgramTotal);
			}

			// Initialize top-ranked array values for ordering and comparison
			for (int i = 0; i < topRankedNgrams.length; i++) {
				topRankedNgrams[i] = new NGramIGPair(Integer.toString(i), 0.0);
				randomNgrams[i] = new NGramIGPair(Integer.toString(i), 0.0);
			}

			for (Map.Entry<String, Double> entry : ngramsWithIG.entrySet()) {

				// Find the first minimum value and its index in top-ranked
				// array to replace-> index0:index of the item, index 1-> value
				// of item
				double[] minIG = minIndexAndValue(topRankedNgrams);
				if (entry.getValue() > minIG[1]) {
					topRankedNgrams[(int) minIG[0]] = new NGramIGPair(
							entry.getKey(), entry.getValue());
				}

				// If there is positive number add this value to the array with
				// random index to validate distribution
				if (entry.getValue() > 0.0) {
					randomNgrams[randWithinRange(0,
							topRankedSize - 1)] = new NGramIGPair(
							entry.getKey(), entry.getValue());
				}
			}
			// Sort top ranked array and print it to file and console
			Arrays.sort(topRankedNgrams);
			FileUtils.write(rankedNgramsFile, "RANK" + Constants.SEPERATOR_CHAR
					+ "NGRAM" + Constants.SEPERATOR_CHAR + "INFORMATION GAIN",
					true);
			System.out.print("RANK" + Constants.SEPERATOR_CHAR + "NGRAM"
					+ Constants.SEPERATOR_CHAR + "INFORMATION GAIN");
			for (int i = 0; i < topRankedNgrams.length; i++) {
				System.out.print("\n" + (i + 1) + Constants.SEPERATOR_CHAR
						+ topRankedNgrams[i].getKey()
						+ Constants.SEPERATOR_CHAR
						+ topRankedNgrams[i].getValue());
				FileUtils.write(
						rankedNgramsFile,
						"\n" + (i + 1) + Constants.SEPERATOR_CHAR
								+ topRankedNgrams[i].getKey()
								+ Constants.SEPERATOR_CHAR
								+ topRankedNgrams[i].getValue(), true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rankedNgramsFile.getAbsolutePath();
	}

	public static HashMap<String, Double> extractIGOfNgramsByThreshold(
			HashMap<String, Integer[]> ngrams) {
		int ngramSize = Integer.parseInt(FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG, "1"));
		String ngramsByIGPath =  FileHandler.readConfigValue(Constants.DATA_PATH_CONFIG)
				+ "distinctive_"+ngramSize+"-grams_by_information_gain_threshold.tsv";		
		File ngramsByIGFile = new File(ngramsByIGPath);

		// Try to delete if it exists without exception
		FileUtils.deleteQuietly(ngramsByIGFile);
		// This will keep the information gain extracted from each ngram
		HashMap<String, Double> ngramsWithIG = new HashMap<String, Double>();
		HashMap<String, Double> ngramsWithIGTemp = new HashMap<String, Double>();

		try {

			// Retrieve the number of tweets per class from the specific entry
			Integer[] tweetCountPerClass = ngrams
					.get(Constants.COUNT_OF_TWEETS_PER_CLASS);

			// Remove this specific entry not to interpret as ngram
			double threshold = Double.parseDouble(FileHandler.readConfigValue(Constants.IG_THRESHOLD_CONFIG,"0.997"));

			double[] totalNumberOfTweetsInClass = new double[tweetCountPerClass.length];
			int numberOfClass = tweetCountPerClass.length;
			double totalNumberOfTweets = 0.0;
			for (int i = 0; i < tweetCountPerClass.length; i++) {
				totalNumberOfTweetsInClass[i] = (double) tweetCountPerClass[i];
				totalNumberOfTweets += totalNumberOfTweetsInClass[i];
			}
			// Remove this specific entry not to interpret as ngram
			// ngrams.remove(Constants.COUNT_OF_TWEETS_PER_CLASS);

			// Iterate ngram hashmap to calculate information gain for each
			// ngram
			double max = 0.0;
			for (Map.Entry<String, Integer[]> entry : ngrams.entrySet()) {

				// Calculate the number of tweets for each classes that owns the
				// given ngram
				Integer[] values = entry.getValue();
				// Terms that are needed for information gain calculation
				double[] numberOfTweetsHoldNgramInClass = new double[tweetCountPerClass.length];
				double[] numberOfTweetsDoNotHoldNgramInClass = new double[tweetCountPerClass.length];
				for (int i = 0; i < tweetCountPerClass.length; i++) {
					numberOfTweetsHoldNgramInClass[i] = (double) values[i];
					numberOfTweetsDoNotHoldNgramInClass[i] = (double) (totalNumberOfTweetsInClass[i] - values[i]);
				}
				double totalNumberOfTweetsHoldNgram = 0.0;
				double totalNumberOfTweetsDoNotHoldNgram = 0.0;
				for (int i = 0; i < numberOfTweetsHoldNgramInClass.length; i++) {
					totalNumberOfTweetsHoldNgram += numberOfTweetsHoldNgramInClass[i];
					totalNumberOfTweetsDoNotHoldNgram += numberOfTweetsDoNotHoldNgramInClass[i];
				}

				// Calculation formula of information gain: For details see the
				// paper of Kolter et. al
				double[] informationGainForNgram = new double[tweetCountPerClass.length * 2];
				for (int i = 0; i < tweetCountPerClass.length; i++) {
					informationGainForNgram[(i * 2)] = (numberOfTweetsHoldNgramInClass[i] / totalNumberOfTweetsInClass[i])
							* Math.log((numberOfTweetsHoldNgramInClass[i] / totalNumberOfTweetsInClass[i])
									/ (((totalNumberOfTweetsHoldNgram) / totalNumberOfTweets) * (1.0 / (double) numberOfClass)));
					informationGainForNgram[(i * 2) + 1] = (numberOfTweetsDoNotHoldNgramInClass[i] / totalNumberOfTweetsInClass[i])
							* Math.log((numberOfTweetsDoNotHoldNgramInClass[i] / totalNumberOfTweetsInClass[i])
									/ (((totalNumberOfTweetsDoNotHoldNgram) / totalNumberOfTweets) * (1.0 / (double) numberOfClass)));
				}

				// Skip infinite and NaN terms
				double informationGainForNgramTotal = 0.0;
				for (int i = 0; i < informationGainForNgram.length; i++) {
					if (!(Double.isNaN(informationGainForNgram[i]) || !Double
							.isFinite(informationGainForNgram[i]))) {
						informationGainForNgramTotal += informationGainForNgram[i];
					}
				}

				if (informationGainForNgramTotal > max)
					max = informationGainForNgramTotal;
				// Add calculated information gain for the given ngram
				ngramsWithIGTemp.put(entry.getKey(),
						informationGainForNgramTotal);
			}

			// Normalization of values for the range 0-1
			for (Map.Entry<String, Double> entry : ngramsWithIGTemp.entrySet()) {
				double value = entry.getValue() / max;
				if (value > threshold)
					ngramsWithIG.put(entry.getKey(), value);
			}

			FileUtils.write(ngramsByIGFile, 
					"NGRAM" + Constants.SEPERATOR_CHAR + "INFORMATION GAIN",
					true);
			System.out.print("NUMBER" + Constants.SEPERATOR_CHAR + "NGRAM"
					+ Constants.SEPERATOR_CHAR + "INFORMATION GAIN");

			int count = 0;
			for (Map.Entry<String, Double> entry : ngramsWithIG.entrySet()) {
				System.out.print("\n" + (++count) + Constants.SEPERATOR_CHAR
						+ entry.getKey() + Constants.SEPERATOR_CHAR
						+ entry.getValue());
				FileUtils.write(ngramsByIGFile, "\n" + entry.getKey()
						+ Constants.SEPERATOR_CHAR + entry.getValue(), true);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ngramsWithIG;
	}

	public static String extractEntropyOfNgrams(
			HashMap<String, Integer[]> ngrams) {
		int ngramSize = Integer.parseInt(FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG, "1"));
		int topRankedSize = Integer.parseInt(FileHandler.readConfigValue(Constants.TOP_RANKED_SIZE_CONFIG,"5000"));

		String rankedNgramsPath =  FileHandler.readConfigValue(Constants.DATA_PATH_CONFIG)
				+ "distinctive_"+ngramSize+"-grams_list_by_entropy.tsv";
		File rankedNgramsFile = new File(rankedNgramsPath);

		// Try to delete if it exists without exception
		FileUtils.deleteQuietly(rankedNgramsFile);

		try {

			// This will keep the entropy extracted from each ngram
			HashMap<String, Double> ngramsWithEntropy = new HashMap<String, Double>();

			// This structure will keep the top-ranked ngrams with their
			// entropy
			NGramIGPair[] topRankedNgrams = new NGramIGPair[topRankedSize];
			// This structure is defined to validate the distribution of
			// entropy scores
			NGramIGPair[] randomNgrams = new NGramIGPair[topRankedSize];

			// Retrieve the number of tweets per class from the specific entry
			Integer[] tweetCountPerClass = ngrams
					.get(Constants.COUNT_OF_TWEETS_PER_CLASS);
			// Remove this specific entry not to interpret as ngram
			// ngrams.remove(Constants.COUNT_OF_TWEETS_PER_CLASS);
			// Iterate ngram hashmap to calculate information gain for each
			// ngram
			for (Map.Entry<String, Integer[]> entry : ngrams.entrySet()) {

				// Calculate the number of tweets for each classes that owns the
				// given ngram
				Integer[] values = entry.getValue();
				int sum = 0;
				for (int i = 0; i < values.length; i++)
					sum += values[i];
				double entropy = 0.0;
				for (int i = 0; i < tweetCountPerClass.length; i++) {

					double p = (double) values[i] / (double) sum;
					if (p == 0.0 || p == 1.0)
						continue;
					entropy += (p * (Math.log(p) / Math.log(2.0)));
				}
				entropy = -1 * entropy;
				// Add calculated entropy for the given ngram
				if (entropy > 0.0)
					ngramsWithEntropy.put(entry.getKey(), entropy);
				else
					ngramsWithEntropy.put(entry.getKey(), 1.0);

			}

			// Initialize top-ranked array values for ordering and comparison
			for (int i = 0; i < topRankedNgrams.length; i++) {
				topRankedNgrams[i] = new NGramIGPair(Integer.toString(i), 1.0);
				randomNgrams[i] = new NGramIGPair(Integer.toString(i), 1.0);
			}

			for (Map.Entry<String, Double> entry : ngramsWithEntropy.entrySet()) {

				// Find the first minimum value and its index in top-ranked
				// array to replace-> index0:index of the item, index 1-> value
				// of item
				double[] maxIG = maxIndexAndValue(topRankedNgrams);
				if (entry.getValue() < maxIG[1]) {
					topRankedNgrams[(int) maxIG[0]] = new NGramIGPair(
							entry.getKey(), entry.getValue());
				}

				// If there is positive number add this value to the array with
				// random index to validate distribution
				if (entry.getValue() < 1.0) {
					randomNgrams[randWithinRange(0,
							topRankedSize - 1)] = new NGramIGPair(
							entry.getKey(), entry.getValue());
				}
			}
			// Sort top ranked array and print it to file and console
			Arrays.sort(topRankedNgrams, Collections.reverseOrder());
			FileUtils.write(rankedNgramsFile, "RANK" + Constants.SEPERATOR_CHAR
					+ "NGRAM" + Constants.SEPERATOR_CHAR + "ENTROPY", true);
			System.out.print("RANK" + Constants.SEPERATOR_CHAR + "NGRAM"
					+ Constants.SEPERATOR_CHAR + "ENTROPY");
			for (int i = 0; i < topRankedNgrams.length; i++) {
				System.out.print("\n" + (i + 1) + Constants.SEPERATOR_CHAR
						+ topRankedNgrams[i].getKey()
						+ Constants.SEPERATOR_CHAR
						+ topRankedNgrams[i].getValue());
				FileUtils.write(
						rankedNgramsFile,
						"\n" + (i + 1) + Constants.SEPERATOR_CHAR
								+ topRankedNgrams[i].getKey()
								+ Constants.SEPERATOR_CHAR
								+ topRankedNgrams[i].getValue(), true);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return rankedNgramsFile.getAbsolutePath();
	}

	public static HashMap<String, Double> extractEntropyOfNgramsByThreshold(
			HashMap<String, Integer[]> ngrams) {
		int ngramSize = Integer.parseInt(FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG, "1"));
		String ngramsByEntropyPath =  FileHandler.readConfigValue(Constants.DATA_PATH_CONFIG)
				+ "distinctive_"+ngramSize+"-grams_by_entropy_threshold.tsv";
		File ngramsByEntropyFile = new File(ngramsByEntropyPath);

		// Try to delete if it exists without exception
		FileUtils.deleteQuietly(ngramsByEntropyFile);
		// This will keep the entropy extracted from each ngram
		HashMap<String, Double> ngramsWithEntropy = new HashMap<String, Double>();

		try {

			// Retrieve the number of tweets per class from the specific entry
			Integer[] tweetCountPerClass = ngrams
					.get(Constants.COUNT_OF_TWEETS_PER_CLASS);
			// Remove this specific entry not to interpret as ngram
			double threshold = Double.parseDouble(FileHandler.readConfigValue(Constants.ENTROPY_THRESHOLD_CONFIG,"0.8"));
			// Iterate ngram hashmap to calculate entropy gain for each ngram
			for (Map.Entry<String, Integer[]> entry : ngrams.entrySet()) {

				// Calculate the number of tweets for each classes that owns the
				// given ngram
				Integer[] values = entry.getValue();
				int sum = 0;
				for (int i = 0; i < values.length; i++)
					sum += values[i];
				double entropy = 0.0;
				for (int i = 0; i < tweetCountPerClass.length; i++) {
					double p = (double) values[i] / (double) sum;
					if (p == 0.0 || p == 1.0)
						continue;
					entropy += (p * (Math.log(p) / Math.log(2.0)));
				}
				entropy = -1 * entropy;
				// Add calculated entropy for the given ngram
				if (entropy > 0.0 && entropy < threshold)
					ngramsWithEntropy.put(entry.getKey(), entropy);
			}

			FileUtils.write(ngramsByEntropyFile, "NGRAM"
					+ Constants.SEPERATOR_CHAR + "ENTROPY", true);
			System.out.print("NUMBER" + Constants.SEPERATOR_CHAR + "NGRAM"
					+ Constants.SEPERATOR_CHAR + "ENTROPY");

			int count = 0;
			for (Map.Entry<String, Double> entry : ngramsWithEntropy.entrySet()) {
				System.out.print("\n" + (++count) + Constants.SEPERATOR_CHAR
						+ entry.getKey() + Constants.SEPERATOR_CHAR
						+ entry.getValue());
				FileUtils.write(ngramsByEntropyFile, "\n" + entry.getKey()
						+ Constants.SEPERATOR_CHAR + entry.getValue(), true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ngramsWithEntropy;
	}

	public static String extractSalienceOfNgrams(
			HashMap<String, Integer[]> ngrams) {
		int ngramSize = Integer.parseInt(FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG, "1"));
		int topRankedSize = Integer.parseInt(FileHandler.readConfigValue(Constants.TOP_RANKED_SIZE_CONFIG,"5000"));

		String rankedNgramsPath =  FileHandler.readConfigValue(Constants.DATA_PATH_CONFIG)
				+ "distinctive_"+ngramSize+"-grams_list_by_salience.tsv";
		File rankedNgramsFile = new File(rankedNgramsPath);

		// Try to delete if it exists without exception
		FileUtils.deleteQuietly(rankedNgramsFile);

		try {

			// This will keep the information gain extracted from each ngram
			HashMap<String, Double> ngramsWithSalience = new HashMap<String, Double>();

			// This structure will keep the top-ranked ngrams with their
			// salience
			NGramIGPair[] topRankedNgrams = new NGramIGPair[topRankedSize];
			// This structure is defined to validate the distribution of
			// salience scores
			NGramIGPair[] randomNgrams = new NGramIGPair[topRankedSize];

			// Retrieve the number of tweets per class from the specific entry
			Integer[] tweetCountPerClass = ngrams
					.get(Constants.COUNT_OF_TWEETS_PER_CLASS);
			double threshold =  Double.parseDouble(FileHandler.readConfigValue(Constants.SALIENCE_THRESHOLD_CONFIG,"0.8"));
			// Iterate ngram hashmap to calculate salience for each
			// ngram
			for (Map.Entry<String, Integer[]> entry : ngrams.entrySet()) {

				// Calculate the number of tweets for each classes that owns the
				// given ngram
				Integer[] values = entry.getValue();
				int N = values.length;
				double salience = 0.0;
				for (int i = 0; i < N - 1; i++) {
					double p_g_si = (double) values[i]
							/ (double) tweetCountPerClass[i];
					for (int j = i + 1; j < N; j++) {
						double p_g_sj = (double) values[j]
								/ (double) tweetCountPerClass[j];
						double denom = (Double.min(p_g_si, p_g_sj) / Double
								.max(p_g_si, p_g_sj));
						if (Double.isNaN(denom))
							denom = 0.0;
						salience += (1 - denom);
					}
				}
				salience /= N;
				// Add calculated salience for the given ngram
				if (salience > threshold && salience < 1.0)
					ngramsWithSalience.put(entry.getKey(), salience);
			}

			// Initialize top-ranked array values for ordering and comparison
			for (int i = 0; i < topRankedNgrams.length; i++) {
				topRankedNgrams[i] = new NGramIGPair(Integer.toString(i), 0.0);
				randomNgrams[i] = new NGramIGPair(Integer.toString(i), 0.0);
			}

			for (Map.Entry<String, Double> entry : ngramsWithSalience
					.entrySet()) {

				// Find the first minimum value and its index in top-ranked
				// array to replace-> index0:index of the item, index 1-> value
				// of item
				double[] minIG = minIndexAndValue(topRankedNgrams);
				if (entry.getValue() > minIG[1]) {
					topRankedNgrams[(int) minIG[0]] = new NGramIGPair(
							entry.getKey(), entry.getValue());
				}

				// If there is positive number add this value to the array with
				// random index to validate distribution
				if (entry.getValue() < 1.0) {
					randomNgrams[randWithinRange(0,
							topRankedSize - 1)] = new NGramIGPair(
							entry.getKey(), entry.getValue());
				}
			}
			// Sort top ranked array and print it to file and console
			Arrays.sort(topRankedNgrams);
			FileUtils.write(rankedNgramsFile, "RANK" + Constants.SEPERATOR_CHAR
					+ "NGRAM" + Constants.SEPERATOR_CHAR + "SALIENCE", true);
			System.out.print("RANK" + Constants.SEPERATOR_CHAR + "NGRAM"
					+ Constants.SEPERATOR_CHAR + "SALIENCE");
			for (int i = 0; i < topRankedNgrams.length; i++) {
				System.out.print("\n" + (i + 1) + Constants.SEPERATOR_CHAR
						+ topRankedNgrams[i].getKey()
						+ Constants.SEPERATOR_CHAR
						+ topRankedNgrams[i].getValue());
				FileUtils.write(
						rankedNgramsFile,
						"\n" + (i + 1) + Constants.SEPERATOR_CHAR
								+ topRankedNgrams[i].getKey()
								+ Constants.SEPERATOR_CHAR
								+ topRankedNgrams[i].getValue(), true);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return rankedNgramsFile.getAbsolutePath();
	}

	public static HashMap<String, Double> extractSalienceOfNgramsByThreshold(
			HashMap<String, Integer[]> ngrams) {
		int ngramSize = Integer.parseInt(FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG, "1"));
		String ngramsBySaliencePath =  FileHandler.readConfigValue(Constants.DATA_PATH_CONFIG)
				+ "distinctive_"+ngramSize+"-grams_by_salience_threshold.tsv";
		File ngramsBySalienceFile = new File(ngramsBySaliencePath);

		// Try to delete if it exists without exception
		FileUtils.deleteQuietly(ngramsBySalienceFile);
		// This will keep the salience extracted from each ngram
		HashMap<String, Double> ngramsWithSalience = new HashMap<String, Double>();

		try {

			// Retrieve the number of tweets per class from the specific entry
			Integer[] tweetCountPerClass = ngrams
					.get(Constants.COUNT_OF_TWEETS_PER_CLASS);
			// Remove this specific entry not to interpret as ngram
			// ngrams.remove(Constants.COUNT_OF_TWEETS_PER_CLASS);
			double threshold = Double.parseDouble(FileHandler.readConfigValue(Constants.SALIENCE_THRESHOLD_CONFIG,"0.8"));
			// Iterate ngram hashmap to calculate salience for each ngram
			for (Map.Entry<String, Integer[]> entry : ngrams.entrySet()) {

				// Calculate the number of tweets for each classes that owns the
				// given ngram
				Integer[] values = entry.getValue();
				int N = values.length;
				double salience = 0.0;
				for (int i = 0; i < N - 1; i++) {
					double p_g_si = (double) values[i]
							/ (double) tweetCountPerClass[i];
					for (int j = i + 1; j < N; j++) {
						double p_g_sj = (double) values[j]
								/ (double) tweetCountPerClass[j];
						double denom = (Double.min(p_g_si, p_g_sj) / Double
								.max(p_g_si, p_g_sj));
						if (Double.isNaN(denom))
							denom = 0.0;
						salience += (1 - denom);
					}
				}
				salience /= N;
				// Add calculated salience for the given ngram
				if (salience > threshold && salience < 1.0)
					ngramsWithSalience.put(entry.getKey(), salience);
			}

			FileUtils.write(ngramsBySalienceFile, "NGRAM"
					+ Constants.SEPERATOR_CHAR + "SALIENCE", true);
			System.out.print("NUMBER" + Constants.SEPERATOR_CHAR + "NGRAM"
					+ Constants.SEPERATOR_CHAR + "SALIENCE");

			int count = 0;
			for (Map.Entry<String, Double> entry : ngramsWithSalience
					.entrySet()) {
				System.out.print("\n" + (++count) + Constants.SEPERATOR_CHAR
						+ entry.getKey() + Constants.SEPERATOR_CHAR
						+ entry.getValue());
				FileUtils.write(ngramsBySalienceFile, "\n" + entry.getKey()
						+ Constants.SEPERATOR_CHAR + entry.getValue(), true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ngramsWithSalience;
	}

	public static HashMap<String, Double> getTopRankedNgrams(String filePath)
			throws IOException {
		int numberOfDataInput = Integer.parseInt(FileHandler.readConfigValue(Constants.NUMBER_OF_DATA_INPUT_CONFIG,"1000"));
		HashMap<String, Double> topRankedNgrams = new HashMap<String, Double>();
		List<String> fileLines = FileUtils.readLines(new File(filePath));
		for (int i = 1; i <= numberOfDataInput; i++) {
			String[] values = fileLines.get(i).split(Constants.SEPERATOR_CHAR);
			topRankedNgrams.put(values[1], Double.parseDouble(values[2]));
		}
		System.out.println("Distinctive ngrams are read from the file");
		return topRankedNgrams;
	}

	public static File prepareWekaFileHeader(
			HashMap<String, Double> topRankedNgrams, boolean isTestData)
			throws IOException {
		int ngramSize = Integer.parseInt(FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG, "1"));
		int numberOfDataInput = Integer.parseInt(FileHandler.readConfigValue(Constants.NUMBER_OF_DATA_INPUT_CONFIG,"1000"));
		// Data class label for reports and input files
		String trainOrTestLabel = isTestData ? Constants.TEST_LABEL
				: Constants.TRAIN_LABEL;

		// Generate data file and prepare it with its headers
		String wekaDataFilePath = ( FileHandler.readConfigValue(Constants.DATA_PATH_CONFIG)
				+ trainOrTestLabel
				+ Constants.UNDERSCORE
				+ "ngram"
				+ Constants.UNDERSCORE
				+ numberOfDataInput
				+ Constants.UNDERSCORE
				+ ngramSize + ".arff").toLowerCase();

		File wekaDataFile = new File(wekaDataFilePath);
		FileUtils.deleteQuietly(wekaDataFile);

		FileUtils
				.write(wekaDataFile,
						"%%%\n"
								+ "% This "
								+ trainOrTestLabel.toLowerCase()
								+ " data file consists of \n"
								+ "% the most distictive ngrams extracted from tweets \n"
								+ "% of known classes to classify unknown tweets\n"
								+ "% by using Weka classifier algorithms. The study is being conducted\n"
								+ "% by Munir Geden, Olawole Oni, Arwa Alamoudi as part of a coursework in UCL.\n"
								+ "%\n" + "@relation 'ngram'\n", true);

		FileUtils.write(wekaDataFile, "@attribute tweetid string\n", true);
		for (Map.Entry<String, Double> entry : topRankedNgrams.entrySet()) {
			FileUtils.write(wekaDataFile, "@attribute " + entry.getKey()
					+ " numeric\n", true);
		}

		FileUtils.write(wekaDataFile, "@attribute classname {"
				+ Tweet.ClassLabel.Negative.toString() + ","
				+ Tweet.ClassLabel.Neutral.toString() + ","
				+ Tweet.ClassLabel.Positive.toString() + "}\n", true);
		FileUtils.write(wekaDataFile, "@data\n", true);
		return wekaDataFile;
	}

	public static void prepareWekaFileDataFromTweets(
			HashMap<String, Double> topRankedNgrams,
			ArrayList<Tweet> tweets, String dataFilePath,
			boolean isTestData) {

		try {
			int ngramSize = Integer.parseInt(FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG, "1"));
			// Arff file that will keep traning and test data values
			File dataFile = prepareWekaFileHeader(topRankedNgrams,
					isTestData);

			// This will keep the binary values of ngrams
			System.out.println("Generating *.arff file for " + (isTestData?"validation":"training") +".....");
			for (Tweet tweet : tweets) {
				HashMap<String, Integer> calculatedData = new HashMap<String, Integer>();
				for (Map.Entry<String, Double> entry : topRankedNgrams
						.entrySet()) {
					calculatedData.put(entry.getKey(), 0);
				}

				Tweet.ClassLabel classInfo = tweet.getClassInfo();
				String className = classInfo.toString();
				String tweetId = Long.toString(tweet.getId());
				List<String> tokens = tweet.getTokens();
				for (int i = 0; i < tokens.size() - (ngramSize-1); i++) {
					StringBuilder sb = new StringBuilder();
					for (int j = 0; j < ngramSize; j++) {
						if (j == ngramSize - 1)
							sb.append(tokens.get(i + j).toLowerCase());
						else
							sb.append(tokens.get(i + j).toLowerCase()
									+ Constants.UNDERSCORE);
					}
					// Generate ngram as key for hashmap
					String ngram = sb.toString();
					if (topRankedNgrams.containsKey(ngram)) {
						calculatedData.put(ngram, 1);
					}
				}
				FileUtils.write(dataFile, tweetId + ",", true);
				for (Map.Entry<String, Integer> entry : calculatedData
						.entrySet()) {
					FileUtils.write(dataFile, entry.getValue() + ",", true);
				}
				FileUtils.write(dataFile, className + "\n", true);
				
			}
			System.out.println("Generation of *.arff file for " + (isTestData?"validation":"training") +" is completed.");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static double[] minIndexAndValue(NGramIGPair[] arr) {
		double minValue = Double.MAX_VALUE;
		int minIndex = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].getValue() < minValue) {
				minValue = arr[i].getValue();
				minIndex = i;
			}
		}
		return new double[] { (double) minIndex, minValue };
	}

	private static double[] maxIndexAndValue(NGramIGPair[] arr) {
		double maxValue = 0.0;
		int maxIndex = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].getValue() > maxValue) {
				maxValue = arr[i].getValue();
				maxIndex = i;
			}
		}
		return new double[] { (double) maxIndex, maxValue };
	}

	private static int randWithinRange(int min, int max) {
		Random rand = new Random();
		int numValue = rand.nextInt((max - min) + 1) + min;
		return numValue;
	}

}

class NGramIGPair implements Comparable<NGramIGPair> {

	private String key;
	private double value;

	public NGramIGPair(String key, double value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public int compareTo(NGramIGPair item) {
		if (this.value < item.value) {
			return 1;
		} else if (this.value > item.value) {
			return -1;
		} else {
			return 0;
		}
	}
}
