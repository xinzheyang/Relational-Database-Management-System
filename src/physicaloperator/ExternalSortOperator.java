/**
 * 
 */
package physicaloperator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import database.DBCatalog;
import database.Tuple;
import database.TupleReader;
import database.TupleWriter;

/**
 * @author xinzheyang
 *
 */
public class ExternalSortOperator extends SortOperator {
	private int bufSize;
	private String path;
	private int numTuples;  // number of tuples that can be held in main memory
	private int passCounter = 0;
	private int numRuns = 0;
	private TupleReader sortedRun;
	/**
	 * @param op
	 * @param cols
	 * @param size
	 */
	public ExternalSortOperator(Operator op, String[] cols, int size) {
		super(op, cols);
		bufSize = size;
		path = DBCatalog.getTempDir();
		File dir = new File(path);
		dir.mkdirs();
		numTuples = 4096 * bufSize / (4 * op.getColumnIndexMap().size());
		
		numRuns = passZero();
		
		while (numRuns != 1) {
			numRuns = merge();
		}
		sortedRun = new TupleReader(path + File.separator + (passCounter-1) + "_" + (numRuns-1));
	}
	private int mergeHelper(int numRun) {
		int res = 0;
		ArrayList<TupleReader> buffer =  new ArrayList<TupleReader>();
		HashMap<Tuple, TupleReader> map = new HashMap<Tuple, TupleReader>();
		TupleWriter tw = new TupleWriter(path + File.separator + passCounter + "_" + numRun, this);
		tw.writeMetaData();
		for(int i=0; i<bufSize-1; i++) {
			String prevPassRunPath = path + File.separator + (passCounter-1) + "_" + i;
			Path p = Paths.get(prevPassRunPath);
			if (Files.exists(p)) {
				TupleReader tReader = new TupleReader(prevPassRunPath);
				buffer.add(tReader);
			} else {
				res = -1;
				break;
			}
			
		}
		PriorityQueue<Tuple> tupleQueue = new PriorityQueue<Tuple>(com);
		for(int i=0; i<buffer.size(); i++) {
			TupleReader tr = buffer.get(i);
			Tuple tuple = tr.getNextTuple();
			if (tuple != null) {
				tupleQueue.add(tuple);
				map.put(tuple, tr);
			} else {
				break;
			}
		}
		while (!tupleQueue.isEmpty()) {
			Tuple minTuple = tupleQueue.poll();
			tw.setTuple(minTuple);
			tw.writeToBuffer();
			Tuple nextTuple;
			TupleReader mappedTr = map.get(minTuple);
			if ((nextTuple = mappedTr.getNextTuple()) != null) {
				tupleQueue.add(nextTuple);
				map.put(nextTuple, mappedTr);
			} else {
				mappedTr.close();
			}
		}
		tw.flushLastPage();
		tw.close();
		return res;
	}
	
	private int merge() {
		int numRun = 0;
		while (mergeHelper(numRun++) == 0) {}
		passCounter++;
		return numRun;
	}
	
	
	private int passZeroHelper(int numRun) {
		int res = 0;
		TupleWriter tw = new TupleWriter(path + File.separator + "0_" + numRun, this);
		tw.writeMetaData();
		Tuple tuple;
		List<Tuple> tempTuple =new ArrayList<>();
		for(int i = 0; i < numTuples; i++) {
			if((tuple = childOp.getNextTuple()) != null) {
				tempTuple.add(tuple);
			} else {
				res = -1;
				break;
			}
		}
		Collections.sort(tempTuple, com);
		for(int i=0;i<tempTuple.size();i++) {
			tw.setTuple(tempTuple.get(i));
			tw.writeToBuffer();
		}
		tw.flushLastPage();
		tw.close();
		return res;
	}
	
//	private int passZeroHelper(int numRun) {
//		int res = 0;
//		BufferedWriter bw = null;
//		try {
//			
//			File file = new File(path + File.separator + "0_" + numRun);
//			if (!file.exists()) {
//				file.createNewFile();
//			}
//
//			FileWriter fw = new FileWriter(file);
//			bw = new BufferedWriter(fw);
//			Tuple tuple;
//			List<Tuple> tempTuple =new ArrayList<>();
//			for(int i = 0; i < numTuples; i++) {
//				if((tuple = childOp.getNextTuple()) != null) {
//					tempTuple.add(tuple);
//				} else {
//					res = -1;
//					break;
//				}
//			}
//			Collections.sort(tempTuple, com);
//			for(int i=0;i<tempTuple.size();i++) {
//				bw.write(tempTuple.get(i).toString());
//				bw.newLine();
//			}
//			if(bw!=null)
//				bw.close();
//		} catch (Exception e) {
//			if(bw!=null)
//				try {
//					bw.close();
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//		}
//		return res;
//	}
	
	private int passZero() {
		int numRun = 0;
		while (passZeroHelper(numRun++) == 0) {}
		passCounter++;
		return numRun;
	}
	
	

	@Override
	public Tuple getNextTuple() {
		if (sortedRun != null) {
			return sortedRun.getNextTuple();
		} else {
			return null;
		}
	}

	@Override
	public void reset() {
		if (sortedRun != null) {
			sortedRun.reset(0);
		}
		
	}

	@Override
	public void reset(int index) {
		sortedRun.reset(0);
		
	}
}
