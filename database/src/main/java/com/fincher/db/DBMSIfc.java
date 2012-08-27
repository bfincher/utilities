package com.fincher.db;

import java.sql.SQLException;

public interface DBMSIfc <T> {
	
//	public Savepoint createSavepoint() throws IOException;
	
//	public void releaseSavepoint(Savepoint savepoint) throws IOException;
	
//	public void rollback(Savepoint savepoint) throws IOException;
	
	public void rollback() throws SQLException;
	
	public void commit() throws SQLException;
	
	public void dropTable(boolean autoCommit) throws SQLException;
	
	public void createTable(boolean autoCommit) throws SQLException;	
	
//	public T insert(T t, boolean autoCommit) 
//	throws SQLException;
	
	public void closeConnection() throws SQLException;

}
