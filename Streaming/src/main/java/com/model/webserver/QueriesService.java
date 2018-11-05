package com.model.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import javax.net.ssl.SSLServerSocketFactory;

import com.model.connection.ITCPListener;
import com.model.connection.TCPConnection;

public class QueriesService extends Service implements ITCPListener{

    private HashMap<String, TCPConnection> connections;
    private boolean activeSSL;

    public QueriesService(Server server, int port, boolean activeSSL) {
	super(server, port);
	this.activeSSL=activeSSL;
	connections=new HashMap<String, TCPConnection>();
    }

    @Override
    public void startService() {
	ServerSocket serverSocket = openServerSocket();
	while(true) {
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
	getServer().onInputMessageData(data,callback);	
    }

  
}
