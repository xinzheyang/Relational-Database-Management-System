/**
 * 
 */
package joinorderoptimizer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import logicaloperator.LogicalOperator;
import net.sf.jsqlparser.schema.Column;

/** The cost metric of computing plan costs, dependent on relation size and 
 * stores result in field planCost.
 * @author sitianchen
 *
 */
public class CostMetric {
	public int planCost;
	public int relationSize;
	public List<LogicalOperator> bestJoinOrder; //best order of the cost metric
	public HashMap<HashSet<Column>, Integer> vValueMap; //mapping v-values of a set of united columns to their shared v-value
	//to be used for memoizing v-values of intermediate joins
	
	public CostMetric(int bestPlanCost, int relationSize, List<LogicalOperator> bestJoinOrder) {
		this.planCost = bestPlanCost;
		this.relationSize = relationSize;
		this.bestJoinOrder = bestJoinOrder;
		vValueMap = new HashMap<HashSet<Column>, Integer>();
	}
	
	public CostMetric() {
		bestJoinOrder = new LinkedList<LogicalOperator>();
		vValueMap = new HashMap<HashSet<Column>, Integer>();
	}
	
	/** Gets the v-value of an attribute from this cost metric instance's
	 * v-value map if exists, else returns -1.
	 * @param attrib
	 * @return
	 */
	public int getVValue(Column attrib) {
		for (HashSet<Column> key : vValueMap.keySet()) {
			if (key.contains(attrib)) {
				return vValueMap.get(key);
			}
		}
		return -1;
	}
}
