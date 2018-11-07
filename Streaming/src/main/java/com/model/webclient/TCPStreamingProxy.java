package com.model.webclient;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;

import com.model.media.Player;

public class TCPStreamingProxy extends ServiceProxy {

    private Socket connection;
    private AudioFormat format;

    public TCPStreamingProxy(Client client, String host, int port) {
	super(client, host, port);
    }

    @Override
    public void startConsume() {

	connection = openSocket();
	Player p = new Player(format);
	while (!connection.isClosed()) {
	    int bytesReaded = 0;
	    byte[] bytes = new byte[1024];
	    if (bytesReaded == -1) {
		break;
	    }
	    try {

		bytesReaded = connection.getInputStream().read(bytes);
		p.playRaw(bytes);
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }

    public void setAudioFormat(AudioFormat format) {
	this.format = format;
    }

    private Socket openSocket() {
	Socket socket = null;

	try {
	    socket = new Socket(getHost(), getPort());
	} catch (UnknownHostException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return socket;
    }

}
