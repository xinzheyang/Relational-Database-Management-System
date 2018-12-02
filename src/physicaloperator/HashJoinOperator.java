/**
 * 
 */
package physicaloperator;

import java.io.File;

import database.Tuple;
import database.TupleReader;
import database.TupleWriter;
import net.sf.jsqlparser.expression.Expression;
import visitor.EquiConjunctVisitor;
import visitor.PhysicalPlanWriter;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
/**
 * @author sitianchen
 *
 */
public class HashJoinOperator extends JoinOperator {
	private final int mainMemBuffers = 12; //hard-code number of main memory buffers
	private final int bufferSize = 10;
	private final int htBuckets = mainMemBuffers - 2; //hard-code number of buckets in hash table 
	private final int numOfPartitions = mainMemBuffers - 1; //hard-codes number of partitions
	private String leftPartPath;
	private String rightPartPath;
	private int[] leftJoinAttribs; //indices of join attributes in order of comparison for left and right joins
	private int[] rightJoinAttribs;
	private int currPartition; //the index of the current partition
	private TupleReader leftPartRead;
	private TupleReader rightPartRead; //the tuple readers for the current left and right matching partitions
	private List<Tuple>[] currHashtable; //the hash table for this partition
	private int currProbeTupleIdx; //index of the current tuple in the current probe entry of the hash table
	private int currProbeEntry; //index of the current probe entry in the hash table
	private Tuple currRight; //the current right tuple

	private static final int PAGE_SIZE = 4096;

	public HashJoinOperator(Operator left, Operator right, Expression condition) {
		super(left, right, condition);
		leftPartPath = "left_partitions";
		File leftDir = new File(leftPartPath);
		leftDir.mkdirs();
		rightPartPath = "right_partitions";
		File rightDir = new File(rightPartPath);
		rightDir.mkdirs();
		currPartition = 0;
		
		//initialize join attribute indices
		EquiConjunctVisitor equiVisit = new EquiConjunctVisitor(left, right);
		this.joinCondition.accept(equiVisit);
		ArrayList<String> leftAttribCols = equiVisit.getLeftCompareCols();
		ArrayList<String> rightAttribCols = equiVisit.getRightCompareCols();
		
		assert leftAttribCols.size() == rightAttribCols.size();
		
		leftJoinAttribs = new int[leftAttribCols.size()];
		rightJoinAttribs = new int[rightAttribCols.size()];
		for(int i = 0; i < leftAttribCols.size(); i++) {
			leftJoinAttribs[i] = left.getColumnIndex(leftAttribCols.get(i));
			rightJoinAttribs[i] = left.getColumnIndex(rightAttribCols.get(i));
		}
		//initialize partitions
		splitPhase();

	}

	/** Evaluates and return true if the two tuples are equal on the join attributes.
	 * @param left
	 * @param right
	 * @return
	 */
	private boolean areEqual(Tuple left, Tuple right) {
		for(int i = 0; i < leftJoinAttribs.length; i++) {
			int l = leftJoinAttribs[i];
			int r = rightJoinAttribs[i];
			if (left.getColumnValue(l) != right.getColumnValue(r)) return false;
		}
		return true;

	}

	/* (non-Javadoc)
	 * @see physicaloperator.Operator#getNextTuple()
	 */
	@Override
	public Tuple getNextTuple() {
		//for now, assume main memory size is large enough to fit every partition 
		//so that we don't have to recurse
		if (currPartition >= numOfPartitions) return null;

		if (leftPartRead == null || rightPartRead == null) {//need to initialize readers
			//need to initialize build phase
			currPartition++;
			if (currPartition >= numOfPartitions) return null;
			leftPartRead = new TupleReader(leftPartPath + File.separator + "partition" + currPartition);
			rightPartRead = new TupleReader(rightPartPath + File.separator + "partition" + currPartition);
			currHashtable = (LinkedList<Tuple>[]) new LinkedList[htBuckets]; 
			Tuple tupLeft;
			while((tupLeft = leftPartRead.getNextTuple()) != null) {
				int bucket = hash2(htBuckets, tupLeft, leftJoinAttribs);
				currHashtable[bucket].add(tupLeft);
			}
			currProbeEntry = 0; //reset probe entry and tuple index
			currProbeTupleIdx = 0;
		}
		
		while (currProbeTupleIdx < currHashtable[currProbeEntry].size()) {//try probe with the curr right tuple
			Tuple currLeft = currHashtable[currProbeEntry].get(currProbeTupleIdx);
			if (areEqual(currLeft, currRight)) {
				currProbeTupleIdx++;
				return currLeft.merge(currRight);
			}
		}
		while((currRight = rightPartRead.getNextTuple()) != null) { //need to get the next right tuple to probe
			currProbeEntry = hash2(htBuckets, currRight, rightJoinAttribs); //new bucket number
			while (currProbeTupleIdx < currHashtable[currProbeEntry].size()) {
				Tuple currLeft = currHashtable[currProbeEntry].get(currProbeTupleIdx);
				if (areEqual(currLeft, currRight)) {
					currProbeTupleIdx++;
					return currLeft.merge(currRight);
				}
			}
		}
		while(currPartition++ < numOfPartitions) {//need to build the next partition
			//build phase
			leftPartRead = new TupleReader(leftPartPath + File.separator + "partition" + currPartition);
			rightPartRead = new TupleReader(rightPartPath + File.separator + "partition" + currPartition);
			currHashtable = (LinkedList<Tuple>[]) new LinkedList[htBuckets]; //clear the hash table in memory
			Tuple tupLeft;
			while((tupLeft = leftPartRead.getNextTuple()) != null) {
				int bucket = hash2(htBuckets, tupLeft, leftJoinAttribs);
				currHashtable[bucket].add(tupLeft);
			}
			currProbeEntry = 0; //reset probe entry and tuple index
			currProbeTupleIdx = 0;
			while((currRight = rightPartRead.getNextTuple()) != null) { //gets the next right tuple
				currProbeEntry = hash2(htBuckets, currRight, rightJoinAttribs); //new bucket number
				while (currProbeTupleIdx < currHashtable[currProbeEntry].size()) {//try find the equal tuple in the entry
					Tuple currLeft = currHashtable[currProbeEntry].get(currProbeTupleIdx);
					if (areEqual(currLeft, currRight)) {
						currProbeTupleIdx++;
						return currLeft.merge(currRight);
					}
				}
			}
		}
		
		
		return null;

	}


