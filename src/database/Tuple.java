/**
 * 
 */
package database;

import java.util.HashMap;

import net.sf.jsqlparser.schema.Table;

/**
 * @author xinzheyang
 * We use
 *
 */
public class Tuple {
	private Table table;
	public HashMap<String,Integer> tupleData;
	
	/* Initialize an instance of the class with a tupleString (result from table file read) and 
	 * corresponding table. First get corresponding 
	 * column names from the data catalog, then map each column name to the read values.
	 */
	public Tuple(String tupleString, Table table) {
		this.table = table;
		// key = column name, value = value
		this.tupleData = new HashMap<String, Integer>();
		
		String[] columnNames = DBCatalog.getTableColumns(table.getName());
		String[] columnValues = tupleString.split(",");
		assert columnNames.length == columnValues.length;
		
		for(int i = 0; i < columnNames.length; i++) {
			this.tupleData.put(columnNames[i], Integer.parseInt(columnValues[i]));
		}
	}
	
	/*  Get corresponding column value from column name.
	 */
	public Integer getColumnValue(String columnName) {
		return tupleData.get(columnName);
	}
	
	public Table getTable() {
		return this.table;
	}
	
}
