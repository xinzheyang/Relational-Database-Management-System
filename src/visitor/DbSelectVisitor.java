/**
 * 
 */
package visitor;

import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.Union;

/**
 * @author sitianchen
 *
 */
public class DbSelectVisitor implements SelectVisitor {

	@Override
	public void visit(PlainSelect plainSelect) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Union union) {
		throw new UnsupportedOperationException("not supported");
		
	}

}
