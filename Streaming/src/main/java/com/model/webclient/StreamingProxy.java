package com.model.webclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.model.connection.IUDPListener;
import com.model.connection.UDPConnection;

public class StreamingProxy extends ServiceProxy implements IUDPListener {

    private static String ADDRESS_STR = "228.5.6.7";

    private InetAddress address;
    private AudioFormat format;
    private SourceDataLine sourceLine;
    private UDPConnection connection;

    public StreamingProxy(Client client, String host, int port) {
	super(client, host, port);
	try {
	    address = InetAddress.getByName(host);
	} catch (UnknownHostException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public StreamingProxy(Client client, int port) {
	this(client, ADDRESS_STR, port);
    }

    @Override
    public void startConsume() {
	connection = new UDPConnection(openSocket(), this);
	DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
	try {
	    sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
	    sourceLine.open(format);
	    sourceLine.start();
	} catch (LineUnavailableException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	connection.start();

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

    public void onInputDatagram(DatagramPacket packet, IUDPListener callback) {
	byte[] in = packet.getData();
	ByteArrayOutputStream r = new ByteArrayOutputStream();
	r.write(in, 0, in.length);
	r.toByteArray();
	sourceLine.write(in, 0, in.length);
    }

}
