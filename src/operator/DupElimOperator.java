/**
 *
 */
package operator;

import java.util.Arrays;
import java.util.HashMap;

import database.Tuple;

/**
 * @author sitianchen
 *
 */
public class DupElimOperator extends Operator {
	private SortOperator childOp; //child operator of where the source for getNextTuple() comes from.
	private Tuple prevDistinct; //the previous distinct tuple

	public DupElimOperator(SortOperator child) {
		childOp = child;
		columnIndexMap = childOp.getColumnIndexMap();
	}
	/* (non-Javadoc)
	 * @see database.Operator#getNextTuple()
	 */
	@Override
	public Tuple getNextTuple() {
		Tuple curr = childOp.getNextTuple();
		if (prevDistinct != null) {
			while(curr != null && Arrays.equals(curr.getColValues(), prevDistinct.getColValues())) {
				curr = childOp.getNextTuple();
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

	/* (non-Javadoc)
	 * @see database.Operator#dump()
	 */
//	@Override
//	public void dump(String fileOut) {
//		// TODO Auto-generated method stub
//
//	}

}
