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
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import physicaloperator.Operator;
import physicaloperator.ScanOperator;
import physicaloperator.SelectOperator;
import visitor.ParseConjunctExpVisitor;
import visitor.UnionFindVisitor;

/**
 * @author sitianchen
 *
 */
public class JoinOrderOptimizer {

	private LogicalJoinOperator joinOp;
	private List<HashMap<HashSet<LogicalOperator>, CostMetric>> subsetCostMetrics; //cost and relation size of best plan for all subsets 
	private List<LogicalOperator> finalOrder; //final best order to be returned
	private ParseConjunctExpVisitor visitor;

	/** Given the logical join operator and join map (or construct one if null)
	 * @param joinOperator
	 * @throws Exception
	 */
	public JoinOrderOptimizer(LogicalJoinOperator joinOperator, ParseConjunctExpVisitor visitor) {
		joinOp = joinOperator;
		subsetCostMetrics = new ArrayList<HashMap<HashSet<LogicalOperator>, CostMetric>>();
		subsetCostMetrics.add(null); //initialize 0th denoting size 0 subset, and is null
		this.visitor = visitor;
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

		initTwoRelationJoins();
	}

	/**Special case for two relation joins. The cost of a two-relation plan is zero; however, 
	 * you should choose the smaller relation to be the outer in the best plan;
	 */
	private void initTwoRelationJoins() {
		HashMap<HashSet<LogicalOperator>, CostMetric> sizeTwoMap = new HashMap<HashSet<LogicalOperator>, CostMetric>();
		List<LogicalOperator> allChildren = joinOp.getJoinChildren();
		for (int i = 0; i < allChildren.size(); i++) { //for all possible pair combinations
			for (int j = i+1; j < allChildren.size(); j++) {
				LogicalOperator child1 = allChildren.get(i);
				LogicalOperator child2 = allChildren.get(j);

				HashSet<LogicalOperator> twoList = new HashSet<LogicalOperator>();
				twoList.add(child1);
				twoList.add(child2);

				//relation sizes of children
				int child1Size = (child1 instanceof LogicalScanOperator) ? ((LogicalScanOperator) child1).getRelationSize() 
						: ((LogicalSelectOperator) child1).getRelationSize();
				int child2Size = (child2 instanceof LogicalScanOperator) ? ((LogicalScanOperator) child2).getRelationSize() 
						: ((LogicalSelectOperator) child2).getRelationSize();
				//set the relation with smaller size to be the left / outer relation of the join

				HashSet<LogicalOperator> leftSet = new HashSet<LogicalOperator>();
				leftSet.add(child1);
				CostMetric leftCost = this.subsetCostMetrics.get(1).get(leftSet);
				PlanCostCompute comp = new PlanCostCompute(leftCost, leftSet, child2);
				comp.computeJoinSize();
				CostMetric currMetric = comp.getResultCostMetric();
				currMetric.bestJoinOrder = new ArrayList<LogicalOperator>();

				if (child1Size < child2Size) { //always choose the relation with smaller size to be the outer relation
					currMetric.bestJoinOrder.add(child1);
					currMetric.bestJoinOrder.add(child2);
				} else {
					currMetric.bestJoinOrder.add(child2);
					currMetric.bestJoinOrder.add(child1);
				}

				sizeTwoMap.put(twoList, currMetric);		
			}
		}
		subsetCostMetrics.add(sizeTwoMap);
	}

	/** Getter for the final best order of this join.
	 * @return
	 */
	public List<LogicalOperator> getBestOrder() {
		return finalOrder;
	}

