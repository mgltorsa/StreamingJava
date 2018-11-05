package com.model.webclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.model.connection.ITCPListener;
import com.model.media.Media;

public class Client implements ITCPListener {

    private HashMap<Integer, ServiceProxy> mapServices;
    private HashSet<ServiceProxy> services;
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

    public Client(String host, int port) {
	mapServices = new HashMap<Integer, ServiceProxy>();
	services = new HashSet<ServiceProxy>();
	this.port = port;
	this.host = host;
    }

    public void start() {

	initTCPServices();

	initUDPServices();
    }

    private void initTCPServices() {
	initClientInput();
	QueriesProxy queriesProxy = new QueriesProxy(this, host, port, false);
	addService(queriesProxy);
	queriesProxy.startConsume();
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
			} else {
			    processInputClient(line);
			}

		    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }

		}

	    }
	}).start();

    }

    public void processInputClient(String data) {
	((QueriesProxy) mapServices.get(port)).processInputClient(data);
    }

    private void initUDPServices() {
	processInputClient("request-type:query");
	processInputClient("query:streaming-audio-format");
	processInputClient("service-on-port:5556");
	processInputClient("send");

	processInputClient("request-type:query");
	processInputClient("query:streaming-audio-format");
	processInputClient("service-on-port:5557");
	processInputClient("send");

    }

    private void addService(ServiceProxy service) {
	mapServices.put(service.getPort(), service);
	services.add(service);
    }

    public void onInputMessageData(String data, ITCPListener callback) {
	JsonParser parser = new JsonParser();
	JsonObject response = (JsonObject) parser.parse(data);
	String status = response.get("status").getAsString();
	if (status.contains("200") || status.contains("OK") || status.equals("200 OK")) {
	    String query = response.get("query").getAsString();
	    if (query.equals("streaming-audio-format")) {
		int port = response.get("service-on-port").getAsInt();
		initStreamingProxy(response, port);
	    } else {
		printReponseFromServer(response);
	    }
	}

    }

    private void initStreamingProxy(JsonObject audioInfo, int srcPort) {

	int port = -1;
	boolean micro =false;
	// SRCPORT == MICROPHONE STREAMING PORT
	if (srcPort == 5556) {
	    port = 6666;
	    micro=true;
	}

	// SRCPORT == MUSIC STREAMING PORT
	else if (srcPort == 5557) {
	    port = 6667;
	}
	if (mapServices.containsKey(port)) {
	    System.out.println("Already exist service in port: " + port);
	    System.out.println("Info server and audio format:");
	    printReponseFromServer(audioInfo);
	} else {

	    StreamingProxy streamingProxy = new StreamingProxy(this, port,micro);
	    streamingProxy.setAudioFormat(Media.parseFormat(audioInfo));
	    addService(streamingProxy);
	    streamingProxy.startConsume();
	}

    }

    private void printReponseFromServer(JsonObject audioInfo) {
	System.out.println(audioInfo.toString());

    }

    public static void main(String[] args) {
	Client client = new Client();
	client.start();
    }
}
