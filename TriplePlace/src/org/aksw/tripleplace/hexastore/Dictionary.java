package org.aksw.tripleplace.hexastore;

import java.io.IOException;
import java.util.Random;

import org.aksw.tripleplace.Node;

import android.util.Log;

import tokyocabinet.HDB;

/**
 * Mapping of resources and literals to an ID used in the indices
 * 
 * @author natanael
 * 
 */
public class Dictionary {
	private static final String TAG = "Dictionary";
	private HDB dict = new HDB();
	private HDB dictInv = new HDB(); // Is this neccesery or can I open
										// different HDBs with the same instance
	private String pathDir, pathInv;

	public Dictionary(String pathDirIn, String pathInvIn) {
		pathDir = pathDirIn;
		pathInv = pathInvIn;
		Log.v(TAG, "Initialized Dictionary at \"" + pathDir + "\"");
		Log.v(TAG, "Initialized Inverse Dictionary at \"" + pathInv + "\"");
	}

	public long addNode(Node node) throws Exception {
		// find out, if the Node already exists.
		// Search in Dictionary for existing node and apply id
		// else proceed

		if (node.getType() == Node.TYPE_VARIABLE) {
			throw new Exception(
					"The given node is a variable and can't be added to the dictionary");
		}

		if (dict.open(pathDir, HDB.OREADER)) {
			// find resource in hashTable
			try {
				String idString = dict.get(node.getNodeString());
				if (idString != null) {
					long id = Long.parseLong(idString);
					node.setId(id);
					return id;
				}
			} finally {
				dict.close();
			}
		} else {
			// if file not found procede and assume that no entry exists
			if (dict.ecode() != HDB.ENOFILE) {
				throw new IOException(
						"Error on opening Dictionary DB for reading. Error("
								+ dict.ecode() + "): \"" + dict.errmsg() + "\"");
			}
		}

		// add node to dicts
		if (dict.open(pathDir, HDB.OWRITER | HDB.OCREAT)
				&& dictInv.open(pathInv, HDB.OWRITER | HDB.OCREAT)) {
			if (dictInv.rnum() > Long.MAX_VALUE / 2) {
				Log.i(TAG,
						"Please note, that the dictionary is half-full, the probability for collisions is more likely now!");
			}
			Random rand = new Random();
			long key = rand.nextLong();
			try {
				// key 0 is reserved as N/A
				while (key == 0
						|| !dictInv.putkeep(String.valueOf(key),
								node.getNodeString())) {
					if (key == 0 || dictInv.ecode() == HDB.EKEEP) {
						Log.i(TAG,
								"Key kolission on insert of node in dictionary or key was 0. I'm trying another one.");
						key = rand.nextLong();
					} else {
						throw new IOException("Problem adding new Node ("
								+ node.getNodeString()
								+ ") to invers Dictionary. Error("
								+ dictInv.ecode() + "): \"" + dictInv.errmsg()
								+ "\"");
					}
				}
				// Overwrite existing nodes, because it shouldn't happen
				// if it happens the node was orphaned
				if (dict.put(node.getNodeString(), String.valueOf(key))) {
					//Log.i(TAG,
					//		"Successfully added new Node ("
					//				+ node.getNodeString()
					//				+ ") to Dictionary with key = " + key
					//				+ ", dict has now " + dict.rnum()
					//				+ " entries");
					node.setId(key);
					if (dict.rnum() != dictInv.rnum()) {
						Log.w(TAG,
								"The row count of the dictionaries differs: #Dict="
										+ dict.rnum() + " #InvDict="
										+ dictInv.rnum() + "");
					}
				} else {
					// remove entry from inverse dictionary because an error
					// occured
					dictInv.out(String.valueOf(key));
					throw new IOException("Problem adding new Node ("
							+ node.getNodeString() + ") to Dictionary. Error("
							+ dict.ecode() + "): \"" + dict.errmsg() + "\"");
				}
			} catch (IOException e) {
				throw e;
			} finally {
				dict.close();
				dictInv.close();
			}
			return key;
		} else {
			dict.close();
			dictInv.close();
			throw new IOException(
					"Couldn't open Dictionary hash-table for writing. DictError("
							+ dict.ecode() + "): \"" + dict.errmsg()
							+ "\", DictInvError(" + dictInv.ecode() + "): \""
							+ dictInv.errmsg() + "\"");
		}

	}

	public Node getNode(int id) {
		if (dictInv.open(pathInv, HDB.OREADER)) {
			String nodeString = dictInv.get(String.valueOf(id));
			dictInv.close();
			try {
				return new Node(nodeString);
			} catch (Exception e) {
				Log.e(TAG, "Dictionary returned invalide nodeString: \""
						+ nodeString + "\"", e);
				return null;
			}
		} else {
			Log.e(TAG,
					"Couldn't open Dictionary hash-table. Error("
							+ dictInv.ecode() + "): \"" + dictInv.errmsg()
							+ "\"");
			return null;
		}
	}

	/**
	 * Finds a Node matching a given pathern
	 * 
	 * @param node
	 * @return
	 */
	public Node findNode(Node node) {
		return null;
	}

}
