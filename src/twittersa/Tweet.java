package twittersa;
import java.util.ArrayList;
import java.util.Date;

/**
 * 
 */

/**
 * @author Munir Geden
 *
 */
public class Tweet {
	enum ClassLabel {
		Negative,Neutral,Positive;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getOriginalContent() {
		return originalContent;
	}

	public void setOriginalContent(String originalContent) {
		this.originalContent = originalContent;
	}

	public String getProcessedContent() {
		return processedContent;
	}

	public void setProcessedContent(String processedContent) {
		this.processedContent = processedContent;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getAuthorNickName() {
		return authorNickName;
	}

	public void setAuthorNickName(String authorNickName) {
		this.authorNickName = authorNickName;
	}

	public void setClassInfo(ClassLabel classInfo) {
		this.classInfo = classInfo;
	}
	
	public ClassLabel getClassInfo() {
		return classInfo;
	}
	public ArrayList<String> getTokens() {
		return tokens;
	}

	public void setTokens(ArrayList<String> tokens) {
		this.tokens = tokens;
	}
	
	public void setProcessedContent() {
		StringBuilder sb = new StringBuilder();
		for (String token:tokens)
			sb.append(token + " ");
		processedContent = sb.toString();
	}
	private long id;
	private Date date;
	private String originalContent;
	private String processedContent;
	private String query;
	private String authorNickName;
	private ClassLabel classInfo;
	private ArrayList<String> tokens;
	public Tweet(ClassLabel classInfo, long id, Date date, String query,
			String authorNickName, String originalContent) {
		this.classInfo = classInfo;
		this.id = id;
		this.date = date;
		this.query = query;
		this.authorNickName = authorNickName;
		this.originalContent = originalContent;
	}

}
