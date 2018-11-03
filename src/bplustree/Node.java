/**
 * 
 */
package bplustree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sitianchen
 *
 */
public abstract class Node {
	protected List<Integer> keys;
	protected int address;
	
	public Node() {
		keys = new ArrayList<Integer>();
	}
	
	public Node(List<Integer> keys, int address) {
		this.keys = keys;
		this.address = address;
	}
	
	public List<Integer> getKeys() {
		return this.keys;
	}
	
	public abstract int getMin();

}
