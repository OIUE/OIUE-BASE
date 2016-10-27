package org.oiue.service.sql.apache;

import java.io.Reader;
import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.sql.SqlService;
import org.oiue.service.sql.SqlServiceResult;

@SuppressWarnings("serial")
public class SqlServiceImpl implements SqlService {
	private Logger logger;
	private Hashtable<String, BasicDataSource> dataSources = new Hashtable<String, BasicDataSource>();

	public SqlServiceImpl(LogService logService) {
		logger = logService.getLogger(this.getClass());
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean registerDataSource(String alias, Dictionary params) {
		if (logger.isInfoEnabled()) {
			logger.info("register data source, alias = " + alias);
		}
		if (dataSources.contains(alias)) {
			if (logger.isInfoEnabled()) {
				logger.error("data source alias is exits, alais = " + alias);
			}
			return false;
		}
		BasicDataSource ds = new BasicDataSource();
		try {
			ds.setDriverClassName(params.get("driverClassName").toString());
			ds.setUsername(params.get("username").toString());
			ds.setPassword(params.get("password").toString());
			ds.setUrl(params.get("url").toString());
			ds.setMaxActive(Integer.valueOf(params.get("maxActive").toString()));
			ds.setMaxIdle(Integer.valueOf(params.get("maxIdle").toString()));
			ds.setMaxWait(Integer.valueOf(params.get("maxWait").toString()));
			ds.setValidationQuery(params.get("validationQuery").toString());

			if (logger.isInfoEnabled()) {
				logger.debug("data source params = " + ds);
			}
		} catch (Exception ex) {
			logger.error("register data source error", ex);
			return false;
		}
		dataSources.put(alias, ds);
		if (logger.isInfoEnabled()) {
			logger.info("register data source successed, alias = " + alias);
		}
		return true;
	}

	@Override
	public void unregister(String alias) {
		if (logger.isInfoEnabled()) {
			logger.info("unregister data source, alias = " + alias);
		}
		if (dataSources.contains(alias)) {
			try {
				dataSources.get(alias).close();
			} catch (SQLException e) {
				logger.error("data source close error", e);
			}
			dataSources.remove(alias);
		}
	}

    @SuppressWarnings("rawtypes")
    @Override
    public void unregisterAll() {
        if (logger.isInfoEnabled()) {
            logger.info("unregister all data source ");
        }
        for (Iterator iterator = dataSources.values().iterator(); iterator.hasNext();) {
            BasicDataSource ds = (BasicDataSource) iterator.next();
            try {
                ds.close();
            } catch (SQLException e) {
                logger.error("data source close error", e);
            }
            iterator.remove();
        }
    }
	@Override
	public String[] listDataSource() {
		return dataSources.keySet().toArray(new String[0]);
	}

	@Override
	public SqlServiceResult insertUpdateOrDelete(String alias, String sql, List<Object> params) {
		if (logger.isDebugEnabled()) {
			logger.debug("insert update or delete, alias = " + alias + ", sql = " + sql + ", params = " + params);
		}
		SqlServiceResult result = new SqlServiceResult();
		DataSource dataSource = dataSources.get(alias);
		if (dataSource == null) {
			logger.error("can't found alias, alias = " + alias);
			result.setResult(false);
			result.setData("can't found data source");
			return result;
		}

		Connection conn = null;
		ResultSet rset = null;
		PreparedStatement pstmt = null;
		try {
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql);
			if ((params != null) && (params.size() > 0)) {
				for (int i = 0; i < params.size(); i++) {
					Object param = params.get(i);
					if (param instanceof Date) {
						pstmt.setObject(i + 1, new java.sql.Date(((Date) param).getTime()));
					} else {
						pstmt.setObject(i + 1, param);
					}
				}
			}
			result.setResult(true);
			result.setData(pstmt.executeUpdate());
			if (logger.isDebugEnabled()) {
				logger.debug("execute update successed, data = " + result.getData());
			}
			return result;
		} catch (SQLException e) {
			logger.error("execute update sql error", e);
			result.setResult(false);
			result.setData(e.getMessage());
			return result;
		} catch (Exception ex) {
			logger.error("execute update error", ex);
			result.setResult(false);
			result.setData(ex.getMessage());
			return result;
		} finally {
			if (rset != null) {
				try {
					rset.close();
				} catch (Exception e) {
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
		}
	}

	@Override
	public SqlServiceResult insertUpdateOrDeleteWithClob(String alias, String sql, List<Object> params, HashSet<Integer> clobIndexSet) {
		if (logger.isDebugEnabled()) {
			logger.debug("insert update or delete, alias = " + alias + ", sql = " + sql + ", params = " + params);
		}
		SqlServiceResult result = new SqlServiceResult();
		DataSource dataSource = dataSources.get(alias);
		if (dataSource == null) {
			logger.error("can't found alias, alias = " + alias);
			result.setResult(false);
			result.setData("can't found data source");
			return result;
		}

		Connection conn = null;
		ResultSet rset = null;
		PreparedStatement pstmt = null;
		try {
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql);
			if ((params != null) && (params.size() > 0)) {
				for (int i = 0; i < params.size(); i++) {
					Object param = params.get(i);
					if (clobIndexSet.contains(i) && (param != null)) {
						String clobConent = param.toString();
						Reader clobReader = new StringReader(clobConent);
						pstmt.setCharacterStream(i + 1, clobReader, clobConent.length());
					} else {
						pstmt.setObject(i + 1, param);
					}
				}
			}
			result.setResult(true);
			result.setData(pstmt.executeUpdate());
			if (logger.isDebugEnabled()) {
				logger.debug("execute update successed, data = " + result.getData());
			}
			return result;
		} catch (SQLException e) {
			logger.error("execute update sql error", e);
			result.setResult(false);
			result.setData(e.getMessage());
			return result;
		} catch (Exception ex) {
			logger.error("execute update error", ex);
			result.setResult(false);
			result.setData(ex.getMessage());
			return result;
		} finally {
			if (rset != null) {
				try {
					rset.close();
				} catch (Exception e) {
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
		}
	}

	@Override
	public SqlServiceResult select(String alias, String sql, List<Object> params) {
		if (logger.isDebugEnabled()) {
			logger.debug("select, alias = " + alias + ", sql = " + sql + ", params = " + params);
		}
		SqlServiceResult result = new SqlServiceResult();
		DataSource dataSource = dataSources.get(alias);
		if (dataSource == null) {
			logger.error("can't found alias, alias = " + alias);
			result.setResult(false);
			result.setData("can't found data source");
			return result;
		}
		Connection conn = null;
		ResultSet rset = null;
		PreparedStatement pstmt = null;
		try {
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql);
			if ((params != null) && (params.size() > 0)) {
				for (int i = 0; i < params.size(); i++) {
					Object param = params.get(i);
					if (param instanceof Date) {
						pstmt.setObject(i + 1, new java.sql.Date(((Date) param).getTime()));
					} else {
						pstmt.setObject(i + 1, param);
					}
				}
			}
			rset = pstmt.executeQuery();
			int numcols = rset.getMetaData().getColumnCount();
			List<List<Object>> arrayRecords = new ArrayList<List<Object>>();
			while (rset.next()) {
				List<Object> arrayRecord = new ArrayList<Object>();
				for (int i = 1; i <= numcols; i++) {
					Object obj = rset.getObject(i);
					if (obj instanceof Clob) {
						Clob clob = (Clob) obj;
						Reader reader = clob.getCharacterStream();
						if (reader == null) {
							return null;
						}
						StringBuffer sb = new StringBuffer();
						char[] charbuf = new char[4096];
						for (int size = reader.read(charbuf); size > 0; size = reader.read(charbuf)) {
							sb.append(charbuf, 0, size);
						}
						arrayRecord.add(sb.toString());
					} else if (obj instanceof java.math.BigDecimal) {
						arrayRecord.add(Long.valueOf(obj+""));
					} else {
						arrayRecord.add(obj);
					}
				}
				arrayRecords.add(arrayRecord);
			}
			result.setResult(true);
			result.setData(arrayRecords);
			if (logger.isDebugEnabled()) {
				logger.debug("execute select successed, data count = " + arrayRecords.size());
			}
			return result;
		} catch (SQLException e) {
			logger.error("execute select sql error", e);
			result.setResult(false);
			result.setData(e.getMessage());
			return result;
		} catch (Exception ex) {
			logger.error("execute select error", ex);
			result.setResult(false);
			result.setData(ex.getMessage());
			return result;
		} finally {
			if (rset != null) {
				try {
					rset.close();
				} catch (Exception e) {
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
		}
	}

	@Override
	public SqlServiceResult selectMap(String alias, String sql, List<Object> params) {
		if (logger.isDebugEnabled()) {
			logger.debug("select json, alias = " + alias + ", sql = " + sql + ", params = " + params);
		}
		SqlServiceResult result = new SqlServiceResult();
		DataSource dataSource = dataSources.get(alias);
		if (dataSource == null) {
			logger.error("can't found alias, alias = " + alias);
			result.setResult(false);
			result.setData("can't found data source");
			return result;
		}
		Connection conn = null;
		ResultSet rset = null;
		PreparedStatement pstmt = null;
		try {
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql);
			if ((params != null) && (params.size() > 0)) {
				for (int i = 0; i < params.size(); i++) {
					Object param = params.get(i);
					if (param instanceof Date) {
						pstmt.setObject(i + 1, new java.sql.Date(((Date) param).getTime()));
					} else {
						pstmt.setObject(i + 1, param);
					}
				}
			}
			rset = pstmt.executeQuery();
			ResultSetMetaData metaData = rset.getMetaData();

			int numcols = metaData.getColumnCount();
			String[] columnLabels = new String[numcols];
			for (int i = 0; i < numcols; i++) {
				columnLabels[i] = metaData.getColumnLabel(i + 1).toLowerCase();
			}

			List<Map<String, Object>> arrayRecords = new ArrayList<Map<String, Object>>();
			while (rset.next()) {
				Map<String, Object> record = new HashMap<String, Object>();
				for (int i = 1; i <= numcols; i++) {
					Object obj = rset.getObject(i);
					if (obj instanceof Clob) {
						Clob clob = (Clob) obj;
						Reader reader = clob.getCharacterStream();
						if (reader == null) {
							return null;
						}
						StringBuffer sb = new StringBuffer();
						char[] charbuf = new char[4096];
						for (int size = reader.read(charbuf); size > 0; size = reader.read(charbuf)) {
							sb.append(charbuf, 0, size);
						}
						record.put(columnLabels[i - 1], sb.toString());
					} else if (obj instanceof java.math.BigDecimal) {
						try {
							record.put(columnLabels[i - 1], Long.valueOf(obj+""));
						} catch (Throwable e) {
							logger.warn(e.getMessage(),e);
							try {
								record.put(columnLabels[i - 1], Double.valueOf(obj+""));
							} catch (Throwable e2) {
								logger.error(e.getMessage()+e2.getMessage(),e2);
							}
						}
					} else {
						record.put(columnLabels[i - 1].toLowerCase(), obj);
					}
				}
				arrayRecords.add(record);
			}
			result.setResult(true);
			result.setData(arrayRecords);
			if (logger.isDebugEnabled()) {
				logger.debug("execute select map successed, data count = " + arrayRecords.size());
			}
			return result;
		} catch (SQLException e) {
			logger.error("execute select sql error,sql="+sql+",params="+params, e);
			result.setResult(false);
			result.setData(e.getMessage());
			return result;
		} catch (Exception ex) {
			logger.error("execute select map error,sql="+sql+",params="+params, ex);
			result.setResult(false);
			result.setData(ex.getMessage());
			return result;
		} finally {
			if (rset != null) {
				try {
					rset.close();
				} catch (Exception e) {
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
		}
	}

	@Override
	public SqlServiceResult call(String alias, String sql, List<Object> params) {
		if (logger.isDebugEnabled()) {
			logger.debug("call, alias = " + alias + ", sql = " + sql + ", params = " + params);
		}
		SqlServiceResult result = new SqlServiceResult();
		DataSource dataSource = dataSources.get(alias);
		if (dataSource == null) {
			logger.error("can't found alias, alias = " + alias);
			result.setResult(false);
			result.setData("can't found data source");
			return result;
		}
		Connection conn = null;
		ResultSet rset = null;
		CallableStatement cstmt = null;
		String flag = "";
		try {
			conn = dataSource.getConnection();
			if (sql.endsWith("V") || sql.endsWith("v")) {
				flag = "V";
				sql = sql.substring(0, sql.length() - 1);
			} else if (sql.endsWith("R") || sql.endsWith("r")) {
				flag = "R";
				sql = sql.substring(0, sql.length() - 1);
			}
			cstmt = conn.prepareCall(sql);

			int paramCount = cstmt.getParameterMetaData().getParameterCount();
			if (flag.equalsIgnoreCase("V")) {
				if (logger.isDebugEnabled()) {
					logger.debug("call return one string value");
				}
				cstmt.registerOutParameter(paramCount, java.sql.Types.VARCHAR);
			} else if (flag.equalsIgnoreCase("R")) {
				if (logger.isDebugEnabled()) {
					logger.debug("call return one record value");
				}
//				cstmt.registerOutParameter(paramCount, oracle.jdbc.OracleTypes.CURSOR);
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("call return nothing");
				}
			}

			if ((params != null) && (params.size() > 0)) {
				for (int i = 0; i < params.size(); i++) {
					Object param = params.get(i);
					if (param instanceof Date) {
						cstmt.setObject(i + 1, new java.sql.Date(((Date) param).getTime()));
					} else {
						cstmt.setObject(i + 1, param);
					}
				}
			}

			cstmt.execute();

			if (flag.equalsIgnoreCase("V")) {
				List<Object> arrayRecord = new ArrayList<Object>();
				arrayRecord.add(cstmt.getObject(paramCount));
				result.setResult(true);
				result.setData(arrayRecord);
				if (logger.isDebugEnabled()) {
					logger.debug("call return result = true, data = " + arrayRecord);
				}
			} else if (flag.equalsIgnoreCase("R")) {
				rset = (ResultSet) cstmt.getObject(paramCount);

				ResultSetMetaData metaData = rset.getMetaData();
				int numcols = metaData.getColumnCount();

				List<List<Object>> arrayRecords = new ArrayList<List<Object>>();
				while (rset.next()) {
					List<Object> arrayRecord = new ArrayList<Object>();
					for (int i = 1; i <= numcols; i++) {
						Object obj = rset.getObject(i);
						if (obj instanceof Clob) {
							Clob clob = (Clob) obj;
							Reader reader = clob.getCharacterStream();
							if (reader == null) {
								return null;
							}
							StringBuffer sb = new StringBuffer();
							char[] charbuf = new char[4096];
							for (int size = reader.read(charbuf); size > 0; size = reader.read(charbuf)) {
								sb.append(charbuf, 0, size);
							}
							arrayRecord.add(sb.toString());
						} else if (obj instanceof java.math.BigDecimal) {
							arrayRecord.add(Long.valueOf(obj+""));
						} else {
							arrayRecord.add(obj);
						}
					}
					arrayRecords.add(arrayRecord);
				}
				result.setResult(true);
				result.setData(arrayRecords);
				if (logger.isDebugEnabled()) {
					logger.debug("call return result = true, data count = " + arrayRecords.size());
				}
			} else {
				result.setResult(true);
				result.setData("ok");
				if (logger.isDebugEnabled()) {
					logger.debug("call return result = true, data = ok");
				}
			}
			return result;
		} catch (Exception e) {
			logger.error("execute call error", e);
			result.setResult(false);
			result.setData(e.getMessage());
			return result;
		} finally {
			if (rset != null) {
				try {
					rset.close();
				} catch (Exception e) {
				}
			}
			if (cstmt != null) {
				try {
					cstmt.close();
				} catch (Exception e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
		}
	}

	@Override
	public Connection getConnection(String alias) {
		if (logger.isDebugEnabled()) {
			logger.debug("get connect, alias = " + alias);
		}
		DataSource dataSource = dataSources.get(alias);
		if (dataSource == null) {
			throw new RuntimeException("get connect alias is not exits, alias = " + alias);
		} else {
			try {
				Connection conn = dataSource.getConnection();
				if (logger.isDebugEnabled()) {
					logger.debug("get connect successed");
				}
				return conn;
			} catch (SQLException e) {
				throw new RuntimeException("get connect alias error，alias="+alias+","+e.getMessage(), e);
			}
		}
	}

	@Override
	public DataSource getDataSource(String alias) {
		return dataSources.get(alias);
	}
}
