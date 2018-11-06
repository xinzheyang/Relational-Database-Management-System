==================== Project 3 ====================

The top-level class is Main.java in the database package

IndexScanOperator
- the lowkey and highkey are determined in the class DivideSelectVisitor
  And they are passed into the IndexScanOperator constructor in the PhysicalPlanBuilder

- In the IndexScanOperator, I handled the clustered and unclustered in the constructor and also in getNextTuple() and reset()

- I performed the root-to-leaf descent by recursively performing binary search on the keys of the index nodes, and return immediately when a leaf node is found.

I separated out the selection in physical plan builder using a visitor called DivideSelectVisitor, which implements the ExpressionVisitor interface. In the visitor, I determined the seperation by doing case analysis on base expressions such as GreaterThan, LessThan and etc.

==================== Project 2 main submission ====================
The top-level class is Main.java in the database package

- We implemented the external sort operator which is in physicaloperator/ExternalSortOperator.java which inherits an abstract SortOperator.java
according to the algorithm described in the textbook.

- Our SMJ operator is located in physicaloperator/SMJoinOperator.java. It extends the abstract JoinOperator and overrides the constructor. 
  - Sorting phase: A left and a right SortOperator are pushed down on top of the two children of the SMJ in the PhysicalPlanBuilder, in order to sort the child relations before the join phase.
  - Join phase: The merging/join phase logic of SMJ follows basically the algorithm in page 460 of the textbook, except that we modified the algorithm so that it returns one tuple every time getNextTuple() of the operator is called, until all tuples in the outer (left) relation and inner (right) relation are scanned, and the method return s null. We achieve this by checking the condition if the current left and right tuples are equal, entering the innermost while loop first if the condition is satisfied, and then exit the while loop and enter the other levels by conditions. We also check the null case for the right tuple first and reset it back to the start of the current partition if it's not done scanning all of the left tuples.
  - Tuple Comparison: we implemented a compareByCondition(Tuple tl, Tuple tr) helper method to compare tuples with the join condition. We first extract the column values in order of the equality conjunct by an instance of the visitor/EquiConjunctVisitor.java, and then compare the values in this order until one pair of values differ or the comparison finds that all column values are equal.

-Our BNLJ operator is located in physicaloperator/BNLJoinOperator.java. It extends the abstract JoinOperator and overrides the constructor. We follow the instruction on the writeup for the Block-Nested Loop Join. Taking in a parameter as buffer size in pages, we calculate the number of tuples on the buffer and take in left tuples by calling getNextTuple on the left operator. Then we just take in right Tuple and combine it with the tuples in the buffer one by one until all the tuples in the buffer are joined. Then we get the next right tuple and join it with the tuples in the buffer again. We update the buffer to receive a new bunch of tuples if the right tuples are used up. We also reset the right tuples to the beginning as well. Then we repeat the process above.

Acknowledgement: Raghu Ramakrishnan, Johannes Gehrke; Database Management Systems

==================== Project 2 checkpoint ====================
The top-level class is Main.java in the database package

TupleReader.java and TupleWriter.java added to package "database"

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
