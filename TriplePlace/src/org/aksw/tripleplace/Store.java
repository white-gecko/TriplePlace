package org.aksw.tripleplace;

import java.io.IOException;
import java.util.List;

public interface Store {
	public void addTriple(Triple triple) throws Exception;
	public List<Triple> query(Triple triple) throws Exception;
	public void removeTriple(Triple triple) throws IOException;
	public List<Triple> export() throws Exception;
	public Node getNode(String nodeString) throws Exception;
}
