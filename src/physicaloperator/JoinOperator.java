/**
 * 
 */
package physicaloperator;

import java.util.HashMap;

import database.Tuple;
import net.sf.jsqlparser.expression.Expression;
import visitor.EvaluateExpVisitor;
import visitor.PhysicalPlanWriter;

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
	
	/**
	 * @return the left child of the operator
	 */
	public Operator getLeftChild() {
		return leftChild;
	}
	
	/**
	 * @return the right child of the operator
	 */
	public Operator getRightChild() {
		return rightChild;
	}

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
	
	/**
	 * @return the condition expression for this join
	 */
	public Expression getCondition() {
		return joinCondition;
	}

}
