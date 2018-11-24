/**
 * 
 */
package joinorderoptimizer;

import java.util.List;

import logicaloperator.LogicalOperator;
import net.sf.jsqlparser.expression.Expression;

/** An instance of this class could be used to compute the plan cost of a left deep
 * join specified in a particular order.
 * @author sitianchen
 *
 */
public class PlanCostCompute {
	
	private List<LogicalOperator> joinOrder; //all logical operators are either scan or select
	private List<Expression> joinConditions; //join conditions of left deep joins in order
	private int planCost;
	private int relationSize;
	
	public PlanCostCompute(List<LogicalOperator> joinOrder) {
		this.joinOrder = joinOrder;
	}
	
	/** Computes recursively the plan cost of this instance from the cost of its left child
	 * (joinOrder[:-1]) plus the size of the relation produced as the result of 
	 * the left child.
	 */
	public void computePlanCost() {
		if (joinOrder.size() <= 2) {
			planCost = 0;
			if (joinOrder.size() == 2) {
				JoinSizeCompute comp = new JoinSizeCompute(joinOrder.get(0), joinOrder.get(1), joinConditions.get(0));
				relationSize = comp.computeJoinSize();
			}
			return;
		}
		PlanCostCompute leftCompute = new PlanCostCompute(joinOrder.subList(0, joinOrder.size() - 1));
		leftCompute.computePlanCost();
		planCost = leftCompute.getPlanCost() + leftCompute.getJoinRelationSize();
		//TODO: calculate relation size of current join order
	}
	
	/** Computes the intermediate relation size of left-deep-joining the first n-1 tables as
	 * the left relation and the nth table as the right relation. Stores the result in the 
	 * relationSize field.
	 */
	public void computeJoinSize() {
		relationSize = 0;
	}
	
	public int getPlanCost() {
		return planCost;
	}
	
	public int getJoinRelationSize() {
		return relationSize;
	}
}
