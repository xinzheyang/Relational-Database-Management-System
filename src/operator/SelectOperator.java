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
public class SelectOperator extends Operator {
	
	public Operator childOp; //child operator of where the source for getNextTuple() comes from.
	private HashMap<String, Integer> columnIndexMap;

	/* Grabs the next tuple from the scan and check if that tuple passes the 
	 * selection condition, and if so output it. If the tuple doesn’t pass 
	 * the selection condition, the selection operator will continue pulling 
	 * tuples from the scan until either it finds one that passes or it receives 
	 * null (i.e. the scan runs out of output).
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

	@Override
	public void dump(String fileOut) {
		// TODO Auto-generated method stub
		
	}

}
