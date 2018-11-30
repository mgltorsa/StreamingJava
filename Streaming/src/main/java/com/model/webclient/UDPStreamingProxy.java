package com.model.webclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;

import com.model.connection.IUDPListener;
import com.model.connection.UDPConnection;
import com.model.media.Media;
import com.model.media.Player;

public class UDPStreamingProxy extends ServiceProxy implements IUDPListener {

    
    //SAMPLE RATE 19140,2,4,19140,false
    private static String ADDRESS_STR = "228.5.6.7";
//    private static String ADDRESS_STR = "224.0.0.3";

    private static File tempStreaming;

    private InetAddress address;
    private UDPConnection connection;
    private AudioFormat format;
    private Player player;
    private boolean micro;

    public UDPStreamingProxy(Client client, String host, int port, boolean micro) {
	super(client, host, port);
	try {
	    address = InetAddress.getByName(host);
	} catch (UnknownHostException e) {
	    e.printStackTrace();
	}

	this.micro = micro;

    }

    public UDPStreamingProxy(Client client, int port, boolean micro) {
	this(client, ADDRESS_STR, port, micro);
    }

    @Override
    public synchronized void startConsume() {
	if (connection == null) {
	    connection = new UDPConnection(openSocket(), this);
	}
	try {
	    Media media = null;
	    if (micro) {
		player = new Player(format);
	    } else {
		media = new Media();
		initStreamingFile();
		media.setInputStream(new FileInputStream(tempStreaming));
		media.setAudioFormat(format);
		player = new Player(media, new FileOutputStream(tempStreaming));
		// TODO
		player.start();
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

	connection.start();
	notifyAll();

    }

    private void initStreamingFile() throws IOException {

	if (tempStreaming == null) {
	    tempStreaming = new File("./music/streaming-from-server.wav");
	    if (!tempStreaming.exists()) {
		tempStreaming.createNewFile();
	    }
	}

    }

    private DatagramSocket openSocket() {
	try {
	    MulticastSocket socket = new MulticastSocket(getPort());
	    socket.joinGroup(address);
	    return socket;
	} catch (IOException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public void setAudioFormat(AudioFormat format) {
	this.format = format;
    }

    public synchronized void onInputDatagram(DatagramPacket packet, IUDPListener callback) {

	byte[] in = packet.getData();
	
	player.play(in);

    }

    @Override
    public synchronized void setListen(boolean listen) {
	
	
	while (connection == null) {
	    try {
		wait();
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	this.connection.setListen(listen);
	player.setListen(listen);
    }

}
