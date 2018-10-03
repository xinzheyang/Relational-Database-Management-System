/**
 * 
 */
package operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import database.Tuple;

/**
 * @author xinqi
 * SortOperator gets all the tuples from its child operator, put them in a list,
 * and sort them according to the order passed in as the column names list.
 *
 */
public class SortOperator extends Operator {
	
	private Operator childOp; //child operator of where the source for getNextTuple() comes from.
//	private HashMap<String, Integer> columnIndexMap
	private List<Integer> colIndexes=new ArrayList<>();
	private List<Tuple> allTuples=new ArrayList<>();
	private int index;
	
	/**
	 * Initializes the sort operator
	 * @param op The parent operator
	 * @param cols array containing the names of the columns to sort by
	 */
	public SortOperator(Operator op, String[] cols) {
		childOp=op;
//		List<Integer> =new ArrayList<>();
//		colIndexes = new int[cols.length];
		columnIndexMap = new HashMap<String, Integer>(childOp.getColumnIndexMap()); //same col index map as child operator
		for(int i=0;i<cols.length;i++) {
			colIndexes.add(columnIndexMap.get(cols[i]));
		}
		for(int i=0;i<columnIndexMap.size();i++) {
			if(!colIndexes.contains(i)) {
				colIndexes.add(i);
			}
		}
		Tuple tuple;
		while((tuple = childOp.getNextTuple()) != null) {
			allTuples.add(tuple);
		}
		Comparator<Tuple> com = Tuple.getComparator(colIndexes);
		Collections.sort(allTuples, com);
		index=0;
		
	}
	/* (non-Javadoc)
	 * @see database.Operator#getNextTuple()
	 */
	@Override
	public Tuple getNextTuple() {
		if (index < allTuples.size()) {
			return allTuples.get(index++);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see database.Operator#reset()
	 */
	@Override
	public void reset() {
		childOp.reset();
		index=0;
	}

}
