/**
 *
 */
package physicaloperator;

import java.util.Arrays;
import java.util.HashMap;

import database.Tuple;

/** 
 * @author sitianchen
 * Operator for SELECT DISTINCT [...]. The child operator has to be a SortOperator. Duplicates
 * are removed for all tuples.
 */
public class DupElimOperator extends Operator {
	private Operator childOp; //child operator of where the source for getNextTuple() comes from.
	private Tuple prevDistinct; //the previous distinct tuple

	public DupElimOperator(Operator child) {
		childOp = child;
		columnIndexMap = childOp.getColumnIndexMap();
	}
	/* Keep getting next tuple until the next tuple is different from the 
	 * prevDistinct tuple or no more tuples can be retrieved.
	 */
	@Override
	public Tuple getNextTuple() {
//		System.out.println(childOp.getIndex());
		Tuple curr = childOp.getNextTuple();
//		System.out.println(curr);
		if (prevDistinct != null) {
			while(curr != null && Arrays.equals(curr.getColValues(), prevDistinct.getColValues())) {
				curr = childOp.getNextTuple();
//				System.out.println(curr);
			}
		}
		prevDistinct = curr;
		return curr;
	}

	/* (non-Javadoc)
	 * @see database.Operator#reset()
	 */
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		childOp.reset();

	}
	@Override
	public void reset(int index) {
		// TODO Auto-generated method stub
		
	}

}
