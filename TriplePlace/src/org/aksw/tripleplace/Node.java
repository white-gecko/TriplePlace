package org.aksw.tripleplace;

import android.util.Log;

/**
 * A RDF-Node. Can be a named resource, a blind resource, a typed literal, a
 * literal with specified language or a general literal
 * 
 * @author natanael
 * 
 */
public class Node {
	private static final String TAG = "Node";

	/**
	 * A simple literal
	 */
	public static final int TYPE_LITERAL = 1;

	/**
	 * A resource described by an IRI (URI, URL) also known as IRI type
	 */
	public static final int TYPE_NAMED_RESOURCE = 2;

	/**
	 * A resources without identifier also known as Blanknode type
	 */
	public static final int TYPE_BLANK_RESOURCE = 3;

	/**
	 * A literal with specified data type
	 */
	public static final int TYPE_TYPED_LITERAL = 4;

	/**
	 * A literal with specified language
	 */
	public static final int TYPE_LANG_LITERAL = 5;
	
	/**
	 * A literal with specified language
	 */
	public static final int TYPE_VARIABLE = 10;

	/**
	 * The ID of this node in the dictionary
	 */
	private long id = 0;
	private int type;
	private String value;

	/**
	 * Is only set for TYPE_TYPED_LITERAL
	 */
	private String dataType;

	/**
	 * Is only set for TYPE_LANG_LITERAL
	 */
	private String lang;

	public Node(String nodeString) throws Exception {
		// parse nodeString
		int length = nodeString.length();
		
		Log.d(TAG, "Creating new Node with nodeString: \"" + nodeString + "\"");
		
		switch (nodeString.charAt(0)) {
		case '<':
			// Resource
			type = TYPE_NAMED_RESOURCE;
			value = nodeString.substring(1, length - 1);
			Log.d(TAG, "Type " + type + " value: \"" + value + "\"");
			return;
		case '_':
			// BNode
			type = TYPE_BLANK_RESOURCE;
			value = nodeString.substring(2, length);
			Log.d(TAG, "Type " + type + " value: \"" + value + "\"");
			return;
		case '?':
			// Variable
			type = TYPE_VARIABLE;
			value = nodeString.substring(1, length);
			Log.d(TAG, "Type " + type + " value: \"" + value + "\"");
			return;
		case '"':
			// Literal
			int split;
			switch (nodeString.charAt(length - 1)) {
			case '>':
				// Typed Literal
				type = TYPE_TYPED_LITERAL;
				split = nodeString.lastIndexOf("\"^^<");
				if (split < 0) {
					Log.v(TAG, "Unrecognized RDF-Type in string: \"" + nodeString
					+ "\"");
					throw new Exception("Unrecognized RDF-Type in string: \"" + nodeString
					+ "\"");
				}
				value = nodeString.substring(1, split - 1);
				dataType = nodeString.substring(split + 3, length - 1);
				Log.d(TAG, "Type " + type + " value: \"" + value + "\", dataType: \"" + dataType + "\"");
				return;
			case '"':
				// Literal
				type = TYPE_LITERAL;
				value = nodeString.substring(1, length - 1);
				Log.d(TAG, "Type " + type + " value: \"" + value + "\"");
				return;
			default:
				// Language Literal
				type = TYPE_LANG_LITERAL;
				split = nodeString.lastIndexOf("\"@");
				if (split < 0) {
					Log.v(TAG, "Unrecognized RDF-Type in string: \"" + nodeString
					+ "\"");
					throw new Exception("Unrecognized RDF-Type in string: \"" + nodeString
					+ "\"");
				}
				value = nodeString.substring(1, split-1);
				lang = nodeString.substring(split+3, length - 1);
				Log.d(TAG, "Type " + type + " value: \"" + value + "\", dataType: \"" + lang + "\"");
				return;
			}
		default:
			Log.e(TAG, "Unrecognized RDF-Type in string: \"" + nodeString
					+ "\"");
		}
	}

	public String getNodeString() {
		switch (type) {
		case TYPE_NAMED_RESOURCE:
			return "<" + value + ">";
		case TYPE_BLANK_RESOURCE:
			return "_:" + value;
		case TYPE_TYPED_LITERAL:
			return "\"" + value + "\"^^<" + dataType + ">";
		case TYPE_LANG_LITERAL:
			return "\"" + value + "\"@" + lang;
		case TYPE_LITERAL:
			return "\"" + value + "\"";
		case TYPE_VARIABLE:
			return "?" + value;
		default:
			return null;
		}
	}

	public long getId() {
		return id;
	}

	public void setId(long idIn) {
		id = idIn;
	}
	
	public int getType() {
		return type;
	}
}
