package com.model.webclient;

import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

import com.google.gson.JsonObject;
import com.model.connection.ITCPListener;
import com.model.connection.TCPConnection;

public class QueriesProxy extends ServiceProxy implements ITCPListener {

    public static final String TRUSTTORE_LOCATION = "C:/Users/Asus/Desktop/Desktop/clientTrusted.jks";

    private TCPConnection connection;
    private JsonObject json;
    private boolean activateSSL;

    public QueriesProxy(Client client, String host, int port, boolean activateSSL) {
	super(client, host, port);
	json = new JsonObject();
	this.activateSSL = activateSSL;
	if (activateSSL) {
	    System.setProperty("javax.net.ssl.trustStore", TRUSTTORE_LOCATION);

	}
    }

    @Override
    public void startConsume() {
	connection = new TCPConnection(openSocket(), this);
	connection.start();
    }

    private Socket openSocket() {
	Socket socket = null;
	try {
	    if (activateSSL) {
		SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
		socket = sf.createSocket(getHost(), getPort());
	    } else {
		socket = new Socket(getHost(), getPort());
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return socket;
    }

    public void onInputMessageData(String data, ITCPListener callback) {
	getClient().onInputMessageData(data, callback);
    }

    public void processInputClient(String data) {
	if (data.equalsIgnoreCase("send")) {
	    this.connection.onInputMessageData(json.toString(), this);
	    json = new JsonObject();
	} else {
	    String[] info = data.split(":");
	    String property = info[0].trim();
	    String value = info[1].trim();
	    json.addProperty(property, value);
	}

    }

}
