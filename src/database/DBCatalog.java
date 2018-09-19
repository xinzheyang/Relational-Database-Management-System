/**
 * 
 */
package database;

import java.io.*;
import java.util.*;

/**
 * The catalog can keep track of information such as
 * where a file for a given table is located, what the schema of different tables is, 
 * and so on. The catalog is a global entity, therefore we use a singleton pattern.
 * @author sitianchen
 *
 */
public class DBCatalog {
	
	private HashMap<String, String[]> tableMap; //
//	private HashMap<String, String> locMap; //do we need this when all table files are in the same dir?
	private static String locDir;
	private static DBCatalog catalog = new DBCatalog(); //singleton object for global reference
	
	/* A private Constructor prevents any other class
	 * from instantiating a DBCatalog object.
	 */
	private DBCatalog() {
		this.tableMap = new HashMap<String, String[]>();
	}
	
	/* Static get instance method, gets the singleton instance
	 * of the class.
	 */
	public static DBCatalog getCatalog() {
		return catalog;
	}
	
	public static String getTableLoc(String tableName) {
		return locDir + File.separator + tableName;
	}
	
	/* Parses in schema information by reading the schema file in the directory path specified.
	 * Link table present in schema to where the file for the table is located.
	 */
	public void parseSchema(String dir) throws IOException {
		
		locDir = dir + File.separator + "data";
		BufferedReader schemaIn = new BufferedReader(new FileReader(dir + File.separator + "schema.txt"));
		String line;
		while ((line = schemaIn.readLine()) != null) {
			String[] tableInfo = line.split(" ");
			tableMap.put(tableInfo[0], Arrays.copyOfRange(tableInfo, 1, tableInfo.length)); //key=table name, value=col names array
//			locMap.put(tableInfo[0], dir + File.separator + "data" + File.separator)
		}
	}

}
