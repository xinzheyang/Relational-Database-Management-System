/**
 * 
 */
package database;

import java.io.File;
import java.io.IOException;

/**
 * @author sitianchen
 *
 */
public class Main {
	

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
//		String dirIn = args[0];
//		String dirOut = args[1];
//		String dbDir = dirIn + File.separator + "db";
//		String qFile = dirIn + File.separator + "queries.sql";
//		DBCatalog.getCatalog().parseSchema(dbDir);
		QueryParser queryParser = new QueryParser("queriesScan.sql");
		queryParser.parse();
	}

}
