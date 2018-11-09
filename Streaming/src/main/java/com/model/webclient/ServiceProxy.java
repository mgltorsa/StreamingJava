package com.model.webclient;

import java.io.IOException;
import java.net.UnknownHostException;

public abstract class ServiceProxy {

    private int port;
    private String host;
    private Client client;

    public ServiceProxy(Client client, String host, int port) {
	this.host = host;
	this.port = port;
	this.client = client;
    }

    public String getHost() {
	return host;
    }

    public void setHost(String host) {
	this.host = host;
    }

    public int getPort() {
	return port;
    }

    public Client getClient() {
	return client;
    }

    public abstract void startConsume() throws UnknownHostException, IOException;

    public abstract void setListen(boolean listen);

    public void start() throws UnknownHostException, IOException {
	startConsume();

    }
}
