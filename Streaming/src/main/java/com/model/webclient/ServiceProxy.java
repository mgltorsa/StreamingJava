package com.model.webclient;

public abstract class ServiceProxy extends Thread{

    private int port;
    private String host;
    private Client client;

  

    public ServiceProxy(Client client,String host, int port) {
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

    public abstract void startConsume();
    
    @Override
    public void run() {
        startConsume();
    }
}
