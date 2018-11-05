package com.model.webserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import com.model.connection.IUDPListener;
import com.model.connection.UDPConnection;
import com.model.media.Media;

public class StreamingService extends Service implements IUDPListener {

    private static String ADDRESS_STR = "228.5.6.7";

    private InetAddress address;
    private Media media;
    private int sendPort;
    private UDPConnection connection;

    public StreamingService(Server server, String host, int port, int sendPort) {
	this(server, host, port, sendPort, true);
    }

    public StreamingService(Server server, int port, int sendPort, boolean transfer) {
	this(server, ADDRESS_STR, port, sendPort, transfer);
    }

    public StreamingService(Server server, String host, int port, int sendPort, boolean transfer) {
	super(server, port);
	try {
	    address = InetAddress.getByName(host);
	} catch (UnknownHostException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	this.sendPort = sendPort;

    }

    public int getSendPort() {
	return sendPort;
    }

    @Override
    public void startService() {
	init();

    }

    private void init() {
	if (connection == null) {
	    connection = new UDPConnection(openSocket(), this);
	}

	byte[] data = null;
//	if(media.getBufferSize()<1024) {
//	    data = new byte[media.getBufferSize()];
//	}
//	else {
	data = new byte[1024];
//	}
	int numBytesRead;

	while (true) {

	    try {
		numBytesRead = media.getInputStream().read(data, 0, data.length);

		if (numBytesRead == -1) {
		    break;
		}

		DatagramPacket packet = new DatagramPacket(data, data.length);		
		packet.setAddress(address);
		packet.setPort(getSendPort());
		connection.onInputDatagram(packet, this);
		
	    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	media.reload();
	init();

	

    }

    public Media getMedia() {
	return media;
    }

    public void setMedia(Media media) {
	this.media = media;
    }

    private DatagramSocket openSocket() {
	try {
	    MulticastSocket socket = new MulticastSocket(getPort());
	    socket.joinGroup(address);
	    return socket;
	} catch (IOException e) {
	    return null;
	}

    }

    public void onInputDatagram(DatagramPacket packet, IUDPListener callback) {
	// TODO

    }

}
