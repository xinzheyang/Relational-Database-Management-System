/**
 * 
 */
package visitor;

import database.Tuple;
//import java.lang.Throwable.*;
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

/** An instance of the class evaluates an expression by recursively breaking 
 * down the expression visiting its sub-expressions.
 * @author sitianchen
 *
 */
public class EvaluateExpVisitor implements ExpressionVisitor {
	
	private boolean returnBoolValue; //final value of the evaluated expression, if a boolean expression
	private int returnLongValue; //final value of the evaluated expression, for LongValue or Column
	private Tuple currTuple;
	private Operator op;
	
	/** Getter for returnBoolValue.
	 * @return this.returnBoolValue
	 */
	public boolean getReturnBoolValue() {
		return returnBoolValue;
	}
	
	/** Getter for returnLongValue.
	 * @return this.returnLongValue
	 */
	public int getReturnLongValue() {
		return returnLongValue;
	}
	
	/** Set the visitor's current tuple to tup.
	 * @param tup
	 */
	public void setCurrTuple(Tuple tup) {
		currTuple = tup;
	}
	
	/** Set the visitor's operator to op.
	 * @param op
	 */
	public void setOperator(Operator op) {
		this.op = op;
	}

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
	public void visit(LongValue longValue) {
		returnLongValue = (int) longValue.getValue();
	}

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
		boolean leftValue = returnBoolValue;
		andExpression.getRightExpression().accept(this);
		boolean rightValue = returnBoolValue;
		returnBoolValue = leftValue && rightValue;
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.conditional.OrExpression)
	 */
	@Override
	public void visit(OrExpression orExpression) { throw new UnsupportedOperationException("not supported"); }
	
	@Override
	public void visit(Between between) { throw new UnsupportedOperationException("not supported"); }
	
	@Override
	public void visit(EqualsTo equalsTo) {
		equalsTo.getLeftExpression().accept(this);
		int leftValue = returnLongValue;
		equalsTo.getRightExpression().accept(this);
		int rightValue = returnLongValue;
		returnBoolValue = leftValue == rightValue;
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.GreaterThan)
	 */
	@Override
	public void visit(GreaterThan greaterThan) {
		greaterThan.getLeftExpression().accept(this);
		int leftValue = returnLongValue;
		greaterThan.getRightExpression().accept(this);
		int rightValue = returnLongValue;
		returnBoolValue = leftValue > rightValue;

	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals)
	 */
	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		greaterThanEquals.getLeftExpression().accept(this);
		int leftValue = returnLongValue;
		greaterThanEquals.getRightExpression().accept(this);
		int rightValue = returnLongValue;
		returnBoolValue = leftValue >= rightValue;
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.InExpression)
	 */
	@Override
	public void visit(InExpression inExpression) { throw new UnsupportedOperationException("not supported"); }
	
	@Override
	public void visit(IsNullExpression isNullExpression) { throw new UnsupportedOperationException("not supported"); }
	
	@Override
	public void visit(LikeExpression likeExpression) { throw new UnsupportedOperationException("not supported"); }
	
	@Override
	public void visit(MinorThan minorThan) {
		minorThan.getLeftExpression().accept(this);
		int leftValue = returnLongValue;
		minorThan.getRightExpression().accept(this);
		int rightValue = returnLongValue;
		returnBoolValue = leftValue < rightValue;

	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThanEquals)
	 */
	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		minorThanEquals.getLeftExpression().accept(this);
		int leftValue = returnLongValue;
		minorThanEquals.getRightExpression().accept(this);
		int rightValue = returnLongValue;
		returnBoolValue = leftValue <= rightValue;

	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.NotEqualsTo)
	 */
	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		// NOT SURE about isNot()
		notEqualsTo.getLeftExpression().accept(this);
		int leftValue = returnLongValue;
		notEqualsTo.getRightExpression().accept(this);
		int rightValue = returnLongValue;
		returnBoolValue = leftValue != rightValue;

	}

	/* First gets the corresponding index of the tableColumn in the current operator,
	 * then accesses the corresponding value in the tuple by the index.
	 */
	@Override
	public void visit(Column tableColumn) {
		try{
			int colIndex = op.getColumnIndex(tableColumn.getTable().getName() + "." + tableColumn.getColumnName());
			returnLongValue = currTuple.getColumnValue(colIndex);
			
		} catch(NullPointerException e) { 
			System.err.println("column name non-existent or tuple is null, please check your input");
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.statement.select.SubSelect)
	 */
	@Override
	public void visit(SubSelect subSelect) { throw new UnsupportedOperationException("not supported"); }
	
	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.CaseExpression)
	 */
	@Override
	public void visit(CaseExpression caseExpression) { throw new UnsupportedOperationException("not supported"); }

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.WhenClause)
	 */
	@Override
	public void visit(WhenClause whenClause) { throw new UnsupportedOperationException("not supported"); }

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.ExistsExpression)
	 */
	@Override
	public void visit(ExistsExpression existsExpression) { throw new UnsupportedOperationException("not supported"); }

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.AllComparisonExpression)
	 */
	@Override
	public void visit(AllComparisonExpression allComparisonExpression) { throw new UnsupportedOperationException("not supported"); }

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.AnyComparisonExpression)
	 */
	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) { throw new UnsupportedOperationException("not supported"); }

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Concat)
	 */
	@Override
	public void visit(Concat concat) { throw new UnsupportedOperationException("not supported"); }

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.Matches)
	 */
	@Override
	public void visit(Matches matches) { throw new UnsupportedOperationException("not supported"); }

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd)
	 */
	@Override
	public void visit(BitwiseAnd bitwiseAnd) { throw new UnsupportedOperationException("not supported"); }
	
	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr)
	 */
	@Override
	public void visit(BitwiseOr bitwiseOr) { throw new UnsupportedOperationException("not supported"); }

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor)
	 */
	@Override
	public void visit(BitwiseXor bitwiseXor) { throw new UnsupportedOperationException("not supported"); }

}
