/**
 * 
 */
package database;

import java.io.FileReader;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import operator.Operator;
import visitor.DBStatementVisitor;

/**
 * @author sitianchen
 *
 */
public class QueryParser {
	private String queriesFile;
	
	public QueryParser(String fname) {
		queriesFile = fname;
	}
	
	public void parse() {
		try {
			DBCatalog.getCatalog().parseSchema("db");
			CCJSqlParser parser = new CCJSqlParser(new FileReader(queriesFile));
			Statement statement;
			int count = 1;
			while ((statement = parser.Statement()) != null) {
				DBStatementVisitor dbStatementVisitor = new DBStatementVisitor();
				statement.accept(dbStatementVisitor);
				Operator operator = dbStatementVisitor.getOperator();
				operator.dump("query" + count++);
			}
		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}
}
