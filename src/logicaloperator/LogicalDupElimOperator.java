/**
 * 
 */
package logicaloperator;

import visitor.PhysicalPlanBuilder;

/**
 * @author xinzheyang
 *
 */
public class LogicalDupElimOperator extends LogicalOperator {
	private LogicalSortOperator childOp;
	/**
	 * 
	 */
	public LogicalDupElimOperator(LogicalSortOperator child) {
		childOp = child;
	}

	public LogicalSortOperator getChildOp() {
		return childOp;
	}


	/* (non-Javadoc)
	 * @see logicaloperator.LogicalOperator#accept(database.PhysicalPlanBuilder)
	 */
	@Override
	public void accept(PhysicalPlanBuilder pb) {
		pb.visit(this);
	}

}
