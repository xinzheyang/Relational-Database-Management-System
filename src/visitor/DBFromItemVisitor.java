/**
 * 
 */
package visitor;

import java.io.FileNotFoundException;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import operator.Operator;
import operator.ScanOperator;
/**
 * @author xinzheyang
 * A visitor that implements the FromItemVisitor.
 * It only implements visit(Table) because other classes are not
 * supported in this system.
 */
public class DBFromItemVisitor implements FromItemVisitor {
	private Operator operator = null;

	/**
	 * @return operator
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * @param operator
	 */
	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.statement.select.FromItemVisitor#visit(net.sf.jsqlparser.schema.Table)
	 */
	@Override
	public void visit(Table tableName) {
		try {
			ScanOperator scanOperator = new ScanOperator(tableName.getName());
			operator = scanOperator;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.statement.select.FromItemVisitor#visit(net.sf.jsqlparser.statement.select.SubSelect)
	 */
	@Override
	public void visit(SubSelect subSelect) {
		throw new UnsupportedOperationException("not supported");
		
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.statement.select.FromItemVisitor#visit(net.sf.jsqlparser.statement.select.SubJoin)
	 */
	@Override
	public void visit(SubJoin subjoin) {
		throw new UnsupportedOperationException("not supported");
		
	}

}
