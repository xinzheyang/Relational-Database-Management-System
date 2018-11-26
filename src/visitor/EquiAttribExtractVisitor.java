/**
 * 
 */
package visitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import database.Tuple;
import logicaloperator.LogicalOperator;
import logicaloperator.LogicalScanOperator;
import logicaloperator.LogicalSelectOperator;
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
public class EquiAttribExtractVisitor implements ExpressionVisitor {
	private HashSet<LogicalOperator> leftChildren;
	private HashSet<String> leftTableRefs;
	private LogicalOperator rightOp;
	private String rightTableRef;
	private Stack<Column> columnStack;
	private List<String[]> extractedAttribs;
	
	public EquiAttribExtractVisitor(HashSet<LogicalOperator> leftChildren, LogicalOperator rightOp) {
		this.leftChildren = leftChildren;
		this.rightOp = rightOp;
		leftTableRefs = new HashSet<String>();
		for (LogicalOperator child : leftChildren) {
			String ref = "";
			if (child instanceof LogicalScanOperator) {
				ref = ((LogicalScanOperator) child).getReference();
			} else if (child instanceof LogicalSelectOperator) {
				ref = ((LogicalSelectOperator) child).getReference();
			} 
			leftTableRefs.add(ref);
		}
		
		if (rightOp instanceof LogicalScanOperator) {
			rightTableRef = ((LogicalScanOperator) rightOp).getReference();
		} else if (rightOp instanceof LogicalSelectOperator) {
			rightTableRef = ((LogicalSelectOperator) rightOp).getReference();
		}
		extractedAttribs = new ArrayList<String[]>();
	}
	
	//--SETTERS AND GETTERS--

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

	/** No-op
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.LongValue)
	 */
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

	/**
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.conditional.AndExpression)
	 */
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
		//TODO
		equalsTo.getLeftExpression().accept(this);
		equalsTo.getRightExpression().accept(this);
		if (columnStack.size() == 2) {
			Column colAttrib1 = columnStack.pop();
			Column colAttrib2 = columnStack.pop();
			if (colAttrib1.getTable().getName().equals(rightTableRef)) {
				if (this.leftTableRefs.contains(colAttrib2.getTable().getName())) {
					String[] a
				}
			}
		}
		
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

	@Override
	public void visit(Column tableColumn) {
		//TODO
		columnStack.push(tableColumn);
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
