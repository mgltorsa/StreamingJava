import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

//LIBRERIA UTILIZADA SUN.NET:HTTPSERVER.

public class Server implements HttpHandler {

	// SERVIDOR HTTP
	private HttpServer server;
	// SI ES CONEXIÖN SEGURA (NO IMPLEMENTADO AUN)
	private boolean isSecure;
	// PUERTO DONDE EL SERVIDOR ESTARA ESCUCHANDO
	private int port;
	// DB
	private static String dbURL = "jdbc:derby:myDB;create=true;user=me;password=mine";
	// DB CONNECTION
	private Connection conn;

	public Server(int port, boolean isSecure) {
		this.isSecure = isSecure;
		this.port = port;
	}

	public Server(int port) {
		this(port, false);
	}

	public void start() {
		try {
			createConnectionDB();
			// ABRE Y CONFIGURA EL SOCKET
//			updateDB();
			openSocket();

			server.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateDB() {
		try {
			insertIntoUsers("elhombre", "123");
			insertIntoUsers("joaquin.pepe", "245");
			insertIntoUsers("miguel", "lolo45");
			insertIntoUsers("pepe3312", "1314");

			insertIntoBets("elhombre", "Caballo 2", "35.0", "25-11-2018");
			insertIntoBets("elhombre", "Caballo 1", "45.0", "03-11-2014");
			insertIntoBets("elhombre", "Caballo 3", "37.0", "02-11-2015");
			insertIntoBets("elhombre", "Caballo 4", "20.0", "07-11-2016");
			insertIntoBets("elhombre", "Caballo 5", "15.0", "20-11-2017");

			insertIntoBets("joaquin.pepe", "Caballo 2", "23.0", "02-09-2016");
			insertIntoBets("joaquin.pepe", "Caballo 1", "100.0", "07-12-2013");
			insertIntoBets("joaquin.pepe", "Caballo 5", "15.0", "20-03-2014");

			insertIntoBets("miguel", "Caballo 4", "227.0", "12-04-2015");
			insertIntoBets("miguel", "Caballo 2", "1000.0", "07-02-2016");
			insertIntoBets("miguel", "Caballo 6", "33.0", "03-07-2017");

			insertIntoBets("pepe3312", "Caballo 3", "500.6", "12-06-2013");
			insertIntoBets("pepe3312", "Caballo 1", "100.5", "15-07-2016");
			insertIntoBets("pepe3312", "Caballo 5", "36.3", "24-12-2011");
			insertIntoBets("pepe3312", "Caballo 4", "15.0", "13-03-2012");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void insertIntoBets(String username, String horse, String bet, String date) throws SQLException {
		String insert = "INSERT INTO BETS_HORSES_ROADS VALUES ( '" + username + "','" + horse + "'," + bet + ",'" + date
				+ "')";
		Statement st = conn.createStatement();
		st.execute(insert);
	}

	private void insertIntoUsers(String username, String password) throws SQLException {
		String insert = "INSERT INTO USERS_HORSES_ROADS VALUES ( '" + username + "','" + password + "')";
		Statement st = conn.createStatement();
		st.execute(insert);
	}

	private void createConnectionDB() {
		try {
			conn = BDFactory.getConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void openSocket() throws Exception {
		// AUN NO SE IMPLEMENTA SEGURIDAD ---
		if (isSecure) {
			// TODO
//			server = HttpsServer.create(new InetSocketAddress(port), 0);
//			SSLContext sslContext = SSLContext.getInstance("TLS");

		} else {

			// CREA HTTPSERVER QUE ESCUCHA EN EL PUERTO {PORT}, EL 0 ES PARA INDICAR QUE
			// PUEDE ATENDER UN NUMERO
			// 'ILIMITADO' DE PETICIONES HTTP
			server = HttpServer.create(new InetSocketAddress(port), 0);

			// CREA EL CONTEXTO PARA EL SERVIDOR EJ: SI SE REALIZA LA CONSULTA INDEX.HTML
			// ENTONCES
			// EL URI ENTREGADO EN EL HTTP REQUEST SERA: /INDEX.HTML
			HttpContext context = server.createContext("/");
			// PONE EL CONTROLADOR DE LAS SOLICITUDES HTTP
			context.setHandler(this);

		}
	}

	// CONTROLADOR (MANEJADOR) DE SOLICITUDES HTTP

	public void handle(HttpExchange exchange) throws IOException {

		// REQUEST= METHODO SOLICITAD (GET, POST ETC)
		String request = exchange.getRequestMethod();
		System.out.println(exchange.getHttpContext());
		byte[] output = null;
		int status = 200;
		try {
			switch (request) {
			case "GET":

				output = processGet(exchange);
				break;

			case "POST":

				output = processPost(exchange);

				break;
			}

		} catch (Exception e) {
			status = 500;
			output = "Invalid login".getBytes();
			e.printStackTrace();
		} finally {
			exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

			OutputStream out = exchange.getResponseBody();

			exchange.sendResponseHeaders(status, output.length);

			out.write(output);

			out.close();
		}

	}

	private byte[] processPost(HttpExchange exchange) throws Exception {
		String readInput = "";
		byte[] response = null;

		BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));

		while (br.ready()) {
			readInput += br.readLine();
		}
		br.close();

		System.out.println("onInput : " + readInput);

		String[] infoData = parseData(readInput);

		System.out.println("post uri: " + exchange.getRequestURI());
		if (infoData.length == 1) {

			String res = getQueryRes(infoData[0]);
			response = res.getBytes();

		} else {

			String username = infoData[0];
			String password = infoData[1];
			if (validateLogin(username,password)) {
				response = getLogin(infoData[0]);
			
				String pathClient = "/account_"+username+".html";
				File f = new File("./files"+pathClient);
				if(!f.exists()){
					f.createNewFile();					
				}
				FileOutputStream fileOut = new FileOutputStream(f);
				fileOut.write(response);
				fileOut.close();
				response=pathClient.getBytes();
			} else {
				throw new Exception("Invalid Data");
			}

		}

		return response;
	}

	private String getQueryRes(String infoQuery) {
		// TEST RESPONSE
		String response = "Caballo 1:35:25-11-2018|Caballo 2:45:25-11-2017|Caballo3:27:25-11-2016";
		System.out.println("Infoq: " + infoQuery);
		response = getBetInfos(infoQuery);
		return response;
	}

	private byte[] getLogin(String username) {
		String scriptLoad = "function load(){$('#username').text(" + "'Username: " + username + "'" + ");"
				+ "var date = new Date();var showDate = date.getDate()+'-'+(date.getMonth()+1)+'-'+date.getFullYear();$('#date').text(showDate);$('#user_display').show();"
				+ "var datas = 'query=" + username + "'; var url = 'http://localhost:1234' ;"
				+ "var xhttpRequest = new XMLHttpRequest();xhttpRequest.open('POST', url, true);"
				+ "xhttpRequest.onreadystatechange = function() {if (xhttpRequest.readyState === 4) { if(xhttpRequest.status === 200)"
				+ " {var re = xhttpRequest.responseText;showRes(re);}}}; xhttpRequest.send(datas);" + "}";

		byte[] bytes = null;
		try {
			bytes = byteloadLoginPage();
		} catch (Exception e) {
			// TODO
		}

		Document document = Jsoup.parse(new String(bytes));
		bytes = addScriptInHtml(document, scriptLoad);

		return bytes;
	}

	private byte[] addScriptInHtml(Document document, String scriptOnload) {
		Charset charset = Charset.forName("UTF-8");
		document.charset(charset);
		Elements elements = document.getAllElements();
		Element lastElement = elements.get(elements.size() - 1);
		Tag tag = Tag.valueOf("script");
		Element newScript = new Element(tag, "");
		newScript.attr("type", "text/javascript");
		newScript.text(scriptOnload);
		lastElement.after(newScript);

		return document.toString().getBytes();

	}

	private byte[] byteloadLoginPage() throws Exception {

		return getFileResponse("/account.html");
	}

	private String[] parseData(String input) {
		String[] outputArray = null;

		if (input.contains("query")) {
			outputArray = new String[1];
			outputArray[0] = input.split("=")[1];
		} else {
			String[] inputArray = input.split("&");

			String username = inputArray[0].split("=")[1];
			String password = inputArray[1].split("=")[1];
			outputArray = new String[2];
			outputArray[0] = username;
			outputArray[1] = password;
		}
		return outputArray;
	}

	private boolean validateLogin(String username, String password) throws SQLException {

		boolean logged = false;
		Statement st = conn.createStatement();
		String queryLogin = "SELECT * FROM USERS_HORSES_ROADS WHERE username='" + username + "'";
		ResultSet rs = st.executeQuery(queryLogin);
		if (rs.next()) {
			String pass = rs.getString("password");
			if (pass.equals(password)) {
				logged = true;
			}
		}

		return logged;
	}

	private String getBetInfos(String username) {
		String response = "";
		try {
			Statement st = conn.createStatement();
			String query = "SELECT * FROM BETS_HORSES_ROADS WHERE username='" + username + "'";
			ResultSet s = st.executeQuery(query);

			while (s.next()) {
				String horse = s.getString("horse");
				double bet = s.getDouble("bet");
				Date date = s.getDate("Date_bet");
				response += horse + ":" + bet + ":" + date + "|";
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;
	}

	private byte[] processGet(HttpExchange exchange) {
		String path = exchange.getRequestURI().getPath();
		byte[] outputBytes = null;
		try {
			outputBytes = getFileResponse(path);
		} catch (Exception e) {

		}

		return outputBytes;
	}

	private byte[] getFileResponse(String path) throws Exception {

		byte[] outputBytes = null;

		// EN LA SOLICITUD HTTP SE CONSULTA POR UN ICOONO (NO FUNCIONA BIEN)

		// PARA CARGAR PAGINA PRINCIPAL
		// REALPATH = ./files/index.thml;
		String realPath = "./files" + path;

		if (path.equals("/favico.ico")) {

		} else {
			String data = "";
			// SE LEE EL ARCHIVO INDEX.HTML
			FileReader fr = new FileReader(new File(realPath));
			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				data += br.readLine();
			}

			br.close();
			// SE OBTIENE LOS BYTES DEL ARCHIVO
			outputBytes = data.getBytes();
		}

		return outputBytes;
	}

	public static void main(String[] args) {
		Server s = new Server(1234);
		s.start();
	}
}
