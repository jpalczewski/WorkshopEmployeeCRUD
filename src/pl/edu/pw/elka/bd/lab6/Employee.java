package pl.edu.pw.elka.bd.lab6;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

/**
 * Created by erxyi on 08.12.2015.
 */
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
        String line = "";
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
        System.out.format("%d. %20s %40s %20s %15s %8d\n",
                rs.getInt("EMPLOYEE_ID"), rs.getString("FNAME"),            rs.getString("LNAME"),
                rs.getString("POSITION"), rs.getDate("EMPLOYMENT_DATE"),    rs.getInt("HOUR_RATE"));

        System.out.println("If you don't want to edit something, you have to reenter it.");
        interactWithUser();

    }
    private String readString(String msg, String oldvalue)
    {
        System.out.print(msg);
        return inputScanner.next();

    }
    private void interactWithUser() throws ParseException
    {
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        String input;
        java.sql.Date temp;
        int tempval;

        FNAME = readString("First name:", FNAME);
        LNAME = readString("Last name:", LNAME);
        POSITION = readString("Position:", POSITION);

        System.out.print("Employment date(yyyy/MM/dd):");
        input = inputScanner.next();
        temp = new java.sql.Date(df.parse(input).getTime());
        if (!updateMode || !input.isEmpty())
            EMPLOYMENT_DATE = temp;

        System.out.print("Hour rate(-1 don't update while editing):");
        tempval = inputScanner.nextInt();
        if (!updateMode || tempval!=-1)
            HOUR_RATE = tempval;
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
