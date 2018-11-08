/**
 *
 */
package database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author xinzheyang
 *
 */
public class Main {


	/** Runs the program with given args, args[0] is the specified input directory and 
	 * args[1] is the directory we write the output queries to.
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
//		try{
			BufferedReader interpConfigIn = new BufferedReader(new FileReader(args[0]));
//			String line;
			String dirIn = interpConfigIn.readLine();
			String dirOut = interpConfigIn.readLine();
			String dirTemp = interpConfigIn.readLine();
			String dbDir = dirIn + File.separator + "db";
			String qFile = dirIn + File.separator + "queries.sql";
			String configFile = dirIn + File.separator + "plan_builder_config.txt";
			DBCatalog.getCatalog().parseSchema(dbDir); //parse schema
			if (interpConfigIn.readLine().equals("1")) { //should build indices
				DBCatalog.parseIndices(true);
			}
			if (interpConfigIn.readLine().equals("1")) { //should evaluate SQL queries
				DBCatalog.getCatalog().parseConfig(configFile); //parse physical plan configuration
				QueryParser queryParser = new QueryParser(qFile, dirOut);
				DBCatalog.setTempDir(dirTemp);
				queryParser.parse();
				//TODO: change PPB to use index scan as well if indices built
			}
			interpConfigIn.close();
			
//		} catch(Exception e) {
//			System.out.println("An error occurred in main()");
//		}
	}

}
