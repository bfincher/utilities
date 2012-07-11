package com.fincher.db;

import java.sql.SQLException;

public interface DBMSWithInsertIfc <T> extends DBMSIfc<T> {
	
	public T insert(T t, boolean autoCommit) throws SQLException;

}
