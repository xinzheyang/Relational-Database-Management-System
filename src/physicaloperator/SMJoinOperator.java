/**
 * 
 */
package physicaloperator;

import database.Tuple;
import net.sf.jsqlparser.expression.Expression;

/**
 * @author sitianchen
 *
 */
public class SMJoinOperator extends JoinOperator {
	
	public SMJoinOperator(Operator left, Operator right, Expression condition) {
		super(left, right, condition);
		assert(left instanceof SortOperator);
		assert(right instanceof SortOperator);
	}

	/* (non-Javadoc)
	 * @see physicaloperator.Operator#getNextTuple()
	 */
	@Override
	public Tuple getNextTuple() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see physicaloperator.Operator#reset()
	 */
	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset(int index) {
		// TODO Auto-generated method stub
		
	}

}
