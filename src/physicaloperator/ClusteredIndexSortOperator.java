/**
 * 
 */
package physicaloperator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import database.Tuple;

/**
 * @author sitianchen
 *
 */
public class ClusteredIndexSortOperator extends Operator {
//	private ScanOperator childOp;
//	private String searchKey;
	private LinkedList<Tuple> allTuples;
	private int index;
	
	/** Uses unbounded state to get all tuples in the relation and sort them with the 
	 * given search key value and their pageID, tupleID (which is just the order
	 * of occurrence).  
	 * @param childOp
	 * @param searchKey
	 */
	public ClusteredIndexSortOperator(ScanOperator childOp, String searchKey) {
		HashMap<Integer, List<Tuple>> keyMap = new HashMap<Integer, List<Tuple>>();
		Tuple tuple;
		columnIndexMap = childOp.getColumnIndexMap();
		allTuples = new LinkedList<Tuple>();
		
		int keyIndex = columnIndexMap.get(childOp.getReference() + "." + searchKey);
		while((tuple = childOp.getNextTuple()) != null) {
			int colVal = tuple.getColumnValue(keyIndex);
			if (keyMap.containsKey(colVal)) {
				keyMap.get(colVal).add(tuple);
			}
			else {
				List<Tuple> newLst = new LinkedList<Tuple>();
				newLst.add(tuple);
				keyMap.put(colVal, newLst);
			}
		}
		List<Integer> keyValues = new ArrayList<Integer>(keyMap.keySet());
		Collections.sort(keyValues);
		for (Integer key : keyValues) {
			allTuples.addAll(keyMap.get(key));
		}
		index = 0;
	}

	/* (non-Javadoc)
	 * @see physicaloperator.Operator#getNextTuple()
	 */
	@Override
	public Tuple getNextTuple() {
		if (index < allTuples.size()) {
			Tuple cur = allTuples.get(index++);
			return cur;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see physicaloperator.Operator#reset()
	 */
	@Override
	public void reset() {

	}

	/* (non-Javadoc)
	 * @see physicaloperator.Operator#reset(int)
	 */
	@Override
	public void reset(int index) {

	}

}
