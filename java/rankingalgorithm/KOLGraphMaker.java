package rankingalgorithm;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import object.WebObject;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class KOLGraphMaker {
	// Graph
	private Graph<String, DefaultEdge> graph;

	public KOLGraphMaker() {
		graph = new DefaultDirectedGraph<>(DefaultEdge.class);
	}

	public Graph<String, DefaultEdge> getGraph() {
		return graph;
	}

	public void setGraph(Graph<String, DefaultEdge> graph) {
		this.graph = graph;
	}

	public int getEdgeCount() {
		return graph.edgeSet().size();
	}

	public int getVertexCount() {
		return graph.vertexSet().size();
	}

	public <T extends WebObject> void addObjectsToGraph(String fileName, Type setType) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Set<T> objects = new HashSet<>();

		// Load objects from the JSON file
		try (FileReader reader = new FileReader(fileName)) {
			objects = gson.fromJson(reader, setType);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Add objects to the graph
		for (T object : objects) {
			if (object == null)
				continue;

			// Add the main vertex (link)
			graph.addVertex(object.getLink());

			// Add out-edges (e.g., following relationships)
			for (String outVertex : object.outVertexOf()) {
				graph.addVertex(outVertex);
				graph.addEdge(object.getLink(), outVertex);
			}

			// Add in-edges (e.g., follower relationships)
			for (String inVertex : object.inVertexOf()) {
				graph.addVertex(inVertex);
				graph.addEdge(inVertex, object.getLink());
			}
		}
	}

}
