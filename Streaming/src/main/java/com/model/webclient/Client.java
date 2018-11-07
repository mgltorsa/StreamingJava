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

//	float sampleRate = 16000f;
//	int sampleSizeInBits = 16;
//	int channels = 2;
//	boolean bigEndian = false;
//	boolean signed = true;
//	AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
//
//	try {
//	    MulticastSocket s = new MulticastSocket(8001);
//	    String ADDRESS_STR = "224.0.0.3";
//	    DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
//	    SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
//	    sourceLine.open(format);
//	    sourceLine.start();
//	    s.joinGroup(InetAddress.getByName(ADDRESS_STR));
//
//	    while (true) {
//		byte[] bytes = new byte[10000];
//		DatagramPacket p = new DatagramPacket(bytes, bytes.length);
//		s.receive(p);
//		byte[] b =p.getData();
//		System.out.println(b);
//		sourceLine.write(b, 0, b.length);
//
//	    }
//
//	} catch (Exception e) {
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	}

//	StreamingProxy a = new StreamingProxy(this, 8001, true);
//	a.setAudioFormat(format);
//	a.startConsume();
    }

    private void initTCPServices() {
	initClientInput();
	QueriesProxy queriesProxy = new QueriesProxy(this, host, port, false);
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
			} else {
			    processInputClient(line);
			}

		    } catch (IOException e) {
			e.printStackTrace();
		    }

		}

	    }
	}).start();

    }

    public void processInputClient(String data) {

	((QueriesProxy) mapServices.get(port)).processInputClient(data);

    }

    public void initStreamingServices() {
//	processInputClient("request-type:query");
//	processInputClient("query:streaming-audio-format");
//	processInputClient("service-on-port:5556");
	processInputClient("query=streaming-audio-format,service-on-port:5556");
//	processInputClient("send");

//	processInputClient("request-type:query");
//	processInputClient("query:streaming-audio-format");
//	processInputClient("service-on-port:5557");
	processInputClient("query=streaming-audio-format,service-on-port:5557");
//	processInputClient("send");

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
			printRoadStatus(response.get("road-status").getAsJsonObject());
		    } else {
			printRawReponseFromServer(response);
		    }
		} else if (requestType.equals("bet")) {
		    printRawReponseFromServer(response);
		} else {
		    printRawReponseFromServer(response);
		}
	    }
	} catch (Exception e) {
	    System.out.println(data);
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

    private void initStreamingProxy(JsonObject audioInfo, int srcPort) {

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
	    } else if (type.equalsIgnoreCase("UDP")) {
		service = new StreamingProxy(this, port, micro);
		((StreamingProxy) service).setAudioFormat(Media.parseFormat(audioInfo));
	    }

	    addService(service);
	    service.start();
	}

    }

    private void printRawReponseFromServer(JsonObject audioInfo) {
	System.out.println(audioInfo.toString());

    }

    public static void main(String[] args) {
	Client client = new Client();
	client.start();
    }
}
