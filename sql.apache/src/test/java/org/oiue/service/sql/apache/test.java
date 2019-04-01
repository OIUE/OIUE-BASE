package org.oiue.service.sql.apache;

import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;

public class test {
	
	/**
	 * @param args
	 * @throws SQLException
	 */
	public static void main(String[] args) throws SQLException {
		BasicDataSource ds = new BasicDataSource();
		// ds.setDriverClassName("com.mysql.jdbc.Driver");
		// ds.setUsername("root");
		// ds.setPassword("admin");
		// ds.setUrl("jdbc:mysql://192.168.1.5:3306/totalsystem");
		ds.setDriverClassName("org.postgresql.Driver");
		ds.setUsername("postgres");
		ds.setPassword("123456");
		ds.setUrl("jdbc:postgresql://127.0.0.1:5432/ltmap?charset=utf-8");
		String sql = "update map_point set map_point_name='+++'||map_point_name returning *";
		Connection conn = ds.getConnection();
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			pstmt.executeUpdate();
			ResultSet generatedKeys = pstmt.getGeneratedKeys();
			while (generatedKeys.next()) {
				System.out.println(getMapResult(generatedKeys));
			}
		} finally {
			conn.close();
		}
		
	}
	
	public static Map getMapResult(ResultSet rs) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int sum = rsmd.getColumnCount();
		Hashtable row = new Hashtable();
		for (int i = 1; i < sum + 1; i++) {
			Object value = rs.getObject(i);
			if ((value instanceof BigDecimal)) {
				if (((BigDecimal) value).scale() == 0) {
					value = Long.valueOf(((BigDecimal) value).longValue());
				} else {
					value = Double.valueOf(((BigDecimal) value).doubleValue());
				}
			} else if ((value instanceof Clob)) {
				// value = clobToString((Clob)value);
			}
			String key = rsmd.getColumnLabel(i);
			row.put(key, value == null ? "" : value);
		}
		return row;
	}
}
