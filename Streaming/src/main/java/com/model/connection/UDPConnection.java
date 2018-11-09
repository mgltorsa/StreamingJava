package com.model.connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPConnection extends Thread implements IUDPListener {

    private byte[] buf;
    private DatagramSocket socket;
    private IUDPListener listener;
    private boolean listen;

    public UDPConnection(DatagramSocket socket, IUDPListener listener) {
	this.socket = socket;
	buf = new byte[10000];
	this.listener = listener;
	this.listen = true;
    }

    public void setListen(boolean listen) {
	boolean oldListen = this.listen;
	this.listen = listen;
	if (listen && !oldListen) {
	    this.listen = listen;
	    init();
	}
    }

    private void init() {
	new Thread(new Runnable() {

	    public void run() {
		while (!socket.isClosed() && listen) {
		    DatagramPacket inPacket = new DatagramPacket(buf, buf.length);
		    try {
			socket.receive(inPacket);
			onReadData(inPacket);
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		}

	    }
	}).start();
    }

    public void setBufferSize(int bufferSize) {
	buf = new byte[bufferSize];
    }

    @Override
    public void run() {
	init();
    }

    private void onReadData(DatagramPacket packet) {
	listener.onInputDatagram(packet, this);

    }

    public synchronized void onInputDatagram(DatagramPacket packet, IUDPListener callback) {
	if (!socket.isClosed()) {
	    try {
		socket.send(packet);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}

    }

}
