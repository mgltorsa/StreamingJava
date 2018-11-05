package com.model.webserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.model.connection.ITCPListener;
import com.model.media.Media;
import com.model.media.Microphone;

public class Server implements ITCPListener {

    private HashMap<Integer, Service> mapServices;
    private HashSet<Service> services;
    private ArrayList<Media> medias;
    private int currentMedia;

    public Server() {
	mapServices = new HashMap<Integer, Service>();
	services = new HashSet<Service>();
	medias = new ArrayList<Media>();
	currentMedia = 0;
    }

    public void start() {
	initUDPServices();
	initTCPServices();
	System.out.println("finish services");

    }

    private void initTCPServices() {
	QueriesService queriesService = new QueriesService(this, 5555, false);
	queriesService.start();
	addService(queriesService);

    }

    private void initUDPServices() {

	// INIT MICROPHONE STREAMING

	StreamingService microphoneStreaming = new StreamingService(this, 5556, 6666, true);
	Media microphone = new Microphone();
	microphoneStreaming.setMedia(microphone);
	microphone.start();
	microphoneStreaming.start();
	addService(microphoneStreaming);

	// INIT MEDIA STREAMING

	StreamingService mediaStreaming = new StreamingService(this, 5557, 6667, true);
	generateMediaList();

	Media media = medias.get(currentMedia);
	media.start();
	mediaStreaming.setMedia(media);
	mediaStreaming.start();
	addService(mediaStreaming);

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

    public static void main(String[] args) {
	Server s = new Server();
	s.start();
    }

    public void onInputMessageData(String data, ITCPListener callback) {
	JsonParser parser = new JsonParser();
	JsonObject response = new JsonObject();

	try {
	    JsonObject json = (JsonObject) parser.parse(data);
	    String requestType = json.get("request-type").getAsString();
	    if (requestType.equals("query")) {
		response = computeQuery(json);
	    }

	} catch (JsonParseException e) {
	    
	}
	callback.onInputMessageData(response.toString(), this);
    }

    private JsonObject computeQuery(JsonObject json) {
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
	} else {
	    response.addProperty("status", "400 Bad Request");
	    response.addProperty("info", "not yet implemented");
	}

	return response;

    }

    public Media assignNextMedia() {
	nextMedia();
	medias.get(currentMedia).start();
	return medias.get(currentMedia);

    }

}
