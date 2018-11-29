/**
 * 
 */
package visitor;

import java.util.ArrayList;

import database.Tuple;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import physicaloperator.Operator;

/** An ExpressionVisitor only used for processing join condition in Sort Merge Join.
 * Visit every conjunct in the equality expression and record all columns names and values
 * referenced in left and right relations.
 * @author sitianchen
 *
 */
public class EquiConjunctVisitor implements ExpressionVisitor {

	private ArrayList<String> leftCompareCols; //column names of left relation to be compared in order
	private ArrayList<Integer> leftCompareVals; //column values of left relation to be compared in order
	private ArrayList<String> rightCompareCols;  //column names of right relation to be compared in order
	private ArrayList<Integer> rightCompareVals; //column values of right relation to be compared in order
	private Tuple tupleLeft; //current tuple from left operator getNextTuple()
	private Tuple tupleRight; //current tuple from right operator getNextTuple()
	private boolean isLeft; //for evaluating column expressions, differ between left and right
	private Operator leftOp; //left operator of the join
	private Operator rightOp; //right operator of the join
	private boolean isEval;

	public EquiConjunctVisitor(Tuple tupleLeft, Tuple tupleRight, Operator leftOp, Operator rightOp) {
		leftCompareVals = new ArrayList<Integer>();
		rightCompareVals = new ArrayList<Integer>();
		this.tupleLeft = tupleLeft;
		this.tupleRight = tupleRight;
		this.leftOp = leftOp;
		this.rightOp = rightOp;
		isEval = true;
	}

	/** Constructs an equity conjunct visitor with no evaluation properties.
	 * All the information we want are the left and right column names.
	 */
	public EquiConjunctVisitor(Operator leftOp, Operator rightOp) {
		this.leftOp = leftOp;
		this.rightOp = rightOp;
		leftCompareCols = new ArrayList<String>();
		rightCompareCols = new ArrayList<String>();
		isEval = false;
	}
	//--SETTERS AND GETTERS--
	public void setTupleLeft(Tuple tupleLeft) {
		this.tupleLeft = tupleLeft;
	}

	public void setTupleRight(Tuple tupleRight) {
		this.tupleRight = tupleRight;
	}

	public ArrayList<String> getLeftCompareCols() {
		return leftCompareCols;
	}

	public ArrayList<String> getRightCompareCols() {
		return rightCompareCols;
	}

	public ArrayList<Integer> getLeftCompareVals() {
		return leftCompareVals;
	}

	public ArrayList<Integer> getRightCompareVals() {
		return rightCompareVals;
	}
	//--SETTERS AND GETTERS--

	@Override
	public void visit(NullValue nullValue) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(Function function) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(InverseExpression inverseExpression) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(JdbcParameter jdbcParameter) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(DoubleValue doubleValue) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(LongValue longValue) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(DateValue dateValue) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(TimeValue timeValue) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(TimestampValue timestampValue) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(Parenthesis parenthesis) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(StringValue stringValue) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(Addition addition) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(Division division) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(Multiplication multiplication) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(Subtraction subtraction) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(AndExpression andExpression) {
		andExpression.getLeftExpression().accept(this);
		andExpression.getRightExpression().accept(this);
	}

	@Override
	public void visit(OrExpression orExpression) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(Between between) { throw new UnsupportedOperationException("not supported"); }

	/** Visits the left expression, and then visit the right expression.
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.EqualsTo)
	 */
	@Override
	public void visit(EqualsTo equalsTo) {
		//		isLeft = true;
		equalsTo.getLeftExpression().accept(this);
		//		isLeft = false;
		equalsTo.getRightExpression().accept(this);
	}

	@Override
	public void visit(GreaterThan greaterThan) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(InExpression inExpression) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(IsNullExpression isNullExpression) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(LikeExpression likeExpression) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(MinorThan minorThan) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(MinorThanEquals minorThanEquals) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(NotEqualsTo notEqualsTo) { throw new UnsupportedOperationException("not supported"); }

	/** Create a new Evaluate ExpressionVisitor to evaluate this column expression.
	 * Record the column expression's column name, column value by first checking 
	 * the side of equality the column belongs to and then adding to the right record.
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.schema.Column)
	 */
	@Override
	public void visit(Column tableColumn) {
		if (isEval) { //we need tuple value information, evaluate the column expression
			EvaluateExpVisitor eval = new EvaluateExpVisitor();
			//			if (isLeft) {
			if (leftOp.getColumnIndexMap().containsKey(tableColumn.getWholeColumnName())) {
				eval.setCurrTuple(tupleLeft);
				eval.setOperator(leftOp);
				tableColumn.accept(eval);
				leftCompareVals.add(eval.getReturnLongValue());
			}
			else { //assume column always in right in this case
				eval.setCurrTuple(tupleRight);
				eval.setOperator(rightOp);
				tableColumn.accept(eval);
				rightCompareVals.add(eval.getReturnLongValue());
			}
		}
		else { //we just need the names of columns
			//			if (isLeft) {
			String attrib = tableColumn.getWholeColumnName();
			if (leftOp.getColumnIndexMap().containsKey(tableColumn.getWholeColumnName())) {
				if (!leftCompareCols.contains(attrib)) {//prevent duplicates
					leftCompareCols.add(tableColumn.getWholeColumnName());
				}
			}
			else {
				if (!rightCompareCols.contains(attrib)) {
					rightCompareCols.add(tableColumn.getWholeColumnName());
				}
			}
		}

	}

	@Override
	public void visit(SubSelect subSelect) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(CaseExpression caseExpression) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(WhenClause whenClause) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(ExistsExpression existsExpression) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(AllComparisonExpression allComparisonExpression) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(Concat concat) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(Matches matches) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(BitwiseAnd bitwiseAnd) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(BitwiseOr bitwiseOr) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(BitwiseXor bitwiseXor) { throw new UnsupportedOperationException("not supported"); }

}
