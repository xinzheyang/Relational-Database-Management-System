/**
 * 
 */
package operator;

import java.util.HashMap;

import database.Tuple;

/**
 * @author sitianchen
 *
 */
public class ProjectOperator extends Operator {
	
	public Operator childOp; //child operator of where the source for getNextTuple() comes from.
//	private HashMap<String, Integer> columnIndexMap;

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
