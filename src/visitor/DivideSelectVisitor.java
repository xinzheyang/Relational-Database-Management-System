/**
 * 
 */
package visitor;

import database.Tuple;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
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

/**
 * @author xinzheyang
 *
 */
public class DivideSelectVisitor implements ExpressionVisitor{
	
	private String indexKey;
	private int lowKey = Integer.MIN_VALUE;
	private int highKey = Integer.MAX_VALUE;
	private boolean isIndex = false;
	private boolean isInt = false;
	private AndExpression normalSelect = null;
	
	
	private int intValue;
	
	/**
	 * @param index
	 */
	public DivideSelectVisitor(String index) {
		indexKey = index;
	}
	
	/**
	 * @return the lowKey
	 */
	public int getLowKey() {
		return lowKey;
	}


	/**
	 * @return the highKey
	 */
	public int getHighKey() {
		return highKey;
	}

	public boolean needIndexScan() {
		return ((lowKey != Integer.MIN_VALUE) || (highKey != Integer.MAX_VALUE)); 
	}

	/**
	 * @return the normalSelect
	 */
	public Expression getNormalSelect() {
		if (normalSelect == null) {
			return null;
		}
		Expression left = normalSelect.getLeftExpression();
		Expression right = normalSelect.getRightExpression();
		if (right == null) {
			return left;
		}
		return normalSelect;
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
		isIndex = false;
		isInt = true;
		intValue = (int) longValue.getValue();
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
		andExpression.getRightExpression().accept(this);
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
		boolean leftIsIndex = isIndex;
		boolean leftIsInt = isInt;
		int leftInt = intValue;
		equalsTo.getRightExpression().accept(this);
		boolean rightIsIndex = isIndex;
		boolean rightIsInt = isInt;
		int rightInt = intValue;
		
		if (leftIsIndex && rightIsInt) {
			lowKey = rightInt;
			highKey = rightInt;
		} else if (leftIsInt && rightIsIndex) {
			lowKey = leftInt;
			highKey = rightInt;
		} else {
			normalSelect = new AndExpression(equalsTo, normalSelect);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.GreaterThan)
	 */
	@Override
	public void visit(GreaterThan greaterThan) {
		greaterThan.getLeftExpression().accept(this);
		boolean leftIsIndex = isIndex;
		boolean leftIsInt = isInt;
		int leftInt = intValue;
		greaterThan.getRightExpression().accept(this);
		boolean rightIsIndex = isIndex;
		boolean rightIsInt = isInt;
		int rightInt = intValue;
		
		if (leftIsIndex && rightIsInt) {
			lowKey = Math.max(lowKey, rightInt+1);
		} else if (leftIsInt && rightIsIndex) {
			highKey = Math.min(highKey, leftInt-1);
		} else {
			normalSelect = new AndExpression(greaterThan, normalSelect);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals)
	 */
	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		greaterThanEquals.getLeftExpression().accept(this);
		boolean leftIsIndex = isIndex;
		boolean leftIsInt = isInt;
		int leftInt = intValue;
		greaterThanEquals.getRightExpression().accept(this);
		boolean rightIsIndex = isIndex;
		boolean rightIsInt = isInt;
		int rightInt = intValue;
		
		if (leftIsIndex && rightIsInt) {
			lowKey = Math.max(lowKey, rightInt);
		} else if (leftIsInt && rightIsIndex) {
			highKey = Math.min(highKey, leftInt);
		} else {
			normalSelect = new AndExpression(greaterThanEquals, normalSelect);
		}
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
		boolean leftIsIndex = isIndex;
		boolean leftIsInt = isInt;
		int leftInt = intValue;
		minorThan.getRightExpression().accept(this);
		boolean rightIsIndex = isIndex;
		boolean rightIsInt = isInt;
		int rightInt = intValue;
		
		if (leftIsIndex && rightIsInt) {
			highKey = Math.min(highKey, rightInt-1);
		} else if (leftIsInt && rightIsIndex) {
			lowKey = Math.max(lowKey, leftInt+1);
		} else {
			normalSelect = new AndExpression(minorThan, normalSelect);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThanEquals)
	 */
	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		minorThanEquals.getLeftExpression().accept(this);
		boolean leftIsIndex = isIndex;
		boolean leftIsInt = isInt;
		int leftInt = intValue;
		minorThanEquals.getRightExpression().accept(this);
		boolean rightIsIndex = isIndex;
		boolean rightIsInt = isInt;
		int rightInt = intValue;
		
		if (leftIsIndex && rightIsInt) {
			highKey = Math.min(highKey, rightInt);
		} else if (leftIsInt && rightIsIndex) {
			lowKey = Math.max(lowKey, leftInt);
		} else {
			normalSelect = new AndExpression(minorThanEquals, normalSelect);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.NotEqualsTo)
	 */
	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		normalSelect = new AndExpression(notEqualsTo, normalSelect);
	}

	/* First gets the corresponding index of the tableColumn in the current operator,
	 * then accesses the corresponding value in the tuple by the index.
	 */
	@Override
	public void visit(Column tableColumn) {
		isIndex = indexKey.equals(tableColumn.getColumnName());
		isInt = false;
//		try{
//			
//		} catch(NullPointerException e) { 
//			System.err.println("column name non-existent or tuple is null, please check your input");
//		}
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
