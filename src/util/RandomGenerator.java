/**
 * 
 */
package util;

import java.io.File;
import java.util.HashMap;
import java.util.Random;

import database.Tuple;
import database.TupleWriter;

/**
 * @author xinzheyang
 * A random byte table generator
 */
public class RandomGenerator {
	
	private String path;
	/**
	 * @param dir the directory to store the generated binary file
	 */
	public RandomGenerator(String dir) {
		path = dir;
	}
	
	
	/**
	 * @param fname fname to store the binary file
	 * @param nRow number of rows of the table
	 * @param nCol number of columns of the table
	 * @param lo the lower bound of random generator
	 * @param hi the upper bound of random generator
	 */
//	public void generate(String fname, int nRow, int nCol, int lo, int hi) {
//		TupleWriter tw = new TupleWriter(path + File.separator + fname, nCol);
//		tw.writeMetaData();
//		for (int i = 0; i < nRow; i++) {
//			StringBuilder sb = new StringBuilder();
//			for(int j=0; j< nCol; j++) {
//				Random rand = new Random(); 
//				int randomNum = rand.nextInt((hi - lo) + 1) + lo;
//				sb.append(randomNum);
//				sb.append(",");
//			}
//			sb.deleteCharAt(sb.length()-1);
//			Tuple tuple = new Tuple(sb.toString());
//			tw.setTuple(tuple);
//			tw.writeToBuffer();
//		}
//		tw.flushLastPage();
//		tw.close();
//	}
	public void generate(String fname, int nRow, int nCol, int lo, int hi, int index) {
	TupleWriter tw = new TupleWriter(path + File.separator + fname, nCol);
	tw.writeMetaData();
	HashMap<Integer, Integer> map = new HashMap<>();
	Random rand = new Random(); 
	for (int i = 0; i < nRow; i++) {
		StringBuilder sb = new StringBuilder();
		for(int j=0; j< nCol; j++) {
			if(j==index) {
				int randomNum = rand.nextInt((hi - lo) + 1) + lo;
				while(map.containsKey(randomNum) && map.get(randomNum)>=4) {
					randomNum=rand.nextInt((hi - lo) + 1) + lo;
				};
				map.put(randomNum, map.getOrDefault(randomNum, 0)+1);
				sb.append(randomNum);
				sb.append(",");
			}
			else {
				int randomNum = rand.nextInt((hi - lo) + 1) + lo;
				sb.append(randomNum);
				sb.append(",");
			}
//			Random rand = new Random(); 
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
		randomGenerator.generate("Boats", 1000, 3, 0, 1000, 1);

	}

}
