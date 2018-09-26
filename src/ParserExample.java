import java.io.FileReader;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

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
				System.out.println("Select item is " + body.getSelectItems());
				System.out.println("From item is " + body.getFromItem());
				System.out.println("Joins : " + body.getJoins());
				System.out.println("Where : " + body.getWhere());
				System.out.println(" " + body.getDistinct());
			}
		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}
}
