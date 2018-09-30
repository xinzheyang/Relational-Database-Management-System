/**
 * 
 */
package operator;

import java.util.HashMap;

import database.Tuple;
import visitor.EvaluateExpVisitor;
import net.sf.jsqlparser.expression.Expression;

/**
 * @author sitianchen
 *
 */
public class JoinOperator extends Operator {
	
	public Operator leftChild; //left child operator of where the source for getNextTuple() comes from.
	public Operator rightChild; //right child operator of where the source for getNextTuple() comes from.
	public Expression joinCondition;
	public Tuple leftTuple;
	public EvaluateExpVisitor visitor;
	
	public JoinOperator(Operator left, Operator right, Expression condition) {
		leftChild = left;
		rightChild = right;
		
		columnIndexMap = leftChild.getColumnIndexMap(); //same col index map as child operator
		int leftSize = leftChild.getColumnIndexMap().size();
		HashMap<String,Integer> rightMap = rightChild.getColumnIndexMap();
		for(String s: rightMap.keySet()) {
			columnIndexMap.put(s, rightMap.get(s)+leftSize);
		}
		joinCondition = condition;
		leftTuple = leftChild.getNextTuple();
		visitor = new EvaluateExpVisitor();
	}
	
	public JoinOperator(Operator left, Operator right) {
		this(left, right, null);
	}
	/* (non-Javadoc)
	 * @see database.Operator#getNextTuple()
	 */
	@Override
	public Tuple getNextTuple() {
		while(leftTuple != null) {
			Tuple rightTuple = rightChild.getNextTuple();
			while (rightTuple != null) {
				//combine the two tuples and check if they pass the result;
				Tuple combinedTuple = leftTuple.merge(rightTuple);
				if(joinCondition == null) return combinedTuple;
				visitor.setCurrTuple(combinedTuple);
				visitor.setOperator(this);
				joinCondition.accept(visitor);
				boolean result = visitor.getReturnBoolValue();
				if(result) {
					return combinedTuple;
				}
				rightTuple = rightChild.getNextTuple();
			}
			leftTuple = leftChild.getNextTuple();
			rightChild.reset();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see database.Operator#reset()
	 */
	@Override
	public void reset() {
		leftChild.reset();
		rightChild.reset();
		leftTuple = leftChild.getNextTuple();
	}
}
