/**
 * 
 */
package operator;

import java.util.*;

import database.Tuple;

/**
 * @author sitianchen
 *
 */
public class ScanOperator extends Operator {
	
	/* Upon initialization, opens up a file scan on the appropriate data file
	 * data file.
	 */
	public ScanOperator() {
		
	}

	/* Reads the next line from the file that stores the base table and 
	 * returns the next tuple.
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
	public void dump() {
		// TODO Auto-generated method stub
		
	}
	

}
