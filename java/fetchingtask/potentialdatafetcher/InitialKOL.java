package fetchingtask.potentialdatafetcher;

import fetchingtask.Task;

import java.util.List;

public interface InitialKOL extends Task {
    public void setHashtags (List<String> hashtags);

    public void newHashtagSearch();
}
