/**
 *
 */
package database;

import java.io.File;

/**
 * @author xinzheyang
 * This the top level Main class that takes input and output directory
 * and parses SQL queries in input directory and dumps results in output directory
 * The program will not terminate when errors occur.
 */
public class Main {


	/**
	 * @param args[0] = dirIn, args[1] = dirOut
	 */
	public static void main(String[] args) {
		try{
			
			String dirIn = args[0];
			String dirOut = args[1];
			String dbDir = dirIn + File.separator + "db";
			String qFile = dirIn + File.separator + "queries.sql";
			DBCatalog.getCatalog().parseSchema(dbDir);
			QueryParser queryParser = new QueryParser(qFile, dirOut);
			queryParser.parse();
			
		} catch(Exception e) {
			System.out.println("An error occurred in main()");
		}
	}

}
