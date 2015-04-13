package twittersa;

/**
 * 
 */

/**
 * @author Munir Geden
 *
 */
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

public class FileHandler {

	//private static final String configFile = "config.properties";
	private static final String configFile = System.getProperty("user.dir")+ File.separator + "config.properties";
	public static String createDirectory(String path, String dir) {
		File directory = new File(path + dir);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		return directory.getAbsolutePath();
	}
	
	public static String readConfigValue(String key) {
		Properties prop = new Properties();
		InputStream input;
		try {
			input = new FileInputStream(configFile);
			prop.load(input);
		} catch (IOException e) {
			System.out.println("Cannot read configuration file(s)\n"
					+ e.getMessage());
		}
		return prop.getProperty(key);
	}
	
	public static String readConfigValue(String key, String defaultValue) {
		Properties prop = new Properties();
		InputStream input;
		String value = defaultValue;
		try {
			input = new FileInputStream(configFile);
			prop.load(input);
			value = prop.getProperty(key);
		} catch (IOException e) {
			System.out.println("Cannot read configuration file(s)\n"
					+ e.getMessage());
		}
		return value;
	}
	
    public static void writeConfigValue(String key, String val) {
        Properties prop = new Properties();
        InputStream input;
        OutputStream output;
        try {
            input = new FileInputStream(configFile);
            prop.load(input);
        } catch (IOException e) {
            System.out.println("Cannot read configuration file(s)\n" + e.getMessage());
        }
        prop.setProperty(key, val);
        try
        {
            output = new FileOutputStream(configFile);
            prop.store(output, "");
        } catch(IOException e) {
            System.out.println("Cannot read configuration file(s)\n" + e.getMessage());
        }
    }

	public static void splitFile(
			String filePath, int ratio) {
		File file = new File(filePath);
		String fileName = file.getName();
		fileName = fileName.replace(".tsv", "_reduced.tsv");
		String directory = file.getAbsoluteFile().getParentFile().getAbsolutePath();
		File reducedFile = new File(directory + File.separator + fileName);
		FileUtils.deleteQuietly(reducedFile);
		try {
			List<String> lines = FileUtils.readLines(file);
			int count = 0;
			for (String line : lines) {
				if ((count % ratio) == 0)
					FileUtils.write(reducedFile, line + "\n", true);
				count++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
	}
	@SuppressWarnings("deprecation")
	public static ArrayList<Tweet> splitTweets(
			String directory, String fileName) {
		ArrayList<Tweet> tweets = new ArrayList<Tweet>();
		File testData = new File(directory + "test.tsv");
		File trainingData = new File(directory + "training.tsv");
		FileUtils.deleteQuietly(testData);
		FileUtils.deleteQuietly(trainingData);
		try {
			File file = new File(directory + fileName);
			List<String> lines = FileUtils.readLines(file);
			int count = 0;
			DateFormat format = new SimpleDateFormat("MM/dd/yy HH:mm");
			for (String line : lines) {
				String[] values = line.split(Constants.SEPERATOR_CHAR);
				if (values.length < 6)
					continue;

				values[0] = values[0];
				values[5] = values[5].replaceAll("\"","");
				Tweet.ClassLabel classInfo = Tweet.ClassLabel
						.values()[Integer.parseInt(values[0]) / 2];
				long id = Long.parseLong(values[1]);
				Date date = null;
				if (values[2].length() < 20)
					date = format.parse(values[2]);
				else
					date = new Date(values[2]);
				String query = values[3];
				String authorNickName = values[4];
				String originalContent = values[5];
				Tweet tweet = new Tweet(classInfo, id, date, query,
						authorNickName, originalContent);
				tweets.add(tweet);
				count++;
				if ((count % 5) == 0)
					FileUtils.write(testData, line + "\n", true);
				else
					FileUtils.write(trainingData, line + "\n", true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tweets;
	}

	@SuppressWarnings("deprecation")
	public static ArrayList<Tweet> readTweets(String filePath) {
		ArrayList<Tweet> tweets = new ArrayList<Tweet>();
		try {
			File file = new File(filePath);
			List<String> lines = FileUtils.readLines(file);
			DateFormat format = new SimpleDateFormat("MM/dd/yy HH:mm");
			for (String line : lines) {
				String[] values = line.split(Constants.SEPERATOR_CHAR);
				if (values.length < 6)
					continue;

				values[0] = values[0];
				values[5] = values[5].replaceAll("\"","");
				Tweet.ClassLabel classInfo = Tweet.ClassLabel
						.values()[Integer.parseInt(values[0]) / 2];
				long id = Long.parseLong(values[1]);
				Date date = new Date();
				try {
				if (values[2].length() < 20)
					date = format.parse(values[2]);
				else
					date = new Date(values[2]);
				}
				catch(Exception e){
					System.out.println(values[1]);
				}
				String query = values[3];
				String authorNickName = values[4];
				String originalContent = values[5];
				Tweet tweet = new Tweet(classInfo, id, date, query,
						authorNickName, originalContent);
				tweets.add(tweet);

			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return tweets;
	}

}
