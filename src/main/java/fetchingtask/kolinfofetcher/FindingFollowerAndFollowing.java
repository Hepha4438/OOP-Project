package fetchingtask.kolinfofetcher;

import fetchingtask.Task;

import java.util.Set;

public interface FindingFollowerAndFollowing extends Task {
    public Set<String> findingPerson(String userLink, String type);
}
