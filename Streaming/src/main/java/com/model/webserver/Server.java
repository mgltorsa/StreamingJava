package com.model.webserver;

import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.model.connection.ITCPListener;
import com.model.media.Media;

public class Server implements ITCPListener {

    private HashMap<Integer, Service> mapServices;
    private HashSet<Service> services;

    public Server() {
	mapServices = new HashMap<Integer, Service>();
	services = new HashSet<Service>();
    }

    public void start() {
	initUDPServices();
	System.out.println("finish udp services");
	initTCPServices();
	System.out.println("finish tcp services");

    }

    private void initTCPServices() {
	QueriesService queriesService = new QueriesService(this, 5555, false);
	addService(queriesService);
	queriesService.start();

    }

    private void initUDPServices() {

	// INIT MICROPHONE STREAMING
	
//	StreamingService microphoneStreaming = new StreamingService(this, 5556, 6666, true);
//	Media microphone = new Microphone();
//	microphoneStreaming.setMedia(microphone);
//	microphone.start();
//	microphoneStreaming.start();
//	addService(microphoneStreaming);

	// INIT MEDIA STREAMING

	StreamingService mediaStreaming = new StreamingService(this, 5557, 6667, true);
	Media media = new Media("./music/Van Halen - Jump.wav");
	media.start();
	mediaStreaming.setMedia(media);
	mediaStreaming.start();
	addService(mediaStreaming);

    }

    private void addService(Service service) {
	services.add(service);
	mapServices.put(service.getPort(), service);
    }

    public static void main(String[] args) {
	Server s = new Server();
	s.start();
    }

    public void onInputMessageData(String data, ITCPListener callback) {
	JsonParser parser = new JsonParser();
	JsonObject json = (JsonObject) parser.parse(data);
	JsonObject response = new JsonObject();
	String query = json.get("query").getAsString();
	if (query.trim().equals("streaming-audio-format")) {
	    int port = json.get("service-on-port").getAsInt();
	    Service service = mapServices.get(port);
	    if (service instanceof StreamingService) {
		response = ((StreamingService) service).getMedia().getJsonAudioFormat();
		response.addProperty("status", "200 OK");
		response.addProperty("query", query);
		response.addProperty("service-on-port", port);
	    } else {
		response.addProperty("status", "400 Bad Request");
		response.addProperty("info", "not streaming in port requested");
	    }
	}

	callback.onInputMessageData(response.toString(), this);
    }

}
