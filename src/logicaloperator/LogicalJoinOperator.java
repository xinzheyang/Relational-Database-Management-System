/**
 * 
 */
package logicaloperator;

import java.util.Collection;
import java.util.List;

import datastructure.UnionElement;
import net.sf.jsqlparser.expression.Expression;
import visitor.PhysicalPlanBuilder;

/**
 * @author xinzheyang
 * An logical operator that stores minimal information
 * to be visited by PhysicalPlanBuilder to transform into a physical plan builder
 */
public class LogicalJoinOperator extends LogicalOperator {
//	private LogicalOperator leftChild;
//	private LogicalOperator rightChild;
	private List<LogicalOperator> joinChildren;
	private Expression joinCondition;
	private Collection<UnionElement> unionElements;
	public LogicalJoinOperator(List<LogicalOperator> joinCh, Expression condition, Collection<UnionElement> unions) {
//		leftChild = left;
//		rightChild = right;
		joinChildren = joinCh;
		joinCondition = condition;
		unionElements = unions;
	}

//	public LogicalOperator getLeftChild() {
//		return leftChild;
//	}
//
//	public LogicalOperator getRightChild() {
//		return rightChild;
//	}
	
	

	/**
	 * @return the joinCondition
	 */
	public Expression getJoinCondition() {
		return joinCondition;
	}

	/**
	 * @return the unionElements
	 */
	public Collection<UnionElement> getUnionElements() {
		return unionElements;
	}

	/**
	 * @return the joinChildren
	 */
	public List<LogicalOperator> getJoinChildren() {
		return joinChildren;
	}

	/* (non-Javadoc)
	 * @see logicaloperator.LogicalOperator#accept(visitor.PhysicalPlanBuilder)
	 */
	@Override
	public void accept(PhysicalPlanBuilder pb) {
		pb.visit(this);
	}

}
