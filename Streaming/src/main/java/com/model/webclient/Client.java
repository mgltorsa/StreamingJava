package com.model.webclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.model.connection.ITCPListener;
import com.model.media.Media;

public class Client implements ITCPListener {

    private HashMap<Integer, ServiceProxy> mapServices;
    private HashSet<ServiceProxy> services;
    private ArrayList<ITCPListener> listenersOnClient;
    private int port;
    private String host;

    public Client(String host) {
	this(host, 5555);
    }

    public Client(int port) {
	this("localhost", port);
    }

    public Client() {
	this("localhost", 5555);
    }

    public void addListener(ITCPListener listener) {
	this.listenersOnClient.add(listener);
    }

    public Client(String host, int port) {
	mapServices = new HashMap<Integer, ServiceProxy>();
	services = new HashSet<ServiceProxy>();
	listenersOnClient = new ArrayList<ITCPListener>();
	this.port = port;
	this.host = host;
    }

    public void start() throws UnknownHostException, IOException {

	initTCPServices();

    }

    private void initTCPServices() throws UnknownHostException, IOException {
	initClientInput();
	TCPQueriesProxy queriesProxy = new TCPQueriesProxy(this, host, port, true);
	addService(queriesProxy);
	queriesProxy.start();
    }

    private void initClientInput() {
	new Thread(new Runnable() {

	    public void run() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
		    String line;
		    try {
			line = br.readLine();
			if (line.equalsIgnoreCase("stop")) {
			    break;
			} else if (line.equalsIgnoreCase("l")) {
			    mapServices.get(5557).setListen(false);
			} else if (line.equalsIgnoreCase("n")) {
			    mapServices.get(5557).setListen(true);
			}

			else {
			    processInputClient(line);
			}

		    } catch (IOException e) {
			e.printStackTrace();
		    }

		}

	    }
	}).start();

    }

    public void processInputClient(String data) throws IllegalArgumentException {

	((TCPQueriesProxy) getServiceOnPort(port)).processInputClient(data);

    }

    public void initStreamingServices() {
	processInputClient("query=streaming-audio-format,service-on-port:5556");
	processInputClient("query=streaming-audio-format,service-on-port:5557");
    }

    private void addService(ServiceProxy service) {
	mapServices.put(service.getPort(), service);
	services.add(service);
    }

    public void onInputMessageData(String data, ITCPListener callback) {
	JsonParser parser = new JsonParser();
	try {
	    JsonObject response = (JsonObject) parser.parse(data);
	    String status = response.get("status").getAsString();
	    if (status.contains("200") || status.contains("OK") || status.equals("200 OK")) {
		String requestType = response.get("request-type").getAsString();
		if (requestType.equals("query")) {
		    String query = response.get("query").getAsString();
		    if (query.equals("streaming-audio-format")) {
			int port = response.get("service-on-port").getAsInt();
			initStreamingProxy(response, port);
		    } else if (query.equals("road-status")) {
			if (listenersOnClient.isEmpty()) {
			    printRoadStatus(response.get("road-status").getAsJsonObject());
			}
		    } else {
			printRawReponseFromServer(response);
		    }
		} else if (requestType.equals("bet")) {
		    printRawReponseFromServer(response);
		} else {
		    printRawReponseFromServer(response);
		}
	    }

	    callbackListeners(data);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.out.println(data);
	}

    }

    private void callbackListeners(String data) {
	for (int i = 0; i < listenersOnClient.size(); i++) {
	    listenersOnClient.get(i).onInputMessageData(data, this);
	}
    }

    private void printRoadStatus(JsonObject json) {
	String toPrint = "";
	toPrint += "Distancia: " + json.get("distance").getAsDouble() + "\n";
	toPrint += "Estado: " + json.get("estado").getAsString() + "\n";
	toPrint += "Formato:\n" + json.get("formato").getAsString() + "\n";
	int horses = json.get("cantidad-caballos").getAsInt();
	for (int i = 1; i <= horses; i++) {
	    toPrint += i + " : " + json.get(i + "").getAsString() + "\n";
	}

	System.out.println(toPrint);

    }

    private void initStreamingProxy(JsonObject audioInfo, int srcPort) throws UnknownHostException, IOException {

	int port = -1;
	boolean micro = false;
	String type = audioInfo.get("connection-type").getAsString();
	// SRCPORT == MICROPHONE STREAMING PORT
	if (srcPort == 5556) {
	    port = 6666;
	    micro = true;
	}

	// SRCPORT == MUSIC STREAMING PORT
	else if (srcPort == 5557) {
	    port = 6667;
	}
	if (mapServices.containsKey(port)) {
	    System.out.println("Already exist service in port: " + port);
	    System.out.println("Info server and audio format:");
	    printRawReponseFromServer(audioInfo);
	} else {
	    ServiceProxy service = null;
	    if (type.equalsIgnoreCase("TCP")) {
		service = new TCPStreamingProxy(this, host, srcPort);
		((TCPStreamingProxy) service).setAudioFormat(Media.parseFormat(audioInfo));
		services.add(service);
		mapServices.put(port, service);
	    } else if (type.equalsIgnoreCase("UDP")) {
		service = new UDPStreamingProxy(this, port, micro);
		((UDPStreamingProxy) service).setAudioFormat(Media.parseFormat(audioInfo));
		addService(service);

	    }

	    service.start();
	}

    }

    private void printRawReponseFromServer(JsonObject audioInfo) {
	System.out.println(audioInfo.toString());

    }

    public static void main(String[] args) throws UnknownHostException, IOException {
	Client client = new Client();
	client.start();
    }

    public void bet(String horse, double bet) {
	String query = "bet=" + bet + ",horse-id:" + horse;
	executeQuery(query);

    }

    public void executeQuery(String query) throws IllegalArgumentException {
	processInputClient(query);
    }

    public ServiceProxy getServiceOnPort(int proxyPort) throws IllegalArgumentException {
	ServiceProxy proxy = mapServices.get(proxyPort);
	if (proxy == null) {
	    throw new IllegalArgumentException("There is no proxy on port");
	}
	return proxy;

    }

    public void setListenOnPort(int proxyPort, boolean listen) throws IllegalArgumentException {
	ServiceProxy proxy = mapServices.get(proxyPort);
	if (proxy == null) {
	    throw new IllegalArgumentException("There is no proxy on port " + proxyPort);
	}
	proxy.setListen(listen);
    }

    public void setRoadStreamingListen(boolean listen) {
	TCPQueriesProxy proxy = (TCPQueriesProxy) getServiceOnPort(5555);
	proxy.setRoadStreaming(listen);

    }
}
