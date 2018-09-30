/**
 * 
 */
package visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
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
import operator.Operator;

/**
 * @author sitianchen
 * Expression Visitor for parsing the WHERE clause of a query that involve
 * Joins into separately independent expressions.
 */
public class ParseConjunctExpVisitor implements ExpressionVisitor {
	
//	private Expression[] indepExps;
//	private ArrayList<ArrayList<Table>> tablesInvolved;
	private Stack<String> tbStack; //stack keeping record of tables last involved
	private HashMap<String[], Expression> joinMap; //mapping tables referenced --> Join Condition
	private HashMap<String, Expression> selectMap; //mapping tables referenced --> Select Condition
	private Operator root; //nearest top root operator of the current expression involved
	private boolean alwaysFalse; //
	
	public Operator getOperator() {
		return root;
	}
	
	public void setOperator(Operator op) {
		this.root = op;
	}
	
	public HashMap<String[], Expression> getJoinMap() {
		return joinMap;
	}
	
	public HashMap<String, Expression> getSelectMap() {
		return selectMap;
	}
	
	public Expression getJoinCondition(String tb1, String tb2) {
		String[] key = new String[2];
		key[0] = tb1;
		key[1] = tb2;
		Arrays.sort(key);
		return joinMap.get(key);
	}
	
	public Expression getSelectCondition(String tb) {
		return selectMap.get(tb);
	}
	
	public void visitBinExp(BinaryExpression binExp) {
		binExp.getLeftExpression().accept(this);
		binExp.getRightExpression().accept(this);
	}
	
	public boolean isAlwaysFalse() {
		return alwaysFalse;
	}
	
	/* Visits operator that's one of =, ! =, <, >, <=, >=.
	 */
	public void visitOp(Expression op) {
		String tb1 = "";
		String tb2 = "";
		if (!tbStack.isEmpty()) {
			tb1 = tbStack.pop();
			if (!tbStack.isEmpty()) {
				tb2 = tbStack.pop();
			}
		}
		if (tb1 != "" && tb2 != "") { //Join Condition
			String[] key = new String[2];
			key[0] = tb1;
			key[1] = tb2;
			Arrays.sort(key);
			if (!joinMap.containsKey(key)) {
				joinMap.put(key, op);
			}
			else {
				Expression newExp = new AndExpression(joinMap.get(key), op);
				joinMap.put(key, newExp);
			}
		} 
		else if (tb1 != "" || tb2 != "") { //Select Condition
			String key = "";
			if (tb1 != "") {
				key = tb1;
			}
			else {
				key = tb2;
			}
			if (!selectMap.containsKey(key)) {
				selectMap.put(key, op);
			}
			else {
				Expression newExp = new AndExpression(selectMap.get(key), op);
				selectMap.put(key, newExp);
			}
		}
		else { //TODO //neither select of join, both sides should be long values
			try {
				EvaluateExpVisitor eval = new EvaluateExpVisitor();
				op.accept(eval);
				if (!eval.getReturnBoolValue()) { //if, after evaluation the result is false
					alwaysFalse = true; //where condition always false
				}
				//if is true, simply ignore the expression
			}
			catch(Exception e) {
				System.out.println("This try block should never fail.");
			}
		}
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
		//no-op, since no table referenced
		return;
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
	    visitBinExp(andExpression);
	    
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
		visitOp(equalsTo);
	}

	@Override
	public void visit(GreaterThan greaterThan) {
		visitOp(greaterThan);
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		visitOp(greaterThanEquals);

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
		visitOp(minorThan);
		
	}

	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		visitOp(minorThanEquals);
		
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		visitOp(notEqualsTo);
		
	}

	@Override
	public void visit(Column tableColumn) {
		this.tbStack.push(tableColumn.getTable().getName());
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
