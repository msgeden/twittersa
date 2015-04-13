package twittersa;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerWrapper;
import org.apache.commons.io.FileUtils;

import twittersa.Tweet.ClassLabel;

public class POSExtractor {

	
	public static HashMap<String, Double[]> calculateCondProbsOfPosTags(
			ArrayList<Tweet> tweets) {
			// TreeTagger Library folder
			System.setProperty("treetagger.home",
					Constants.TREE_TAGGER_PATH);
			TreeTaggerWrapper<String> tt = new TreeTaggerWrapper<String>();
			final HashMap<String, Double[]> posTagsWithCondProbs = new HashMap<String, Double[]>();
			final HashMap<String, Integer[]> posTagsFrequencies = new HashMap<String, Integer[]>();
			final Integer[] tweetCountPerClass = new Integer[]{0,0,0};
			final Integer[] tokenCountPerClass = new Integer[]{0,0,0};
			final Integer[] posTagsCountPerClass = new Integer[]{0,0,0};
			final ClassLabel[] tweetClass = {ClassLabel.Neutral};
			//final int tweetClass = ClassLabel.Neutral.ordinal();
			//double posThreshold = Double.parseDouble(FileHandler
			//		.readConfigValue(Constants.POS_THRESHOLD_CONFIG));
			try {
				tt.setModel("english-utf8.par:iso8859-1");
				tt.setHandler(new TokenHandler<String>() {
					public void token(String token, String pos, String lemma) {
						if (posTagsFrequencies.containsKey(pos))
						{
							Integer[] frequencies = posTagsFrequencies.get(pos);
							frequencies[tweetClass[0].ordinal()]++;
							posTagsFrequencies.put(pos, frequencies);
							posTagsCountPerClass[tweetClass[0].ordinal()]++;
						}
						else
						{
							Integer[] frequencies = new Integer[]{0,0,0};
							frequencies[tweetClass[0].ordinal()]++;
							posTagsFrequencies.put(pos, frequencies);
							posTagsCountPerClass[tweetClass[0].ordinal()]++;
						}
					}
				});
				try {
					Tweet.ClassLabel classInfo;
					// Traverse tweets to be analyzed
					for (Tweet tweet : tweets) {
						classInfo = tweet.getClassInfo();
						tweetCountPerClass[classInfo.ordinal()]++;
						tweetClass[0] = classInfo;
						List<String> tokens = tweet.getTokens();
						tokenCountPerClass[classInfo.ordinal()]+=tokens.size();
						tt.process(tokens);
					}
					
				} finally {
					tt.destroy();
				}
				;

			} catch (Exception e) {
				e.printStackTrace();
			}
			int numberOfClass = ClassLabel.values().length;
			//Calculate prior probability of classes
			int totalCountOfTweets = 0;
			for (int i=0;i<tweetCountPerClass.length;i++)
				totalCountOfTweets+=tweetCountPerClass[i];
			Double[] priors = new Double[]{0.0,0.0,0.0};
			for (int i=0;i<tweetCountPerClass.length;i++)
				priors[i]=(double)tweetCountPerClass[i]/(double)totalCountOfTweets;
			
			//posTagsWithCondProbs.put(Constants.COUNT_OF_POSTAGS_PER_CLASS, posTagsCountPerClass);
			posTagsWithCondProbs.put(Constants.PRIOR_PER_CLASS, priors);
			
			int vocabularySize = posTagsFrequencies.size();
			
			// Iterate ngram hashmap to calculate conditional probabilities for
			// each ngram
			for (Map.Entry<String, Integer[]> entry : posTagsFrequencies.entrySet()) {

				Double[] condProbs = new Double[numberOfClass];
				Integer[] values = entry.getValue();
				for (int i = 0; i < numberOfClass; i++)
					condProbs[i] = (((double)values[i] + 1.0) / ((double)posTagsCountPerClass[i] + (double)vocabularySize));

				posTagsWithCondProbs.put(entry.getKey(), condProbs);
			}

			writeCondProbsOfPosTagsToFile(posTagsWithCondProbs);
			return posTagsWithCondProbs;
		}

