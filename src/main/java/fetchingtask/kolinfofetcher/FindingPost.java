package fetchingtask.kolinfofetcher;

import fetchingtask.Task;

import java.util.Set;

public interface FindingPost extends Task {
    public Set<String> findingPost(String KOLLink);

    public Set<String> findingCommenter(String tweetLink, String KOLLink);

    public Set<String> findingReposter(String tweetLink, String KOLLink);

    public void setupFindingPost(int maxPostPerKOL, int maxCommentPerTweet, int maxRepostPerTweet);
}
