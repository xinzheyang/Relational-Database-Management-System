/**
 * 
 */
package visitor;

import static org.junit.Assert.*;

import org.junit.Test;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

/**
 * @author sitianchen
 *
 */
public class CheckEquityTest {

	@Test
	public void test() {
		BinaryExpression equi1 = new EqualsTo();
		Table sailors = new Table("", "Sailors");
		Table boats = new Table("", "Boats");
		equi1.setLeftExpression(new Column(sailors, "A"));
		equi1.setRightExpression(new Column(boats, "C"));
		CheckAllEquityExpVisitor vis = new CheckAllEquityExpVisitor();
//		equi1.accept(vis);
//		assert vis.isAllEquity();
		BinaryExpression equi2 = new EqualsTo();
		equi2.setLeftExpression(new Column(boats, "B"));
		equi2.setRightExpression(new Column(sailors, "F"));
		AndExpression and1 = new AndExpression();
		and1.setLeftExpression(equi1);
		and1.setRightExpression(equi2);
//		System.out.println(equi1);
//		System.out.println(equi2);
//		System.out.println(and1);
		and1.accept(vis);
		assert vis.isAllEquity();
	}

}
