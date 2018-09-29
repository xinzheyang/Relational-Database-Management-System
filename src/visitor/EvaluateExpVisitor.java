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

/**
 * @author sitianchen
 *
 */
public class EvaluateExpVisitor implements ExpressionVisitor {
	
	private boolean returnBoolValue; //final value of the evaluated expression, if a boolean expression
	private long returnLongValue; //final value of the evaluated expression, for LongValue or Column
	public Tuple currTuple;
	
	public boolean getReturnBoolValue() {
		return this.returnBoolValue;
	}
	
	public long getReturnLongValue() {
		return this.returnLongValue;
	}
	
	public void setCurrTuple(Tuple tup) {
		this.currTuple = tup;
	}

	/* (non-Javadoc)
	 * (don't know what should be done for this one)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.NullValue)
	 */
	@Override
	public void visit(NullValue nullValue) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.Function)
	 */
	@Override
	public void visit(Function function) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");

	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.InverseExpression)
	 */
	@Override
	public void visit(InverseExpression inverseExpression) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.JdbcParameter)
	 */
	@Override
	public void visit(JdbcParameter jdbcParameter) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");

	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.DoubleValue)
	 */
	@Override
	public void visit(DoubleValue doubleValue) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");

	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.LongValue)
	 */
	@Override
	public void visit(LongValue longValue) {
		// TODO Auto-generated method stub
		this.returnLongValue = longValue.getValue();
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.DateValue)
	 */
	@Override
	public void visit(DateValue dateValue) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.TimeValue)
	 */
	@Override
	public void visit(TimeValue timeValue) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.TimestampValue)
	 */
	@Override
	public void visit(TimestampValue timestampValue) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.Parenthesis)
	 */
	@Override
	public void visit(Parenthesis parenthesis) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.StringValue)
	 */
	@Override
	public void visit(StringValue stringValue) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Addition)
	 */
	@Override
	public void visit(Addition addition) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Division)
	 */
	@Override
	public void visit(Division division) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Multiplication)
	 */
	@Override
	public void visit(Multiplication multiplication) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Subtraction)
	 */
	@Override
	public void visit(Subtraction subtraction) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.conditional.AndExpression)
	 */
	@Override
	public void visit(AndExpression andExpression) {
		// TODO Auto-generated method stub
		andExpression.getLeftExpression().accept(this);
		boolean leftValue = this.getReturnBoolValue();
		andExpression.getRightExpression().accept(this);
		boolean rightValue = this.getReturnBoolValue();
		this.returnBoolValue = leftValue && rightValue;
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.conditional.OrExpression)
	 */
	@Override
	public void visit(OrExpression orExpression) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.Between)
	 */
	@Override
	public void visit(Between between) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.EqualsTo)
	 */
	@Override
	public void visit(EqualsTo equalsTo) {
		// TODO Auto-generated method stub
		equalsTo.getLeftExpression().accept(this);
		long leftValue = this.getReturnLongValue();
		equalsTo.getRightExpression().accept(this);
		long rightValue = this.getReturnLongValue();
		this.returnBoolValue = leftValue == rightValue;
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.GreaterThan)
	 */
	@Override
	public void visit(GreaterThan greaterThan) {
		// TODO Auto-generated method stub
		greaterThan.getLeftExpression().accept(this);
		long leftValue = this.getReturnLongValue();
		greaterThan.getRightExpression().accept(this);
		long rightValue = this.getReturnLongValue();
		this.returnBoolValue = leftValue > rightValue;

	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals)
	 */
	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		// TODO Auto-generated method stub
		greaterThanEquals.getLeftExpression().accept(this);
		long leftValue = this.getReturnLongValue();
		greaterThanEquals.getRightExpression().accept(this);
		long rightValue = this.getReturnLongValue();
		this.returnBoolValue = leftValue >= rightValue;
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.InExpression)
	 */
	@Override
	public void visit(InExpression inExpression) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.IsNullExpression)
	 */
	@Override
	public void visit(IsNullExpression isNullExpression) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.LikeExpression)
	 */
	@Override
	public void visit(LikeExpression likeExpression) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThan)
	 */
	@Override
	public void visit(MinorThan minorThan) {
		// TODO Auto-generated method stub
		minorThan.getLeftExpression().accept(this);
		long leftValue = this.getReturnLongValue();
		minorThan.getRightExpression().accept(this);
		long rightValue = this.getReturnLongValue();
		this.returnBoolValue = leftValue < rightValue;

	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThanEquals)
	 */
	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		// TODO Auto-generated method stub
		minorThanEquals.getLeftExpression().accept(this);
		long leftValue = this.getReturnLongValue();
		minorThanEquals.getRightExpression().accept(this);
		long rightValue = this.getReturnLongValue();
		this.returnBoolValue = leftValue <= rightValue;

	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.NotEqualsTo)
	 */
	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		// TODO Auto-generated method stub
		// NOT SURE about isNot()
		notEqualsTo.getLeftExpression().accept(this);
		long leftValue = this.getReturnLongValue();
		notEqualsTo.getRightExpression().accept(this);
		long rightValue = this.getReturnLongValue();
		this.returnBoolValue = leftValue != rightValue;

	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.schema.Column)
	 */
	@Override
	public void visit(Column tableColumn) {
		// TODO Auto-generated method stub
//		assert tableColumn.getTable().equals(currTuple.getTable());
//		this.returnLongValue = (long) currTuple.getColumnValue(tableColumn.getColumnName());
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.statement.select.SubSelect)
	 */
	@Override
	public void visit(SubSelect subSelect) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.CaseExpression)
	 */
	@Override
	public void visit(CaseExpression caseExpression) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.WhenClause)
	 */
	@Override
	public void visit(WhenClause whenClause) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.ExistsExpression)
	 */
	@Override
	public void visit(ExistsExpression existsExpression) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.AllComparisonExpression)
	 */
	@Override
	public void visit(AllComparisonExpression allComparisonExpression) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.AnyComparisonExpression)
	 */
	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Concat)
	 */
	@Override
	public void visit(Concat concat) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.Matches)
	 */
	@Override
	public void visit(Matches matches) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd)
	 */
	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr)
	 */
	@Override
	public void visit(BitwiseOr bitwiseOr) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor)
	 */
	@Override
	public void visit(BitwiseXor bitwiseXor) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not supported");
	}

}
