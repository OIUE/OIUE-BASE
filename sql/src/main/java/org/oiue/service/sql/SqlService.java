package org.oiue.service.sql;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;

import javax.sql.DataSource;

public interface SqlService extends Serializable{
	@SuppressWarnings("rawtypes")
	public boolean registerDataSource(String alias, Dictionary params);

	public void unregister(String alias);
	public void unregisterAll();

	public String[] listDataSource();

	public SqlServiceResult select(String alias, String sql, List<Object> params);

	public SqlServiceResult selectMap(String alias, String sql, List<Object> params);

	public SqlServiceResult insertUpdateOrDelete(String alias, String sql, List<Object> params);

	public SqlServiceResult insertUpdateOrDeleteWithClob(String alias, String sql, List<Object> params, HashSet<Integer> clobIndexSet);

	public SqlServiceResult call(String alias, String sql, List<Object> params);

	public Connection getConnection(String alias);

	public DataSource getDataSource(String alias);
}
