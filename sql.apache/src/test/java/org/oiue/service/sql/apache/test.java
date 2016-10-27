package org.oiue.service.sql.apache;

import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

public class test {

	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUsername("root");
		ds.setPassword("admin");
		ds.setUrl("jdbc:mysql://192.168.1.5:3306/totalsystem");
		System.out.println(ds.getConnection().createStatement().execute("select 2"));

	}

}
