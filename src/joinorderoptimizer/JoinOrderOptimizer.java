/**
 * 
 */
package joinorderoptimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import logicaloperator.LogicalJoinOperator;
import logicaloperator.LogicalOperator;
import logicaloperator.LogicalScanOperator;
import logicaloperator.LogicalSelectOperator;
import net.sf.jsqlparser.expression.Expression;

/**
 * @author sitianchen
 *
 */
public class JoinOrderOptimizer {
	
	private LogicalJoinOperator joinOp;
	private List<HashMap<HashSet<LogicalOperator>, CostMetric>> subsetCostMetrics; //cost and relation size of best plan for all subsets 
	private List<LogicalOperator> finalOrder;
	
	/**
	 * @param joinOperator
	 * @throws Exception
	 */
	public JoinOrderOptimizer(LogicalJoinOperator joinOperator) throws Exception {
		joinOp = joinOperator;
		subsetCostMetrics = new ArrayList<HashMap<HashSet<LogicalOperator>, CostMetric>>();
		subsetCostMetrics.add(null); //initialize 0th denoting size 0 subset, and is null
		HashMap<HashSet<LogicalOperator>, CostMetric> sizeOneMap = new HashMap<HashSet<LogicalOperator>, CostMetric>();
		//initialize cost metric for size one subsets
		for (LogicalOperator child : joinOp.getJoinChildren()) {
			
			CostMetric currMetric = new CostMetric();
			currMetric.bestPlanCost = 0; //plan cost for single table is 0
			currMetric.bestJoinOrder = new ArrayList<LogicalOperator>();
			currMetric.bestJoinOrder.add(child);
			
			if (child instanceof LogicalScanOperator) {
				currMetric.relationSize = ((LogicalScanOperator) child).getRelationSize();
			} else if (child instanceof LogicalSelectOperator) {
				currMetric.relationSize = ((LogicalSelectOperator) child).getRelationSize();
			}
			
			HashSet<LogicalOperator> opList = new HashSet<LogicalOperator>();
			opList.add(child);
			sizeOneMap.put(opList, currMetric);
		}	
		subsetCostMetrics.add(sizeOneMap);
	}
	
	/** Chooses the best join order of the instance's logical operator by the bottom-up
	 * dynamic programming approach.
	 */
	private void dpChooseBestPlan() {
		//TODO: implement dp bottom-up.
		List<LogicalOperator> allChildren = joinOp.getJoinChildren();
		List<HashSet<LogicalOperator>> subsetsBySize = enumerateSubsets(allChildren);
		for (int i = 2; i < subsetsBySize.size(); i++) {
			
			int bestPlanCost = Integer.MAX_VALUE;
			int relationSize = 0;
			LogicalOperator optimalRightOp = null;
			List<LogicalOperator> leftBestOrder = null;
			
			for(LogicalOperator op : subsetsBySize.get(i)) {
				HashSet<LogicalOperator> leftRelations = new HashSet<LogicalOperator>(subsetsBySize.get(i));
				leftRelations.remove(op);
				CostMetric lastBest = subsetCostMetrics.get(i - 1).get(leftRelations);
				PlanCostCompute comp = new PlanCostCompute(lastBest, op);
				comp.computePlanCost();
				comp.computeJoinSize();
				if (comp.getPlanCost() < bestPlanCost) {
					bestPlanCost = comp.getPlanCost();
					relationSize = comp.getJoinRelationSize();
					optimalRightOp = op;
					leftBestOrder = new ArrayList<LogicalOperator>(lastBest.bestJoinOrder);
				}
			}
			
			leftBestOrder.add(optimalRightOp);
			CostMetric curBest = new CostMetric(bestPlanCost, relationSize, leftBestOrder);
			if (subsetCostMetrics.size() == i) 
				subsetCostMetrics.add(new HashMap<HashSet<LogicalOperator>, CostMetric>()); //curr level not yet initialized
			subsetCostMetrics.get(i).put(subsetsBySize.get(i), curBest);
		}
		
		finalOrder = subsetCostMetrics.get(allChildren.size()).get(new HashSet<LogicalOperator>(allChildren)).bestJoinOrder;
	}
	
	private List<HashSet<LogicalOperator>> enumerateSubsets(List<LogicalOperator> lst) {
		List<HashSet<LogicalOperator>> subsets = new ArrayList<HashSet<LogicalOperator>>(lst.size() + 1);
		enumerateSubsets(lst, new HashSet<LogicalOperator>(), subsets, 0);
		return subsets;
	}
	
	private void enumerateSubsets(List<LogicalOperator> lst, HashSet<LogicalOperator> tmp, List<HashSet<LogicalOperator>> res, int currElemIndex) {
		if (currElemIndex >= lst.size()) {
			res.set(tmp.size(), tmp);
			return;
		}
		tmp.add(lst.get(currElemIndex));
		enumerateSubsets(lst, tmp, res, currElemIndex + 1);
		tmp.remove(lst.get(currElemIndex));
		enumerateSubsets(lst, tmp, res, currElemIndex + 1);
	}
	
	private class PlanCostCompute {
		
		private CostMetric leftRelations; //all logical operators are either scan or select
		private LogicalOperator rightOp;
//		private List<Expression> joinCondition; //join conditions of left deep joins in order
		private int planCost;
		private int relationSize;
		
		public PlanCostCompute(CostMetric leftRelations, LogicalOperator rightOp) {
			this.leftRelations = leftRelations;
			this.rightOp = rightOp;
		}
		
		/** Computes recursively the plan cost of this instance from the cost of its left child
		 * (joinOrder[:-1]) plus the size of the relation produced as the result of 
		 * the left child.
		 */
		public void computePlanCost() {
//			if (joinOrder.size() <= 2) {
//				planCost = 0;
//				if (joinOrder.size() == 2) {
//					JoinSizeCompute comp = new JoinSizeCompute(joinOrder.get(0), joinOrder.get(1), joinConditions.get(0));
//					relationSize = comp.computeJoinSize();
//					CostMetric curCostMetric = new CostMetric();
//					curCostMetric.bestPlanCost = planCost;
//					curCostMetric.relationSize = relationSize;
//					if (subsetCostMetrics.size() == 2) { //size 2 subset uninitialized, create a new one
//						HashMap<HashSet<LogicalOperator>, CostMetric> newSubset = new HashMap<HashSet<LogicalOperator>, CostMetric>();
//						newSubset.put(joinOrder, curCostMetric);
//						subsetCostMetrics.add(newSubset);
//					} else if (subsetCostMetrics.size() == 3) {//size 2 subset already initialized, create
//						subsetCostMetrics.get(2).put(joinOrder, curCostMetric);
//					}
//				}
//				return;
//			}
//			PlanCostCompute leftCompute = new PlanCostCompute(joinOrder.subList(0, joinOrder.size() - 1));
//			leftCompute.computePlanCost();
//			planCost = leftCompute.getPlanCost() + leftCompute.getJoinRelationSize();
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
}
