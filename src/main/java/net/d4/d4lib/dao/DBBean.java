package net.d4.d4lib.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DBBean {
	
	Connection conn;
	PreparedStatement prest;
	ResultSet rs;
	
	public Connection getConn() {
		return conn;
	}
	public void setConn(Connection conn) {
		this.conn = conn;
	}
	public PreparedStatement getPrest() {
		return prest;
	}
	public void setPrest(PreparedStatement prest) {
		this.prest = prest;
	}
	public ResultSet getRs() {
		return rs;
	}
	public void setRs(ResultSet rs) {
		this.rs = rs;
	}
	
}
