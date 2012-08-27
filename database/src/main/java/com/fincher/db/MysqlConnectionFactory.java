package com.fincher.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class MysqlConnectionFactory extends ConnectionFactory {
	
	private static final Logger LOGGER = Logger.getLogger(MysqlConnectionFactory.class);
	public static final String DEFAULT_BASE_DB_URL = "jdbc:mysql://127.0.0.1";
	
	private final String baseDBUrl;
	private final String dbName;
	private final String fullDbURL;
	
	private static PreparedStatement tableExistsPS;
	
	public MysqlConnectionFactory(boolean autoCommit,
			String baseDbUrl,
			String dbName) throws SQLException {
		this(null, null, null, null, autoCommit, baseDbUrl, dbName);		
	}
	
	public MysqlConnectionFactory(String dbUserName, 
			String dbPassword, 
			String rootUserName, 
			String rootPassword,
			boolean autoCommit,
			String baseDbUrl,
			String dbName) 
	throws SQLException {
		super(dbUserName, dbPassword, rootUserName, rootPassword, autoCommit, "com.mysql.jdbc.Driver");
		this.baseDBUrl = baseDbUrl;
		this.dbName = dbName;
		fullDbURL = baseDBUrl + "/" + dbName;
	}	
	
	protected Connection connectToDB() throws SQLException {		
		Connection connection;
		
		try {						
			String url = fullDbURL;
			LOGGER.trace("DB URL = " + url);
			connection = connectToDB(url, 
					getDbUserName(),
					getDbPassword());
					
		} catch (SQLException e) {
			connection = connectToDB(baseDBUrl,
					getRootUserName(),
					getRootPassword());
			
			LOGGER.warn("Trying to create database due to exception: ", e);
			Statement stmt = connection.createStatement();
			
			String sql = "drop database if exists " + dbName;
			LOGGER.info(sql);
			stmt.execute(sql);
			
			sql = "create database " + dbName;
			LOGGER.info(sql);
			stmt.execute(sql);
			
			if (getDbUserName() != null) {
				sql = "select user from mysql.user where user = '" + getDbUserName() + "'";
				LOGGER.info(sql);
				if (stmt.executeQuery(sql).next()) {
					sql = "drop user " + getDbUserName();
					LOGGER.info(sql);
					stmt.execute(sql);
				}

				sql = "create user " + getDbUserName() + " identified by '" + getDbPassword() + "'";
				LOGGER.info(sql);
				stmt.execute(sql);
				sql = "grant all privileges on " + dbName + ".* to " + getDbUserName();
				LOGGER.info(sql);
				stmt.execute(sql);
				
			}
			connection.close();
			
			connection = connectToDB(fullDbURL, 
					getDbUserName(),
					getDbPassword());
						
		}
		
		return connection;
	}
	
	private Connection connectToDB(String dbUrl, String userName, String password) throws SQLException {		
		if (userName == null) {
			LOGGER.trace("Creating connection without username/password");
			return DriverManager.getConnection(dbUrl);
		} else {
			LOGGER.trace("Creating connection with username/password");
			return DriverManager.getConnection(dbUrl,
					userName, password);
		}		
	}
	
	public static boolean doesTableExist(Connection connection, String dbName, String tableName) throws SQLException {
		if (tableExistsPS == null) {
			tableExistsPS = connection.prepareStatement("select * from information_schema.tables where  table_schema = ? AND table_name = ?");
		}
		
		synchronized (tableExistsPS) {
			tableExistsPS.setString(1, dbName);
			tableExistsPS.setString(2, tableName);
			
			ResultSet rs = null;
			try {
				rs = tableExistsPS.executeQuery();
				return rs.next();
			} finally {
				if (rs != null) {
					rs.close();
				}
			}
		}
	}
		
}
