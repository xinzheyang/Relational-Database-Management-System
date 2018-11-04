/**
 * 
 */
package database;

import java.io.*;
import java.util.*;

import bplustree.BPlusTree;

/**
 * The catalog can keep track of information such as
 * where a file for a given table is located, what the schema of different tables is, 
 * and so on. The catalog is a global entity, therefore we use a singleton pattern.
 * @author sitianchen
 *
 */
public class DBCatalog {
	
	private static HashMap<String, String[]> schemaMap; //
	private static HashMap<String, String> treeIndexMap;
	private static String locDir;
	private static String outputDir; //path to the directory where the output files to be written are
	private static String tempDir; //path to the directory where the temp files for external sort to be written are
	private static int tempDirCount = 0; // a self incrementing counter inside tempDir
	private static DBCatalog catalog = null; //singleton object for global reference
	private static String[] joinMethod;
	private static String[] sortMethod;
	/* A private Constructor prevents any other class
	 * from instantiating a DBCatalog object.
	 */
	private DBCatalog() {
		//key=table name, value=col names array
		schemaMap = new HashMap<String, String[]>();
		treeIndexMap = new HashMap<String, String>();
	}

	/** Static get instance method, gets the singleton instance
	 * of the class. Initialize the catalog if necessary.
	 * @return the singleton instance of the class
	 */
	public static DBCatalog getCatalog() {
		if (catalog == null) {
			catalog = new DBCatalog();
		}
		return catalog;
	}
	
	/** Gets all column names of the input table.
	 * @param tableName
	 * @return an array of all column names of the table.
	 */
	public static String[] getTableColumns(String tableName) {
		return schemaMap.get(tableName);
	}
	
	
	/**
	 * @param tableName
	 * @return the table file location in local directory for the given tableName.
	 */
	public static String getTableLoc(String tableName) {
		return locDir + File.separator + tableName;
	}
	
	public static String getIndexKey(String tableName) {
		return treeIndexMap.get(tableName);
	}
	
	public static String getOutputDir() {
		return outputDir;
	}
	
	public static String getTempDir() {
		return tempDir + File.separator + "temp" + tempDirCount++;
	}
	
	public static void setTempDir(String tempDir) {
		DBCatalog.tempDir = tempDir;
	}
	
	private static boolean deleteTempDir(File file) {
		File[] allFiles = file.listFiles();
		if (allFiles != null) {
			for (File f: allFiles) {
				deleteTempDir(f);
			}
		}
		return file.delete();
	}
	
	public static void resetTempDir() {
		tempDirCount = 0;
		File file = new File(tempDir);
		deleteTempDir(file);
	}
	
	public static String getJoinMethod() {
		assert joinMethod.length > 0;
		return joinMethod[0];
	}
	
	public static int getJoinBufferSize() {
		assert joinMethod.length == 2;
		return Integer.parseInt(joinMethod[1]);
	}
	
	public static String getSortMethod() {
		assert sortMethod.length > 0;
		return sortMethod[0];
	}
	
	public static int getSortBufferSize() {
		assert sortMethod.length == 2;
		return Integer.parseInt(sortMethod[1]);
	}
	
	/** Parses the physical plan configuration file and sets the join and sort methods 
	 * for all queries that will be processed.
	 * @param dir: path to the physical plan configuration file
	 */
	public void parseConfig(String dir) {
		try {
			BufferedReader configIn = new BufferedReader(new FileReader(dir));
			String line = configIn.readLine();
			joinMethod = line.split(" ");
			line = configIn.readLine();
			sortMethod = line.split(" ");
			if (joinMethod.length < 1 || joinMethod.length > 2) {
				joinMethod = new String[]{"0"}; //set default to TNLJ if invalid input
			}
			if (sortMethod.length < 1 || sortMethod.length > 2) {
				sortMethod = new String[]{"0"}; //set default to in memory sort if invalid input
			}
			configIn.close();
		} catch (Exception e) {
			System.out.println("An error occurred in processing the Physical Plan Configuration file.");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** Parses in schema information by reading the schema file in the directory path specified.
	 * Link table present in schema to where the file for the table is located.
	 * Added feature for p3 to parse the index_info.txt as well.
	 * @param dir: path to the /db directory
	 */
	public void parseDbDir(String dir) throws IOException {
		
		locDir = dir + File.separator + "data";
		BufferedReader schemaIn = new BufferedReader(new FileReader(dir + File.separator + "schema.txt"));
		String line;
		while ((line = schemaIn.readLine()) != null) {
			String[] schemaInfo = line.split(" ");
			schemaMap.put(schemaInfo[0], Arrays.copyOfRange(schemaInfo, 1, schemaInfo.length)); //key=table name, value=col names array
//			locMap.put(tableInfo[0], dir + File.separator + "data" + File.separator)
		}
		schemaIn.close();
		
		String indexesOut = dir + File.separator + "indexes";
		BufferedReader indexInfoIn = new BufferedReader(new FileReader(dir + File.separator + "index_info.txt"));
		while (((line = indexInfoIn.readLine()) != null)) {
			String[] indexInfo = line.split(" ");
			treeIndexMap.put(indexInfo[0], indexInfo[1]);
			assert indexInfo.length == 4;
			String curIndexOut = indexesOut + File.separator + indexInfo[0] + "." + indexInfo[1];
			BPlusTree curTree = new BPlusTree(getTableLoc(indexInfo[0]), curIndexOut, "1".equals(indexInfo[2]), Integer.parseInt(indexInfo[3]));
			curTree.scanAndConstructAll();
			//PLACEHOLDER: set up indices by calling BPlusTree methods
		}
		indexInfoIn.close();
	}

}
