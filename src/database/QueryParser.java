/**
 * 
 */
package database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import logicaloperator.LogicalOperator;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import physicaloperator.Operator;
import visitor.DBStatementVisitor;
import visitor.PhysicalPlanBuilder;


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


	/**
	 * The parse method creates a File reader and makes each statement
	 * accept the dbStatementVisitor and dumps the result into output dir
	 * If operator is null, output an empty file
	 */
	public void parse() {
		try {
			CCJSqlParser parser = new CCJSqlParser(new FileReader(queriesFile));
			Statement statement;
			int count = 1;
			while ((statement = parser.Statement()) != null) {
				try {
					DBStatementVisitor dbStatementVisitor = new DBStatementVisitor();
					statement.accept(dbStatementVisitor);
					LogicalOperator logicalOperator = dbStatementVisitor.getOperator();
					PhysicalPlanBuilder physicalPlanBuilder = new PhysicalPlanBuilder();
					logicalOperator.accept(physicalPlanBuilder);
					Operator operator = physicalPlanBuilder.getOperator();
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
						operator.dumpHumanReadable(output + File.separator + "query" + count++);
					}
				} catch (Exception e) {
					File file = new File(output + File.separator + "query" + count++);
					FileWriter fw = new FileWriter(file); 
					if (!file.exists()) {
						file.createNewFile();
					}

				}
			} 
		}catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}
}
