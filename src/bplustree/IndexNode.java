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
	
	public String toString() {
		String prefix = "IndexNode with keys [";
		String middle = "] and child addresses [";
		StringBuilder build = new StringBuilder(prefix);
		for (int i = 0; i < keys.size(); i++) {
			build.append(keys.get(i));
			if (i < keys.size() - 1 )
				build.append(", ");
		}
		build.append(middle);
		List<Integer> addresses = getChildAddresses();
		for (int i = 0; i < addresses.size(); i++) {
			build.append(addresses.get(i));
			if (i < addresses.size() - 1 )
				build.append(", ");
		}
		build.append("]\n");
		return build.toString();
	}

}

