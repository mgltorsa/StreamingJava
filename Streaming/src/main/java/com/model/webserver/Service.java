package com.model.webserver;

public abstract class Service extends Thread{

    private int port;
    private Server server;

    public Service(Server server, int port) {
	this.server = server;
	this.port = port;
    }

    @Override
    public void run() {
        startService();
    }
    public abstract void startService();

    public int getPort() {
	return port;
    }

    public Server getServer() {
	return server;
    }
    
    public abstract void stopService();
    
}
