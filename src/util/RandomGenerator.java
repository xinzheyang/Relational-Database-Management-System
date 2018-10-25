/**
 * 
 */
package util;

import java.io.File;
import java.util.Random;

import database.Tuple;
import database.TupleWriter;

/**
 * @author xinzheyang
 *
 */
public class RandomGenerator {
	
	private String path;
	/**
	 * 
	 */
	public RandomGenerator(String dir) {
		path = dir;
	}
	
	
	public void generate(String fname, int nRow, int nCol, int lo, int hi) {
		TupleWriter tw = new TupleWriter(path + File.separator + fname, nCol);
		tw.writeMetaData();
		for (int i = 0; i < nRow; i++) {
			StringBuilder sb = new StringBuilder();
			for(int j=0; j< nCol; j++) {
				Random rand = new Random(); 
				int randomNum = rand.nextInt((hi - lo) + 1) + lo;
				sb.append(randomNum);
				sb.append(",");
			}
			sb.deleteCharAt(sb.length()-1);
			Tuple tuple = new Tuple(sb.toString());
			tw.setTuple(tuple);
			tw.writeToBuffer();
		}
		tw.flushLastPage();
		tw.close();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RandomGenerator randomGenerator = new RandomGenerator("random");
		randomGenerator.generate("Sailors", 1000, 3, 0, 1500);

	}

}
