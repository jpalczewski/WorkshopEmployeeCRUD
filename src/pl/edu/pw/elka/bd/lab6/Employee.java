package pl.edu.pw.elka.bd.lab6;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;


public class Employee {
    int EMPLOYEE_ID;
    String FNAME;
    String LNAME;
    String POSITION;
    java.sql.Date EMPLOYMENT_DATE;
    int HOUR_RATE;

    Scanner inputScanner;
    boolean updateMode;

    public static String skipEmptyLines(Scanner fileIn) {
        String line;
        while (fileIn.hasNext()) {
            if (!(line = fileIn.nextLine()).isEmpty()) {
                return line;
            }
        }
        return null;
    }

    Employee(int id, Scanner s) throws ParseException
    {
        EMPLOYEE_ID = id;
        inputScanner = s;
        updateMode = false;


        interactWithUser();
    }
    Employee(ResultSet rs, Scanner s) throws SQLException, ParseException
    {
        inputScanner = s;
        updateMode = true;

        EMPLOYEE_ID = rs.getInt("EMPLOYEE_ID");
        FNAME = rs.getString("FNAME");
        LNAME = rs.getString("LNAME");
        POSITION = rs.getString("POSITION");
        EMPLOYMENT_DATE = rs.getDate("EMPLOYMENT_DATE");
        HOUR_RATE = rs.getInt("HOUR_RATE");

        System.out.println("Actual data:");
        Toolbox.PrintEmployee(this);

        System.out.println("If you don't want to edit something, you have to reenter it.");
        interactWithUser();

    }
    private String readString(String msg)
    {
        System.out.print(msg);
        return inputScanner.next();
    }
    private void interactWithUser() throws ParseException
    {
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");

        FNAME = readString("First name:");
        LNAME = readString("Last name:");
        POSITION = readString("Position:");

        System.out.print("Employment date(yyyy/MM/dd):");
        EMPLOYMENT_DATE = new java.sql.Date(df.parse(inputScanner.next()).getTime());

        System.out.print("Hour rate:");
        HOUR_RATE = inputScanner.nextInt();
    }


    public PreparedStatement makeStatement(Connection conn) throws SQLException
    {
        PreparedStatement ps = conn.prepareStatement("insert into EMPLOYEE values (:1, :2, :3, :4, :5, :6, 0)");

        ps.setInt(1, EMPLOYEE_ID);
        ps.setString(2, FNAME);
        ps.setString(3, LNAME);
        ps.setString(4, POSITION);
        ps.setDate(5, EMPLOYMENT_DATE);
        ps.setInt(6, HOUR_RATE);

        return ps;
    }

    public PreparedStatement updateStatement(Connection conn, int version) throws SQLException
    {
        PreparedStatement ps = conn.prepareStatement("update EMPLOYEE SET FNAME=:1, LNAME=:2, POSITION=:3, EMPLOYMENT_DATE=:4, HOUR_RATE=:5, VERSION=:6 where EMPLOYEE_ID=:7 and VERSION=:8");

        ps.setString(1, FNAME);
        ps.setString(2, LNAME);
        ps.setString(3, POSITION);
        ps.setDate(4, EMPLOYMENT_DATE);
        ps.setInt(5, HOUR_RATE);
        ps.setInt(6, version+1);
        ps.setInt(7, EMPLOYEE_ID);
        ps.setInt(8, version);

        return ps;
    }


}
