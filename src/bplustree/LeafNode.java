/**
 * 
 */
package bplustree;

import java.util.List;

/**
 * @author xinqi
 * The class that represent the leaf node in the B+ tree containing data entries
 */
public class LeafNode extends Node {
//	private List<DataEntry> dataEntries;
	private List<List<int[]>> listOfRids; //shares the same indices as this.keys
	/**
	 * 
	 */
//	public LeafNode(List<DataEntry> entries) {
//		super();
////		dataEntries = entries;
//	}
	
	/**
	 * @param keys the list of keys the node contains
	 * @param listOfRids the list of record ids the node contains
	 * @param address the number of the page of the node
	 */
	public LeafNode(List<Integer> keys, List<List<int[]>> listOfRids, int address) {
		super(keys, address);
		assert keys.size() == listOfRids.size();
		this.listOfRids = listOfRids;
	}
	
	
	/**
	 * @return the list of record ids
	 */
	public List<List<int[]>> getListOfRids() {
		return listOfRids;
	}
	
	/* (non-Javadoc)
	 * @see bplustree.Node#getMin()
	 */
	public int getMin() {
//		return dataEntries.get(0).getKey();
		return this.keys.size() > 0 ? keys.get(0) : null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String prefix = "LeafNode[\n";
		StringBuilder build = new StringBuilder(prefix);
		for (int i = 0; i < keys.size(); i++) {
			build.append("<[");
			build.append(keys.get(i));
			build.append(":");
			for (int[] rid : listOfRids.get(i)) {
				build.append("(");
				build.append(rid[0]);
				build.append(",");
				build.append(rid[1]);
				build.append(")");
			}
			build.append("]>\n");
		}
		build.append("]\n");
		return build.toString();
	}
//	public List<DataEntry> getEntries() {
//		return dataEntries;
//	}
}
