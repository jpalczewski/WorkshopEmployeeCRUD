package pl.edu.pw.elka.bd.lab6;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import oracle.jdbc.pool.OracleDataSource;


/**
 * BD.A 
 * lab 6
 * Workshop - Employee CRUD.
 *	
 * @author B.Twardowski <B.Twardowski@ii.pw.edu.pl>
 *
 */
public class WorkshopEmployeeCRUD {

	//const's
	private static String JDBC_URL = "jdbc:oracle:thin:jpalczew/jpalczew@ora3.elka.pw.edu.pl:1521:ora3inf";
	
	
	private OracleDataSource ods;
	private Connection conn;
	private Scanner inputScanner = new Scanner(System.in);
	
	
	
	/**
	 * Initializing DB connection.
	 * @throws SQLException
	 */
	private void init() throws SQLException {
		ods = new OracleDataSource();
		ods.setURL(JDBC_URL);
		conn = ods.getConnection();
		DatabaseMetaData meta = conn.getMetaData();
		System.out.println("Successfully connected to DB. JDBC driver version is " + meta.getDriverVersion());
	    
	}
	
	/**
	 * Closing DB connection.
	 * @throws SQLException
	 */
	private void close() throws SQLException {
		conn.close();
		System.out.println("Database connection successfully closed.");
	}
	
	
	/**
	 * Showcase.
	 * @throws SQLException
	 */
	public void doShowcase() throws SQLException {
		init();
	//	simpleTest();
	//	preparedStatementShowcace();
	//	transactionShowcace();
		close();
	}
	
	
	/**
	 * Simple select statement.
	 * @throws SQLException
	 */
	private void simpleTest() throws SQLException {
	   
		System.out.println("Simple query statement test...");
		
	    // Create a statement
	    Statement stmt = conn.createStatement();

	    // Execute SQL
	    ResultSet rset = stmt.executeQuery("select * FROM CUSTOMERS");

	    System.out.println("Query result: ");
	    
	    int i = 1;
	    while (rset.next()) {
	       System.out.println("[" + i + "]:" + rset.getString(1));
	       System.out.println("[" + i + "]:" + rset.getString(2));
	       i++;
	    }
	    
	    // close the result set, the statement and connect
	    rset.close();
	    stmt.close();
	    
	}
	
	
	/**
	 * Prepared statement showcase.
	 * @throws SQLException
	 */
	private void preparedStatementShowcace() throws SQLException {
		
		System.out.println("Prepared statement showcase...");
		
		//TODO: To modify!!!
		
		PreparedStatement preparedStatement = conn.prepareStatement("select * FROM CUSTOMERS");
		//get params from console
		System.out.println("Type employee name :" );
		preparedStatement.setString(1, inputScanner.nextLine() );
		
		ResultSet rs = preparedStatement.executeQuery();
		
	    int i = 1;
	    while (rs.next()) {
	       System.out.println("[" + i + "]:" + rs.getString(1));
	       i++;
	    }
	    
	    // close the result set, the statement and connect
	    rs.close();
	    preparedStatement.close();
		
	}

	
	
	/**
	 * Transaction showcase.
	 * @throws SQLException
	 */
	private void transactionShowcace() throws SQLException {
		
		System.out.println("Transactions statement showcase...");
		
		//TODO: To fill...
		
		conn.setAutoCommit(false);
		
		//operations - selects/updates/inserts with user interactions
		
		//if something wrong 
		//conn.rollback();
		
		//if no error
		//conn.commit();

	}
	
	/**
	 * Shows a menu.
	 * 
	 */
	public int Menu()
	{
		char userResponse;
		
		System.out.println(	"Menu:\n" +
							"\t (c) Create new employee\n" +
							"\t (r) Read all employees\n"  +
							"\t (u) Update an employee\n"  +	
							"\t (d) Delete an employee\n"  +
							"\t (t) Connect an employee with repair\n" +
							"\t (e) Exit\n");
		System.out.println("Selected option(c,r,u,d,t):");
		userResponse = inputScanner.next(".").charAt(0);
		switch(userResponse)
		{
		case 'c':
			System.out.println("ccc");
			break;
		case 'r':
			System.out.println("rrr");
			break;
		case 'u':
			System.out.println("uuu");
			break;
		case 'd':
			System.out.println("ddd");
			break;
		case 't':
			System.out.println("ttt");
			break;
		case 'e':
			return 0;
		default:
			System.out.println("Not recognized");
			return 0;
		}
		
		return 1;
	}		
	
	
	/**
	 * Run showcase.
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws SQLException {
		System.out.println("WorkshopEmployeeCRUD");
		
		WorkshopEmployeeCRUD wec = new WorkshopEmployeeCRUD();
		int returnValue = 0;
		
		try
		{
			System.out.println("Connecting with database...");
			wec.init();
		}
		catch(SQLException e)
		{
			System.out.println("Error: cannot connect with database!");
			returnValue = 1;
		}
		
		
		
		try 
		{
			while(wec.Menu() != 0)
					;
		}
		catch(java.util.InputMismatchException e)
		{
			System.out.println("[-] Only one char allowed!");
			returnValue = 1;
		}
		finally
		{
			wec.close();
		}
		
		
		//return returnValue;
	}

}
