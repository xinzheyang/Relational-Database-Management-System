/**
 * 
 */
package database;

import java.io.FileReader;
import java.io.IOException;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operator.ScanOperator;

/**
 * @author sitianchen
 *
 */
public class ScanMain {
	private static String queriesFile = "queries.sql";
	private static String dbDir = "db";
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		DBCatalog.getCatalog().parseSchema(dbDir);
		try {
			CCJSqlParser parser = new CCJSqlParser(new FileReader(queriesFile));
			Statement statement;
			int count = 1;
			while ((statement = parser.Statement()) != null) {
				System.out.println("Read statement: " + statement);
				Select select = (Select) statement;
				PlainSelect body = (PlainSelect) select.getSelectBody();
				String tableName = ((Table) body.getFromItem()).getName();
				ScanOperator scan = new ScanOperator(tableName);
				scan.dump("query" + count);
			}

		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}

	}
}
