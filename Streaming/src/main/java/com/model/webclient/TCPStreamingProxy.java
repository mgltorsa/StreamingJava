package com.model.webclient;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;

import com.model.media.Player;

public class TCPStreamingProxy extends ServiceProxy {

    private Socket connection;
    private AudioFormat format;
    private Player player;
    private boolean listen;

    public TCPStreamingProxy(Client client, String host, int port) {
	super(client, host, port);
	this.listen = false;
    }

    @Override
    public void startConsume() {
	this.listen = true;

	if (connection == null) {
	    new Thread(new Runnable() {

		public void run() {
		    connection = openSocket();
		    player = new Player(format);
		    init();
		}
	    }).start();

	}

	init();

    }

    private void init() {

	new Thread(new Runnable() {

	    public void run() {
		while (connection != null && !connection.isClosed()) {
		    int bytesReaded = 0;
		    byte[] bytes = new byte[1024];

		    try {

			bytesReaded = connection.getInputStream().read(bytes);
			if (bytesReaded == -1) {
			    break;
			}
			if (listen && player != null) {
			    player.playRaw(bytes);
			}
		    } catch (IOException e) {
			break;
		    }
		}
	    }
	}).start();

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

    @Override
    public void setListen(boolean listen) {
	this.listen = listen;
    }

}
