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
 * @author xinzheyang
 * We use
 *
 */
public class Tuple {
//	private Table table;
//	public HashMap<String,Integer> tupleData;
	public static Comparator<Tuple> getComparator(List<Integer> lst) {
		return new TupleComparator(lst);
	}
	
	private int[] colValues;

	public int[] getColValues() {
		return colValues;
	}

	public void setColValues(int[] colValues) {
		this.colValues = colValues;
	}

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
	
	public int getColumnValue(int colIndex) {
		assert colIndex < colValues.length;
		return colValues[colIndex];
	}

//	/* Overloading constructor for join operator (merging two tuples together).
//	 */
//	public Tuple(Tuple tup1, Tuple tup2) {
//
//	}

	public Tuple merge(Tuple tuple2) {
//		this.colValues = ArrayUtils.addAll(first, second);
		int[] newarr = new int[colValues.length + tuple2.colValues.length];
		System.arraycopy(colValues, 0, newarr, 0, colValues.length);
		System.arraycopy(tuple2.colValues, 0, newarr, colValues.length, tuple2.colValues.length);
		this.colValues = newarr;
		return this;
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


