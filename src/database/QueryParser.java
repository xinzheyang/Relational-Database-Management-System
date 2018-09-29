/**
 * 
 */
package database;

import java.io.FileReader;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operator.Operator;

/**
 * @author sitianchen
 *
 */
public class QueryParser {
//	private final String queriesFile;
//	
//	public QueryParser(String fname) {
//		queriesFile = fname;
//	}
//	
//	public void parse() {
//		
//	}
	public static void parse(String queriesFile) {
		try {
			CCJSqlParser parser = new CCJSqlParser(new FileReader(queriesFile));
			Statement statement;
			while ((statement = parser.Statement()) != null) {
				Operator operator = QueryPlan.buildQueryPlan(statement);
				
//				statement.accept(statementVisitor);
//				System.out.println("Read statement: " + statement);
//				Select select = (Select) statement;
//				PlainSelect body = (PlainSelect) select.getSelectBody();
//				System.out.println("Select body is " + body);
//				System.out.println("Select item is " + body.getSelectItems());
//				System.out.println("From item is " + body.getFromItem());
//				System.out.println("Joins : " + body.getJoins());
//				System.out.println("Where : " + body.getWhere());
//				System.out.println(" " + body.getDistinct());
			}
		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}
}
