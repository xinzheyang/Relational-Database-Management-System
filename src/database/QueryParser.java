/**
 * 
 */
package database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import operator.Operator;
import visitor.DBStatementVisitor;


/**
 * @author xinzheyang
 * This class creates an instance of a query parser, which
 * parses according to input and output files and using a 
 * while loop to go over all the queries in the file
 */
public class QueryParser {
	private String queriesFile;
	private String output;
	public QueryParser(String in, String out) {
		queriesFile = in;
		output = out;
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
					File file = new File(output + File.separator + "query" + count++);
					FileWriter fw = new FileWriter(file); //the instant the file is opened for writing, original
					//contents are overwrite. We write nothing in this case since operator == null

					/* This logic will make sure that the file 
					 * gets created if it is not present at the
					 * specified location*/
					if (!file.exists()) {
						file.createNewFile();
					}
				} else {
					operator.dump(output + File.separator + "query" + count++);
				}
			}
		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}
}
