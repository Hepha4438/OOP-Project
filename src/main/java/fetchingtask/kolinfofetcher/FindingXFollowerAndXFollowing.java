package fetchingtask.kolinfofetcher;

import static filemanager.FileManager.*;
import static webbrowserwithcookie.WebInteractor.*;

import java.time.Duration;
import java.util.*;

import org.openqa.selenium.By;
import com.google.gson.reflect.TypeToken;
import webbrowserwithcookie.WebInteractor;
import object.KOL;
import fetchingtask.potentialdatafetcher.FilteringXKOL;
import webbrowserwithcookie.WebManager;
import static webbrowserwithcookie.WebManager.createDriverWithNewCookies;
import static webbrowserwithcookie.WebManager.driver;

public class FindingXFollowerAndXFollowing implements FindingFollowerAndFollowing {
    private WebManager manager;
    private WebInteractor helper;
    private final String fileCheckpointName = "Checkpoint_for_" + this.getClass().getSimpleName() + ".json";
    public String outputFF;
    private static int followers_count = 0;
    private static int followings_count = 0;
    private static int checkpoint = 0;
    public static final String outputFilePath = FindingXFollowerAndXFollowing.class.getSimpleName()+".json";

    public FindingXFollowerAndXFollowing() {
        manager= new WebManager(driver,Duration.ofSeconds(30));
        helper= new WebInteractor(Duration.ofSeconds(30));
    }

    @Override
    public String getOutputFilePath() {
    	return outputFilePath;
    }

    @Override
    public void saveCheckpoint() {
        List<Integer> state = Arrays.asList(checkpoint, followers_count, followings_count, manager.getChannel());
        saveFile(state,fileCheckpointName);
        System.out.println("Lưu file " + fileCheckpointName + " thành công.");
    }

    @Override
    public void loadCheckpoint() {
        List<Integer> state = loadFile(fileCheckpointName,new TypeToken<List<Integer>>() {}.getType() );
        if(state==null){
            return;
        }
        checkpoint = state.get(0);
        followers_count=state.get(1);
        followings_count=state.get(2);
        manager.setChannel(state.get(3));
        System.out.println("Load file " + fileCheckpointName + " thành công.");
    }

    @Override
    public Set<String> findingPerson(String userLink, String type) {
        System.out.println("Bắt đầu tìm kiếm: " + type);
        Set<String> fetching = new LinkedHashSet<>();
        int preSize = 0;
        driver.get(userLink + type);
        waitForPageToLoad(2000);
        By elementLocator = By.cssSelector("[data-testid='UserCell'][role='button']"); 
        By linkLocator = By.tagName("a"); 
        while(true) {
        	helper.fetcher(fetching, Integer.MAX_VALUE, elementLocator, linkLocator);
        	if(preSize == fetching.size()) {
        		break;
        	}
        	preSize = fetching.size();
        }
        return new LinkedHashSet<>(fetching);
    }


    private void processUserLink(String ownerLink) {
        try {
            KOL user = new KOL();
            user.setLink(ownerLink);
            LinkedHashSet<String> verified_followers = (LinkedHashSet<String>) findingPerson(ownerLink, "/verified_followers");
            LinkedHashSet<String> followers = (LinkedHashSet<String>) findingPerson(ownerLink, "/followers");
            LinkedHashSet<String> followers_final = new LinkedHashSet<>();
            followers_final.addAll(verified_followers);
            followers_final.addAll(followers);
            System.out.println("Số Follower tìm được: "+followers_final.size());
            followers_count += followers_final.size();
            user.setFollowers(followers_final);
            LinkedHashSet<String> following = (LinkedHashSet<String>) findingPerson(ownerLink, "/following");
            System.out.println("Số Following tìm đươc: "+following.size());
            followings_count += following.size();
            user.setFollowing(following);
            saveObject(user, outputFilePath, new TypeToken<LinkedHashSet<KOL>>() {}.getType());
        } catch (Exception e) {
            System.err.println("Error processing link: " + ownerLink);
            e.printStackTrace();
        }
    }

    @Override
    public void runTask() {
    	loadCheckpoint();
    	createDriverWithNewCookies();
        Map<String, Integer> userLinks = loadFile(FilteringXKOL.outputFilePath, new TypeToken<Map<String, Integer>>() {}.getType());
        if (userLinks == null || userLinks.isEmpty()) {
            System.out.println("Chưa có data, hãy thực hiện bước 1 và 2.");
            return;
        }
        System.out.println("Đã load: " + userLinks.size() + " user");

        Set<String> userLinksSet = userLinks.keySet();
        if(checkpoint>=userLinksSet.size()){
            driver.quit();
            return;
        }

        ArrayList<String> userLinksList = new ArrayList<>(userLinksSet);
        for (int i = checkpoint; i < userLinksList.size(); i++) {
            String userLink = userLinksList.get(i);
            System.out.println("Processing (" + (i + 1) + "/" + userLinksList.size() + "): " + userLink);
            processUserLink(userLink);

            checkpoint = i + 1;
            saveCheckpoint();
            if (i != 0 && i % 50 == 0) {
                createDriverWithNewCookies();
            }
        }

        if (driver != null) {
            driver.quit(); // Quit
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("Bắt đầu...");
            FindingXFollowerAndXFollowing program = new FindingXFollowerAndXFollowing();
            program.runTask();
            System.out.println("Hoàn Thành.");
        } catch (Exception e) {
            System.err.println("Error.");
            e.printStackTrace();
        }
    }

    @Override
    public void deleteCheckpoint(){
        deleteFile(fileCheckpointName);
    }
}
