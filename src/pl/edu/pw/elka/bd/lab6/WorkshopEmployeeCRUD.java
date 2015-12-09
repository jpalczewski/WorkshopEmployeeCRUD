package pl.edu.pw.elka.bd.lab6;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Scanner;

import com.sun.corba.se.impl.orb.PrefixParserAction;
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
	
	private OracleDataSource ods;
	private Connection conn;
	private Scanner inputScanner = new Scanner(System.in);
    private boolean pessimisticEnabled;

    private String getPassword() {
        System.out.print("User password:");
        return inputScanner.next();
    }

    private String generateJDBC_URL(String password) {
        String prefix = "jdbc:oracle:thin:jpalczew/";
        String postfix = "@ora3.elka.pw.edu.pl:1521:ora3inf";
        return prefix + password + postfix;
    }

	/**
	 * Initializing DB connection.
	 * @throws SQLException
	 */
	private void init() throws SQLException {
        pessimisticEnabled = false;
        ods = new OracleDataSource();
        String password = getPassword();
		ods.setURL(generateJDBC_URL(password));
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

    private void readAllEmployees() throws SQLException {
        System.out.println("Reading all employees...");

        // Create a statement & Execute SQL
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery("select * FROM EMPLOYEE");

        System.out.println("Query result: ");

        String form = "%d. %20s %40s %20s %15s %8d\n";

        System.out.format("%s %20s %40s %20s %15s %8s\n", "ID", "FIRST NAME", "LAST NAME", "POSITION", "EMP. DATE", "HR_RATE");
        while (rset.next()) {
            System.out.format(form,
                    rset.getInt("EMPLOYEE_ID"), rset.getString("FNAME"),            rset.getString("LNAME"),
                    rset.getString("POSITION"), rset.getDate("EMPLOYMENT_DATE"),    rset.getInt("HOUR_RATE"));

        }

        // close the result set, the statement and connect
        rset.close();
        stmt.close();

    }

    private int getLastID() throws Exception {
        try {
            PreparedStatement ps = conn.prepareStatement("select max(EMPLOYEE_ID) from EMPLOYEE");
            ResultSet rs = ps.executeQuery();
            if(rs.getFetchSize()==0)
                return 0;
            rs.next();
            int retValue = rs.getInt(1);
            rs.close();
            ps.close();
            return (1+retValue);

        }
        catch(SQLException se) {
            throw new Exception("getLastE_ID failed\n"+se.getLocalizedMessage());
        }
    }

    private void createNewEmployee() throws Exception {
        int employeeNr = getLastID();
        System.out.println("Preparing new employee, nr " + employeeNr);
        Employee newEmployee = new Employee(employeeNr, inputScanner);
        PreparedStatement ps = newEmployee.makeStatement(conn);

        try {
            ps.executeQuery();
        }
        catch(SQLException se)
        {
            System.out.println("SQLException thrown - possibly someone stolen your EMPLOYEE_ID. Try again.");
            System.out.println(se.getLocalizedMessage());
        }


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

    private void updateEmployee() throws SQLException, ParseException {
        int who, version;
        ResultSet ret;
        System.out.print("Enter employee id to continue:");
        who = inputScanner.nextInt();
        PreparedStatement ps;
        if(pessimisticEnabled)
            ps = conn.prepareStatement("SELECT * FROM EMPLOYEE WHERE EMPLOYEE_ID=:1 FOR UPDATE");
        else
            ps = conn.prepareStatement("SELECT * FROM EMPLOYEE WHERE EMPLOYEE_ID=:1");

        ps.setInt(1, who);

        try {
            ret = ps.executeQuery();
            if(!ret.next()) throw new IllegalArgumentException("Employee not found!");
        }
        catch(SQLException se)
        {
            System.out.println(se.getLocalizedMessage());
            return;
        }
        catch(IllegalArgumentException ile)
        {
            System.out.println(ile.getMessage());
            return;
        }
        System.out.println("OK - employee found!");
        version = ret.getInt("VERSION");

        Employee upEmp = new Employee(ret, inputScanner);
        try {
			PreparedStatement ps2 = upEmp.updateStatement(conn, version);
			int updated = ps2.executeUpdate();
            if(updated==0) throw new IllegalArgumentException("Someone edited that employee before you - try again.");
		}
        catch(SQLException se) {
            System.out.println("Can't update record:\n" + se.getLocalizedMessage());
            return;
        }
        catch(IllegalArgumentException ile) {
            System.out.println(ile.getMessage());
            return;
        }

        System.out.println("Record updated!");
    }
    void deleteEmployee() throws SQLException
    {
        int id;
        readAllEmployees();
        System.out.print("Which employee do you want to delete:");
        id = inputScanner.nextInt();

        PreparedStatement ps = conn.prepareStatement("delete from EMPLOYEE where EMPLOYEE_ID=:1");
        ps.setInt(1, id);
        try {
            int affected = ps.executeUpdate();
            if(affected==0)
                throw new IllegalArgumentException("Statement didn't run - try bad id?");
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    void assignEmployees() throws SQLException {
        conn.setAutoCommit(false);
        try {
            Repairs repairs = new Repairs(conn);
            boolean good = true;
            int repairid, employeeid, hours;
            PreparedStatement ps = conn.prepareStatement("INSERT INTO EMPLOYEE_REPAIRS VALUES (:1, :2, :3)");

            while(good) {
                repairs.Print();
                System.out.print("Select repair id:");
                repairid = inputScanner.nextInt();
                readAllEmployees();
                System.out.print("Select employee id:");
                employeeid = inputScanner.nextInt();
                System.out.print("Enter how much employee spent on that repair(in hours):");
                hours = inputScanner.nextInt();

                ps.setInt(1, repairid);
                ps.setInt(2, employeeid);
                ps.setInt(3, hours);
                ps.executeQuery();
                System.out.print("Do you want to continue(y/n):");
                good = inputScanner.next(".").charAt(0) == 'y';
            }


            conn.commit();
            System.out.println("Commited!");
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.out.println("Doing a rollback...");
            conn.rollback();
        }
        finally
        {
            conn.setAutoCommit(true);
        }
    }

    void togglePessimistic() {
        pessimisticEnabled = !pessimisticEnabled;
        System.out.println("Pessismistic locking switched. Now: " + pessimisticEnabled);
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
							"\t (t) Assign employees with repairs\n" +
                            "\t (p) Toggle pessimistic locking on employee table\n" +
							"\t (e) Exit\n");
		System.out.println("Selected option(c,r,u,d,t,e):");
		userResponse = inputScanner.next(".").charAt(0);

        try {
            switch(userResponse)
            {
            case 'c':
                createNewEmployee();
                break;
            case 'r':
                readAllEmployees();
                break;
            case 'u':
                updateEmployee();
                break;
            case 'd':
                deleteEmployee();
                break;
            case 't':
                assignEmployees();
                break;
            case 'p':
                togglePessimistic();
                break;
            case 'e':
                return 0;
            default:
                System.out.println("Not recognized");
                return 0;
            }
        }
        catch(SQLException se) {
            System.out.println("SQL Exception thrown!");
            System.out.println("Exception msg: " + se.getLocalizedMessage());
            System.out.println("Exiting...");
            return 1;
        }
        catch(Exception e) {
            System.out.println("General exception thrown!");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return 1;
        }
		return 1;
	}		
	
	
	/**
	 * Runs menu.
	 *
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
			System.exit(1);
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
        System.exit(returnValue);
	}

}
