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
	private int reductionFactor; //reduction factor calculated from select condition
	private Expression ex;
	
	public LogicalOperator getChildOp() {
		return childOp;
	}

	public Expression getEx() {
		return ex;
	}
	
	/**
	 * 
	 */
	public LogicalSelectOperator(LogicalOperator child, Expression exp) {
		childOp = child;
		ex = exp;
	}
	
	/** Sets the reduction factor of this instance.
	 * @param reductionFactor
	 */
	public void setReductionFactor(int reductionFactor) {
		this.reductionFactor = reductionFactor;
	}
	
	/** Gets the relation size of this select operator.
	 * @return
	 * @throws Exception 
	 */
	public int getRelationSize() throws Exception {
		return ((LogicalScanOperator) childOp).getRelationSize() * reductionFactor;
	}

	/* (non-Javadoc)
	 * @see logicaloperator.LogicalOperator#accept(visitor.PhysicalPlanBuilder)
	 */
	@Override
	public void accept(PhysicalPlanBuilder pb) {
		pb.visit(this);
	}

}
