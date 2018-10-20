/**
 * 
 */
package visitor;

import java.io.FileNotFoundException;

import logicaloperator.*;
import physicaloperator.*;

/**
 * @author xinzheyang
 *
 */
public class PhysicalPlanBuilder {
	Operator operator;
	
	public PhysicalPlanBuilder() {
	}
	
	public void visit(LogicalDupElimOperator op) {
		op.getChildOp().accept(this);
		DupElimOperator dupElimOperator = new DupElimOperator(operator);
		operator = dupElimOperator;
	}
	
	public void visit(LogicalSortOperator op) {
		op.getChildOp().accept(this);
		SortOperator sortOperator = new SortOperator(operator, op.getCols());
		operator = sortOperator;
	}
	
	public void visit(LogicalProjectOperator op) {
		op.getChildOp().accept(this);
		ProjectOperator projectOperator = new ProjectOperator(operator, op.getCols());
		operator = projectOperator;
	}
	
	public void visit(LogicalJoinOperator op) {
		op.getLeftChild().accept(this);
		Operator left = operator;
		op.getRightChild().accept(this);
		Operator right = operator;
		TNLJoinOperator joinOperator = op.getJoinCondition() == null ? 
				new TNLJoinOperator(left, right) : new TNLJoinOperator(left, right, op.getJoinCondition());
		operator = joinOperator;
	}
	
	public void visit(LogicalSelectOperator op) {
		op.getChildOp().accept(this);
		SelectOperator selectOperator = new SelectOperator(operator, op.getEx());
		operator = selectOperator;
	}
	
	public void visit(LogicalScanOperator op) {
		ScanOperator scanOperator;
		try {
			scanOperator = op.getAlias() == "" ?
					new ScanOperator(op.getTableName()) : new ScanOperator(op.getTableName(), op.getAlias());
			operator = scanOperator;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public Operator getOperator() {
		return operator;
	}
}
