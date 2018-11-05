/**
 * 
 */
package physicaloperator;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import database.Tuple;
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

	private Queue<int[]> tempRids = new LinkedList<>();
	private int initLeaf;
	private int currLeaf;
	private int[] initRid;
	private boolean unclusterExceedHigh = false;

	/**
	 * @throws IOException
	 * 
	 */
	public IndexScanOperator(String tablename, String alias, String indexColumn, int cluster, int low, int high)
			throws IOException {
		super(tablename, alias);
		lowKey = low;
		highKey = high;
		indexCol = indexColumn;
		cluster = clustered;
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
		}
	}

	private int[] findFirstKeyInLeaf(int ind) throws IOException {
		channel.position(currLeaf * PAGE_SIZE);
		buffer.clear();
		channel.read(buffer);
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

	private void readLeaf(int nodeInd) throws IOException {
		channel.position(nodeInd * PAGE_SIZE);
		buffer.clear();
		channel.read(buffer);
		assert buffer.getInt() == 0;
		int numEntry = buffer.getInt();
		int offset = 8;

		for (int i = 0; i < numEntry; i++) {
			int key = buffer.getInt(offset);
			offset += 4;
			int numRid = buffer.getInt(offset);
			offset += 4;
			if (key >= lowKey && key <= highKey) {
				for (int j = 0; j < numRid; j += 2) {
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

	private void deserializeHeader() throws IOException {
		channel.position(0);
		buffer.clear();
		channel.read(buffer);
		rootIndex = buffer.getInt();
		numLeaf = buffer.getInt();
		order = buffer.getInt();
	}

	private int findLowkey(int ind) throws IOException {
		channel.position(ind * PAGE_SIZE);
		buffer.clear();
		channel.read(buffer);
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
				if (tuple.getColumnValue(getColumnIndex(indexCol)) > highKey) {
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
			currLeaf = initLeaf;
		}
	}

}
