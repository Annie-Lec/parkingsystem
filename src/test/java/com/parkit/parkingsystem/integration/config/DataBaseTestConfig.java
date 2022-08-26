package com.parkit.parkingsystem.integration.config;

import com.parkit.parkingsystem.config.DataBaseConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class DataBaseTestConfig extends DataBaseConfig {

	private static final Logger logger = LogManager.getLogger("DataBaseTestConfig");

	public Connection getConnection() throws ClassNotFoundException, SQLException {
		logger.info("Create DB connection in Test");
		Class.forName("com.mysql.cj.jdbc.Driver");
		return DriverManager.getConnection("jdbc:mysql://localhost:3306/test?useUnicode=true &useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false", "root", "rootroot");
//		return DriverManager.getConnection(
//				"jdbc:mysql://localhost:3306/test?useUnicode=true\r\n"
//						+ "&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&\r\n" + "serverTimezone=UTC",
//				"root", "rootroot");
	}

	public void closeConnection(Connection con) {
		if (con != null) {
			try {
				con.close();
				logger.info("Closing DB connection in Test");
			} catch (SQLException e) {
				logger.error("Error while closing connection in Test", e);
			}
		}
	}

	public void closePreparedStatement(PreparedStatement ps) {
		if (ps != null) {
			try {
				ps.close();
				logger.info("Closing Prepared Statement in Test");
			} catch (SQLException e) {
				logger.error("Error while closing prepared statement in Test", e);
			}
		}
	}

	public void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
				logger.info("Closing Result Set in Test");
			} catch (SQLException e) {
				logger.error("Error while closing result set in Test", e);
			}
		}
	}
}
