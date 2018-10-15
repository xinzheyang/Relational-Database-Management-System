/**
 * 
 */
package database;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author sitianchen
 *
 */
public class TupleWriter {
	
	private Tuple tuple;
	private FileChannel fc;
	private ByteBuffer buffer;
	private static final int PAGE_SIZE = 4096;
	
	public TupleWriter(String fileout) {
		try {
			FileOutputStream fout = new FileOutputStream(fileout);
			fc = fout.getChannel();
			buffer = ByteBuffer.allocate(PAGE_SIZE);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/** Gets the instance's tuple
	 * @return the instance's tuple
	 */
	public Tuple getTuple() {
		return tuple;
	}
	
	/** Sets the instance's tuple
	 * @param t: the tuple to be set
	 */
	public void setTuple(Tuple t) {
		this.tuple = t;
	}
	
	/** Writes a tuple to the file path specified
	 * @param fileout: the file path to be written out to
	 */
	public void write() {
		
	}

}
