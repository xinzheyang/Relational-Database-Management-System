/**
 * 
 */
package visitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import datastructure.UnionElement;
import datastructure.UnionFind;
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

/**
 * @author xinzheyang
 *
 */
public class UnionFindVisitor implements ExpressionVisitor {

	private UnionFind unionFind;
	private int intValue;
	private boolean isInt = false;
	private Expression normalSelect = null;
	private Expression normalJoin = null;
	private Column col;
	/**
	 * table reference -> list of attributes of the table that appear in equal join
	 * conditions
	 */
	private HashMap<String, List<Column>> eqJoinAttrMap;

	/**
	 * 
	 */
	public UnionFindVisitor() {
		unionFind = new UnionFind();
		eqJoinAttrMap = new HashMap<>();
	}
	

	/**
	 * @param table
	 *            the table reference
	 * @return list of attributes of the table that appear in equal join conditions
	 */
	public List<Column> getEqJoinAttrByReference(String table) {
		if (eqJoinAttrMap != null) {
			return eqJoinAttrMap.get(table);
		} else {
			return null;
		}
	}

	/**
	 * @return the unionFind
	 */
	public UnionFind getUnionFind() {
		return unionFind;
	}


	/**
	 * @return the normal select expression
	 */
	public Expression getNormalSelect() {
		return normalSelect;
	}
	
	
	/**
	 * @return the normalJoin
	 */
	public Expression getNormalJoin() {
		return normalJoin;
	}


	@Override
	public void visit(NullValue nullValue) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void visit(Function function) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void visit(InverseExpression inverseExpression) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void visit(JdbcParameter jdbcParameter) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void visit(DoubleValue doubleValue) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void visit(LongValue longValue) {
		intValue = (int) longValue.getValue();
		isInt = true;
	}

