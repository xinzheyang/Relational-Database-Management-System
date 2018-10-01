/**
 * 
 */
package database;

import java.io.File;
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
	private String output;
	public QueryParser(String in, String out) {
		queriesFile = in;
	}
	
	public void parse() {
		try {
			CCJSqlParser parser = new CCJSqlParser(new FileReader(queriesFile));
			Statement statement;
			int count = 1;
			while ((statement = parser.Statement()) != null) {
				DBStatementVisitor dbStatementVisitor = new DBStatementVisitor();
				statement.accept(dbStatementVisitor);
				Operator operator = dbStatementVisitor.getOperator();
				if (operator == null) {
					File file = new File(output + "query" + count++);

					/* This logic will make sure that the file 
					 * gets created if it is not present at the
					 * specified location*/
					if (!file.exists()) {
						file.createNewFile();
					}
				} else {
					operator.dump(output + "query" + count++);
				}
			}
		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}
}
