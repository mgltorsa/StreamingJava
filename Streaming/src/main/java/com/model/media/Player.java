package com.model.media;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.SourceDataLine;

public class Player extends Thread {

//    public static Info[] mixerInfo;
//    private static int currentInfo = 0;

    private SourceDataLine sourceLine;
    private OutputStream output;
    private InputStream input;

    public Player(AudioFormat format) {

//	if (mixerInfo == null) {
//	    Info[] infos = AudioSystem.getMixerInfo();
//	    mixerInfo = new Info[2];
//	    mixerInfo[0] = infos[0];
//	    mixerInfo[1] = infos[1];
//	}
//	    Mixer m = AudioSystem.getMixer(mixerInfo[currentInfo]);
//	    sourceLine = AudioSystem.getSourceDataLine(format, mixerInfo[currentInfo++]);

	DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
	try {
	    sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
	    sourceLine.open(format);
	    sourceLine.start();
	} catch (LineUnavailableException e) {
	    e.printStackTrace();
	}
    }

    public Player(Media media, OutputStream stream) {

	this(media.getAudioFormat());
	output = stream;
	input = media.getInputStream();

    }

    @Override
    public void run() {

	byte[] bytes = new byte[1024];
	setRestVolume(30.0f);

	try {
	    sleep(500);
	    int bytesReaded = 0;
	    while (true) {
		if (bytesReaded != -1) {
		    bytesReaded = input.read(bytes, 0, bytes.length);
		    sourceLine.write(bytes, 0, bytes.length);
		}
	    }
	} catch (Exception e) {

	}
    }

    private void setRestVolume(float f) {
	((FloatControl) sourceLine.getControl(FloatControl.Type.MASTER_GAIN)).setValue(-f);
    }

    public synchronized void play(byte[] bytes) {
	try {
	    output.write(bytes, 0, bytes.length);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public synchronized void playRaw(byte[] bytes) {
	sourceLine.write(bytes, 0, bytes.length);
    }

    public static void main(String[] args) {
	Info[] infos = AudioSystem.getMixerInfo();
	for (Info inf : infos) {
	    System.out.println(inf);
	}
    }
}
