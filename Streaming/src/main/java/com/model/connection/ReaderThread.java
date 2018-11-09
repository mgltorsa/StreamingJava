package com.model.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ReaderThread {

    private Socket socket;
    private BufferedReader in;
    private boolean listen;
    private TCPConnection listener;

    public ReaderThread(Socket socket, TCPConnection connection) {
	this.socket = socket;
	this.listener = connection;

	openInputStream();
	listen = true;
    }

    public void setListen(boolean listen) {
	boolean oldListen = this.listen;
	this.listen = listen;
	if (listen && !oldListen) {
	    this.listen = listen;
	    init();
	}

    }

    private void openInputStream() {
	try {
	    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	} catch (IOException e) {
	    System.out.println("Exception in openInput TCPConnection");
	    e.printStackTrace();
	}

    }

    public void init() {
	new Thread(new Runnable() {

	    public void run() {
		String input = null;
		while (!socket.isClosed() && listen) {
		    try {
			while (((input = in.readLine()) != null) && !socket.isClosed()) {
			    onReadData(input);
			}
		    } catch (IOException e) {
			break;
		    }
		}
	    }

	}).start();

    }

    private void onReadData(String input) {
	listener.onReadData(input);
    }

}