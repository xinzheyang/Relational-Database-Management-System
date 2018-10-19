/**
 * 
 */
package visitor;

import logicaloperator.LogicalOperator;
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
import physicaloperator.Operator;;


/**
 * @author xinzheyang
 * A visitor that implements the StatementVisitor.
 * It only implements visit(Select) because other classes are not
 * supported in this system.
 */
public class DBStatementVisitor implements StatementVisitor{
	private LogicalOperator operator = null;

	public LogicalOperator getOperator() {
		return operator;
	}

	public void setOperator(LogicalOperator operator) {
		this.operator = operator;
	}

	/* (non-Javadoc)
	 * @see net.sf.jsqlparser.statement.StatementVisitor#visit(net.sf.jsqlparser.statement.select.Select)
	 */
	@Override
	public void visit(Select select) {
		PlainSelect body = (PlainSelect) select.getSelectBody();
		DBSelectVisitor dbSelectVisitor = new DBSelectVisitor();
		if (body != null) {
			body.accept(dbSelectVisitor);
			operator = dbSelectVisitor.getOperator();
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
