/**
 *
 */
package database;

import java.io.File;
import java.io.IOException;

/**
 * @author xinzheyang
 *
 */
public class Main {


	/**
	 * @param args
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
