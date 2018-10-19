/**
 * 
 */
package database;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
/**
 * @author xinqi
 *
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
	
	public void reset(int index) {
		int pageIndex = index/maxTuples;
		try {
			channel.position(pageIndex*4096);
			getNextPage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
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
	
	public void close() {
		try {
			channel.close();
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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
