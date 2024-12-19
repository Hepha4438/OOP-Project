package main;

import static filemanager.FileManager.loadFile;
import static filemanager.FileManager.saveFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

public class Setting {
	public List<String> hashtags = new ArrayList<>();
    public int maxKOLSelected = 0;
    public int maxPostRetrievedPerUser = 0;
    public int maxComments = 0;
    public int maxReposter = 0;
    public boolean isTask1Completed = false;
    public boolean isTask2Completed = false;
    private static final String PROGRESS_FILE = "setting.json";
    public static Map<String, Object> setting;
    
    public List<String> getHashtags() {
        return hashtags;
    }
    
    public void setHashtags(List<String> hashtags) {
        setting.put("hashtags", hashtags);
    }
    
    public void saveState() {
    	setting.put("Task1State", isTask1Completed);
    	setting.put("Task2State", isTask2Completed);
    }
    public int getMaxKOLSelected() {
        return maxKOLSelected;
    }
    
    public void setMaxKOL(int maxKOL) {
        setting.put("MaxKOL", maxKOL);
    }

    public int getMaxPostRetrievedPerUser() {
        return maxPostRetrievedPerUser;
    }
    
    public void setMaxPostRetrievedPerUser(int maxTweets) {
        setting.put("MaxTweetsRetrievedPerUser", maxTweets);
    }
    
    public int getMaxComments() {
        return maxComments;
    }
    
    public void setMaxComments(int maxComments) {
        setting.put("MaxComments", maxComments);
    }
    
    public int getReposter() {
    	return maxReposter;
    }
    
    public void setMaxReposter(int maxReposter) {
        setting.put("MaxReposter", maxReposter);
    }
    
    public void saveProgress() {
        saveFile(setting, PROGRESS_FILE);
    }

    public void loadProgress() {
        setting = loadFile(PROGRESS_FILE, new TypeToken<Map<String, Object>>() {}.getType());

        // Kiểm tra và gán các giá trị từ setting, nhưng giữ lại giá trị cũ của TextField
        this.hashtags = (List<String>) setting.getOrDefault("hashtags", new ArrayList<String>());
        this.maxKOLSelected = (int) Math.round((Double) setting.getOrDefault("MaxKOL", 0.0));
        this.maxPostRetrievedPerUser = (int) Math.round((Double) setting.getOrDefault("MaxTweetsRetrievedPerUser", 0.0));
        this.maxComments = (int) Math.round((Double) setting.getOrDefault("MaxComments", 0.0));
        this.maxReposter = (int) Math.round((Double) setting.getOrDefault("MaxReposter", 0.0));
        this.isTask1Completed = (boolean) setting.getOrDefault("Task1State", false);
        this.isTask2Completed = (boolean) setting.getOrDefault("Task2State", false);
    }


    public void clearProgress() {
        if (setting == null) {
            setting = new HashMap<>();  // Khởi tạo setting nếu nó là null
        }
        setting.clear();
        saveProgress();
    }
    
    public boolean hasData() {
    	setting = loadFile(PROGRESS_FILE, new TypeToken<Map<String, Object>>() {}.getType());
        return setting != null && !setting.isEmpty();
    }
}
