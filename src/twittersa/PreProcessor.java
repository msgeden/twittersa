package twittersa;
/**
 * 
 */

/**
 * @author Munir Geden
 *
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cmu.arktweetnlp.Twokenize;
public class PreProcessor {

	private static String[] negations = { "are", "is", "was", "were", "do",
			"does", "did", "have", "has", "will", "can", "shall", "may",
			"would", "could", "should", "might" };
	private static String[] stopWords = { "a","an","and", "as", "at",
			"be", "by", "from", "has", "he","in","its","of","on","that","the","to","with"};
	
	private static String unnecessaryChars = "['\"“”‘’.?!…,:;()&*=+_-[(][)][<][>][{]\\}|\\\\/\\[\\]]";
	private static String unicode = "[^\\x00-\\x7F]"; 
	private static String repetitiveChars =  "(.)\\1{2,}";
	public static ArrayList<Tweet> processTweets(String filePath) {
		ArrayList<Tweet> tweets = FileHandler.readTweets(filePath);
		for (Tweet tweet : tweets) {
			ArrayList<String> tokens = tokenize(tweet.getOriginalContent().toLowerCase());
			ArrayList<String> filteredTokens = filterUrlsMentionsHashTags(tokens);
			ArrayList<String> emoticonRemovedTokens = removeEmoticons(filteredTokens);
			ArrayList<String> puncRemovedTokens = removePunctuations(emoticonRemovedTokens);
			ArrayList<String> invalidCharsRemovedTokens = removeInvalidChars(puncRemovedTokens);
			ArrayList<String> stopWordsRemovedTokens = removeStopWords(invalidCharsRemovedTokens);
			ArrayList<String> negationCombinedTokens = combineNegations(stopWordsRemovedTokens);
			tweet.setTokens(negationCombinedTokens);
			tweet.setProcessedContent(); 
		}
		System.out.println("Processing steps are completed.");
		return tweets;
	}
	
	
	public static ArrayList<String> tokenize(String tweet) {
		ArrayList<String> correctedTokens = new ArrayList<String>();
		List<String> tokens = Twokenize.tokenizeRawTweetText(tweet);
		for (String token:tokens)
		{
			if (token != null && !token.equals(""))
			{
				String correctedToken = token.replaceAll(repetitiveChars, "$1");
				correctedTokens.add(correctedToken);
			}
		}
		return correctedTokens;
	}

	public static ArrayList<String> filterUrlsMentionsHashTags(ArrayList<String> tokens) {
		ArrayList<String> filteredTokens = new ArrayList<String>();
		for (String token : tokens) {
			if (token.startsWith("#") 
					|| token.startsWith("@")
					|| token.startsWith("http://")
					|| token.startsWith("https://")
					|| token.startsWith("www.")
					|| token.startsWith("ftp://")
					|| token.startsWith("rt"))
				continue;
			else
				filteredTokens.add(token);
		}
		return filteredTokens;
	}
	
	public static ArrayList<String> removeEmoticons(ArrayList<String> tokens) {
		ArrayList<String> filteredTokens = new ArrayList<String>();
		Pattern postingPattern = Pattern.compile(Twokenize.emoticon);
		for (String token : tokens) {
			Matcher m = postingPattern.matcher(token);
			if (m.find())
				continue;
			else
				filteredTokens.add(token);
		}
		return filteredTokens;
	}

	public static ArrayList<String> removePunctuations(ArrayList<String> tokens) {
		ArrayList<String> filteredTokens = new ArrayList<String>();
		Pattern postingPatternPunc = Pattern.compile(unnecessaryChars);
		Pattern postingPatternEmoticons = Pattern.compile(Twokenize.emoticon);
		for (String token : tokens) {
			Matcher mEmoticons = postingPatternEmoticons.matcher(token);
			Matcher mPuncs = postingPatternPunc.matcher(token);
			if (mEmoticons.find())
				filteredTokens.add(token);
			else if (mPuncs.find())
				continue;
			else
				filteredTokens.add(token);
		}
		return filteredTokens;
	}
	
	public static ArrayList<String> removeInvalidChars(ArrayList<String> tokens) {
		ArrayList<String> filteredTokens = new ArrayList<String>();
		Pattern postingPattern = Pattern.compile(unicode,Pattern.UNICODE_CASE | Pattern.CANON_EQ
                | Pattern.CASE_INSENSITIVE);
		for (String token : tokens) {
			Matcher m = postingPattern.matcher(token);
			token = m.replaceAll("");
			if (!token.isEmpty())
				filteredTokens.add(token);
		}
		return filteredTokens;
	}
	

	public static ArrayList<String> removeStopWords(ArrayList<String> tokens) {
		ArrayList<String> filteredTokens = new ArrayList<String>();
		for (String token : tokens) {
			if (!Arrays.asList(stopWords).contains(token))
				filteredTokens.add(token);
		}
		return filteredTokens;
	}
	
	public static ArrayList<String> combineNegations(ArrayList<String> tokens) {
		List<String> combinedTokens = new ArrayList<String>();
		//HashSet<String> combinedTokensSet = new HashSet<>(); 
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).toLowerCase().equals("not")) {
				if (i != 0 && Arrays.asList(negations).contains(tokens.get(i - 1).toLowerCase())) {
					combinedTokens.add(tokens.get(i - 1) + "+" + tokens.get(i));
					//Remove preceeding element;
					combinedTokens.remove(combinedTokens.size()-2);
				} 
				if ((i+1)<tokens.size())
				{
					combinedTokens.add(tokens.get(i) + "+" + tokens.get(i+1));
					i++;
				}
				else
					combinedTokens.add(tokens.get(i));
			} else if (tokens.get(i).toLowerCase().equals("no")) {
				if ((i + 1) < tokens.size()) {
					combinedTokens.add(tokens.get(i) + "+" + tokens.get(i+1));
					i++;
				} else
					combinedTokens.add(tokens.get(i));

			} else
				combinedTokens.add(tokens.get(i));
		}
		Set<String> s = new LinkedHashSet<String>(combinedTokens);
		ArrayList<String> combinedTokensSet = new ArrayList<String>(s);
		return combinedTokensSet;
	}
}
