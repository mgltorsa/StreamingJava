package com.model.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

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

    public static void main(String[] args) {
	Scanner sc = new Scanner(System.in);
	while (true) {
	    String n = sc.nextLine();
	    if (n.equals("stop")) {
		break;
	    } else if (n.equals("del")) {
		gotoxy(0, 0);
	    } else {
		System.out.println(n);
	    }

	}
	sc.close();
    }

    static void gotoxy(int line, int row) {
	System.out.print("\033[" + line + ";" + row + "H");
    }

}
