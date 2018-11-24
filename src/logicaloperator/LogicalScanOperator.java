/**
 * 
 */
package logicaloperator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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
	
	public LogicalScanOperator(String table, String alias) throws FileNotFoundException {
		this.tb = table;
		this.alias = alias;
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
	
	/** Gets the relation size of this base table by reading from db/stats.txt.
	 * @return
	 * @throws Exception 
	 */
	public int getRelationSize() throws Exception {
		//TODO: access stats from DBCatalog
		BufferedReader readStats = new BufferedReader(new FileReader(DBCatalog.getStatsFileLoc()));
		String line;
		while ((line = readStats.readLine()) != null) {
			String[] splits = line.split(" ");
			if (tb.equals(splits[0])) {
				readStats.close();
				return Integer.parseInt(splits[1]);
			}
		}
		readStats.close();
		//table not found in stats.txt, throw exception
		throw new Exception("table stats not found");
	}
	
	/** Computes the V-value of this base table on the given attribute, which is
	 * max - min + 1;
	 * @param attrib
	 * @return
	 * @throws Exception
	 */
	public int getVValue(String attrib) throws Exception {
		BufferedReader readStats = new BufferedReader(new FileReader(DBCatalog.getStatsFileLoc()));
		String line;
		while ((line = readStats.readLine()) != null) {
			String[] splits = line.split(" ");
			if (tb.equals(splits[0])) {
				for (int i = 2; i < splits.length; i++) {
					String[] colStats = splits[i].split(",");
					if (colStats[0].equals(attrib)) {
						readStats.close();
						return Integer.parseInt(colStats[2]) - Integer.parseInt(colStats[1]) + 1;
					}
				}
			}
		}
		readStats.close();
		throw new Exception("table stats not found, or attribute not found in table stats");
	}
	/* (non-Javadoc)
	 * @see logicaloperator.LogicalOperator#accept(visitor.PhysicalPlanBuilder)
	 */
	@Override
	public void accept(PhysicalPlanBuilder pb) {
		pb.visit(this);
	}

}
