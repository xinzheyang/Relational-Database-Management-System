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

	/* (non-Javadoc)
	 * @see database.Operator#getNextTuple()
	 */
	@Override
	public Tuple getNextTuple() {
		// TODO Auto-generated method stub
		EvaluateExpVisitor eval = new EvaluateExpVisitor();
		this.joinCondition.accept(eval);
		boolean result = eval.getReturnBoolValue();
//		Tuple tup_left = leftChild.getNextTuple();
//		while (tup_left != null) {
//			
//		}
			
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
