/**
 *
 */
package database;

import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

import net.sf.jsqlparser.schema.Table;

/**
 * @author xinzheyang
 * We use
 *
 */
public class Tuple {
//	private Table table;
//	public HashMap<String,Integer> tupleData;
	public int[] colValues;

	/* Initialize an instance of the class with a tupleString (result from table file read) and
	 * corresponding table. First get corresponding
	 * column names from the data catalog, then map each column name to the read values.
	 */
	public Tuple(String tupleString) {
//		this.table = table;
//		String[] columnNames = DBCatalog.getTableColumns(tableName);
		String[] columnValuesStr = tupleString.split(",");
//		assert columnNames.length == columnValuesStr.length;
		colValues = new int[columnValuesStr.length];

		for(int i = 0; i < columnValuesStr.length; i++) {
			colValues[i] = Integer.parseInt(columnValuesStr[i]);
		}
	}

	/* Overloading constructor for join operator (merging two tuples together).
	 */
	public Tuple(Tuple tup1, Tuple tup2) {

	}

	/* Overloading constructor for project operator.
	 */
	public Tuple(Tuple tup) {

	}

	/*  Get corresponding column value from column name.
	 */
//	public Integer getColumnValue(String columnName) {
//		return tupleData.get(columnName);
//	}

	public String toString() {
		StringBuilder res = new StringBuilder();
//		Collection<Integer> values = tupleData.values();
//		String result = values.stream().collect(Collectors.joining(", "));
		for(Integer v:colValues) {
			res.append(v+",");
		}
		res.deleteCharAt(res.length()-1);
		return res.toString();
	}

//	public Table getTable() {
//		return this.table;
//	}

}
