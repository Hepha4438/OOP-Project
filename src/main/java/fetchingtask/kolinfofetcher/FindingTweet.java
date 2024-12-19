package fetchingtask.kolinfofetcher;

import com.google.gson.reflect.TypeToken;
import webbrowserwithcookie.WebInteractor;
import object.Tweet;
import fetchingtask.potentialdatafetcher.FilteringXKOL;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import webbrowserwithcookie.WebManager;

import java.time.Duration;
import java.util.*;

import static filemanager.FileManager.*;
import static webbrowserwithcookie.WebInteractor.*;
import static webbrowserwithcookie.WebManager.createDriverWithNewCookies;
import static webbrowserwithcookie.WebManager.driver;

public class FindingTweet implements FindingPost {
	public static final String outputFilePath = FindingTweet.class.getSimpleName() + ".json";
	public static int tweet_count = 0;
	public static int retweet_count = 0;
	private static int error_count = 0;
	private static int checkpoint = 0;
	private WebManager manager;
	private WebInteractor helper;
	private int maxPostPerKOL;
	private int maxCommentPerTweet;
	private int maxRepostPerTweet;
	private int unchange = 0;
	private final String fileCheckpointName = "Checkpoint_for_" + this.getClass().getSimpleName() + ".json";
	private final String fileErrorName = "ErrorKOL.json";
	
	public FindingTweet() {
		manager = new WebManager(driver, Duration.ofSeconds(30));
		helper = new WebInteractor(Duration.ofSeconds(30));
	}

	@Override
	public void setupFindingPost(int maxPostPerKOL, int maxCommentPerTweet, int maxRepostPerTweet) {
		this.maxPostPerKOL = maxPostPerKOL;
		this.maxCommentPerTweet = maxCommentPerTweet;
		this.maxRepostPerTweet = maxRepostPerTweet;
	}

	@Override
	public String getOutputFilePath() {
		return outputFilePath;
	}

	@Override
	public void saveCheckpoint() {
		List<Integer> state = Arrays.asList(checkpoint, tweet_count, retweet_count, manager.getChannel());
		saveFile(state, fileCheckpointName);
		System.out.println("Lưu file " + fileCheckpointName + " thành công.");
	}

	@Override
	public void loadCheckpoint() {
		List<Integer> state = loadFile(fileCheckpointName, new TypeToken<List<Integer>>() {
		}.getType());
		if (state == null) {
			return;
		}
		checkpoint = state.get(0);
		tweet_count = state.get(1);
		retweet_count = state.get(2);
		manager.setChannel(state.get(3));
		System.out.println("Load file " + fileCheckpointName + " thành công.");
	}

	public boolean checkRetweet(String tweetLink, String KOLLink) {
		String link = Tweet.getOwnerLink(tweetLink); // Lấy link chủ sở hữu bài đăng (owner)
		return !link.equals(KOLLink);
	}

	@Override
	public Set<String> findingPost(String KOLLink) {
		driver.get(KOLLink);
		waitForPageToLoad(null);
		try {
			helper.setWait(Duration.ofSeconds(20));
			helper.waitForElement(By.cssSelector("[role='article']"));
			error_count = 0;
		} catch (Exception e) {
			error_count++;
			if (error_count == 3) {
				error_count = 0;
				System.out.println("Không tìm được bài post của: " + KOLLink);
				saveErrorKOL(KOLLink);
				return Collections.EMPTY_SET;
			} else {
				createDriverWithNewCookies();
				helper.setWait(Duration.ofSeconds(6));
				return findingPost(KOLLink);
			}
		}
		Set<String> tweetLinks = new HashSet<>();
		int preHashSize = 0;
		while (true) {
			// Tìm các element là tweet
			helper.fetcher(tweetLinks, maxPostPerKOL, By.cssSelector("[role='article']"),
					By.xpath(".//a[contains(@href,'/status/')]"));
			if (preHashSize == tweetLinks.size()) {
				unchange++;
			} else {
				preHashSize = tweetLinks.size();
				unchange = 0;
			}
			// Break if no new links are added
			if (unchange >= 3) {
				unchange = 0;
				System.out.println("Không có tweet mới được tìm thấy. Dừng tìm kiếm.");
				break;
			}
		}
		return tweetLinks;
	}

	public void saveErrorKOL(String KOLLink) {
		System.out.println("Không tìm được bài post của: " + KOLLink);
		List<String> errorLinks = loadFile(fileErrorName, new TypeToken<List<String>>() {
		}.getType());
		if (errorLinks == null) {
			errorLinks = new ArrayList<>();
		}
		errorLinks.add(KOLLink);
		saveFile(errorLinks, fileErrorName);
	}

