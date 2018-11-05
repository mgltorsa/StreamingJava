package com.model.horses;

import java.util.Random;

public class Horse {

    public final static Random rs = new Random();

    private int id;
    private String name;
    private double bet;
    private double speed;
    private double distanceRoaded;

    public Horse(int id, String name) {
	this.id = id;
	this.name = name;
	this.speed = rs.nextDouble() * 60;
    }

    public Horse(int id) {
	this(id, "Caballo-" + id);
    }

    public void addBet(double bet) {
	if (bet <= 0) {
	    throw new IllegalArgumentException("bet for horse must be > 0");
	}
	this.bet += bet;
    }

    public String getName() {
	return name;
    }

    public int getId() {
	return id;
    }

    public double getBet() {
	return bet;
    }

    public double getSpeed() {
	return speed;
    }

    public double getDistanceRoaded() {
	return distanceRoaded;
    }

    public double advance(double time) {
	speed = rs.nextDouble() * 7;
	distanceRoaded += speed * time;
	return distanceRoaded;
    }
}
