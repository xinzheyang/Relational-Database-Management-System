/**
 * 
 */
package operator;

import database.Tuple;

/**
 * @author sitianchen
 *
 */
public class DupElimOperator extends Operator {
	public SortOperator childOp; //child operator of where the source for getNextTuple() comes from.
	
	public DupElimOperator(SortOperator child) {
		childOp = child;
	}
	/* (non-Javadoc)
	 * @see database.Operator#getNextTuple()
	 */
	@Override
	public Tuple getNextTuple() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see database.Operator#reset()
	 */
	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see database.Operator#dump()
	 */
	@Override
	public void dump(String fileOut) {
		// TODO Auto-generated method stub

	}

}
