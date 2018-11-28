/**
 * 
 */
package physicaloperator;

import java.util.HashMap;

import database.Tuple;

/**
 * The Project Operator selects the columns to be projected from its child Operator
 * @author xinqi
 */
public class ProjectOperator extends Operator {
	
	private Operator childOp; //child operator of where the source for getNextTuple() comes from.
//	private HashMap<String, Integer> columnIndexMap;
	private int[] indices;
	
	/**
	 * Initializes the project operator
	 * @param op the child operator of this object
	 * @param cols the names of the columns to be projected
	 */
	public ProjectOperator(Operator op, String[] cols) {
		childOp = op;
		HashMap<String,Integer> map = new HashMap<>();
		indices = new int[cols.length];
		for(int i=0;i<cols.length;i++) {
			map.put(cols[i], i);
			indices[i] = childOp.getColumnIndexMap().get(cols[i]);
		}
		columnIndexMap = map;
	}
	
	
	/* (non-Javadoc)
	 * @see database.Operator#getNextTuple()
	 * Iterate over columns to be projected
	 */
	@Override
	public Tuple getNextTuple() {
		Tuple curr;
		int[] arr=new int[indices.length];
		if((curr=childOp.getNextTuple()) != null) {
			for(int i=0;i<arr.length;i++) {
				arr[i]=curr.getColValues()[indices[i]];
			}
			curr.setColValues(arr);
			return curr;
		}
		return null;
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
