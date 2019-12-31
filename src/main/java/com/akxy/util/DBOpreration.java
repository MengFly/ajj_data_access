package com.akxy.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//import org.junit.jupiter.api.Test;

public class DBOpreration {
	
//	@Test
	public  void DBCollection() throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        //下面这个千万千万千万要注意，1521/ORCLPDB是斜杠！！！不是:冒号！！！
        String url = "jdbc:oracle:thin:@192.168.1.29:1521/AJDATA";
        String username = "ANKE";
        String psw = "123456";
        Connection conn = DriverManager.getConnection(url,username,psw);
 
        String sql = "select * from STRESS";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet resultSet = ps.executeQuery();
        while(resultSet.next()){
            System.out.println(resultSet.getObject(1));
            System.out.println(resultSet.getObject(2));
            System.out.println(resultSet.getObject(3));
        }
        resultSet.close();
        ps.close();
        conn.close();
	}
}
