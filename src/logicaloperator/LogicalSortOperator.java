/**
 * 
 */
package logicaloperator;

import visitor.PhysicalPlanBuilder;

/**
 * @author xinzheyang
 * An logical operator that stores minimal information
 * to be visited by PhysicalPlanBuilder to transform into a physical plan builder
 */
public class LogicalSortOperator extends LogicalOperator {
	private LogicalOperator childOp;
	private String[] cols;
 	/**
	 * 
	 */
	public LogicalSortOperator(LogicalOperator child, String[] columns) {
		childOp = child;
		cols = columns;
	}

	public LogicalOperator getChildOp() {
		return childOp;
	}

	public String[] getCols() {
		return cols;
	}

	/* (non-Javadoc)
	 * @see logicaloperator.LogicalOperator#accept(database.PhysicalPlanBuilder)
	 */
	@Override
	public void accept(PhysicalPlanBuilder pb) {
		pb.visit(this);
	}

}
