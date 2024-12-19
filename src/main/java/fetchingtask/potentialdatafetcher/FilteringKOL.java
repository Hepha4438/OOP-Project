package fetchingtask.potentialdatafetcher;

import fetchingtask.Task;

import java.util.Map;

public interface FilteringKOL extends Task {
    public String getFollowerCount(String KOLlink);

    public Map<String, Integer> filteredKOLMap(Map<String,Integer> kolMap, int maxFilteredKOL);

    public void setMaxFilteredKOL(int maxFilteredKOL);
}
