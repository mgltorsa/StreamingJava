package com.model.webclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

    private static File tempStreaming;

    private InetAddress address;
    private UDPConnection connection;
    private OutputStream outputStream;
    private FileInputStream inputStream;
    private AudioFormat format;
    private Player player;
    private boolean micro;
    private SourceDataLine sourceLine;

    public StreamingProxy(Client client, String host, int port, boolean micro) {
	super(client, host, port);
	try {
	    address = InetAddress.getByName(host);
	} catch (UnknownHostException e) {
	    e.printStackTrace();
	}

    }

    public StreamingProxy(Client client, int port, boolean micro) {
	this(client, ADDRESS_STR, port, micro);
    }

    @Override
    public void startConsume() {
	if (connection == null) {
	    connection = new UDPConnection(openSocket(), this);
	}

	try {
	    if (!micro) {
		initStreamingFile();
	    } else {
		DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);

		sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
		sourceLine.open(format);
		sourceLine.start();
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

	connection.start();
	if (!micro) {
	    initPlayThread();
	}

    }

    private void initStreamingFile() throws IOException {

	if (tempStreaming == null) {
	    tempStreaming = new File("./music/streaming-from-server.wav");
	    if(!tempStreaming.exists()) {
		tempStreaming.createNewFile();
	    }
	}

	outputStream = new FileOutputStream(tempStreaming);

	inputStream = new FileInputStream(tempStreaming);

    }

    private void initPlayThread() {

	if (player == null) {
	    player = new Player(format);
	    player.start();
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
	try {
	    if (micro) {
		sourceLine.write(in, 0, in.length);
	    } else {
		outputStream.write(in);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public class Player extends Thread {

	private SourceDataLine sourceLine;

	public Player(AudioFormat format) {

	    DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);

	    try {
		sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
	    } catch (LineUnavailableException e) {
		e.printStackTrace();
	    }
	}

	@Override
	public void run() {
	    byte[] bytes = new byte[1024];
	    try {
		sleep(500);
		sourceLine.open();
		sourceLine.start();
		int bytesReaded = 0;
		while (true) {
		    if (bytesReaded != -1) {

			bytesReaded = inputStream.read(bytes, 0, bytes.length);
			sourceLine.write(bytes, 0, bytes.length);

		    }
		}
	    } catch (Exception e) {

	    }
	}
    }

}
