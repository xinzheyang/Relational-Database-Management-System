/**
 * 
 */
package visitor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import database.DBCatalog;
import logicaloperator.*;
import net.sf.jsqlparser.expression.Expression;
import physicaloperator.*;

/**
 * @author xinzheyang
 * A physical plan builder that uses visitor pattern to
 * transform a logical operator into a physical operator
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
	
	/**
	 * @param op the logicalJoinOperator
	 * it consults the config and determines which physical Join operators to construct
	 */
	public void visit(LogicalJoinOperator op) {
		op.getLeftChild().accept(this);
		Operator left = operator;
		op.getRightChild().accept(this);
		Operator right = operator;
		JoinOperator joinOperator;
		if (DBCatalog.getJoinMethod().equals("0")) { //TNLJ
			joinOperator =  new TNLJoinOperator(left, right, op.getJoinCondition());
//			joinOperator = op.getJoinCondition() == null ? 
//					new TNLJoinOperator(left, right) : new TNLJoinOperator(left, right, op.getJoinCondition());
		}
		else {
			if (DBCatalog.getJoinMethod().equals("1")) { //BNLJ
				int bufferSize = DBCatalog.getJoinBufferSize();
				joinOperator = new BNLJoinOperator(left, right, op.getJoinCondition(), bufferSize);
//				joinOperator = op.getJoinCondition() == null ?
//						new BNLJoinOperator(left, right, bufferSize) : 
//							new BNLJoinOperator(left, right, op.getJoinCondition(), bufferSize);
			}
			else if (DBCatalog.getJoinMethod().equals("2")) { //SMJ
				Expression smjCondition = op.getJoinCondition();
				EquiConjunctVisitor equiVisit = new EquiConjunctVisitor();
				smjCondition.accept(equiVisit); /*by accepting, the visitor processes the 
				join condition and extract left and right column names in the sorting order.*/
				Object[] sortLeftObj = equiVisit.getLeftCompareCols().toArray();
				Object[] sortRightObj = equiVisit.getRightCompareCols().toArray();
				
				String[] sortOrderLeft = Arrays.copyOf(sortLeftObj, sortLeftObj.length, String[].class);
				String[] sortOrderRight = Arrays.copyOf(sortRightObj, sortRightObj.length, String[].class);
				//push down left and right sort operator to sort relations before merging
				SortOperator leftSort = getSortOperator(left, sortOrderLeft);
				SortOperator rightSort = getSortOperator(right, sortOrderRight);
				joinOperator = new SMJoinOperator(leftSort, rightSort, smjCondition);
//				joinOperator = op.getJoinCondition() == null ? new SMJoinOperator(leftSort, rightSort) :
//					new SMJoinOperator(leftSort, rightSort, smjCondition);
			}
			else { //invalid input, default to TNLJ
				joinOperator = new TNLJoinOperator(left, right, op.getJoinCondition());
//				joinOperator = op.getJoinCondition() == null ? 
//						new TNLJoinOperator(left, right) : new TNLJoinOperator(left, right, op.getJoinCondition());
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
			if (DBCatalog.useIndex()) {
				String tableName = op.getTableName();
				scanOperator = new IndexScanOperator(tableName, op.getAlias(), DBCatalog.getIndexFileLoc(tableName), 
						DBCatalog.getIndexKey(tableName), DBCatalog.hasClusteredIndex(tableName), Integer.MIN_VALUE, Integer.MAX_VALUE);
			} else {
				scanOperator = new ScanOperator(op.getTableName(), op.getAlias());
			}
			operator = scanOperator;
		} catch (FileNotFoundException e) {
			System.err.println("error occurred during building scan operator, file not found");
			e.printStackTrace();
		}
		
	}

	/**
	 * @return operator
	 */
	public Operator getOperator() {
		return operator;
	}
}
