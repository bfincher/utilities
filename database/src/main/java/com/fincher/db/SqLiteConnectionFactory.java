package com.fincher.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class SqLiteConnectionFactory extends ConnectionFactory {
	
	private static final Logger LOGGER = Logger.getLogger(SqLiteConnectionFactory.class);
	public static final String BASE_DB_URL = "jdbc:sqlite";
	private static final String DRIVER_CLASS_NAME = "org.sqlite.JDBC";
	
	private final String fullDbURL;
	
	public SqLiteConnectionFactory(boolean autoCommit,
			String dbName) throws SQLException {
		super(autoCommit, DRIVER_CLASS_NAME);
		fullDbURL = BASE_DB_URL + ":" + dbName + ".db"; 		
	}		
	
	@Override
	protected Connection connectToDB() throws SQLException {										
		String url = fullDbURL;
		LOGGER.trace("DB URL = " + url);
		return DriverManager.getConnection(fullDbURL);
	}
}
