package com.model.connection;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPConnection extends Thread implements ITCPListener {

    private Socket socket;
    private ITCPListener listener;
    private PrintWriter out;
    private ReaderThread reader;

    public TCPConnection(Socket socket, ITCPListener listener) {
	this.socket = socket;
	this.listener = listener;
	openOutputStream();
    }

    public void setListen(boolean listen) {
	reader.setListen(listen);
    }

    @Override
    public void run() {
	initReaderThread();

    }

    private void initReaderThread() {
	reader = new ReaderThread(socket,this);
	reader.init();
    }

    public void onReadData(String input) {
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

    public String getAddress() {

	return socket.getInetAddress().getHostAddress();
    }

    

}
