/**
 *
 */
package physicaloperator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import database.DBCatalog;
import java.util.HashMap;
import database.Tuple;
import visitor.EvaluateExpVisitor;
import net.sf.jsqlparser.expression.Expression;

/**
 * @author sitianchen
 * The select operator selects tuples from its child operator that satisfy certain 
 * conditions.
 */
public class SelectOperator extends Operator {

	private Operator childOp; //child operator of where the source for getNextTuple() comes from.
	private Expression ex;
	private EvaluateExpVisitor visitor;
//	private HashMap<String, Integer> columnIndexMap;

	/**Gettor for childOp
	 * @return the child operator of this object
	 */
	public Operator getChildOp() {
		return childOp;
	}

	/**
	 * Setter for the childOp
	 * @param childOp the operator to be set as childOp
	 */
	public void setChildOp(Operator childOp) {
		this.childOp = childOp;
	}

	/**
	 * Getter for ex
	 * @return ex of the object
	 */
	public Expression getEx() {
		return ex;
	}

	/**
	 * Setter for ex
	 * @param ex the expression to be set as ex
	 */
	public void setEx(Expression ex) {
		this.ex = ex;
	}

	/**Getter for visitor
	 * @return visitor of the object
	 */
	public EvaluateExpVisitor getVisitor() {
		return visitor;
	}

	/** Setter for visitor
	 * @param visitor the visitor to be set
	 */
	public void setVisitor(EvaluateExpVisitor visitor) {
		this.visitor = visitor;
	}

	/**
	 * Initializes the select operator
	 * @param op the child operator
	 * @param exp the selection condition
	 */
	public SelectOperator(Operator op, Expression exp) {
		childOp = op;
		ex = exp;
		visitor = new EvaluateExpVisitor();
		columnIndexMap = new HashMap<String, Integer>(childOp.getColumnIndexMap()); //same col index map as child operator
	}
	
	/* Get the input parameter's mapped index in columnIndexMap.
	 * 
	 */
//	public int getColumnIndex(String colName) {
//		return columnIndexMap.get(colName);
//	}

	/* Grabs the next tuple from the scan and check if that tuple passes the
	 * selection condition, and if so output it. If the tuple doesn’t pass
	 * the selection condition, the selection operator will continue pulling
	 * tuples from the scan until either it finds one that passes or it receives
	 * null (i.e. the scan runs out of output).
	 */
	@Override
	public Tuple getNextTuple() {
		Tuple curr;
		while((curr = childOp.getNextTuple()) != null) {
			visitor.setCurrTuple(curr);
			visitor.setOperator(this);
//			visitor.visit(curr);
			ex.accept(visitor);
			if(visitor.getReturnBoolValue()) {
				return curr;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see database.Operator#reset()
	 */
	@Override
	public void reset() {
		childOp.reset();
	}

	@Override
	public void reset(int index) {
		throw new UnsupportedOperationException("not supported");
		
	}

}
