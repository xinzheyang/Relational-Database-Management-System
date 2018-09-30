/**
 * 
 */
package visitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.Union;
import operator.Operator;
import java.util.List;
/**
 * @author sitianchen
 *
 */
public class DBSelectVisitor implements SelectVisitor {
	private Operator operator = null;

    public Operator getOp() {
        return operator;
    }
    
	@SuppressWarnings("unchecked")
	@Override
	public void visit(PlainSelect plainSelect) {
		Distinct distinct = plainSelect.getDistinct();
		FromItem fromItem = plainSelect.getFromItem();
		List<SelectItem> selectItems = plainSelect.getSelectItems();
		List<Join> joins = plainSelect.getJoins();
		List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
		Expression expression = plainSelect.getWhere();
//		DBSelectVisitor dbSelectVisitor = new DBSelectVisitor();
	}

	@Override
	public void visit(Union union) {
		throw new UnsupportedOperationException("not supported");
		
	}

}
