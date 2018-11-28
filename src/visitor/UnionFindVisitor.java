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

	private HashMap<String, List<Column>> attrMap;
	/**
	 * 
	 */
	public UnionFindVisitor() {
		unionFind = new UnionFind();
		eqJoinAttrMap = new HashMap<>();
		attrMap = new HashMap<>();
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
	
	
	public List<Column> getAttrByReference(String table) {
		if (attrMap != null) {
			return attrMap.get(table);
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
	
	private void updateAttrMap(String key, Column val) {
		if (attrMap.containsKey(key)) {
			attrMap.get(key).add(val);
		} else {
			List<Column> newAttrs = new LinkedList<>();
			newAttrs.add(val);
			attrMap.put(key, newAttrs);
		}
	}
	private void updateUnused(Boolean isNormal, Expression toBeAcc) {
		if (!isNormal) {
			if (normalSelect == null)
				normalSelect = toBeAcc;
			else
				normalSelect = new AndExpression(toBeAcc, normalSelect);
		} else {
			if (normalJoin == null)
				normalJoin = toBeAcc;
			else
				normalJoin = new AndExpression(toBeAcc, normalJoin);
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
			updateAttrMap(leftCol.getTable().getName(), leftCol);
			updateAttrMap(rightCol.getTable().getName(), rightCol);
		} else if (!leftIsInt && rightIsInt) { // S.A = 2
			UnionElement left = unionFind.find(leftCol);
			left.setEquality(rightValue);
			updateAttrMap(leftCol.getTable().getName(), leftCol);
		} else if (leftIsInt && !rightIsInt) { // 2 = S.A
			UnionElement right = unionFind.find(rightCol);
			right.setEquality(leftValue);
			updateAttrMap(rightCol.getTable().getName(), rightCol);
		} else {
			updateUnused(false, equalsTo);
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
			if (!leftCol.getTable().getWholeTableName().equals(rightCol.getTable().getWholeTableName())) {
				updateUnused(true, greaterThan);
			} else {
				updateUnused(false, greaterThan);
			}
		} else if (!leftIsInt && rightIsInt) {
			UnionElement left = unionFind.find(leftCol);
			left.setLower(rightValue + 1);
			updateAttrMap(leftCol.getTable().getName(), leftCol);
		} else if (leftIsInt && !rightIsInt) {
			UnionElement right = unionFind.find(rightCol);
			right.setUpper(leftValue - 1);
			updateAttrMap(rightCol.getTable().getName(), rightCol);
		} else {
			updateUnused(false, greaterThan);
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
			if (!leftCol.getTable().getWholeTableName().equals(rightCol.getTable().getWholeTableName())) {
				updateUnused(true, greaterThanEquals);
			} else {
				updateUnused(false, greaterThanEquals);
			}
		} else if (!leftIsInt && rightIsInt) {
			UnionElement left = unionFind.find(leftCol);
			left.setLower(rightValue);
			updateAttrMap(leftCol.getTable().getName(), leftCol);
		} else if (leftIsInt && !rightIsInt) {
			UnionElement right = unionFind.find(rightCol);
			right.setUpper(leftValue);
			updateAttrMap(rightCol.getTable().getName(), rightCol);
		} else {
			updateUnused(false, greaterThanEquals);
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
			if (!leftCol.getTable().getWholeTableName().equals(rightCol.getTable().getWholeTableName())) {
				updateUnused(true, minorThan);
			} else {
				updateUnused(false, minorThan);
			}
		} else if (!leftIsInt && rightIsInt) {
			UnionElement left = unionFind.find(leftCol);
			left.setUpper(rightValue - 1);
			updateAttrMap(leftCol.getTable().getName(), leftCol);
		} else if (leftIsInt && !rightIsInt) {
			UnionElement right = unionFind.find(rightCol);
			right.setLower(leftValue + 1);
			updateAttrMap(rightCol.getTable().getName(), rightCol);
		} else {
			updateUnused(false, minorThan);
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
			if (!leftCol.getTable().getWholeTableName().equals(rightCol.getTable().getWholeTableName())) {
				updateUnused(true, minorThanEquals);
			} else {
				updateUnused(false, minorThanEquals);
			}
		} else if (!leftIsInt && rightIsInt) {
			UnionElement left = unionFind.find(leftCol);
			left.setUpper(rightValue);
			updateAttrMap(leftCol.getTable().getName(), leftCol);
		} else if (leftIsInt && !rightIsInt) {
			UnionElement right = unionFind.find(rightCol);
			right.setLower(leftValue);
			updateAttrMap(rightCol.getTable().getName(), rightCol);
		} else {
			updateUnused(false, minorThanEquals);
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
		Column leftCol = col;
		notEqualsTo.getRightExpression().accept(this);
		boolean rightIsInt = isInt;
		Column rightCol = col;
		
		if (!leftIsInt && !rightIsInt) { // normal Join
			if (!leftCol.getTable().getWholeTableName().equals(rightCol.getTable().getWholeTableName())) {
				updateUnused(true, notEqualsTo);
			} else {
				updateUnused(false, notEqualsTo);
			}
		} else {
			updateUnused(false, notEqualsTo);
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
