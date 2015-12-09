package pl.edu.pw.elka.bd.lab6;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Scanner;

import oracle.jdbc.pool.OracleDataSource;


/**
 * BD.A 
 * lab 6
 * Workshop - Employee CRUD.
 *	
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
		ods.setURL(generateJDBC_URL(getPassword()));
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


    private void readAllEmployees() throws SQLException {
        System.out.println("Reading all employees...");

        // Create a statement & Execute SQL
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery("select * FROM EMPLOYEE");

        System.out.println("Our fellow employees: ");

        System.out.format("%s %20s %40s %20s %15s %8s\n", "ID", "FIRST NAME", "LAST NAME", "POSITION", "EMP. DATE", "HR_RATE");
        while (rset.next())
            Toolbox.PrintEmployee(rset);

        // close the result set, the statement and connect
        rset.close();
        stmt.close();

    }

    private void createNewEmployee() throws Exception {
        Employee newEmployee = new Employee(Toolbox.getLastID(conn), inputScanner);
        PreparedStatement ps = newEmployee.makeStatement(conn);

        try {
            ps.executeQuery();
        }
        catch(SQLException se)
        {
            System.out.println("SQLException thrown - possibly someone stolen your EMPLOYEE_ID. Try again.");
            System.out.println(se.getLocalizedMessage());
        }
        finally {
            ps.close();
        }

    }

    private void updateEmployee() throws SQLException, ParseException {
        int version;
        ResultSet ret;
        PreparedStatement ps, ps2;

        if(pessimisticEnabled)
            ps = conn.prepareStatement("SELECT * FROM EMPLOYEE WHERE EMPLOYEE_ID=:1 FOR UPDATE");
        else
            ps = conn.prepareStatement("SELECT * FROM EMPLOYEE WHERE EMPLOYEE_ID=:1");


        System.out.print("Enter employee id to continue:");
        ps.setInt(1, inputScanner.nextInt());

        try {
            ret = ps.executeQuery();
            if(!ret.next()) throw new IllegalArgumentException("Employee not found!");
        }
        catch(SQLException se)
        {
            System.out.println(se.getMessage());
            return;
        }
        catch(IllegalArgumentException ile)
        {
            System.out.println(ile.getMessage());
            return;
        }

        System.out.println("OK - employee found!");

        try {
            version = ret.getInt("VERSION");
            Employee upEmp = new Employee(ret, inputScanner);

            ps2 = upEmp.updateStatement(conn, version);
            if(ps2.executeUpdate()==0)
                throw new IllegalArgumentException("Someone edited that employee before you - try again.");
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

        ret.close();
        ps.close();
        ps2.close();
    }


    void deleteEmployee() throws SQLException {
        PreparedStatement ps = conn.prepareStatement("delete from EMPLOYEE where EMPLOYEE_ID=:1");
        readAllEmployees();

        System.out.print("Which employee do you want to delete:");
        ps.setInt(1, inputScanner.nextInt());

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
            PreparedStatement ps = conn.prepareStatement("INSERT INTO EMPLOYEE_REPAIRS VALUES (:1, :2, :3)");

            while(good) {
                repairs.Print();
                System.out.print("Select repair id:");
                ps.setInt(1, inputScanner.nextInt());

                readAllEmployees();
                System.out.print("Select employee id:");
                ps.setInt(2, inputScanner.nextInt());

                System.out.print("Enter how much employee spent on that repair(in hours):");
                ps.setInt(3, inputScanner.nextInt());

                ps.executeQuery();

                System.out.print("Do you want to continue(y/n):");
                good = inputScanner.next(".").charAt(0) == 'y';
            }


            conn.commit();
            System.out.println("Committed!");
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