	/** Splits both left and right relations in to partitions by hashing on the join attributes
	 * of tuples.
	 */
	private void splitPhase() {
		//partition left relation
		TupleWriter[] lWriters = new TupleWriter[numOfPartitions]; //initialize writer of each partition 
		for (int i = 0; i < numOfPartitions; i++) { //set left write buffers
			lWriters[i] = new TupleWriter(leftPartPath + File.separator + "partition" + i, this);
		}
		Tuple tupLeft;
		while((tupLeft = leftChild.getNextTuple()) != null) {
			int partition = hash1(numOfPartitions, tupLeft, leftJoinAttribs); //hash to partition
			lWriters[partition].setTuple(tupLeft);
			lWriters[partition].writeToBuffer();
		}

		for (TupleWriter tw : lWriters) { //finalize all left partition writers
			tw.flushLastPage();
			tw.close();
		}

		//partition right

		TupleWriter[] rWriters = new TupleWriter[numOfPartitions]; //initialize writer of each partition 
		for (int i = 0; i < numOfPartitions; i++) { //set right write buffers
			rWriters[i] = new TupleWriter(rightPartPath + File.separator + "partition" + i, this);
		}
		Tuple tupRight;
		while((tupRight = leftChild.getNextTuple()) != null) {
			int partition = hash1(numOfPartitions, tupRight, rightJoinAttribs); //hash to partition
			rWriters[partition].setTuple(tupRight);
			rWriters[partition].writeToBuffer();
		}

		for (TupleWriter tw : rWriters) { //finalize all right partition writers
			tw.flushLastPage();
			tw.close();
		}

	}

	/** A java hashCode(int[]) like hash function for tuples considering the
	 * join attributes of this tuple, mod by number of buckets in the hash
	 * table.
	 * @param numOfBuckets
	 * @param tup
	 * @param joinAttribs
	 * @return
	 */
	private int hash1(int base, Tuple tup, int[] joinAttribs) {
		assert tup != null;
		int result = 1;
		for (int idx : joinAttribs) {
			result = 31 * result + tup.getColumnValue(idx);
		}
		return result % base;
	}

	private void joinPhase() {
		//for now, assume main memory size is large enough to fit every partition 
		//so we don't have to recurse
		for (int i = 0; i < numOfPartitions; i++) {
			//build phase for one left partition
			TupleReader readPart = new TupleReader(leftPartPath + File.separator + "partition" + i);
			Tuple tupLeft;
			List<Tuple>[] hashtable = (LinkedList<Tuple>[]) new LinkedList[htBuckets]; 
			while((tupLeft = readPart.getNextTuple()) != null) {
				int bucket = hash2(htBuckets, tupLeft, leftJoinAttribs);
				hashtable[bucket].add(tupLeft);
			}
			//probe phase for one left partition
		}
	}

	/**A java hashCode(int[]) like hash function for tuples considering the
	 * join attributes of this tuple, mod by number of buckets in the hash
	 * table. Uses a different prime from hash1().
	 * @param numOfBuckets
	 * @param tup
	 * @param joinAttribs
	 * @return
	 */
	private int hash2(int base, Tuple tup, int[] joinAttribs) {
		assert tup != null;
		int result = 1;
		for (int idx : joinAttribs) {
			result = 37 * result + tup.getColumnValue(idx);
		}
		return result % base;
	}

	/* (non-Javadoc)
	 * @see physicaloperator.Operator#accept(visitor.PhysicalPlanWriter)
	 */
	@Override
	public void accept(PhysicalPlanWriter write) {
		// TODO Auto-generated method stub

	}

}
