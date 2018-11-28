/**
 * 
 */
package visitor;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
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
import net.sf.jsqlparser.expression.ExpressionVisitor;

/**
 * @author sitianchen
 *
 */
public class CheckAllEquityExpVisitor implements ExpressionVisitor{
	
	private int numAnds; //number of expressions and-ed together
	private int numEquiExps; //number of equality expressions
	
	public CheckAllEquityExpVisitor() {
		numAnds = 1;
		numEquiExps = 0;
	}
	
	public boolean isAllEquity() {
//		System.out.println(numAnds);
//		System.out.println(numEquiExps);
		return numAnds == numEquiExps;
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
	public void visit(LongValue longValue) { }

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
		numAnds++;
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
//		System.out.println(numEquiExps);
//		System.out.println(equalsTo);
		this.numEquiExps++;
	}

	@Override
	public void visit(GreaterThan greaterThan) { }

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) { }

	@Override
	public void visit(InExpression inExpression) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(IsNullExpression isNullExpression) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(LikeExpression likeExpression) { throw new UnsupportedOperationException("not supported"); }

	@Override
	public void visit(MinorThan minorThan) { }

	@Override
	public void visit(MinorThanEquals minorThanEquals) { }

	@Override
	public void visit(NotEqualsTo notEqualsTo) { }

	@Override
	public void visit(Column tableColumn) { }

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
