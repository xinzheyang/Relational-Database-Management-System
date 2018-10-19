==================== Project 2 checkpoint ====================
The top-level class is Main.java in the database package

The logical operators are in the package “logicaloperator”
The physical operators are in the package “physicaloperator”
The PhysicalPlanBuilder is in the package “visitor”


==================== Project 1 ====================
- Our program is run by Main.java with its main() method calling our interpreter (the QueryParser class along with DBStatementVisitor, DBSelectVisitor and DBFromItemVisitor) to parse the input queries.

- Our logic for extracting join conditions from the WHERE clause comes as the following:
we create a ParseConjunctExpVisitor that implements the ExpressionVisitor interface: 
   -two maps, a joinMap and a selectMap are declared as fields for each instance of this visitor class. The visitor should divide up the conjunctions in the WHERE clause from top-down until a LongValue or Column expression is found. 
   -Every time an expression is found involving one or more table (involve one or more Column expressions, etc.), we push the table name to a stack (tbStack), and later we pop from the table stack when the visitor returns from visiting the left and right expressions of a BinaryExpression that is NOT an AndExpression (one of =, ! =, <, >, <=, >=). 
   -Then we check for the number of tables in the stack by popping the tables out - if there are two tables, we know this is a join condition and we add/conjunct the two tables as key mapping to this binary expression to the joinMap; if there is exactly one table, we know that this is a select condition and we add/conjunct the table name as key mapping to this binary expression to the selectMap; if no tables were in the stack, the expression is a constant boolean and we simply evaluate the expression by the EvaluateExpVisitor - if the expression is true, we simply skip and ignore it in the where clause because it won't affect the final evaluation of the conjunctions; if it's false, we deliver this message to the query plan by the isAlwaysFalse field as the where clause must be false from this constant expression.
   -After the WHERE clause gets visited by a ParseConjunctExpVisitor, we look for select conditions by accessing the visitor's selectMap for every table we are joining from left to right and add a selectOperator to the top of the table's scanOperator if necessary. We also look for join conditions when joining a new table by accessing the visitor's joinMap. Since every key in the joinMap is tables in pairs, we iterate over all existing left tables joined to find join conditions with the new right table to be joined, and conjunct all join conditions to the new JoinOperator to be initialized.