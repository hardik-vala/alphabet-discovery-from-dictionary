import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


class AmbiguousTopologicalSortException extends RuntimeException {
	AmbiguousTopologicalSortException(String message) {
		super(message);
	}
}


class CyclicDirectedGraphException extends RuntimeException {
	CyclicDirectedGraphException () {}
}


/**
 * Directed graph.
 * 
 * @param <E> Type of data stored at each vertex.
 */
class DirectedGraph<E> {

	// Adjacency lists (really sets) for managing edges.
	private Map<E, HashSet<E>> adjLists = new HashMap<>();
	// Tracks the in-degree of each vertex.
	private Map<E, Integer> inDegrees = new HashMap<>();

	/**
 	 * Checks whether an edge from one vertex to the other exists.
	 *
	 * @param fromVertex Start vertex of the edge.
	 * @param toVertex End vertex of the edge.
	 * @return True if the edge exists, false otherwise.
	 */
	boolean hasEdge(E fromVertex, E toVertex) {
		HashSet<E> fromVertexAdjList = this.adjLists.get(fromVertex);
		if (fromVertexAdjList != null)
			return fromVertexAdjList.contains(toVertex);
		
		return false;
	}

	// Increments the in-degree for the given vertex by 1.
	private void updateInDegree(E vertex) {
		// Vertex has in-degree 0, hence isn't being tracked yet.
		if (this.inDegrees.get(vertex) == null)
			// Add the vertex to the in-degree map with an in-degree of 1.
			this.inDegrees.put(vertex, 1);
		// Vertex has in-degree > 0.
		else
			// Increment that in-degree by 1.
			this.inDegrees.put(vertex, this.inDegrees.get(vertex) + 1);
	}

	/**
 	 * Adds the given edge from one vertex to the other to the graph.
	 *
	 * @param fromVertex Start vertex of the edge.
	 * @param toVertex End vertex of the edge.
	 */
	void addEdge(E fromVertex, E toVertex) {
		// Only add the edge if it doesn't already exist.
		if (!this.hasEdge(fromVertex, toVertex)) {
			// Get the adjacency list for the originating vertex.
			HashSet<E> fromVertexAdjList = this.adjLists.get(fromVertex);
			// Create the list if the vertex doesn't currently exist in the graph.
			if (fromVertexAdjList == null)
				fromVertexAdjList = new HashSet<>();
			
			// Add the terminating vertex to the adjacency list.
			fromVertexAdjList.add(toVertex);
			this.adjLists.put(fromVertex, fromVertexAdjList);
			
			// Update the in-degree of the terminating vertex.
			this.updateInDegree(toVertex);
			
			// Add the terminating vertex to the graph if it doesn't already exist.
			if (this.adjLists.get(toVertex) == null)
				this.adjLists.put(toVertex, new HashSet<>());
		}
	}

	/**
	 * Returns the number of vertices.
	 *
	 * @return Number of vertices.
	 */
	int getVertexCount() {
		return this.adjLists.size();
	}

	/**
	 * Returns the toplogical sort of vertices (using <a href="http://www.geeksforgeeks.org/topological-sorting-indegree-based-solution/">Kahn's algorithm</a>).
	 *
	 * @return List of vertices in the graph, topologically sorted.
	 * @throws AmbiguousTopologicalSortException If the graph has multiple topological sorts.
	 * @throws CyclicDirectedGraphException If the graph is cyclic and hence does not have a topological sort.
	 */
	List<E> getTopologicalSort() {
		// Stores the topologically sorted vertices.
		List<E> sorted = new LinkedList<>();

		DirectedGraph<E> self = this;
		// Create a deep copy of the in-degree map.
		Map<E, Integer> inDegreesTracker = new HashMap<E, Integer>() {{
			for (E v : self.adjLists.keySet()) {
				if (self.inDegrees.get(v) == null)
					put(v, 0);
				else
					put(v, (int) self.inDegrees.get(v));
			}
		}};

		// Queue for tracking vertices to visit, initialized with vertices an in-degree of 0.
		Deque<E> queue = new LinkedList<E>() {{
			for (E v : inDegreesTracker.keySet()) {
				if (inDegreesTracker.get(v) == 0)
					add(v);
			}
		}};

		while (!queue.isEmpty()) {
			// If the queue every has more than one vertex at a time, then multiple topological
			// sortings can be produced by considering the vertices in the queue in a different order.
			if (queue.size() > 1)
				throw new AmbiguousTopologicalSortException("Unknown order of elements in " + queue);
			// Dequeue the vertex from the queue and add it to the sort.
			E v = queue.removeFirst();
			sorted.add(v);
			// Decrement the in-degrees of each of its neighbours by 1, and if the resulting
			// in-degree is 0, add the corresponding vertex to the queue.
			for (E w : this.adjLists.get(v)) {
				int inDegree = inDegreesTracker.get(w);
				if (--inDegree == 0)
					queue.add(w);
				inDegreesTracker.put(w, inDegree);
			}
		}

		// If the number of vertices encountered doesn't equate to the total number of vertices, then the
		// graph has a cycle.
		if (sorted.size() != this.getVertexCount())
			throw new CyclicDirectedGraphException(); 

		return sorted;
	}

}


class UnderspecifiedDictionaryException extends RuntimeException {
	UnderspecifiedDictionaryException(String message) {
		super(message);
	}
}


