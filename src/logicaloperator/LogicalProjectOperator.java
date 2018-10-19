/**
 * 
 */
package logicaloperator;

import visitor.PhysicalPlanBuilder;

/**
 * @author xinzheyang
 *
 */
public class LogicalProjectOperator extends LogicalOperator {
	private LogicalOperator childOp;
	private String[] cols;
	public LogicalOperator getChildOp() {
		return childOp;
	}

	public String[] getCols() {
		return cols;
	}

	/**
	 * 
	 */
	public LogicalProjectOperator(LogicalOperator child, String[] columns) {
		childOp = child;
		cols = columns;
	}

	/* (non-Javadoc)
	 * @see logicaloperator.LogicalOperator#accept(visitor.PhysicalPlanBuilder)
	 */
	@Override
	public void accept(PhysicalPlanBuilder pb) {
		pb.visit(this);
	}

}
