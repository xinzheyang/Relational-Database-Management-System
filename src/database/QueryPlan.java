/**
 * 
 */
package database;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operator.DupElimOperator;
import operator.Operator;

/**
 * @author xinzheyang
 *
 */
public class QueryPlan {
	private PlainSelect body;
	public QueryPlan(Statement statement) {
		Select select = (Select) statement;
		body = (PlainSelect) select.getSelectBody();
		
	}
	public Operator buildQueryPlan() {
		Operator operator;
		if (body.getDistinct() != null) {
			operator = new DupElimOperator();
		}
	}
}
