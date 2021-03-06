/**
 * 
 */
package bplustree;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

/** This class functions as a serializer for a B+ Tree. Serializes different types of
 * nodes of the tree according to the required format into the relation's index file.
 * 
 * @author sitianchen
 */
public class TreeSerializer {
	private FileChannel fc;
	private ByteBuffer buffer;
	private FileOutputStream fout;
	private static final int PAGE_SIZE = 4096;
	private String fileout; // output or input path

	public TreeSerializer(String fileout) throws FileNotFoundException {
		this.fileout = fileout;
		fout = new FileOutputStream(fileout);
		fc = fout.getChannel();
		buffer = ByteBuffer.allocate(PAGE_SIZE);
	}

	/** Closes the current serializer.
	 * @throws IOException
	 */
	public void close() throws IOException {
		fc.close();
		fout.close();
	}

	/** Serializes the header of this tree.
	 * @param order
	 * @throws IOException
	 */
	public void serializeHeader(int order) throws IOException {
		buffer.clear();
		buffer.putInt(0); // placeholder for root address
		buffer.putInt(0); //placeholder for number of leaf nodes
		buffer.putInt(order);
		buffer.position(0);
		fc.write(buffer);
	}

	/**
	 * Serializes the header page of the tree, in order of: - the address of the
	 * root, stored at offset 0 on the header page - the number of leaves in the
	 * tree, at offset 4 - the order of the tree, at offset 8
	 * 
	 * @param rootAddress
	 * @param numberOfLeaves
	 * @param order
	 * @throws IOException
	 */
	public void updateHeader(int rootAddress, int numberOfLeaves) throws IOException {
		fc.position(0);
		buffer.clear();
		FileInputStream fin = new FileInputStream(fileout);
		FileChannel readChannel = fin.getChannel();
		readChannel.read(buffer);
		readChannel.close();
		fin.close();
		buffer.flip();
		buffer.putInt(rootAddress);
		buffer.putInt(numberOfLeaves);
		buffer.position(0);
		fc.write(buffer);
	}

	/**
	 * Serializes an index node, in order of: - the integer 1 as a flag to indicate
	 * this is an index node (rather than a leaf node) - the number of keys in the
	 * node - the actual keys in the node, in order - the addresses of all the
	 * children of the node, in order
	 * 
	 * @param in
	 * @throws IOException
	 */
	public void serialize(IndexNode in) throws IOException {
		buffer.clear();
		buffer.putInt(1);
		List<Integer> childAddresses = in.getChildAddresses();
		List<Integer> keys = in.getKeys();
		buffer.putInt(keys.size());
		for (Integer key : keys) {
			buffer.putInt(key);
		}

		for (Integer addr : childAddresses) {
			buffer.putInt(addr);
		}
		while (buffer.position() < buffer.capacity()) {
			buffer.put((byte) 0);
		}

		buffer.position(0);
		fc.write(buffer);
	}

	/**
	 * Serializes a leaf node, in order of: - the integer 0 as a flag to indicate
	 * this is a leaf node - the number of data entries in the node - the serialized
	 * representation of each data entry in the node, in order.
	 * 
	 * @param ln
	 * @throws IOException
	 */
	public void serialize(LeafNode ln) throws IOException {
		buffer.clear();
		buffer.putInt(0);

		List<Integer> keys = ln.getKeys();
		List<List<int[]>> listOfRids = ln.getListOfRids();
		int numOfEntries = keys.size();
		buffer.putInt(numOfEntries);

		for (int i = 0; i < numOfEntries; i++) {

			buffer.putInt(keys.get(i));
			List<int[]> rids = listOfRids.get(i);
			buffer.putInt(rids.size());

			for (int[] rid : listOfRids.get(i)) {
				buffer.putInt(rid[0]);
				buffer.putInt(rid[1]);
			}
		}
		while (buffer.position() < buffer.capacity()) {
			buffer.put((byte) 0);
		}

		buffer.position(0);
		fc.write(buffer);
	}

}
