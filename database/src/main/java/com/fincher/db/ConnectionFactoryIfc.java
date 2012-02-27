package com.fincher.db;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionFactoryIfc {
	
	public Connection getConnection() throws SQLException;

}
