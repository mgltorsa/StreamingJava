package com.model.horses;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import com.google.gson.JsonObject;

public class Road {

    public static final double advanceTime = 20;

    public static final int NOT_STARTED_ROAD = 0;
    public static final int STARTED_ROAD = 1;
    public static final int FINISHED_ROAD = 1;

    // HORSE NUMBER - HORSE
    private HashMap<Integer, Horse> horses;

    // ADDRES - HORSE
    private HashMap<String, Horse> bets;

    private int state;

    private double distance;

    public Road() {
	this(0, 1000);
    }

    public Road(int numHorses) {
	this(numHorses, 10000000);
    }

    public Road(int numHorses, double distance) {
	horses = new HashMap<Integer, Horse>();
	bets = new HashMap<String, Horse>();
	for (int i = 1; i <= numHorses; i++) {
	    horses.put(i, new Horse(i));
	}
	state = 0;
	this.distance = distance;
    }

    public String bet(String bettor, int horse, double quantity) {
	Horse bettorHorse = bets.get(bettor);
	String response = "";
	if (state == NOT_STARTED_ROAD || state == STARTED_ROAD) {
	    if (bettorHorse != null) {
		if (bettorHorse.getId() != horse) {
		    response = "La apuesta no se puede realizar a un caballo diferente.\n"
			    + "El identificador de su caballo es: " + bettorHorse.getId();
		} else {
		    response = addBet(horse, quantity);
		}
	    } else {
		horses.get(horse).addBet(quantity);
		bets.put(bettor, horses.get(horse));
		response = "Apuesta exitosa a " + horse + " - " + horses.get(horse).getName();
	    }
	} else {
	    response = "La carrera ya ha finalizado, no puede realizar mas apuestas";
	}
	return response;
    }

    private String addBet(int horseSrc, double quantity) {
	String response = "";
	try {
	    Horse horse = horses.get(horseSrc);
	    if (horse != null) {
		horse.addBet(quantity);
		response = "apuesta exitosa a " + horseSrc;
	    } else {
		response = "No existe el caballo con identificador " + horseSrc;
	    }
	} catch (IllegalArgumentException e) {
	    response = e.getMessage();
	}
	return response;
    }

    public JsonObject getInfo() {
	return getInfo(null);
    }

    public void init() {
	state++;
	new Thread(new Runnable() {

	    public void run() {
		try {
		    Thread.sleep((long) (advanceTime * 45));
		    ArrayList<Horse> hrs = new ArrayList<Horse>(horses.values());
		    while (true) {
			int horsesThatFinished = 0;
			for (int i = 0; i < hrs.size(); i++) {
			    Horse h = hrs.get(i);
			    if (h.getDistanceRoaded() >= distance) {
				horsesThatFinished++;
			    } else if (h.advance(advanceTime) > distance) {
				horsesThatFinished++;
			    }
			}
			if (horsesThatFinished == hrs.size()) {
			    state++;
			    break;
			}
			Thread.sleep((long) (advanceTime * 10));

		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}

	    }
	}).start();
    }

    public JsonObject getInfo(String bettor) {
	JsonObject json = new JsonObject();
	Horse targetHorse = null;
	json.addProperty("distance", distance);
	if (bettor != null) {
	    if (!bets.containsKey(bettor)) {
		json.addProperty("Advertencia", "No has apostador por ningún caballo");
	    } else {
		targetHorse = bets.get(bettor);
	    }
	}

	ArrayList<Horse> horses = getHorseByOrder();
	json.addProperty("estado", (state == 0 ? "no iniciada" : state == 1 ? "iniciada" : "finalizada"));
	json.addProperty("cantidad-caballos", horses.size());
	json.addProperty("formato",
		"Posición en la carrera : Caballo - Identificador - velocidadActual - distancia recorrida - apuestas / observaciones");

	DecimalFormat ff = new DecimalFormat("#.##");
	for (int i = 0; i < horses.size(); i++) {
	    Horse h = horses.get(i);

	    String info = h.getName() + " - " + h.getId() + " - " + ff.format(h.getSpeed()) + " - "
		    + ff.format(h.getDistanceRoaded()) + " - " + ff.format(h.getBet());

	    if (targetHorse != null && targetHorse.getId() == h.getId()) {
		info += " - " + "<- Su caballo";
	    }

	    json.addProperty((i + 1) + "", info);

	}

	return json;

    }

    private ArrayList<Horse> getHorseByOrder() {
	ArrayList<Horse> horses = new ArrayList<Horse>(this.horses.values());
	Comparator<Horse> comp = new Comparator<Horse>() {

	    public int compare(Horse o1, Horse o2) {

		return -Double.compare(o1.getDistanceRoaded(), o2.getDistanceRoaded());
	    }
	};

	horses.sort(comp);
	return horses;
    }

}