	public static HashMap<String, Double> calculatePOSProbsForPolarity(
			ArrayList<Tweet> tweets) {
			// TreeTagger Library folder
			System.setProperty("treetagger.home",
					Constants.TREE_TAGGER_PATH);
			TreeTaggerWrapper<String> tt = new TreeTaggerWrapper<String>();
			final HashMap<String, Double> posTweets = new HashMap<String, Double>();
			final HashMap<String, Double> negTweets = new HashMap<String, Double>();
			final HashMap<String, Double> uniquePOS = new HashMap<String, Double>();
			final Integer[] tweetClass = { 0 };// 0:Negative, 1:Positive
			double posThreshold = Constants.POS_THRESHOLD;
			try {
				tt.setModel("english-utf8.par:iso8859-1");
				tt.setHandler(new TokenHandler<String>() {
					public void token(String token, String pos, String lemma) {
						double freq;
						if (tweetClass[0] == 0) {
							if (negTweets.containsKey(pos)) {
								freq = negTweets.get(pos) + 1;
								negTweets.put(pos, freq);
							} else
								negTweets.put(pos, 1.0);
						} else {
							if (posTweets.containsKey(pos)) {
								freq = posTweets.get(pos) + 1;
								posTweets.put(pos, freq);
							} else
								posTweets.put(pos, 1.0);
						}
					}
				});
				try {
					int[] tweetCountOfClasses = new int[] { 0, 0 };
					Tweet.ClassLabel classInfo;
					// Traverse tweets to be analyzed
					for (Tweet tweet : tweets) {
						classInfo = tweet.getClassInfo();
						if (classInfo == Tweet.ClassLabel.Positive) {
							tweetCountOfClasses[1]++;
							tweetClass[0] = 1;
						} else {
							tweetCountOfClasses[0]++;
							tweetClass[0] = 0;
						}
						tt.process(tweet.getTokens());

					}

				} finally {
					tt.destroy();
				}
				;

			} catch (Exception e) {
				e.printStackTrace();
			}
			normalize(posTweets, sum(posTweets));
			normalize(negTweets, sum(negTweets));
			double posValue = 0.0;
			double negValue = 0.0;
			for (String key : posTweets.keySet()) {
				posValue = posTweets.get(key);
				negValue = negTweets.containsKey(key) ? negTweets.get(key) : 0.0;
				//
				double ratio = (negValue - posValue) / (posValue + negValue);
				if (Math.abs(ratio) >= posThreshold) {
					uniquePOS.put(key, ratio);
				}
			}
			
			PosTagProb[] rankedPosTags = new PosTagProb[uniquePOS.size()];
			int index=0;
			for (Map.Entry<String, Double> entry : uniquePOS.entrySet()) {
	 			rankedPosTags[index++]= new PosTagProb(entry.getKey(), entry.getValue());
			}
			Arrays.sort(rankedPosTags);
			writePOSProbsToFile(rankedPosTags,true);
			return uniquePOS;
	}
	public static HashMap<String, Double> calculatePOSProbsForSubjectivity(
			ArrayList<Tweet> tweets) {
		// TreeTagger Library folder
		System.setProperty("treetagger.home",
				Constants.TREE_TAGGER_PATH);
		TreeTaggerWrapper<String> tt = new TreeTaggerWrapper<String>();
		final HashMap<String, Double> objTweets = new HashMap<String, Double>();
		final HashMap<String, Double> subTweets = new HashMap<String, Double>();
		final HashMap<String, Double> uniquePOS = new HashMap<String, Double>();
		final Integer[] tweetClass = { 0 };// 0:Subjective, 1:Objective
		double posThreshold = Constants.POS_THRESHOLD;
		try {
			tt.setModel("english-utf8.par:iso8859-1");
			tt.setHandler(new TokenHandler<String>() {
				public void token(String token, String pos, String lemma) {
					double freq;
					if (tweetClass[0] == 0) {
						if (subTweets.containsKey(pos)) {
							freq = subTweets.get(pos) + 1;
							subTweets.put(pos, freq);
						} else
							subTweets.put(pos, 1.0);
					} else {
						if (objTweets.containsKey(pos)) {
							freq = objTweets.get(pos) + 1;
							objTweets.put(pos, freq);
						} else
							objTweets.put(pos, 1.0);
					}
				}
			});
			try {
				int[] tweetCountOfClasses = new int[] { 0, 0 };
				Tweet.ClassLabel classInfo;
				// Traverse tweets to be analyzed
				for (Tweet tweet : tweets) {
					classInfo = tweet.getClassInfo();
					if (classInfo == Tweet.ClassLabel.Neutral) {
						tweetCountOfClasses[1]++;
						tweetClass[0] = 1;
					} else {
						tweetCountOfClasses[0]++;
						tweetClass[0] = 0;
					}
					tt.process(tweet.getTokens());

				}

			} finally {
				tt.destroy();
			}
			;

		} catch (Exception e) {
			e.printStackTrace();
		}
		normalize(objTweets, sum(objTweets));
		normalize(subTweets, sum(subTweets));
		double objValue = 0.0;
		double subValue = 0.0;
		for (String key : objTweets.keySet()) {
			objValue = objTweets.get(key);
			subValue = subTweets.containsKey(key) ? subTweets.get(key) : 0.0;
			//
			double ratio = (objValue - subValue) / (objValue + subValue);
			if (Math.abs(ratio) >= posThreshold) {
				uniquePOS.put(key, ratio);
			}
			// Negative values represents Objective class
			// Positive values represents Subjective class
		}
		
		PosTagProb[] rankedPosTags = new PosTagProb[uniquePOS.size()];
		int index=0;
		for (Map.Entry<String, Double> entry : uniquePOS.entrySet()) {
 			rankedPosTags[index++]= new PosTagProb(entry.getKey(), entry.getValue());
		}
		Arrays.sort(rankedPosTags);
		writePOSProbsToFile(rankedPosTags,false);
		return uniquePOS;
	}
	public static HashMap<String, Double[]> readCondProbsOfPosTagsFromFile(
			String filePath) {
		
		File posTagsCondProbFile = new File(filePath);
		// This will keep the conditional probabilities of each ngram for each
		// class: P(g|S_i)
		HashMap<String, Double[]> posTagsWithCondProbs = new HashMap<String, Double[]>();

		try {
			List<String> lines = FileUtils.readLines(posTagsCondProbFile);
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
					posTagsWithCondProbs.put(tokens[0], probs);
				}		
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return posTagsWithCondProbs;
	}
	
