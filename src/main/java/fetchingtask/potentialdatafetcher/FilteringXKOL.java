package fetchingtask.potentialdatafetcher;

import com.google.gson.reflect.TypeToken;

import webbrowserwithcookie.WebInteractor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import webbrowserwithcookie.WebManager;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static filemanager.FileManager.*;
import static webbrowserwithcookie.WebInteractor.*;
import static webbrowserwithcookie.WebManager.createDriverWithNewCookies;
import static webbrowserwithcookie.WebManager.driver;


public class FilteringXKOL implements FilteringKOL {
    private WebInteractor helper;
    private WebManager manager;
    private int maxFilteredKOL;
    private static int error_count = 0;
    private static int checkpoint = 0;
    private final String fileCheckpointName = "Checkpoint_for_" + this.getClass().getSimpleName() +".json";
    public static final String outputFilePath = FilteringXKOL.class.getSimpleName()+".json";
    
    public FilteringXKOL(){
        manager = new WebManager(driver,Duration.ofSeconds(30));
        helper= new WebInteractor(Duration.ofSeconds(30));
    }

    @Override
    public void setMaxFilteredKOL(int maxFilteredKOL){
        this.maxFilteredKOL=maxFilteredKOL;
    }

    @Override
    public String getOutputFilePath() {return outputFilePath;}
    
    public void ErrorCheck() {
        if (error_count == 4) {
            driver.quit();
            waitForPageToLoad(65000);
            createDriverWithNewCookies();
        }
    }

    @Override
    public void saveCheckpoint() {
        List<Integer> state = new ArrayList<>();
        state.add(manager.getChannel());
        state.add(checkpoint);
        saveFile(state,fileCheckpointName);
        System.out.println("Lưu file " + fileCheckpointName + " thành công.");
    }

    @Override
    public void loadCheckpoint() {
        Type memType = new TypeToken<List<Integer>>() {}.getType();
        List<Integer> state = loadFile(fileCheckpointName,memType );
        if(state==null){
            return;
        }
        manager.setChannel(state.get(0));
        checkpoint = state.get(1);
        System.out.println("Load file " + fileCheckpointName + " thành công.");
    }

    @Override
    public String getFollowerCount(String KOLlink) {
        String followerCount = "";
        driver.get(KOLlink);
        try {
            helper.setWait(Duration.ofSeconds(10));
            helper.waitForElement(By.xpath("//a[contains(., 'Followers')]"));
            WebElement followerContainer = driver.findElement(By.xpath("//a[contains(., 'Followers')]"));
            WebElement followerCountElement = followerContainer.findElement(By.xpath(".//span[1]"));
            followerCount = followerCountElement.getText();
            error_count = 0;
        } catch (Exception ignored){
            error_count++;
            ErrorCheck();
        }
        return followerCount;
    }

    public int parseFollowerNumber(String numStr) {
        if (numStr == null || numStr.isEmpty()) {
            return 0;
        }

        // Remove commas
        numStr = numStr.replace(",", "");

        // Check for K and M suffix
        if (numStr.endsWith("K")) {
            return (int) (Double.parseDouble(numStr.replace("K", "")) * 1_000);
        } else if (numStr.endsWith("M")) {
            return (int) (Double.parseDouble(numStr.replace("M", "")) * 1_000_000);
        } else {
            return Integer.parseInt(numStr);
        }
    }

    @Override
    public Map<String,Integer> filteredKOLMap(Map<String,Integer> kolMap, int maxFilteredKOL){
        return kolMap.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue())) // Sắp xếp giảm dần theo follower
                .limit(maxFilteredKOL)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
    }

    @Override
    public void runTask() {
        loadCheckpoint();
        createDriverWithNewCookies();
        String inputFilePath = InitialXKOL.outputFilePath;

        Type setType = new TypeToken<List<String>>() {}.getType();
        List<String> savedKOLList =loadFile(inputFilePath,setType);
        if(savedKOLList ==null){
            savedKOLList =new ArrayList<>();
        }
        int target = savedKOLList.size();
        if(target<=checkpoint){
            driver.quit();
            return;
        }
        List<String> uncheckedKOLList = savedKOLList.subList(checkpoint, savedKOLList.size());
        for (String e: uncheckedKOLList){
            System.out.println(e);
        }

        setType = new TypeToken<LinkedHashMap<String,Integer>>(){}.getType();
        LinkedHashMap<String,Integer> linkFollowerMap = loadFile(outputFilePath,setType);
        if(linkFollowerMap==null){
            linkFollowerMap=new LinkedHashMap<>();
        }

        for (String link : uncheckedKOLList) {
            System.out.println(link);
            String followerCount = getFollowerCount(link);
            linkFollowerMap.put(link, parseFollowerNumber(followerCount));
            saveFile(linkFollowerMap,outputFilePath);
            checkpoint++;
            saveCheckpoint(); // Lưu checkpoint vào file
            System.out.println("Đã xử lý xong KOL thứ " + checkpoint);


            // Sau mỗi 80 link, reset driver và nạp lại cookies
            if (checkpoint % 80 == 0 && checkpoint<target) {
                System.out.println("Đã xử lý " + checkpoint + " link. Đang khởi tạo lại driver...");
                driver.quit();
                waitForPageToLoad(65000);
                createDriverWithNewCookies();
            }
        }
        // Lọc các link có số lượng follower cao nhất
        LinkedHashMap<String, Integer> topLinks = (LinkedHashMap<String, Integer>) filteredKOLMap(linkFollowerMap,maxFilteredKOL);

        saveFile(topLinks,outputFilePath);
        saveCheckpoint();
        System.out.println("Top "+maxFilteredKOL+" links đã được ghi vào file " + outputFilePath);
        System.out.println("Chương trình hoàn thành");
        driver.quit();
    }

    @Override
    public void deleteCheckpoint(){
        deleteFile(fileCheckpointName);
    }

}