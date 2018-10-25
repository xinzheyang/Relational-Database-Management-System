/**
 * 
 */
package physicaloperator;

import java.util.HashMap;

import database.Tuple;
import net.sf.jsqlparser.expression.Expression;
import visitor.EvaluateExpVisitor;

/** An abstract class that all physical join operators inherits.
 * @author sitianchen
 *
 */
public abstract class JoinOperator extends Operator {
	
	protected Operator leftChild; //left child operator of where the source for getNextTuple() comes from.
	protected Operator rightChild; //right child operator of where the source for getNextTuple() comes from.
	protected Expression joinCondition;
	protected EvaluateExpVisitor visitor;
	
	public JoinOperator(Operator left, Operator right, Expression condition) {
		leftChild = left;
		rightChild = right;
		columnIndexMap = new HashMap<String, Integer>(leftChild.getColumnIndexMap()); //same col index map as child operator
		int leftSize = leftChild.getColumnIndexMap().size();
		HashMap<String,Integer> rightMap = rightChild.getColumnIndexMap();
		for(String s: rightMap.keySet()) {
			columnIndexMap.put(s, rightMap.get(s)+leftSize);
		}
		joinCondition = condition;
		
		visitor = new EvaluateExpVisitor();
	}

	/* (non-Javadoc)
	 * @see physicaloperator.Operator#getNextTuple()
	 */
//	@Override
//	public Tuple getNextTuple() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
	/* (non-Javadoc)
	 * @see physicaloperator.Operator#reset()
	 */
	@Override
	public void reset() {
		leftChild.reset();
		rightChild.reset();

	}
//
//	/* (non-Javadoc)
//	 * @see physicaloperator.Operator#reset(int)
//	 */
//	@Override
	public void reset(int index) {
		throw new UnsupportedOperationException("not supported");

	}

}
