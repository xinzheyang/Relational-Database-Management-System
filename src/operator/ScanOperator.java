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
 *
 */
public class ScanOperator extends Operator {
	
	private String tb;
	private BufferedReader f;
	private HashMap<String, Integer> columnIndexMap;
	/* Upon initialization, opens up a file scan on the appropriate data file
	 * data file.
	 */
	public ScanOperator(String tableName) throws FileNotFoundException {
		tb = tableName;
		f = new BufferedReader(new FileReader(DBCatalog.getTableLoc(tb)));
	}

	/* Reads the next line from the file that stores the base table and 
	 * returns the next tuple.
	 */
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
			f = new BufferedReader(new FileReader(this.tb));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
//
//	@Override
//	public void dump(String fileOut) {
//		super.dump(fileOut);
//	}


}
