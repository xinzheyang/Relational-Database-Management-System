/**
 * 
 */
package joinorderoptimizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import datastructure.UnionElement;
import logicaloperator.LogicalJoinOperator;
import logicaloperator.LogicalOperator;
import logicaloperator.LogicalScanOperator;
import logicaloperator.LogicalSelectOperator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Column;
import visitor.ParseConjunctExpVisitor;
import visitor.UnionFindVisitor;

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
			currMetric.planCost = 0; //plan cost for single table is 0
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
//			int relationSize = 0;
			CostMetric curBest = null;
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
					curBest = comp.getResultCostMetric();
					optimalRightOp = op;
					leftBestOrder = new LinkedList<LogicalOperator>(lastBest.bestJoinOrder);
				}
			}

			leftBestOrder.add(optimalRightOp);
			curBest.bestJoinOrder = leftBestOrder;
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
		//		private int planCost;
		//		private int relationSize;
		private Expression joinCondition;
		private Collection<UnionElement> allUnions;
		private CostMetric costMetric; //the cost metric to be returned

		public PlanCostCompute(CostMetric leftCostMetric, HashSet<LogicalOperator> leftChildren, LogicalOperator rightOp) {
			this.leftRelations = leftCostMetric;
			this.leftChildren = leftChildren;
			this.rightOp = rightOp;
			costMetric = new CostMetric();
			joinCondition = null;
			//extracts the join condition for this join
			String rightRef = null;
			if (rightOp instanceof LogicalScanOperator) {
				rightRef = ((LogicalScanOperator) rightOp).getReference();
			} else if (rightOp instanceof LogicalSelectOperator) {
				rightRef = ((LogicalSelectOperator) rightOp).getReference();
			}
			//			Expression condition = null;
			for (LogicalOperator leftChild : leftChildren) {
				String leftRef = null;
				if (leftChild instanceof LogicalScanOperator) {
					leftRef = ((LogicalScanOperator) leftChild).getReference();
				} else if (rightOp instanceof LogicalSelectOperator) {
					leftRef = ((LogicalSelectOperator) rightOp).getReference();
				}
				Expression tempCondition = visitor.getJoinCondition(leftRef, rightRef); 
				//gets the join exp of every left child with the right child
				joinCondition = joinCondition == null ? tempCondition : new AndExpression(joinCondition, tempCondition);
				//AND them all together to get the final join condition
			}

		}

		/** 3.4.2 Computes the plan cost of this join from the left child's best plan cost and its
		 * relation size.
		 */
		public void computePlanCost() {
			costMetric.planCost = leftRelations.planCost + leftRelations.relationSize; 
		}

		/** 3.4.3 Computes the intermediate relation size of the left relation joining the right logical
		 * operator. The estimate of the join size is computed by dividing the product of the left and 
		 * right relation sizes by product of V-values on all attributes the two relations are joined with.
		 */
		public void computeJoinSize() {
			
			int rightRelationSize = -1;
			if (rightOp instanceof LogicalScanOperator) {
				rightRelationSize = ((LogicalScanOperator) rightOp).getRelationSize();
			} else if (rightOp instanceof LogicalSelectOperator) {
				rightRelationSize = ((LogicalSelectOperator) rightOp).getRelationSize();
			}
			int leftRelationSize = leftRelations.relationSize;
			//use UF to get all unions of table attributes joined on equality in the join condition.
			//compute denominators on these unions (max of all v-values of union attributes).
			UnionFindVisitor findVisit = new UnionFindVisitor();
			joinCondition.accept(findVisit);
			allUnions = findVisit.getUnionFind().getRootElementMap();
			int relationSize = leftRelationSize * rightRelationSize;
			for (UnionElement union : allUnions) {
				//compute max of all v-values on this union
				relationSize = (int) Math.ceil(((double) relationSize) / ((double) computeMaxVValue(union)));
			}
			costMetric.relationSize = Math.max(relationSize, 1); //clamp up relation size by 1
		}

		/** Computes the max v-value (part of denominator) of this union element (max of all attributes), 
		 * memorizes min of all V-values as new V value for this union element in cost metric's v-value map 
		 * for future use.
		 * @param union
		 * @return the V-value computed from this union
		 */
		private int computeMaxVValue(UnionElement union) {
			int min = Integer.MAX_VALUE;
			int res = Integer.MIN_VALUE;
			for (Column attrib : union.getAttributes()) {
				int curVValue = computeVValue(attrib);
				assert curVValue != -1; //assert valid V-values
				if (curVValue < min) {
					min = curVValue;
				}
				if (curVValue > res) {
					res = curVValue;
				}
			}
			costMetric.vValueMap.put(new HashSet<Column>(union.getAttributes()), min); //add to the new v value map for this join
			return res;
		}

		/** 3.4.4 Computes the V value of this join relation on the given attribute. If not memorized by the left relations,
		 * recompute V value from all left and right children operators.
		 * @param attrib
		 */
		private int computeVValue(Column attrib) {
			int result = -1;
			int leftRelVValue = leftRelations.getVValue(attrib); //try checking existence in the left relations cost metric
			if (leftRelVValue != -1) { //found
				result = Math.min(leftRelVValue, leftRelations.relationSize); //clamp down V-value by left relation size
			} else { //bad luck, have to recompute from the column's corresponding LogicalOperator (base table or selection on base table)
				String attribTableRef = attrib.getTable().getName();
				String attribColRef = attrib.getColumnName();
				//checks right
				if (rightOp instanceof LogicalScanOperator && ((LogicalScanOperator) rightOp).getReference().equals(attribTableRef)) {
					result = ((LogicalScanOperator) rightOp).getVValue(attribColRef);
				} else if (rightOp instanceof LogicalSelectOperator && ((LogicalSelectOperator) rightOp).getReference().equals(attribTableRef)) {
					result = ((LogicalSelectOperator) rightOp).getVValue(attribColRef);
				}
				else {
					//checks left
					for (LogicalOperator op : leftChildren) {
						if (op instanceof LogicalScanOperator && ((LogicalScanOperator) op).getReference().equals(attribTableRef)) {
							result = ((LogicalScanOperator) op).getVValue(attribColRef);
						} else if (op instanceof LogicalSelectOperator && ((LogicalSelectOperator) op).getReference().equals(attribTableRef)) {
							result = ((LogicalSelectOperator) op).getVValue(attribColRef);
						}
					}
				}

			}
			if (result != -1) return Math.max(1, result); //if valid result, clamp up V-value by 1
			return result;
		}

		/** Getter for the result cost metric.
		 * @return
		 */
		public CostMetric getResultCostMetric() {
			return costMetric;
		}

		/** Getter for this plan's plan cost
		 * @return
		 */
		public int getPlanCost() {
			return costMetric.planCost;
		}
		
	}
}