	/** 3.4.1 Chooses the best join order of the instance's logical operator by the bottom-up
	 * dynamic programming approach.
	 */
	public void dpChooseBestPlan() {
		//implement dp bottom-up.
		List<LogicalOperator> allChildren = joinOp.getJoinChildren();
		List<List<HashSet<LogicalOperator>>> subsetsBySize = enumerateSubsets(allChildren);
		for (int i = 3; i <= allChildren.size(); i++) { //starts from size = 3, since 0,1,2 already initialized

			for(HashSet<LogicalOperator> ops : subsetsBySize.get(i)) { //for all subsets of this size
				//compute optimal for this subset
				long bestPlanCost = Long.MAX_VALUE;
				CostMetric curBest = null;
				LogicalOperator optimalRightOp = null;
				List<LogicalOperator> leftBestOrder = null;
				
				HashSet<LogicalOperator> leftRelations = new HashSet<LogicalOperator>(ops);
				for(LogicalOperator op : ops) {//choose the rightmost operator
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
					leftRelations.add(op);
				}
				
				leftBestOrder.add(optimalRightOp);
				curBest.bestJoinOrder = leftBestOrder;
				if (subsetCostMetrics.size() == i) 
					subsetCostMetrics.add(new HashMap<HashSet<LogicalOperator>, CostMetric>()); //curr level not yet initialized
				subsetCostMetrics.get(i).put(ops, curBest); //memoize the opt
				
			}

		}

		finalOrder = subsetCostMetrics.get(allChildren.size()).get(new HashSet<LogicalOperator>(allChildren)).bestJoinOrder;
	}

	/** Enumerates the subsets of the logical operator list and group subsets by their size. e.g. for
	 * set {A, B, C}, result is [{}, [{A}, {B}, {C}], [{AB}, {AC}, {BC}], [{ABC}]]
	 * @param lst
	 * @return subsets group by size
	 */
	private List<List<HashSet<LogicalOperator>>> enumerateSubsets(List<LogicalOperator> lst) {
		List<List<HashSet<LogicalOperator>>> subsetsBySize = new ArrayList<List<HashSet<LogicalOperator>>>();
		for (int i = 0; i < lst.size() + 1; i++) {
			subsetsBySize.add(null);
		}
		enumerateSubsets(lst, new HashSet<LogicalOperator>(), subsetsBySize, 0);
		return subsetsBySize;
	}

