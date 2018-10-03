/**
 * 
 */
package operator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import database.DBCatalog;
import database.Tuple;

/**
 * @author sitianchen
 * Scan Operator reads information from the text file containing a table and stores it
 * in some attributes.
 */

public class ScanOperator extends Operator {
	
	private String tb;
	private String alias;
	private BufferedReader f;
//	private HashMap<String, Integer> columnIndexMap;
	
	/* Upon initialization, opens up a file scan on the appropriate data file
	 * data file.
	 */
	
	
	
	/**
	 * Upon initialization, opens up a file scan on the appropriate data file
	 * @param tableName the name of the table we want to scan from
	 * @throws FileNotFoundException
	 */
	public ScanOperator(String tableName) throws FileNotFoundException {
		tb = tableName;
		alias = "";
		f = new BufferedReader(new FileReader(DBCatalog.getTableLoc(tb)));
		columnIndexMap = new HashMap<String, Integer>();
		String[] schemaColNames = DBCatalog.getTableColumns(tb);
		for(int i = 0; i < schemaColNames.length; i++) {
			columnIndexMap.put((tb + "." + schemaColNames[i]), i);
		}
			
	}
	/* Overloading constructor that enables aliases. 
	 */
	
	/**
	 * Initializes the scan operator if a table uses alias
	 * @param tableName the name of the table we want to scan from
	 * @param alias the alias that the table uses
	 * @throws FileNotFoundException
	 */
	public ScanOperator(String tableName, String alias) throws FileNotFoundException {
		tb = tableName;
		this.alias = alias;
		f = new BufferedReader(new FileReader(DBCatalog.getTableLoc(tableName)));
		columnIndexMap = new HashMap<String, Integer>();
		String[] schemaColNames = DBCatalog.getTableColumns(tableName);
		for(int i = 0; i < schemaColNames.length; i++) {
			columnIndexMap.put((alias + "." + schemaColNames[i]), i);
		}
			
	}
	
	
	/**
	 * @return the alias of the table
	 */
	public String getAlias() {
		return alias;
	}
	
	/**
	 * @return the name of the table
	 */
	public String getTableName() {
		return tb;
	}

	@Override
	public Tuple getNextTuple() {
		// TODO Auto-generated method stub

		try {
			//			String[] cols = DBCatalog.getTableColumns(this.tb);
			//			String values = f.readLine();
			String values;
			if((values=f.readLine()) != null) {
				return new Tuple(values);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("fail to read table file");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see database.Operator#reset()
	 */
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		try {
			f = new BufferedReader(new FileReader(DBCatalog.getTableLoc(tb)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
