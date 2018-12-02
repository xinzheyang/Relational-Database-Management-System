/**
 * 
 */
package physicaloperator;

import database.Tuple;
import net.sf.jsqlparser.expression.Expression;
import visitor.PhysicalPlanWriter;

/**
 * @author sitianchen
 *
 */
public class HashJoinOperator extends JoinOperator {
	private int bufferNumber;
	public HashJoinOperator(Operator left, Operator right, Expression condition) {
		super(left, right, condition);
		// TODO Auto-generated constructor stub
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
	 * @see physicaloperator.Operator#accept(visitor.PhysicalPlanWriter)
	 */
	@Override
	public void accept(PhysicalPlanWriter write) {
		// TODO Auto-generated method stub

	}

}
