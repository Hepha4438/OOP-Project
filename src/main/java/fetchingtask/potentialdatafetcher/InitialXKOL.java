package fetchingtask.potentialdatafetcher;

import static filemanager.FileManager.*;
import static webbrowserwithcookie.WebInteractor.*;

import java.time.Duration;
import java.util.*;

import org.openqa.selenium.By;
import com.google.gson.reflect.TypeToken;

import webbrowserwithcookie.WebInteractor;
import webbrowserwithcookie.WebManager;
import static webbrowserwithcookie.WebManager.createDriverWithNewCookies;
import static webbrowserwithcookie.WebManager.driver;

public class InitialXKOL implements InitialKOL {
    private WebManager manager;
    private WebInteractor helper;
    private String hashtag;
    private int checkpoint = 0;
    private static int KOLs_count = 0;
    private int scroll_count = 0;
    private List<String> hashtags;
    private final String fileCheckpointName = "Checkpoint_for_" + this.getClass().getSimpleName() +".json";
    public static final String outputFilePath = InitialXKOL.class.getSimpleName()+".json";
    
    public InitialXKOL() {
        manager= new WebManager(driver,Duration.ofSeconds(30));
        helper= new WebInteractor(Duration.ofSeconds(30));
    }

    public void setHashtags (List<String> hashtags){
        this.hashtags=hashtags;
        System.out.println("oke");
        System.out.println(hashtags);
    }

    @Override
    public String getOutputFilePath() {
    	return outputFilePath;
    }

    @Override
    public void saveCheckpoint() {
        List<Integer> state = Arrays.asList(checkpoint,KOLs_count, manager.getChannel());
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
        KOLs_count = state.get(1);
        manager.setChannel(state.get(2));
        System.out.println("Load file " + fileCheckpointName + " thành công.");
    }

    @Override
    public void newHashtagSearch() {
    	createDriverWithNewCookies();
    	driver.get("https://x.com/search?q="+hashtag+"&src=typed_query&f=user");
    	waitForPageToLoad(5000);
    }

    @Override
    public void runTask(){
    	loadCheckpoint();
    	createDriverWithNewCookies();
        LinkedHashSet<String> links = loadFile(outputFilePath,new TypeToken<LinkedHashSet<String>>(){}.getType());
        if(links==null){
            links= new LinkedHashSet<>();
        }

        if(hashtags.size()<=checkpoint){
            driver.quit();
            return;
        }

    	int preSize = 0; 
    	hashtag = hashtags.get(checkpoint);
    	driver.get("https://x.com/search?q="+hashtag+"&src=typed_query&f=user");

    	while (checkpoint <= hashtags.size() - 1 ) {
            if ((preSize == links.size()&&!links.isEmpty()) || scroll_count == 4) {
            	System.out.println("Thực hiện: "+scroll_count+" lượt vuốt");
            	scroll_count = 0;
                KOLs_count = links.size();
                saveFile(links, outputFilePath);
                checkpoint++;
                saveCheckpoint();
                if (checkpoint <= hashtags.size() - 1){
                    hashtag = hashtags.get(checkpoint);
                    System.out.println("Chuyển hashtag");
                    newHashtagSearch();
                }
            } else {
                preSize = links.size();
            }
            helper.fetcher( links ,null,By.cssSelector("[data-testid='UserCell'][role='button']"),By.tagName("a") );
            scroll_count++;
            waitForPageToLoad(2000);
        }
    }

    @Override
    public void deleteCheckpoint(){
        deleteFile(fileCheckpointName);
    }
}
