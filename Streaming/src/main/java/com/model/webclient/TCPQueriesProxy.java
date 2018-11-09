package com.model.webclient;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocketFactory;

import com.model.connection.ITCPListener;
import com.model.connection.TCPConnection;

public class TCPQueriesProxy extends ServiceProxy implements ITCPListener {

    public static final String TRUSTTORE_LOCATION = "./docs/key.jks";

    private TCPConnection connection;
    private boolean activateSSL;
    private boolean roadStreaming;

    public TCPQueriesProxy(Client client, String host, int port, boolean activateSSL) {
	super(client, host, port);
	this.activateSSL = activateSSL;
	if (activateSSL) {
	    System.setProperty("javax.net.ssl.trustStore", TRUSTTORE_LOCATION);
	}

    }

    @Override
    public void startConsume() throws UnknownHostException, IOException {

	connection = new TCPConnection(openSocket(), this);
	connection.start();
	getClient().initStreamingServices();
    }

    private Socket openSocket() throws UnknownHostException, IOException {
	Socket socket = null;

	if (activateSSL) {
	    SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
	    socket = sf.createSocket(getHost(), getPort());
	} else {
	    socket = new Socket(getHost(), getPort());
	}

	return socket;

    }

    public void onInputMessageData(String data, ITCPListener callback) {
	getClient().onInputMessageData(data, callback);
    }

    public void processInputClient(String data) {
	connection.onInputMessageData(data, this);
    }

    public void setListen(boolean b) {
	connection.setListen(b);
    }

    public void setRoadStreaming(boolean listen) {
	boolean oldListen = this.roadStreaming;
	this.roadStreaming = listen;
	if (listen && !oldListen) {
	    initRoadStreaming();
	}
    }

    private void initRoadStreaming() {
	new Thread(new Runnable() {

	    public void run() {
		while (roadStreaming && connection != null && !connection.isClosed()) {
		    processInputClient("query=road-status");
		    try {
			Thread.sleep(500);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		}

	    }
	}).start();

    }

}
