package com.model.webserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.model.connection.ITCPListener;
import com.model.connection.TCPConnection;
import com.model.horses.Road;
import com.model.media.Media;
import com.model.media.Microphone;

public class Server implements ITCPListener {

    private HashMap<Integer, Service> mapServices;
    private HashSet<Service> services;
    private ArrayList<Media> medias;
    private int currentMedia;
    private Road road;

    public Server() {
	road = new Road(6);
	mapServices = new HashMap<Integer, Service>();
	services = new HashSet<Service>();
	medias = new ArrayList<Media>();
	currentMedia = 0;
    }

    public void start() {
	initStreamingServices();
	initTCPServices();
	System.out.println("finish services");
	initRoad();
    }

    private void initTCPServices() {
	QueriesService queriesService = new QueriesService(this, 5555, false);
	queriesService.start();
	addService(queriesService);

    }

    private void initStreamingServices() {

	// INIT MICROPHONE STREAMING

	UDPStreamingService microphoneStreaming = new UDPStreamingService(this, 5556, 6666, true);
	Media microphone = new Microphone();
	microphoneStreaming.setMedia(microphone);
	microphone.start();
//	microphoneStreaming.start();
	addService(microphoneStreaming);

	// INIT MEDIA STREAMING

	generateMediaList();
	Media media = medias.get(currentMedia);
	media.start();

	TCPStreamingService mediaStreaming = new TCPStreamingService(this, 5557);

	mediaStreaming.setMedia(media);
	mediaStreaming.start();
	addService(mediaStreaming);

//	UDPStreamingService mediaStreaming = new UDPStreamingService(this, 5557, 6667, true);
//
//	media.start();
//	mediaStreaming.setMedia(media);
//	mediaStreaming.start();
//	addService(mediaStreaming);

    }

    private void generateMediaList() {
	medias.add(new Media("./music/Van Halen Jump.wav"));
	medias.add(new Media("./music/Norihiro Tsuru Last Carnival.wav"));
    }

    private void nextMedia() {
	currentMedia++;
	if (currentMedia >= medias.size()) {
	    currentMedia = 0;
	}
    }

    private void addService(Service service) {
	services.add(service);
	mapServices.put(service.getPort(), service);
    }

    public static void main(String[] args) throws Exception {
	Server s = new Server();
	s.start();

    }

    public void onInputMessageData(String data, ITCPListener callback) {
	JsonParser parser = new JsonParser();
	JsonObject response = new JsonObject();

	try {
	    JsonObject json = (JsonObject) parser.parse(data);
	    response = computeJsonRequest(json, (TCPConnection) callback);

	} catch (JsonParseException e) {
	    response = computeRequest(data, (TCPConnection) callback);
	}

	callback.onInputMessageData(response.toString(), this);
    }

    private JsonObject computeRequest(String data, TCPConnection connection) {
	String[] info = data.split("=");
	JsonObject json = new JsonObject();
	String requestType = info[0];
	json.addProperty("request-type", requestType);
	if (requestType.equalsIgnoreCase("query") || requestType.equalsIgnoreCase("bet")) {

	    String[] infoQuery = info[1].split(",");
	    String queryContent = infoQuery[0];
	    json.addProperty(requestType.toLowerCase(), queryContent);
	    for (int i = 1; i < infoQuery.length; i++) {
		String[] infos = infoQuery[i].split(":");
		json.addProperty(infos[0], infos[1]);
	    }

	}
	return computeJsonRequest(json, connection);
    }

    private void initRoad() {
	new Thread(new Runnable() {

	    public void run() {
		try {
		    Thread.sleep(3000);
		    road.init();
		    ((TCPStreamingService) mapServices.get(5557)).initStreaming();
		} catch (InterruptedException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}

	    }
	}).start();
    }

    private JsonObject computeJsonRequest(JsonObject json, TCPConnection connection) {
	JsonObject response = new JsonObject();

	String requestType = json.get("request-type").getAsString();
	try {
	    if (requestType.equals("query")) {

		String query = json.get("query").getAsString();

		if (query.trim().equals("streaming-audio-format")) {
		    int port = json.get("service-on-port").getAsInt();
		    Service service = mapServices.get(port);
		    if (service instanceof UDPStreamingService) {
			response = ((UDPStreamingService) service).getMedia().getJsonAudioFormat();
			response.addProperty("status", "200 OK");
			response.addProperty("connection-type", "UDP");
			response.addProperty("service-on-port", port);
		    } else if (service instanceof TCPStreamingService) {
			response = ((TCPStreamingService) service).getMedia().getJsonAudioFormat();
			response.addProperty("status", "200 OK");
			response.addProperty("connection-type", "TCP");
			response.addProperty("service-on-port", port);
		    } else {
			response.addProperty("status", "400 Bad Request");
			response.addProperty("info", "not streaming in port requested");
		    }
		} else if (query.equals("road-status")) {
		    response.addProperty("status", "200 OK");
		    response.add("road-status", road.getInfo(connection.getAddress()));
		}
		response.addProperty("query", query);

	    } else if (requestType.equals("bet")) {
		int horse = json.get("horse-id").getAsInt();
		String bettor = connection.getAddress();
		double bet = json.get("bet").getAsDouble();
		String betResponse = road.bet(bettor, horse, bet);
		response.addProperty("status", "200 OK");
		response.addProperty("bet-response", betResponse);

	    } else {
		response.addProperty("status", "400 Bad Request");
		response.addProperty("info", "invalid request type");
	    }
	} catch (Exception e) {
	    response.addProperty("status", "500 Internal error");
	    response.addProperty("info", e.getMessage());
	}

	response.addProperty("request-type", requestType);

	return response;

    }

    public Media assignNextMedia() {
	nextMedia();
	medias.get(currentMedia).start();
	return medias.get(currentMedia);

    }

}
