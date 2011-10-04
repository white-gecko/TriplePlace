package org.aksw.tripleplace;

import java.io.IOException;
import java.util.List;

public interface Store {
	public void addTriple(Triple triple) throws IOException;
	public List<Triple> query(Triple triple) throws IOException;
	public void removeTriple(Triple triple) throws IOException;
	public List<Triple> export() throws IOException;
}
