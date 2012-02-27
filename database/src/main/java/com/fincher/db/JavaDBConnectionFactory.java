package com.fincher.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class JavaDBConnectionFactory extends ConnectionFactory {
	
	private static final Logger LOGGER = Logger.getLogger(JavaDBConnectionFactory.class);
	private static final String BASE_DB_URL = "jdbc:derby";
	private static final String DRIVER_CLASS_NAME = "org.apache.derby.jdbc.EmbeddedDriver";
	
	private final String fullDbURL;
	
	public JavaDBConnectionFactory(boolean autoCommit, String dbName) throws SQLException {
		super(autoCommit, DRIVER_CLASS_NAME);
		fullDbURL = BASE_DB_URL + ":" + dbName + ";create=true"; 
	}
	
	@Override
	protected Connection connectToDB() throws SQLException {										
		String url = fullDbURL;
		LOGGER.trace("DB URL = " + url);
		return DriverManager.getConnection(fullDbURL);
	}

}
