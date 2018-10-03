/**
 *
 */
package database;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import net.sf.jsqlparser.schema.Table;

/**
 * @author xinqi
 * We use this class to represent a data entry with selected columns in a table
 *
 */
public class Tuple {
//	private Table table;
//	public HashMap<String,Integer> tupleData;
	
	/**
	 * @param lst the list of indices to sort by
	 * @return the comparator that Collection can use
	 */
	public static Comparator<Tuple> getComparator(List<Integer> lst) {
		return new TupleComparator(lst);
	}
	
	private int[] colValues;

	/** Getter for colValues
	 * @return the colValues of the object
	 */
	public int[] getColValues() {
		return colValues;
	}

	/** Setter for colValues
	 * @param colValues the column values to be set
	 */
	public void setColValues(int[] colValues) {
		this.colValues = colValues;
	}

	/* Initialize an instance of the class with a tupleString (result from table file read) and
	 * corresponding table. First get corresponding
	 * column names from the data catalog, then map each column name to the read values.
	 */
	
	/**
	 *Initializes a Tuple with a string containing column names
	 * @param tupleString the column values separated by ","
	 */
	public Tuple(String tupleString) {
//		this.table = table;
//		String[] columnNames = DBCatalog.getTableColumns(tableName);
		String[] columnValuesStr = tupleString.split(",");
		colValues = new int[columnValuesStr.length];

		for(int i = 0; i < columnValuesStr.length; i++) {
			colValues[i] = Integer.parseInt(columnValuesStr[i]);
		}
	}
	

	/**
	 * Construct the Tuple with an array as column values
	 * @param colValues the column values
	 */
	public Tuple(int[] colValues) {
		this.colValues = colValues;
	}
	
	/**
	 * Get the value of this index
	 * @param colIndex the index to look from
	 * @return 
	 */
	public int getColumnValue(int colIndex) {
		assert colIndex < colValues.length;
		return colValues[colIndex];
	}

	/**
	 * Merge this tuple with another tuple
	 * @param tuple2 the tuple to be merged with
	 * @return The new tuple generated
	 */
	public Tuple merge(Tuple tuple2) {
		int[] newarr = new int[colValues.length + tuple2.colValues.length];
		System.arraycopy(colValues, 0, newarr, 0, colValues.length);
		System.arraycopy(tuple2.colValues, 0, newarr, colValues.length, tuple2.colValues.length);
		return new Tuple(newarr);
	}

	/*  Get corresponding column value from column name.
	 */
//	public Integer getColumnValue(String columnName) {
//		return tupleData.get(columnName);
//	}

	/* 
	 * Converts the Tuple into a string where each value is connected by a ","
	 */
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
	


	/**
	 * @author xinqi
	 * The comparator that sorts the Tuples according to a list of column 
	 * indices as the order
	 */
	private static class TupleComparator implements Comparator<Tuple> { 	
		private List<Integer> indexes;
	    public TupleComparator(List<Integer> lst) {
	    	indexes=lst;
	    }
	    public int compare(Tuple a, Tuple b) 
	    { 
	        for(int i:indexes) {
	        	if(a.getColumnValue(i) < b.getColumnValue(i)) return -1;
	        	else if(a.getColumnValue(i) > b.getColumnValue(i)) return 1;
	        }
	        return 0;
	    } 
	}
	
	

}


