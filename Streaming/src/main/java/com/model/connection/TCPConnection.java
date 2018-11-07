package com.model.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPConnection extends Thread implements ITCPListener {

    private Socket socket;
    private ITCPListener listener;
    private BufferedReader in;
    private PrintWriter out;

    public TCPConnection(Socket socket, ITCPListener listener) {
	this.socket = socket;
	this.listener = listener;
	openInputStream();
	openOutputStream();
    }

    @Override
    public void run() {
	initReaderThread();

    }

    private void initReaderThread() {
	Thread t = new Thread(new Runnable() {

	    public void run() {
		String input = null;
		while (!socket.isClosed()) {
		    try {
			while (((input = in.readLine()) != null) && !socket.isClosed()) {
			    onReadData(input);
			}
		    } catch (IOException e) {
			break;
		    }
		}
	    }

	});
	t.start();

    }

    private void onReadData(String input) {
	listener.onInputMessageData(input, this);

    }

    public void onInputMessageData(String input, ITCPListener callback) {
	if (!socket.isClosed()) {
	    out.println(input);
	}
    }
    
    public boolean isClosed() {
	return socket.isClosed();
    }

    private void openOutputStream() {

	try {
	    out = new PrintWriter(socket.getOutputStream(), true);
	} catch (IOException e) {
	    System.out.println("Exception in openOutput TCPConnection");
	    e.printStackTrace();
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

    public String getAddress() {

	return socket.getInetAddress().getHostAddress();
    }

}
