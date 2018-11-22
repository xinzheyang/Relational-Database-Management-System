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
public class LogicalJoinOperator extends LogicalOperator {
	private LogicalOperator leftChild;
	private LogicalOperator rightChild;
	private Expression joinCondition;
	/**
	 * 
	 */
//	public LogicalJoinOperator(LogicalOperator left, LogicalOperator right) {
//		leftChild = left;
//		rightChild = right;
//	}
	
	public LogicalJoinOperator(LogicalOperator left, LogicalOperator right, Expression condition) {
		leftChild = left;
		rightChild = right;
		joinCondition = condition;
	}

	public LogicalOperator getLeftChild() {
		return leftChild;
	}

	public LogicalOperator getRightChild() {
		return rightChild;
	}

	public Expression getJoinCondition() {
		return joinCondition;
	}

	/* (non-Javadoc)
	 * @see logicaloperator.LogicalOperator#accept(visitor.PhysicalPlanBuilder)
	 */
	@Override
	public void accept(PhysicalPlanBuilder pb) {
		pb.visit(this);
	}

}
