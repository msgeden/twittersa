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
		String distinctiveNgramsFile = commonDataPath+"threshold_entropy_ngrams_" + ngramSize + ".tsv";
		String distinctiveNgramsListFile = commonDataPath+"list_entropy_ranked_ngrams_" + ngramSize + ".tsv";
		String posProbsFile = commonDataPath+"list_ranked_pos_polarity.tsv";
		String posCondProbsFile = commonDataPath+"condprob_postags.tsv";
		String ngramCondProbsFile = commonDataPath+"condprob_ngrams_"+ ngramSize + ".tsv";

		String bigramCondProbsFile = commonDataPath+"condprob_ngrams_"+ 2 + ".tsv";
		String unigramCondProbsFile = commonDataPath+"condprob_ngrams_"+ 1 + ".tsv";
		String distinctiveUnigramsFile = commonDataPath+"threshold_entropy_ngrams_1.tsv";
		String distinctiveBigramsFile = commonDataPath+"threshold_entropy_ngrams_2.tsv";
		
		// Split file into test and training with ratio 1:4
		//args = new String[] {"-s", commonDataPath, "stanford.tsv"};

		// Constructs distinctive ngrams and their conditional probabilities
		args = new String[] {"-rn", commonDataPath+"all.tsv"};

		// Constructs distinctive postags and their conditional probabilities
		args = new String[] { "-rp", commonDataPath+"all.tsv" };

		// Constructs weka arff files of training and test data with for given
		// distinctive ngrams list
		//args = new String[] { "-w", commonDataPath+"stanford_training_0_2.tsv", distinctiveNgramsListFile,"training"};
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

					HashMap<String, Double> posProbs = POSExtractor
							.readPOSProbsFromFile(posProbsFile);

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

					//MultinomialNaiveBayesClassifier.classifyTweets(testTweets, tweetsClassBigramProbs, tweetsClassPosProbs, 0.5);

					//alpha represents PosTag probs threshold and beta represents threshold for the ngram cond. probability difference
					//MultinomialNaiveBayesClassifier.classifyTweetsSeparately(testTweets, tweetsClassBigramProbs, posProbs, 0.5, 0.1);
					
					double start=0.0;
					double optimumLambda=start;
					double increment = 0.05;
					double limit = 1;
					ArrayList<Double[]> results1 = new ArrayList<Double[]>();
					while(optimumLambda <= limit)
					{
						results1.add(new Double[]{MultinomialNaiveBayesClassifier.classifyTweets(testTweets, tweetsClassBigramProbs, tweetsClassPosProbs, optimumLambda),optimumLambda});
						optimumLambda+=increment;
					}
					System.out.println("");
					double lambda = start;
					for (Double[] result:results1)
					{
						System.out.println("lambda:"+ String.format("%.5f",result[1]) + ", Success Ratio:"+result[0]*100 + " %");
					}
					
					//alpha represents PosTag probs threshold and beta represents threshold for the ngram cond. probability difference
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
//						results2.add(new Double[]{MultinomialNaiveBayesClassifier.classifyTweetsSeparately(testTweets, tweetsClassBigramProbs, posProbs, optimumAlpha, optimumBeta),optimumAlpha,optimumBeta});
//						if(optimumAlpha <= limitAlpha)
//							optimumAlpha+=incrementAlpha;	
//						if(optimumBeta <= limitBeta)
//							optimumBeta+=incrementBeta;
//					}
//					System.out.println("");
//					double alpha=alphaStart;
//					double beta=betaStart;
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
