/**
 *
 */
package operator;

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
 *
 */
public class SelectOperator extends Operator {

	public Operator childOp; //child operator of where the source for getNextTuple() comes from.
	public Expression ex;
	public EvaluateExpVisitor visitor;
//	private HashMap<String, Integer> columnIndexMap;

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
		// TODO Auto-generated method
		Tuple curr;
		while((curr = childOp.getNextTuple()) != null) {
			visitor.setCurrTuple(curr);
			visitor.setOperator(this);
//			visitor.visit(curr);
			ex.accept(visitor);
			if(visitor.getReturnBoolValue() == true) {
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
		// TODO Auto-generated method stub
		childOp.reset();
	}
//
//	@Override
//	public void dump(String fileOut) {
//		// TODO Auto-generated method stub
//		super.dump(fileOut);
//	}

}
