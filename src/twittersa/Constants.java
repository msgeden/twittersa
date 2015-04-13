package twittersa;

/**
 * 
 */

/**
 * @author Munir Geden
 *
 */
public class Constants {
	public static final String NGRAM_SIZE_CONFIG = "ngramSize";
	public static final String TOP_RANKED_SIZE_CONFIG = "listSize";
	public static final String NUMBER_OF_DATA_INPUT_CONFIG = "numberOfInput";
	public static final String REPORTS_PATH_CONFIG = "reportsPath";
	public static final String DATA_PATH_CONFIG = "dataPath";
	public static final String TRAINING_CORPUS_PATH_CONFIG = "trainCorpus";
	public static final String TEST_CORPUS_PATH_CONFIG = "testCorpus";
	public static final String TREE_TAGGER_PATH_CONFIG = "treeTaggerPath";
	public static final String KNN_CONFIG = "kValue";
	public static final String POS_THRESHOLD_CONFIG = "posThreshold";
	public static final String IG_THRESHOLD_CONFIG = "igThreshold";
	public static final String ENTROPY_THRESHOLD_CONFIG = "entropyThreshold";
	public static final String SALIENCE_THRESHOLD_CONFIG = "salienceThreshold";
	public static final String SUBJECTIVITY_THRESHOLD_CONFIG = "subjectivityThreshold";
	
	public static final int NGRAM_SIZE = Integer.parseInt(FileHandler.readConfigValue(NGRAM_SIZE_CONFIG,"1"));
	public static final int TOP_RANKED_SIZE = Integer.parseInt(FileHandler.readConfigValue(TOP_RANKED_SIZE_CONFIG,"5000"));
	public static final int NUMBER_OF_DATA_INPUT =Integer.parseInt(FileHandler.readConfigValue(NUMBER_OF_DATA_INPUT_CONFIG,"1000"));
	public static final String REPORTS_PATH=FileHandler.readConfigValue(REPORTS_PATH_CONFIG);
	public static final String DATA_PATH = FileHandler.readConfigValue(DATA_PATH_CONFIG);
	public static final String TRAINING_CORPUS_PATH = FileHandler.readConfigValue(TRAINING_CORPUS_PATH_CONFIG);
	public static final String TEST_CORPUS_PATH = FileHandler.readConfigValue(TEST_CORPUS_PATH_CONFIG);
	public static final String TREE_TAGGER_PATH = FileHandler.readConfigValue(TREE_TAGGER_PATH_CONFIG);
	public static final int KNN = Integer.parseInt(FileHandler.readConfigValue(KNN_CONFIG,"3"));
	public static final double POS_THRESHOLD = Double.parseDouble(FileHandler.readConfigValue(POS_THRESHOLD_CONFIG,"0.0"));
	public static final double IG_THRESHOLD = Double.parseDouble(FileHandler.readConfigValue(IG_THRESHOLD_CONFIG,"0.997"));
	public static final double ENTROPY_THRESHOLD = Double.parseDouble(FileHandler.readConfigValue(ENTROPY_THRESHOLD_CONFIG,"0.8"));
	public static final double SALIENCE_THRESHOLD = Double.parseDouble(FileHandler.readConfigValue(SALIENCE_THRESHOLD_CONFIG,"0.8"));
	public static final double SUBJECTIVITY_THRESHOLD = Double.parseDouble(FileHandler.readConfigValue(SUBJECTIVITY_THRESHOLD_CONFIG,"0.8"));
	
	public static final String UNDERSCORE = "_";
	public static final String SEPERATOR_CHAR = "\t";
	public static final String TEST_LABEL = "Test";
	public static final String TRAIN_LABEL = "Train";
	public static final String OBJECTIVE_CLASS_LABEL = "Objective";
	public static final String SUBJECTIVE_CLASS_LABEL = "Subjective";
	public static final String POSITIVE_CLASS_LABEL = "Positive";
	public static final String NEGATIVE_CLASS_LABEL = "Negative";
	public static final String COUNT_OF_NGRAMS_PER_CLASS = "CLASSNGRAMS";
	public static final String COUNT_OF_POSTAGS_PER_CLASS = "CLASSPOSTAGS";
	public static final String COUNT_OF_TWEETS_PER_CLASS = "CLASSTWEETS";
	public static final String PRIOR_PER_CLASS = "CLASSPRIORS";
}
