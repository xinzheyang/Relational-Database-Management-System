/**
 * 
 */
package physicaloperator;

import java.util.ArrayList;
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
public abstract class SortOperator extends Operator {
	
	protected Operator childOp; //child operator of where the source for getNextTuple() comes from.
	protected List<Integer> colIndexes=new ArrayList<>();
	protected int index;
	Comparator<Tuple> com;
	
	/**
	 * Initializes the sort operator
	 * @param op The parent operator
	 * @param cols array containing the names of the columns to sort by
	 */
	public SortOperator(Operator op, String[] cols) {
		childOp=op;
		columnIndexMap = new HashMap<String, Integer>(childOp.getColumnIndexMap()); //same col index map as child operator
		for(int i=0;i<cols.length;i++) {
			colIndexes.add(columnIndexMap.get(cols[i]));
		}
		for(int i=0;i<columnIndexMap.size();i++) {
			if(!colIndexes.contains(i)) {
				colIndexes.add(i);
			}
		}
		com = Tuple.getComparator(colIndexes);
	}
	
//	public int getIndex() {
//		return index;
//	}
//	/* (non-Javadoc)
//	 * @see database.Operator#getNextTuple()
//	 */
//	@Override
//	public Tuple getNextTuple() {
//		if (index < allTuples.size()) {
//			Tuple cur = allTuples.get(index++);
//			return cur;
//		}
//		return null;
//	}
//
//	/* (non-Javadoc)
//	 * @see database.Operator#reset()
//	 */
//	@Override
//	public void reset() {
//		childOp.reset();
//		index=0;
//	}
//
//	@Override
//	public void reset(int index) {
//		// TODO Auto-generated method stub
//		
//	}

}
