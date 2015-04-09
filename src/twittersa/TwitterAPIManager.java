package twittersa;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twittersa.Tweet.ClassLabel;

public class TwitterAPIManager {
	public static void main(String[] args) {
		args = new String[] { "AJEnglish" };

		getTweets(args[0]);
	}

	public static ArrayList<String> getTweets(String userAccount) {

		ArrayList<String> tweetList = new ArrayList<String>();
		ArrayList<Tweet> neutralTweets = new ArrayList<Tweet>();
		try {
			String neutralTweetDatasetPath = FileHandler
					.readConfigValue(Constants.DATA_PATH_CONFIG)
					+ File.separator + "neutral_tweets.tsv";
			File neutralTweetDatasetFile = new File(neutralTweetDatasetPath);
			Twitter unauthenticatedTwitter = new TwitterFactory().getInstance();
			// First param of Paging() is the page number, second is the number
			// per page (this is capped around 200 I think.
			int pageNumber = 1;
			int numberOfTweetsPerPage = 200;
			for (int i = 0; i < 75; i++) {
				
				Paging paging = new Paging(pageNumber, numberOfTweetsPerPage);
				List<Status> statuses = unauthenticatedTwitter.getUserTimeline(
						userAccount, paging);
				for (Status tweet : statuses) {
					Tweet neutralTweet = new Tweet(ClassLabel.Neutral,
							tweet.getId(), tweet.getCreatedAt(), userAccount,
							tweet.getUser().getName(), tweet.getText().replace("\n", ""));
					neutralTweets.add(neutralTweet);
					FileUtils.write(
							neutralTweetDatasetFile,
							neutralTweet.getClassInfo().ordinal()*2
									+ Constants.SEPERATOR_CHAR
									+ neutralTweet.getId()
									+ Constants.SEPERATOR_CHAR
									+ neutralTweet.getDate()
									+ Constants.SEPERATOR_CHAR
									+ neutralTweet.getQuery()
									+ Constants.SEPERATOR_CHAR
									+ neutralTweet.getAuthorNickName()
									+ Constants.SEPERATOR_CHAR
									+ neutralTweet.getOriginalContent()+"\n", true);
					tweetList.add(tweet.getText());
					
				}
				pageNumber++;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return tweetList;
	}
}