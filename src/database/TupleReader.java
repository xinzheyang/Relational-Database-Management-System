/**
 *
 */
package database;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
/**
 * @author xinqi
 * An TupleReader reads the binary-formatted file and grabs tuples from it.
 */
public class TupleReader {

//	private Tuple tuple;
	private FileChannel channel;
	private ByteBuffer bf;
	private FileInputStream fin;
	private static final int PAGE_SIZE = 4096;
	int index = 0;
	int numAttr;
	int numTuples;
	int[] colValues;
	int maxTuples;


	/**Construct the TupleReader object
	 * @param file the binary file to read from
	 */
	public TupleReader(String file) {
		try {
			fin = new FileInputStream(file);
			channel = fin.getChannel();
			bf = ByteBuffer.allocate(PAGE_SIZE);
			channel.read(bf);
			index = 1;
			numAttr = bf.getInt(0);
//			System.out.println("numAttr" + numAttr);
			numTuples = bf.getInt(4);
//			System.out.println(numTuples);
			colValues = new int[numAttr];
			maxTuples = (PAGE_SIZE - 8)/(4*numAttr);
//			System.out.println("max" + maxTuples);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**Given the index of the tuple, direct FileChannel to the page it is in
	 * and move the pointer to the beginning of that tuple
	 * @param index the index of the tuple that we want from the table
	 */
	public void reset(int index) {
		int pageIndex = index/maxTuples;
		try {
			channel.position(pageIndex*4096);
			getNextPage();
			int i=0;
			while(i<index % maxTuples) {
				getNextTuple();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**Read a page of given size into the buffer
	 * @return -1 if there is no next page, 1 otherwise
	 */
	public int getNextPage() {
//		try {
//			channel.read(bf);
//			numTuples = bf.getInt(4);
//			index=1;
//			return 1;
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return -1;
//		}
//		if(channel.position() >= channel.size()) {
//
//		}
		try {
			bf.clear();
			if(channel.read(bf) == -1) {
				return -1;
			}
			else {
				numTuples = bf.getInt(4);
				index=1;
				return 1;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Closes the channel and the file
	 */
	public void close() {
		try {
			channel.close();
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**Grab the next tuple from the file
	 * @return the next Tuple object starting at the current pointer
	 */
	public Tuple getNextTuple() {
		//if there is any more tuple on the current page
		if(index > numTuples) {
//			System.out.println("enter the loop");
			if(getNextPage() < 0) {return null;}
		}
		int i=0;
		while(i<numAttr) {
			colValues[i] = bf.getInt(8+4*(index-1)*numAttr+i*4);
//			System.out.println(colValues[i]);
			i++;
		}
//		System.out.println("\n");
		index++;
//		System.out.println("index" + index);
		return new Tuple(colValues.clone());
	}
}
