/**
 * 
 */
package bplustree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xinqi
 *
 */
public class IndexNode {
	private List<Integer> addresses;
//	private List<LeafNode> pointers;
	private List<Integer> keys;
	private int min;
	/**
	 * 
	 */
	public IndexNode() {
//		pointers = new ArrayList<>();
		keys = new ArrayList<>();
		addresses = new ArrayList<>();
		min=0;
	}
	
	public void addKey(int i) {
		keys.add(i);
	}
	
//	public void addPointer(LeafNode p) {
//		pointers.add(p);
//	}
	public int getMin() {
		return min;
	}
	
	public List<Integer> getChildAddresses() {
		return addresses;
	}
	
	public List<Integer> getKeys() {
		return keys;
	}

}

