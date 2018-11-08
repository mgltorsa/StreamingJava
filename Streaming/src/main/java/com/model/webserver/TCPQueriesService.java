package com.model.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import javax.net.ssl.SSLServerSocketFactory;

import com.model.connection.ITCPListener;
import com.model.connection.TCPConnection;

public class QueriesService extends Service implements ITCPListener {

    public static final String KEYSTORE_LOCATION = "./docs/key.jks";
    public static final String KEYSTORE_PASSWORD = "password";

    private HashMap<String, TCPConnection> connections;
    private boolean activeSSL;

    public QueriesService(Server server, int port, boolean activeSSL) {
	super(server, port);
	this.activeSSL = activeSSL;
	if (activeSSL) {
	    System.setProperty("javax.net.ssl.keyStore", KEYSTORE_LOCATION);
	    System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD);
	}
	connections = new HashMap<String, TCPConnection>();
    }

    @Override
    public void startService() {
	ServerSocket serverSocket = openServerSocket();
	while (true) {
	    try {
		Socket socket = serverSocket.accept();

		TCPConnection connection = new TCPConnection(socket, this);
		connections.put(connection.getAddress(), connection);
		connection.start();

	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

    }

    private ServerSocket openServerSocket() {
	ServerSocket serverSocket = null;
	try {
	    if (activeSSL) {
		SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		serverSocket = ssf.createServerSocket(getPort());

	    } else {
		serverSocket = new ServerSocket(getPort());
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return serverSocket;

    }

    public void onInputMessageData(String data, ITCPListener callback) {
	getServer().onInputMessageData(data, callback);
    }

    @Override
    public void stopService() {
	// TODO Auto-generated method stub
	
    }
    
    public static void main(String[] args) {
	
    }

}
