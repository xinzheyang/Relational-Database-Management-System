/**
 * 
 */
package bplustree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sitianchen
 * The abstract class for the index nodes and leaf nodes
 */
public abstract class Node {
	protected List<Integer> keys;
	protected int address;
	
	public Node() {
		keys = new ArrayList<Integer>();
	}
	
	/**
	 * Construct the Node object
	 * @param keys the list of keys of the index
	 * @param address the number of the page in the file the node is on
	 */
	public Node(List<Integer> keys, int address) {
		this.keys = keys;
		this.address = address;
	}
	
	/**
	 * @return the list of keys of the index in the file
	 */
	public List<Integer> getKeys() {
		return this.keys;
	}
	
	/**
	 * @return the minimum key value that appears in the whole subtree of the node
	 */
	public abstract int getMin();

}
