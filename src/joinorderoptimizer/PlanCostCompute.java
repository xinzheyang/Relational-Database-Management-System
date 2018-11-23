/**
 * 
 */
package joinorderoptimizer;

import java.util.List;

import logicaloperator.LogicalOperator;

/** An instance of this class could be used to compute the plan cost of a left deep
 * join specified in a particular order.
 * @author sitianchen
 *
 */
public class PlanCostCompute {
	
	private List<LogicalOperator> joinOrder; //all logical operators are either scan or select
	
	public PlanCostCompute(List<LogicalOperator> joinOrder) {
		this.joinOrder = joinOrder;
	}
	
	

}
