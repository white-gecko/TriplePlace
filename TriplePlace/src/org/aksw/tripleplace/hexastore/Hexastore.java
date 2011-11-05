package org.aksw.tripleplace.hexastore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.tripleplace.Node;
import org.aksw.tripleplace.Store;
import org.aksw.tripleplace.Triple;

import android.util.Log;

public class Hexastore implements Store {
	private static final String TAG = "Hexastore";

	private Index[] indices;
	private int[][] indexSelection;
	private Dictionary dict;

	public Hexastore(String path) {
		Log.v(TAG, "Initialized Hexastore at \"" + path + "\"");
		dict = new Dictionary(path + "/dict.tch", path + "/dictInv.tch");
		indices = new Index[6];
		indices[0] = new Index(Index.ORDER_SPO, path + "/spo.tcb");
		indices[1] = new Index(Index.ORDER_SOP, path + "/sop.tcb");
		indices[2] = new Index(Index.ORDER_PSO, path + "/pso.tcb");
		indices[3] = new Index(Index.ORDER_POS, path + "/pos.tcb");
		indices[4] = new Index(Index.ORDER_OSP, path + "/osp.tcb");
		indices[5] = new Index(Index.ORDER_OPS, path + "/ops.tcb");

		indexSelection = new int[][] { { 0, 0, 1 }, { 2, 2, 3 }, { 4, 5, 4 } };
	}

	/**
	 * Add a new Triple to the model
	 * 
	 * @param triple
	 *            the Triple which should be added to the model
	 */
	public void addTriple(Triple triple) throws IOException {

		Node[] nodes = triple.getNodes();
		long[] nodeIds = new long[nodes.length];

		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i].getId() == 0) {
				// add Node to dictionary
				try {
					dict.addNode(nodes[i]);
				} catch (Exception e) {
					// have to remove the other nodes
					throw new IOException(
							"Could not add triple s="
									+ nodes[0].getNodeString()
									+ ",p="
									+ nodes[1].getNodeString()
									+ ",o="
									+ nodes[2].getNodeString()
									+ " because of an Exception on adding Node at position "
									+ i, e);
				}
			}

			// Log.v(TAG, "Adding Node has ID: " + nodes[i].getId());
			nodeIds[i] = nodes[i].getId();
		}

		// maybe check first if this value already exists to get no
		// duplicates in the list

		if (!indices[0].hasTriple(nodeIds)) {
			for (Index index : indices) {
				// should catch exceptions and try to asure ACID
				index.addTriple(nodeIds);
			}
		}
	}

	/**
	 * The query method takes a Triple object which has one ore more variable
	 * Nodes and returns a List of all Triples which match the given Triple
	 * 
	 * @param triple
	 *            a which has one ore more variable Nodes
	 * @return a List of Triples which match the given Triple
	 * @throws Exception
	 *             if a bug occures
	 */
	public List<Triple> query(Triple triple) throws Exception {
		// find out which parts are unbound
		Node[] nodes = triple.getNodes();
		long[] pathern = new long[nodes.length];

		// get the right index
		int[] selectedIndeces = null;
		int selectedIndex = 0;

		for (int i = 0; i < 3; i++) {
			// check for fixed nodes
			if (pathern[i] != 0) {
				// this should be true the first time
				if (selectedIndeces == null) {
					selectedIndeces = indexSelection[i];
				}
				selectedIndex = selectedIndeces[i];
			}
		}

		List<long[]> resultSetIndex = indices[selectedIndex]
				.getTriples(pathern);

		List<Triple> resultSet = new ArrayList<Triple>();
		if (resultSetIndex != null) {
			Node s, p, o;
			for (long[] result : resultSetIndex) {
				// should get full Nodes from reverse Dictionary
				s = dict.getNode(result[0]);
				p = dict.getNode(result[1]);
				o = dict.getNode(result[2]);
				resultSet.add(new Triple(s, p, o));
			}
		}
		return resultSet;
	}

	public void removeTriple(Triple triple) throws IOException {
		// TODO Auto-generated method stub

	}

	public List<Triple> export() throws Exception {
		Node s, p, o;
		s = new Node("?s");
		p = new Node("?p");
		o = new Node("?o");
		Triple triple = new Triple(s, p, o);
		return query(triple);
	}

	/**
	 * Retrieves the spezified Node fromt the model or creates a new one, if
	 * this Nodes doesn't exist in the Model.
	 * 
	 * @param nodeString
	 *            spezifies the Node as used in N3, N-Triples and Turtle
	 * @return a Node object representing the given Node with a Dictionary ID
	 */
	public Node getNode(String nodeString) throws Exception {
		Node node = new Node(nodeString);

		try {
			dict.addNode(node);
		} catch (Exception e) {
			throw new IOException("Could not add node \""
					+ node.getNodeString() + "\" to Dictionary", e);
		}

		return node;
	}

	public Node getNode(long id) throws Exception {
		return dict.getNode(id);
	}
}
