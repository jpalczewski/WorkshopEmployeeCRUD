package pl.edu.pw.elka.bd.lab6;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Toolbox {

    static int getLastID(Connection conn) throws Exception {
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

    private static void _PrintEmp(int empid, String fname, String lname, String pos, java.sql.Date date, int hour_rate)
    {
        System.out.format("%3d. %20s %40s %20s %15s %8d\n",
                empid, fname, lname, pos, date, hour_rate);
    }

    static void PrintEmployee(ResultSet rset) throws SQLException
    {
        _PrintEmp( rset.getInt("EMPLOYEE_ID"), rset.getString("FNAME"),  rset.getString("LNAME"), rset.getString("POSITION"), rset.getDate("EMPLOYMENT_DATE"), rset.getInt("HOUR_RATE"));
    }

    static void PrintEmployee(Employee emp)
    {
        _PrintEmp(emp.EMPLOYEE_ID, emp.FNAME, emp.LNAME, emp.POSITION, emp.EMPLOYMENT_DATE, emp.HOUR_RATE);
    }


}
