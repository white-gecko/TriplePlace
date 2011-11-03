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
	private Dictionary dict;

	public Hexastore(String path) {
		dict = new Dictionary(path + "/dict.tch", path + "/dictInv.tch");
		indices = new Index[6];
		indices[0] = new Index(Index.ORDER_SPO, path + "/spo.tcb");
		indices[1] = new Index(Index.ORDER_SOP, path + "/sop.tcb");
		indices[2] = new Index(Index.ORDER_PSO, path + "/pso.tcb");
		indices[3] = new Index(Index.ORDER_POS, path + "/pos.tcb");
		indices[4] = new Index(Index.ORDER_OSP, path + "/osp.tcb");
		indices[5] = new Index(Index.ORDER_OPS, path + "/ops.tcb");
	}

	public void addTriple(Triple triple) throws IOException {

		Node[] nodes = triple.getNodes();
		long[] nodeIds = new long[nodes.length];

		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i].getId() == 0) {
				// add Node to dictionary
				// Log.v(TAG, "Adding Node to Dict");
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

		Log.v(TAG, "Adding Triple s(" + nodes[0].getId() + ")="
									+ nodes[0].getNodeString()
									+ ",p(" + nodes[1].getId() + ")="
									+ nodes[1].getNodeString()
									+ ",o(" + nodes[2].getId() + ")="
									+ nodes[2].getNodeString()
									+ " to indices.");
		for (Index index : indices) {
			index.addTriple(nodeIds);
		}
	}

	public List<Triple> query(Triple triple) throws IOException {
		// find out which parts are unbound
		Node[] nodes = triple.getNodes();
		long[] pathern = new long[nodes.length];

		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i].getId() == 0) {
				// unbound
				pathern[i] = 0;
			} else {
				pathern[i] = nodes[i].getId();
			}
		}

		// get the right index
		List<long[]> resultSetIndex = indices[0].getTriples(pathern);
		List<Triple> resultSet = new ArrayList<Triple>();
		Node s, p, o;
		for (long[] result : resultSetIndex) {
			s = new Node(result[0]);
			p = new Node(result[1]);
			o = new Node(result[2]);
			Log.v(TAG, "Add new Triple " + result[0] + "," + result[1] + "," + result[2] + " to query result");
			resultSet.add(new Triple(s, p, o));
		}
		return resultSet;
	}

	public void removeTriple(Triple triple) throws IOException {
		// TODO Auto-generated method stub

	}

	public List<Triple> export() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

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
}
