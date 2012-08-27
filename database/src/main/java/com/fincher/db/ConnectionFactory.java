package com.fincher.db;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class ConnectionFactory implements ConnectionFactoryIfc {	
	
	private final String dbUserName;
	private final String dbPassword;
	private final String rootUserName;
	private final String rootPassword;
	private final boolean autoCommit;
	private final String driverClassName;
	
	public ConnectionFactory(boolean autoCommit,
			String driverClassName) throws SQLException {
		this(null, null, null, null, autoCommit, driverClassName);	
		
		try {
			Class.forName(driverClassName).newInstance();
		} catch (Exception e) {
			throw new SQLException(e);
		}
	}
	
	public ConnectionFactory(String dbUserName, 
			String dbPassword, 
			String rootUserName, 
			String rootPassword,
			boolean autoCommit,
			String driverClassName) 
	throws SQLException {
		this.dbUserName = dbUserName;
		this.dbPassword = dbPassword;
		this.rootUserName = rootUserName;
		this.rootPassword = rootPassword;
		this.autoCommit = autoCommit;
		this.driverClassName = driverClassName;
		
		try {
			Class.forName(driverClassName).newInstance();
		} catch (Exception e) {
			throw new SQLException(e);
		}
	}	
	
	@Override
	public final Connection getConnection() throws SQLException {
		try {
			Class.forName(driverClassName);
		} catch (ClassNotFoundException cnfe) {
			throw new SQLException(cnfe);
		}
		Connection connection = connectToDB();
		connection.setAutoCommit(autoCommit);
		return connection;
	}		
	
	protected String getDbUserName() {
		return dbUserName;
	}

	protected String getDbPassword() {
		return dbPassword;
	}

	protected String getRootUserName() {
		return rootUserName;
	}

	protected String getRootPassword() {
		return rootPassword;
	}

	protected boolean isAutoCommit() {
		return autoCommit;
	}

	protected abstract Connection connectToDB() throws SQLException;	
}
