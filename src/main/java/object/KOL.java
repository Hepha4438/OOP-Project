package object;

import java.util.*;

public class KOL extends WebObject {

	private LinkedHashSet<String> followers = new LinkedHashSet<>(); // Danh sách followers
	private LinkedHashSet<String> following = new LinkedHashSet<>(); // Danh sách following

	public KOL() {
		super();
	}

	public void setFollowers(LinkedHashSet<String> followers) {
		this.followers = followers;
	}

	public void setFollowing(LinkedHashSet<String> following) {
		this.following = following;
	}

	public LinkedHashSet<String> getFollowers() {
		return followers;
	}

	public LinkedHashSet<String> getFollowing() {
		return following;
	}

	@Override
	public Set<String> outVertexOf() {
		return this.following;
	}

	@Override
	public Set<String> inVertexOf() {
		return this.followers;
	}

}
