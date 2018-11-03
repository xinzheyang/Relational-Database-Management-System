/**
 * 
 */
package bplustree;

import java.util.List;

/**
 * @author xinqi
 *
 */
public class LeafNode {
	private List<DataEntry> dataEntries;
	/**
	 * 
	 */
	public LeafNode(List<DataEntry> entries) {
		dataEntries = entries;
	}
	
	public int getMin() {
		return dataEntries.get(0).getKey();
	}
	public List<DataEntry> getEntries() {
		return dataEntries;
	}
}