	/** Recursive helper for enumerate subsets by size.
	 * @param lst
	 * @param tmp
	 * @param res
	 * @param currElemIndex
	 */
	private void enumerateSubsets(List<LogicalOperator> lst, HashSet<LogicalOperator> tmp, List<List<HashSet<LogicalOperator>>> res, int currElemIndex) {
		if (currElemIndex >= lst.size()) {
			if (res.get(tmp.size()) == null) {
				res.set(tmp.size(), new ArrayList<>());
			} 
			res.get(tmp.size()).add((HashSet<LogicalOperator>) tmp.clone());
			//			res.set(tmp.size(), (HashSet<LogicalOperator>) tmp.clone());
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
		private String rightRef;
		private Expression joinCondition;
		private Collection<UnionElement> allUnions;
		private CostMetric costMetric; //the cost metric to be returned

		public PlanCostCompute(CostMetric leftCostMetric, HashSet<LogicalOperator> leftChildren, LogicalOperator rightOp) {
			this.leftRelations = leftCostMetric;
			this.leftChildren = leftChildren;
			this.rightOp = rightOp;
			costMetric = new CostMetric();
			joinCondition = null;

			rightRef = getLogicalScanOrSelectRef(rightOp);
			
			//extracting the join condition for this intermediate join
			//first extracts the join condition from union find
			for (UnionElement union : joinOp.getUnionElements()) {
				List<Column> rightAttribs = union.getAttrByTable(rightRef);
				for (LogicalOperator leftChild : leftChildren) {
					List<Column> leftAttribs = union.getAttrByTable(getLogicalScanOrSelectRef(leftChild));
					for(Column rightAttrib : rightAttribs) {
						for(Column leftAttrib : leftAttribs) {
							Expression newEqual = new EqualsTo(leftAttrib, rightAttrib);
							joinCondition = joinCondition == null ? newEqual : new AndExpression(joinCondition, newEqual);
						}
					}
				}
			}

			//then extracts the join condition for this join
			if (visitor != null) {
				for (LogicalOperator leftChild : leftChildren) {
					String leftRef = getLogicalScanOrSelectRef(leftChild);
					HashSet<Expression> tempConditions = visitor.getJoinConditionSet(leftRef, rightRef); 
					if (tempConditions != null) {
						for(Expression tempCondition : tempConditions) {
							//gets the join exp of every left child with the right child
							if (tempCondition != null && !(tempCondition instanceof EqualsTo)) 
								joinCondition = joinCondition == null ? tempCondition : new AndExpression(joinCondition, tempCondition);
							//AND them all together to get the final join condition
						}
					}
				}
			}

		}

		/** Helper for getting the operator reference of a logical scan or select operator
		 * @param op
		 * @return
		 */
		private String getLogicalScanOrSelectRef(LogicalOperator op) {
			String ref;
			if (op instanceof LogicalScanOperator) {
				ref = ((LogicalScanOperator) op).getReference();
			} else {
				ref = ((LogicalSelectOperator) op).getReference();
			}
			return ref;
		}

		/** 3.4.2 Computes the plan cost of this join from the left child's best plan cost and its
		 * relation size.
		 */
		public void computePlanCost() {
			if (this.leftChildren.size() == 1) { //two-relation join plan, plan cost is 0
				costMetric.planCost = 0;
			}
			else {
				costMetric.planCost = leftRelations.planCost + leftRelations.relationSize; 
			}
		}

		/** 3.4.3 Computes the intermediate relation size of the left relation joining the right logical
		 * operator. The estimate of the join size is computed by dividing the product of the left and 
		 * right relation sizes by product of V-values on all attributes the two relations are joined with.
		 */
		public void computeJoinSize() {

			long rightRelationSize = -1;
			if (rightOp instanceof LogicalScanOperator) {
				rightRelationSize = ((LogicalScanOperator) rightOp).getRelationSize();
			} else if (rightOp instanceof LogicalSelectOperator) {
				rightRelationSize = ((LogicalSelectOperator) rightOp).getRelationSize();
			}
			long leftRelationSize = leftRelations.relationSize;
			long relationSize = leftRelationSize * rightRelationSize; //cross product relation size
			if (joinCondition != null) {//if there is join condition
				//use UF to get all unions of table attributes joined on equality in the join condition.
				//compute denominators on these unions (max of all v-values of union attributes).
				UnionFindVisitor findVisit = new UnionFindVisitor();
				joinCondition.accept(findVisit);
				allUnions = findVisit.getUnionFind().getRootElementMap();
				for (UnionElement union : allUnions) {
					//compute max of all v-values on this union
					relationSize = (long) Math.ceil(((double) relationSize) / ((double) computeMaxVValue(union)));
				}
			}
			costMetric.relationSize = Math.max(relationSize, 1); //clamp up relation size by 1
		}

		/** Computes the max v-value (part of denominator) of this union element (max of all attributes), 
		 * memorizes min of all V-values as new V value for this union element in cost metric's v-value map 
		 * for future use.
		 * @param union
		 * @return the V-value computed from this union
		 */
		private long computeMaxVValue(UnionElement union) {
			long min = Long.MAX_VALUE;
			long res = Long.MIN_VALUE;
			for (Column attrib : union.getAttributes()) {
				long curVValue = computeVValue(attrib);
				assert curVValue != -1; //assert valid V-values
				if (curVValue < min) {
					min = curVValue;
				}
				if (curVValue > res) {
					res = curVValue;
				}
			}
//			System.out.println(leftChildren);
//			System.out.println(rightOp);
//			System.out.println(res);
			costMetric.vValueMap.put(new HashSet<Column>(union.getAttributes()), min); //add to the new v value map for this join
			return res;
		}

		/** 3.4.4 Computes the V value of this join relation on the given attribute. If not memorized by the left relations,
		 * recompute V value from all left and right children operators.
		 * @param attrib
		 */
		private long computeVValue(Column attrib) {
			long result = -1;
			long leftRelVValue = leftRelations.getVValue(attrib); //try checking existence in the left relations cost metric
			if (leftRelVValue != -1) { //found
				result = Math.min(leftRelVValue, leftRelations.relationSize); //clamp down V-value by left relation size
			} 
			else { //bad luck, have to recompute from the column's corresponding LogicalOperator (base table or selection on base table)
				String attribTableRef = attrib.getTable().getName();
				String attribColRef = attrib.getColumnName();
				//checks right
				if (rightRef.equals(attribTableRef)) {
					if (rightOp instanceof LogicalScanOperator) {
						result = ((LogicalScanOperator) rightOp).getVValue(attribColRef);
					} else if (rightOp instanceof LogicalSelectOperator) {
						result = ((LogicalSelectOperator) rightOp).getVValue(attribColRef);
					}
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
		public long getPlanCost() {
			return costMetric.planCost;
		}

	}
}
