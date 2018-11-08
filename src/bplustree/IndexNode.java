/**
 * 
 */
package bplustree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xinqi
 * The class represents the index node in the B+ tree.
 */
public class IndexNode extends Node {
	private List<Node> children;
	private int min;
	

	/**
	 * Constructor of an empty index node
	 */
	public IndexNode() {
		super();
		children = new ArrayList<Node>();
		min=0;
	}
	
	/**
	 * Constructor given the children and address
	 * @param children all the nodes that it points to
	 * @param address the number of page the node is on
	 */
	public IndexNode(List<Node> children, int address) {
		this.children = children;
		this.address = address;
		int numOfChildren = children.size();
		assert numOfChildren > 0;
		min = children.get(0).getMin();
		for (int i = 1; i < numOfChildren; i++) {
			this.keys.add(children.get(i).getMin());
		}
	}
	
	/* (non-Javadoc)
	 * @see bplustree.Node#getMin()
	 */
	public int getMin() {
		return min;
	}
	
	/**
	 * @return the list of addresses of all children nodes
	 */
	public List<Integer> getChildAddresses() {
		List<Integer> addresses = new ArrayList<Integer>();
		for (Node c : children) {
			addresses.add(c.address);
		}
		return addresses;
	}
	
	/* (non-Javadoc)
	 * @see bplustree.Node#getKeys()
	 */
	public List<Integer> getKeys() {
		return keys;
	}
	
	/** Returns the human-readable string representation of this node.
	 * @see java.lang.Object#toString()
	 */
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

