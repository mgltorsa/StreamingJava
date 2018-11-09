package com.controller;

public class HorsesUpdater implements Runnable {

    
    private Controller controller;
    private double distance;
    private String state;
    private String[][] horses;
    
    
    public HorsesUpdater(double distance, String state, String[][] horses, Controller controller) {
	this.distance=distance;
	this.state=state;
	this.horses=horses;
	this.controller=controller;
    }

    public void run() {
	controller.setDistanceLabel(distance + "");
	controller.setRoadState(state);
	controller.setHorsesView(horses);
    }

}
