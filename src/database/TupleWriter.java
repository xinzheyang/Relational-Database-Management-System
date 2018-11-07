/**
 *
 */
package database;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import physicaloperator.Operator;

/** A class for writing tuples to binary files. Uses the Java NIO package
 * to write page by page.
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

	/**
	 * @param fileout    file output path
	 * @param op  the operator to be written
	 */
	public TupleWriter(String fileout, Operator op) {
		try {
			fout = new FileOutputStream(fileout);
			fc = fout.getChannel();
			buffer = ByteBuffer.allocate(PAGE_SIZE);
			numAttribs = op.getColumnIndexMap().size();
		    numTuples = 0;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * @param fileout  file output path
	 * @param nCols  number of columns to be generated
	 */
	public TupleWriter(String fileout, int nCols) {
		try {
			fout = new FileOutputStream(fileout);
			fc = fout.getChannel();
			buffer = ByteBuffer.allocate(PAGE_SIZE);
			numAttribs = nCols;
		    numTuples = 0;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 *  closes the TupleWriter and output channel
	 */
	public void close() {
		try {
			fc.close();
			fout.close();
		} catch (IOException e) {
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


	/** Puts meta data to buffer.
	 * @param numAttribs
	 * @param numTuples
	 */
	public void writeMetaData() {
		buffer.putInt(numAttribs);
		buffer.putInt(0); //number of tuples initialized as 0
	}

	/** Flushes the last page to be written out from the buffer
	 * to the channel, fill remaining page with 0s and then write the page out.
	 *  To be called in Operator.dump()
	 */
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
		if (buffer.position() + numAttribs * 4 > buffer.capacity()) {
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
			buffer.putInt(val);
		}
		++numTuples;
	}

}
