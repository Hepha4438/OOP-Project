package rankingalgorithm;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import com.google.gson.reflect.TypeToken;

import object.KOL;
import object.Tweet;
import fetchingtask.potentialdatafetcher.FilteringXKOL;

import fetchingtask.kolinfofetcher.FindingXFollowerAndXFollowing;
import fetchingtask.kolinfofetcher.FindingTweet;

import static filemanager.FileManager.loadFile;
import static filemanager.FileManager.saveFile;

public class XPageRankAlgorithm implements Algorithm {
	private Graph<String, DefaultEdge> graph;
	// Pagerank hyperparameter
	private static double dampingFactor=0.85;
	private static double tolerance=1e-10;
	private static int maxIterations=100;

	public void setGraph(Graph<String, DefaultEdge> graph) {
		this.graph=graph;
	}

	public Graph<String,DefaultEdge> getGraph() {
		return this.graph;
	}
	
	@Override
	public Map<String, Double> algorithmImplementation() {
		Map<String, Integer> userLinks = loadFile(FilteringXKOL.outputFilePath, new TypeToken<Map<String, Integer>>() {}.getType());
        Set<String> userLinksSet = userLinks.keySet();
		List<String> kolList = new ArrayList<>(userLinksSet);
		// A Map to save score for every nodes in the graph
		Map<String, Double> scores = new HashMap<>();
		double initialScore = 1.0 / graph.vertexSet().size();
		for (String vertex : graph.vertexSet()) {
			scores.put(vertex, initialScore);
		}

		// Iterative computation
		for (int i = 0; i < maxIterations; i++) {
			Map<String, Double> newScores = new HashMap<>();
			double maxChange = 0;

			for (String vertex : graph.vertexSet()) {
				double rank = (1 - dampingFactor) / graph.vertexSet().size(); // Base score from teleportation
				for (DefaultEdge incomingEdge : graph.incomingEdgesOf(vertex)) {
					String source = graph.getEdgeSource(incomingEdge);
					rank += dampingFactor * scores.get(source) / graph.outDegreeOf(source);
				}
				
				newScores.put(vertex, rank);
				maxChange = Math.max(maxChange, Math.abs(rank - scores.get(vertex)));
			}

			// Update scores
			scores = newScores;

			// Check for convergence
			if (maxChange < tolerance) {
				break;
			}
		}
		Map<String, Double> rankScore = new HashMap<>();

		// Put the augmented rank of each KOL in the map rankScore
		for (String key : kolList) {
			if (scores.containsKey(key)) {
				rankScore.put(key, scores.get(key)); // 0.5*scores.get(key) +
														// 0.5*rankList.get(key)/graph.vertexSet().size()
			}
		}

		LinkedHashMap<String, Double> topKOL = rankScore.entrySet().stream()
				.sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue())) // Sắp xếp giảm dần theo follower
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(oldValue, newValue) -> oldValue,
						LinkedHashMap::new
				));
		
		return topKOL;
	}

	@Override
	 public void ranking() {
		Type setType = new TypeToken<LinkedHashSet<KOL>>(){}.getType();
		Type setType2 = new TypeToken<LinkedHashSet<Tweet>>(){}.getType();
		KOLGraphMaker builtGraph=new KOLGraphMaker();
		XPageRankAlgorithm pagerank=new XPageRankAlgorithm();
	
		builtGraph.addObjectsToGraph(FindingXFollowerAndXFollowing.outputFilePath, setType);
		builtGraph.addObjectsToGraph(FindingTweet.outputFilePath, setType2);
		pagerank.setGraph(builtGraph.getGraph());
		Map<String, Double> result= pagerank.algorithmImplementation();
		saveFile(result,"rank.json");

	}
}
