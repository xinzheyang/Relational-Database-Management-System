/**
 * 
 */
package logicaloperator;

import net.sf.jsqlparser.expression.Expression;
import visitor.PhysicalPlanBuilder;

/**
 * @author xinzheyang
 * An logical operator that stores minimal information
 * to be visited by PhysicalPlanBuilder to transform into a physical plan builder
 */
public class LogicalSelectOperator extends LogicalOperator {
	private LogicalOperator childOp;
	public LogicalOperator getChildOp() {
		return childOp;
	}

	public Expression getEx() {
		return ex;
	}

	private Expression ex;
	/**
	 * 
	 */
	public LogicalSelectOperator(LogicalOperator child, Expression exp) {
		childOp = child;
		ex = exp;
	}

	/* (non-Javadoc)
	 * @see logicaloperator.LogicalOperator#accept(visitor.PhysicalPlanBuilder)
	 */
	@Override
	public void accept(PhysicalPlanBuilder pb) {
		pb.visit(this);
	}

}
