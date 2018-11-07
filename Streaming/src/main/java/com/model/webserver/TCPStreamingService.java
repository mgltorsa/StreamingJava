package com.model.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import com.model.media.Media;

public class TCPStreamingService extends Service {

    private ArrayList<Socket> listeners;
    private volatile Media media;
    private Thread streamingThread;

    public TCPStreamingService(Server server, int port) {
	super(server, port);
	listeners = new ArrayList<Socket>();

    }

    @Override
    public void startService() {
	ServerSocket serverSocket = openSocket();
	while (true) {
	    try {
		System.out.println("listening");
		Socket socket = serverSocket.accept();

		listeners.add(socket);

	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

    }

    public void initStreaming() {

	if (streamingThread == null) {
	    streamingThread = new Thread(new Runnable() {

		public void run() {
		    while (true) {

			byte[] bytes = new byte[1024];
			try {
			    int bytesReaded = media.getInputStream().read(bytes);
			    if (bytesReaded > 0) {
				for (int i = 0; i < listeners.size(); i++) {
				    Socket listener = listeners.get(i);
				    if (listener.isClosed()) {
					listeners.remove(i);
				    } else {
					listener.getOutputStream().write(bytes);
				    }
				}
			    } else if (bytesReaded == -1) {
				media = getServer().assignNextMedia();
			    }
			} catch (IOException e) {

			    e.printStackTrace();
			}

		    }

		}
	    });

	    streamingThread.start();
	}
    }

    public void setMedia(Media media) {
	this.media = media;
    }

    private ServerSocket openSocket() {
	ServerSocket socket = null;
	try {
	    socket = new ServerSocket(getPort());
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	return socket;
    }

    @Override
    public void stopService() {
	try {
	    streamingThread.interrupt();
	    this.interrupt();
	} catch (Exception e) {

	}

    }

    public Media getMedia() {
	return media;
    }

    public static void main(String[] args) throws UnknownHostException, IOException {

	new Thread(new Runnable() {

	    public void run() {
		ServerSocket s;
		try {
		    s = new ServerSocket(5555);

		    while (true) {
			System.out.println("listening");
			Socket so = s.accept();
			System.out.println("accepted " + so.getInetAddress().getHostAddress() + " on " + so.getPort());
		    }
		} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}

	    }
	}).start();

    }

}
