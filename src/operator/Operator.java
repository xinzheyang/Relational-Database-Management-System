/**
 * 
 */
package operator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import database.Tuple;

/**
 * @author xinzheyang
 *
 */
public abstract class Operator {
	
	/**
	 * @return
	 */
	public abstract Tuple getNextTuple();
	/**
	 * 
	 */
	public abstract void reset();
	
	public void dump(String fileOut) {
		BufferedWriter bw = null;
		try {
			//	 String mycontent = "This String would be written" +
			//	    " to the specified File";
			//Specify the file name and path here
			File file = new File(fileOut);

			/* This logic will make sure that the file 
			 * gets created if it is not present at the
			 * specified location*/
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			Tuple tup;
			while((tup=getNextTuple()) != null) {
				bw.write(tup.toString());
				bw.newLine();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		finally
		{ 
			try{
				if(bw!=null)
					bw.close();
			}catch(Exception ex){
				System.out.println("Error in closing the BufferedWriter"+ex);
			}
		}
	};
}
