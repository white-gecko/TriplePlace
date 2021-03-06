package org.aksw.tripleplace;

import java.io.IOException;
import java.util.List;

import org.aksw.tripleplace.hexastore.Hexastore;

public class TriplePlace implements Store {
	public static final String ENGINE_HEXASTORE = "org.aksw.tripleplace.hexastore.Hexastore";
	
	private Store store;
	
	public TriplePlace(String engine, String path) throws Exception {
		if (engine.equals(ENGINE_HEXASTORE)) {
			store = new Hexastore(path);
		} else {
			throw new Exception("Could not find store engin \"" + engine + "\"");
		}
	}
	
	public void addTriple(Triple triple) throws Exception {
		store.addTriple(triple);
	}

	public List<Triple> query(Triple triple) throws Exception {
		return store.query(triple);
	}

	public void removeTriple(Triple triple) throws IOException {
		store.removeTriple(triple);
	}

	public List<Triple> export() throws Exception {
		return store.export();
	}

	public Node getNode(String nodeString) throws Exception {
		return store.getNode(nodeString);
	}
}
