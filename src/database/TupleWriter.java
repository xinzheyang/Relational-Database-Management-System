/**
 * 
 */
package database;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import operator.Operator;

/**
 * @author sitianchen
 *
 */
public class TupleWriter {
	
	private Tuple tuple;
	private FileChannel fc;
	private ByteBuffer buffer;
	private FileOutputStream fout;
	private static final int PAGE_SIZE = 4096;
	private int numTuples;
	private int numAttribs;
	
	public TupleWriter(String fileout, Operator op) {
		try {
			fout = new FileOutputStream(fileout);
			fc = fout.getChannel();
			buffer = ByteBuffer.allocate(PAGE_SIZE);
			numAttribs = op.getColumnIndexMap().size();
		    numTuples = 0;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
	
	public void close() {
		try {
			fc.close();
			fout.close();
		} catch (IOException e) {
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
	
	
	/** Puts meta data to buffer and then writes to channel.
	 * @param numAttribs
	 * @param numTuples
	 */
	public void writeMetaData() {
		buffer.putInt(numAttribs);
//		System.out.println(numAttribs);
		buffer.putInt(0); //number of tuples initialized as 0
	}
	
	public void flushLastPage() {
		if(numTuples == 0) {
			return;
		}
		buffer.putInt(4, numTuples);
		while (buffer.position() < buffer.capacity()) {
			buffer.put((byte) 0);
		}
		buffer.position(0);
		try {
			fc.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Writes a tuple to the file path specified
	 * @param fileout: the file path to be written out to
	 */
	public void writeToBuffer() {
		//clear buffer if exceed limit/capacity??? if limit exceed capacity
		if (buffer.position() + numAttribs * 4 > buffer.capacity()) {
//			System.out.println("hi");
			buffer.putInt(4, numTuples); //second int to be written
			//flush this page to the buffer
			buffer.position(0);
			try {
				fc.write(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			buffer.clear();
			writeMetaData();
			numTuples = 0;
		}
		for(int val : tuple.getColValues()) {
//			System.out.println(tuple);
			buffer.putInt(val);
		}
		++numTuples;
	}

}
