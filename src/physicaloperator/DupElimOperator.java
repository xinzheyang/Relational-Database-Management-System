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
		assert (child instanceof SortOperator);
		childOp = child;
		columnIndexMap = new HashMap<String, Integer>(childOp.getColumnIndexMap());
	}
	/* Keep getting next tuple until the next tuple is different from the 
	 * prevDistinct tuple or no more tuples can be retrieved.
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
		childOp.reset();

	}
	
	public Operator getChildOp() {
		return childOp;
	}
	
	@Override
	public void reset(int index) {
		throw new UnsupportedOperationException("not supported");
		
	}

}
