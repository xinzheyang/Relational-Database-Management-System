/**
 * 
 */
package bplustree;

import java.util.List;

/**
 * @author xinqi
 *
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
	
	public LeafNode(List<Integer> keys, List<List<int[]>> listOfRids, int address) {
		super(keys, address);
		assert keys.size() == listOfRids.size();
		this.listOfRids = listOfRids;
	}
	
	
	public List<List<int[]>> getListOfRids() {
		return listOfRids;
	}
	
	public int getMin() {
//		return dataEntries.get(0).getKey();
		return this.keys.size() > 0 ? keys.get(0) : null;
	}
//	public List<DataEntry> getEntries() {
//		return dataEntries;
//	}
}
