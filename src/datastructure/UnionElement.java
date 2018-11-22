/**
 * 
 */
package datastructure;

import java.util.List;

/**
 * @author xinzheyang a protected class that houses information of a union
 *         element
 */
class UnionElement {
	private List<String> attributes;
	private int lower = Integer.MIN_VALUE;
	private int upper = Integer.MAX_VALUE;
	private int equality = Integer.MIN_VALUE;

	private UnionElement(int low, int up, int eq) {
		lower = low;
		upper = up;
		equality = eq;
	}

	/**
	 * @return the attributes
	 */
	public List<String> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}

	/**
	 * @return the lower
	 */
	public int getLower() {
		return lower;
	}

	/**
	 * @param lower
	 *            the lower to set
	 */
	public void setLower(int lower) {
		this.lower = lower;
	}

	/**
	 * @return the upper
	 */
	public int getUpper() {
		return upper;
	}

	/**
	 * @param upper
	 *            the upper to set
	 */
	public void setUpper(int upper) {
		this.upper = upper;
	}

	/**
	 * @return the equality
	 */
	public int getEquality() {
		return equality;
	}

	/**
	 * @param equality
	 *            the equality to set
	 */
	public void setEquality(int equality) {
		this.equality = equality;
	}

}
