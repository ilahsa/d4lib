package net.d4.d4lib.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDBCUtils {

	private static final Logger log = LoggerFactory.getLogger(JDBCUtils.class);

	public static boolean insertPre(String sql, String... params) {
		Connection conn = connect();
		boolean result = false;
		PreparedStatement prest = null;
		try {
			prest = conn.prepareStatement(sql);
			int i = 1;
			for (String param : params) {
				prest.setString(i, param);
				i++;
			}
			int count = prest.executeUpdate();
			if (count > 0) {
				result = true;
			}
			// log.info("insert data success ! count : " + count);
		} catch (SQLException e) {
			log.error("SQLException:" + e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			log.error("Exception:" + e.toString());
			e.printStackTrace();
		} finally {
			close(prest);
			close(conn);
		}
		return result;
	}

	public static boolean insertPre(String sql, Object... params) {
		Connection conn = connect();
		boolean result = false;
		PreparedStatement prest = null;
		try {
			prest = conn.prepareStatement(sql);
			int i = 1;
			for (Object param : params) {
				prest.setObject(i, param);
				i++;
			}
			int count = prest.executeUpdate();
			if (count > 0) {
				result = true;
			}
			// log.info("insert data success ! count : " + count);
		} catch (SQLException e) {
			log.error("SQLException:" + e.toString());
		} catch (Exception e) {
			log.error("Exception:" + e.toString());
			e.printStackTrace();
		} finally {
			close(prest);
			close(conn);
		}
		return result;
	}

	/* 插入数据记录，并输出插入的数据记录数 */
	public static boolean insert(String sql) {
		Connection conn = connect();
		boolean result = false;
		Statement st = null;
		try {
			st = conn.createStatement();
			int count = st.executeUpdate(sql);
			if (count > 0) {
				result = true;
			}
			// log.info("insert data success ! count : " + count);
		} catch (SQLException e) {
			log.error("SQLException:" + e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			log.error("Exception:" + e.toString());
			e.printStackTrace();
		} finally {
			close(st);
			close(conn);
		}
		return result;
	}

	/* 删除符合要求的记录，输出情况 */
	public static boolean delete(String sql) {
		Connection conn = connect();
		boolean result = false;
		Statement st = null;
		try {
			st = conn.createStatement();
			int count = st.executeUpdate(sql);
			if (count > 0) {
				result = true;
			}
			// log.info("delete data success ! count : " + count);
		} catch (SQLException e) {
			log.error("SQLException:" + e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			log.error("Exception:" + e.toString());
			e.printStackTrace();
		} finally {
			close(st);
			close(conn);
		}
		return result;
	}

	public static boolean update(String sql) {
		Connection conn = connect();
		boolean result = false;
		Statement st = null;
		try {
			st = conn.createStatement();
			int count = st.executeUpdate(sql);
			// log.info("update sql : " + sql);
			if (count > 0) {
				result = true;
				// log.info("update data success ! count : " + count);
			}
		} catch (SQLException e) {
			log.error("SQLException:" + e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			log.error("Exception:" + e.toString());
			e.printStackTrace();
		} finally {
			close(st);
			close(conn);
		}
		return result;
	}

	public static int queryNum(String sql) {
		int result = 0;
		Connection conn = connect();
		Statement st = null;
		ResultSet rs = null;
		try {
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				result = rs.getInt(1);
			}
		} catch (SQLException e) {
			log.error("SQLException:" + e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			log.error("Exception:" + e.toString());
			e.printStackTrace();
		} finally {
			close(rs);
			close(st);
			close(conn);
		}

		return result;
	}

	public static int queryNumPre(String sql, String... params) {
		int result = 0;
		Connection conn = connect();
		PreparedStatement prest = null;
		ResultSet rs = null;
		try {
			prest = conn.prepareStatement(sql);
			int i = 1;
			for (String param : params) {
				prest.setString(i, param);
				i++;
			}
			rs = prest.executeQuery();
			if (rs.next()) {
				result = rs.getInt(1);
			}
		} catch (SQLException e) {
			log.error("SQLException:" + e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			log.error("Exception:" + e.toString());
			e.printStackTrace();
		} finally {
			close(rs);
			close(prest);
			close(conn);
		}

		return result;
	}

	public static DBBean query(String sql, String... params) {
		Connection conn = connect();
		PreparedStatement prest = null;
		ResultSet rs = null;
		try {
			prest = conn.prepareStatement(sql);
			int i = 1;
			for (String param : params) {
				prest.setString(i, param);
				i++;
			}
			DBBean bean = new DBBean();
			rs = prest.executeQuery();
			bean.setConn(conn);
			bean.setPrest(prest);
			bean.setRs(rs);
			return bean;
		} catch (SQLException e) {
			log.error("SQLException:" + e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			log.error("Exception:" + e.toString());
			e.printStackTrace();
		} finally {
			// close(prest);
			// close(conn);
		}

		return null;
	}

	public static void closeDB(DBBean bean) {
		close(bean.getConn());
		close(bean.getPrest());
		close(bean.getRs());
	}

	public static int saveOrUpdate(String sql, Object... params) {
		Connection conn = connect();
		PreparedStatement prest = null;
		try {
			prest = conn.prepareStatement(sql);
			int i = 1;
			for (Object param : params) {
				prest.setObject(i, param);
				i++;
			}
			return prest.executeUpdate();
		} catch (SQLException e) {
			log.error("SQLException:" + e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			log.error("Exception:" + e.toString());
			e.printStackTrace();
		} finally {
			close(prest);
			close(conn);
		}

		return 0;
	}

	public static boolean execute(String sql) {
		boolean result = true;
		Connection conn = connect();
		Statement st = null;
		try {
			st = conn.createStatement();
			st.execute(sql);
		} catch (SQLException e) {
			log.error(e.toString());
			result = false;
		} catch (Exception e) {
			log.error(e.toString());
			result = false;
		} finally {
			close(st);
			close(conn);
		}

		return result;
	}

	public static Connection connect() {
		return ConnectionManager.getInstance().getConnection();
	}

	public static Statement statement(Connection conn) throws SQLException {
		return conn.createStatement();
	}

	public static PreparedStatement preparedStatement(Connection connection, String sql, List<Object> params) throws SQLException {
		PreparedStatement ps = connection.prepareStatement(sql);
		if (!isEmpty(params)) {
			int i = 1;
			for (Object obj : params) {
				ps.setObject(i, obj);
				i++;
			}
		}
		return ps;
	}

	public static ResultSet executeQuery(String sql, Statement st) throws SQLException {
		ResultSet rs = st.executeQuery(sql);
		return rs;
	}

	public static void close(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				log.error(e.toString());
			}
		}
	}

	public static void close(Statement st) {
		if (st != null) {
			try {
				st.close();
			} catch (SQLException e) {
				log.error(e.toString());
			}
		}
	}

	/**
	 * 游标方式执行查询
	 * 
	 * @param sql
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static PreparedStatement getLargeQueryPreparedStatement(Connection con, String sql, List<Object> params) throws SQLException {
		PreparedStatement ps = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ps.setFetchSize(Integer.MIN_VALUE);
		ps.setFetchDirection(ResultSet.FETCH_FORWARD);
		if (!isEmpty(params)) {
			int i = 1;
			for (Object param : params) {
				ps.setObject(i, param);
				i++;
			}
		}
		return ps;
	}

	public static void colse(PreparedStatement ps) {
		if (ps != null) {
			try {
				ps.close();
			} catch (SQLException e) {
				log.error(e.toString());
			}
		}
	}

	public static int executeUpdate(Connection con, String sql, List<Object> params) {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			if (!isEmpty(params)) {
				int i = 1;
				for (Object param : params) {
					ps.setObject(i, param);
					i++;
				}
			}
			return ps.executeUpdate();
		} catch (SQLException e) {
			log.error(e.toString());
		} catch (Exception e) {
			log.error(e.toString());
		} finally {
			close(ps);
			close(con);
		}
		return -1;
	}

	public static void executeUpdateMulti(Connection con, String sql, List<Object> params) throws Exception {
		PreparedStatement ps = con.prepareStatement(sql);
		if (!isEmpty(params)) {
			int i = 1;
			for (Object param : params) {
				ps.setObject(i, param);
				i++;
			}
		}
		ps.executeUpdate();
		ps.close();
	}

	public static void close(Connection connection) {
		ConnectionManager.getInstance().close(connection);
	}

	public static boolean isEmpty(Collection<?> collection) {
		if (collection == null || collection.size() == 0) {
			return true;
		} else {
			return false;
		}
	}
}
