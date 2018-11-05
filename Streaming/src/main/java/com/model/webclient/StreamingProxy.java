package com.model.webclient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
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
    private FileOutputStream output;
    private FileInputStream input;

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
	try {
	    File file = File.createTempFile("MusicFromServer", ".wav", new File("./music"));
	    input = new FileInputStream(file);
	    output = new FileOutputStream(file);
	    DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);

	    sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
	    sourceLine.open(format);
	    sourceLine.start();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	connection.start();
	initPlayThread();

    }

    private void initPlayThread() {

	new Thread(new Runnable() {

	    public void run() {
		byte[] bytes = new byte[1024];
		try {
		    while (input.read(bytes) == -1) {
			Thread.sleep(500);
		    }
		    int bytesRead = 0;
		    while (true) {
			if (bytesRead != -1) {
			    sourceLine.write(bytes, 0, bytes.length);
			    bytesRead = input.read(bytes, 0, bytes.length);
			}

		    }

		} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	}).start();

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
	// TODO
	try {
	    output.write(in);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

}
