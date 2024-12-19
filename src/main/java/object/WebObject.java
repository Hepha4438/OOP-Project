package object;

import java.util.*;
import java.util.Objects;

public abstract class WebObject {
	private String link;

	public WebObject() {
		this.link="";

	}

	public void setLink(String s) {
		this.link=s;
	}

	public String getLink() {
		return this.link;
	}

	@Override
	public int hashCode() {
		return Objects.hash(link);
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null || getClass() != obj.getClass()) return false;

		WebObject that = (WebObject) obj;
		return this.link.equals(that.link);
	}
	
	public abstract Set<String> outVertexOf();
	
	public abstract Set<String> inVertexOf();	
		
	


}