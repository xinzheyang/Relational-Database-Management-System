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


	/** Runs the program with given args, args[0] is the specified input directory and 
	 * args[1] is the directory we write the output queries to.
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			
			String dirIn = args[0];
			String dirOut = args[1];
			String dirTemp = args[2];
			String dbDir = dirIn + File.separator + "db";
			String qFile = dirIn + File.separator + "queries.sql";
			String configFile = dirIn + File.separator + "plan_builder_config.txt";
			DBCatalog.getCatalog().parseSchema(dbDir); //parse schema
			DBCatalog.getCatalog().parseConfig(configFile); //parse physical plan configuration
			QueryParser queryParser = new QueryParser(qFile, dirOut);
			DBCatalog.setTempDir(dirTemp);
			queryParser.parse();
			
		} catch(Exception e) {
			System.out.println("An error occurred in main()");
		}
	}

}