class MalformedDictionaryException extends RuntimeException {
	MalformedDictionaryException() {}
}


/**
 * Solution for the <i>Alphabet from Dictionary Discovery</i> problem.
 */
class Solution {

	/**
	 * Returns the alphabet, i.e. letters ordered according to the language, for the given dictionary of words.
	 *
	 * @param dict Dictionary of words.
	 * @return Alphabet as a list of letters, ordered according to the dictionary.
	 * @throws UnderspecifiedDictionaryException If there is ambiguity in the alphabet ordering resulting
	 * 	from a lack of some comparison information in the dictionary.
	 * @throws MalformedDictionaryException If there are contradictions in the alphabet ordering resulting
	 *	from a malformed dictionary.
	 * @precondition dict is ordered lexicographically according to the given language. 
	 */
	static List<Character> getAlphabet(String[] dict) {
		// Tracks the column index in the table defined by the dictionary of words, where each row
		// corresponds to a word (left-aligned) and each column is letter-wide.
		int col = 0;
		// Directed graph to keep track of pairwise letter orderings. An edge exists from character x to
		// y if x precedes y in the alphabet ordering.
		DirectedGraph<Character> dg = new DirectedGraph<>();

		// Stores the previous letter in the given column.
		Character prevLetter;
		// Stores the current letter in the given column.
		Character currLetter;
		// Stores the prefix of the word containing prevLetter up to the prevLetter (exclusive).
		String prefixBeforePrevLetter = null;
		// Stores the prefix of the word containing currLetter up to the currLetter (exclusive).
		String prefixBeforeCurrLetter = null;
		do {
			prevLetter = null;
			currLetter = null;
			
			// Word index in the dictionary.
			int wordIdx = 0;

			// Proceed through the words in order until the first is found with a letter in the given
			// column.
			while (wordIdx < dict.length && dict[wordIdx].length() <= col) wordIdx++;
			if (wordIdx < dict.length) {
				// Track the letter of that word falling in the given column, and the preceding
				// prefix as well.
				prevLetter = dict[wordIdx].charAt(col);
				prefixBeforePrevLetter = dict[wordIdx++].substring(0, col);
			}

			// Proceed through the words in order until the second is found with a letter in the given
			// column.
			while (wordIdx < dict.length && dict[wordIdx].length() <= col) wordIdx++;
			if (wordIdx < dict.length) {
				// Track the letter of that word falling in the given column, and the preceding
				// prefix as well.
				currLetter = dict[wordIdx].charAt(col);
				prefixBeforeCurrLetter = dict[wordIdx++].substring(0, col);
			}

			// If the two letters aren't equal to each other and their prefixes are equal, then
			// prevLetter comes before currLetter in the alphabet.
			if (prevLetter != currLetter && prefixBeforePrevLetter.equals(prefixBeforeCurrLetter))
				// Therefore we add an directed edge from prevLetter to currLetter in the graph.
				dg.addEdge(prevLetter, currLetter);

			// Continue through the rest of the words in the dictionary.
			while (wordIdx < dict.length) {
				// If a word has a letter falling in the given column.
				if (dict[wordIdx].length() > col) {
					// Update the variables tracking the letters and prefixes.
					prevLetter = currLetter;
					prefixBeforePrevLetter = prefixBeforeCurrLetter;
					currLetter = dict[wordIdx].charAt(col);
					// If the two letters aren't equal to each other and their prefixes are
					// equal, then prevLetter comes before currLetter in the alphabet.
					prefixBeforeCurrLetter = dict[wordIdx].substring(0, col);
					if (prevLetter != currLetter && prefixBeforePrevLetter.equals(prefixBeforeCurrLetter))
						// Therefore we add an directed edge from prevLetter to
						// currLetter in the graph.
						dg.addEdge(prevLetter, currLetter);

				}
				wordIdx++;
			}	

			col++;
		// Keep iterating column-by-column until no two words intersecting a column can be found.
		} while (prevLetter != null && currLetter != null);

		// The topological sort of the directed graph spells out the alphabet in the correct order.
		List<Character> alphabet = null; 
		try {
			alphabet = dg.getTopologicalSort();
		// An ambigious topological sort entails the dictionary is underspecified, i.e. at least two
		// letters in the alphabet have an ambiguous order.
		} catch (AmbiguousTopologicalSortException atse) {
			throw new UnderspecifiedDictionaryException(atse.getMessage());
		// A cycle in the graph implies there is a circular ordering of a subset of letters in the
		// alphabet, which is a contradiction resulting from a malformed dictionary.
		} catch (CyclicDirectedGraphException cdge) {
			throw new MalformedDictionaryException();
		}

		return alphabet;
	}

	public static void main(String[] args) {
		if (args.length == 0)
			throw new IllegalArgumentException("Expected a dictionary file as the first argument.");

		// Read the dictionary file, which is expected to have one word per line, into the buffer.
		ArrayList<String> dictBuffer = new ArrayList<>();		
		try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
			String line = br.readLine();
			while (line != null) {
				dictBuffer.add(line);
				line = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		// Copy the buffer over to an array.
		String[] dict = new String[dictBuffer.size()];
		dictBuffer.toArray(dict);

		System.out.println("Alphabet: " + getAlphabet(dict));	
	}

}

