/**
 * 
 */
package physicaloperator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import database.Tuple;
import visitor.PhysicalPlanWriter;

/**
 * @author xinzheyang
 *
 */
public class IndexScanOperator extends ScanOperator {
	private int lowKey, highKey;
	private String indexCol;
	private String indexPath;
	private static final int PAGE_SIZE = 4096;
	private int clustered; // unclustered (0) or clustered (1)
	private ByteBuffer buffer;
	private FileChannel channel;
	private FileInputStream fin;

	private int rootIndex;
	private int numLeaf;
	private int order;

	// state variables for unclustered
	private Queue<int[]> tempRids = new LinkedList<>();
	private Queue<int[]> initRids = new LinkedList<>();
	private int initLeaf;
	private int currLeaf;
	private boolean unclusterExceedHigh = false;

	private int[] initRid; // for clustered

	/**
	 * @throws FileNotFoundException
	 *             constructs an IndexScanOperater, it calls the super constructor
	 *             from ScanOperator it performs the first root-to-leaf descent
	 */
	public IndexScanOperator(String tablename, String alias, String path, String indexColumn, int cluster, int low,
			int high) throws FileNotFoundException {
		super(tablename, alias);
		try {
			indexPath = path;
			lowKey = low;
			highKey = high;
			indexCol = indexColumn;
			clustered = cluster;
			fin = new FileInputStream(indexPath);
			channel = fin.getChannel();
			buffer = ByteBuffer.allocate(PAGE_SIZE);
			deserializeHeader();
			initLeaf = currLeaf = findLowkey(rootIndex);
			initRid = findFirstKeyInLeaf(currLeaf);
			if (clustered == 1) {
				if (initRid != null) {
					reader.reset(initRid[0], initRid[1]);
				}
			} else {
				readLeaf(initLeaf);
				initRids = new LinkedList<>(tempRids);
			}
		} catch (IOException e) {
			System.err.println("err occured when constructing IndexScan");
			e.printStackTrace();
		}

	}

	/**
	 * @param ind,
	 *            the index of a leaf node
	 * @return the (pid, tid) of the first key that satisfies the constraint
	 * @throws IOException
	 */
	private int[] findFirstKeyInLeaf(int ind) throws IOException {
		channel.position(currLeaf * PAGE_SIZE);
		buffer.clear();
		channel.read(buffer);
		buffer.position(0);
		if (buffer.getInt() == 0) {
			int numEntry = buffer.getInt();
			int offset = 8;
			for (int i = 0; i < numEntry; i++) {
				int key = buffer.getInt(offset);
				offset += 4;
				int numRid = buffer.getInt(offset);
				offset += 4;
				if (key >= lowKey && key < highKey) {
					return new int[] { buffer.getInt(offset), buffer.getInt(offset + 4) };
				}
				offset += numRid * 2 * 4;
			}
		}
		return null;
	}

	/**
	 * @param nodeInd,
	 *            the leaf node index The method reads all the satisfying rids in
	 *            the leaf node and updates the temperory Queue tempRids
	 * @throws IOException
	 */
	private void readLeaf(int nodeInd) throws IOException {
		channel.position(nodeInd * PAGE_SIZE);
		buffer.clear();
		channel.read(buffer);
		buffer.position(0);
		int nodeType = buffer.getInt();
		assert nodeType == 0;
//		assert buffer.getInt() == 0;
		int numEntry = buffer.getInt();
		int offset = 8;

		for (int i = 0; i < numEntry; i++) {
			int key = buffer.getInt(offset);
			offset += 4;
			int numRid = buffer.getInt(offset);
			offset += 4;
			if (key >= lowKey && key <= highKey) {
				// System.out.println("key: "+key);
				for (int j = 0; j < numRid * 2; j += 2) {
					// System.out.println("pid: "+buffer.getInt(offset + j * 4));
					// System.out.println("rid: "+buffer.getInt(offset + (j + 1) * 4));
					tempRids.add(new int[] { buffer.getInt(offset + j * 4), buffer.getInt(offset + (j + 1) * 4) });
				}
			} else {
				if (key > highKey) {
					unclusterExceedHigh = true;
				}
			}
			offset += numRid * 2 * 4;
		}
	}

	/**
	 * deserializes the head node, i.e. the first node in the B+ tree index
	 * 
	 * @throws IOException
	 */
	private void deserializeHeader() throws IOException {
		channel.position(0);
		buffer.clear();
		channel.read(buffer);
		buffer.position(0);
		rootIndex = buffer.getInt();
		numLeaf = buffer.getInt();
		order = buffer.getInt();
	}

	/**
	 * @param ind,
	 *            the index node to start searching
	 * @return the leaf node index that has the first key that satisfies the
	 *         constraint
	 * @throws IOException
	 */
	private int findLowkey(int ind) throws IOException {
		channel.position(ind * PAGE_SIZE);
		buffer.clear();
		channel.read(buffer);
		buffer.position(0);
		if (buffer.getInt() == 1) {
			int numKeys = buffer.getInt();
			int keys[] = new int[numKeys];
			for (int i = 0; i < numKeys; i++) {
				int key = buffer.getInt();
				keys[i] = key;
			}
			int begin = Arrays.binarySearch(keys, lowKey);
			if (begin < 0) {
				begin = -(begin + 1);
			} else {
				begin = begin + 1;
			}
			int offset = (1 + 1 + numKeys) * 4;
			return findLowkey(buffer.getInt(offset + begin * 4));
		} else { // leaf node
			return ind;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see physicaloperator.Operator#getNextTuple()
	 */
	@Override
	public Tuple getNextTuple() {
		if (clustered == 1) {
			if (initRid == null) {
				return null;
			} else {
				Tuple tuple = reader.getNextTuple();
				String ref = this.alias == null ? tb : this.alias;
				if (tuple == null || tuple.getColumnValue(getColumnIndex(ref + "." + indexCol)) > highKey) {
					return null;
				} else {
					return tuple;
				}
			}
		} else {
			if (tempRids.isEmpty()) {
				currLeaf++;
				if (unclusterExceedHigh || currLeaf > numLeaf) {
					return null;
				} else {
					try {
						readLeaf(currLeaf);
					} catch (IOException e) {
						e.printStackTrace();
					}
					return getNextTuple();
				}
			} else {
				int rid[] = tempRids.poll();
				reader.reset(rid[0], rid[1]);
				return reader.getNextTuple();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see physicaloperator.Operator#reset()
	 */
	@Override
	public void reset() {
		if (clustered == 1) {
			reader.reset(initRid[0], initRid[1]);
		} else {
			tempRids = new LinkedList<>(initRids);
			currLeaf = initLeaf;
			unclusterExceedHigh = false;
		}
	}
	
	/* (non-Javadoc)
	 * @see physicaloperator.ScanOperator#accept(visitor.PhysicalPlanWriter)
	 */
	public void accept(PhysicalPlanWriter write) {
		write.visit(this);
	}
	
	/**
	 * @return the minimum value of this index in the query condition
	 */
	public int getLowKey() {
		return lowKey;
	}
	
	/**
	 * @return the maximum value for this index in the query condition
	 */
	public int getHighKey() {
		return highKey;
	}
	

	/**
	 * @return the column name of this index
	 */
	public String getIndexName() {
		return indexCol;
	}
}
