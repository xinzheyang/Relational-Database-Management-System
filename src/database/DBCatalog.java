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
	private static HashMap<String, String[]> treeIndexMap;
	private static String dbDir;
	private static String locDir;
	private static String indexDir;
	private static String outputDir; //path to the directory where the output files to be written are
	private static String tempDir; //path to the directory where the temp files for external sort to be written are
	private static int tempDirCount = 0; // a self incrementing counter inside tempDir
	private static DBCatalog catalog = null; //singleton object for global reference
	private static String[] joinMethod;
	private static String[] sortMethod;
	private static boolean useIndex;
	private static boolean builtIndex;
	/* A private Constructor prevents any other class
	 * from instantiating a DBCatalog object.
	 */
	private DBCatalog() {
		//key=table name, value=col names array
		schemaMap = new HashMap<String, String[]>();
		treeIndexMap = new HashMap<String, String[]>();
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
	
	/** Gets the attribute name that serve as an index key for the table.
	 * @param tableNamethe actual table name (not alias)
	 * @return
	 */
	public static String getIndexKey(String tableName) {
		return treeIndexMap.get(tableName)[0];
	}
	
	/** Returns 1 if this table has clustered index, 0 otherwise.
	 * @param tableName the actual table name (not alias)
	 * @return
	 */
	public static int hasClusteredIndex(String tableName) {
		return Integer.parseInt(treeIndexMap.get(tableName)[1]);
	}
	
	/** Gets the index file location of this table.
	 * @param tableName the actual table name (not alias)
	 * @return
	 */
	public static String getIndexFileLoc(String tableName) {
		return indexDir + File.separator + tableName + "." + getIndexKey(tableName);
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
	
	public static boolean useIndex() {
		return useIndex;
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
			line = configIn.readLine();
			useIndex = "1".equals(line);
			if (useIndex) {
				if (!builtIndex) {
					parseIndices(false);
				}
			}
			configIn.close();
		} catch (Exception e) {
			System.out.println("An error occurred in processing the Physical Plan Configuration file.");
			e.printStackTrace();
		}
	}
	
	/** Parses in schema information by reading the schema file in the directory path specified.
	 * Link table present in schema to where the file for the table is located.
	 * @param dir: path to the /db directory
	 */
	public void parseSchema(String dir) throws IOException {
		dbDir = dir;
		locDir = dir + File.separator + "data";
		BufferedReader schemaIn = new BufferedReader(new FileReader(dir + File.separator + "schema.txt"));
		String line;
		while ((line = schemaIn.readLine()) != null) {
			String[] schemaInfo = line.split(" ");
			schemaMap.put(schemaInfo[0], Arrays.copyOfRange(schemaInfo, 1, schemaInfo.length)); //key=table name, value=col names array
		}
		schemaIn.close();
		
	}
	
	public static void parseIndices(boolean buildIndex) throws IOException {
		indexDir = dbDir + File.separator + "indexes";
		BufferedReader indexInfoIn = new BufferedReader(new FileReader(dbDir + File.separator + "index_info.txt"));
		String line;
		while (((line = indexInfoIn.readLine()) != null)) {
			String[] indexInfo = line.split(" ");
			treeIndexMap.put(indexInfo[0], new String[]{indexInfo[1], indexInfo[2]});
			assert indexInfo.length == 4;
			String curIndexOut = indexDir + File.separator + indexInfo[0] + "." + indexInfo[1];
			if (buildIndex) {
				BPlusTree curTree = new BPlusTree(indexInfo[0], curIndexOut, "1".equals(indexInfo[2]), Integer.parseInt(indexInfo[3]));
				curTree.scanAndConstructAll();
			}
		}
		if (buildIndex)
			builtIndex = true; //set built index to true whenever this function is called and indices are successfully built.
		indexInfoIn.close();
	}
	
	/** Gathers statistics of all relations in this database, put statistics in db/stats.txt.
	 * @throws IOException 
	 * 
	 */
	public static void gatherStats() throws IOException {
		BufferedWriter stats = new BufferedWriter(new FileWriter(dbDir + File.separator + "stats.txt"));
		assert DBCatalog.schemaMap != null;
		for (String tb : DBCatalog.schemaMap.keySet()) {
			String[] schema = DBCatalog.schemaMap.get(tb);
			TupleReader read = new TupleReader(DBCatalog.getTableLoc(tb));
			Tuple tup;
			int[][] colValBounds = new int[schema.length][2]; //for each column, correspond to [upper, lower]
			for (int i = 0; i < colValBounds.length; i++) {
				colValBounds[i] = new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE};
			}
			int tupCount = 0;
			while ((tup = read.getNextTuple()) != null) {
				for (int j = 0; j < tup.getColValues().length; j++) {
					int colVal = tup.getColumnValue(j);
					if (colVal > colValBounds[j][0]) colValBounds[j][0] = colVal; //update upper bound
					if (colVal < colValBounds[j][1]) colValBounds[j][1] = colVal; //update lower bound
				}
				tupCount++;
			}
			read.close();
			StringBuilder buildStr = new StringBuilder(tb + " " + tupCount + " ");
			
			for (int i = 0; i < schema.length; i++) {
				buildStr.append(schema[i] + "," + colValBounds[i][1] + "," + colValBounds[i][0]);
			}
			
			stats.write(buildStr.toString());
		}
		stats.close();
	}

}
