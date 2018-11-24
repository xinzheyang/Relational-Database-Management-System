/**
 * 
 */
package joinorderoptimizer;

import java.util.List;

import logicaloperator.LogicalOperator;

/** The cost metric of computing plan costs, dependent on relation size and 
 * stores result in field planCost.
 * @author sitianchen
 *
 */
public class CostMetric {
	public int bestPlanCost;
	public int relationSize;
	public List<LogicalOperator> bestJoinOrder;
	
	public CostMetric(int bestPlanCost, int relationSize, List<LogicalOperator> bestJoinOrder) {
		this.bestPlanCost = bestPlanCost;
		this.relationSize = relationSize;
		this.bestJoinOrder = bestJoinOrder;
	}
	
	public CostMetric() {
		
	}
}
