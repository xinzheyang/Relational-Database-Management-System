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
public class IndexNode extends Node {
	private List<Node> children;
//	private List<Integer> addresses;
//	private List<LeafNode> pointers;
//	private List<Integer> keys;
	private int min;
	/**
	 * 
	 */
	public IndexNode() {
//		pointers = new ArrayList<>();
//		keys = new ArrayList<>();
		super();
//		addresses = new ArrayList<>();
		children = new ArrayList<Node>();
		min=0;
	}
	
	public IndexNode(List<Node> children, int address) {
//		super(keys);
		this.children = children;
		this.address = address;
		int numOfChildren = children.size();
		assert numOfChildren > 0;
		min = children.get(0).getMin();
		for (int i = 1; i < numOfChildren; i++) {
			this.keys.add(children.get(i).getMin());
		}
		
	}
	
//	public 
//	public void addKey(int i) {
//		keys.add(i);
//	}
	
//	public void addPointer(LeafNode p) {
//		pointers.add(p);
//	}
	public int getMin() {
		return min;
	}
	
	public List<Integer> getChildAddresses() {
		List<Integer> addresses = new ArrayList<Integer>();
		for (Node c : children) {
			addresses.add(c.address);
		}
		return addresses;
	}
	
	public List<Integer> getKeys() {
		return keys;
	}

}

