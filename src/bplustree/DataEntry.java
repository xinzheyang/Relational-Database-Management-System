/**
 * 
 */
package bplustree;

import java.util.Comparator;
import java.util.List;

import database.Tuple;

/**
 * @author xinqi
 *
 */
public class DataEntry {
	private int key;
	private List<int[]> rid;
	
	/**
	 * 
	 */
	public DataEntry(int k, List<int[]> r) {
		// TODO Auto-generated constructor stub
		key = k;
		rid = r;
		rid.sort(new Comparator<int []>() {
			@Override
			public int compare(int[] a, int[] b) {
				int i=0;
		        while(i<a.length) {
		        	if(a[i] < b[i]) return -1;
		        	else if(a[i] > b[i]) return 1;
		        	i++;
		        }
		        return 0;
			}
		});
	}
	
	public int getKey() {
		return key;
	}

}