	public static HashMap<String, Double> readPOSProbsFromFile(String filePath) {

		File posProbFile = new File(filePath);
		// This will keep the probabilities of each postag for each
		HashMap<String, Double> distinctivePosWithProbs = new HashMap<String, Double>();

		try {
			List<String> lines = FileUtils.readLines(posProbFile);
			int lineNumber = 0;
			for (String line : lines) {
				lineNumber++;
				// Skip header line
				if (line != null && !line.equals("") && lineNumber != 1) {
					String[] tokens = line.split(Constants.SEPERATOR_CHAR);
					Double score = Double.valueOf(tokens[1]);
					distinctivePosWithProbs.put(tokens[0], score);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return distinctivePosWithProbs;
	}

	public static double sum(HashMap<String, Double> map) {
		if (map == null)
			return 0;
		double mapSum = 0;
		for (String key : map.keySet()) {
			mapSum += map.get(key);
		}
		return mapSum;
	}

	public static void normalize(HashMap<String, Double> map, double total) {
		if (map.isEmpty())
			return;
		double temp;
		for (String key : map.keySet()) {
			temp = map.get(key);
			map.put(key, temp / total);
		}
	}

	public static void writeCondProbsOfPosTagsToFile(HashMap<String,Double[]> posTagsWithCondProbs) {
		String posTagsCondProbPath = Constants.DATA_PATH
				+ "conditional_probabilities_of_postags.tsv";;
		File posTagsCondProbFile = new File(posTagsCondProbPath);
		// Try to delete if it exists without exception
		FileUtils.deleteQuietly(posTagsCondProbFile);
		int numberOfClass = ClassLabel.values().length;
		try {
			FileUtils.write(posTagsCondProbFile,  "POSTAG" + Constants.SEPERATOR_CHAR 
					+ "CONDITIONAL PROBABILITES", true);
			
			System.out.print("COUNT" + Constants.SEPERATOR_CHAR 
					+ "P(POSTAG|CLASS)" + Constants.SEPERATOR_CHAR 
					+ "CONDITIONAL PROBABILITES");

			int count = 0;

			for (Map.Entry<String, Double[]> entry : posTagsWithCondProbs
					.entrySet()) {
				FileUtils.write(
						posTagsCondProbFile,
						"\n" + entry.getKey(), 
						true);
			
				for (int i = 0; i < numberOfClass; i++) {
					FileUtils.write(
							posTagsCondProbFile,
							Constants.SEPERATOR_CHAR + entry.getValue()[i], true);
					System.out
							.print("\n" + (++count) + Constants.SEPERATOR_CHAR
									+ "P(" + entry.getKey() + "|"
									+ Tweet.ClassLabel.values()[i].name()
									+ ")" + Constants.SEPERATOR_CHAR
									+ entry.getValue()[i]);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public static void writePOSProbsToFile(PosTagProb[] rankedPosTags, boolean isPolarity) {
		
		String fileName=isPolarity?"distinctive_postags_list_for_polarity.tsv":"distinctive_postags_list_for_subjectivity.tsv";
		String rankedPOSPath = Constants.DATA_PATH
				+ fileName;
		File rankedPOSFile = new File(rankedPOSPath);
		// Try to delete if it exists without exception
		FileUtils.deleteQuietly(rankedPOSFile);
		try {
			FileUtils.write(rankedPOSFile,
					"POS" + Constants.SEPERATOR_CHAR + "PROBABILITY", true);
			System.out.print("POS" + Constants.SEPERATOR_CHAR + "PROBABILITY");
			for (int i = 0; i < rankedPosTags.length; i++) {
				System.out.print("\n" 
						+ rankedPosTags[i].getKey()
						+ Constants.SEPERATOR_CHAR
						+ rankedPosTags[i].getValue());
				FileUtils.write(
						rankedPOSFile,
						"\n" + rankedPosTags[i].getKey()
								+ Constants.SEPERATOR_CHAR
								+ rankedPosTags[i].getValue(), true);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static File prepareDataFileHeader(
			HashMap<String, Double> topRankedPOS, boolean isTestData)
			throws IOException {
		// Data class label for reports and input files
		String trainOrTestLabel = isTestData ? Constants.TEST_LABEL
				: Constants.TRAIN_LABEL;

		// Generate data file and prepare it with its headers
		String wekaDataFilePath = Constants.DATA_PATH
				+ trainOrTestLabel
				+ Constants.UNDERSCORE + "pos" + Constants.UNDERSCORE + ".arff";

		File wekaDataFile = new File(wekaDataFilePath);
		FileUtils.deleteQuietly(wekaDataFile);

		FileUtils
				.write(wekaDataFile,
						"%%%\n"
								+ "% This "
								+ trainOrTestLabel.toLowerCase()
								+ " data file consists of \n"
								+ "% the most distictive POS extracted from tweets \n"
								+ "% of known classes to classify unknown tweets\n"
								+ "% by using Weka classifier algorithms. The study is being conducted\n"
								+ "% by Munir Geden, Olawole Oni, Arwa Alamoudi as part of a coursework in UCL.\n"
								+ "%\n" + "@relation 'ngram'\n", true);

		FileUtils.write(wekaDataFile, "@attribute tweetid string\n", true);
		for (Map.Entry<String, Double> entry : topRankedPOS.entrySet()) {
			FileUtils.write(wekaDataFile, "@attribute " + entry.getKey()
					+ " numeric\n", true);
		}

		FileUtils.write(wekaDataFile, "@attribute class {" + "Subjective ,"
				+ "Objective }\n", true);
		FileUtils.write(wekaDataFile, "@data\n", true);
		return wekaDataFile;
	}

	public static void wekaDataConstructorForTweets(
			HashMap<String, Double> topRankedPOS, ArrayList<Tweet> tweets,
			String dataFilePath, boolean isTestData) {
		System.setProperty("treetagger.home",
				Constants.TREE_TAGGER_PATH);
		TreeTaggerWrapper<String> tt = new TreeTaggerWrapper<String>();
		final ArrayList<String> buffer = new ArrayList<String>();
		final int tweetClass[] = { 0 };
		try {
			tt.setModel("english-utf8.par:iso8859-1");
			tt.setHandler(new TokenHandler<String>() {
				public void token(String token, String pos, String lemma) {
					if (!buffer.contains(pos)) {
						buffer.add(pos);
					}
				}
			});
			try {

				// Arff file that will keep traning and test data values
				File dataFile = prepareDataFileHeader(topRankedPOS, isTestData);

				// This will keep the binary values of POS

				for (Tweet tweet : tweets) {
					HashMap<String, Integer> calculatedData = new HashMap<String, Integer>();
					for (Map.Entry<String, Double> entry : topRankedPOS
							.entrySet()) {
						calculatedData.put(entry.getKey(), 0);
					}
					String className;
					Tweet.ClassLabel classInfo = tweet.getClassInfo();
					if (classInfo == Tweet.ClassLabel.Neutral) {
						className = Constants.OBJECTIVE_CLASS_LABEL;
						tweetClass[0] = 1;
					} else {
						className = Constants.SUBJECTIVE_CLASS_LABEL;
						tweetClass[0] = 0;
					}

					String tweetId = Long.toString(tweet.getId());
					tt.process(tweet.getTokens());
					for (String k : buffer) {
						if (topRankedPOS.containsKey(k)) {
							calculatedData.put(k, 1);
						}
					}
					buffer.clear();
					FileUtils.write(dataFile, tweetId + ",", true);
					for (Map.Entry<String, Integer> entry : calculatedData
							.entrySet()) {
						FileUtils.write(dataFile, entry.getValue() + ",", true);
					}
					FileUtils.write(dataFile, className + "\n", true);
				}

			} finally {
				tt.destroy();
			}
			;

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static ArrayList<String> getPosTagsOfTweet(Tweet tweet, TreeTaggerWrapper<String> tt) {
		System.setProperty("treetagger.home",
				Constants.TREE_TAGGER_PATH);
		ArrayList<String> posTags = new ArrayList<String>();
		try {
			tt.setModel("english-utf8.par:iso8859-1");
			tt.setHandler(new TokenHandler<String>() {
				public void token(String token, String pos, String lemma) {
					if (!posTags.contains(pos)) {
						posTags.add(pos);
					}
				}
			});
			try {
				tt.process(tweet.getTokens());
			} finally {
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return posTags;
	}
}
class PosTagProb implements Comparable<PosTagProb> {

	private String key;
	private double value;

	public PosTagProb(String key, double value) {
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
	public int compareTo(PosTagProb item) {
		if (this.value < item.value) {
			return 1;
		} else if (this.value > item.value) {
			return -1;
		} else {
			return 0;
		}
	}
}
