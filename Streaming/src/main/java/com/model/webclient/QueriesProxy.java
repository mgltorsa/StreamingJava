package com.model.webclient;

import java.net.Socket;

import com.google.gson.JsonObject;
import com.model.connection.ITCPListener;
import com.model.connection.TCPConnection;

public class QueriesProxy extends ServiceProxy implements ITCPListener {

    private TCPConnection connection;
    private JsonObject json;

    public QueriesProxy(Client client, String host, int port) {
	super(client, host, port);
	json = new JsonObject();
    }

    @Override
    public void startConsume() {
	connection = new TCPConnection(openSocket(), this);
	connection.start();
    }

    private Socket openSocket() {
	Socket socket = null;

	try {
	    socket = new Socket(getHost(), getPort());

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
	    json= new JsonObject();
	} else {
	    String[] info = data.split(":");
	    String property = info[0].trim();
	    String value = info[1].trim();
	    json.addProperty(property, value);
	}

    }

}
