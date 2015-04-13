package twittersa;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

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
				.addOption("r", "-ratio", true, "split ratio of data file [r]")
				.addOption("n", "-ngram-size", true, "set ngram length [n]")
				.addOption("t", "training-file", true,
						"specify the training data file [f]")
				.addOption("v", "test-file", true,
						"specify the test/validation data file [f]")
				.addOption(
						"xn",
						"extract-ngrams",
						true,
						"construct the class conditional probabilities of ngrams and extract the distinctive ngrams")
				.addOption(
						"xp",
						"extract-postags",
						true,
						"construct the class conditional probabilities of POSTags and extract the POSTags")
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
						"cn",
						"classify-by-ngrams",
						true,
						"run multinomial naive bayes classifier with the suitable options => 1:unigrams, 2:bigrams, 3:unigrams+bigrams [o]")
				.addOption("cp", "classify-by-postags", true,
						"run multinomial naive bayes classifier on the given validation file [f]")
				.addOption(
						"gw",
						"generate-weka-data",
						true,
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

		String commonDataPath = "/Users/msgeden/OneDrive/SSE/COMPGI15/TwitterSA/Data/";
		//args = new String[] { "-s",
		//		commonDataPath + "stanford_polarity_0_1.tsv", "-r", "10" };

		try {
			CommandLine commandLine = parser.parse(options, args);
			System.out.println(commandLine.getOptionValue("b"));
			if (commandLine.hasOption("s"))
				FileHandler.splitFile(commandLine.getOptionValue("s"),
						Integer.parseInt(commandLine.getOptionValue("r")));
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
				FileHandler.writeConfigValue(Constants.NUMBER_OF_DATA_INPUT_CONFIG,
						commandLine.getOptionValue("i"));
			
			if (commandLine.hasOption("xp")) {
				ArrayList<Tweet> trainTweets = PreProcessor
						.processTweets(commandLine.getOptionValue("t"));
				POSExtractor.calculateCondProbsOfPosTags(trainTweets);
			}
			if (commandLine.hasOption("xn")) {
				ArrayList<Tweet> trainTweets = PreProcessor
						.processTweets(commandLine.getOptionValue("t"));
				HashMap<String, Integer[]> ngrams = NgramExtractor
						.generateNgramsOfTweets(trainTweets, false);
				NgramExtractor.extractIGOfNgrams(ngrams);
				NgramExtractor.extractEntropyOfNgrams(ngrams);
				NgramExtractor.extractSalienceOfNgrams(ngrams);
				NgramExtractor.calculateCondProbsOfNgrams(ngrams);
			}
			if (commandLine.hasOption("gw")) {
				ArrayList<Tweet> trainTweets = PreProcessor
						.processTweets(commandLine.getOptionValue("t"));
				HashMap<String, Double> topRankedNgrams = NgramExtractor.getTopRankedNgrams(commandLine.getOptionValue("d"));
				NgramExtractor
						.prepareWekaFileDataFromTweets(
								topRankedNgrams,
								trainTweets,
								FileHandler
										.readConfigValue(Constants.REPORTS_PATH_CONFIG),
								false);
				ArrayList<Tweet> testTweets = PreProcessor
						.processTweets(commandLine.getOptionValue("v"));
				
				NgramExtractor
						.prepareWekaFileDataFromTweets(
								topRankedNgrams,
								testTweets,
								FileHandler
										.readConfigValue(Constants.REPORTS_PATH_CONFIG),
								true);
			}
			if (commandLine.hasOption("cw")) {
				
				WekaClassifier.getClassifierResults(commandLine.getOptionValue("wt"), commandLine.getOptionValue("wv"),
						commandLine.getOptionValue("cw"), Integer.parseInt(commandLine.getOptionValue("i")));
			}
			if (commandLine.hasOption("cn")) {
				ArrayList<Tweet> testTweets = PreProcessor
						.processTweets(commandLine.getOptionValue("v"));

				HashMap<String, Double[]> unigramsCondProbs = null;
				HashMap<String, Double[]> bigramsCondProbs = null;
				HashMap<String, Double[]> trigramsCondProbs = null;
				HashMap<Long, Double[]> tweetsClassUnigramProbs = null;
				HashMap<Long, Double[]> tweetsClassBigramProbs = null;
				HashMap<Long, Double[]> tweetsClassTrigramProbs = null;
				if (commandLine.hasOption("n1"))
				{
					unigramsCondProbs = NgramExtractor
						.readCondProbsOfNgramsFromFile(commandLine.getOptionValue("n1"));
					tweetsClassUnigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets,
									unigramsCondProbs, null, 1);

				}
				if (commandLine.hasOption("n2"))
				{
					bigramsCondProbs = NgramExtractor
						.readCondProbsOfNgramsFromFile(commandLine.getOptionValue("n2"));
					tweetsClassBigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets,
									bigramsCondProbs, null, 2);
				}
				if (commandLine.hasOption("n3"))
				{
					trigramsCondProbs = NgramExtractor
						.readCondProbsOfNgramsFromFile(commandLine.getOptionValue("n3"));
					tweetsClassTrigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets,
									trigramsCondProbs, null, 3);
				}
				if (commandLine.hasOption("n1") && commandLine.hasOption("n2")&& commandLine.hasOption("n2")) 
				{
					MultinomialNaiveBayesClassifier.classifyTweetsByAllNgrams(testTweets, tweetsClassTrigramProbs, tweetsClassBigramProbs, tweetsClassUnigramProbs);
				}
				else if (commandLine.hasOption("n1") && commandLine.hasOption("n2")) {
					// Use unigrams and bigrams together
					MultinomialNaiveBayesClassifier.classifyTweetsByBothNgrams(
							testTweets, tweetsClassBigramProbs,
							tweetsClassUnigramProbs);
				}
				else if (commandLine.hasOption("n1")) {
					// Use only unigrams
					MultinomialNaiveBayesClassifier.classifyTweetsByNgrams(
							testTweets, tweetsClassUnigramProbs);
				}
				else if (commandLine.hasOption("n2")) {
					// Use only unigrams
					MultinomialNaiveBayesClassifier.classifyTweetsByNgrams(
							testTweets, tweetsClassBigramProbs);
				}
			}
			if (commandLine.hasOption("cp")) {
				ArrayList<Tweet> testTweets = PreProcessor
						.processTweets(commandLine.getOptionValue("v"));

				HashMap<String, Double[]> posCondProbs = POSExtractor
						.readCondProbsOfPosTagsFromFile(commandLine.getOptionValue("p"));

				HashMap<Long, Double[]> tweetsClassPosProbs = MultinomialNaiveBayesClassifier
						.calculatePosTagLogProbsOfTweets(testTweets,
								posCondProbs);

				MultinomialNaiveBayesClassifier.classifyTweetsByPosTags(
						testTweets, tweetsClassPosProbs);

			}
			if (commandLine.hasOption("cnp")) {
				ArrayList<Tweet> testTweets = PreProcessor
						.processTweets(commandLine.getOptionValue("v"));

				HashMap<String, Double[]> posCondProbs = POSExtractor
						.readCondProbsOfPosTagsFromFile(commandLine.getOptionValue("p"));

				HashMap<Long, Double[]> tweetsClassPosProbs = MultinomialNaiveBayesClassifier
						.calculatePosTagLogProbsOfTweets(testTweets,
								posCondProbs);

				HashMap<String, Double[]> unigramsCondProbs = null;
				HashMap<String, Double[]> bigramsCondProbs = null;
				HashMap<Long, Double[]> tweetsClassUnigramProbs = null;
				HashMap<Long, Double[]> tweetsClassBigramProbs = null;
				if (commandLine.hasOption("n1") && commandLine.hasOption("n2"))
				{
					unigramsCondProbs = NgramExtractor
						.readCondProbsOfNgramsFromFile(commandLine.getOptionValue("n1"));
					tweetsClassUnigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets,
									unigramsCondProbs, null, 1);

					bigramsCondProbs = NgramExtractor
							.readCondProbsOfNgramsFromFile(commandLine.getOptionValue("n2"));
						tweetsClassBigramProbs = MultinomialNaiveBayesClassifier
								.calculateNgramLogProbsOfTweets(testTweets,
										bigramsCondProbs, null, 2);

					MultinomialNaiveBayesClassifier
						.classifyTweetsByBothNgramsAndPosTags(testTweets,
								tweetsClassBigramProbs,
								tweetsClassUnigramProbs,
								tweetsClassPosProbs);						
				}
				else if (commandLine.hasOption("n1"))
				{
					unigramsCondProbs = NgramExtractor
						.readCondProbsOfNgramsFromFile(commandLine.getOptionValue("n1"));
					tweetsClassUnigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets,
									unigramsCondProbs, null, 1);
					MultinomialNaiveBayesClassifier
					.classifyTweetsByNgramsAndPosTags(
							testTweets,
							tweetsClassUnigramProbs,
							tweetsClassPosProbs,
							Double.parseDouble(commandLine.getOptionValue("l")));
				}

				else if (commandLine.hasOption("n2"))
				{
					bigramsCondProbs = NgramExtractor
						.readCondProbsOfNgramsFromFile(commandLine.getOptionValue("n2"));
					tweetsClassBigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets,
									bigramsCondProbs, null, 2);
					MultinomialNaiveBayesClassifier
					.classifyTweetsByNgramsAndPosTags(
							testTweets,
							tweetsClassBigramProbs,
							tweetsClassPosProbs,
							Double.parseDouble(commandLine.getOptionValue("l")));
				}
			}
			

		} catch (Exception parseException) {
			System.out.println("Exception " + parseException.getMessage());
		}
	}

	public static void mainOld(String[] args) {
		int numberOfInputs = Integer.parseInt(FileHandler
				.readConfigValue(Constants.NUMBER_OF_DATA_INPUT_CONFIG));
		;
		String commonDataPath = "/Users/msgeden/OneDrive/SSE/COMPGI15/TwitterSA/Data/";
		int ngramSize = Integer.parseInt(FileHandler
				.readConfigValue(Constants.NGRAM_SIZE_CONFIG));
		String distinctiveNgramsListFile = commonDataPath + "distinctive_"
				+ ngramSize + "-grams_list_by_salience.tsv";
		String distinctiveNgramsFile = commonDataPath + "distinctive_"
				+ ngramSize + "-grams_by_entropy_threshold.tsv";
		String posProbsPolarityFile = commonDataPath
				+ "distinctive_postags_list_for_polarity.tsv";
		String posProbsSubjectivityFile = commonDataPath
				+ "distinctive_postags_list_for_subjectivity.tsv";

		String posCondProbsFile = commonDataPath
				+ "conditional_probabilities_of_postags.tsv";
		String ngramCondProbsFile = commonDataPath
				+ "conditional_probabilities_of_" + ngramSize + "-grams.tsv";

		String trigramCondProbsFile = commonDataPath
				+ "conditional_probabilities_of_3-grams.tsv";
		String bigramCondProbsFile = commonDataPath
				+ "conditional_probabilities_of_2-grams.tsv";
		String unigramCondProbsFile = commonDataPath
				+ "conditional_probabilities_of_1-grams.tsv";
		String distinctiveUnigramsFile = commonDataPath
				+ "distinctive_1-grams_by_entropy_threshold.tsv";
		String distinctiveBigramsFile = commonDataPath
				+ "distinctive_2-grams_by_entropy_threshold.tsv";

		// args = new String[] {"-s", commonDataPath,
		// "stanford_polarity_0_1.tsv"};

		// Constructs distinctive ngrams and their conditional probabilities
		// args = new String[] {"-rn",
		// commonDataPath+"stanford_polarity_0_5.tsv"};

		// Constructs distinctive postags and their conditional probabilities
		// args = new String[] { "-rp",
		// commonDataPath+"stanford_polarity_0_1.tsv" };

		// Constructs weka arff files of training and test data with for given
		// distinctive ngrams list
		// args = new String[] { "-w",
		// commonDataPath+"stanford_polarity_0_02.tsv",
		// distinctiveNgramsListFile,"training"};
		// args = new String[] { "-w", commonDataPath +
		// "stanford_validation_polarity.tsv",
		// distinctiveNgramsListFile,"test"};

		// Run weka classifier algorithms for the generated arff files
		// args = new String[] { "-wc", "svm", commonDataPath+"train_ngram_"+
		// numberOfInputs+ "_" +ngramSize+".arff", commonDataPath+"test_ngram_"+
		// numberOfInputs+ "_" +ngramSize+".arff" };
		// args = new String[] { "-wc", "nb", commonDataPath+"train_ngram_"+
		// numberOfInputs+ "_" +ngramSize+"_entropy.arff",
		// commonDataPath+"test_ngram_"+ numberOfInputs+ "_"
		// +ngramSize+"_entropy.arff" };
		// args = new String[] { "-wc", "nb", commonDataPath+"train_ngram_"+
		// numberOfInputs+ "_" +ngramSize+"_salience.arff",
		// commonDataPath+"test_ngram_"+ numberOfInputs+ "_"
		// +ngramSize+"_salience.arff" };
		// args = new String[] { "-wc", "nb", commonDataPath+"train_ngram_"+
		// numberOfInputs+ "_" +ngramSize+"_information_gain.arff",
		// commonDataPath+"test_ngram_"+ numberOfInputs+ "_"
		// +ngramSize+"_information_gain.arff" };

		// Run custom multinomial naive bayes classifier for the data
		// args = new String[] { "-mnbcpos",
		// commonDataPath+"stanford_validation_polarity.tsv"};
		// args = new String[] { "-mnbcngram",
		// commonDataPath+"stanford_validation_polarity.tsv"};
		// args = new String[] { "-mnbcboth",
		// commonDataPath+"stanford_validation_polarity.tsv"};

		try {
			if (args.length > 0) {

				if ((args[0].equals("-s") || args[0].equals("--split"))) {
					FileHandler.splitTweets(args[1], args[2]);
				} else if ((args[0].equals("-rn") || args[0]
						.equals("--readngrams"))) {
					ArrayList<Tweet> trainTweets = PreProcessor
							.processTweets(args[1]);

					HashMap<String, Integer[]> ngrams = NgramExtractor
							.generateNgramsOfTweets(trainTweets, false);

					NgramExtractor.extractIGOfNgrams(ngrams);
					NgramExtractor.extractEntropyOfNgrams(ngrams);
					NgramExtractor.extractEntropyOfNgramsByThreshold(ngrams);
					NgramExtractor.extractSalienceOfNgrams(ngrams);
					NgramExtractor.extractSalienceOfNgramsByThreshold(ngrams);

					NgramExtractor.calculateCondProbsOfNgrams(ngrams);

				} else if ((args[0].equals("-rp") || args[0]
						.equals("--readpos"))) {
					ArrayList<Tweet> trainTweets = PreProcessor
							.processTweets(args[1]);

					POSExtractor.calculatePOSProbsForSubjectivity(trainTweets);
					POSExtractor.calculatePOSProbsForPolarity(trainTweets);
					POSExtractor.calculateCondProbsOfPosTags(trainTweets);
				}

				else if ((args[0].equals("-w") || args[0]
						.equals("--classifier"))) {
					ArrayList<Tweet> tweets = PreProcessor
							.processTweets(args[1]);
					HashMap<String, Double> topRankedNgrams = NgramExtractor
							.getTopRankedNgrams(args[2]);
					NgramExtractor
							.prepareWekaFileDataFromTweets(
									topRankedNgrams,
									tweets,
									FileHandler
											.readConfigValue(Constants.REPORTS_PATH_CONFIG),
									args[3].equals("test"));
				} else if ((args[0].equals("-wc") || args[0]
						.equals("--wekaclassifier"))) {
					WekaClassifier.getClassifierResults(args[2], args[3],
							args[1], numberOfInputs);
				} else if ((args[0].equals("-mnbcngram") || args[0]
						.equals("--mnbclassifierngram"))) {

					ArrayList<Tweet> testTweets = PreProcessor
							.processTweets(args[1]);

					HashMap<String, Double[]> unigramsCondProbs = NgramExtractor
							.readCondProbsOfNgramsFromFile(unigramCondProbsFile);

					HashMap<String, Double[]> bigramsCondProbs = NgramExtractor
							.readCondProbsOfNgramsFromFile(bigramCondProbsFile);

					HashMap<String, Double[]> trigramsCondProbs = NgramExtractor
							.readCondProbsOfNgramsFromFile(trigramCondProbsFile);

					HashMap<String, Double> distinctiveUnigrams = NgramExtractor
							.readDistinctiveNgramsFromFile(distinctiveUnigramsFile);

					HashMap<String, Double> distinctiveBigrams = NgramExtractor
							.readDistinctiveNgramsFromFile(distinctiveBigramsFile);

					HashMap<Long, Double[]> tweetsClassUnigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets,
									unigramsCondProbs, null, 1);

					HashMap<Long, Double[]> tweetsClassBigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets,
									bigramsCondProbs, null, 2);

					HashMap<Long, Double[]> tweetsClassTrigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets,
									trigramsCondProbs, null, 3);

					if (ngramSize == 1) {
						// Use only unigrams
						MultinomialNaiveBayesClassifier.classifyTweetsByNgrams(
								testTweets, tweetsClassUnigramProbs);
					}
					if (ngramSize == 2) {
						// Use only bigrams
						MultinomialNaiveBayesClassifier.classifyTweetsByNgrams(
								testTweets, tweetsClassBigramProbs);
					}
					if (ngramSize == 3) {
						// Use only bigrams
						MultinomialNaiveBayesClassifier.classifyTweetsByNgrams(
								testTweets, tweetsClassTrigramProbs);
					}
					// Use unigrams and bigrams together
					MultinomialNaiveBayesClassifier.classifyTweetsByBothNgrams(
							testTweets, tweetsClassBigramProbs,
							tweetsClassUnigramProbs);
					MultinomialNaiveBayesClassifier.classifyTweetsByAllNgrams(
							testTweets, tweetsClassTrigramProbs,
							tweetsClassBigramProbs, tweetsClassUnigramProbs);

				} else if ((args[0].equals("-mnbcpos") || args[0]
						.equals("--mnbclassifierpos"))) {

					ArrayList<Tweet> testTweets = PreProcessor
							.processTweets(args[1]);

					HashMap<String, Double[]> posCondProbs = POSExtractor
							.readCondProbsOfPosTagsFromFile(posCondProbsFile);

					HashMap<Long, Double[]> tweetsClassPosProbs = MultinomialNaiveBayesClassifier
							.calculatePosTagLogProbsOfTweets(testTweets,
									posCondProbs);

					MultinomialNaiveBayesClassifier.classifyTweetsByPosTags(
							testTweets, tweetsClassPosProbs);
				} else if ((args[0].equals("-mnbcboth") || args[0]
						.equals("--mnbclassifierboth"))) {

					ArrayList<Tweet> testTweets = PreProcessor
							.processTweets(args[1]);

					HashMap<String, Double> posPolarityProbs = POSExtractor
							.readPOSProbsFromFile(posProbsPolarityFile);

					HashMap<String, Double> posSubjectivityProbs = POSExtractor
							.readPOSProbsFromFile(posProbsSubjectivityFile);

					HashMap<String, Double[]> posCondProbs = POSExtractor
							.readCondProbsOfPosTagsFromFile(posCondProbsFile);

					HashMap<String, Double[]> unigramsCondProbs = NgramExtractor
							.readCondProbsOfNgramsFromFile(unigramCondProbsFile);

					HashMap<String, Double[]> bigramsCondProbs = NgramExtractor
							.readCondProbsOfNgramsFromFile(bigramCondProbsFile);

					HashMap<String, Double> distinctiveUnigrams = NgramExtractor
							.readDistinctiveNgramsFromFile(distinctiveUnigramsFile);

					HashMap<String, Double> distinctiveBigrams = NgramExtractor
							.readDistinctiveNgramsFromFile(distinctiveBigramsFile);

					HashMap<Long, Double[]> tweetsClassPosProbs = MultinomialNaiveBayesClassifier
							.calculatePosTagLogProbsOfTweets(testTweets,
									posCondProbs);

					HashMap<Long, Double[]> tweetsClassUnigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets,
									unigramsCondProbs, null, 1);

					HashMap<Long, Double[]> tweetsClassBigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets,
									bigramsCondProbs, null, 2);

					MultinomialNaiveBayesClassifier
							.classifyTweetsByBothNgramsAndPosTags(testTweets,
									tweetsClassBigramProbs,
									tweetsClassUnigramProbs,
									tweetsClassPosProbs);

					// lambda represents the weights between postags features
					// and ngrams features
					String resultsPath = FileHandler
							.readConfigValue(Constants.REPORTS_PATH_CONFIG)
							+ File.separator
							+ "mnb_results_with_"
							+ ngramSize
							+ "-grams_and_postags.tsv";
					File resultsFile = new File(resultsPath);
					double start = 0.0;
					double optimumLambda = start;
					double increment = 0.05;
					double limit = 1.0;
					ArrayList<Double[]> results1 = new ArrayList<Double[]>();
					while (optimumLambda <= limit) {
						if (ngramSize == 1)
							results1.add(new Double[] {
									MultinomialNaiveBayesClassifier
											.classifyTweetsByNgramsAndPosTags(
													testTweets,
													tweetsClassUnigramProbs,
													tweetsClassPosProbs,
													optimumLambda),
									optimumLambda });
						else
							results1.add(new Double[] {
									MultinomialNaiveBayesClassifier
											.classifyTweetsByNgramsAndPosTags(
													testTweets,
													tweetsClassBigramProbs,
													tweetsClassPosProbs,
													optimumLambda),
									optimumLambda });
						optimumLambda += increment;
					}// optimumlambda=0.5 for 3-classes.tsv
					System.out.println("");
					FileUtils.write(resultsFile, "\n", true);
					for (Double[] result : results1) {
						System.out.println("lambda:"
								+ String.format("%.5f", result[1])
								+ ", Success Ratio:" + result[0] * 100 + " %");
						FileUtils.write(resultsFile,
								"lambda:" + String.format("%.5f", result[1])
										+ ", Success Ratio:" + result[0] * 100
										+ " %\n", true);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
