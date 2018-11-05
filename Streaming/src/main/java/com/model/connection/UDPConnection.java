package com.model.connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPConnection extends Thread implements IUDPListener {

    private byte[] buf;
    private DatagramSocket socket;
    private IUDPListener listener;

    public UDPConnection(DatagramSocket socket, IUDPListener listener) {
	this.socket = socket;
	buf = new byte[1024];
	this.listener = listener;
    }

    @Override
    public void run() {
	while (!socket.isClosed()) {
	    DatagramPacket inPacket = new DatagramPacket(buf, buf.length);
	    try {
		socket.receive(inPacket);
		onReadData(inPacket);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    private void onReadData(DatagramPacket packet) {
	listener.onInputDatagram(packet, this);

    }

    public synchronized void onInputDatagram(DatagramPacket packet, IUDPListener callback) {
	if (!socket.isClosed()) {
	    try {
		socket.send(packet);
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

    }

}
