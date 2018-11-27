/**
 * 
 */
package datastructure;

import java.util.LinkedList;
import java.util.List;

import net.sf.jsqlparser.schema.Column;

/**
 * @author xinzheyang a protected class that houses information of a union
 *         element
 */
public class UnionElement {
	private List<Column> attributes;
	private Integer lower;
	private Integer upper;
	private Integer equality;
	private int rootId;
	
	public UnionElement(int root) {
		attributes = new LinkedList<>();
		rootId = root;
	}

	public List<Column> getAttrByTable(String tbReference) {
		List<Column> res = new LinkedList<>();
		for (Column col : attributes) {
			if (tbReference.equals(col.getTable().getName()))
				res.add(col);
		}
		return res;
	}
	/**
	 * @return the attributes
	 */
	public List<Column> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(List<Column> attributes) {
		this.attributes = attributes;
	}

	/**
	 * @param attr
	 */
	public void addAttribute(Column attr) {
		attributes.add(attr);
	}
	
	/**
	 * @return the lower
	 */
	public Integer getLower() {
		return lower;
	}

	/**
	 * @param lower the lower to set
	 */
	public void setLower(Integer lower) {
		if (lower != null && lower < this.lower)
			this.lower = lower;
	}

	/**
	 * @return the upper
	 */
	public Integer getUpper() {
		return upper;
	}

	/**
	 * @param upper the upper to set
	 */
	public void setUpper(Integer upper) {
		if (upper != null && upper > this.upper)
			this.upper = upper;
	}

	/**
	 * @return the equality
	 */
	public Integer getEquality() {
		return equality;
	}

	/**
	 * @param equality the equality to set
	 */
	public void setEquality(Integer equality) {
		this.equality = equality;
	}

	/**
	 * @return the rootId
	 */
	public int getRootId() {
		return rootId;
	}

	/**
	 * @param rootId the rootId to set
	 */
	public void setRootId(int rootId) {
		this.rootId = rootId;
	}

	

}
