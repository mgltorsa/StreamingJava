import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 
 */

/**
 * @author Miguel
 *
 */
public final class BDFactory {

	private static String ip = "200.3.193.24"; //200.3.193.24 //172.16.0.103
	private static int port = 1522;
	private static String instance = "ESTUD";
	private static String user = "P09551_1_23"; // EL MIO P09551_1_23 // EL DE ALEJO P09551_1_10
	private static String password = "P09551_1_23";


	private static Connection conection = null;
	private static boolean dataChanged = false;

	public static void setDefaultConfiguration() {
		ip = "200.3.193.24";
		port = 1522;
		instance = "ESTUD";
		user = "P09551_1_10";
		password = "P09551_1_10";
	}

	public static Connection getConnection() throws Exception {

		if (conection == null) {
			Connection con = getConnection(ip, port, instance, user, password);
			if (con != null) {
				conection = con;
			}
		} else {
			if (dataChanged) {
				Connection con = getConnection(ip, port, instance, user, password);
				if (con != null) {
					conection = con;
					dataChanged = false;
				}
			}
		}
		return conection;
	}

	public static Connection getConnection(String ip, int port, String instance, String user, String password)
			throws Exception {
		Class.forName("oracle.jdbc.driver.OracleDriver");
		Connection con = DriverManager.getConnection("jdbc:oracle:thin:@" + ip + ":" + port + ":" + instance, user,
				password);
		return con;

	}

	public static String getIP() {
		return ip;
	}

	public static String getInstanceName() {
		return instance;
	}

	public static String getUser() {
		return user;
	}

	public static String getPassword() {
		return password;
	}

	
	public static int getPort() {
		return port;
	}

	public static void setConfigurations(String ip, String instance, int port, String user, String password,
			String students, String courses) {
		if (!ip.equals(BDFactory.ip) || !instance.equals(BDFactory.instance) || !user.equals(BDFactory.user)
				|| !password.equals(BDFactory.password)) {
			dataChanged = true;
		}

		if (dataChanged) {
			BDFactory.ip = ip;
			BDFactory.instance = instance;
			BDFactory.port = port;
			BDFactory.user = user;
			BDFactory.password = password;
	
		}
	}

//	private static void ConectarYConsultarBD(String username, String password) throws SQLException {
//		Connection con = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.103:1522:ESTUD", username, password);
//		con.close();
//	}
}
