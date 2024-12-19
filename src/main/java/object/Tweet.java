package object;

import java.util.*;



public class Tweet extends WebObject {

	private Set<String> adjPerson = new HashSet<>();
	private String owner="";

	public Tweet() {
		super();
	}

	public static String getOwnerLink(String url) {
		// Find the position of "/status"
		int statusIndex = url.indexOf("/status");

		// If "/status" is found, extract the substring before it
		if (statusIndex != -1) {
			return url.substring(0, statusIndex);
		} else {

			return "Invalid URL format: '/status' not found.";
		}
	}

	public void setOwner(String own) {
		this.owner=own;
	}

	public String getOwner() {
		return this.owner;
	}

	public void showTweet() {
		System.out.println("This is a tweet: ");

		System.out.println(owner);
		for(String a: adjPerson) {
			System.out.println(a);
		}
	}

	public Set<String> getAdjPerson(){
		return this.adjPerson;
	}

	public void addAdjPerson(Set<String> personList) {
		this.adjPerson.addAll(personList);
	}

	public void addAdjPerson(String person) {
		adjPerson.add(person);
	}
	@Override
	public Set<String> outVertexOf() {
		Set<String> result=new HashSet<>();
		result.add(owner);
		return result;
	}
	@Override
	public Set<String> inVertexOf() {
		return this.adjPerson;
	}
}