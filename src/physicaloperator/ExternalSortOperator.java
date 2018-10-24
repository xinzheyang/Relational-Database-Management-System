/**
 * 
 */
package physicaloperator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import database.DBCatalog;
import database.Tuple;
import database.TupleReader;
import database.TupleWriter;

/**
 * @author xinzheyang
 *
 */
public class ExternalSortOperator extends SortOperator {
	int bufSize;
	private ArrayList<TupleReader> buffer;
	String path;
	int numTuples;  // number of tuples that can be held in main memory
	/**
	 * @param op
	 * @param cols
	 * @param size
	 */
	public ExternalSortOperator(Operator op, String[] cols, int size) {
		super(op, cols);
		bufSize = size;
		buffer = new ArrayList<>();
		path = DBCatalog.getTempDir();
		File dir = new File(path);
		dir.mkdir();
		numTuples = 4096 * bufSize / (4 * op.getColumnIndexMap().size());
	}
	
	private int helper(int numRun) {
		TupleWriter tw = new TupleWriter(path + File.separator + "pass0" + "run" + numRun, this);
		tw.writeMetaData();
		Tuple tup;
		while((tup=getNextTuple()) != null) {
			tw.setTuple(tup);
			tw.writeToBuffer();
		}
		tw.flushLastPage();
		tw.close();
		
		Tuple tuple;
		List<Tuple> tempTuple =new ArrayList<>();
		for(int i = 0; i < 4096; i++) {
			if((tuple = childOp.getNextTuple()) != null) {
				tempTuple.add(tuple);
			} else {
				
				return -1;
			}
		}
		return 0;
	}
	private void passZero() {
		int numRun = 1;
		
		
	}

	@Override
	public Tuple getNextTuple() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reset(int index) {
		// TODO Auto-generated method stub
		
	}
}
