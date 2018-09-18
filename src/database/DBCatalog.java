/**
 * 
 */
package database;

/**
 * The catalog can keep track of information such as
 * where a file for a given table is located, what the schema of different tables is, 
 * and so on. The catalog is a global entity, therefore we use a singleton pattern.
 * @author sitianchen
 *
 */
public class DBCatalog {
	
	private static DBCatalog catalog = new DBCatalog(); //singleton object for global reference
	
	/* A private Constructor prevents any other class
	 * from instantiating a DBCatalog object.
	 */
	private DBCatalog() {
		
	}
	
	/* Static get instance method, gets the singleton instance
	 * of the class.
	 */
	public static DBCatalog getCatalog() {
		return catalog;
	}

}
