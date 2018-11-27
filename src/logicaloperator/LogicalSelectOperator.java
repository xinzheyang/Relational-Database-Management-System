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
	private int totalReductionFactor; //reduction factor calculated from select condition
	private Expression ex;
	
	public LogicalOperator getChildOp() {
		return childOp;
	}
	
	public String getReference() {
		return ((LogicalScanOperator) childOp).getReference();
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
	
	public int computeReductionFactor(String attrib) {
		//TODO: xinqi: implement this
		return -1;
	}
	
	/** Sets the reduction factor of this instance.
	 * @param reductionFactor
	 */
	public void setReductionFactor(int reductionFactor) {
		this.totalReductionFactor = reductionFactor;
	}
	
	/** Gets the relation size, in tuples, of this select operator.
	 * @return
	 * @throws Exception 
	 */
	public int getRelationSize() {
		return ((LogicalScanOperator) childOp).getRelationSize() * totalReductionFactor;
	}
	
	/** Setter just for testing, not allowed to be called for other purposes.
	 * @param newFactor
	 */
	public void setTotalReductionFactor(int newFactor) {
		totalReductionFactor = newFactor;
	}
	
	/** Computes and returns the V-value of this selection on the given attribute
	 * from its base table's V-value on the attribute multiplied by the reduction factor
	 * of this attribute, adding an upper bound of the selection's output relation size.
	 * @param attrib
	 * @return
	 * @throws Exception 
	 */
	public int getVValue(String attrib) {
		//TODO: implement this
		int reductionFactor = computeReductionFactor(attrib);
		return Math.min(getRelationSize(),  ((LogicalScanOperator) childOp).getVValue(attrib) * reductionFactor);
	}

	/* (non-Javadoc)
	 * @see logicaloperator.LogicalOperator#accept(visitor.PhysicalPlanBuilder)
	 */
	@Override
	public void accept(PhysicalPlanBuilder pb) {
		pb.visit(this);
	}

}
