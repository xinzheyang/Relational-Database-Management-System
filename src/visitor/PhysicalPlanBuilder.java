/**
 * 
 */
package visitor;

import java.io.FileNotFoundException;

import database.DBCatalog;
import logicaloperator.*;
import net.sf.jsqlparser.expression.Expression;
import physicaloperator.*;

/**
 * @author xinzheyang
 *
 */
public class PhysicalPlanBuilder {
	Operator operator;
	
	public PhysicalPlanBuilder() {
	}
	
	/** Constructs the sort operator according to the PPB configuration.
	 * @param child: child operator to be sorted
	 * @param cols: column order for sorting
	 * @return the right sort operator constructed
	 */
	private SortOperator getSortOperator(Operator child, String[] cols) {
		SortOperator sortOperator;
		if (DBCatalog.getSortMethod().equals("0")) {
			sortOperator = new InMemorySortOperator(child, cols);
		}
		else {
			assert DBCatalog.getSortMethod().equals("1");
			sortOperator = new ExternalSortOperator(child, cols, DBCatalog.getSortBufferSize());
		}
		
		return sortOperator;
	}
	
	public void visit(LogicalDupElimOperator op) {
		op.getChildOp().accept(this);
		DupElimOperator dupElimOperator = new DupElimOperator(operator);
		operator = dupElimOperator;
	}
	
	public void visit(LogicalSortOperator op) {
		op.getChildOp().accept(this);
		SortOperator sortOperator = getSortOperator(operator, op.getCols());
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
		JoinOperator joinOperator;
		if (DBCatalog.getJoinMethod().equals("0")) { //TNLJ
			joinOperator = op.getJoinCondition() == null ? 
					new TNLJoinOperator(left, right) : new TNLJoinOperator(left, right, op.getJoinCondition());
		}
		else {
			if (DBCatalog.getJoinMethod().equals("1")) { //BNLJ
				int bufferSize = DBCatalog.getJoinBufferSize();
				joinOperator = op.getJoinCondition() == null ?
						new BNLJoinOperator(left, right, bufferSize) : 
							new BNLJoinOperator(left, right, op.getJoinCondition(), bufferSize);
			}
			else if (DBCatalog.getJoinMethod().equals("2")) { //SMJ
				Expression smjCondition = op.getJoinCondition();
				EquiConjunctVisitor equiVisit = new EquiConjunctVisitor();
				smjCondition.accept(equiVisit); /*by accepting, the visitor processes the 
				join condition and extract left and right column names in the sorting order.*/
				String[] sortOrderLeft = (String[]) equiVisit.getLeftCompareCols().toArray();
				String[] sortOrderRight = (String[]) equiVisit.getRightCompareCols().toArray();
				//push down left and right sort operator to sort relations before merging
				SortOperator leftSort = getSortOperator(left, sortOrderLeft);
				SortOperator rightSort = getSortOperator(right, sortOrderRight);
				joinOperator = op.getJoinCondition() == null ? new SMJoinOperator(leftSort, rightSort) :
					new SMJoinOperator(leftSort, rightSort, smjCondition);
			}
			else { //invalid input, default to TNLJ
				joinOperator = op.getJoinCondition() == null ? 
						new TNLJoinOperator(left, right) : new TNLJoinOperator(left, right, op.getJoinCondition());
			}
		}
//		TNLJoinOperator joinOperator = op.getJoinCondition() == null ? 
//				new TNLJoinOperator(left, right) : new TNLJoinOperator(left, right, op.getJoinCondition());
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
