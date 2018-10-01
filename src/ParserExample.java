import java.awt.print.Printable;
import java.io.FileReader;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import java.util.List;
/**
 * Example class for getting started with JSQLParser. Reads SQL statements from
 * a file and prints them to screen; then extracts SelectBody from each query
 * and also prints it to screen.
 * 
 * @author Lucja Kot
 */
public class ParserExample {

	private static final String queriesFile = "queries.sql";

	public static void main(String[] args) {
		try {
			CCJSqlParser parser = new CCJSqlParser(new FileReader(queriesFile));
			Statement statement;
			while ((statement = parser.Statement()) != null) {
//				statement.accept(statementVisitor);
				System.out.println("Read statement: " + statement);
				Select select = (Select) statement;
				PlainSelect body = (PlainSelect) select.getSelectBody();
				System.out.println("Select body is " + body);
				
				List<SelectItem> listofitems = body.getSelectItems();
				System.out.println("Selects item is " + listofitems);
//				for(SelectItem item : listofitems) {
//					if (item instanceof AllColumns) {
//						System.out.println("===========allclomns");
//					}
//					if (item instanceof AllTableColumns) {
//						System.out.println("============alltableclomns");
//					}
//					if (item instanceof SelectExpressionItem) {
//						System.out.println("==============SelectExpressionItem");
//					}
//				}
				System.out.println("From item is " + body.getFromItem());
				System.out.println("Table alias is " + body.getFromItem().getAlias());
				System.out.println("Joins : " + body.getJoins());
				List<Join> joins = body.getJoins();
				if (joins != null) {
					for (Join j:joins) {
						System.out.println(j.getRightItem());
					}
				}
				
				System.out.println("Where : " + body.getWhere());
				List<SelectItem> items = body.getSelectItems();
				System.out.println("=================");
				for (SelectItem item : items) {
					if (item instanceof SelectExpressionItem) {
						System.out.println(((SelectExpressionItem) item).getExpression());
					}
				}
				System.out.println("=================");
//					System.out.println("Left expression : " + ((MinorThan) body.getWhere()).getLeftExpression());
//					System.out.println("Left expression Column : " + ((Column) ((MinorThan) body.getWhere()).getLeftExpression()).getColumnName());
//					System.out.println("Left expression Table Schema : " + ((Column) ((MinorThan) body.getWhere()).getLeftExpression()).getTable().getAlias());
//				}
				System.out.println(" " + body.getDistinct());
				System.out.println(body.getOrderByElements());
				Expression expression = new AndExpression(null, null);
				System.out.println(expression);
			}
		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}
}
