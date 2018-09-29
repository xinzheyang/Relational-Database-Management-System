/**
 * 
 */
package visitor;

import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.Union;
import operator.Operator;

/**
 * @author sitianchen
 *
 */
public class DBSelectVisitor implements SelectVisitor {
	private Operator operator = null;

    public Operator getOp() {
        return operator;
    }
	@Override
	public void visit(PlainSelect plainSelect) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Union union) {
		throw new UnsupportedOperationException("not supported");
		
	}

}
