/**
 * 
 */
package operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import database.Tuple;

/**
 * @author sitianchen
 *
 */
public class SortOperator extends Operator {
	
	private Operator childOp; //child operator of where the source for getNextTuple() comes from.
//	private HashMap<String, Integer> columnIndexMap
	private int[] colIndexes;
	private List<Tuple> allTuples=new ArrayList<>();
	
	public SortOperator(Operator op, String[] cols) {
		childOp=op;
		colNames=cols;
		Tuple tuple;
		while((tuple = childOp.getNextTuple()) != null) {
			allTuples.add(tuple);
		}
		TupleComparator com = new TupleComparator() 
		Collections.sort(allTuples,);
		
	}
	/* (non-Javadoc)
	 * @see database.Operator#getNextTuple()
	 */
	@Override
	public Tuple getNextTuple() {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see database.Operator#reset()
	 */
	@Override
	public void reset() {
		childOp.reset();
	}

}
