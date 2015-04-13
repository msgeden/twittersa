package twittersa;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * 
 */

/**
 * @author Munir Geden
 *
 */
public class SentimentAnalyser extends BasicParser {
	public static void main(String[] args) {

		BasicParser parser = new BasicParser();
		Options options = new Options()
				.addOption("s", "-split", true, "split the given data file [f]")
				.addOption("gt", "-get-tweets", true, "collect the tweets of the given user [u]")
				.addOption("r", "-ratio", true, "split ratio of data file [r]")
				.addOption("n", "-ngram-size", true, "set ngram length [n]")
				.addOption("t", "training-file", true,
						"specify the training data file [f]")
				.addOption("dd", "define-data-path", true,
						"specify the absolute path data of data folder [p]")
				.addOption("dr", "define-report-path", true,
						"specify the absolute path of report folder [p]")
				.addOption("v", "test-file", true,
						"specify the test/validation data file [f]")
				.addOption(
						"xn",
						"extract-ngrams",
						false,
						"construct the class conditional probabilities of ngrams and extract the distinctive ngrams")
				.addOption("xp", "extract-postags", false,
						"construct the class conditional probabilities of POSTags")
				.addOption("n1", "unigram-condprobs-file", true,
						"specify unigrams file [f]")
				.addOption("n2", "bigram-condprobs-file", true,
						"specify bigrams file [f]")
				.addOption("n3", "trigram-condprobs-file", true,
						"specify trigrams file [f]")
				.addOption("p", "postag-condprobs-file", true,
						"specify postags file [f]")
				.addOption("d", "distinctive-ngrams-file", true,
						"specify the distinctive ngrams file [f]")
				.addOption(
						"cp",
						"classify-by-postags",
						false,
						"run multinomial naive bayes classifier for the validation file by using POSTags")
				.addOption(
						"cn",
						"classify-by-ngrams",
						false,
						"run multinomial naive bayes classifier for the validation file by using given ngrams")
				.addOption(
						"cnp",
						"classify-by-ngrams-postags",
						false,
						"run multinomial naive bayes classifier for the validation file by using given ngrams and POSTags")
				.addOption("l", "lambda", true,
						"specify the lambda value to tune weight of ngram and POSTag features [l]")
				.addOption(
						"gw",
						"generate-weka-data",
						false,
						"generate weka *.arrf files of the given data file from the distinctive ngrams list [f]")
				.addOption(
						"i",
						"number-of-inputs",
						true,
						"specify the number of ngrams as inputs that to be used for weka classfiers [i]")
				.addOption("cw", "classify-with-weka", true,
						"run weka classifier with the specified algorithm [a]")
				.addOption("wt", "weka-training-file", true,
						"specify the training *.arff file [f] for weka classifier")
				.addOption("wv", "weka-validation-file", true,
						"specify the test/validation *.arff file [f] for weka classifier");

		//String commonDataPath = "/Users/msgeden/OneDrive/SSE/COMPGI15/TwitterSA/Data/";
		// args = new String[] { "-s", commonDataPath +
		// "stanford_polarity_0_1.tsv", "-r", "10" };

		// args = new String[] { "-gt", "msgeden"};

		// args = new String[] { "-xp", "-t", commonDataPath +
		// "stanford_polarity_0_1_reduced.tsv"};

		// args = new String[] { "-xn", "-t", commonDataPath +
		// "stanford_polarity_0_1_reduced.tsv", "-n", "1"};
		// args = new String[] { "-xn", "-t", commonDataPath +
		// "stanford_polarity_0_1_reduced.tsv", "-n", "2"};

		// args = new String[] { "-cp", "-v", commonDataPath +
		// "stanford_validation_polarity.tsv", "-p",commonDataPath +
		// "conditional_probabilities_of_postags.tsv"};

		// args = new String[] { "-cn", "-v", commonDataPath +
		// "stanford_validation_polarity.tsv", "-n1",commonDataPath +
		// "conditional_probabilities_of_1-grams.tsv"};
		// args = new String[] { "-cn", "-v", commonDataPath +
		// "stanford_validation_polarity.tsv", "-n2",commonDataPath +
		// "conditional_probabilities_of_2-grams.tsv"};
		// args = new String[] { "-cn", "-v", commonDataPath +
		// "stanford_validation_polarity.tsv", "-n1",commonDataPath +
		// "conditional_probabilities_of_1-grams.tsv", "-n2",commonDataPath +
		// "conditional_probabilities_of_2-grams.tsv"};

		// args = new String[] { "-cnp", "-v", commonDataPath +
		// "stanford_validation_polarity.tsv", "-p",commonDataPath +
		// "conditional_probabilities_of_postags.tsv", "-n1",commonDataPath +
		// "conditional_probabilities_of_1-grams.tsv", "-n2",commonDataPath +
		// "conditional_probabilities_of_2-grams.tsv"};
		// args = new String[] { "-cnp", "-v", commonDataPath +
		// "stanford_validation_polarity.tsv", "-p",commonDataPath +
		// "conditional_probabilities_of_postags.tsv", "-n1",commonDataPath +
		// "conditional_probabilities_of_1-grams.tsv", "-l", "0.45"};

		//args = new String[] {
		//		"-gw",
		//		"-n",
		//		"1",
		//		"-t",
		//		commonDataPath + "stanford_polarity_0_1_reduced.tsv",
		//		"-v",
		//		commonDataPath + "stanford_validation_polarity.tsv",
		//		"-d",
		//		commonDataPath
		//				+ "distinctive_1-grams_list_by_information_gain.tsv" };

		//args = new String[] {
		//		"-cw",
		//		"mnb",
		//		"-wt",
		//		commonDataPath + "train_ngram_1000_1.tsv",
		//		"-wv",
		//		commonDataPath + "test_ngram_1000_1.tsv"
		//		};

		try {
			CommandLine commandLine = parser.parse(options, args);
			if (commandLine.hasOption("s"))
				FileHandler.splitFile(commandLine.getOptionValue("s"),
						Integer.parseInt(commandLine.getOptionValue("r")));
			if (commandLine.hasOption("gt"))
				TwitterAPIManager.getTweets(commandLine.getOptionValue("gt"));
			if (commandLine.hasOption("n"))
				FileHandler.writeConfigValue(Constants.NGRAM_SIZE_CONFIG,
						commandLine.getOptionValue("n"));
			if (commandLine.hasOption("t"))
				FileHandler.writeConfigValue(
						Constants.TRAINING_CORPUS_PATH_CONFIG,
						commandLine.getOptionValue("t"));
			if (commandLine.hasOption("v"))
				FileHandler.writeConfigValue(Constants.TEST_CORPUS_PATH_CONFIG,
						commandLine.getOptionValue("v"));
			if (commandLine.hasOption("i"))
				FileHandler.writeConfigValue(
						Constants.NUMBER_OF_DATA_INPUT_CONFIG,
						commandLine.getOptionValue("i"));

			if (commandLine.hasOption("xp")) {
				ArrayList<Tweet> trainTweets = PreProcessor
						.processTweets(commandLine.hasOption("t")?commandLine.getOptionValue("t"):Constants.TRAINING_CORPUS_PATH);
				POSExtractor.calculateCondProbsOfPosTags(trainTweets);
			}
			if (commandLine.hasOption("xn")) {
				ArrayList<Tweet> trainTweets = PreProcessor
						.processTweets(commandLine.hasOption("t")?commandLine.getOptionValue("t"):Constants.TRAINING_CORPUS_PATH);
				HashMap<String, Integer[]> ngrams = NgramExtractor
						.generateNgramsOfTweets(trainTweets, false);
				NgramExtractor.extractIGOfNgrams(ngrams);
				NgramExtractor.extractEntropyOfNgrams(ngrams);
				NgramExtractor.extractSalienceOfNgrams(ngrams);
				NgramExtractor.calculateCondProbsOfNgrams(ngrams);
			}
			if (commandLine.hasOption("gw")) {
				HashMap<String, Double> topRankedNgrams = NgramExtractor
						.getTopRankedNgrams(commandLine.getOptionValue("d"));

				if (commandLine.hasOption("t")) {
					ArrayList<Tweet> trainTweets = PreProcessor
							.processTweets(commandLine.getOptionValue("t"));
					NgramExtractor
							.prepareWekaFileDataFromTweets(
									topRankedNgrams,
									trainTweets,
									Constants.REPORTS_PATH,
									false);
				}
				if (commandLine.hasOption("v")) {
					ArrayList<Tweet> testTweets = PreProcessor
							.processTweets(commandLine.getOptionValue("v"));

					NgramExtractor
							.prepareWekaFileDataFromTweets(
									topRankedNgrams,
									testTweets,
									Constants.REPORTS_PATH,
									true);
				}
			}
			if (commandLine.hasOption("cw")) {

				WekaClassifier.getClassifierResults(
						commandLine.getOptionValue("wt"),
						commandLine.getOptionValue("wv"),
						commandLine.getOptionValue("cw"),
						commandLine.hasOption("i")?Integer.parseInt(commandLine.getOptionValue("i")):Constants.NUMBER_OF_DATA_INPUT);
			}
			if (commandLine.hasOption("cn")) {
				ArrayList<Tweet> testTweets = PreProcessor
						.processTweets(commandLine.hasOption("v")?commandLine.getOptionValue("v"):Constants.TEST_CORPUS_PATH);

				HashMap<String, Double[]> unigramsCondProbs = null;
				HashMap<String, Double[]> bigramsCondProbs = null;
				HashMap<String, Double[]> trigramsCondProbs = null;
				HashMap<Long, Double[]> tweetsClassUnigramProbs = null;
				HashMap<Long, Double[]> tweetsClassBigramProbs = null;
				HashMap<Long, Double[]> tweetsClassTrigramProbs = null;
				if (commandLine.hasOption("n1")) {
					unigramsCondProbs = NgramExtractor
							.readCondProbsOfNgramsFromFile(commandLine
									.getOptionValue("n1"));
					tweetsClassUnigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets,
									unigramsCondProbs, null, 1);

				}
				if (commandLine.hasOption("n2")) {
					bigramsCondProbs = NgramExtractor
							.readCondProbsOfNgramsFromFile(commandLine
									.getOptionValue("n2"));
					tweetsClassBigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets,
									bigramsCondProbs, null, 2);
				}
				if (commandLine.hasOption("n3")) {
					trigramsCondProbs = NgramExtractor
							.readCondProbsOfNgramsFromFile(commandLine
									.getOptionValue("n3"));
					tweetsClassTrigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets,
									trigramsCondProbs, null, 3);
				}
				if (commandLine.hasOption("n1") && commandLine.hasOption("n2")
						&& commandLine.hasOption("n3")) {
					MultinomialNaiveBayesClassifier.classifyTweetsByAllNgrams(
							testTweets, tweetsClassTrigramProbs,
							tweetsClassBigramProbs, tweetsClassUnigramProbs);
				} else if (commandLine.hasOption("n1")
						&& commandLine.hasOption("n2")) {
					// Use unigrams and bigrams together
					MultinomialNaiveBayesClassifier.classifyTweetsByBothNgrams(
							testTweets, tweetsClassBigramProbs,
							tweetsClassUnigramProbs);
				} else if (commandLine.hasOption("n1")) {
					// Use only unigrams
					MultinomialNaiveBayesClassifier.classifyTweetsByNgrams(
							testTweets, tweetsClassUnigramProbs);
				} else if (commandLine.hasOption("n2")) {
					// Use only unigrams
					MultinomialNaiveBayesClassifier.classifyTweetsByNgrams(
							testTweets, tweetsClassBigramProbs);
				}
			}
			if (commandLine.hasOption("cp")) {
				ArrayList<Tweet> testTweets = PreProcessor
						.processTweets(commandLine.hasOption("v")?commandLine.getOptionValue("v"):Constants.TEST_CORPUS_PATH);

				HashMap<String, Double[]> posCondProbs = POSExtractor
						.readCondProbsOfPosTagsFromFile(commandLine
								.getOptionValue("p"));

				HashMap<Long, Double[]> tweetsClassPosProbs = MultinomialNaiveBayesClassifier
						.calculatePosTagLogProbsOfTweets(testTweets,
								posCondProbs);

				MultinomialNaiveBayesClassifier.classifyTweetsByPosTags(
						testTweets, tweetsClassPosProbs);

			}
			if (commandLine.hasOption("cnp")) {
				ArrayList<Tweet> testTweets = PreProcessor
						.processTweets(commandLine.hasOption("v")?commandLine.getOptionValue("v"):Constants.TEST_CORPUS_PATH);

				HashMap<String, Double[]> posCondProbs = POSExtractor
						.readCondProbsOfPosTagsFromFile(commandLine
								.getOptionValue("p"));

				HashMap<Long, Double[]> tweetsClassPosProbs = MultinomialNaiveBayesClassifier
						.calculatePosTagLogProbsOfTweets(testTweets,
								posCondProbs);

				HashMap<String, Double[]> unigramsCondProbs = null;
				HashMap<String, Double[]> bigramsCondProbs = null;
				HashMap<Long, Double[]> tweetsClassUnigramProbs = null;
				HashMap<Long, Double[]> tweetsClassBigramProbs = null;
				if (commandLine.hasOption("n1") && commandLine.hasOption("n2")) {
					unigramsCondProbs = NgramExtractor
							.readCondProbsOfNgramsFromFile(commandLine
									.getOptionValue("n1"));
					tweetsClassUnigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets,
									unigramsCondProbs, null, 1);

					bigramsCondProbs = NgramExtractor
							.readCondProbsOfNgramsFromFile(commandLine
									.getOptionValue("n2"));
					tweetsClassBigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets,
									bigramsCondProbs, null, 2);

					MultinomialNaiveBayesClassifier
							.classifyTweetsByBothNgramsAndPosTags(testTweets,
									tweetsClassBigramProbs,
									tweetsClassUnigramProbs,
									tweetsClassPosProbs);
				} else if (commandLine.hasOption("n1")) {
					unigramsCondProbs = NgramExtractor
							.readCondProbsOfNgramsFromFile(commandLine
									.getOptionValue("n1"));
					tweetsClassUnigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets,
									unigramsCondProbs, null, 1);
					MultinomialNaiveBayesClassifier
							.classifyTweetsByNgramsAndPosTags(testTweets,
									tweetsClassUnigramProbs,
									tweetsClassPosProbs, Double
											.parseDouble(commandLine
													.getOptionValue("l")));
				}

				else if (commandLine.hasOption("n2")) {
					bigramsCondProbs = NgramExtractor
							.readCondProbsOfNgramsFromFile(commandLine
									.getOptionValue("n2"));
					tweetsClassBigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets,
									bigramsCondProbs, null, 2);
					MultinomialNaiveBayesClassifier
							.classifyTweetsByNgramsAndPosTags(testTweets,
									tweetsClassBigramProbs,
									tweetsClassPosProbs, Double
											.parseDouble(commandLine
													.getOptionValue("l")));
				}
			}

		} catch (Exception parseException) {
			System.out.println("Exception " + parseException.getMessage());
		}
	}}
