package com.fincher.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;


public abstract class AbstractDBMS <T> implements DBMSIfc<T> {

	private static final Logger LOGGER = Logger.getLogger(AbstractDBMS.class); 
	
	protected static final String TABLE_NAME_MASK = "!!TABLE_NAME!!";

	private final Connection connection;
	private final String tableName;
	private final String[] createTableSQL;
	private final String[] dropTableSQL;

	protected AbstractDBMS(Connection connection,
			String tableName,
			String[] createTableSQL,
			String[] dropTableSQL) throws SQLException {					

		this.connection = connection;
		
		this.tableName = tableName;
		
		this.createTableSQL = new String[createTableSQL.length];
		for (int i = 0; i < createTableSQL.length; i++) {
			this.createTableSQL[i] = createTableSQL[i].replace(TABLE_NAME_MASK, tableName);
		}
		
		this.dropTableSQL = new String[dropTableSQL.length];
		for (int i = 0; i < dropTableSQL.length; i++) {
			this.dropTableSQL[i] = dropTableSQL[i].replace(TABLE_NAME_MASK, tableName);
		}
		
		if (!doesTableExist()) {
			createTable(true);
		}
	}		
	
	protected final Connection getConnection() throws SQLException {				
		return connection;
	}		

	@Override
	public void rollback() throws SQLException {
		if (savepointsSupported()) {
			LOGGER.trace("Savepoint rolled back");
			getConnection().rollback();			
		}	
	}
	
	@Override
	public void commit() throws SQLException {
		LOGGER.trace("commit");
		getConnection().commit();						
	}	
	
	public static void dropDatabase(String mysqlUrl, String dbName,
			String rootUserName, String rootPassword) throws SQLException {
		
		Connection connection;
		if (rootUserName == null) {
			connection = DriverManager.getConnection(mysqlUrl);
		} else {			
			connection = DriverManager.getConnection(mysqlUrl,
				rootUserName,
				rootPassword);
		}
			
		Statement stmt = connection.createStatement();
		String sql = "drop database " + dbName;
		LOGGER.trace(sql);
		stmt.execute(sql);		
	}

	public static byte booleanToInt(boolean val) {
		if (val) {
			return (byte)1;
		} else {
			return (byte)0;
		}
	}

	public static boolean intToBoolean(byte val) {
		if (val == 0) {
			return false;
		} else {
			return true;
		}
	}

	protected final List<T> executeQuery(PreparedStatement ps) throws SQLException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Executing query: " + ps.toString());
		}
		
		ResultSet rs = null;
		try {
			rs = ps.executeQuery();
			LinkedList<T> list = new LinkedList<T>();
		
			while (rs.next()) {
				list.add(extract(rs));
			}
			return list;
		} finally {		
			if (rs != null) {
				rs.close();
			}
		}					
	}
	
	protected final T executeQuerySingleReturn(PreparedStatement ps) throws SQLException {
		List<T> list = executeQuery(ps);
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

	protected final int executeUpdate(PreparedStatement ps) throws SQLException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Executing update: " + ps.toString());
		}
		return ps.executeUpdate();	
	}

	protected final boolean execute(PreparedStatement ps) throws SQLException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Executing statement: " + ps.toString());
		}
		return ps.execute();		
	}

	public final int executeUpdate(String sql) throws SQLException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Executing update: " + sql);
		}

		Statement statement = getConnection().createStatement();
		return statement.executeUpdate(sql);
	}

	public final PreparedStatement prepareStatement (String sql) throws SQLException {		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Preparing statement: " + sql);
		}

		return getConnection().prepareStatement(sql);
	}		
	
	@Override
	public void closeConnection() throws SQLException {
		getConnection().close();				
	}
	
	@Override
	public void createTable(boolean autoCommit) throws SQLException {
		LOGGER.trace("creating table: ");
		for (String sql: createTableSQL) {
			LOGGER.trace(sql);
			executeUpdate(sql);
		}
		
		if (autoCommit) {
			commit();
		}
	}
	
	@Override
	public void dropTable(boolean autoCommit) throws SQLException {
		LOGGER.trace("dropping table: ");
		for (String sql: dropTableSQL) {
			LOGGER.trace(sql);
			executeUpdate(sql);
		}
		
		if (autoCommit) {
			commit();
		}
	}	
		
	public abstract boolean savepointsSupported();

	public final String getTableName() {
		return tableName;
	}
	
	protected abstract T extract(ResultSet rs) throws SQLException;	
	
	public abstract boolean doesTableExist() throws SQLException;

}
