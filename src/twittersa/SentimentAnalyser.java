package twittersa;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 */

/**
 * @author Munir Geden
 *
 */
public class SentimentAnalyser {
	public static void main(String[] args) {
		int numberOfInputs = 200;
		String commonDataPath = "/Users/msgeden/OneDrive/SSE/COMPGI15/TwitterSA/Data/";
		int ngramSize = Integer.parseInt(FileHandler
				.readConfigValue(Constants.NGRAM_SIZE_CONFIG));
		String distinctiveNgramsListFile = commonDataPath+ "distinctive_"+ngramSize+"-grams_list_by_entropy.tsv";
		String distinctiveNgramsFile = commonDataPath+ "distinctive_"+ngramSize+"-grams_by_entropy_threshold.tsv";
		String posProbsPolarityFile = commonDataPath+"distinctive_postags_list_for_polarity.tsv";
		String posProbsSubjectivityFile = commonDataPath+"distinctive_postags_list_for_subjectivity.tsv";
		
		String posCondProbsFile = commonDataPath+"conditional_probabilities_of_postags.tsv";
		String ngramCondProbsFile = commonDataPath+"conditional_probabilities_of_" + ngramSize +"-grams.tsv";

		String bigramCondProbsFile = commonDataPath+"conditional_probabilities_of_2-grams.tsv";
		String unigramCondProbsFile = commonDataPath+"conditional_probabilities_of_1-grams.tsv";
		String distinctiveUnigramsFile = commonDataPath+"distinctive_1-grams_by_entropy_threshold.tsv";
		String distinctiveBigramsFile = commonDataPath+"distinctive_2-grams_by_entropy_threshold.tsv";
		
		// Split file into test and training with ratio 1:4
		//args = new String[] {"-s", commonDataPath, "stanford.tsv"};

		// Constructs distinctive ngrams and their conditional probabilities
		//args = new String[] {"-rn", commonDataPath+"stanford.tsv"};

		// Constructs distinctive postags and their conditional probabilities
		//args = new String[] { "-rp", commonDataPath+"stanford.tsv" };

		// Constructs weka arff files of training and test data with for given
		// distinctive ngrams list
		//args = new String[] { "-w", commonDataPath+"stanford_polarity_0_1.tsv", distinctiveNgramsListFile,"training"};
		//args = new String[] { "-w", commonDataPath + "stanford_validation_polarity.tsv", distinctiveNgramsListFile,"test"};

		// Run weka classifier algorithms for the generated arff files
		//args = new String[] { "-wc", "j48", commonDataPath+"Train_ngram_"+ numberOfInputs+ "_" +ngramSize+".arff", commonDataPath+"Test_ngram_"+ numberOfInputs+ "_" +ngramSize+".arff" };

		// Run custom multinomial naive bayes classifier for the data
		//args = new String[] { "-mnbcpos", commonDataPath+"stanford_validation.tsv"};
		//args = new String[] { "-mnbcngram", commonDataPath+"stanford_validation.tsv"};
		//args = new String[] { "-mnbcboth", commonDataPath+"stanford_validation.tsv"};
		
		try {
			if (args.length > 0) {

				if ((args[0].equals("-s") || args[0].equals("--split"))) {
					FileHandler.readAndSplitTweets(args[1], args[2]);
				} else if ((args[0].equals("-rn") || args[0]
						.equals("--readngrams"))) {
					ArrayList<Tweet> trainTweets = PreProcessor
							.processTweets(args[1]);

					HashMap<String, Integer[]> ngrams = NgramExtractor
							.generateNgramsOfTweets(trainTweets, false);

					NgramExtractor.extractIGOfNgrams(ngrams);
					NgramExtractor.extractIGOfNgramsByThreshold(ngrams);
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

				else if ((args[0].equals("-w") || args[0].equals("--classifier"))) {
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
				}
				else if ((args[0].equals("-mnbcngram") || args[0]
						.equals("--mnbclassifierngram"))) {

					ArrayList<Tweet> testTweets = PreProcessor
							.processTweets(args[1]);

					HashMap<String, Double[]> unigramsCondProbs = NgramExtractor
							.readCondProbsOfNgramsFromFile(unigramCondProbsFile);

					HashMap<String, Double[]> bigramsCondProbs = NgramExtractor
							.readCondProbsOfNgramsFromFile(bigramCondProbsFile);

					HashMap<String, Double> distinctiveUnigrams = NgramExtractor
							.readDistinctiveNgramsFromFile(distinctiveUnigramsFile);

					HashMap<String, Double> distinctiveBigrams = NgramExtractor
							.readDistinctiveNgramsFromFile(distinctiveBigramsFile);

					HashMap<Long, Double[]> tweetsClassUnigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets, unigramsCondProbs, null, 1);
						
					HashMap<Long, Double[]> tweetsClassBigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets, bigramsCondProbs, null, 2);

					if (ngramSize == 1){
						//Use only unigrams
						MultinomialNaiveBayesClassifier.classifyTweetsByNgrams(testTweets, tweetsClassUnigramProbs);
					}
					if (ngramSize == 2){
						//Use only bigrams
						MultinomialNaiveBayesClassifier.classifyTweetsByNgrams(testTweets, tweetsClassBigramProbs);
					}
					//Use unigrams and bigrams together
					MultinomialNaiveBayesClassifier.classifyTweetsByNgrams(testTweets, tweetsClassBigramProbs, tweetsClassUnigramProbs);
				
				}
				else if ((args[0].equals("-mnbcpos") || args[0]
						.equals("--mnbclassifierpos"))) {

					ArrayList<Tweet> testTweets = PreProcessor
							.processTweets(args[1]);

					HashMap<String, Double[]> posCondProbs = POSExtractor
							.readCondProbsOfPosTagsFromFile(posCondProbsFile);

					HashMap<Long, Double[]> tweetsClassPosProbs = MultinomialNaiveBayesClassifier
							.calculatePosLogProbsOfTweets(testTweets, posCondProbs);

					MultinomialNaiveBayesClassifier.classifyTweetsByPosTags(testTweets, tweetsClassPosProbs);
				}
				else if ((args[0].equals("-mnbcboth") || args[0]
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
							.calculatePosLogProbsOfTweets(testTweets, posCondProbs);
					
					HashMap<Long, Double[]> tweetsClassUnigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets, unigramsCondProbs, null, 1);
					
					HashMap<Long, Double[]> tweetsClassBigramProbs = MultinomialNaiveBayesClassifier
							.calculateNgramLogProbsOfTweets(testTweets, bigramsCondProbs, null, 2);

					//lambda represents the weights between postags features and ngrams features
					MultinomialNaiveBayesClassifier.classifyTweets(testTweets, tweetsClassBigramProbs, tweetsClassUnigramProbs, tweetsClassPosProbs);
					MultinomialNaiveBayesClassifier.classifyTweets(testTweets, tweetsClassBigramProbs, tweetsClassPosProbs, 0.5);

					//alpha represents PosTag probs threshold and beta represents threshold for the ngram cond. probability difference
				    //MultinomialNaiveBayesClassifier.classifyTweetsSeparately(testTweets, tweetsClassBigramProbs, posSubjectivityProbs, 0.2, 0.1);

//					//lambda represents the weights between postags features and ngrams features
//					double start=0.0;
//					double optimumLambda=start;
//					double increment = 0.05;
//					double limit = 1;
//					ArrayList<Double[]> results1 = new ArrayList<Double[]>();
//					while(optimumLambda <= limit)
//					{
//						results1.add(new Double[]{MultinomialNaiveBayesClassifier.classifyTweets(testTweets, tweetsClassBigramProbs, tweetsClassPosProbs, optimumLambda),optimumLambda});
//						optimumLambda+=increment;
//					}//optimumlambda=0.5 for 3-classes.tsv
//					System.out.println("");
//					for (Double[] result:results1)
//					{
//						System.out.println("lambda:"+ String.format("%.5f",result[1]) + ", Success Ratio:"+result[0]*100 + " %");
//					}
					
//					//alpha represents PosTag probs threshold and beta represents threshold for the ngram cond. probability difference
//					double alphaStart=0.2;
//					double betaStart=0.0;
//					double optimumAlpha=alphaStart;
//					double optimumBeta=betaStart;
//					double incrementAlpha = 0.1;
//					double incrementBeta = 0.01;
//					double limitAlpha = 1;
//					double limitBeta = 0.1;
//					ArrayList<Double[]> results2 = new ArrayList<Double[]>();
//					while((optimumAlpha <= limitAlpha)||(optimumBeta <= limitBeta))
//					{
//						results2.add(new Double[]{MultinomialNaiveBayesClassifier.classifyTweetsSeparately(testTweets, tweetsClassBigramProbs, posSubjectivityProbs, optimumAlpha, optimumBeta),optimumAlpha,optimumBeta});
//						if(optimumAlpha <= limitAlpha)
//							optimumAlpha+=incrementAlpha;	
//						if(optimumBeta <= limitBeta)
//							optimumBeta+=incrementBeta;
//					}
//					System.out.println("");
//					for (Double[] result:results2)
//					{
//						System.out.println("alpha:"+ String.format("%.5f",result[1]) + ", beta:" + String.format("%.5f",result[2]) + ", Success Ratio:"+result[0]*100 + " %");
//					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