	@Override
	public void visit(DateValue dateValue) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void visit(TimeValue timeValue) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void visit(TimestampValue timestampValue) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void visit(Parenthesis parenthesis) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void visit(StringValue stringValue) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void visit(Addition addition) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void visit(Division division) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void visit(Multiplication multiplication) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void visit(Subtraction subtraction) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void visit(AndExpression andExpression) {
		andExpression.getLeftExpression().accept(this);
		andExpression.getRightExpression().accept(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.conditional.OrExpression)
	 */
	@Override
	public void visit(OrExpression orExpression) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void visit(Between between) {
		throw new UnsupportedOperationException("not supported");
	}

	
	/**
	 * @param key the attribute that acts as an key in eqJoinAttrMap
	 * @param val the attribute to be added in the value in eqJoinAttrMap
	 */
	private void updateEqJoinAttrMap(String key, Column val) {
		if (eqJoinAttrMap.containsKey(key)) {
			eqJoinAttrMap.get(key).add(val);
		} else {
			List<Column> newAttrs = new LinkedList<>();
			newAttrs.add(val);
			eqJoinAttrMap.put(key, newAttrs);
		}
	}
	
	
	@Override
	public void visit(EqualsTo equalsTo) {
		equalsTo.getLeftExpression().accept(this);
		int leftValue = intValue;
		Column leftCol = col;
		boolean leftIsInt = isInt;
		equalsTo.getRightExpression().accept(this);
		int rightValue = intValue;
		Column rightCol = col;
		boolean rightIsInt = isInt;

		if (!leftIsInt && !rightIsInt) { //S.A = S.B
			unionFind.unite(unionFind.find(leftCol), unionFind.find(rightCol));
			
			updateEqJoinAttrMap(leftCol.getTable().getName(), leftCol);
			updateEqJoinAttrMap(rightCol.getTable().getName(), rightCol);
			
		} else if (!leftIsInt && rightIsInt) { // S.A = 2
			UnionElement left = unionFind.find(leftCol);
			left.setEquality(rightValue);
		} else if (leftIsInt && !rightIsInt) { // 2 = S.A
			UnionElement right = unionFind.find(rightCol);
			right.setEquality(leftValue);
		} else {
			if (normalSelect == null)
				normalSelect = equalsTo;
			else
				normalSelect = new AndExpression(equalsTo, normalSelect);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.GreaterThan)
	 */
	@Override
	public void visit(GreaterThan greaterThan) {
		greaterThan.getLeftExpression().accept(this);
		int leftValue = intValue;
		Column leftCol = col;
		boolean leftIsInt = isInt;
		greaterThan.getRightExpression().accept(this);
		int rightValue = intValue;
		Column rightCol = col;
		boolean rightIsInt = isInt;
		
		if (!leftIsInt && !rightIsInt) { // normal Join
			if (normalJoin == null)
				normalJoin = greaterThan;
			else
				normalJoin = new AndExpression(greaterThan, normalJoin);
		} else if (!leftIsInt && rightIsInt) {
			UnionElement left = unionFind.find(leftCol);
			left.setLower(rightValue + 1);
		} else if (leftIsInt && !rightIsInt) {
			UnionElement right = unionFind.find(rightCol);
			right.setUpper(leftValue - 1);
		} else {
			if (normalSelect == null)
				normalSelect = greaterThan;
			else
				normalSelect = new AndExpression(greaterThan, normalSelect);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.GreaterThanEquals)
	 */
	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		greaterThanEquals.getLeftExpression().accept(this);
		int leftValue = intValue;
		Column leftCol = col;
		boolean leftIsInt = isInt;
		greaterThanEquals.getRightExpression().accept(this);
		int rightValue = intValue;
		Column rightCol = col;
		boolean rightIsInt = isInt;

		if (!leftIsInt && !rightIsInt) { // normal Join
			if (normalJoin == null)
				normalJoin = greaterThanEquals;
			else
				normalJoin = new AndExpression(greaterThanEquals, normalJoin);
		} else if (!leftIsInt && rightIsInt) {
			UnionElement left = unionFind.find(leftCol);
			left.setLower(rightValue);
		} else if (leftIsInt && !rightIsInt) {
			UnionElement right = unionFind.find(rightCol);
			right.setUpper(leftValue);
		} else {
			if (normalSelect == null)
				normalSelect = greaterThanEquals;
			else
				normalSelect = new AndExpression(greaterThanEquals, normalSelect);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.InExpression)
	 */
	@Override
	public void visit(InExpression inExpression) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void visit(IsNullExpression isNullExpression) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void visit(LikeExpression likeExpression) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void visit(MinorThan minorThan) {
		minorThan.getLeftExpression().accept(this);
		int leftValue = intValue;
		Column leftCol = col;
		boolean leftIsInt = isInt;
		minorThan.getRightExpression().accept(this);
		int rightValue = intValue;
		Column rightCol = col;
		boolean rightIsInt = isInt;

		if (!leftIsInt && !rightIsInt) { // normal Join
			if (normalJoin == null)
				normalJoin = minorThan;
			else
				normalJoin = new AndExpression(minorThan, normalJoin);
		} else if (!leftIsInt && rightIsInt) {
			UnionElement left = unionFind.find(leftCol);
			left.setUpper(rightValue - 1);
		} else if (leftIsInt && !rightIsInt) {
			UnionElement right = unionFind.find(rightCol);
			right.setLower(leftValue + 1);
		} else {
			if (normalSelect == null)
				normalSelect = minorThan;
			else
				normalSelect = new AndExpression(minorThan, normalSelect);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.MinorThanEquals)
	 */
	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		minorThanEquals.getLeftExpression().accept(this);
		int leftValue = intValue;
		Column leftCol = col;
		boolean leftIsInt = isInt;
		minorThanEquals.getRightExpression().accept(this);
		int rightValue = intValue;
		Column rightCol = col;
		boolean rightIsInt = isInt;

		if (!leftIsInt && !rightIsInt) { // normal Join
			if (normalJoin == null)
				normalJoin = minorThanEquals;
			else
				normalJoin = new AndExpression(minorThanEquals, normalJoin);
		} else if (!leftIsInt && rightIsInt) {
			UnionElement left = unionFind.find(leftCol);
			left.setUpper(rightValue);
		} else if (leftIsInt && !rightIsInt) {
			UnionElement right = unionFind.find(rightCol);
			right.setLower(leftValue);
		} else {
			if (normalSelect == null)
				normalSelect = minorThanEquals;
			else
				normalSelect = new AndExpression(minorThanEquals, normalSelect);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.NotEqualsTo)
	 */
	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		notEqualsTo.getLeftExpression().accept(this);
		boolean leftIsInt = isInt;
		notEqualsTo.getRightExpression().accept(this);
		boolean rightIsInt = isInt;
		
		if (!leftIsInt && !rightIsInt) { // normal Join
			if (normalJoin == null)
				normalJoin = notEqualsTo;
			else
				normalJoin = new AndExpression(notEqualsTo, normalJoin);
		} else {
			if (normalSelect == null)
				normalSelect = notEqualsTo;
			else
				normalSelect = new AndExpression(notEqualsTo, normalSelect);
		}
	}

	/*
	 * First gets the corresponding index of the tableColumn in the current
	 * operator, then accesses the corresponding value in the tuple by the index.
	 */
	@Override
	public void visit(Column tableColumn) {
		col = tableColumn;
		isInt = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * statement.select.SubSelect)
	 */
	@Override
	public void visit(SubSelect subSelect) {
		throw new UnsupportedOperationException("not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.CaseExpression)
	 */
	@Override
	public void visit(CaseExpression caseExpression) {
		throw new UnsupportedOperationException("not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.WhenClause)
	 */
	@Override
	public void visit(WhenClause whenClause) {
		throw new UnsupportedOperationException("not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.ExistsExpression)
	 */
	@Override
	public void visit(ExistsExpression existsExpression) {
		throw new UnsupportedOperationException("not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.AllComparisonExpression)
	 */
	@Override
	public void visit(AllComparisonExpression allComparisonExpression) {
		throw new UnsupportedOperationException("not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.AnyComparisonExpression)
	 */
	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		throw new UnsupportedOperationException("not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.arithmetic.Concat)
	 */
	@Override
	public void visit(Concat concat) {
		throw new UnsupportedOperationException("not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.Matches)
	 */
	@Override
	public void visit(Matches matches) {
		throw new UnsupportedOperationException("not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.arithmetic.BitwiseAnd)
	 */
	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
		throw new UnsupportedOperationException("not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.arithmetic.BitwiseOr)
	 */
	@Override
	public void visit(BitwiseOr bitwiseOr) {
		throw new UnsupportedOperationException("not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.arithmetic.BitwiseXor)
	 */
	@Override
	public void visit(BitwiseXor bitwiseXor) {
		throw new UnsupportedOperationException("not supported");
	}

}
