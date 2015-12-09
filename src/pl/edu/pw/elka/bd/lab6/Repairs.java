package pl.edu.pw.elka.bd.lab6;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by erxyi on 09.12.2015.
 */


public class Repairs {
    public class Repair {
        int id;
        String car;
        java.sql.Date date;

        Repair(ResultSet rs) throws SQLException
        {
            id = rs.getInt("REPAIR_ID");
            car = rs.getString("BRAND") + " " + rs.getString("MODEL");
            date = rs.getDate("REPAIR_DATE");
        }

        String ToString()
        {
            return String.format("%5d. %41s %10s", id, car, date);
        }
    };

    ArrayList<Repair> repairList;

    Repairs(Connection conn) throws SQLException {
        repairList = new ArrayList<>();

        PreparedStatement ps = conn.prepareStatement("SELECT r.REPAIR_ID, c.BRAND , c.MODEL, r.REPAIR_DATE FROM REPAIRS r, CARS c WHERE r.CAR_ID = c.CAR_ID ORDER BY r.REPAIR_ID");
        ResultSet rs = ps.executeQuery();

        while(rs.next())
        {
            repairList.add(new Repair(rs));
        }

        rs.close();
        ps.close();
    }

    void Print()
    {

        for (Repair r:
             repairList) {
            System.out.println(r.ToString());

        }
    }
}
