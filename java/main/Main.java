package main;

import javafx.application.Application;
import javafx.application.Platform;
import rankingalgorithm.Algorithm;
import rankingalgorithm.XPageRankAlgorithm;
import fetchingtask.kolinfofetcher.FindingPost;
import fetchingtask.kolinfofetcher.FindingXFollowerAndXFollowing;
import fetchingtask.potentialdatafetcher.FilteringKOL;
import fetchingtask.potentialdatafetcher.FilteringXKOL;
import fetchingtask.potentialdatafetcher.InitialKOL;
import fetchingtask.kolinfofetcher.FindingFollowerAndFollowing;
import fetchingtask.kolinfofetcher.FindingTweet;
import fetchingtask.potentialdatafetcher.InitialXKOL;
import webbrowserwithcookie.WebManager;
import java.util.List;
import filemanager.FileManager;

public class Main {
	public static InitialKOL initialKOL;
    public static FilteringKOL filteringKOL;
    public static FindingFollowerAndFollowing findingFollowerAndFollowing;
    public static FindingPost findingPost;
    public static Algorithm algorithm;
    private static Setting setting = new Setting();

    public static void main(String[] args) {
        algorithm = new XPageRankAlgorithm();
        initialKOL = new InitialXKOL();
        filteringKOL = new FilteringXKOL();
        findingFollowerAndFollowing = new FindingXFollowerAndXFollowing();
        findingPost = new FindingTweet();

        Application.launch(MainMenu.class);

        Platform.runLater(() -> {
            try {
                List<String> hashtags = setting.getHashtags();
                int maxFilteredKOL = setting.getMaxKOLSelected();
                int maxTweetPerKOL = setting.getMaxPostRetrievedPerUser();
                int maxCommentPerTweet = setting.getMaxComments();
                int maxReposterPerTweet = setting.getMaxReposter();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Lỗi khi đang chạy chương trình.");
            } finally {
                if (WebManager.driver != null) {
                    WebManager.driver.quit();
                }
                Platform.exit();
            }
        });
    }

    // Thực hiện Task 1
    public static void runTask1(List<String> hashtags, int maxFilteredKOL) {
        System.out.println("Thực hiện task 1 với tham số:");
        System.out.println("Hashtags: " + hashtags);
        System.out.println("Max Filtered KOL: " + maxFilteredKOL);

        setting.setHashtags(hashtags);
        setting.setMaxKOL(maxFilteredKOL);
        setting.saveState();
        setting.saveProgress();

        initialKOL.setHashtags(hashtags);
        initialKOL.runTask();

        filteringKOL.setMaxFilteredKOL(maxFilteredKOL);
        filteringKOL.runTask();

        setting.isTask1Completed = true;
        setting.saveState();
        setting.saveProgress();
        System.out.println("Task 1 hoàn thành.");
        setting.loadProgress();
    }

    // Thực hiện Task 2
    public static void runTask2(int maxPostPerKOL, int maxCommentPerTweet, int maxReposterPerTweet) {
        System.out.println("Thực hiện task 2 với tham số:");
        System.out.println("Max Tweet: " + maxPostPerKOL);
        System.out.println("Max Comment: " + maxCommentPerTweet);
        System.out.println("Max Reposter: " + maxReposterPerTweet);

        setting.loadProgress();
        setting.setMaxPostRetrievedPerUser(maxPostPerKOL);
        setting.setMaxComments(maxCommentPerTweet);
        setting.setMaxReposter(maxReposterPerTweet);
        setting.saveState();
        setting.saveProgress();

        findingFollowerAndFollowing.runTask();

        findingPost.setupFindingPost(maxPostPerKOL, maxCommentPerTweet, maxReposterPerTweet);
        findingPost.runTask();

        algorithm.ranking();

        setting.isTask2Completed = true;
        setting.isTask1Completed = true;
        setting.saveState();
        setting.saveProgress();
        System.out.println("Task 2 completed.");
    }

    public static void storeFile() {
        String storeFolder = "lastrun";

        initialKOL.deleteCheckpoint();
        filteringKOL.deleteCheckpoint();
        findingFollowerAndFollowing.deleteCheckpoint();
        findingPost.deleteCheckpoint();

        FileManager.moveFileToFolder(initialKOL.getOutputFilePath(), storeFolder);
        FileManager.moveFileToFolder(filteringKOL.getOutputFilePath(), storeFolder);
        FileManager.moveFileToFolder(findingFollowerAndFollowing.getOutputFilePath(), storeFolder);
        FileManager.moveFileToFolder(findingPost.getOutputFilePath(), storeFolder);
    }
}
