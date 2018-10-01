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
 * @author sitianchen
 *
 */
public class DBFromItemVisitor implements FromItemVisitor {
	private Operator operator = null;

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	@Override
	public void visit(Table tableName) {
		try {
			ScanOperator scanOperator = new ScanOperator(tableName.getName());
			operator = scanOperator;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void visit(SubSelect subSelect) {
		throw new UnsupportedOperationException("not supported");
		
	}

	@Override
	public void visit(SubJoin subjoin) {
		throw new UnsupportedOperationException("not supported");
		
	}

}
