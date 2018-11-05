/**
 * 
 */
package test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import database.DBCatalog;
import database.Tuple;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import physicaloperator.*;

/**
 * @author sitianchen
 * Several simple test cases for scan and select.
 */
public class SelectOperatorTest {
	
	public void initializeCatalog() throws IOException {
		DBCatalog.getCatalog().parseSchema("db");
	}

	@Test
	public void test() throws IOException {
		initializeCatalog();
		ScanOperator scan1 = new ScanOperator("Sailors", "");
		GreaterThan gt0 = new GreaterThan();
		gt0.setLeftExpression(new LongValue("2"));
		gt0.setRightExpression(new LongValue("3"));
		SelectOperator select10 = new SelectOperator(scan1, gt0);
		assert select10.getNextTuple() == null;
		scan1.reset();
		
		GreaterThan gt1 = new GreaterThan();
		gt1.setLeftExpression(new LongValue("2"));
		gt1.setRightExpression(new LongValue("1"));
		SelectOperator select11 = new SelectOperator(scan1, gt1);
		Tuple next0 = select11.getNextTuple();
		assert next0 != null;
//		System.out.println(next);
		assert next0.toString().equals("1,200,50");
		scan1.reset();
		
		Table t1 = new Table("", "Sailors");
		Column c1 = new Column(t1, "A");
		GreaterThan gt2 = new GreaterThan();
		gt2.setLeftExpression(c1);
		gt2.setRightExpression(new LongValue("1"));
		SelectOperator select12 = new SelectOperator(scan1, gt2);
		Tuple next1 = select12.getNextTuple();
		assert next1 != null;
		assert next1.toString().equals("2,200,200");
		Tuple next2 = select12.getNextTuple();
		assert next2 != null;
		assert next2.toString().equals("3,100,105");
	}

}
