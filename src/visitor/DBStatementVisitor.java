/**
 * 
 */
package visitor;

import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import operator.Operator;;

/**
 * @author sitianchen
 *
 */
public class DBStatementVisitor implements StatementVisitor{
	private Operator operator = null;

    public Operator getOp() {
        return operator;
    }
	@Override
	public void visit(Select select) {
		PlainSelect body = (PlainSelect) select.getSelectBody();
		DBSelectVisitor dbSelectVisitor = new DBSelectVisitor();
		if (body != null) {
			body.accept(dbSelectVisitor);
			operator = dbSelectVisitor.getOp();
		}
		
	}

	@Override
	public void visit(Delete delete) {
		throw new UnsupportedOperationException("not supported");
		
	}

	@Override
	public void visit(Update update) {
		throw new UnsupportedOperationException("not supported");
		
	}

	@Override
	public void visit(Insert insert) {
		throw new UnsupportedOperationException("not supported");
		
	}

	@Override
	public void visit(Replace replace) {
		throw new UnsupportedOperationException("not supported");
		
	}

	@Override
	public void visit(Drop drop) {
		throw new UnsupportedOperationException("not supported");
		
	}

	@Override
	public void visit(Truncate truncate) {
		throw new UnsupportedOperationException("not supported");
		
	}

	@Override
	public void visit(CreateTable createTable) {
		throw new UnsupportedOperationException("not supported");
		
	}

}
