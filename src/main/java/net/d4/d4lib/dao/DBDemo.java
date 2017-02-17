package net.d4.d4lib.dao;

import java.sql.ResultSet;

public class DBDemo {
	public static void main(String[] args) {
		DBDemo demo = new DBDemo();
		demo.select();
		demo.saveOrUpdate();
		demo.delete();
		demo.insert();
	}
	public synchronized void select() {
		try {
			String sql = "select id,name from user";

			DBBean dbbean = JDBCUtils.query(sql);
			ResultSet rSet = dbbean.getRs();
			while (rSet.next()) {
				int id = rSet.getInt("id");
				String name = rSet.getString("name");
				System.out.printf("id %d,name %s", id ,name);
			}
			JDBCUtils.closeDB(dbbean);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public boolean saveOrUpdate() {
		String updatasql = "UPDATE user SET name=? WHERE ID=?";

		try {
			JDBCUtils.saveOrUpdate(updatasql, "test1", 1);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public boolean delete() {
		String updatasql = "delete from user where ID=?";

		try {
			JDBCUtils.saveOrUpdate(updatasql, 3);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public boolean insert() {
		String updatasql = "insert into user(id,name,age,sex,isDeleted) values(3,?,0,1,0)";

		try {
			JDBCUtils.saveOrUpdate(updatasql, "name_insert");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