	@Override
	public Set<String> findingCommenter(String tweetLink, String KOLLink) {
		driver.get(tweetLink);
		waitForPageToLoad(null);
		try {
			// Đợi phần tử article có tabindex = -1 xuất hiện
			WebElement test = helper.waitForElement(By.cssSelector("[role='article'][tabindex='-1']"));
			// Cuộn xuống đúng bằng chiều cao của phần tử test
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(false);", test);
		} catch (Exception ignored) {
		}

		try {
			// Đợi phần tử article có tabindex = 0 xuất hiện
			helper.waitForElement(By.cssSelector("[role='article'][tabindex='0']"));
			System.out.println("Đã load được comment");
		} catch (Exception ignored) {
		}

		Set<String> commenterLinks = new HashSet<>();
		int preHashSize = 0;
		while (true) {
			// Tìm các element là commenter
			helper.fetcher(commenterLinks, maxCommentPerTweet, By.cssSelector("[role='article']"),
					By.cssSelector("a[href*='/']"));

			if (preHashSize == commenterLinks.size()) {
				unchange++;
			} else {
				preHashSize = commenterLinks.size();
				unchange = 0;
			}

			// Break nếu không có comment nào được tìm thấy nữa
			if (unchange >= 3) {
				unchange = 0;
				System.out.println("Không có commenter mới được tìm thấy. Dừng tìm kiếm.");
				break;
			}
		}
		commenterLinks.remove(KOLLink); // Xóa phần tử 'KOLLink'
		commenterLinks.remove("https://help.twitter.com/rules-and-policies/notices-on-twitter");
		System.out.println("Đã thu được: " + commenterLinks.size() + " commenter");
		return commenterLinks;
	}

	@Override
	public Set<String> findingReposter(String tweetLink, String KOLLink) {
		String repostURL = tweetLink + "/retweets";
		driver.get(repostURL);
		waitForPageToLoad(null);
		Set<String> reposterLinks = new HashSet<>();
		int preHashSize = 0;
		while (true) {
			// Tìm các element là reposter
			helper.fetcher(reposterLinks, maxRepostPerTweet, By.cssSelector("[data-testid='UserCell'][role='button']"),
					By.tagName("a"));

			if (preHashSize == reposterLinks.size()) {
				unchange++;
			} else {
				preHashSize = reposterLinks.size();
				unchange = 0;
			}

			// Break nếu không tìm được reposter mới
			if (unchange >= 3) {
				unchange = 0;
				System.out.println("Không có reposter mới được tìm thấy. Dừng tìm kiếm.");
				break;
			}
		}
		reposterLinks.remove(KOLLink); // Xóa phần tử 'KOLLink'
		reposterLinks.remove("https://help.twitter.com/rules-and-policies/notices-on-twitter");
		System.out.println("Đã thu được: " + reposterLinks.size() + " reposter");
		return reposterLinks;
	}

	@Override
	public void runTask() {
		loadCheckpoint();
		createDriverWithNewCookies();
		helper.setWait(Duration.ofSeconds(6));
		List<String> savedKOLList = new ArrayList<>();
		Map<String, Integer> savedKOLMap = loadFile(FilteringXKOL.outputFilePath,
				new TypeToken<LinkedHashMap<String, Integer>>() {
				}.getType());
		if (savedKOLMap != null) {
			savedKOLList = new ArrayList<>(savedKOLMap.keySet());
		}
		int target = savedKOLList.size();

		if (checkpoint >= target) {
			driver.quit();
			return;
		}

		List<String> uncheckedKOLList = savedKOLList.subList(checkpoint, target);
		Iterator<String> iterator = uncheckedKOLList.iterator();
		int linkCounter = checkpoint;

		// Duyệt từng KOLLink và các tweet, các comment trong từng tweet của từng
		// KOLLink
		while (iterator.hasNext()) {
			linkCounter++; // Biến đếm số KOLLink đã duyệt
			String KOLLink = iterator.next();
			System.out.println("Xử lý KOL thứ " + linkCounter + ": " + KOLLink);
			Set<String> tweetLinks = findingPost(KOLLink);
			System.out.println("Đã thu được " + tweetLinks.size() + " tweet");

			// Duyệt các comment và repost của từng tweet
			if (!tweetLinks.isEmpty()) {
				// Tạo danh sách để lưu các bài tweet
				List<Tweet> tweetsOFKOL = new ArrayList<>();

				for (String tweetLink : tweetLinks) {
					System.out.println("Duyệt bài tweet:" + tweetLink);
					Tweet tweetOfKOL = new Tweet();
					String link = Tweet.getOwnerLink(tweetLink); // Lấy link chủ sở hữu bài đăng (owner)
					tweetOfKOL.setOwner(link);
					if (checkRetweet(tweetLink, KOLLink)) {
						tweetOfKOL.addAdjPerson(KOLLink);
						retweet_count++;
					} else {
						tweet_count++;
						tweetOfKOL.addAdjPerson(findingCommenter(tweetLink, KOLLink)); // Lấy các bình luận bài tweet
						tweetOfKOL.addAdjPerson(findingReposter(tweetLink, KOLLink)); // Lấy các bài đăng lại bài tweet
					}
					tweetOfKOL.setLink(tweetLink);
					tweetsOFKOL.add(tweetOfKOL); // Lưu bài tweet vào danh sách
				}
				// Sau khi duyệt hết tất cả tweetLink của 1 KOL, lưu các bài tweet
				for (Tweet checkedTweet : tweetsOFKOL) {
					saveObject(checkedTweet, outputFilePath, new TypeToken<LinkedHashSet<Tweet>>() {
					}.getType());
				}
			}
			checkpoint = linkCounter;
			saveCheckpoint();
			System.out.println("Đã xử lý xong KOL thứ " + checkpoint);

			// Đổi account mỗi khi duyệt được thêm 5 link
			if (linkCounter % 4 == 0 && linkCounter < target) {
				System.out.println("Đã xử lý " + linkCounter + " KOLLink. Đang khởi tạo lại driver...");
				createDriverWithNewCookies();
				helper.setWait(Duration.ofSeconds(6));
			}
		}
		if (driver != null) {
			driver.quit();
		}
	}

	@Override
	public void deleteCheckpoint() {
		deleteFile(fileCheckpointName);
	}

}