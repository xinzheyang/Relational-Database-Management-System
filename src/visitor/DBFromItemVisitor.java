/**
 * 
 */
package visitor;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import operator.Operator;
/**
 * @author sitianchen
 *
 */
public class DBFromItemVisitor implements FromItemVisitor {
	private Operator operator = null;

    public Operator getOp() {
        return operator;
    }
	@Override
	public void visit(Table tableName) {
		// TODO Auto-generated method stub
		
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
