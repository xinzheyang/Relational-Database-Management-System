/**
 * 
 */
package visitor;

import java.util.ArrayList;

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
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * @author sitianchen
 * Expression Visitor for parsing the WHERE clause of a query that involve
 * Joins into separately independent expressions.
 */
public class ParseConjunctExpVisitor implements ExpressionVisitor {
	
	private Expression[] indepExps;
	private ArrayList<ArrayList<Table>> tablesInvolved;
	
	public Expression[] getIndepExps() {
		return indepExps;
	}
	
	public ArrayList<ArrayList<Table>> getTablesInvolved() {
		return tablesInvolved;
	}

	@Override
	public void visit(NullValue nullValue) {
		// everything empty
		tablesInvolved = new ArrayList<ArrayList<Table>>();
		indepExps = new Expression[0];
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
		// TODO Auto-generated method stub
		tablesInvolved = new ArrayList<ArrayList<Table>>();
		indepExps = new Expression[1];
		indepExps[0] = longValue;
		
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
	    andExpression.getRightExpression().accept(this);
	    Expression[] rightExps = indepExps;
	    ArrayList<ArrayList<Table>> rightTables = tablesInvolved;
	    
	    andExpression.getLeftExpression().accept(this);
		Expression[] leftExps = indepExps;
		ArrayList<ArrayList<Table>> leftTables = tablesInvolved;
	    
	    indepExps = new Expression[leftExps.length + rightExps.length];
	    System.arraycopy(leftExps, 0, indepExps, 0, leftExps.length);
	    System.arraycopy(rightExps, 0, indepExps, leftExps.length, rightExps.length);
	    //concatenate left and right expression arrays
	    for (ArrayList<Table> tbs : rightTables) {
	    	leftTables.add(tbs);
	    }
	    tablesInvolved = leftTables;
	    //concatenate left and right tables
	    
	}

	@Override
	public void visit(OrExpression orExpression) {
		throw new UnsupportedOperationException("not supported");
		
	}

	@Override
	public void visit(Between between) {
		throw new UnsupportedOperationException("not supported");
		
	}

	@Override
	public void visit(EqualsTo equalsTo) {
		equalsTo.getRightExpression().accept(this);
		ArrayList<ArrayList<Table>> rightTables = tablesInvolved;
		assert rightTables.size() == 1;
		equalsTo.getLeftExpression().accept(this);
		ArrayList<ArrayList<Table>> leftTables = tablesInvolved;
		assert leftTables.size() == 1;
		indepExps = new Expression[1];
		indepExps[0] = equalsTo;
		for (Table tb : rightTables.get(0)) {
			leftTables.get(0).add(tb);
		}
		tablesInvolved = leftTables;
	}

	@Override
	public void visit(GreaterThan greaterThan) {
		// TODO Auto-generated method stub
		greaterThan.getRightExpression().accept(this);
		ArrayList<ArrayList<Table>> rightTables = tablesInvolved;
		assert rightTables.size() == 1;
		greaterThan.getLeftExpression().accept(this);
		ArrayList<ArrayList<Table>> leftTables = tablesInvolved;
		assert leftTables.size() == 1;
		indepExps = new Expression[1];
		indepExps[0] = greaterThan;
		for (Table tb : rightTables.get(0)) {
			leftTables.get(0).add(tb);
		}
		tablesInvolved = leftTables;
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		// TODO Auto-generated method stub
		greaterThanEquals.getRightExpression().accept(this);
		ArrayList<ArrayList<Table>> rightTables = tablesInvolved;
		assert rightTables.size() == 1;
		greaterThanEquals.getLeftExpression().accept(this);
		ArrayList<ArrayList<Table>> leftTables = tablesInvolved;
		assert leftTables.size() == 1;
		indepExps = new Expression[1];
		indepExps[0] = greaterThanEquals;
		for (Table tb : rightTables.get(0)) {
			leftTables.get(0).add(tb);
		}
		tablesInvolved = leftTables;
	}

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
		// TODO Auto-generated method stub
		minorThan.getRightExpression().accept(this);
		ArrayList<ArrayList<Table>> rightTables = tablesInvolved;
		assert rightTables.size() == 1;
		minorThan.getLeftExpression().accept(this);
		ArrayList<ArrayList<Table>> leftTables = tablesInvolved;
		assert leftTables.size() == 1;
		indepExps = new Expression[1];
		indepExps[0] = minorThan;
		for (Table tb : rightTables.get(0)) {
			leftTables.get(0).add(tb);
		}
		tablesInvolved = leftTables;
	}

	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		// TODO Auto-generated method stub
		minorThanEquals.getRightExpression().accept(this);
		ArrayList<ArrayList<Table>> rightTables = tablesInvolved;
		assert rightTables.size() == 1;
		minorThanEquals.getLeftExpression().accept(this);
		ArrayList<ArrayList<Table>> leftTables = tablesInvolved;
		assert leftTables.size() == 1;
		indepExps = new Expression[1];
		indepExps[0] = minorThanEquals;
		for (Table tb : rightTables.get(0)) {
			leftTables.get(0).add(tb);
		}
		tablesInvolved = leftTables;
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		// TODO Auto-generated method stub
		notEqualsTo.getRightExpression().accept(this);
		ArrayList<ArrayList<Table>> rightTables = tablesInvolved;
		assert rightTables.size() == 1;
		notEqualsTo.getLeftExpression().accept(this);
		ArrayList<ArrayList<Table>> leftTables = tablesInvolved;
		assert leftTables.size() == 1;
		indepExps = new Expression[1];
		indepExps[0] = notEqualsTo;
		for (Table tb : rightTables.get(0)) {
			leftTables.get(0).add(tb);
		}
		tablesInvolved = leftTables;
		
	}

	@Override
	public void visit(Column tableColumn) {
		// TODO Auto-generated method stub
		Table curTable = tableColumn.getTable();
		tablesInvolved = new ArrayList<ArrayList<Table>>();
		ArrayList<Table> zeroth = new ArrayList<Table>();
		zeroth.set(0, curTable);
		tablesInvolved.set(0, zeroth);
		indepExps = new Expression[1];
		indepExps[0] = tableColumn;
	}

	@Override
	public void visit(SubSelect subSelect) {
		throw new UnsupportedOperationException("not supported");
		
	}

	@Override
	public void visit(CaseExpression caseExpression) {
		throw new UnsupportedOperationException("not supported");
		
	}

	@Override
	public void visit(WhenClause whenClause) {
		throw new UnsupportedOperationException("not supported");
		
	}

	@Override
	public void visit(ExistsExpression existsExpression) {
		throw new UnsupportedOperationException("not supported");
		
	}

	@Override
	public void visit(AllComparisonExpression allComparisonExpression) {
		throw new UnsupportedOperationException("not supported");
		
	}

	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		throw new UnsupportedOperationException("not supported");
		
	}

	@Override
	public void visit(Concat concat) {
		throw new UnsupportedOperationException("not supported");
		
	}

	@Override
	public void visit(Matches matches) {
		throw new UnsupportedOperationException("not supported");
		
	}

	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
		throw new UnsupportedOperationException("not supported");
		
	}

	@Override
	public void visit(BitwiseOr bitwiseOr) {
		throw new UnsupportedOperationException("not supported");
		
	}

	@Override
	public void visit(BitwiseXor bitwiseXor) {
		throw new UnsupportedOperationException("not supported");
		
	}

}
