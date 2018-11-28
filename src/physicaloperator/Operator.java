/**
 *
 */
package physicaloperator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import database.Tuple;
import database.TupleWriter;
import visitor.PhysicalPlanWriter;

/**
 * The abstract class for all kinds of operators
 * @author xinzheyang
 *
 */
public abstract class Operator {


	protected HashMap<String, Integer> columnIndexMap;

	/**
	 * get the index of the table given a column name
	 * @param colName the column name
	 * @return
	 */
	public int getColumnIndex(String colName) {
		return columnIndexMap.get(colName);
	}

	/**
	 * Getter for columnIndexMap
	 * @return The HashMap that maps column names to indices
	 */
	public HashMap<String, Integer> getColumnIndexMap() {
		return columnIndexMap;
	}

	/** Reads the next line from the file that stores the base table and returns the next tuple.
	 * @return the next Tuple object
	 */
	public abstract Tuple getNextTuple();


	/**
	 * Resets the operator to the beginning so that when it calls getNextTuple(), it starts from the first tuple again.
	 */
	public abstract void reset();
	
	
	/** Resets the operator by index of page. Will only be implemented by the SortOperator subclass.
	 * @param index The index of the page to be reset to.
	 */
	public abstract void reset(int index);


	/** Human readable version of dump.
	 * @param fileOut
	 */
	public void dumpHumanReadable(String fileOut) {
		BufferedWriter bw = null;
		try {
			//	 String mycontent = "This String would be written" +
			//	    " to the specified File";
			//Specify the file name and path here
			File file = new File(fileOut);

			/* This logic will make sure that the file
			 * gets created if it is not present at the
			 * specified location*/
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			Tuple tup;
			while((tup=getNextTuple()) != null) {
				bw.write(tup.toString());
				bw.newLine();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		finally
		{
			try{
				if(bw!=null)
					bw.close();
			}catch(Exception ex){
				System.out.println("Error in closing the BufferedWriter"+ex);
			}
		}

	}
	
	public abstract void accept(PhysicalPlanWriter write);
	
	public String[] getColsInOrder() {
		String[] colsInOrder = new String[this.columnIndexMap.size()];
		for(String col : this.columnIndexMap.keySet()) {
			colsInOrder[columnIndexMap.get(col)] = col;
		}
		return colsInOrder;
	}


	/**
	 * Keeps on calling the getNextTuple() method of this operator and keeps
 	 * writing tuples into the fileOut path until end of table file is reached.
	 * @param fileOut the path of the txt file that the data should be written to.
	 */
	public void dump(String fileOut) {
		TupleWriter tw = new TupleWriter(fileOut, this);
		tw.writeMetaData();
		Tuple tup;
		while((tup=getNextTuple()) != null) {
			tw.setTuple(tup);
			tw.writeToBuffer();
		}
		tw.flushLastPage();
		tw.close();
	}
}
