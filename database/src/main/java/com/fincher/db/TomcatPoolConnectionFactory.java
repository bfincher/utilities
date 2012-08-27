package com.fincher.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class TomcatPoolConnectionFactory implements ConnectionFactoryIfc {
	
	private final DataSource dataSource;
	private final boolean autoCommit;
	
	public TomcatPoolConnectionFactory(String resourceName, boolean autoCommit) throws SQLException {
		this.autoCommit = autoCommit;
		try {
			Context ctx = new InitialContext();  
			Context env  = (Context)ctx.lookup("java:comp/env");

			dataSource = (DataSource)env.lookup(resourceName);
		} catch (NamingException e) {
			throw new SQLException(e);
		}
	}
	
	public Connection getConnection() throws SQLException {
		Connection conn = dataSource.getConnection();
		
		try {
			conn.setAutoCommit(autoCommit);
			conn.createStatement().execute("select 1");
		} catch (SQLException e) {
			conn = dataSource.getConnection();
			conn.setAutoCommit(autoCommit);
		}
		
		return conn;
	}

}
