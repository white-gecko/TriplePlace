package org.aksw.tripleplace;

public class Triple {

	private Node[] nodes;

	public Triple(Node sIn, Node pIn, Node oIn) {
		nodes = new Node[] { sIn, pIn, oIn };
	}

	public Node[] getNodes() {
		return nodes;
	}
	
}
