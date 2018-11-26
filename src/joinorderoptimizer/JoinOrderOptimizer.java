/**
 * 
 */
package joinorderoptimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import logicaloperator.LogicalJoinOperator;
import logicaloperator.LogicalOperator;
import logicaloperator.LogicalScanOperator;
import logicaloperator.LogicalSelectOperator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import visitor.ParseConjunctExpVisitor;

/**
 * @author sitianchen
 *
 */
public class JoinOrderOptimizer {
	
	private LogicalJoinOperator joinOp;
	private List<HashMap<HashSet<LogicalOperator>, CostMetric>> subsetCostMetrics; //cost and relation size of best plan for all subsets 
	private List<LogicalOperator> finalOrder;
	private ParseConjunctExpVisitor visitor;
//	private HashMap<List<String>, Expression> joinMap;
	
	/** Given the logical join operator and join map (or construct one if null)
	 * @param joinOperator
	 * @throws Exception
	 */
	public JoinOrderOptimizer(LogicalJoinOperator joinOperator, ParseConjunctExpVisitor visitor) throws Exception {
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
	
	public List<LogicalOperator> getBestOrder() {
		return finalOrder;
	}
	
	/** 3.4.1 Chooses the best join order of the instance's logical operator by the bottom-up
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
				PlanCostCompute comp = new PlanCostCompute(lastBest, leftRelations, op);
				comp.computePlanCost();
				comp.computeJoinSize();
				if (comp.getPlanCost() < bestPlanCost) {
					bestPlanCost = comp.getPlanCost();
					relationSize = comp.getJoinRelationSize();
					optimalRightOp = op;
					leftBestOrder = new LinkedList<LogicalOperator>(lastBest.bestJoinOrder);
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
	
	/** A private class belongs to the join order optimizer to assist with plan cost and intermediate relation size
	 * computation.
	 * @author sitianchen
	 *
	 */
	private class PlanCostCompute {
		
		private CostMetric leftRelations; //all logical operators are either scan or select
		private HashSet<LogicalOperator> leftChildren;
		private LogicalOperator rightOp;
		private int planCost;
		private int relationSize;
		private Expression joinCondition;
		
		public PlanCostCompute(CostMetric leftCostMetric, HashSet<LogicalOperator> leftChildren, LogicalOperator rightOp) {
			this.leftRelations = leftCostMetric;
			this.leftChildren = leftChildren;
			this.rightOp = rightOp;
			joinCondition = null;
			//extracts the join condition for this join
			String rightRef = null;
			if (rightOp instanceof LogicalScanOperator) {
				rightRef = ((LogicalScanOperator) rightOp).getReference();
			} else if (rightOp instanceof LogicalSelectOperator) {
				rightRef = ((LogicalSelectOperator) rightOp).getReference();
			}
			Expression condition = null;
			for (LogicalOperator leftChild : leftChildren) {
				String leftRef = null;
				if (leftChild instanceof LogicalScanOperator) {
					leftRef = ((LogicalScanOperator) leftChild).getReference();
				} else if (rightOp instanceof LogicalSelectOperator) {
					leftRef = ((LogicalSelectOperator) rightOp).getReference();
				}
				Expression tempCondition = visitor.getJoinCondition(leftRef, rightRef); 
				//gets the join exp of every left child with the right child
				condition = condition == null ? tempCondition : new AndExpression(condition, tempCondition);
				//AND them all together to get the final join condition
			}
		}
		
		/** 3.4.2 Computes the plan cost of this join from the left child's best plan cost and its
		 * relation size.
		 */
		public void computePlanCost() {
			planCost = leftRelations.bestPlanCost + leftRelations.relationSize; 

			//TODO: calculate relation size of current join order
		}
		
		/** 3.4.3 Computes the intermediate relation size of the left relation joining the right logical
		 * operator. The estimate of the join size is computed by dividing the product of the left and 
		 * right relation sizes by product of V-values on all attributes the two relations are joined with.
		 */
		public void computeJoinSize() {
			//TODO: implement this
			int rightRelationSize = -1;
			if (rightOp instanceof LogicalScanOperator) {
				rightRelationSize = ((LogicalScanOperator) rightOp).getRelationSize();
			} else if (rightOp instanceof LogicalSelectOperator) {
				rightRelationSize = ((LogicalSelectOperator) rightOp).getRelationSize();
			}
			int leftRelationSize = leftRelations.relationSize;
			//use UF to get all mappings of table -> attributes joined on equality in the join condition.
			//compute V-values on these attributes.
			//clamp down V-value by relation size
			relationSize = 0;
		}
		
		/** 3.4.4 Computes the V value of this join relation on the given attribute.
		 * @param attrib
		 */
		public void computeVValue(String attrib) {
			//TODO: implement this
		}
		
		public int getPlanCost() {
			return planCost;
		}
		
		public int getJoinRelationSize() {
			return relationSize;
		}
	}
}
