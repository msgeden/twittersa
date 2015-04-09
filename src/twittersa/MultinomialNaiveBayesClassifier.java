package twittersa;

/**
 * 
 */

/**
 * @author Munir geden
 *
 */
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.annolab.tt4j.TreeTaggerWrapper;
import org.apache.commons.io.FileUtils;

import twittersa.Tweet.ClassLabel;

public class MultinomialNaiveBayesClassifier {

	public static HashMap<Long, Double[]> calculateClassProbsOfTweets(
			HashMap<String, Double[]> condProbs, ArrayList<Tweet> testTweets,
			HashMap<String, Double> distinctiveNgrams) {

		// This structure will keep class probabilities for each tweet:
		// HashMap<TweetId, Probability[Class]>
		HashMap<Long, Double[]> tweetClassProbs = new HashMap<Long, Double[]>();
		try {
			int ngramSize = Integer.parseInt(FileHandler
					.readConfigValue(Constants.NGRAM_SIZE_CONFIG));
			Double[] priors = condProbs.get(Constants.PRIOR_PER_CLASS);
			int numberOfClass = priors.length;
			String ngramOfInterest = "";
			// Traverse tweets to be calculate class probabilities for each
			// tweet
			for (Tweet tweet : testTweets) {

				Double[] classProbabilities = new Double[numberOfClass];
				for (int i = 0; i < classProbabilities.length; i++)
					classProbabilities[i] = 1.0;

				List<String> tokens = tweet.getTokens();
				for (int i = 0; i < tokens.size() - (ngramSize - 1); i++) {
					StringBuilder sb = new StringBuilder();
					for (int j = 0; j < ngramSize; j++) {
						if (j == ngramSize - 1)
							sb.append(tokens.get(i + j).toLowerCase());
						else
							sb.append(tokens.get(i + j).toLowerCase()
									+ Constants.UNDERSCORE);

					}
					ngramOfInterest = sb.toString();
					Double[] condProbsOfNgram = condProbs.get(ngramOfInterest);
					// if ngram of test tweet is not found in training data
					// assign equal probabilities for each class and classify it
					// according to the class prior
					if (condProbsOfNgram == null) {
						condProbsOfNgram = new Double[numberOfClass];
						for (int j = 0; j < numberOfClass; j++)
							condProbsOfNgram[j] = 1.0;
					}
					for (int j = 0; j < numberOfClass; j++) {
						classProbabilities[j] *= condProbsOfNgram[j];
					}
				}
				for (int j = 0; j < numberOfClass; j++) {
					classProbabilities[j] *= priors[j];
				}
				tweetClassProbs.put(tweet.getId(), classProbabilities);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tweetClassProbs;
	}

	public static HashMap<Long, Double[]> calculateNgramLogProbsOfTweets(
			ArrayList<Tweet> testTweets, HashMap<String, Double[]> condProbs,
			HashMap<String, Double> distinctiveNgrams, int ngramSize) {

		// This structure will keep class probabilities for each tweet:
		// HashMap<TweetId, Probability[Class]>
		HashMap<Long, Double[]> tweetClassNgramProbs = new HashMap<Long, Double[]>();
		try {
			Double[] priors = condProbs.get(Constants.PRIOR_PER_CLASS);
			int numberOfClass = priors.length;
			String ngramOfInterest = "";
			// Traverse tweets to be calculate class probabilities for each
			// tweet
			for (Tweet tweet : testTweets) {

				Double[] classProbabilities = new Double[numberOfClass];
				for (int i = 0; i < classProbabilities.length; i++)
					classProbabilities[i] = Double.MIN_VALUE;

				List<String> tokens = tweet.getTokens();
				for (int i = 0; i < tokens.size() - (ngramSize - 1); i++) {
					StringBuilder sb = new StringBuilder();
					for (int j = 0; j < ngramSize; j++) {
						if (j == ngramSize - 1)
							sb.append(tokens.get(i + j).toLowerCase());
						else
							sb.append(tokens.get(i + j).toLowerCase()
									+ Constants.UNDERSCORE);

					}
					ngramOfInterest = sb.toString();
					if (distinctiveNgrams != null) {
						if (distinctiveNgrams.containsKey(ngramOfInterest)) {
							Double[] condProbsOfNgram = condProbs
									.get(ngramOfInterest);
							if (condProbsOfNgram == null) {
								condProbsOfNgram = new Double[numberOfClass];
								for (int j = 0; j < numberOfClass; j++)
									condProbsOfNgram[j] = Double.MIN_VALUE;
							}
							for (int j = 0; j < numberOfClass; j++) {
								classProbabilities[j] += Math
										.log(condProbsOfNgram[j]);
							}
						}

					} else {
						Double[] condProbsOfNgram = condProbs
								.get(ngramOfInterest);
						if (condProbsOfNgram == null) {
							condProbsOfNgram = new Double[numberOfClass];
							for (int j = 0; j < numberOfClass; j++)
								condProbsOfNgram[j] = Double.MIN_VALUE;
						}
						for (int j = 0; j < numberOfClass; j++) {
							classProbabilities[j] += Math
									.log(condProbsOfNgram[j]);
						}
					}
				}
				for (int j = 0; j < numberOfClass; j++) {
					classProbabilities[j] += Math.log(priors[j]);
				}
				tweetClassNgramProbs.put(tweet.getId(), classProbabilities);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tweetClassNgramProbs;
	}

	public static HashMap<Long, Double[]> calculatePosLogProbsOfTweets(
			ArrayList<Tweet> testTweets, HashMap<String, Double[]> condProbs) {
		TreeTaggerWrapper<String> tt = new TreeTaggerWrapper<>();
		// This structure will keep class probabilities for each tweet:
		// HashMap<TweetId, Probability[Class]>
		HashMap<Long, Double[]> tweetClassPosTagProbs = new HashMap<Long, Double[]>();
		try {
			Double[] priors = condProbs.get(Constants.PRIOR_PER_CLASS);
			int numberOfClass = priors.length;

			// Traverse tweets to be calculate class probabilities for each
			// tweet
			for (Tweet tweet : testTweets) {

				Double[] classProbabilities = new Double[numberOfClass];
				for (int i = 0; i < classProbabilities.length; i++)
					classProbabilities[i] = Double.MIN_VALUE;

				ArrayList<String> posTagsOfTweet = POSExtractor
						.getPosTagsOfTweet(tweet, tt);
				for (String posTag : posTagsOfTweet) {
					Double[] condProbsOfPosTag = condProbs.get(posTag);
					if (condProbsOfPosTag == null) {
						condProbsOfPosTag = new Double[numberOfClass];
						for (int j = 0; j < numberOfClass; j++)
							condProbsOfPosTag[j] = Double.MIN_VALUE;
					}
					for (int j = 0; j < numberOfClass; j++) {
						classProbabilities[j] += Math.log(condProbsOfPosTag[j]);
					}
				}
				for (int j = 0; j < numberOfClass; j++) {
					classProbabilities[j] += Math.log(priors[j]);
				}
				tweetClassPosTagProbs.put(tweet.getId(), classProbabilities);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			tt.destroy();
		}
		return tweetClassPosTagProbs;
	}

	public static void classifyTweetsByNgrams(ArrayList<Tweet> testTweets,
			HashMap<Long, Double[]> ngramClassProbs) {

		try {
			int ngramSize = Integer.parseInt(FileHandler
					.readConfigValue(Constants.NGRAM_SIZE_CONFIG));
			String resultsPath = FileHandler
					.readConfigValue(Constants.REPORTS_PATH_CONFIG)
					+ File.separator
					+ "mnb_results_with"
					+ Constants.UNDERSCORE
					+ Integer.toString(ngramSize)+"-grams" 
					+ ".tsv";

			// This structure will keep class probabilities for each tweet:
			// HashMap<TweetId, Probability[Class]>

			File resultsFile = new File(resultsPath);
			FileUtils.deleteQuietly(resultsFile);

			Tweet.ClassLabel actualClassInfo = null;
			Tweet.ClassLabel predictedClassInfo;
			int correctClassification = 0;
			int incorrectClassification = 0;
			int totalInstances = 0;
			// Traverse tweets to be calculate class probabilities for each
			// tweet
			FileUtils.write(resultsFile, "\nResults\n======\n\nIncorrect Classifications:\n",true);
			System.out.print("\nResults\n======\n\nIncorrect Classifications:\n");
			for (Tweet tweet : testTweets) {

				actualClassInfo = tweet.getClassInfo();
				predictedClassInfo = predictTweetClass(ngramClassProbs
						.get(tweet.getId()));
				// Count correct and incorrect classifications
				if (predictedClassInfo == actualClassInfo)
					correctClassification++;
				else {
					incorrectClassification++;
				}
				FileUtils.write(resultsFile,"\nId: " + tweet.getId()
						+ Constants.SEPERATOR_CHAR + "Actual: "
						+ actualClassInfo.name() + Constants.SEPERATOR_CHAR
						+ "Predicted: " + predictedClassInfo.name()
						+ Constants.SEPERATOR_CHAR + "Text: "
						+ tweet.getOriginalContent(),true);
				System.out.print("\nId: " + tweet.getId()
						+ Constants.SEPERATOR_CHAR + "Actual: "
						+ actualClassInfo.name() + Constants.SEPERATOR_CHAR
						+ "Predicted: " + predictedClassInfo.name()
						+ Constants.SEPERATOR_CHAR + "Text: "
						+ tweet.getOriginalContent());
			}
			totalInstances = correctClassification + incorrectClassification;
			double successRatio = (double) correctClassification
					/ totalInstances;
			FileUtils.write(resultsFile,"\nCorrectly Classified Instances  "
					+ Constants.SEPERATOR_CHAR + correctClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", (successRatio * 100)) + " %",true);
			FileUtils.write(resultsFile,"\nIncorrectly Classified Instances"
					+ Constants.SEPERATOR_CHAR + incorrectClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", ((1 - successRatio) * 100)) + " %",true);
			FileUtils.write(resultsFile,"\nTotal Number of Instances       "
					+ Constants.SEPERATOR_CHAR + totalInstances,true);
			System.out.print("\nCorrectly Classified Instances  "
					+ Constants.SEPERATOR_CHAR + correctClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", (successRatio * 100)) + " %");
			System.out.print("\nIncorrectly Classified Instances"
					+ Constants.SEPERATOR_CHAR + incorrectClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", ((1 - successRatio) * 100)) + " %");
			System.out.print("\nTotal Number of Instances       "
					+ Constants.SEPERATOR_CHAR + totalInstances);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void classifyTweetsByNgrams(ArrayList<Tweet> testTweets,
			HashMap<Long, Double[]> bigramClassProbs, HashMap<Long, Double[]> unigramClassProbs) {

		try {
			int ngramSize = Integer.parseInt(FileHandler
					.readConfigValue(Constants.NGRAM_SIZE_CONFIG));
			String resultsPath = FileHandler
					.readConfigValue(Constants.REPORTS_PATH_CONFIG)
					+ File.separator
					+ "mnb_results_with"
					+ Constants.UNDERSCORE
					+ "both_ngrams"  + ".tsv";

			// This structure will keep class probabilities for each tweet:
			// HashMap<TweetId, Probability[Class]>

			File resultsFile = new File(resultsPath);
			FileUtils.deleteQuietly(resultsFile);

			Tweet.ClassLabel actualClassInfo = null;
			Tweet.ClassLabel predictedClassInfo;
			int correctClassification = 0;
			int incorrectClassification = 0;
			int totalInstances = 0;
			// Traverse tweets to be calculate class probabilities for each
			// tweet
			FileUtils.write(resultsFile, "\nResults\n======\n\nIncorrect Classifications:\n",true);
			System.out.print("\nResults\n======\n\nIncorrect Classifications:\n");
			for (Tweet tweet : testTweets) {

				actualClassInfo = tweet.getClassInfo();
				predictedClassInfo = predictTweetClassByBigramsAndUnigrams(bigramClassProbs.get(tweet.getId()),unigramClassProbs.get(tweet.getId()));
				// Count correct and incorrect classifications
				if (predictedClassInfo == actualClassInfo)
					correctClassification++;
				else {
					incorrectClassification++;
				}
				FileUtils.write(resultsFile,"\nId: " + tweet.getId()
						+ Constants.SEPERATOR_CHAR + "Actual: "
						+ actualClassInfo.name() + Constants.SEPERATOR_CHAR
						+ "Predicted: " + predictedClassInfo.name()
						+ Constants.SEPERATOR_CHAR + "Text: "
						+ tweet.getOriginalContent(),true);
				System.out.print("\nId: " + tweet.getId()
						+ Constants.SEPERATOR_CHAR + "Actual: "
						+ actualClassInfo.name() + Constants.SEPERATOR_CHAR
						+ "Predicted: " + predictedClassInfo.name()
						+ Constants.SEPERATOR_CHAR + "Text: "
						+ tweet.getOriginalContent());
			}
			totalInstances = correctClassification + incorrectClassification;
			double successRatio = (double) correctClassification
					/ totalInstances;
			FileUtils.write(resultsFile,"\nCorrectly Classified Instances  "
					+ Constants.SEPERATOR_CHAR + correctClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", (successRatio * 100)) + " %",true);
			FileUtils.write(resultsFile,"\nIncorrectly Classified Instances"
					+ Constants.SEPERATOR_CHAR + incorrectClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", ((1 - successRatio) * 100)) + " %",true);
			FileUtils.write(resultsFile,"\nTotal Number of Instances       "
					+ Constants.SEPERATOR_CHAR + totalInstances,true);
			System.out.print("\nCorrectly Classified Instances  "
					+ Constants.SEPERATOR_CHAR + correctClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", (successRatio * 100)) + " %");
			System.out.print("\nIncorrectly Classified Instances"
					+ Constants.SEPERATOR_CHAR + incorrectClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", ((1 - successRatio) * 100)) + " %");
			System.out.print("\nTotal Number of Instances       "
					+ Constants.SEPERATOR_CHAR + totalInstances);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void classifyTweetsByPosTags(ArrayList<Tweet> testTweets,
			HashMap<Long, Double[]> posTagClassProbs) {
		try {
			String resultsPath = FileHandler
					.readConfigValue(Constants.REPORTS_PATH_CONFIG)
					+ File.separator + "mnb_results_with_postags.tsv";

			// This structure will keep class probabilities for each tweet:
			// HashMap<TweetId, Probability[Class]>

			File resultsFile = new File(resultsPath);
			FileUtils.deleteQuietly(resultsFile);

			Tweet.ClassLabel actualClassInfo = null;
			Tweet.ClassLabel predictedClassInfo;
			int correctClassification = 0;
			int incorrectClassification = 0;
			int totalInstances = 0;
			// Traverse tweets to be calculate class probabilities for each
			// tweet
			FileUtils.write(resultsFile, "\nResults\n======\n\nIncorrect Classifications:\n",true);
			System.out.print("\nResults\n======\n\nIncorrect Classifications:\n");
			for (Tweet tweet : testTweets) {

				actualClassInfo = tweet.getClassInfo();
				predictedClassInfo = predictTweetClass(posTagClassProbs
						.get(tweet.getId()));
				// Count correct and incorrect classifications
				if (predictedClassInfo == actualClassInfo)
					correctClassification++;
				else {
					incorrectClassification++;
				}
				FileUtils.write(resultsFile,"\nId: " + tweet.getId()
						+ Constants.SEPERATOR_CHAR + "Actual: "
						+ actualClassInfo.name() + Constants.SEPERATOR_CHAR
						+ "Predicted: " + predictedClassInfo.name()
						+ Constants.SEPERATOR_CHAR + "Text: "
						+ tweet.getOriginalContent(),true);
				System.out.print("\nId: " + tweet.getId()
						+ Constants.SEPERATOR_CHAR + "Actual: "
						+ actualClassInfo.name() + Constants.SEPERATOR_CHAR
						+ "Predicted: " + predictedClassInfo.name()
						+ Constants.SEPERATOR_CHAR + "Text: "
						+ tweet.getOriginalContent());
			}
			totalInstances = correctClassification + incorrectClassification;
			double successRatio = (double) correctClassification
					/ totalInstances;
			FileUtils.write(resultsFile,"\nCorrectly Classified Instances  "
					+ Constants.SEPERATOR_CHAR + correctClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", (successRatio * 100)) + " %",true);
			FileUtils.write(resultsFile,"\nIncorrectly Classified Instances"
					+ Constants.SEPERATOR_CHAR + incorrectClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", ((1 - successRatio) * 100)) + " %",true);
			FileUtils.write(resultsFile,"\nTotal Number of Instances       "
					+ Constants.SEPERATOR_CHAR + totalInstances,true);
			System.out.print("\nCorrectly Classified Instances  "
					+ Constants.SEPERATOR_CHAR + correctClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", (successRatio * 100)) + " %");
			System.out.print("\nIncorrectly Classified Instances"
					+ Constants.SEPERATOR_CHAR + incorrectClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", ((1 - successRatio) * 100)) + " %");
			System.out.print("\nTotal Number of Instances       "
					+ Constants.SEPERATOR_CHAR + totalInstances);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static double classifyTweets(ArrayList<Tweet> testTweets,
			HashMap<Long, Double[]> ngramClassProbs,
			HashMap<Long, Double[]> posTagClassProbs, double lambda) {
		double successRatio = 0.0;
		try {
			int ngramSize = Integer.parseInt(FileHandler
					.readConfigValue(Constants.NGRAM_SIZE_CONFIG));
			String resultsPath = FileHandler
					.readConfigValue(Constants.REPORTS_PATH_CONFIG)
					+ File.separator + "mnb_results_with_"+ngramSize+"-grams_and_postags.tsv";

			// This structure will keep class probabilities for each tweet:
			// HashMap<TweetId, Probability[Class]>

			File resultsFile = new File(resultsPath);
			FileUtils.deleteQuietly(resultsFile);

			Tweet.ClassLabel actualClassInfo = null;
			Tweet.ClassLabel predictedClassInfo;
			int correctClassification = 0;
			int incorrectClassification = 0;
			int totalInstances = 0;
			// Traverse tweets to be calculate class probabilities for each
			// tweet
			FileUtils.write(resultsFile, "\nResults\n======\n\nIncorrect Classifications:\n",true);
			System.out.print("\nResults\n======\n\nIncorrect Classifications:\n");
			for (Tweet tweet : testTweets) {

				actualClassInfo = tweet.getClassInfo();
				predictedClassInfo = predictTweetClass(ngramClassProbs.get(tweet.getId()), posTagClassProbs.get(tweet.getId()), lambda);
				//predictedClassInfo = predictTweetClassConditional(ngramClassProbs.get(tweet.getId()), posTagClassProbs.get(tweet.getId()), lambda,actualClassInfo);
				// Count correct and incorrect classifications
				if (predictedClassInfo == actualClassInfo)
					correctClassification++;
				else {
					incorrectClassification++;
					
				}
				FileUtils.write(resultsFile,"\nId: " + tweet.getId()
						+ Constants.SEPERATOR_CHAR + "Actual: "
						+ actualClassInfo.name() + Constants.SEPERATOR_CHAR
						+ "Predicted: " + predictedClassInfo.name()
						+ Constants.SEPERATOR_CHAR + "Text: "
						+ tweet.getOriginalContent(),true);
				System.out.println("Id: " + tweet.getId()
						+ Constants.SEPERATOR_CHAR + "Actual: "
						+ actualClassInfo.name() + Constants.SEPERATOR_CHAR
						+ "Predicted: " + predictedClassInfo.name()
						+ Constants.SEPERATOR_CHAR + "Text: "
						+ tweet.getOriginalContent());
			}
			totalInstances = correctClassification + incorrectClassification;
			successRatio = (double) correctClassification / totalInstances;
			FileUtils.write(resultsFile,"\nCorrectly Classified Instances  "
					+ Constants.SEPERATOR_CHAR + correctClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", (successRatio * 100)) + " %",true);
			FileUtils.write(resultsFile,"\nIncorrectly Classified Instances"
					+ Constants.SEPERATOR_CHAR + incorrectClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", ((1 - successRatio) * 100)) + " %",true);
			FileUtils.write(resultsFile,"\nTotal Number of Instances       "
					+ Constants.SEPERATOR_CHAR + totalInstances,true);
			System.out.print("\nCorrectly Classified Instances  "
					+ Constants.SEPERATOR_CHAR + correctClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", (successRatio * 100)) + " %");
			System.out.print("\nIncorrectly Classified Instances"
					+ Constants.SEPERATOR_CHAR + incorrectClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", ((1 - successRatio) * 100)) + " %");
			System.out.print("\nTotal Number of Instances       "
					+ Constants.SEPERATOR_CHAR + totalInstances);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return successRatio;
	}

	public static double classifyTweetsSeparately(ArrayList<Tweet> testTweets,
			HashMap<Long, Double[]> ngramClassProbs,
			HashMap<String, Double> posProbs, double alpha, double beta) {
		double successRatio = 0.0;
		// double subjectivityThreshold = Double.parseDouble(FileHandler
		// .readConfigValue(Constants.SUBJECTIVITY_THRESHOLD_CONFIG));
		TreeTaggerWrapper<String> tt = new TreeTaggerWrapper<>();
		try {
			int ngramSize = Integer.parseInt(FileHandler
					.readConfigValue(Constants.NGRAM_SIZE_CONFIG));
			String resultsPath = FileHandler
					.readConfigValue(Constants.REPORTS_PATH_CONFIG)
					+ File.separator + "mnb_results_with_"+ngramSize+"-grams_and_postags_seperately.tsv";

			// This structure will keep class probabilities for each tweet:
			// HashMap<TweetId, Probability[Class]>

			File resultsFile = new File(resultsPath);
			FileUtils.deleteQuietly(resultsFile);

			Tweet.ClassLabel actualClassInfo = null;
			Tweet.ClassLabel predictedClassInfo;
			int correctClassification = 0;
			int incorrectClassification = 0;
			int totalInstances = 0;
			// Traverse tweets to be calculate class probabilities for each
			// tweet
			FileUtils.write(resultsFile, "\nResults\n======\n\nIncorrect Classifications:\n",true);
			System.out.print("\nResults\n======\n\nIncorrect Classifications:\n");
			for (Tweet tweet : testTweets) {

				actualClassInfo = tweet.getClassInfo();
				ArrayList<String> posTagsOfTweet = POSExtractor
						.getPosTagsOfTweet(tweet, tt);
				double posTagProbsMean = 0.0;
				for (String posTag : posTagsOfTweet) {
					if (posProbs.containsKey(posTag))
						posTagProbsMean += posProbs.get(posTag);
				}
				posTagProbsMean = (Math.abs(posTagProbsMean) / (double) posTagsOfTweet
						.size());

				predictedClassInfo = predictTweetClass(
						ngramClassProbs.get(tweet.getId()), posTagProbsMean,
						alpha, beta);
				// Count correct and incorrect classifications
				if (predictedClassInfo == actualClassInfo)
					correctClassification++;
				else {
					incorrectClassification++;
				}
				FileUtils.write(resultsFile,"\nId: " + tweet.getId()
						+ Constants.SEPERATOR_CHAR + "Actual: "
						+ actualClassInfo.name() + Constants.SEPERATOR_CHAR
						+ "Predicted: " + predictedClassInfo.name()
						+ Constants.SEPERATOR_CHAR + "Text: "
						+ tweet.getOriginalContent(),true);
				System.out.print("\nId: " + tweet.getId()
						+ Constants.SEPERATOR_CHAR + "Actual: "
						+ actualClassInfo.name() + Constants.SEPERATOR_CHAR
						+ "Predicted: " + predictedClassInfo.name()
						+ Constants.SEPERATOR_CHAR + "Text: "
						+ tweet.getOriginalContent());

			}
			totalInstances = correctClassification + incorrectClassification;
			successRatio = (double) correctClassification / totalInstances;
			FileUtils.write(resultsFile,"\nCorrectly Classified Instances  "
					+ Constants.SEPERATOR_CHAR + correctClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", (successRatio * 100)) + " %",true);
			FileUtils.write(resultsFile,"\nIncorrectly Classified Instances"
					+ Constants.SEPERATOR_CHAR + incorrectClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", ((1 - successRatio) * 100)) + " %",true);
			FileUtils.write(resultsFile,"\nTotal Number of Instances       "
					+ Constants.SEPERATOR_CHAR + totalInstances,true);
			System.out.print("\nCorrectly Classified Instances  "
					+ Constants.SEPERATOR_CHAR + correctClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", (successRatio * 100)) + " %");
			System.out.print("\nIncorrectly Classified Instances"
					+ Constants.SEPERATOR_CHAR + incorrectClassification
					+ Constants.SEPERATOR_CHAR
					+ String.format("%.4f", ((1 - successRatio) * 100)) + " %");
			System.out.print("\nTotal Number of Instances       "
					+ Constants.SEPERATOR_CHAR + totalInstances);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			tt.destroy();
		}
		return successRatio;
	}

	private static Tweet.ClassLabel predictTweetClass(Double[] classProbs) {
		double maxValue = Double.NEGATIVE_INFINITY;
		int maxIndex = 0;
		for (int i = 0; i < classProbs.length; i++) {
			if (classProbs[i] > maxValue) {
				maxValue = classProbs[i];
				maxIndex = i;
			}
		}
		return Tweet.ClassLabel.values()[maxIndex];
	}

	private static Tweet.ClassLabel predictTweetClass(Double[] ngramClassProbs,
			double posTagProbMean, double alpha, double beta) {
		double p = Math
				.abs((ngramClassProbs[ClassLabel.Negative.ordinal()] - ngramClassProbs[ClassLabel.Positive
						.ordinal()])
						/ (ngramClassProbs[ClassLabel.Negative.ordinal()] + ngramClassProbs[ClassLabel.Positive
								.ordinal()]));
		if (posTagProbMean > alpha && p < beta)
			return Tweet.ClassLabel.Neutral;
		double maxValue = Double.NEGATIVE_INFINITY;
		int maxIndex = 0;
		for (int i = 0; i < ngramClassProbs.length; i++) {
			if (ngramClassProbs[i] > maxValue) {
				maxValue = ngramClassProbs[i];
				maxIndex = i;
			}
		}
		return Tweet.ClassLabel.values()[maxIndex];
	}

	private static Tweet.ClassLabel predictTweetClassConditional(Double[] ngramClassProbs,
			Double[] posTagClassProbs, double threshold, ClassLabel actualClass) {
		
		double p = (ngramClassProbs[ClassLabel.Negative.ordinal()] - ngramClassProbs[ClassLabel.Positive
						.ordinal()])
						/ (ngramClassProbs[ClassLabel.Negative.ordinal()] + ngramClassProbs[ClassLabel.Positive
								.ordinal()]);
		System.out.print(String.format("%.05f", Math.abs(p*100)) + " %\t");
		double[] weighted = new double[ngramClassProbs.length];
		if (Math.abs(p)<threshold)
		{
			for (int i = 0; i < weighted.length; i++) {
				weighted[i] = posTagClassProbs[i];
			}
		}
		else
		{
			for (int i = 0; i < weighted.length; i++) {
				weighted[i] = ngramClassProbs[i];
			}
		}
		double maxValue = Double.NEGATIVE_INFINITY;
		int maxIndex = 0;
		for (int i = 0; i < weighted.length; i++) {
			if (weighted[i] > maxValue) {
				maxValue = weighted[i];
				maxIndex = i;
			}
		}
		//System.out.println(p);
		
		return Tweet.ClassLabel.values()[maxIndex];
	}
	
	
	private static Tweet.ClassLabel predictTweetClassByBigramsAndUnigrams(Double[] bigramClassProbs,
			Double[] unigramClassProbs) {
		
			//|p1-p2|/|p1+p2|
			double bigramRatio = (bigramClassProbs[ClassLabel.Negative.ordinal()] - bigramClassProbs[ClassLabel.Positive
			                                                     						.ordinal()])
			                                                     						/ (bigramClassProbs[ClassLabel.Negative.ordinal()] + bigramClassProbs[ClassLabel.Positive
			                                                     								.ordinal()]);
			System.out.print("Bigram Ratio:" + String.format("%.05f", Math.abs(bigramRatio*100)) + " %, ");
			
			double unigramRatio = (unigramClassProbs[ClassLabel.Negative.ordinal()] - unigramClassProbs[ClassLabel.Positive
			 			                                                     						.ordinal()])
			 			                                                     						/ (unigramClassProbs[ClassLabel.Negative.ordinal()] + unigramClassProbs[ClassLabel.Positive
			 			                                                     								.ordinal()]);
			
			System.out.print("Unigram Ratio:" + String.format("%.05f", Math.abs(unigramRatio*100)) + " %\t");
			
			double[] weighted = new double[unigramClassProbs.length];
			if (Math.abs(bigramRatio)>Math.abs(unigramRatio)){
				for (int i = 0; i < weighted.length; i++) {
					weighted[i] = bigramClassProbs[i];
				}
			}
			else
			{
				for (int i = 0; i < weighted.length; i++) {
					weighted[i] = unigramClassProbs[i];
				}
			}
			double maxValue = Double.NEGATIVE_INFINITY;
			int maxIndex = 0;
			for (int i = 0; i < weighted.length; i++) {
				if (weighted[i] > maxValue) {
					maxValue = weighted[i];
					maxIndex = i;
				}
			}
			return Tweet.ClassLabel.values()[maxIndex];
		}
	private static Tweet.ClassLabel predictTweetClass(Double[] ngramClassProbs,
		Double[] posTagClassProbs, double lambda) {
		double p = (ngramClassProbs[ClassLabel.Negative.ordinal()] - ngramClassProbs[ClassLabel.Positive
		                                                     						.ordinal()])
		                                                     						/ (ngramClassProbs[ClassLabel.Negative.ordinal()] + ngramClassProbs[ClassLabel.Positive
		                                                     								.ordinal()]);
		System.out.print("Class Difference:" + String.format("%.05f", Math.abs(p*100)) + " %\t");
		                                                     	
		double[] weighted = new double[ngramClassProbs.length];
		for (int i = 0; i < weighted.length; i++) {
			weighted[i] = (ngramClassProbs[i] * lambda)
					+ ((1 - lambda) * posTagClassProbs[i]);
		}
		double maxValue = Double.NEGATIVE_INFINITY;
		int maxIndex = 0;
		for (int i = 0; i < weighted.length; i++) {
			if (weighted[i] > maxValue) {
				maxValue = weighted[i];
				maxIndex = i;
			}
		}
		return Tweet.ClassLabel.values()[maxIndex];
	}
}
