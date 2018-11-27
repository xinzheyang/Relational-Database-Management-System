/**
 * 
 */
package logicaloperator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import database.DBCatalog;
import visitor.PhysicalPlanBuilder;

/**
 * @author xinzheyang
 * An logical operator that stores minimal information
 * to be visited by PhysicalPlanBuilder to transform into a physical plan builder
 */
public class LogicalScanOperator extends LogicalOperator {
	//TODO: set fields to store stats
	private String tb;
	private String alias;
	private int relationSize;
	private HashMap<String, int[]> attribBounds; //attribute name --> [min, max]
	
	public LogicalScanOperator(String table, String alias) {
		this.tb = table;
		this.alias = alias;
		relationSize = DBCatalog.getRelationSize(table);
		attribBounds = DBCatalog.getAttribBounds(table); 
//		BufferedReader readStats = new BufferedReader(new FileReader(DBCatalog.getStatsFileLoc()));
//		String line;
//		while ((line = readStats.readLine()) != null) {
//			String[] splits = line.split(" ");
//			if (tb.equals(splits[0])) {
//				readStats.close();
//				relationSize = Integer.parseInt(splits[1]);
//				for (int i = 2; i < splits.length; i++) {
//					String[] colStats = splits[i].split(",");
//					attribBounds.put(colStats[0], new int[] {Integer.parseInt(colStats[1]), Integer.parseInt(colStats[2])});
//				}
//				break;
//			}
//		}
//		readStats.close();
//		if (line == null) throw new Exception("table stats not found"); //table not found in stats.txt, throw exception
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
	
	/**
	 * @return the reference to the table, either its alias if the table uses alias
	 * or the table's actual table name.
	 */
	public String getReference() {
		return alias == null ? tb : alias;
	}
	
	/** Gets the relation size, in tuples, of this base table by reading from db/stats.txt.
	 * @return
	 * @throws Exception 
	 */
	public int getRelationSize() {
		return relationSize;
	}
	
	/** Setter just for testing, not allowed to be called for other purposes.
	 * @param newSize
	 */
	public void setRelationSize(int newSize) {
		relationSize = newSize;
	}
	
	/** Computes the V-value of this base table on the given attribute, which is
	 * max - min + 1;
	 * @param attrib
	 * @return
	 * @throws Exception
	 */
	public int getVValue(String attrib) {
		assert attribBounds.containsKey(attrib); //assert attribute name validity
		int[] bounds = attribBounds.get(attrib);
		return bounds[1] - bounds[0] + 1;
	}
	/* (non-Javadoc)
	 * @see logicaloperator.LogicalOperator#accept(visitor.PhysicalPlanBuilder)
	 */
	@Override
	public void accept(PhysicalPlanBuilder pb) {
		pb.visit(this);
	}

}
